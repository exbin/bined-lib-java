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
 * @version 0.2.0 2017/04/02
 * @author ExBin Project (http://exbin.org)
 */
/* package */ class CodeAreaMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

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
            codeArea.moveCaret(me, me.getModifiersEx());
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
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        if (e.getX() >= hexRect.x && e.getY() >= hexRect.y) {
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
            codeArea.moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
            codeArea.revealCursor();
        }
    }

    @Override
    public void mouseWheelMoved(@NotNull MouseWheelEvent e) {
        if (!isEnabled() || e.getWheelRotation() == 0) {
            return;
        }

        if (e.isShiftDown() && horizontalScrollBar.isVisible()) {
            if (e.getWheelRotation() > 0) {
                if (paintDataCache.bytesPerRect < paintDataCache.charsPerLine) {
                    int maxScroll = paintDataCache.charsPerLine - paintDataCache.bytesPerRect;
                    if (scrollPosition.scrollCharPosition < maxScroll - MOUSE_SCROLL_LINES) {
                        scrollPosition.scrollCharPosition += MOUSE_SCROLL_LINES;
                    } else {
                        scrollPosition.scrollCharPosition = maxScroll;
                    }
                    updateScrollBars();
                    notifyScrolled();
                }
            } else if (scrollPosition.scrollCharPosition > 0) {
                if (scrollPosition.scrollCharPosition > MOUSE_SCROLL_LINES) {
                    scrollPosition.scrollCharPosition -= MOUSE_SCROLL_LINES;
                } else {
                    scrollPosition.scrollCharPosition = 0;
                }
                updateScrollBars();
                notifyScrolled();
            }
        } else if (e.getWheelRotation() > 0) {
            long lines = (data.getDataSize() + scrollPosition.lineByteShift) / paintDataCache.bytesPerLine;
            if (lines * paintDataCache.bytesPerLine < data.getDataSize()) {
                lines++;
            }
            lines -= paintDataCache.linesPerRect;
            if (scrollPosition.scrollLinePosition < lines) {
                if (scrollPosition.scrollLinePosition < lines - MOUSE_SCROLL_LINES) {
                    scrollPosition.scrollLinePosition += MOUSE_SCROLL_LINES;
                } else {
                    scrollPosition.scrollLinePosition = lines;
                }
                updateScrollBars();
                notifyScrolled();
            }
        } else if (scrollPosition.scrollLinePosition > 0) {
            if (scrollPosition.scrollLinePosition > MOUSE_SCROLL_LINES) {
                scrollPosition.scrollLinePosition -= MOUSE_SCROLL_LINES;
            } else {
                scrollPosition.scrollLinePosition = 0;
            }
            updateScrollBars();
            notifyScrolled();
        }
    }
}
