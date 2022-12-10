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
package org.exbin.bined.operation.swing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.CodeAreaCaretPosition;

/**
 * Abstract class for operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class CodeAreaOperation implements BinaryDataOperation {

    @Nonnull
    protected final CodeAreaCore codeArea;
    @Nonnull
    protected final DefaultCodeAreaCaretPosition backPosition = new DefaultCodeAreaCaretPosition();

    public CodeAreaOperation(CodeAreaCore codeArea) {
        this(codeArea, null);
    }

    public CodeAreaOperation(CodeAreaCore codeArea, @Nullable CodeAreaCaretPosition backPosition) {
        this.codeArea = codeArea;
        if (backPosition != null) {
            this.backPosition.setPosition(backPosition);
        }
    }

    /**
     * Returns type of the operation.
     *
     * @return operation type
     */
    @Nonnull
    public abstract CodeAreaOperationType getType();

    @Nonnull
    public CodeAreaCore getCodeArea() {
        return codeArea;
    }

    /**
     * Returns caption as text.
     *
     * @return text caption
     */
    @Nonnull
    @Override
    public String getCaption() {
        String caption = getType().getCaption();
        return caption == null ? "" : caption;
    }

    @Nonnull
    public CodeAreaCaretPosition getBackPosition() {
        return backPosition;
    }

    public void setBackPosition(CodeAreaCaretPosition backPosition) {
        this.backPosition.setPosition(backPosition);
    }

    /**
     * Performs operation on given document.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void execute() throws BinaryDataOperationException {
        execute(ExecutionType.NORMAL);
    }

    /**
     * Performs operation on given document and returns undo operation.
     *
     * @return undo operation or null if not available
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Nullable
    @Override
    public CodeAreaOperation executeWithUndo() throws BinaryDataOperationException {
        return execute(ExecutionType.WITH_UNDO);
    }

    /**
     * Default empty execution method supporting both modes ready for override.
     *
     * @param executionType if undo should be included
     * @return undo operation or null if not available
     */
    @Nullable
    protected CodeAreaOperation execute(ExecutionType executionType) {
        return null;
    }

    /**
     * Performs dispose of the operation.
     *
     * Default dispose is empty.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void dispose() throws BinaryDataOperationException {
    }

    public enum ExecutionType {
        NORMAL, WITH_UNDO
    }
}
