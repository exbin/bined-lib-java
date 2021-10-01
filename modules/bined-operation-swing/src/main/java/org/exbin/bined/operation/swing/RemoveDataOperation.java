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
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.paged_data.EditableBinaryData;

/**
 * Operation for deleting child block.
 *
 * @version 0.2.1 2021/09/26
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class RemoveDataOperation extends CodeAreaOperation {

    private final long position;
    private final int codeOffset;
    private final long length;

    public RemoveDataOperation(CodeAreaCore codeArea, long position, int codeOffset, long length) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.length = length;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.REMOVE_DATA;
    }

    @Override
    protected CodeAreaOperation execute(ExecutionType executionType) {
        EditableBinaryData contentData = CodeAreaUtils.requireNonNull((EditableBinaryData) codeArea.getContentData());
        CodeAreaOperation undoOperation = null;
        if (executionType == ExecutionType.WITH_UNDO) {
            EditableBinaryData undoData = (EditableBinaryData) contentData.copy(position, length);
            undoOperation = new InsertDataOperation(codeArea, position, codeOffset, undoData);
        }
        contentData.remove(position, length);
        ((CaretCapable) codeArea).getCaret().setCaretPosition(position, codeOffset);
        return undoOperation;
    }
}
