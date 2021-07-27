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
package org.exbin.bined.capability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.basic.MovementDirection;

/**
 * Support for caret / cursor capability.
 *
 * @version 0.2.0 2021/06/12
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CaretCapable {

    /**
     * Returns handler for caret.
     *
     * @return caret handler
     */
    @Nonnull
    CodeAreaCaret getCaret();

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
     * Computes closest caret position for given relative component position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @param overflowMode overflow mode
     * @return mouse position
     */
    @Nonnull
    CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode);

    /**
     * Returns if cursor should be visible in other sections.
     *
     * @return true if cursor should be mirrored
     */
    boolean isShowMirrorCursor();

    /**
     * Sets if cursor should be visible in other sections.
     *
     * @param showMirrorCursor true if cursor should be mirrored
     */
    void setShowMirrorCursor(boolean showMirrorCursor);

    /**
     * Notifies component, that caret position has changed.
     */
    void notifyCaretMoved();

    /**
     * Notifies component, that caret state has changed.
     */
    void notifyCaretChanged();

    /**
     * Returns cursor shape type for given position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @return cursor type from java.awt.Cursor
     */
    int getMouseCursorShape(int positionX, int positionY);

    /**
     * Adds caret movement listener.
     *
     * @param caretMovedListener listener
     */
    void addCaretMovedListener(CaretMovedListener caretMovedListener);

    /**
     * Removes caret movement listener.
     *
     * @param caretMovedListener listener
     */
    void removeCaretMovedListener(CaretMovedListener caretMovedListener);
}
