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
package org.exbin.deltahex.swing.basic;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import org.exbin.deltahex.capability.CaretCapable;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.swing.CodeAreaWorker;

/**
 * Code Area component mouse listener.
 *
 * @version 0.2.0 2017/12/28
 * @author ExBin Project (http://exbin.org)
 */
/* package */ class DefaultCodeAreaMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

    public static final int MOUSE_SCROLL_LINES = 3;

    private final CodeArea codeArea;
    private final JComponent view;

    private final Cursor defaultCursor = Cursor.getDefaultCursor();
    private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    private Cursor currentCursor;
    private boolean mouseDown = false;

    public DefaultCodeAreaMouseListener(@Nonnull CodeArea codeArea, @Nonnull JComponent view) {
        this.codeArea = codeArea;
        this.view = view;
        currentCursor = codeArea.getCursor();
    }

    @Override
    public void mousePressed(@Nonnull MouseEvent me) {
        codeArea.requestFocus();
        if (codeArea.isEnabled() && me.getButton() == MouseEvent.BUTTON1) {
            boolean isDataView = me.getSource() != codeArea;
            int mouseX = isDataView ? me.getX() + view.getX() : me.getX();
            int mouseY = isDataView ? me.getY() + view.getY() : me.getY();
            codeArea.getCommandHandler().moveCaret(mouseX, mouseY, me.getModifiersEx());
            ((CaretCapable) codeArea.getWorker()).revealCursor();
            mouseDown = true;
        }
    }

    @Override
    public void mouseReleased(@Nonnull MouseEvent me) {
        mouseDown = false;
    }

    @Override
    public void mouseExited(@Nonnull MouseEvent e) {
        currentCursor = defaultCursor;
        codeArea.setCursor(defaultCursor);
    }

    @Override
    public void mouseEntered(@Nonnull MouseEvent e) {
        updateMouseCursor(e);
    }

    @Override
    public void mouseMoved(@Nonnull MouseEvent e) {
        updateMouseCursor(e);
    }

    private void updateMouseCursor(@Nonnull MouseEvent e) {
        CodeAreaWorker worker = codeArea.getWorker();
        int cursorShape = ((CaretCapable) worker).getCursorShape(e.getX(), e.getY());
        Cursor newCursor = cursorShape == 0 ? defaultCursor : textCursor;

        if (newCursor != currentCursor) {
            currentCursor = newCursor;
            codeArea.setCursor(newCursor);
        }
    }

    @Override
    public void mouseDragged(@Nonnull MouseEvent me) {
        CodeAreaWorker worker = codeArea.getWorker();
        updateMouseCursor(me);
        if (codeArea.isEnabled() && mouseDown) {
            boolean isDataView = me.getSource() != codeArea;
            int mouseX = isDataView ? me.getX() + view.getX() : me.getX();
            int mouseY = isDataView ? me.getY() + view.getY() : me.getY();
            codeArea.getCommandHandler().moveCaret(mouseX, mouseY, KeyEvent.SHIFT_DOWN_MASK);
            ((CaretCapable) worker).revealCursor();
        }
    }

    @Override
    public void mouseWheelMoved(@Nonnull MouseWheelEvent e) {
        if (!codeArea.isEnabled() || e.getWheelRotation() == 0) {
            return;
        }

//        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//
//        if (e.isShiftDown() && codeArea.getPainter().isHorizontalScrollBarVisible()) {
//            if (e.getWheelRotation() > 0) {
//                if (codeArea.getBytesPerRectangle() < codeArea.getCharactersPerLine()) {
//                    int maxScroll = codeArea.getCharactersPerLine() - codeArea.getBytesPerRectangle();
//                    if (scrollPosition.getScrollCharPosition() < maxScroll - MOUSE_SCROLL_LINES) {
//                        scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() + MOUSE_SCROLL_LINES);
//                    } else {
//                        scrollPosition.setScrollCharPosition(maxScroll);
//                    }
//                    codeArea.getPainter().updateScrollBars();
//                    codeArea.notifyScrolled();
//                }
//            } else if (scrollPosition.getScrollCharPosition() > 0) {
//                if (scrollPosition.getScrollCharPosition() > MOUSE_SCROLL_LINES) {
//                    scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() - MOUSE_SCROLL_LINES);
//                } else {
//                    scrollPosition.setScrollCharPosition(0);
//                }
//                codeArea.getPainter().updateScrollBars();
//                codeArea.notifyScrolled();
//            }
//        } else if (e.getWheelRotation() > 0) {
//            long lines = (codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / codeArea.getBytesPerLine();
//            if (lines * codeArea.getBytesPerLine() < codeArea.getDataSize()) {
//                lines++;
//            }
//            lines -= codeArea.getLinesPerRectangle();
//            if (scrollPosition.getScrollLinePosition() < lines) {
//                if (scrollPosition.getScrollLinePosition() < lines - MOUSE_SCROLL_LINES) {
//                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + MOUSE_SCROLL_LINES);
//                } else {
//                    scrollPosition.setScrollLinePosition(lines);
//                }
//                codeArea.getPainter().updateScrollBars();
//                codeArea.notifyScrolled();
//            }
//        } else if (scrollPosition.getScrollLinePosition() > 0) {
//            if (scrollPosition.getScrollLinePosition() > MOUSE_SCROLL_LINES) {
//                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - MOUSE_SCROLL_LINES);
//            } else {
//                scrollPosition.setScrollLinePosition(0);
//            }
//            codeArea.getPainter().updateScrollBars();
//            codeArea.notifyScrolled();
//        }
    }
}
