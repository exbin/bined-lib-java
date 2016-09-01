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
package org.exbin.deltahex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayData;

/**
 * Simulation of huge binary data source.
 *
 * @version 0.1.1 2016/09/01
 * @author ExBin Project (http://exbin.org)
 */
public class HugeBinaryData implements BinaryData {

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getDataSize() {
        return Long.MAX_VALUE / 2;
    }

    @Override
    public byte getByte(long l) {
        return (byte) (l % 255);
    }

    @Override
    public BinaryData copy() {
        return new HugeBinaryData();
    }

    @Override
    public BinaryData copy(long startFrom, long length) {
        if (length > Integer.MAX_VALUE) {
            throw new IllegalStateException("Unable to copy too huge memory segment");
        }
        ByteArrayData data = new ByteArrayData(new byte[(int) length]);
        copyToArray(startFrom, data.getData(), 0, (int) length);
        return data;
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        for (int position = 0; position < length; position++) {
            target[offset + position] = getByte(startFrom + position);
        }
    }

    @Override
    public void saveToStream(OutputStream out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getDataInputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
