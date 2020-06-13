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
package org.exbin.bined.swt;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.graphics.GC;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.basic.PositionScrollVisibility;

/**
 * Binary editor painter interface.
 *
 * @version 0.2.0 2019/06/13
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CodeAreaPainter {

    /**
     * Returns true if painter was initialized.
     *
     * @return true if initialized
     */
    boolean isInitialized();

    /**
     * Attaches painter to code area.
     */
    void attach();

    /**
     * Detaches painter to code area.
     */
    void detach();

    /**
     * Paints the main component.
     *
     * @param g graphics
     */
    void paintComponent(GC g);

    /**
     * Paints main hexadecimal data section of the component.
     *
     * @param g graphics
     */
    void paintMainArea(GC g);

    /**
     * Paints cursor symbol.
     *
     * @param g graphics
     */
    void paintCursor(GC g);

    /**
     * Resets complete painter state for new painting.
     */
    void reset();

    /**
     * Resets painter font state for new painting.
     */
    void resetFont();

    /**
     * Rebuilds colors after UIManager change.
     */
    void resetColors();

    /**
     * Updates painter layout state for new painting.
     */
    void resetLayout();

    /**
     * Resets caret state.
     */
    void resetCaret();

    /**
     * Returns type of cursor for given painter relative position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @return java.awt.Cursor cursor type value
     */
    int getMouseCursorShape(int positionX, int positionY);

    /**
     * Returns zone type for given position.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return specific zone in component
     */
    @Nonnull
    BasicCodeAreaZone getPositionZone(int x, int y);

    /**
     * Returns closest caret position for provided component relative mouse
     * position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @param overlowMode overflow mode
     * @return closest caret position
     */
    @Nonnull
    CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overlowMode);

    void updateScrollBars();

    /**
     * Returns state of the visibility of given caret position within current
     * scrolling window.
     *
     * @param caretPosition caret position
     * @return visibility state
     */
    @Nonnull
    PositionScrollVisibility computePositionScrollVisibility(CodeAreaCaretPosition caretPosition);

    /**
     * Returns scroll position so that provided caret position is visible in
     * scrolled area.
     *
     * Performs minimal scrolling and tries to preserve current vertical /
     * horizontal scrolling if possible. If given position cannot be fully
     * shown, top left corner is preferred.
     *
     * @param caretPosition caret position
     * @return scroll position or null if caret position is already visible /
     * scrolled to the best fit
     */
    @Nonnull
    Optional<CodeAreaScrollPosition> computeRevealScrollPosition(CodeAreaCaretPosition caretPosition);

    /**
     * Returns scroll position so that provided caret position is visible in the
     * center of the scrolled area.
     *
     * Attempts to center as much as possible while preserving scrolling limits.
     *
     * @param caretPosition caret position
     * @return scroll position or null if desired scroll position is the same as
     * current scroll position.
     */
    @Nonnull
    Optional<CodeAreaScrollPosition> computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition);

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    @Nonnull
    CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction);

    /**
     * Computes scrolling position for given shift action.
     *
     * @param startPosition start position
     * @param direction scrolling direction
     * @return target position
     */
    @Nonnull
    CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction);

    void repaint();

    void dispose();
}
