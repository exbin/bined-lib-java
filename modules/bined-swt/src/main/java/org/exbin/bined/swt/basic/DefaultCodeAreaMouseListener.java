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
package org.exbin.bined.swt.basic;

import javax.annotation.Nonnull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Cursor;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.swt.CodeAreaCore;

/**
 * Code Area component mouse listener.
 *
 * @version 0.2.0 2018/08/11
 * @author ExBin Project (https://exbin.org)
 */
public class DefaultCodeAreaMouseListener implements MouseListener, MouseMoveListener, MouseWheelListener, MouseTrackListener {

    public static final int MOUSE_SCROLL_LINES = 3;

    private final CodeAreaCore codeArea;
    private final ScrolledComposite view;

    private final Cursor defaultCursor = new Cursor(null, SWT.CURSOR_ARROW); // Cursor.getDefaultCursor();
    private final Cursor textCursor = new Cursor(null, SWT.CURSOR_IBEAM);
    private Cursor currentCursor;
    private boolean mouseDown = false;

    public DefaultCodeAreaMouseListener(@Nonnull CodeAreaCore codeArea, @Nonnull ScrolledComposite view) {
        this.codeArea = codeArea;
        this.view = view;
        currentCursor = codeArea.getCursor();
    }

    @Override
    public void mouseDoubleClick(@Nonnull MouseEvent me) {
    }

    @Override
    public void mouseDown(@Nonnull MouseEvent me) {
        codeArea.forceFocus();
        if (codeArea.isEnabled() && me.button == 1) {
            moveCaret(me);
            mouseDown = true;
        }
    }

    @Override
    public void mouseUp(@Nonnull MouseEvent me) {
        mouseDown = false;
    }

    private void moveCaret(@Nonnull MouseEvent me) {
        int relativeX = computeRelativeX(me);
        int relativeY = computeRelativeY(me);
        boolean selecting = (me.stateMask & SWT.SHIFT) > 0;
        codeArea.getCommandHandler().moveCaret(relativeX, relativeY, selecting);
        ((ScrollingCapable) codeArea).revealCursor();
    }

    @Override
    public void mouseExit(@Nonnull MouseEvent e) {
        currentCursor = defaultCursor;
        codeArea.setCursor(defaultCursor);
    }

    @Override
    public void mouseEnter(@Nonnull MouseEvent e) {
        updateMouseCursor(e);
    }

    @Override
    public void mouseHover(@Nonnull MouseEvent e) {
        updateMouseCursor(e);
    }

    private void updateMouseCursor(@Nonnull MouseEvent me) {
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
            codeArea.getCommandHandler().moveCaret(computeRelativeX(me), computeRelativeY(me), true);
            ((ScrollingCapable) codeArea).revealCursor();
        }
    }

    private int computeRelativeX(@Nonnull MouseEvent me) {
        boolean isDataView = me.getSource() == view.getContent();
        return isDataView ? me.x + view.getLocation().x : me.x;
    }

    private int computeRelativeY(@Nonnull MouseEvent me) {
        boolean isDataView = me.getSource() == view.getContent();
        return isDataView ? me.y + view.getLocation().y : me.y;
    }

    @Override
    public void mouseScrolled(@Nonnull MouseEvent e) {
//        if (!codeArea.isEnabled() || e.getWheelRotation() == 0) {
//            return;
//        }
//
//        codeArea.getCommandHandler().wheelScroll(e.getWheelRotation() > 0 ? MOUSE_SCROLL_LINES : -MOUSE_SCROLL_LINES, e.isShiftDown() ? CodeAreaCommandHandler.ScrollbarOrientation.VERTICAL : CodeAreaCommandHandler.ScrollbarOrientation.HORIZONTAL);
    }
}
