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
import org.exbin.deltahex.delta.MemoryHexadecimalData;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data unsing insert mode.
 *
 * @version 0.1.0 2015/05/14
 * @author ExBin Project (http://exbin.org)
 */
public class InsertHexEditDataOperation extends HexEditDataOperation {

    private final long startPosition;
    private final boolean startLowerHalf;
    private boolean trailing = false;

    private long length;
    private boolean lowerHalf = false;

    public InsertHexEditDataOperation(Hexadecimal hexadecimal, long startPosition, boolean startLowerHalf) {
        super(hexadecimal);
        this.startPosition = startPosition;
        this.startLowerHalf = startLowerHalf;
        this.lowerHalf = startLowerHalf;
        if (lowerHalf) {
            length = 1;
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
    public HexOperation executeWithUndo() throws Exception {
        return execute(true);
    }

    private HexOperation execute(boolean withUndo) {
        throw new IllegalStateException("Cannot be executed");
    }

    @Override
    public void appendEdit(byte value) {
        EditableBinaryData data = (EditableBinaryData) hexadecimal.getData();
        long editedDataPosition = startPosition + length;

        if (lowerHalf) {
            byte lowerPart = (byte) (data.getByte(editedDataPosition - 1) & 0xf);
            if (lowerPart > 0) {
                data.insert(editedDataPosition, 1);
                data.setByte(editedDataPosition, lowerPart);
                trailing = true;
            }

            byte byteValue = (byte) (data.getByte(editedDataPosition - 1) & 0xf0);
            data.setByte(editedDataPosition - 1, (byte) (byteValue | value));
        } else {
            byte byteValue = (byte) (value << 4);
            data.insert(editedDataPosition, 1);
            data.setByte(editedDataPosition, byteValue);
            length++;
        }
        lowerHalf = !lowerHalf;
    }

    @Override
    public HexOperation[] generateUndo() {
        if (trailing) {
            MemoryHexadecimalData undoData = new MemoryHexadecimalData();
            BinaryData data = hexadecimal.getData();
            byte value = (byte) ((data.getByte(startPosition) & 0xf0) | (data.getByte(startPosition + length) & 0xf));
            undoData.insert(0, new byte[]{value});
            ModifyDataOperation modifyDataOperation = new ModifyDataOperation(hexadecimal, startPosition, undoData);
            return new HexOperation[]{modifyDataOperation, new RemoveDataOperation(hexadecimal, startPosition, startLowerHalf, length)};
        }

        return new HexOperation[]{new RemoveDataOperation(hexadecimal, startPosition, startLowerHalf, length)};
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
