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
import org.exbin.deltahex.data.EditableHexadecimalData;
import org.exbin.deltahex.data.HexadecimalData;
import org.exbin.xbup.core.type.XBData;

/**
 * Encapsulation of data for hexadecimal editor.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class MemoryHexadecimalData implements EditableHexadecimalData {

    private final XBData data;

    public MemoryHexadecimalData() {
        data = new XBData();
    }

    public MemoryHexadecimalData(XBData data) {
        this.data = data;
    }

    public MemoryHexadecimalData(byte[] data) {
        this.data = new XBData();
        this.data.insert(0, data);
    }

    @Override
    public void setDataSize(long size) {
        data.setDataSize(size);
    }

    @Override
    public void setByte(long position, byte value) {
        data.setByte(position, value);
    }

    @Override
    public void insert(long startFrom, long length) {
        data.insert(startFrom, length);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        data.insert(startFrom, insertedData);
    }

    @Override
    public void insert(long startFrom, HexadecimalData insertedData) {
        // TODO general support for HexadecimalData
        data.insert(startFrom, ((MemoryHexadecimalData) insertedData).data);
    }

    @Override
    public void remove(long startFrom, long length) {
        data.remove(startFrom, length);
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        data.loadFromStream(inputStream);
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public long getDataSize() {
        return data.getDataSize();
    }

    @Override
    public byte getByte(long position) {
        return data.getByte(position);
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        data.saveToStream(outputStream);
    }

    @Override
    public HexadecimalData copy() {
        return new MemoryHexadecimalData(data.copy());
    }

    @Override
    public HexadecimalData copy(long startFrom, long length) {
        return new MemoryHexadecimalData(data.copy(startFrom, length));
    }
}
