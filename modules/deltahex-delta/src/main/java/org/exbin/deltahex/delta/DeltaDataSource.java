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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.exbin.deltahex.delta.list.DefaultDoublyLinkedList;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Data source for access to resource with keeping list of modifications to it.
 *
 * Data source is opened in read only mode and there structure keeping all the
 * changes.
 *
 * @version 0.1.0 2016/06/05
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDataSource {

    private final RandomAccessFile file;
    private final DeltaDataPageWindow window;

    private long fileLength = 0;
    private long pointerPosition;
    private DataSegment pointerSegment;

    private final DefaultDoublyLinkedList<DataSegment> segments = new DefaultDoublyLinkedList<>();

    public DeltaDataSource(File sourceFile) throws FileNotFoundException, IOException {
        file = new RandomAccessFile(sourceFile, "rw");
        fileLength = file.length();
        DataSegment fullFileSegment = new DocumentSegment(0, fileLength);
        segments.add(fullFileSegment);
        pointerPosition = 0;
        pointerSegment = fullFileSegment;
        window = new DeltaDataPageWindow(this);
    }

    public long getFileLength() {
        return fileLength;
    }

    RandomAccessFile getFile() {
        return file;
    }

    public byte getByte(long position) throws IOException {
        focusSegment(position);

        if (pointerSegment instanceof DocumentSegment) {
            return window.getByte(((DocumentSegment) pointerSegment).getStartPosition() + (position - pointerPosition));
        } else {
            return ((BinaryDataSegment) pointerSegment).getByte(position - pointerPosition);
        }
    }

    public void setByte(long position, byte value) throws IOException {
        focusSegment(position);

        if (pointerSegment instanceof DocumentSegment) {
            // TODO extend previous binary data segment or split document segment and add binary segment
            // window.setByte(((DocumentSegment) pointerSegment).getStartPosition() + (position - pointerPosition));
        } else {
            ((BinaryDataSegment) pointerSegment).setByte(position - pointerPosition, value);
        }
    }

    private void focusSegment(long position) throws IOException {
        if (position < pointerPosition) {
            while (position < pointerPosition) {
                pointerSegment = (DataSegment) segments.prevTo(pointerSegment);
                if (pointerSegment == null) {
                    throw new IOException("Unable to access previous segment");
                }
                pointerPosition -= pointerSegment.getLength();
            }
        } else if (position > pointerPosition + pointerSegment.getLength()) {
            while (position > pointerPosition + pointerSegment.getLength()) {
                pointerSegment = (DataSegment) segments.nextTo(pointerSegment);
                if (pointerSegment == null) {
                    throw new IOException("Unable to access next segment");
                }
                pointerPosition += pointerSegment.getLength();
            }
        }
    }

    public void remove(long startFrom, long length) throws IOException {
        if (length > 0) {
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

    public void setDataSize(long dataSize) throws IOException {
        if (dataSize < fileLength) {
            remove(fileLength, fileLength - dataSize);
        } else {
            // TODO insert()
        }
    }

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
}
