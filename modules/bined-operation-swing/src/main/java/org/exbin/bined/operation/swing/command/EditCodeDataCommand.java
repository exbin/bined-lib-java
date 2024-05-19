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
import org.exbin.bined.operation.BinaryDataOperationListener;
import org.exbin.bined.operation.swing.CodeAreaOperation;
import org.exbin.bined.operation.swing.CodeAreaOperationEvent;
import org.exbin.bined.operation.swing.CodeAreaOperationListener;
import org.exbin.bined.operation.swing.CodeEditDataOperation;
import org.exbin.bined.operation.swing.DeleteCodeEditDataOperation;
import org.exbin.bined.operation.swing.InsertCodeEditDataOperation;
import org.exbin.bined.operation.swing.OverwriteCodeEditDataOperation;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Command for editing data in hexadecimal mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditCodeDataCommand extends EditDataCommand {

    private final EditCommandType commandType;
    protected boolean operationPerformed = false;
    private CodeAreaOperation[] operations = null;
    private byte value;

    public EditCodeDataCommand(CodeAreaCore codeArea, EditCommandType commandType, long position, int positionCodeOffset, byte value) {
        super(codeArea);
        this.commandType = commandType;
        this.value = value;
        CodeAreaOperation operation;
        switch (commandType) {
            case INSERT: {
                operation = new InsertCodeEditDataOperation(codeArea, position, positionCodeOffset, value);
                break;
            }
            case OVERWRITE: {
                operation = new OverwriteCodeEditDataOperation(codeArea, position, positionCodeOffset, value);
                break;
            }
            case DELETE: {
                operation = new DeleteCodeEditDataOperation(codeArea, position);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(commandType);
        }
        operations = new CodeAreaOperation[]{operation};
    }

    @Override
    public void execute() {
        if (operationPerformed) {
            throw new IllegalStateException();
        }

        appendEdit(value);
        operationPerformed = true;
    }

    @Override
    public void undo() {
        if (operations.length == 1 && operations[0] instanceof CodeEditDataOperation) {
            CodeEditDataOperation operation = (CodeEditDataOperation) operations[0];
            operations = operation.generateUndo();
            operation.dispose();
        }

        if (operationPerformed) {
            for (int i = operations.length - 1; i >= 0; i--) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation redoOperation = operation.executeWithUndo();
                operation.dispose();
                if (codeArea instanceof BinaryDataOperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
                }
                operations[i] = redoOperation;
            }
            operationPerformed = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() {
        if (!operationPerformed) {
            for (int i = 0; i < operations.length; i++) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation undoOperation = operation.executeWithUndo();
                operation.dispose();
                // TODO Drop listener?
                if (codeArea instanceof BinaryDataOperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operations[i]));
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

    /**
     * Appends next binary value in editing action sequence.
     *
     * @param value half-byte value (0..15)
     */
    public void appendEdit(byte value) {
        if (operations.length == 1 && operations[0] instanceof CodeEditDataOperation) {
            ((CodeEditDataOperation) operations[0]).appendEdit(value);
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
        return !(operations.length == 1 && operations[0] instanceof CodeEditDataOperation);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (operations != null) {
            for (CodeAreaOperation operation : operations) {
                operation.dispose();
            }
        }
    }
}
