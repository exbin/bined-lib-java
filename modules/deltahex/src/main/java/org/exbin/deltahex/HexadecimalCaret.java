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
package org.exbin.deltahex;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

/**
 * Hexadecimal editor caret.
 *
 * @version 0.2.0 2016/06/06
 * @author ExBin Project (http://exbin.org)
 */
public class HexadecimalCaret {

    private static final int DEFAULT_CURSOR_WIDTH = 2;

    private final Hexadecimal hexadecimal;

    private final CaretPosition caretPosition = new CaretPosition();
    private Section section = Section.HEXADECIMAL;

    public HexadecimalCaret(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
    }

    public void paint(Graphics g) {
        g.setColor(hexadecimal.getCursorColor());
        int bytesPerBounds = hexadecimal.getBytesPerLine();
        int lineHeight = hexadecimal.getLineHeight();
        int charWidth = hexadecimal.getCharWidth();
        Point scrollPoint = hexadecimal.getScrollPoint();
        Point cursorPoint = getCursorPoint(bytesPerBounds, lineHeight, charWidth);
        if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
            g.drawRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, charWidth, lineHeight - 1);
        } else {
            g.fillRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, DEFAULT_CURSOR_WIDTH, lineHeight - 1);
        }
        if (hexadecimal.getViewMode() == Hexadecimal.ViewMode.DUAL && hexadecimal.isShowShadowCursor()) {
            Point shadowCursorPoint = getShadowCursorPoint(bytesPerBounds, lineHeight, charWidth);
            Graphics2D g2d = (Graphics2D) g.create();
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
            g2d.setStroke(dashed);
            g2d.drawRect(shadowCursorPoint.x - scrollPoint.x, shadowCursorPoint.y - scrollPoint.y,
                    charWidth * (hexadecimal.getActiveSection() == Section.PREVIEW ? 2 : 1), lineHeight - 1);
        }
    }

    private Point getCursorPoint(int bytesPerLine, int lineHeight, int charWidth) {
        long dataPosition = caretPosition.getDataPosition();
        long line = dataPosition / bytesPerLine;
        int offset = (int) (dataPosition % bytesPerLine);

        Rectangle rect = hexadecimal.getHexadecimalRectangle();
        int caretY = (int) (rect.y + line * lineHeight);
        int caretX;
        if (section == Section.PREVIEW) {
            caretX = hexadecimal.getPreviewX() + charWidth * offset;
        } else {
            caretX = rect.x + charWidth * (offset * 3 + getHalfBytePosition());
        }

        return new Point(caretX, caretY);
    }

    private Point getShadowCursorPoint(int bytesPerLine, int lineHeight, int charWidth) {
        long dataPosition = caretPosition.getDataPosition();
        long line = dataPosition / bytesPerLine;
        int offset = (int) (dataPosition % bytesPerLine);

        Rectangle rect = hexadecimal.getHexadecimalRectangle();
        int caretY = (int) (rect.y + line * lineHeight);
        int caretX;
        if (section == Section.PREVIEW) {
            caretX = rect.x + charWidth * (offset * 3);
        } else {
            caretX = hexadecimal.getPreviewX() + charWidth * offset;
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

    public CaretPosition getCaretPosition() {
        return new CaretPosition(caretPosition.getDataPosition(), caretPosition.isLowerHalf());
    }

    public void setCaretPosition(CaretPosition caretPosition) {
        this.caretPosition.setDataPosition(caretPosition == null ? 0 : caretPosition.getDataPosition());
        this.caretPosition.setLowerHalf(caretPosition == null ? true : caretPosition.isLowerHalf());
    }

    public long getDataPosition() {
        return caretPosition.getDataPosition();
    }

    public void setCaretPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setLowerHalf(false);
    }

    public void setCaretPosition(long dataPosition, boolean lowerHalf) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setLowerHalf(lowerHalf);
    }

    public boolean isLowerHalf() {
        return caretPosition.isLowerHalf();
    }

    public int getHalfBytePosition() {
        return caretPosition.isLowerHalf() ? 1 : 0;
    }

    public void setLowerHalf(boolean lowerHalf) {
        caretPosition.setLowerHalf(lowerHalf);
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public static enum Section {
        HEXADECIMAL, PREVIEW
    }
}
