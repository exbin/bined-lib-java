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
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.BinaryDataAbstractCommand;
import org.exbin.bined.operation.swing.CodeAreaState;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Abstract class for operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class CodeAreaCommand extends BinaryDataAbstractCommand {

    @Nonnull
    protected final CodeAreaCore codeArea;
    protected CodeAreaState beforeState;
    protected CodeAreaState afterState;

    public CodeAreaCommand(CodeAreaCore codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void redo() {
        performRedo();
        restoreState(afterState);
    }

    @Override
    public void execute() {
        beforeState = fetchState();
        performExecute();
        afterState = fetchState();
    }

    @Override
    public void undo() {
        performUndo();
        restoreState(beforeState);
    }

    /**
     * Executes main command.
     */
    public void performRedo() {
        performExecute();
    }

    /**
     * Executes main command.
     */
    public abstract void performExecute();

    /**
     * Executes main undo command.
     */
    public abstract void performUndo();

    @Nonnull
    public CodeAreaState fetchState() {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        return new CodeAreaState(caretPosition, selection);
    }

    public void restoreState(CodeAreaState codeAreaState) {
        ((CaretCapable) codeArea).setActiveCaretPosition(codeAreaState.getCaretPosition());
        ((SelectionCapable) codeArea).setSelection(codeAreaState.getSelection());
    }
}
