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

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.BinaryDataOperation;

/**
 * Operation for editing data using overwrite mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OverwriteCharEditDataOperation extends CharEditDataOperation {

    private final long startPosition;
    private long length = 0;
    private EditableBinaryData undoData = null;
    private char key;

    public OverwriteCharEditDataOperation(CodeAreaCore coreArea, long startPosition, char key) {
        super(coreArea);
        this.key = key;
        this.startPosition = startPosition;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
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
                RemoveDataOperation removeOperation = new RemoveDataOperation(codeArea, startPosition + undoDataSize, 0, removeLength);
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
        
        appendEdit(key);
        
        return undoOperation;
    }

    @Override
    public boolean appendOperation(BinaryDataOperation operation) {
        if (operation instanceof OverwriteCharEditDataOperation) {
            OverwriteCharEditDataOperation overwriteOperation = (OverwriteCharEditDataOperation) operation;
            if (overwriteOperation.codeArea == codeArea) { // && overwriteOperation.position
                appendEdit(key);
                return true;
            }
        }
        
        return false;
    }

    private void appendEdit(char value) {
        EditableBinaryData data = (EditableBinaryData) CodeAreaUtils.requireNonNull(codeArea.getContentData());
        long editedDataPosition = startPosition + length;

        Charset charset = ((CharsetCapable) codeArea).getCharset();
        byte[] bytes = CodeAreaUtils.characterToBytes(value, charset);
        if (editedDataPosition < data.getDataSize()) {
            long overwritten = data.getDataSize() - editedDataPosition;
            if (overwritten > bytes.length) {
                overwritten = bytes.length;
            }
            EditableBinaryData overwrittenData = (EditableBinaryData) data.copy(editedDataPosition, overwritten);
            if (undoData == null) {
                undoData = overwrittenData;
            } else {
                undoData.insert(undoData.getDataSize(), overwrittenData);
            }
            for (int i = 0; i < overwritten; i++) {
                data.setByte(editedDataPosition + i, bytes[i]);
            }
        }
        if (editedDataPosition + bytes.length > data.getDataSize()) {
            if (editedDataPosition == data.getDataSize()) {
                data.insert(editedDataPosition, bytes);
            } else {
                int inserted = (int) (editedDataPosition + bytes.length - data.getDataSize());
                long insertPosition = editedDataPosition + bytes.length - inserted;
                data.insert(insertPosition, inserted);
                for (int i = 0; i < inserted; i++) {
                    data.setByte(insertPosition + i, bytes[bytes.length - inserted + i]);
                }
            }
        }

        length += bytes.length;
        long dataPosition = startPosition + length;
        ((CaretCapable) codeArea).setCaretPosition(dataPosition);
        ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);
    }

    public long getStartPosition() {
        return startPosition;
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
