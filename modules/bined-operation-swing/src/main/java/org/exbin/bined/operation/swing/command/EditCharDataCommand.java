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
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.operation.command.BinaryDataCommand;
import org.exbin.bined.operation.command.BinaryDataCommandPhase;
import org.exbin.bined.operation.swing.DeleteCharEditDataOperation;
import org.exbin.bined.operation.swing.InsertCharEditDataOperation;
import org.exbin.bined.operation.swing.OverwriteCharEditDataOperation;
import org.exbin.bined.operation.command.BinaryDataAppendableCommand;
import org.exbin.bined.operation.BinaryDataAppendableOperation;
import org.exbin.bined.operation.BinaryDataUndoableOperation;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Command for editing data in text mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditCharDataCommand extends EditDataCommand implements BinaryDataAppendableCommand {

    @Nonnull
    protected final EditOperationType editOperationType;
    @Nonnull
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;
    @Nonnull
    protected BinaryDataUndoableOperation activeOperation;

    public EditCharDataCommand(CodeAreaCore codeArea, EditOperationType editOperationType, long position, char charData) {
        super(codeArea);
        this.editOperationType = editOperationType;
        switch (editOperationType) {
            case INSERT: {
                activeOperation = new InsertCharEditDataOperation(position, charData, ((CharsetCapable) codeArea).getCharset());
                break;
            }
            case OVERWRITE: {
                activeOperation = new OverwriteCharEditDataOperation(position, charData, ((CharsetCapable) codeArea).getCharset());
                break;
            }
            case DELETE: {
                activeOperation = new DeleteCharEditDataOperation(position, charData);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(editOperationType);
        }
    }

    @Override
    public void performExecute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }
        
        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo(contentData);
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void performUndo() {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo(contentData);
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void performRedo() {
        if (phase != BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo(contentData);
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_EDITED;
    }

    @Override
    public boolean appendExecute(BinaryDataCommand command) {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        command.execute();

        if (command instanceof EditCharDataCommand && activeOperation instanceof BinaryDataAppendableOperation) {
            return ((BinaryDataAppendableOperation) activeOperation).appendOperation(((EditCharDataCommand) command).activeOperation);
        }

        return false;
    }

    @Nonnull
    @Override
    public EditOperationType getEditOperationType() {
        return editOperationType;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (activeOperation != null) {
            activeOperation.dispose();
        }
    }
}
