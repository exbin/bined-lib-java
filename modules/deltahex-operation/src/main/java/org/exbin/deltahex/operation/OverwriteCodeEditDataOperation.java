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
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data using overwrite mode.
 *
 * @version 0.1.0 2015/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class OverwriteCodeEditDataOperation extends CodeEditDataOperation {

    private final long startPosition;
    private final int startCodeOffset;
    private long length = 0;
    private final MemoryPagedData undoData = new MemoryPagedData();

    private int codeOffset = 0;

    public OverwriteCodeEditDataOperation(CodeArea codeArea, long startPosition, int startCodeOffset) {
        super(codeArea);
        this.startPosition = startPosition;
        this.startCodeOffset = startCodeOffset;
        this.codeOffset = startCodeOffset;
        if (startCodeOffset > 0 && codeArea.getData().getDataSize() > startPosition) {
            undoData.insert(0, new byte[]{codeArea.getData().getByte(startPosition)});
            length++;
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
        // TODO other code types
        codeOffset = 1 - codeOffset;
    }

    @Override
    public CodeAreaOperation[] generateUndo() {
        ModifyDataOperation modifyOperation = null;
        if (!undoData.isEmpty()) {
            modifyOperation = new ModifyDataOperation(codeArea, startPosition, undoData);
        }
        RemoveDataOperation removeOperation = new RemoveDataOperation(codeArea, startPosition + undoData.getDataSize(), startCodeOffset, length - undoData.getDataSize());

        if (modifyOperation != null) {
            return new CodeAreaOperation[]{modifyOperation, removeOperation};
        }
        return new CodeAreaOperation[]{removeOperation};
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
