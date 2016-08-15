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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.exbin.deltahex.CodeArea.Section;

/**
 * Code area caret.
 *
 * @version 0.1.1 2016/08/15
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaCaret {

    private static final int DEFAULT_CURSOR_WIDTH = 2;
    private static final int DEFAULT_BLINK_RATE = 450;

    private final CodeArea codeArea;

    private final CaretPosition caretPosition = new CaretPosition();
    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private boolean cursorVisible = true;
    private Section section = Section.CODE_MATRIX;
    private OverrideCursorShape overrideCursorShape = OverrideCursorShape.FULL;

    public CodeAreaCaret(CodeArea codeArea) {
        this.codeArea = codeArea;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public void paint(Graphics g) {
        if (cursorVisible) {
            g.setColor(codeArea.getCursorColor());
            int bytesPerLine = codeArea.getBytesPerLine();
            int lineHeight = codeArea.getLineHeight();
            int charWidth = codeArea.getCharWidth();
            int codeDigits = codeArea.getCodeType().getMaxDigits();
            Point scrollPoint = codeArea.getScrollPoint();
            Point cursorPoint = getCursorPoint(bytesPerLine, lineHeight, charWidth);
            g.setXORMode(Color.WHITE);
            if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                switch (overrideCursorShape) {
                    case EMPTY: {
                        g.drawRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, charWidth, lineHeight - 1);
                        break;
                    }
                    case FULL: {
                        g.fillRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, charWidth, lineHeight - 1);
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unexpected overrideCursorShape " + overrideCursorShape.name());
                    }
                }
            } else {
                g.fillRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, DEFAULT_CURSOR_WIDTH, lineHeight - 1);
            }
            g.setPaintMode();

            // Paint shadow cursor
            if (codeArea.getViewMode() == CodeArea.ViewMode.DUAL && codeArea.isShowShadowCursor()) {
                Point shadowCursorPoint = getShadowCursorPoint(bytesPerLine, lineHeight, charWidth);
                Graphics2D g2d = (Graphics2D) g.create();
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
                g2d.setStroke(dashed);
                g2d.drawRect(shadowCursorPoint.x - scrollPoint.x, shadowCursorPoint.y - scrollPoint.y,
                        charWidth * (codeArea.getActiveSection() == Section.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
            }
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
        Point scrollPoint = codeArea.getScrollPoint();
        if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
            int width = charWidth;
            if (overrideCursorShape == OverrideCursorShape.EMPTY) {
                width++;
            }
            return new Rectangle(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, width, fontHeight);
        } else {
            return new Rectangle(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, DEFAULT_CURSOR_WIDTH, fontHeight);
        }
    }

    public CaretPosition getCaretPosition() {
        return new CaretPosition(caretPosition.getDataPosition(), caretPosition.getCodeOffset());
    }

    public void resetBlink() {
        if (blinkTimer != null) {
            cursorVisible = true;
            blinkTimer.restart();
        }
    }

    private void cursorRepaint() {
        int bytesPerLine = codeArea.getBytesPerLine();
        if (bytesPerLine > 0) {
            int lineHeight = codeArea.getLineHeight();
            int charWidth = codeArea.getCharWidth();
            Rectangle cursorRect = getCursorRect(bytesPerLine, lineHeight, charWidth);
            codeArea.repaint(cursorRect);
        }
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
        resetBlink();
    }

    public void setCaretPosition(long dataPosition, int codeOffset) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    public int getCodeOffset() {
        return caretPosition.getCodeOffset();
    }

    public void setCodeOffset(int codeOffset) {
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
        resetBlink();
    }

    public int getBlinkRate() {
        return blinkRate;
    }

    public void setBlinkRate(int blinkRate) {
        privateSetBlinkRate(blinkRate);
    }

    public OverrideCursorShape getOverrideCursorShape() {
        return overrideCursorShape;
    }

    public void setOverrideCursorShape(OverrideCursorShape overrideCursorShape) {
        if (overrideCursorShape == null) {
            throw new NullPointerException("Override cursor shape cannot be null");
        }
        this.overrideCursorShape = overrideCursorShape;
        cursorRepaint();
    }

    private void privateSetBlinkRate(int blinkRate) {
        if (blinkRate < 0) {
            throw new IllegalArgumentException("Blink rate cannot be negative");
        }
        this.blinkRate = blinkRate;
        if (blinkTimer != null) {
            if (blinkRate == 0) {
                blinkTimer.stop();
                blinkTimer = null;
                cursorVisible = true;
                cursorRepaint();
            } else {
                blinkTimer.setDelay(blinkRate);
            }
        } else if (blinkRate > 0) {
            blinkTimer = new javax.swing.Timer(blinkRate, new Blink());
            blinkTimer.setRepeats(true);
            blinkTimer.start();
        }
    }

    private class Blink implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            cursorVisible = !cursorVisible;
            cursorRepaint();
        }
    }

    public static enum OverrideCursorShape {
        EMPTY, FULL
    }
}
