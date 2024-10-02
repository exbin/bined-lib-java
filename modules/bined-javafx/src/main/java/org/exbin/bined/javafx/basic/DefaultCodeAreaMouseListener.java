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
package org.exbin.bined.javafx.basic;

import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.javafx.CodeAreaCommandHandler.SelectingMode;
import org.exbin.bined.javafx.CodeAreaCore;

/**
 * Code Area component mouse listener.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaMouseListener {

    public static final int MOUSE_SCROLL_LINES = 3;

    protected final CodeAreaCore codeArea;
    protected final ScrollPane view;

    protected final Cursor defaultCursor = Cursor.DEFAULT;
    protected final Cursor textCursor = Cursor.TEXT;
    protected Cursor currentCursor;
    protected boolean mouseDown = false;

    public DefaultCodeAreaMouseListener(CodeAreaCore codeArea, ScrollPane view) {
        this.codeArea = codeArea;
        this.view = view;
//        currentCursor = codeArea.getCursor();
    }

    public void mousePressed(MouseEvent me) {
        codeArea.requestFocus();
        if (!codeArea.isDisabled() && me.getButton() == MouseButton.PRIMARY) {
            moveCaret(me);
            mouseDown = true;
        }
    }

    private void moveCaret(MouseEvent me) {
        SelectingMode selecting = me.isShiftDown() ? SelectingMode.SELECTING : SelectingMode.NONE;
        codeArea.getCommandHandler().moveCaret(computeRelativeX(me), computeRelativeY(me), selecting);
        ((ScrollingCapable) codeArea).revealCursor();
    }

    public void mouseReleased(MouseEvent me) {
        mouseDown = false;
    }

    public void mouseExited(MouseEvent me) {
        currentCursor = defaultCursor;
        codeArea.setCursor(defaultCursor);
    }

    public void mouseEntered(MouseEvent me) {
        updateMouseCursor(me);
    }

    public void mouseMoved(MouseEvent me) {
        updateMouseCursor(me);
    }

    private void updateMouseCursor(MouseEvent me) {
        int cursorShape = ((CaretCapable) codeArea).getMouseCursorShape((int) computeRelativeX(me), (int) computeRelativeY(me));

        // Reuse current cursor if unchanged
        Cursor newCursor = cursorShape == 0 ? defaultCursor : textCursor;
        if (newCursor != currentCursor) {
            currentCursor = newCursor;
            codeArea.setCursor(newCursor);
        }
    }

    public void mouseDragged(MouseEvent me) {
        updateMouseCursor(me);
        if (!codeArea.isDisabled() && mouseDown) {
            codeArea.getCommandHandler().moveCaret(computeRelativeX(me), computeRelativeY(me), SelectingMode.SELECTING);
            ((ScrollingCapable) codeArea).revealCursor();
        }
    }

    private double computeRelativeX(MouseEvent me) {
        boolean isDataView = me.getSource() != codeArea;
        return isDataView ? me.getSceneX() + view.getScene().getX() : me.getSceneX();
    }

    private double computeRelativeY(MouseEvent me) {
        boolean isDataView = me.getSource() != codeArea;
        return isDataView ? me.getSceneY() + view.getScene().getY() : me.getSceneY();
    }

    public void mouseWheelMoved(MouseEvent me) {
//        if (!codeArea.isEnabled() || me.getWheelRotation() == 0) {
//            return;
//        }

//        ScrollbarOrientation orientation = me.isShiftDown() ? CodeAreaCommandHandler.ScrollbarOrientation.HORIZONTAL : CodeAreaCommandHandler.ScrollbarOrientation.VERTICAL;
//        int scrollAmount = me.getWheelRotation() > 0 ? MOUSE_SCROLL_LINES : -MOUSE_SCROLL_LINES;
//        codeArea.getCommandHandler().wheelScroll(scrollAmount, orientation);
    }
}
