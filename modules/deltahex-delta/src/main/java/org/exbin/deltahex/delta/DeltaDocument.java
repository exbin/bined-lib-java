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
 * @version 0.1.1 2016/10/16
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDocument implements EditableBinaryData {

    private final SegmentsRepository repository;
    private FileDataSource fileSource;
    private final DefaultDoublyLinkedList<DataSegment> segments = new DefaultDoublyLinkedList<>();

    private long dataLength = 0;
    private long pointerPosition;
    private DataSegment pointerSegment;

    public DeltaDocument(SegmentsRepository repository, FileDataSource fileSource) throws IOException {
        this.repository = repository;
        this.fileSource = fileSource;
        dataLength = fileSource.getFileLength();
        DataSegment fullFileSegment = repository.createFileSegment(fileSource, 0, dataLength);
        segments.add(fullFileSegment);
        pointerPosition = 0;
        pointerSegment = fullFileSegment;
    }

    public DeltaDocument(SegmentsRepository repository) {
        this.repository = repository;
        dataLength = 0;
        pointerPosition = 0;
        pointerSegment = null;
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

        if (pointerSegment instanceof FileSegment) {
            return ((FileSegment) pointerSegment).getByte(((FileSegment) pointerSegment).getStartPosition() + (position - pointerPosition));
        } else {
            return ((MemorySegment) pointerSegment).getByte(position - pointerPosition);
        }
    }

    @Override
    public void setByte(long position, byte value) {
        focusSegment(position);

        if (pointerSegment instanceof FileSegment) {
            if (pointerPosition != position) {
                splitSegment(position);
                focusSegment(position);
            }

            DataSegment prev = segments.prevTo(pointerSegment);
            if (prev instanceof MemorySegment) {
                repository.setMemoryByte((MemorySegment) prev, prev.getLength(), value);
            } else {
                MemorySegment segment = repository.createMemorySegment(repository.openMemorySource(), 0, 0);
                repository.setMemoryByte(segment, 0, value);
                segments.addBefore(pointerSegment, segment);
            }
            pointerPosition++;
            FileSegment documentSegment = ((FileSegment) pointerSegment);
            documentSegment.setStartPosition(documentSegment.getStartPosition() + 1);
            if (documentSegment.getLength() == 1) {
                segments.remove(documentSegment);
                repository.dropSegment(documentSegment);
            } else {
                documentSegment.setLength(documentSegment.getLength() - 1);
            }
        } else {
            if (pointerSegment == null) {
                pointerSegment = repository.createMemorySegment(repository.openMemorySource(), 0, 0);
                segments.add(pointerSegment);
            }
            repository.setMemoryByte((MemorySegment) pointerSegment, position - pointerPosition, value);
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
        if (pointerSegment instanceof MemorySegment) {
            repository.insertUninitializedMemoryData((MemorySegment) pointerSegment, startFrom - pointerPosition, length);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertUninitializedMemoryData((MemorySegment) insertedSegment, 0, length);
            if (pointerSegment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointerSegment, insertedSegment);
            }
            pointerSegment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, long length) {
        if (length == 0) {
            return;
        }

        focusSegment(startFrom);
        dataLength += length;
        if (pointerSegment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointerSegment, startFrom - pointerPosition, length);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, length);
            if (pointerSegment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointerSegment, insertedSegment);
            }
            pointerSegment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        if (insertedData.length == 0) {
            return;
        }

        focusSegment(startFrom);
        dataLength += insertedData.length;
        if (pointerSegment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointerSegment, startFrom - pointerPosition, insertedData);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData);
            if (pointerSegment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointerSegment, insertedSegment);
            }
            pointerSegment = insertedSegment;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        focusSegment(startFrom);
        dataLength += insertedDataLength;
        if (pointerSegment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointerSegment, startFrom - pointerPosition, insertedData);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData);
            if (pointerSegment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointerSegment, insertedSegment);
            }
            pointerSegment = insertedSegment;
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
            if (pointerPosition < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }

            // Copy all segments from inserted document
            DeltaDocument document = (DeltaDocument) insertedData;
            DataSegment segment = document.segments.first();
            DataSegment copy = repository.copySegment(segment);
            DataSegment first = copy;
            segments.addBefore(pointerSegment, copy);
            DataSegment next = segment.getNext();
            while (next != null) {
                DataSegment nextCopy = repository.copySegment(next);
                segments.addAfter(copy, nextCopy);
                copy = nextCopy;
                next = next.getNext();
            }
            pointerSegment = first;
            tryMergeArea(startFrom, insertedData.getDataSize());
        } else if (pointerSegment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointerSegment, startFrom - pointerPosition, insertedData);
        } else {
            if (pointerPosition < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData);
            if (pointerSegment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointerSegment, insertedSegment);
            }
            pointerSegment = insertedSegment;
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
            if (pointerPosition < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }

            // Copy all segments from inserted document
            DeltaDocument document = (DeltaDocument) insertedData;

            long length = insertedDataLength;
            DataSegment segment = document.getSegmentPart(insertedDataOffset);
            // TODO cut and detach segment
            DataSegment copy = repository.copySegment(segment);
            DataSegment first = copy;
            segments.addBefore(pointerSegment, copy);
            DataSegment next = segment.getNext();
            while (next != null) {
                DataSegment nextCopy = repository.copySegment(next);
                segments.addAfter(copy, nextCopy);
                copy = nextCopy;
                next = next.getNext();
            }
            pointerSegment = first;
            tryMergeArea(startFrom, insertedData.getDataSize());
        } else if (pointerSegment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointerSegment, startFrom - pointerPosition, insertedData, insertedDataOffset, insertedDataLength);
        } else {
            if (pointerPosition < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData((MemorySegment) insertedSegment, 0, insertedData, insertedDataOffset, insertedDataLength);
            if (pointerSegment == null) {
                segments.add(0, insertedSegment);
            } else {
                segments.addBefore(pointerSegment, insertedSegment);
            }
            pointerSegment = insertedSegment;
        }
    }

    public void insert(long startFrom, DataSegment segment) {
        focusSegment(startFrom);
        if (pointerPosition < startFrom) {
            splitSegment(startFrom);
            focusSegment(startFrom);
        }
        if (pointerSegment == null) {
            segments.add(0, segment);
        } else {
            segments.addAfter(pointerSegment, segment);
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
            DataSegment prevSegment = (DataSegment) pointerSegment.getPrev();
            long prevPointerPosition = prevSegment == null ? 0 : pointerPosition - prevSegment.getLength();

            // Drop all segments in given range
            while (length > 0) {
                length -= pointerSegment.getLength();
                DataSegment next = segments.nextTo(pointerSegment);
                repository.dropSegment(pointerSegment);
                segments.remove(pointerSegment);
                pointerSegment = next;
            }

            // Set pointer position
            pointerSegment = prevSegment;
            pointerPosition = prevPointerPosition;
            tryMergeSegments(startFrom);
        }
    }

    @Override
    public void clear() {
        pointerPosition = 0;
        pointerSegment = null;
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

        DataSegment segment = pointerSegment;
        long offset = startFrom - pointerPosition;
        while (length > 0) {
            long segmentLength = segment.getLength();
            long copyLength = segmentLength - offset;
            if (copyLength > length) {
                copyLength = length;
            }

            if (offset == 0 && copyLength == segmentLength) {
                copy.segments.add(repository.copySegment(pointerSegment));
            } else if (pointerSegment instanceof MemorySegment) {
                MemorySegment memorySegment = (MemorySegment) pointerSegment;
                copy.segments.add(repository.createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + offset, copyLength));
            } else {
                FileSegment fileSegment = (FileSegment) pointerSegment;
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
        if (position < pointerPosition || position > pointerPosition + pointerSegment.getLength()) {
            throw new IllegalStateException("Split position is out of current segment");
        }

        if (pointerPosition == position) {
            // No action needed
            return;
        }

        long firstPartSize = position - pointerPosition;
        if (pointerSegment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) pointerSegment;
            MemorySegment newSegment = repository.createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + firstPartSize, memorySegment.getLength() - firstPartSize);
            memorySegment.setLength(firstPartSize);
            segments.addAfter(pointerSegment, newSegment);
        } else {
            FileSegment fileSegment = (FileSegment) pointerSegment;
            FileSegment newSegment = repository.createFileSegment(fileSegment.getSource(), fileSegment.getStartPosition() + firstPartSize, fileSegment.getLength() - firstPartSize);
            fileSegment.setLength(firstPartSize);
            segments.addAfter(pointerSegment, newSegment);
        }
    }

    public DataSegment getSegment(long position) {
        focusSegment(position);
        return pointerSegment;
    }

    /**
     * Returns segment starting from given position or copy of part of the
     * segment starting from given position.
     *
     * @param position position
     * @return segment
     */
    public DataSegment getSegmentPart(long position) {
        focusSegment(position);
        if (pointerPosition < position) {
            long offset = position - pointerPosition;
            return repository.copySegment(pointerSegment, offset, pointerSegment.getLength() - offset);
        }
        return pointerSegment;
    }

    private void focusSegment(long position) {
        if (position == 0) {
            pointerPosition = 0;
            pointerSegment = segments.first();
            return;
        }

        if (position < pointerPosition) {
            while (position < pointerPosition) {
                pointerSegment = (DataSegment) segments.prevTo(pointerSegment);
                if (pointerSegment == null) {
                    throw new OutOfBoundsException("Unable to access previous segment");
                }
                pointerPosition -= pointerSegment.getLength();
            }
        } else {
            while (position >= pointerPosition + pointerSegment.getLength()) {
                if ((pointerSegment.getNext() == null) && (position == pointerPosition + pointerSegment.getLength())) {
                    break;
                }
                pointerPosition += pointerSegment.getLength();
                pointerSegment = (DataSegment) segments.nextTo(pointerSegment);
                if (pointerSegment == null) {
                    throw new OutOfBoundsException("Unable to access next segment");
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
        DataSegment nextSegment = pointerSegment;
        focusSegment(position - 1);
        DataSegment segment = pointerSegment;
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
}
