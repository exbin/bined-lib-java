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
package org.exbin.framework.deltahex.operation;

import org.exbin.deltahex.EditableHexadecimalData;
import org.exbin.deltahex.component.Hexadecimal;
import org.exbin.framework.deltahex.XBHexadecimalData;
import org.exbin.xbup.operation.Operation;

/**
 * Operation for editing data in delete mode.
 *
 * @version 0.1.0 2015/05/16
 * @author ExBin Project (http://exbin.org)
 */
public class DeleteCharEditDataOperation extends CharEditDataOperation {

    private final long startPosition;
    private final boolean startLowerHalf;
    private long length = 0;
    private final XBHexadecimalData undoData = new XBHexadecimalData();

    private boolean lowerHalf = false;

    public DeleteCharEditDataOperation(Hexadecimal hexadecimal, long startPosition, boolean startLowerHalf) {
        super(hexadecimal);
        this.startPosition = startPosition;
        this.startLowerHalf = startLowerHalf;
        this.lowerHalf = startLowerHalf;
        if (startLowerHalf && hexadecimal.getData().getDataSize() > startPosition) {
            undoData.insert(0, new byte[]{hexadecimal.getData().getByte(startPosition)});
            length++;
        }
    }

    @Override
    public HexOperationType getType() {
        return HexOperationType.EDIT_DATA;
    }

    @Override
    public void execute() throws Exception {
        execute(false);
    }

    @Override
    public Operation executeWithUndo() throws Exception {
        return execute(true);
    }

    private Operation execute(boolean withUndo) {
        throw new IllegalStateException("Cannot be executed");
    }

    @Override
    public void appendEdit(char value) {
        EditableHexadecimalData data = (EditableHexadecimalData) hexadecimal.getData();
        long editedDataPosition = startPosition + length;

        if (lowerHalf) {
            byte dataValue = 0;
            if (editedDataPosition <= data.getDataSize()) {
                dataValue = data.getByte(editedDataPosition - 1);
            }

            data.setByte(editedDataPosition - 1, (byte) ((dataValue & 0xf0) | value));
        } else {
            byte dataValue = 0;
            if (editedDataPosition < data.getDataSize()) {
                dataValue = data.getByte(editedDataPosition);
                undoData.insert(undoData.getDataSize(), new byte[]{dataValue});
            } else {
                data.insert(editedDataPosition, 1);
            }

            data.setByte(editedDataPosition, (byte) ((dataValue & 0xf) | (value << 4)));
            length++;
        }
        lowerHalf = !lowerHalf;
    }

    @Override
    public HexOperation[] generateUndo() {
        ModifyDataOperation modifyOperation = null;
        if (!undoData.isEmpty()) {
            modifyOperation = new ModifyDataOperation(hexadecimal, startPosition, undoData);
        }
        RemoveDataOperation removeOperation = new RemoveDataOperation(hexadecimal, startPosition + undoData.getDataSize(), startLowerHalf, length - undoData.getDataSize());

        if (modifyOperation != null) {
            return new HexOperation[]{modifyOperation, removeOperation};
        }
        return new HexOperation[]{removeOperation};
    }

    public long getStartPosition() {
        return startPosition;
    }

    public boolean isStartLowerHalf() {
        return startLowerHalf;
    }

    public long getLength() {
        return length;
    }
}
