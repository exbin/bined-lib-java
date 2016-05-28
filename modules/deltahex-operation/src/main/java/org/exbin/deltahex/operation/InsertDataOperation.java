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
 * Operation for inserting data.
 *
 * @version 0.1.0 2015/05/25
 * @author ExBin Project (http://exbin.org)
 */
public class InsertDataOperation extends HexOperation {

    private long position;
    private boolean lowerHalf;
    private BinaryData data;

    public InsertDataOperation(Hexadecimal hexadecimal, long position, boolean lowerHalf, BinaryData data) {
        super(hexadecimal);
        this.position = position;
        this.lowerHalf = lowerHalf;
        this.data = data;
    }

    @Override
    public HexOperationType getType() {
        return HexOperationType.INSERT_DATA;
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
        ((EditableBinaryData) hexadecimal.getData()).insert(position, data);
        if (withUndo) {
            undoOperation = new RemoveDataOperation(hexadecimal, position, lowerHalf, data.getDataSize());
        }
        hexadecimal.getCaret().setCaretPosition(position + data.getDataSize(), lowerHalf);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        ((EditableBinaryData) data).insert(data.getDataSize(), appendData);
    }
}
