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
package org.exbin.bined.javafx;

import javafx.scene.input.KeyEvent;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for code area data manipulation.
 *
 * @version 0.2.0 2018/12/25
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CodeAreaCommandHandler {

    /**
     * Notifies command handler about end of sequence of append-able commands.
     */
    void undoSequenceBreak();

    /**
     * Keyboard key was pressed.
     *
     * @param keyEvent key event
     */
    void keyPressed(KeyEvent keyEvent);

    /**
     * Keyboard key was typed.
     *
     * @param keyEvent key event
     */
    void keyTyped(KeyEvent keyEvent);

    /**
     * Backspace key was pressed.
     */
    void backSpacePressed();

    /**
     * Delete key was pressed.
     */
    void deletePressed();

    /**
     * Deletes selection.
     */
    void delete();

    /**
     * Copies selection to clipboard.
     */
    void copy();

    /**
     * Copies selection to clipboard as code string.
     */
    void copyAsCode();

    /**
     * Cuts selection to clipboard.
     */
    void cut();

    /**
     * Pastes content of clipboard to cursor area.
     */
    void paste();

    /**
     * Expands selection to all data.
     */
    void selectAll();

    /**
     * Clears data selection.
     */
    void clearSelection();

    /**
     * Pastes content of clipboard to cursor area analyzing string code.
     */
    void pasteFromCode();

    /**
     * Returns true if paste action is possible.
     *
     * @return true if paste is possible
     */
    boolean canPaste();

    /**
     * Move caret with mouse event.
     *
     * @param positionX relative position X
     * @param positionY relative position Y
     * @param selecting selection selecting
     */
    void moveCaret(int positionX, int positionY, SelectingMode selecting);

    /**
     * Performs scrolling.
     *
     * @param scrollSize number of scroll units (positive or negative)
     * @param orientation scrollbar orientation
     */
    void wheelScroll(int scrollSize, ScrollbarOrientation orientation);

    enum ScrollbarOrientation {
        HORIZONTAL, VERTICAL
    }

    enum SelectingMode {
        NONE, SELECTING
    }

    @ParametersAreNonnullByDefault
    interface CodeAreaCommandHandlerFactory {

        @Nonnull
        CodeAreaCommandHandler createCommandHandler(CodeAreaCore codeArea);
    }
}
