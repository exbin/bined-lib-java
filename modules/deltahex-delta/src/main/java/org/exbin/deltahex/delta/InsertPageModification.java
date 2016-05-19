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

/**
 * Insertion page modification.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class InsertPageModification implements DataPageModification {

    private int offset;
    private byte[] inserted;

    public InsertPageModification() {
    }

    public InsertPageModification(int offset, byte[] inserted) {
        this.offset = offset;
        this.inserted = inserted;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getInserted() {
        return inserted;
    }

    public void setInserted(byte[] inserted) {
        this.inserted = inserted;
    }
}
