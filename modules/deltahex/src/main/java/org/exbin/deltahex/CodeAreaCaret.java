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
import org.exbin.deltahex.CodeArea.Section;

/**
 * Code area caret.
 *
 * @version 0.1.0 2016/06/15
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaCaret {

    private static final int DEFAULT_CURSOR_WIDTH = 2;

    private final CodeArea codeArea;

    private final CaretPosition caretPosition = new CaretPosition();
    private Section section = Section.CODE_MATRIX;

    public CodeAreaCaret(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    public void paint(Graphics g) {
        g.setColor(codeArea.getCursorColor());
        int bytesPerBounds = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        int charWidth = codeArea.getCharWidth();
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        Point scrollPoint = codeArea.getScrollPoint();
        Point cursorPoint = getCursorPoint(bytesPerBounds, lineHeight, charWidth);
        if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
            g.drawRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, charWidth, lineHeight - 1);
        } else {
            g.fillRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, DEFAULT_CURSOR_WIDTH, lineHeight - 1);
        }
        if (codeArea.getViewMode() == CodeArea.ViewMode.DUAL && codeArea.isShowShadowCursor()) {
            Point shadowCursorPoint = getShadowCursorPoint(bytesPerBounds, lineHeight, charWidth);
            Graphics2D g2d = (Graphics2D) g.create();
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
            g2d.setStroke(dashed);
            g2d.drawRect(shadowCursorPoint.x - scrollPoint.x, shadowCursorPoint.y - scrollPoint.y,
                    charWidth * (codeArea.getActiveSection() == Section.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
        }
    }

    private Point getCursorPoint(int bytesPerLine, int lineHeight, int charWidth) {
        long dataPosition = caretPosition.getDataPosition();
        long line = dataPosition / bytesPerLine;
        int offset = (int) (dataPosition % bytesPerLine);
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        Rectangle rect = codeArea.getCodeSectionRectangle();
        int caretY = (int) (rect.y + line * lineHeight);
        int caretX;
        if (section == Section.TEXT_PREVIEW) {
            caretX = codeArea.getPreviewX() + charWidth * offset;
        } else {
            caretX = rect.x + charWidth * (offset * charsPerByte + getCodeOffset());
        }

        return new Point(caretX, caretY);
    }

    private Point getShadowCursorPoint(int bytesPerLine, int lineHeight, int charWidth) {
        long dataPosition = caretPosition.getDataPosition();
        long line = dataPosition / bytesPerLine;
        int offset = (int) (dataPosition % bytesPerLine);
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        Rectangle rect = codeArea.getCodeSectionRectangle();
        int caretY = (int) (rect.y + line * lineHeight);
        int caretX;
        if (section == Section.TEXT_PREVIEW) {
            caretX = rect.x + charWidth * (offset * charsPerByte);
        } else {
            caretX = codeArea.getPreviewX() + charWidth * offset;
        }

        return new Point(caretX, caretY);
    }

    public Rectangle getCursorRect(int bytesPerLine, int fontHeight, int charWidth) {
        Point cursorPoint = getCursorPoint(bytesPerLine, fontHeight, charWidth);
        if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
            return new Rectangle(cursorPoint.x, cursorPoint.y, charWidth, fontHeight);
        } else {
            return new Rectangle(cursorPoint.x, cursorPoint.y, DEFAULT_CURSOR_WIDTH, fontHeight);
        }
    }

    public CaretPosition getCaretPosition() {
        return new CaretPosition(caretPosition.getDataPosition(), caretPosition.getCodeOffset());
    }

    public void setCaretPosition(CaretPosition caretPosition) {
        this.caretPosition.setDataPosition(caretPosition == null ? 0 : caretPosition.getDataPosition());
        this.caretPosition.setCodeOffset(caretPosition == null ? 0 : caretPosition.getCodeOffset());
    }

    public long getDataPosition() {
        return caretPosition.getDataPosition();
    }

    public void setCaretPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(0);
    }

    public void setCaretPosition(long dataPosition, int codeOffset) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(codeOffset);
    }

    public int getCodeOffset() {
        return caretPosition.getCodeOffset();
    }

    public void setCodeOffset(int codeOffset) {
        caretPosition.setCodeOffset(codeOffset);
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}
