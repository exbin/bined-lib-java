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
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Basic implementation of hexadecimal data interface using byte array.
 *
 * @version 0.1.0 2016/05/24
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaHexadecimalData implements EditableBinaryData {

    private final DeltaDataSource data;

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
    public void setDataSize(long l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setByte(long l, byte b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insertUninitialized(long l, long l1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long l, long l1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long l, byte[] bytes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long l, byte[] bytes, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long l, BinaryData bd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long l, BinaryData bd, long l1, long l2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long l, BinaryData bd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long l, BinaryData bd, long l1, long l2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long l, byte[] bytes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long l, byte[] bytes, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillData(long l, long l1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillData(long l, long l1, byte b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(long l, long l1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BinaryData copy(long l, long l1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copyToArray(long l, byte[] bytes, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputStream getDataOutputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getDataInputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
