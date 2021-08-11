/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.bined.operation.swing.command;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.InsertDataOperation;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.paged_data.EditableBinaryData;

/**
 * Command for inserting data.
 *
 * @version 0.2.0 2018/10/20
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataCommand extends OpCodeAreaCommand {

    private final long position;
    private final long dataLength;

    public InsertDataCommand(CodeAreaCore codeArea, long position, EditableBinaryData data) {
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
