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
package org.exbin.dhex.deltahex.component;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Hexadecimal editor caret.
 *
 * @version 0.2.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class HexadecimalCaret {

    private final Hexadecimal hexadecimal;

    public HexadecimalCaret(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
    }

    private static final int DEFAULT_CURSOR_WIDTH = 2;

    // TODO convert from half-byte step to full byte
    private long caretPosition = 0;

    public void paint(Graphics g, int bytesPerLine, int fontHeight, int charWidth) {
        Point cursorPoint = getCursorPoint(bytesPerLine, fontHeight, charWidth);
        if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
            g.drawRect(cursorPoint.x, cursorPoint.y, charWidth, fontHeight);
        } else {
            g.fillRect(cursorPoint.x, cursorPoint.y, DEFAULT_CURSOR_WIDTH, fontHeight);
        }
    }

    private Point getCursorPoint(int bytesPerLine, int fontHeight, int charWidth) {
        long dataPosition = caretPosition / 2;
        int bytePosition = (int) (caretPosition & 1);
        long line = dataPosition / bytesPerLine;
        int offset = (int) (dataPosition % bytesPerLine);

        int caretY = (int) ((line + 2) * fontHeight);
        int caretX;
        if (hexadecimal.getActiveSection() == Hexadecimal.Section.PREVIEW) {
            caretX = hexadecimal.getPreviewX() + charWidth * offset;
        } else {
            caretX = hexadecimal.getHexadecimalX() + charWidth * (offset * 3 + bytePosition);
        }

        return new Point(caretX, caretY);
    }

    public Rectangle getCursorRect(int bytesPerLine, int fontHeight, int charWidth) {
        Point cursorPoint = getCursorPoint(bytesPerLine, fontHeight, charWidth);
        if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
            return new Rectangle(cursorPoint.x, cursorPoint.y, charWidth, fontHeight);
        } else {
            return new Rectangle(cursorPoint.x, cursorPoint.y, DEFAULT_CURSOR_WIDTH, fontHeight);
        }
    }

    public long getCaretPosition() {
        return caretPosition;
    }

    public void setCaretPosition(long caretPosition) {
        this.caretPosition = caretPosition;
    }
}
