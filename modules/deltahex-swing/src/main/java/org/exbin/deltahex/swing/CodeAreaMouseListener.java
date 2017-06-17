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

import com.sun.istack.internal.NotNull;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Code Area component mouse listener.
 *
 * @version 0.2.0 2017/06/17
 * @author ExBin Project (http://exbin.org)
 */
/* package */ class CodeAreaMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

    public static final int MOUSE_SCROLL_LINES = 3;

    private final CodeArea codeArea;

    private final Cursor defaultCursor = Cursor.getDefaultCursor();
    private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    private Cursor currentCursor;
    private boolean mouseDown = false;

    public CodeAreaMouseListener(CodeArea codeArea) {
        this.codeArea = codeArea;
        currentCursor = codeArea.getCursor();
    }

    @Override
    public void mousePressed(@NotNull MouseEvent me) {
        codeArea.requestFocus();
        if (codeArea.isEnabled() && me.getButton() == MouseEvent.BUTTON1) {
            codeArea.getCommandHandler().moveCaret(me, me.getModifiersEx());
            codeArea.revealCursor();
            mouseDown = true;
        }
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent me) {
        mouseDown = false;
    }

    @Override
    public void mouseExited(@NotNull MouseEvent e) {
        currentCursor = defaultCursor;
        codeArea.setCursor(defaultCursor);
    }

    @Override
    public void mouseEntered(@NotNull MouseEvent e) {
        updateMouseCursor(e);
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent e) {
        updateMouseCursor(e);
    }

    private void updateMouseCursor(@NotNull MouseEvent e) {
        Cursor newCursor = defaultCursor;
        Rectangle dataViewRectangle = codeArea.getDataViewRectangle();
        if (e.getX() >= dataViewRectangle.x && e.getY() >= dataViewRectangle.y) {
            newCursor = textCursor;
        }

        if (newCursor != currentCursor) {
            currentCursor = newCursor;
            codeArea.setCursor(newCursor);
        }
    }

    @Override
    public void mouseDragged(@NotNull MouseEvent me) {
        updateMouseCursor(me);
        if (codeArea.isEnabled() && mouseDown) {
            codeArea.getCommandHandler().moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
            codeArea.revealCursor();
        }
    }

    @Override
    public void mouseWheelMoved(@NotNull MouseWheelEvent e) {
        if (!codeArea.isEnabled() || e.getWheelRotation() == 0) {
            return;
        }

        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();

        if (e.isShiftDown() && codeArea.isHorizontalScrollBarVisible()) {
            if (e.getWheelRotation() > 0) {
                if (codeArea.getBytesPerRectangle() < codeArea.getCharactersPerLine()) {
                    int maxScroll = codeArea.getCharactersPerLine() - codeArea.getBytesPerRectangle();
                    if (scrollPosition.getScrollCharPosition() < maxScroll - MOUSE_SCROLL_LINES) {
                        scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() + MOUSE_SCROLL_LINES);
                    } else {
                        scrollPosition.setScrollCharPosition(maxScroll);
                    }
                    codeArea.updateScrollBars();
                    codeArea.notifyScrolled();
                }
            } else if (scrollPosition.getScrollCharPosition() > 0) {
                if (scrollPosition.getScrollCharPosition() > MOUSE_SCROLL_LINES) {
                    scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() - MOUSE_SCROLL_LINES);
                } else {
                    scrollPosition.setScrollCharPosition(0);
                }
                codeArea.updateScrollBars();
                codeArea.notifyScrolled();
            }
        } else if (e.getWheelRotation() > 0) {
            long lines = (codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / codeArea.getBytesPerLine();
            if (lines * codeArea.getBytesPerLine() < codeArea.getDataSize()) {
                lines++;
            }
            lines -= codeArea.getLinesPerRectangle();
            if (scrollPosition.getScrollLinePosition() < lines) {
                if (scrollPosition.getScrollLinePosition() < lines - MOUSE_SCROLL_LINES) {
                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + MOUSE_SCROLL_LINES);
                } else {
                    scrollPosition.setScrollLinePosition(lines);
                }
                codeArea.updateScrollBars();
                codeArea.notifyScrolled();
            }
        } else if (scrollPosition.getScrollLinePosition() > 0) {
            if (scrollPosition.getScrollLinePosition() > MOUSE_SCROLL_LINES) {
                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - MOUSE_SCROLL_LINES);
            } else {
                scrollPosition.setScrollLinePosition(0);
            }
            codeArea.updateScrollBars();
            codeArea.notifyScrolled();
        }
    }
}
