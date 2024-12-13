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
package org.exbin.bined.swt.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Cursor;
import org.exbin.bined.basic.SelectingMode;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.swt.CodeAreaCore;

/**
 * Code Area component mouse listener.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaMouseListener implements MouseListener, MouseMoveListener, MouseWheelListener, MouseTrackListener {

    public static final int MOUSE_SCROLL_LINES = 3;

    protected final CodeAreaCore codeArea;
    protected final DefaultCodeAreaScrollPane view;

    protected final Cursor defaultCursor = new Cursor(null, SWT.CURSOR_ARROW); // Cursor.getDefaultCursor();
    protected final Cursor textCursor = new Cursor(null, SWT.CURSOR_IBEAM);
    protected Cursor currentCursor;
    protected boolean mouseDown = false;

    public DefaultCodeAreaMouseListener(CodeAreaCore codeArea, DefaultCodeAreaScrollPane view) {
        this.codeArea = codeArea;
        this.view = view;
        currentCursor = codeArea.getCursor();
    }

    @Override
    public void mouseDoubleClick(MouseEvent me) {
    }

    @Override
    public void mouseDown(MouseEvent me) {
        codeArea.forceFocus();
        if (codeArea.isEnabled() && me.button == 1) {
            moveCaret(me);
            mouseDown = true;
        }
    }

    @Override
    public void mouseUp(MouseEvent me) {
        mouseDown = false;
    }

    private void moveCaret(MouseEvent me) {
        int relativeX = computeRelativeX(me);
        int relativeY = computeRelativeY(me);
        boolean selecting = (me.stateMask & SWT.SHIFT) > 0;
        codeArea.getCommandHandler().moveCaret(relativeX, relativeY, selecting ? SelectingMode.SELECTING : SelectingMode.NONE);
        ((ScrollingCapable) codeArea).revealCursor();
    }

    @Override
    public void mouseExit(MouseEvent me) {
        currentCursor = defaultCursor;
        codeArea.setCursor(defaultCursor);
    }

    @Override
    public void mouseEnter(MouseEvent me) {
        updateMouseCursor(me);
    }

    @Override
    public void mouseHover(MouseEvent me) {
        updateMouseCursor(me);
    }

    private void updateMouseCursor(MouseEvent me) {
        int cursorShape = ((CaretCapable) codeArea).getMouseCursorShape(computeRelativeX(me), computeRelativeY(me));

        // Reuse current cursor if unchanged
        Cursor newCursor = cursorShape == 0 ? defaultCursor : textCursor;
        if (newCursor != currentCursor) {
            currentCursor = newCursor;
            codeArea.setCursor(newCursor);
        }
    }

    @Override
    public void mouseMove(MouseEvent me) {
        updateMouseCursor(me);
        if (codeArea.isEnabled() && mouseDown) {
            codeArea.getCommandHandler().moveCaret(computeRelativeX(me), computeRelativeY(me), SelectingMode.SELECTING);
            ((ScrollingCapable) codeArea).revealCursor();
        }
    }

    private int computeRelativeX(MouseEvent me) {
        boolean isDataView = me.getSource() == view;
        return isDataView ? me.x + view.getLocation().x : me.x;
    }

    private int computeRelativeY(MouseEvent me) {
        boolean isDataView = me.getSource() == view;
        return isDataView ? me.y + view.getLocation().y : me.y;
    }

    @Override
    public void mouseScrolled(MouseEvent me) {
//        if (!codeArea.isEnabled() || me.getWheelRotation() == 0) {
//            return;
//        }
//
//        codeArea.getCommandHandler().wheelScroll(me.getWheelRotation() > 0 ? MOUSE_SCROLL_LINES : -MOUSE_SCROLL_LINES, me.isShiftDown() ? CodeAreaCommandHandler.ScrollbarOrientation.VERTICAL : CodeAreaCommandHandler.ScrollbarOrientation.HORIZONTAL);
    }
}
