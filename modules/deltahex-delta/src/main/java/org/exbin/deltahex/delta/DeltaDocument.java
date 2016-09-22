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
 * @version 0.1.1 2016/09/22
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDocument implements EditableBinaryData {

    private final SegmentsRepository repository;
    private final DefaultDoublyLinkedList<DataSegment> segments = new DefaultDoublyLinkedList<>();

    private long dataLength = 0;
    private long pointerPosition;
    private DataSegment pointerSegment;

    public DeltaDocument(SegmentsRepository repository, FileDataSource fileSource) throws IOException {
        this.repository = repository;
        dataLength = fileSource.getFileLength();
        DataSegment fullFileSegment = new FileSegment(fileSource, 0, dataLength);
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
        throw new UnsupportedOperationException("Not supported yet.");
//        focusSegment(startFrom);
//        if (pointerSegment instanceof MemorySegment) {
//            ((MemorySegment) pointerSegment).getBinaryData().insertUninitialized(startFrom - pointerPosition, length);
//        } else {
//            if (startFrom > pointerPosition) {
//                splitSegment(startFrom);
//                focusSegment(startFrom);
//            }
//            MemoryPagedData binaryData = new MemoryPagedData();
//            binaryData.insertUninitialized(0, length);
//            MemorySegment binarySegment = new MemorySegment(binaryData);
//            segments.addBefore(pointerSegment, binarySegment);
//        }
    }

    @Override
    public void insert(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
//        focusSegment(startFrom);
//        if (pointerSegment instanceof MemorySegment) {
//            ((MemorySegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, length);
//        } else {
//            if (startFrom > pointerPosition) {
//                splitSegment(startFrom);
//                focusSegment(startFrom);
//            }
//            MemoryPagedData binaryData = new MemoryPagedData();
//            binaryData.insert(0, length);
//            MemorySegment binarySegment = new MemorySegment(binaryData);
//            segments.addBefore(pointerSegment, binarySegment);
//        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        throw new UnsupportedOperationException("Not supported yet.");
//        focusSegment(startFrom);
//        if (pointerSegment instanceof MemorySegment) {
//            ((MemorySegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData);
//        } else {
//            if (startFrom > pointerPosition) {
//                splitSegment(startFrom);
//                focusSegment(startFrom);
//            }
//            MemoryPagedData binaryData = new MemoryPagedData();
//            binaryData.insert(0, insertedData);
//            MemorySegment binarySegment = new MemorySegment(binaryData);
//            segments.addBefore(pointerSegment, binarySegment);
//        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        throw new UnsupportedOperationException("Not supported yet.");
//        focusSegment(startFrom);
//        if (pointerSegment instanceof MemorySegment) {
//            ((MemorySegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData, insertedDataOffset, insertedDataLength);
//        } else {
//            if (startFrom > pointerPosition) {
//                splitSegment(startFrom);
//                focusSegment(startFrom);
//            }
//            MemoryPagedData binaryData = new MemoryPagedData();
//            binaryData.insert(0, insertedData, insertedDataOffset, insertedDataLength);
//            MemorySegment binarySegment = new MemorySegment(binaryData);
//            segments.addBefore(pointerSegment, binarySegment);
//        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        throw new UnsupportedOperationException("Not supported yet.");
//        focusSegment(startFrom);
//        if (pointerSegment instanceof MemorySegment) {
//            ((MemorySegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData);
//        } else {
//            if (startFrom > pointerPosition) {
//                splitSegment(startFrom);
//                focusSegment(startFrom);
//            }
//            MemoryPagedData binaryData = new MemoryPagedData();
//            binaryData.insert(0, insertedData);
//            MemorySegment binarySegment = new MemorySegment(binaryData);
//            segments.addBefore(pointerSegment, binarySegment);
//        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        throw new UnsupportedOperationException("Not supported yet.");
//        focusSegment(startFrom);
//        if (pointerSegment instanceof MemorySegment) {
//            ((MemorySegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData, insertedDataOffset, insertedDataLength);
//        } else {
//            if (startFrom > pointerPosition) {
//                splitSegment(startFrom);
//                focusSegment(startFrom);
//            }
//            MemoryPagedData binaryData = new MemoryPagedData();
//            binaryData.insert(0, insertedData, insertedDataOffset, insertedDataLength);
//            MemorySegment binarySegment = new MemorySegment(binaryData);
//            segments.addBefore(pointerSegment, binarySegment);
//        }
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
        if (length > 0) {
            dataLength -= length;
            focusSegment(startFrom + length);
            splitSegment(startFrom + length);
            focusSegment(startFrom);
            splitSegment(startFrom);
            focusSegment(startFrom);
            while (length > 0) {
                length -= pointerSegment.getLength();
                DataSegment next = segments.nextTo(pointerSegment);
                segments.remove(pointerSegment);
                repository.dropSegment(pointerSegment);
                pointerSegment = next;
            }
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

        copy.dataLength = length;
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
        throw new UnsupportedOperationException("Not supported yet.");
//        if (dataSize < dataLength) {
//            remove(dataSize, dataLength - dataSize);
//        } else if (dataSize > dataLength) {
//            focusSegment(dataSize);
//            if (pointerSegment instanceof MemorySegment) {
//                EditableBinaryData binaryData = ((MemorySegment) pointerSegment).getBinaryData();
//                binaryData.setDataSize(binaryData.getDataSize() + (dataSize - dataLength));
//            } else {
//                MemoryPagedData binaryData = new MemoryPagedData();
//                binaryData.setDataSize(dataSize - dataLength);
//                MemorySegment emptySegment = new MemorySegment(binaryData);
//                segments.add(emptySegment);
//            }
//        }
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

    private void focusSegment(long position) {
        if (pointerSegment == null && position == 0) {
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

    /**
     * Attempts to merge segments at specified position.
     *
     * @param position target position
     */
    private void tryMergeSegments(long position) {
//        if (position == 0 || position >= getDataSize()) {
//            return;
//        }
//
//        focusSegment(position);
//        DataSegment nextSegment = pointerSegment;
//        focusSegment(position - 1);
//        DataSegment segment = pointerSegment;
//        if (segment == nextSegment) {
//            return;
//        }
//
//        if (segment instanceof FileSegment && nextSegment instanceof FileSegment) {
//            if (((FileSegment) segment).getStartPosition() + segment.getLength() == ((FileSegment) nextSegment).getStartPosition()) {
//                ((FileSegment) segment).setLength(segment.getLength() + nextSegment.getLength());
//                segments.remove(nextSegment);
//            }
//        }
//
//        if (segment instanceof MemorySegment && nextSegment instanceof MemorySegment) {
//            EditableBinaryData binaryData = ((MemorySegment) segment).getBinaryData();
//            EditableBinaryData nextBinaryData = ((MemorySegment) nextSegment).getBinaryData();
//            binaryData.insert(binaryData.getDataSize(), nextBinaryData);
//            segments.remove(nextSegment);
//        }
    }
}
