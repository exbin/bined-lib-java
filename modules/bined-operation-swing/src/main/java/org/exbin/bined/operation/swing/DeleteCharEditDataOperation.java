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
import org.exbin.auxiliary.binary_data.BinaryData;

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.undo.BinaryDataAppendableOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data in delete mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeleteCharEditDataOperation extends CharEditDataOperation {

    private static final char BACKSPACE_CHAR = '\b';
    private static final char DELETE_CHAR = (char) 0x7f;

    protected long position;
    protected char value;

    public DeleteCharEditDataOperation(long startPosition, char value) {
        this.value = value;
        this.position = startPosition;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
    }

    @Override
    public void execute(EditableBinaryData contentData) {
        execute(contentData, false);
    }

    @Nonnull
    @Override
    public BinaryDataUndoableOperation executeWithUndo(EditableBinaryData contentData) {
        return CodeAreaUtils.requireNonNull(execute(contentData, true));
    }

    @Nullable
    private BinaryDataUndoableOperation execute(EditableBinaryData contentData, boolean withUndo) {
        BinaryDataUndoableOperation undoOperation = null;
        EditableBinaryData undoData = null;

        switch (value) {
            case BACKSPACE_CHAR: {
                if (position <= 0) {
                    throw new IllegalStateException("Cannot apply backspace on position " + position);
                }

                position--;
                undoData = (EditableBinaryData) contentData.copy(position, 1);
                contentData.remove(position, 1);
                break;
            }
            case DELETE_CHAR: {
                if (position >= contentData.getDataSize()) {
                    throw new IllegalStateException("Cannot apply delete on position " + position);
                }

                undoData = (EditableBinaryData) contentData.copy(position, 1);
                contentData.remove(position, 1);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected character " + value);
            }
        }

        if (withUndo) {
            undoOperation = new UndoOperation(position, 0, undoData, value);
        }
        return undoOperation;
    }

    @ParametersAreNonnullByDefault
    private static class UndoOperation extends InsertDataOperation implements BinaryDataAppendableOperation {

        private char value;

        public UndoOperation(long position, int codeOffset, BinaryData data, char value) {
            super(position, codeOffset, data);
            this.value = value;
        }

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation && ((UndoOperation) operation).value == value) {
                EditableBinaryData editableData = (EditableBinaryData) data;
                switch (value) {
                    case BACKSPACE_CHAR: {
                        editableData.insert(0, ((UndoOperation) operation).getData());
                        break;
                    }
                    case DELETE_CHAR: {
                        editableData.insert(editableData.getDataSize(), ((UndoOperation) operation).getData());
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unexpected character " + value);
                    }
                }
                return true;
            }

            return false;
        }
    }
}
