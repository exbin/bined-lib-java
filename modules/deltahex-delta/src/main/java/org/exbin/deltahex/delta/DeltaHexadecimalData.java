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
 * Basic implementation of hexadecimal data interface using byte array.
 *
 * @version 0.1.0 2016/06/07
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaHexadecimalData implements EditableBinaryData {

    private final DeltaDataSource data;

    private long dataLength = 0;
    private long pointerPosition;
    private DataSegment pointerSegment;

    // Temporary public
    public final DefaultDoublyLinkedList<DataSegment> segments = new DefaultDoublyLinkedList<>();

    public DeltaHexadecimalData(DeltaDataSource data) throws IOException {
        this.data = data;
        dataLength = data.getFileLength();
        DataSegment fullFileSegment = new DocumentSegment(0, dataLength);
        segments.add(fullFileSegment);
        pointerPosition = 0;
        pointerSegment = fullFileSegment;
    }

    /**
     * This is copy constructor.
     *
     * @param source source object
     */
    private DeltaHexadecimalData(DeltaHexadecimalData source) {
        data = source.data;
        dataLength = source.dataLength;
        for (DataSegment segment : segments) {
            segments.add(segment.copy());
        }
    }

    private DeltaHexadecimalData(DeltaDataSource data, long length) {
        this.data = data;
        if (length > 0) {
            dataLength = length;
            MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
            binaryData.insert(0, length);
            DataSegment fullFileSegment = new BinaryDataSegment(binaryData);
            segments.add(fullFileSegment);
        }
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

        if (pointerSegment instanceof DocumentSegment) {
            return data.getWindow().getByte(((DocumentSegment) pointerSegment).getStartPosition() + (position - pointerPosition));
        } else {
            return ((BinaryDataSegment) pointerSegment).getByte(position - pointerPosition);
        }
    }

    @Override
    public void setByte(long position, byte value) {
        focusSegment(position);

        if (pointerSegment instanceof DocumentSegment) {
            if (pointerPosition != position) {
                splitSegment(position);
                focusSegment(position);
            }

            DataSegment prev = segments.prevTo(pointerSegment);
            EditableBinaryData binaryData;
            if (prev instanceof BinaryDataSegment) {
                binaryData = ((BinaryDataSegment) prev).getBinaryData();
                binaryData.insert(binaryData.getDataSize(), new byte[]{value});
            } else {
                binaryData = new MemoryHexadecimalData();
                binaryData.insert(0, new byte[]{value});
                BinaryDataSegment binarySegment = new BinaryDataSegment(binaryData);
                segments.addBefore(pointerSegment, binarySegment);
            }
            pointerPosition++;
            DocumentSegment documentSegment = ((DocumentSegment) pointerSegment);
            documentSegment.setStartPosition(documentSegment.getStartPosition() + 1);
            if (documentSegment.getLength() == 1) {
                segments.remove(documentSegment);
            } else {
                documentSegment.setLength(documentSegment.getLength() - 1);
            }
        } else {
            ((BinaryDataSegment) pointerSegment).setByte(position - pointerPosition, value);
        }
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        focusSegment(startFrom);
        if (pointerSegment instanceof BinaryDataSegment) {
            ((BinaryDataSegment) pointerSegment).getBinaryData().insertUninitialized(startFrom - pointerPosition, length);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
            binaryData.insertUninitialized(0, length);
            BinaryDataSegment binarySegment = new BinaryDataSegment(binaryData);
            segments.addBefore(pointerSegment, binarySegment);
        }
    }

    @Override
    public void insert(long startFrom, long length) {
        focusSegment(startFrom);
        if (pointerSegment instanceof BinaryDataSegment) {
            ((BinaryDataSegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, length);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
            binaryData.insert(0, length);
            BinaryDataSegment binarySegment = new BinaryDataSegment(binaryData);
            segments.addBefore(pointerSegment, binarySegment);
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        focusSegment(startFrom);
        if (pointerSegment instanceof BinaryDataSegment) {
            ((BinaryDataSegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
            binaryData.insert(0, insertedData);
            BinaryDataSegment binarySegment = new BinaryDataSegment(binaryData);
            segments.addBefore(pointerSegment, binarySegment);
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        focusSegment(startFrom);
        if (pointerSegment instanceof BinaryDataSegment) {
            ((BinaryDataSegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData, insertedDataOffset, insertedDataLength);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
            binaryData.insert(0, insertedData, insertedDataOffset, insertedDataLength);
            BinaryDataSegment binarySegment = new BinaryDataSegment(binaryData);
            segments.addBefore(pointerSegment, binarySegment);
        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        focusSegment(startFrom);
        if (pointerSegment instanceof BinaryDataSegment) {
            ((BinaryDataSegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
            binaryData.insert(0, insertedData);
            BinaryDataSegment binarySegment = new BinaryDataSegment(binaryData);
            segments.addBefore(pointerSegment, binarySegment);
        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        focusSegment(startFrom);
        if (pointerSegment instanceof BinaryDataSegment) {
            ((BinaryDataSegment) pointerSegment).getBinaryData().insert(startFrom - pointerPosition, insertedData, insertedDataOffset, insertedDataLength);
        } else {
            if (startFrom > pointerPosition) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
            binaryData.insert(0, insertedData, insertedDataOffset, insertedDataLength);
            BinaryDataSegment binarySegment = new BinaryDataSegment(binaryData);
            segments.addBefore(pointerSegment, binarySegment);
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
                pointerSegment = next;
            }
        }
    }

    @Override
    public void clear() {
        pointerPosition = 0;
        pointerSegment = null;
        dataLength = 0;
        segments.clear();
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
        return new DeltaHexadecimalData(this);
    }

    @Override
    public BinaryData copy(long startFrom, long length) {
        DeltaHexadecimalData copy = new DeltaHexadecimalData(data, 0);
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
                copy.segments.add(pointerSegment.copy());
            } else if (pointerSegment instanceof BinaryDataSegment) {
                BinaryData partialData = ((BinaryDataSegment) pointerSegment).getBinaryData().copy(offset, copyLength);
                copy.segments.add(new BinaryDataSegment((EditableBinaryData) partialData));
            } else {
                copy.segments.add(new DocumentSegment(((DocumentSegment) segment).getStartPosition() + offset, copyLength));
            }
            length -= copyLength;
            offset = 0;
            segment = segments.nextTo(segment);
        }

        return copy;
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
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
            focusSegment(dataSize);
            if (pointerSegment instanceof BinaryDataSegment) {
                EditableBinaryData binaryData = ((BinaryDataSegment) pointerSegment).getBinaryData();
                binaryData.setDataSize(binaryData.getDataSize() + (dataSize - dataLength));
            } else {
                MemoryHexadecimalData binaryData = new MemoryHexadecimalData();
                binaryData.setDataSize(dataSize - dataLength);
                BinaryDataSegment emptySegment = new BinaryDataSegment(binaryData);
                segments.add(emptySegment);
            }
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
        if (pointerSegment instanceof BinaryDataSegment) {
            EditableBinaryData binaryData = ((BinaryDataSegment) pointerSegment).getBinaryData();
            EditableBinaryData copy = (EditableBinaryData) binaryData.copy(firstPartSize, binaryData.getDataSize() - (firstPartSize));
            BinaryDataSegment newSegment = new BinaryDataSegment(copy);
            segments.addAfter(pointerSegment, newSegment);
            if (binaryData instanceof EditableBinaryData) {
                ((EditableBinaryData) binaryData).setDataSize(firstPartSize);
            } else {
                ((BinaryDataSegment) pointerSegment).setBinaryData((EditableBinaryData) binaryData.copy(0, firstPartSize));
            }
        } else {
            DocumentSegment documentSegment = (DocumentSegment) pointerSegment;
            DocumentSegment newSegment = new DocumentSegment(documentSegment.getStartPosition() + firstPartSize, documentSegment.getLength() - firstPartSize);
            segments.addAfter(pointerSegment, newSegment);
            documentSegment.setLength(firstPartSize);
        }
    }

    private void focusSegment(long position) {
        if (pointerSegment == null && position == 0) {
            pointerPosition = 0;
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
}
