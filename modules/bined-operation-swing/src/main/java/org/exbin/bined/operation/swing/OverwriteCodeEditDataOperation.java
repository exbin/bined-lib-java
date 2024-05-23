/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.operation.swing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.operation.BinaryDataOperation;

/**
 * Operation for editing data using overwrite mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OverwriteCodeEditDataOperation extends CodeEditDataOperation {

    private final long startPosition;
    private final int startCodeOffset;
    private long length = 0;
    private EditableBinaryData undoData = null;
    private final CodeType codeType;
    private byte value;

    private int codeOffset = 0;

    public OverwriteCodeEditDataOperation(CodeAreaCore codeArea, long startPosition, int startCodeOffset, byte value) {
        super(codeArea);
        this.value = value;
        this.startPosition = startPosition;
        this.startCodeOffset = startCodeOffset;
        this.codeOffset = startCodeOffset;
        this.codeType = ((CodeTypeCapable) codeArea).getCodeType();
        if (startCodeOffset > 0 && codeArea.getDataSize() > startPosition) {
            undoData = (EditableBinaryData) codeArea.getContentData().copy(startPosition, 1);
            length++;
        }
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
    }

    @Nonnull
    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    @Nullable
    @Override
    protected CodeAreaOperation execute(ExecutionType executionType) {
        CodeAreaOperation undoOperation = null;
        if (executionType == ExecutionType.WITH_UNDO) {
            ModifyDataOperation modifyOperation = null;
            if (undoData != null && !undoData.isEmpty()) {
                modifyOperation = new ModifyDataOperation(codeArea, startPosition, undoData.copy());
            }
            long undoDataSize = undoData == null ? 0 : undoData.getDataSize();
            long removeLength = length - undoDataSize;
            if (removeLength == 0) {
                undoOperation = modifyOperation;
            } else {
                RemoveDataOperation removeOperation = new RemoveDataOperation(codeArea, startPosition + undoDataSize, startCodeOffset, removeLength);
                if (modifyOperation != null) {
                    CodeAreaCompoundOperation compoundOperation = new CodeAreaCompoundOperation(codeArea);
                    compoundOperation.addOperation(modifyOperation);
                    compoundOperation.addOperation(removeOperation);
                    undoOperation = compoundOperation;
                } else {
                    undoOperation = removeOperation;
                }
            }
        }

        appendEdit(value);

        return undoOperation;
    }

    @Override
    public boolean appendOperation(BinaryDataOperation operation) {
        if (operation instanceof OverwriteCharEditDataOperation) {
            OverwriteCharEditDataOperation overwriteOperation = (OverwriteCharEditDataOperation) operation;
            if (overwriteOperation.codeArea == codeArea) { // && overwriteOperation.position
                appendEdit(value);
                return true;
            }
        }

        return false;
    }

    private void appendEdit(byte value) {
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        long editedDataPosition = startPosition + length;

        byte byteValue = 0;
        if (codeOffset > 0) {
            if (editedDataPosition <= data.getDataSize()) {
                byteValue = data.getByte(editedDataPosition - 1);
            }

            editedDataPosition--;
        } else {
            if (editedDataPosition < data.getDataSize()) {
                if (undoData == null) {
                    undoData = (EditableBinaryData) data.copy(editedDataPosition, 1);
                    byteValue = undoData.getByte(0);
                } else {
                    undoData.insert(undoData.getDataSize(), data, editedDataPosition, 1);
                }
            } else if (editedDataPosition > data.getDataSize()) {
                throw new IllegalStateException("Cannot overwrite outside of the document");
            } else {
                data.insertUninitialized(editedDataPosition, 1);
            }

            length++;
        }

        byteValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);

        data.setByte(editedDataPosition, byteValue);
        codeOffset++;
        if (codeOffset == codeType.getMaxDigitsForByte()) {
            codeOffset = 0;
        }
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

    @Override
    public void dispose() {
        super.dispose();
        if (undoData != null) {
            undoData.dispose();
        }
    }
}
