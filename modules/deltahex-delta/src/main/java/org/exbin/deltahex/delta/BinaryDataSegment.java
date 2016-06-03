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

import org.exbin.utils.binary_data.BinaryData;

/**
 * Data segment with binary data.
 *
 * @version 0.1.0 2016/06/03
 * @author ExBin Project (http://exbin.org)
 */
public class BinaryDataSegment extends DataSegment {

    private BinaryData binaryData;

    public BinaryDataSegment(BinaryData binaryData) {
        this.binaryData = binaryData;
    }

    @Override
    public long getLength() {
        return binaryData.getDataSize();
    }

    public BinaryData getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(BinaryData binaryData) {
        this.binaryData = binaryData;
    }

    public byte getByte(long position) {
        return binaryData.getByte(position);
    }
}
