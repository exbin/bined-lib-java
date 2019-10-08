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
package org.exbin.bined.delta.swap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.utils.binary_data.PagedData;

/**
 * Encapsulation class for binary data blob.
 *
 * Data are stored using paging. Last page might be shorter than page size, but
 * not empty.
 *
 * @version 0.1.3 2019/07/16
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SwapPagedData implements EditableBinaryData {
    
    private final PagedData pagedData;
    private final SwapFileRepository swapFile;

    public SwapPagedData() throws IOException {
        pagedData = new PagedData();
        swapFile = new SwapFileRepository(pagedData.getPageSize());
    }

    public SwapPagedData(int pageSize) throws IOException {
        pagedData = new PagedData(pageSize);
        swapFile = new SwapFileRepository(pageSize);
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
    public long insert(long l, InputStream in, long l1) throws IOException {
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
    public OutputStream getDataOutputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getDataSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte getByte(long l) {
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
    public void saveToStream(OutputStream out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getDataInputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
