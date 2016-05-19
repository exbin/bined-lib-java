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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Basic implementation of hexadecimal data interface using byte array.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class ByteHexadecimalData implements HexadecimalData {

    protected byte[] data = new byte[0];

    public ByteHexadecimalData() {
    }

    public ByteHexadecimalData(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }

        this.data = data;
    }

    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }

    @Override
    public long getDataSize() {
        return data.length;
    }

    @Override
    public byte getByte(long position) {
        return data[(int) position];
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        outputStream.write(data);
    }

    @Override
    public HexadecimalData copy() {
        byte[] copy = Arrays.copyOf(data, data.length);
        return new ByteHexadecimalData(copy);
    }

    @Override
    public HexadecimalData copy(long startFrom, long length) {
        byte[] copy = Arrays.copyOfRange(data, (int) startFrom, (int) (startFrom + length));
        return new ByteHexadecimalData(copy);
    }
}
