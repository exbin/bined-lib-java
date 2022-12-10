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
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.BinaryDataOperationListener;
import org.exbin.bined.operation.swing.CharEditDataOperation;
import org.exbin.bined.operation.swing.CodeAreaOperation;
import org.exbin.bined.operation.swing.CodeAreaOperationEvent;
import org.exbin.bined.operation.swing.CodeAreaOperationListener;
import org.exbin.bined.operation.swing.DeleteCharEditDataOperation;
import org.exbin.bined.operation.swing.InsertCharEditDataOperation;
import org.exbin.bined.operation.swing.OverwriteCharEditDataOperation;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Command for editing data in text mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditCharDataCommand extends EditDataCommand {

    @Nonnull
    private final EditCommandType commandType;
    protected boolean operationPerformed = false;
    private CodeAreaOperation[] operations = null;

    public EditCharDataCommand(CodeAreaCore codeArea, EditCommandType commandType, long position) {
        super(codeArea);
        this.commandType = commandType;
        CodeAreaOperation operation;
        switch (commandType) {
            case INSERT: {
                operation = new InsertCharEditDataOperation(codeArea, position);
                break;
            }
            case OVERWRITE: {
                operation = new OverwriteCharEditDataOperation(codeArea, position);
                break;
            }
            case DELETE: {
                operation = new DeleteCharEditDataOperation(codeArea, position);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(commandType);
        }
        operations = new CodeAreaOperation[]{operation};
        operationPerformed = true;
    }

    @Override
    public void undo() throws BinaryDataOperationException {
        if (operations.length == 1 && operations[0] instanceof CharEditDataOperation) {
            CharEditDataOperation operation = (CharEditDataOperation) operations[0];
            operations = operation.generateUndo();
            operation.dispose();
        }

        if (operationPerformed) {
            for (int i = operations.length - 1; i >= 0; i--) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation redoOperation = operation.executeWithUndo();
                operation.dispose();
                if (codeArea instanceof BinaryDataOperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operations[i]));
                }
                operations[i] = redoOperation;
            }
            operationPerformed = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() throws BinaryDataOperationException {
        if (!operationPerformed) {
            for (int i = 0; i < operations.length; i++) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation undoOperation = operation.executeWithUndo();
                operation.dispose();
                if (codeArea instanceof BinaryDataOperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
                }

                operations[i] = undoOperation;
            }
            operationPerformed = true;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_EDITED;
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    public void appendEdit(char value) {
        if (operations.length == 1 && operations[0] instanceof CharEditDataOperation) {
            ((CharEditDataOperation) operations[0]).appendEdit(value);
        } else {
            throw new IllegalStateException("Cannot append edit on reverted command");
        }
    }

    @Nonnull
    @Override
    public EditCommandType getCommandType() {
        return commandType;
    }

    @Override
    public boolean wasReverted() {
        return !(operations.length == 1 && operations[0] instanceof CharEditDataOperation);
    }

    @Override
    public void dispose() throws BinaryDataOperationException {
        super.dispose();
        if (operations != null) {
            for (CodeAreaOperation operation : operations) {
                operation.dispose();
            }
        }
    }
}
