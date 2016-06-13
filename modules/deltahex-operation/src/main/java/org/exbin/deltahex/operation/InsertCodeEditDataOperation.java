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

import org.exbin.deltahex.CodeArea;
import org.exbin.deltahex.delta.MemoryPagedData;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data unsing insert mode.
 *
 * @version 0.1.0 2015/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class InsertCodeEditDataOperation extends CodeEditDataOperation {

    private final long startPosition;
    private final int startCodeOffset;
    private boolean trailing = false;

    private long length;
    private int codeOffset = 0;

    public InsertCodeEditDataOperation(CodeArea codeArea, long startPosition, int startCodeOffset) {
        super(codeArea);
        this.startPosition = startPosition;
        this.startCodeOffset = startCodeOffset;
        this.codeOffset = startCodeOffset;
        if (codeOffset > 0) {
            length = 1;
        }
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
    }

    @Override
    public void execute() throws Exception {
        execute(false);
    }

    @Override
    public CodeAreaOperation executeWithUndo() throws Exception {
        return execute(true);
    }

    private CodeAreaOperation execute(boolean withUndo) {
        throw new IllegalStateException("Cannot be executed");
    }

    @Override
    public void appendEdit(byte value) {
        EditableBinaryData data = (EditableBinaryData) codeArea.getData();
        long editedDataPosition = startPosition + length;

        if (codeOffset > 0) {
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
        // TODO other code types
        codeOffset = 1 - codeOffset;
    }

    @Override
    public CodeAreaOperation[] generateUndo() {
        if (trailing) {
            MemoryPagedData undoData = new MemoryPagedData();
            BinaryData data = codeArea.getData();
            byte value = (byte) ((data.getByte(startPosition) & 0xf0) | (data.getByte(startPosition + length) & 0xf));
            undoData.insert(0, new byte[]{value});
            ModifyDataOperation modifyDataOperation = new ModifyDataOperation(codeArea, startPosition, undoData);
            return new CodeAreaOperation[]{modifyDataOperation, new RemoveDataOperation(codeArea, startPosition, startCodeOffset, length)};
        }

        return new CodeAreaOperation[]{new RemoveDataOperation(codeArea, startPosition, startCodeOffset, length)};
    }

    public long getStartPosition() {
        return startPosition;
    }

    public int getStartCodeOffset() {
        return startCodeOffset;
    }

    public long getLength() {
        return length;
    }
}
