/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.deltahex.delta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.exbin.deltahex.delta.list.DefaultDoublyLinkedList;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.utils.binary_data.OutOfBoundsException;

/**
 * Delta document defined as sequence of segments.
 *
 * @version 0.1.1 2016/10/19
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDocument implements EditableBinaryData {

    private final SegmentsRepository repository;
    private FileDataSource fileSource;
    private final DefaultDoublyLinkedList<DataSegment> segments = new DefaultDoublyLinkedList<>();

    private long dataLength = 0;
    private final DataPointer pointer = new DataPointer();

    public DeltaDocument(SegmentsRepository repository, FileDataSource fileSource) throws IOException {
        this.repository = repository;
        this.fileSource = fileSource;
        dataLength = fileSource.getFileLength();
        DataSegment fullFileSegment = repository.createFileSegment(fileSource, 0, dataLength);
        segments.add(fullFileSegment);
        pointer.setPointer(0, fullFileSegment);
    }

    public DeltaDocument(SegmentsRepository repository) {
        this.repository = repository;
        dataLength = 0;
        pointer.setPointer(0, null);
    }

    // Temporary method for accessing data pages
    public DefaultDoublyLinkedList<DataSegment> getSegments() {
        return segments;
    }

    @Override
    public boolean isEmpty() {
        return dataLength == 0;
    }

    @Override
    public long getDataSize() {
        return dataLength;
    }

    @Override
    public byte getByte(long position) {
        focusSegment(position);

        if (pointer.segment instanceof FileSegment) {
            return ((FileSegment) pointer.segment).getByte(((FileSegment) pointer.segment).getStartPosition() + (position - pointer.position));
        } else {
            return ((MemorySegment) pointer.segment).getByte(position - pointer.position);
        }
    }

    @Override
    public void setByte(long position, byte value) {
        focusSegment(position);

        if (pointer.segment instanceof FileSegment) {
            if (pointer.position != position) {
                splitSegment(position);
                focusSegment(position);
            }

            DataSegment prev = segments.prevTo(pointer.segment);
            if (prev instanceof MemorySegment) {
                repository.setMemoryByte((MemorySegment) prev, prev.getLength(), value);
            } else {
                MemorySegment segment = repository.createMemorySegment(repository.openMemorySource(), 0, 0);
                repository.setMemoryByte(segment, 0, value);
                segments.addBefore(pointer.segment, segment);
            }
            pointer.position++;
            FileSegment documentSegment = ((FileSegment) pointer.segment);
            documentSegment.setStartPosition(documentSegment.getStartPosition() + 1);
            if (documentSegment.getLength() == 1) {
                segments.remove(documentSegment);
                repository.dropSegment(documentSegment);
            } else {
                documentSegment.setLength(documentSegment.getLength() - 1);
            }
        } else {
            if (pointer.segment == null) {
                pointer.segment = repository.createMemorySegment(repository.openMemorySource(), 0, 0);
                segments.add(pointer.segment);
            }
            repository.setMemoryByte((MemorySegment) pointer.segment, position - pointer.position, value);
        }

        if (position >= dataLength) {
            dataLength = position + 1;
        }
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        if (length == 0) {
            return;
        }

        focusSegment(startFrom);
        dataLength += length;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertUninitializedMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, length);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertUninitializedMemoryData((MemorySegment) insertedSegment, 0, length);
            if (pointer.segment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, long length) {
        if (length == 0) {
            return;
        }

        focusSegment(startFrom);
        dataLength += length;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, length);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, length);
            if (pointer.segment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        if (insertedData.length == 0) {
            return;
        }

        focusSegment(startFrom);
        dataLength += insertedData.length;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData);
            if (pointer.segment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        focusSegment(startFrom);
        dataLength += insertedDataLength;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData);
            if (pointer.segment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        if (insertedData.isEmpty()) {
            return;
        }

        focusSegment(startFrom);
        dataLength += insertedData.getDataSize();
        if (insertedData instanceof DeltaDocument) {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }

            // Copy all segments from inserted document
            DeltaDocument document = (DeltaDocument) insertedData;
            DataSegment segment = document.segments.first();
            DataSegment copy = repository.copySegment(segment);
            DataSegment first = copy;
            segments.addBefore(pointer.segment, copy);
            DataSegment next = segment.getNext();
            while (next != null) {
                DataSegment nextCopy = repository.copySegment(next);
                segments.addAfter(copy, nextCopy);
                copy = nextCopy;
                next = next.getNext();
            }
            pointer.segment = first;
            tryMergeArea(startFrom, insertedData.getDataSize());
        } else if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData);
        } else {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData);
            if (pointer.segment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        if (insertedDataLength == 0) {
            return;
        }

        focusSegment(startFrom);
        dataLength += insertedDataLength;
        if (insertedData instanceof DeltaDocument) {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }

            // Copy all segments from inserted document
            DeltaDocument document = (DeltaDocument) insertedData;

            long position = insertedDataOffset;
            long length = insertedDataLength;
            DataSegment segment = document.getPartCopy(position, length);
            position += segment.getLength();
            length -= segment.getLength();
            DataSegment first = segment;
            if (pointer.segment == null) {
                segments.add(segment);
            } else {
                segments.addBefore(pointer.segment, segment);
            }
            DataSegment next = segment.getNext();
            while (length > 0) {
                DataSegment nextSegment = document.getPartCopy(position, length);
                position += nextSegment.getLength();
                length -= nextSegment.getLength();
                segments.addAfter(segment, nextSegment);
                segment = nextSegment;
                next = next.getNext();
            }
            pointer.segment = first;
            tryMergeArea(startFrom, insertedData.getDataSize());
        } else if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData, insertedDataOffset, insertedDataLength);
        } else {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData, insertedDataOffset, insertedDataLength);
            if (pointer.segment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
        }
    }

    public void insert(long startFrom, DataSegment segment) {
        focusSegment(startFrom);
        if (pointer.position < startFrom) {
            splitSegment(startFrom);
            focusSegment(startFrom);
        }
        if (pointer.segment == null) {
            segments.add(0, segment);
        } else {
            segments.addAfter(pointer.segment, segment);
        }
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillData(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(long startFrom, long length) {
        if (startFrom + length > dataLength) {
            throw new OutOfBoundsException("Removed area is out of bounds");
        }

        if (length > 0) {
            dataLength -= length;
            focusSegment(startFrom + length);
            splitSegment(startFrom + length);
            focusSegment(startFrom);
            splitSegment(startFrom);
            focusSegment(startFrom);

            // Save position to return to
            DataSegment prevSegment = (DataSegment) pointer.segment.getPrev();
            long prevPointerPosition = prevSegment == null ? 0 : pointer.position - prevSegment.getLength();

            // Drop all segments in given range
            while (length > 0) {
                length -= pointer.segment.getLength();
                DataSegment next = segments.nextTo(pointer.segment);
                repository.dropSegment(pointer.segment);
                segments.remove(pointer.segment);
                pointer.segment = next;
            }

            // Set pointer position
            pointer.segment = prevSegment;
            pointer.position = prevPointerPosition;
            tryMergeSegments(startFrom);
        }
    }

    @Override
    public void clear() {
        pointer.position = 0;
        pointer.segment = null;
        dataLength = 0;
        for (DataSegment segment : segments) {
            repository.dropSegment(segment);
        }
        segments.clear();
    }

    public void destroy() {
        repository.dropDocument(this);
    }

    @Override
    public void loadFromStream(InputStream in) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long loadFromStream(InputStream in, long l, long l1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveToStream(OutputStream out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BinaryData copy() {
        DeltaDocument copy = repository.createDocument();
        copy.dataLength = dataLength;
        for (DataSegment segment : segments) {
            copy.segments.add(repository.copySegment(segment));
        }
        return copy;
    }

    @Override
    public BinaryData copy(long startFrom, long length) {
        DeltaDocument copy = repository.createDocument();
        copy.dataLength = length;
        focusSegment(startFrom);

        DataSegment segment = pointer.segment;
        long offset = startFrom - pointer.position;
        while (length > 0) {
            long segmentLength = segment.getLength();
            long copyLength = segmentLength - offset;
            if (copyLength > length) {
                copyLength = length;
            }

            if (offset == 0 && copyLength == segmentLength) {
                copy.segments.add(repository.copySegment(pointer.segment));
            } else if (pointer.segment instanceof MemorySegment) {
                MemorySegment memorySegment = (MemorySegment) pointer.segment;
                copy.segments.add(repository.createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + offset, copyLength));
            } else {
                FileSegment fileSegment = (FileSegment) pointer.segment;
                copy.segments.add(repository.createFileSegment(fileSegment.getSource(), fileSegment.getStartPosition() + offset, copyLength));
            }
            length -= copyLength;
            offset = 0;
            segment = segments.nextTo(segment);
        }

        return copy;
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        // TODO optimalization later
        for (int i = 0; i < length; i++) {
            target[offset + i] = getByte(startFrom + i);
        }
    }

    @Override
    public OutputStream getDataOutputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getDataInputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDataSize(long dataSize) {
        if (dataSize < dataLength) {
            remove(dataSize, dataLength - dataSize);
        } else if (dataSize > dataLength) {
            insert(dataLength, dataSize - dataLength);
        }
    }

    /**
     * Splits current pointer segment on given absolute position.
     *
     * @param position split position
     */
    public void splitSegment(long position) {
        if (position < pointer.position || position > pointer.position + pointer.segment.getLength()) {
            throw new IllegalStateException("Split position is out of current segment");
        }

        if (pointer.position == position || pointer.segment.getStartPosition() + pointer.segment.getLength() == position) {
            // No action needed
            return;
        }

        long firstPartSize = position - pointer.position;
        if (pointer.segment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) pointer.segment;
            MemorySegment newSegment = repository.createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + firstPartSize, memorySegment.getLength() - firstPartSize);
            memorySegment.setLength(firstPartSize);
            segments.addAfter(pointer.segment, newSegment);
        } else {
            FileSegment fileSegment = (FileSegment) pointer.segment;
            FileSegment newSegment = repository.createFileSegment(fileSegment.getSource(), fileSegment.getStartPosition() + firstPartSize, fileSegment.getLength() - firstPartSize);
            fileSegment.setLength(firstPartSize);
            segments.addAfter(pointer.segment, newSegment);
        }
    }

    public DataSegment getSegment(long position) {
        focusSegment(position);
        return pointer.segment;
    }

    /**
     * Returns segment starting from given position or copy of part of the
     * segment starting from given position up to the end of length.
     *
     * @param position position
     * @return data segment
     */
    public DataSegment getPartCopy(long position, long length) {
        focusSegment(position);
        if (pointer.segment == null) {
            return null;
        }

        long offset = position - pointer.position;
        long partLength = length;
        if (pointer.segment.getLength() - offset < partLength) {
            partLength = pointer.segment.getLength() - offset;
        }
        return repository.copySegment(pointer.segment, offset, partLength);
    }

    /**
     * Focuses segment starting at or before given position and ending after it.
     *
     * Returns null if position is at the end of the document and throws out of
     * bounds exception otherwise.
     *
     * @param position requested position
     * @throws OutOfBoundsException if position is before or after document
     */
    private void focusSegment(long position) {
        if (position == 0) {
            pointer.position = 0;
            pointer.segment = segments.first();
            return;
        } else if (position == dataLength) {
            pointer.position = dataLength;
            pointer.segment = null;
            return;
        } else if (position < 0 || position > dataLength) {
            throw new OutOfBoundsException("Position index out of range");
        }

        if (position < pointer.position) {
            if (pointer.segment == null && position == dataLength) {
                pointer.segment = segments.last();
                if (pointer.segment == null) {
                    throw new IllegalStateException("Unexpected null segment");
                }
                pointer.position -= pointer.segment.getLength();
            }

            while (position < pointer.position) {
                pointer.segment = (DataSegment) segments.prevTo(pointer.segment);
                if (pointer.segment == null) {
                    throw new IllegalStateException("Unexpected null segment");
                }
                pointer.position -= pointer.segment.getLength();
            }
        } else {
            while (position >= pointer.position + pointer.segment.getLength()) {
                if ((pointer.segment.getNext() == null) && (position == pointer.position + pointer.segment.getLength())) {
                    break;
                }
                pointer.position += pointer.segment.getLength();
                pointer.segment = (DataSegment) segments.nextTo(pointer.segment);
                if (pointer.segment == null) {
                    throw new IllegalStateException("Unexpected null segment");
                }
            }
        }
    }

    private void tryMergeArea(long position, long length) {
        tryMergeSegments(position);
        // TODO
        tryMergeSegments(position + length);
    }

    /**
     * Attempts to merge segments at specified position.
     *
     * @param position target position
     */
    private boolean tryMergeSegments(long position) {
        if (position == 0 || position >= getDataSize()) {
            return false;
        }

        focusSegment(position);
        DataSegment nextSegment = pointer.segment;
        focusSegment(position - 1);
        DataSegment segment = pointer.segment;
        if (segment == nextSegment) {
            return false;
        }

        if (segment instanceof FileSegment && nextSegment instanceof FileSegment) {
            if (((FileSegment) segment).getStartPosition() + segment.getLength() == ((FileSegment) nextSegment).getStartPosition()) {
                ((FileSegment) segment).setLength(segment.getLength() + nextSegment.getLength());
                repository.dropSegment(nextSegment);
                segments.remove(nextSegment);
                return true;
            }
        }

        if (segment instanceof MemorySegment && nextSegment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) segment;
            MemorySegment nextMemorySegment = (MemorySegment) nextSegment;
            if (memorySegment.getSource() == nextMemorySegment.getSource()) {
                if (memorySegment.getStartPosition() + segment.getLength() == nextMemorySegment.getStartPosition()) {
                    memorySegment.setLength(segment.getLength() + nextSegment.getLength());
                    repository.dropSegment(nextSegment);
                    segments.remove(nextSegment);
                    return true;
                }
            }
            // TODO join two single memory segments?
        }

        return false;
    }

    public FileDataSource getFileSource() {
        return fileSource;
    }

    public void setFileSource(FileDataSource fileSource) {
        this.fileSource = fileSource;
    }

    /**
     * POJO structure for sliding data pointer.
     */
    private static class DataPointer {

        long position;
        DataSegment segment;

        void setPointer(long position, DataSegment segment) {
            this.position = position;
            this.segment = segment;
        }
    }
}
