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
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.undo.BinaryDataAppendableOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data using overwrite mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OverwriteCharEditDataOperation extends CharEditDataOperation {

    protected final long startPosition;
    protected long length = 0;
    protected char value;
    protected Charset charset;

    public OverwriteCharEditDataOperation(long startPosition, char value, Charset charset) {
        this.value = value;
        this.startPosition = startPosition;
        this.charset = charset;
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
        EditableBinaryData undoData;

        long editedDataPosition = startPosition + length;

        byte[] bytes = CodeAreaUtils.characterToBytes(value, charset);
        if (editedDataPosition < contentData.getDataSize()) {
            long overwritten = contentData.getDataSize() - editedDataPosition;
            if (overwritten > bytes.length) {
                overwritten = bytes.length;
            }
            undoData = (EditableBinaryData) contentData.copy(editedDataPosition, overwritten);
            for (int i = 0; i < overwritten; i++) {
                contentData.setByte(editedDataPosition + i, bytes[i]);
            }
        } else {
            undoData = (EditableBinaryData) contentData.copy(editedDataPosition, 0);
        }

        if (editedDataPosition + bytes.length > contentData.getDataSize()) {
            if (editedDataPosition == contentData.getDataSize()) {
                contentData.insert(editedDataPosition, bytes);
            } else {
                int inserted = (int) (editedDataPosition + bytes.length - contentData.getDataSize());
                long insertPosition = editedDataPosition + bytes.length - inserted;
                contentData.insert(insertPosition, inserted);
                for (int i = 0; i < inserted; i++) {
                    contentData.setByte(insertPosition + i, bytes[bytes.length - inserted + i]);
                }
            }
        }

        length += bytes.length;

        if (withUndo) {
            undoOperation = new UndoOperation(startPosition, undoData, length - undoData.getDataSize());
        }

        return undoOperation;
    }

    @ParametersAreNonnullByDefault
    private static class UndoOperation implements BinaryDataUndoableOperation, BinaryDataAppendableOperation {

        private final long position;
        private final BinaryData data;
        private long removeLength;

        public UndoOperation(long position, BinaryData data, long removeLength) {
            this.position = position;
            this.data = data;
            this.removeLength = removeLength;
        }

        @Nonnull
        @Override
        public CodeAreaOperationType getType() {
            return CodeAreaOperationType.MODIFY_DATA;
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

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation) {
                ((EditableBinaryData) data).insert(data.getDataSize(), ((UndoOperation) operation).data);
                removeLength += ((UndoOperation) operation).removeLength;
                return true;
            }

            return false;
        }

        @Nullable
        private BinaryDataUndoableOperation execute(EditableBinaryData contentData, boolean withUndo) {
            BinaryDataUndoableOperation undoOperation = null;
            RemoveDataOperation removeOperation = null;
            if (removeLength > 0) {
                removeOperation = new RemoveDataOperation(position + data.getDataSize(), 0, removeLength);
            }

            if (withUndo) {
                BinaryData undoData = contentData.copy(position, data.getDataSize());
                undoOperation = new ModifyDataOperation(position, undoData);
            }
            contentData.replace(position, data);
            if (removeOperation != null) {
                if (withUndo) {
                    CodeAreaCompoundOperation compoundOperation = new CodeAreaCompoundOperation();
                    compoundOperation.addOperation(removeOperation.executeWithUndo(contentData));
                    compoundOperation.addOperation(undoOperation);
                    undoOperation = compoundOperation;
                } else {
                    removeOperation.execute(contentData);
                }
            }
            return undoOperation;
        }

        @Override
        public void dispose() {
        }
    }
}
