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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.operation.BinaryDataOperationListener;
import org.exbin.bined.operation.swing.CodeAreaOperation;
import org.exbin.bined.operation.swing.CodeAreaOperationEvent;
import org.exbin.bined.operation.swing.CodeAreaOperationListener;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Abstract class for operation on hexadecimal document.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class OpCodeAreaCommand extends CodeAreaCommand {

    @Nullable
    protected CodeAreaOperation operation;
    protected boolean operationPerformed = false;

    public OpCodeAreaCommand(CodeAreaCore codeArea) {
        super(codeArea);
    }

    @Nullable
    public CodeAreaOperation getOperation() {
        return operation;
    }

    public void setOperation(CodeAreaOperation operation) {
        if (this.operation != null) {
            try {
                this.operation.dispose();
            } catch (Exception ex) {
                Logger.getLogger(OpCodeAreaCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.operation = operation;
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public void undo() {
        if (operationPerformed) {
            CodeAreaOperation redoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo();
            operation.dispose();
            if (codeArea instanceof BinaryDataOperationListener) {
                ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
            }

            operation = redoOperation;
            operationPerformed = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() {
        if (!operationPerformed) {
            CodeAreaOperation undoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo();
            operation.dispose();
            if (codeArea instanceof BinaryDataOperationListener) {
                ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
            }

            operation = undoOperation;
            operationPerformed = true;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (operation != null) {
            operation.dispose();
        }
    }
}
