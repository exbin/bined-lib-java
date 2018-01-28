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
package org.exbin.deltahex.swing;

import java.awt.Graphics;
import javax.annotation.Nonnull;
import org.exbin.deltahex.CaretPosition;

/**
 * Hexadecimal editor painter interface.
 *
 * @version 0.2.0 2018/01/07
 * @author ExBin Project (http://exbin.org)
 */
public interface CodeAreaPainter {

    /**
     * Returns true if painter was initialized.
     *
     * @return true if initialized
     */
    boolean isInitialized();

    /**
     * Paints the main component.
     *
     * @param g graphics
     */
    void paintComponent(@Nonnull Graphics g);

    /**
     * Paints main hexadecimal data section of the component.
     *
     * @param g graphics
     */
    void paintMainArea(@Nonnull Graphics g);

    /**
     * Paints cursor symbol.
     *
     * @param g graphics
     */
    void paintCursor(@Nonnull Graphics g);

    /**
     * Resets painter state for new painting.
     */
    void reset();

    /**
     * Returns type of cursor for given painter relative position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @return java.awt.Cursor cursor type value
     */
    int getCursorShape(int positionX, int positionY);

    /**
     * Returns closest caret position for provided component relative mouse
     * position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @return closest caret position
     */
    @Nonnull
    CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY);

    void updateScrollBars();

    /**
     * Attempts to scroll component so that provided caret position is visible.
     *
     * Performs minimal scrolling and tries to preserve current vertical /
     * horizontal scrolling if possible. If given position cannot be fully
     * shown, top left corner is preferred.
     *
     * @param caretPosition caret position
     * @return true if any scrolling was performed
     */
    boolean revealPosition(@Nonnull CaretPosition caretPosition);

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    @Nonnull
    CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementShift direction);
}
