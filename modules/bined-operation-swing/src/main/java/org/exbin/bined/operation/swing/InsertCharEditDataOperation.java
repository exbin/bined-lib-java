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
 * Operation for editing data using insert mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertCharEditDataOperation extends CharEditDataOperation {

    private final long startPosition;
    private long length;
    private char key;

    public InsertCharEditDataOperation(CodeAreaCore coreArea, long startPosition, char key) {
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
            undoOperation = new RemoveDataOperation(codeArea, startPosition, 0, length);
        }
        
        appendEdit(key);
        
        return undoOperation;
    }

    @Override
    public boolean appendOperation(BinaryDataOperation operation) {
        if (operation instanceof InsertCharEditDataOperation) {
            InsertCharEditDataOperation insertOperation = (InsertCharEditDataOperation) operation;
            if (insertOperation.codeArea == codeArea) { // && insertOperation.position
                appendEdit(key);
                return true;
            }
        }
        
        return false;
    }

    private void appendEdit(char value) {
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        long editedDataPosition = startPosition + length;

        Charset charset = ((CharsetCapable) codeArea).getCharset();
        byte[] bytes = CodeAreaUtils.characterToBytes(value, charset);
        data.insert(editedDataPosition, bytes);
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
}
