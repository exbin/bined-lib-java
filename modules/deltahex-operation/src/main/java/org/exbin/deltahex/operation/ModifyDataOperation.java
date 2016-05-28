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
package org.exbin.deltahex.operation;

import org.exbin.deltahex.Hexadecimal;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for modifying data.
 *
 * @version 0.1.0 2016/05/25
 * @author ExBin Project (http://exbin.org)
 */
public class ModifyDataOperation extends HexOperation {

    private long position;
    private BinaryData data;

    public ModifyDataOperation(Hexadecimal hexadecimal, long position, BinaryData data) {
        super(hexadecimal);
        this.position = position;
        this.data = data;
    }

    @Override
    public HexOperationType getType() {
        return HexOperationType.MODIFY_DATA;
    }

    @Override
    public void execute() throws Exception {
        execute(false);
    }

    @Override
    public HexOperation executeWithUndo() throws Exception {
        return execute(true);
    }

    private HexOperation execute(boolean withUndo) {
        HexOperation undoOperation = null;
        if (withUndo) {
            BinaryData undoData = hexadecimal.getData().copy(position, data.getDataSize());
            undoOperation = new ModifyDataOperation(hexadecimal, position, undoData);
        }
        ((EditableBinaryData) hexadecimal.getData()).remove(position, data.getDataSize());
        ((EditableBinaryData) hexadecimal.getData()).insert(position, data);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        ((EditableBinaryData) data).insert(data.getDataSize(), appendData);
    }
}
