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
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.BinaryDataOperation;

/**
 * Operation for editing data in delete mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeleteCharEditDataOperation extends CharEditDataOperation {

    private static final char BACKSPACE_CHAR = '\b';
    private static final char DELETE_CHAR = (char) 0x7f;

    private long position;
    private char key;
    private EditableBinaryData undoData = null;

    public DeleteCharEditDataOperation(CodeAreaCore codeArea, long startPosition, char key) {
        super(codeArea);
        this.key = key;
        this.position = startPosition;
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
            undoOperation = new InsertDataOperation(codeArea, position, 0, undoData.copy());
        }
        
        appendEdit(key);
        
        return undoOperation;
    }

    @Override
    public boolean appendOperation(BinaryDataOperation operation) {
        if (operation instanceof DeleteCharEditDataOperation) {
            DeleteCharEditDataOperation deleteOperation = (DeleteCharEditDataOperation) operation;
            if (deleteOperation.codeArea == codeArea && deleteOperation.key == key) { // && deleteOperation.position
                appendEdit(key);
                return true;
            }
        }
        
        return false;
    }

    private void appendEdit(char value) {
        EditableBinaryData data = (EditableBinaryData) CodeAreaUtils.requireNonNull(codeArea.getContentData());
        switch (value) {
            case BACKSPACE_CHAR: {
                if (position > 0) {
                    position--;
                    if (undoData == null) {
                        undoData = (EditableBinaryData) data.copy(position, 1);
                    } else {
                        EditableBinaryData dataCopy = (EditableBinaryData) data.copy(position, 1);
                        undoData.insert(0, dataCopy);
                        dataCopy.dispose();
                    }
                    data.remove(position, 1);
                }
                break;
            }
            case DELETE_CHAR: {
                if (position < data.getDataSize()) {
                    if (undoData == null) {
                        undoData = (EditableBinaryData) data.copy(position, 1);
                    } else {
                        EditableBinaryData dataCopy = (EditableBinaryData) data.copy(position, 1);
                        undoData.insert(0, dataCopy);
                        dataCopy.dispose();
                    }
                    data.remove(position, 1);
                }
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected character " + value);
            }
        }
        ((CaretCapable) codeArea).setCaretPosition(position);
        ((SelectionCapable) codeArea).setSelection(position, position);
        codeArea.repaint();
    }

    public long getPosition() {
        return position;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (undoData != null) {
            undoData.dispose();
        }
    }
}
