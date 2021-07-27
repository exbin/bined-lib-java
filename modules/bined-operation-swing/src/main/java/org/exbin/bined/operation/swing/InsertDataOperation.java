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
package org.exbin.bined.operation.swing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;

/**
 * Operation for inserting data.
 *
 * @version 0.1.2 2017/01/02
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataOperation extends CodeAreaOperation {

    private final long position;
    private final int codeOffset;
    private final EditableBinaryData data;

    public InsertDataOperation(CodeAreaCore codeArea, long position, int codeOffset, EditableBinaryData data) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.data = data;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.INSERT_DATA;
    }

    @Nullable
    @Override
    public void execute() throws BinaryDataOperationException {
        execute(false);
    }

    @Nullable
    @Override
    public CodeAreaOperation executeWithUndo() throws BinaryDataOperationException {
        return execute(true);
    }

    @Nullable
    private CodeAreaOperation execute(boolean withUndo) {
        CodeAreaOperation undoOperation = null;
        ((EditableBinaryData) codeArea.getContentData()).insert(position, data);
        if (withUndo) {
            undoOperation = new RemoveDataOperation(codeArea, position, codeOffset, data.getDataSize());
        }
        ((CaretCapable) codeArea).getCaret().setCaretPosition(position + data.getDataSize(), codeOffset);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        data.insert(data.getDataSize(), appendData);
    }

    @Override
    public void dispose() throws BinaryDataOperationException {
        super.dispose();
        data.dispose();
    }
}
