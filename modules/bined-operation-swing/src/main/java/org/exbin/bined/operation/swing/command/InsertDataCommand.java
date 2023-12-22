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
package org.exbin.bined.operation.swing.command;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.InsertDataOperation;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Command for inserting data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataCommand extends OpCodeAreaCommand {

    private final long position;
    private final long dataLength;

    public InsertDataCommand(CodeAreaCore codeArea, long position, BinaryData data) {
        super(codeArea);
        this.position = position;
        dataLength = data.getDataSize();
        super.setOperation(new InsertDataOperation(codeArea, position, 0 /* TODO codeArea.getCaretPosition().getCodeOffset() */, data));
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_INSERTED;
    }

    @Override
    public void redo() throws BinaryDataOperationException {
        super.redo();
        ((CaretCapable) codeArea).setCaretPosition(position + dataLength);
    }

    @Override
    public void undo() throws BinaryDataOperationException {
        super.undo();
        ((CaretCapable) codeArea).setCaretPosition(position);
    }
}
