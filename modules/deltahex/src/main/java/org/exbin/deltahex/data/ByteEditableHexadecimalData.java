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
package org.exbin.deltahex.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Basic implementation of editable hexadecimal data interface using byte array.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class ByteEditableHexadecimalData extends ByteHexadecimalData implements EditableHexadecimalData {

    public ByteEditableHexadecimalData() {

    }

    public ByteEditableHexadecimalData(byte[] data) {
        super(data);
    }

    @Override
    public void setDataSize(long size) {
        if (data.length != size) {
            if (size < data.length) {
                data = Arrays.copyOfRange(data, 0, (int) size);
            } else {
                byte[] newData = new byte[(int) size];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
        }
    }

    @Override
    public void setByte(long position, byte value) {
        data[(int) position] = value;
    }

    @Override
    public void insert(long startFrom, long length) {
        if (startFrom > data.length) {
            throw new IndexOutOfBoundsException("Cannot insert after document");
        }
        if (length > 0) {
            byte[] newData = new byte[(int) (data.length + length)];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        if (startFrom > data.length) {
            throw new IndexOutOfBoundsException("Cannot insert after document");
        }
        int length = insertedData.length;
        if (length > 0) {
            byte[] newData = new byte[(int) (data.length + length)];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            System.arraycopy(insertedData, 0, newData, (int) startFrom, length);
            System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, HexadecimalData insertedData) {
        if (startFrom > data.length) {
            throw new IndexOutOfBoundsException("Cannot insert after document");
        }
        if (insertedData instanceof ByteHexadecimalData) {
            insert(startFrom, ((ByteHexadecimalData) insertedData).data);
        } else {
            long length = insertedData.getDataSize();
            if (length > 0) {
                byte[] newData = new byte[(int) (data.length + length)];
                System.arraycopy(data, 0, newData, 0, (int) startFrom);
                for (int i = 0; i < length; i++) {
                    newData[(int) (startFrom + i)] = insertedData.getByte(i);
                }
                System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
                data = newData;
            }
        }
    }

    @Override
    public void remove(long startFrom, long length) {
        if (startFrom + length > data.length) {
            throw new IndexOutOfBoundsException("Cannot remove from " + startFrom + " with length " + length);
        }
        if (length > 0) {
            byte[] newData = new byte[(int) (data.length - length)];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            System.arraycopy(data, (int) (startFrom + length), newData, (int) startFrom, (int) (data.length - startFrom - length));
            data = newData;
        }
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            while (inputStream.available() > 0) {
                int read = inputStream.read(buffer);
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            }
            data = output.toByteArray();
        }
    }
}
