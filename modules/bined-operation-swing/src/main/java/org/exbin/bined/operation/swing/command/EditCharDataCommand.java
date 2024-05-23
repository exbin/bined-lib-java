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
import org.exbin.bined.operation.BinaryDataAppendableCommand;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataCommandPhase;
import org.exbin.bined.operation.swing.CharEditDataOperation;
import org.exbin.bined.operation.swing.CodeAreaOperation;
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
public class EditCharDataCommand extends EditDataCommand implements BinaryDataAppendableCommand {

    @Nonnull
    private final EditCommandType commandType;
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;
    private char charData;
    private CodeAreaOperation[] operations = null;

    public EditCharDataCommand(CodeAreaCore codeArea, EditCommandType commandType, long position, char charData) {
        super(codeArea);
        this.charData = charData;
        this.commandType = commandType;
        CodeAreaOperation operation;
        switch (commandType) {
            case INSERT: {
                operation = new InsertCharEditDataOperation(codeArea, position, charData);
                break;
            }
            case OVERWRITE: {
                operation = new OverwriteCharEditDataOperation(codeArea, position, charData);
                break;
            }
            case DELETE: {
                operation = new DeleteCharEditDataOperation(codeArea, position, charData);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(commandType);
        }
        operations = new CodeAreaOperation[]{operation};
    }

    @Override
    public void execute() {
        if (phase == BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        for (int i = 0; i < operations.length; i++) {
            CodeAreaOperation operation = operations[i];
            CodeAreaOperation undoOperation = operation.executeWithUndo();
            operation.dispose();
            operations[i] = undoOperation;
        }
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void undo() {
        if (phase == BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }
        
        if (operations.length == 1 && operations[0] instanceof CharEditDataOperation) {
            CharEditDataOperation operation = (CharEditDataOperation) operations[0];
            operations = new CodeAreaOperation[] { operation.executeWithUndo() };
            operation.dispose();
        }

        for (int i = operations.length - 1; i >= 0; i--) {
            CodeAreaOperation operation = operations[i];
            CodeAreaOperation redoOperation = operation.executeWithUndo();
            operation.dispose();
            operations[i] = redoOperation;
        }
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void redo() {
        if (phase == BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        execute();
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_EDITED;
    }

    @Override
    public boolean appendCommand(BinaryDataCommand command) {
        if (command instanceof EditCharDataCommand && ((EditCharDataCommand) command).phase == BinaryDataCommandPhase.CREATED) {
            if (operations.length == 1 && operations[0] instanceof CharEditDataOperation) {
                return ((CharEditDataOperation) operations[0]).appendOperation(((EditCharDataCommand) command).operations[0]);
            }
        }
        
        return false;
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
    public void dispose() {
        super.dispose();
        if (operations != null) {
            for (CodeAreaOperation operation : operations) {
                operation.dispose();
            }
        }
    }
}
