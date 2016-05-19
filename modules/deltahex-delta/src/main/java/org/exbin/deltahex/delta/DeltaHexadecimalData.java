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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.deltahex.data.EditableHexadecimalData;
import org.exbin.deltahex.data.HexadecimalData;

/**
 * Basic implementation of hexadecimal data interface using byte array.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaHexadecimalData implements EditableHexadecimalData {

    private DeltaDataSource data;

    public DeltaHexadecimalData(DeltaDataSource data) {
        this.data = data;
    }

    @Override
    public boolean isEmpty() {
        return data.getFileLength() == 0;
    }

    @Override
    public long getDataSize() {
        return data.getFileLength();
    }

    @Override
    public byte getByte(long position) {
        try {
            return data.getByte(position);
        } catch (IOException ex) {
            Logger.getLogger(DeltaHexadecimalData.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HexadecimalData copy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HexadecimalData copy(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDataSize(long size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setByte(long position, byte value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, HexadecimalData insertedData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(long startFrom, long length) {
        data.remove(startFrom, length);
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
