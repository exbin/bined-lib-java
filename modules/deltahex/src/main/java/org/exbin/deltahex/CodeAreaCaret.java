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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.exbin.deltahex.CodeArea.Section;

/**
 * Code area caret.
 *
 * @version 0.1.1 2016/08/19
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaCaret {

    private static final int LINE_CURSOR_WIDTH = 1;
    private static final int DOUBLE_CURSOR_WIDTH = 2;
    private static final int DEFAULT_BLINK_RATE = 450;

    private final CodeArea codeArea;

    private final CaretPosition caretPosition = new CaretPosition();
    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private boolean cursorVisible = true;
    private Section section = Section.CODE_MATRIX;
    private CursorShape insertCursorShape = CursorShape.DOUBLE_LEFT;
    private CursorShape overwriteCursorShape = CursorShape.BOX;
    private CursorRenderingMode renderingMode = CursorRenderingMode.XOR;

    public CodeAreaCaret(CodeArea codeArea) {
        this.codeArea = codeArea;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public void paint(Graphics g) {
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        int charWidth = codeArea.getCharWidth();
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        Point scrollPoint = codeArea.getScrollPoint();
        Point cursorPoint = getCursorPoint(bytesPerLine, lineHeight, charWidth);

        if (cursorVisible) {
            g.setColor(codeArea.getCursorColor());
            if (renderingMode == CursorRenderingMode.XOR) {
                g.setXORMode(Color.WHITE);
            }

            CursorShape cursorShape = codeArea.getEditationMode() == CodeArea.EditationMode.INSERT ? insertCursorShape : overwriteCursorShape;
            switch (cursorShape) {
                case LINE_TOP:
                case DOUBLE_TOP: {
                    paintCursorRect(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y,
                            charWidth, cursorShape == CursorShape.LINE_TOP ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH);
                    break;
                }
                case LINE_BOTTOM:
                case DOUBLE_BOTTOM: {
                    int height = cursorShape == CursorShape.LINE_BOTTOM ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH;
                    paintCursorRect(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y + lineHeight - height,
                            charWidth, height);
                    break;
                }
                case LINE_LEFT:
                case DOUBLE_LEFT: {
                    paintCursorRect(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y,
                            cursorShape == CursorShape.LINE_LEFT ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH, lineHeight);
                    break;
                }
                case LINE_RIGHT:
                case DOUBLE_RIGHT: {
                    int width = cursorShape == CursorShape.LINE_RIGHT ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH;
                    paintCursorRect(g, cursorPoint.x - scrollPoint.x + charWidth - width, cursorPoint.y - scrollPoint.y,
                            width, lineHeight);
                    break;
                }
                case BOX: {
                    paintCursorRect(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y,
                            charWidth, lineHeight - 1);
                    break;
                }
                case FRAME: {
                    switch (renderingMode) {
                        case PAINT:
                        case XOR: {
                            g.drawRect(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, charWidth, lineHeight - 1);
                            break;
                        }
                        case NEGATIVE: {
                            // TODO
                            break;
                        }
                    }
                    break;
                }
                case CORNERS: {
                    int quarterWidth = charWidth / 4;
                    int quarterLine = lineHeight / 4;
                    paintCursorLine(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y,
                            cursorPoint.x - scrollPoint.x + quarterWidth, cursorPoint.y - scrollPoint.y);
                    paintCursorLine(g, cursorPoint.x - scrollPoint.x + charWidth - quarterWidth, cursorPoint.y - scrollPoint.y,
                            cursorPoint.x - scrollPoint.x + charWidth, cursorPoint.y - scrollPoint.y);

                    paintCursorLine(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y + 1,
                            cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y + quarterLine);
                    paintCursorLine(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y + lineHeight - 2);
                    paintCursorLine(g, cursorPoint.x - scrollPoint.x + charWidth, cursorPoint.y - scrollPoint.y + 1,
                            cursorPoint.x - scrollPoint.x + charWidth, cursorPoint.y - scrollPoint.y + quarterLine);
                    paintCursorLine(g, cursorPoint.x - scrollPoint.x + charWidth, cursorPoint.y - scrollPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x - scrollPoint.x + charWidth, cursorPoint.y - scrollPoint.y + lineHeight - 2);

                    paintCursorLine(g, cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y + lineHeight - 1,
                            cursorPoint.x - scrollPoint.x + quarterWidth, cursorPoint.y - scrollPoint.y + lineHeight - 1);
                    paintCursorLine(g, cursorPoint.x - scrollPoint.x + charWidth - quarterWidth, cursorPoint.y - scrollPoint.y + lineHeight - 1,
                            cursorPoint.x - scrollPoint.x + charWidth, cursorPoint.y - scrollPoint.y + lineHeight - 1);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected cursor shape type " + cursorShape.name());
                }
            }

            if (renderingMode == CursorRenderingMode.XOR) {
                g.setPaintMode();
            }
        }

        // Paint shadow cursor
        if (codeArea.getViewMode() == CodeArea.ViewMode.DUAL && codeArea.isShowShadowCursor()) {
            g.setColor(codeArea.getCursorColor());
            Point shadowCursorPoint = getShadowCursorPoint(bytesPerLine, lineHeight, charWidth);
            Graphics2D g2d = (Graphics2D) g.create();
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
            g2d.setStroke(dashed);
            g2d.drawRect(shadowCursorPoint.x - scrollPoint.x, shadowCursorPoint.y - scrollPoint.y,
                    charWidth * (codeArea.getActiveSection() == Section.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
        }
    }

    private void paintCursorRect(Graphics g, int x, int y, int width, int height) {
        switch (renderingMode) {
            case PAINT:
            case XOR: {
                g.fillRect(x, y, width, height);
                break;
            }
            case NEGATIVE: {
                Shape clip = g.getClip();
                g.setClip(x, y, width, height);
                g.fillRect(x, y, width, height);
                codeArea.getPainter().paintCursorNegative(g);
                g.setClip(clip);
                break;
            }
        }
    }

    private void paintCursorLine(Graphics g, int x1, int y1, int x2, int y2) {
        if (renderingMode != CursorRenderingMode.NEGATIVE) {
            g.drawLine(x1, y1, x2, y2);
        } else {
            // TODO
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

    public Rectangle getCursorRect(int bytesPerLine, int lineHeight, int charWidth) {
        Point cursorPoint = getCursorPoint(bytesPerLine, lineHeight, charWidth);
        Point scrollPoint = codeArea.getScrollPoint();
        CursorShape cursorShape = codeArea.getEditationMode() == CodeArea.EditationMode.INSERT ? insertCursorShape : overwriteCursorShape;
        switch (cursorShape) {
            case BOX:
            case FRAME:
            case CORNERS: {
                int width = charWidth;
                if (cursorShape != CursorShape.BOX) {
                    width++;
                }
                return new Rectangle(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y, width, lineHeight);
            }
            case LINE_TOP:
            case DOUBLE_TOP: {
                return new Rectangle(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y,
                        charWidth, cursorShape == CursorShape.LINE_TOP ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH);
            }
            case LINE_BOTTOM:
            case DOUBLE_BOTTOM: {
                int height = cursorShape == CursorShape.LINE_BOTTOM ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH;
                return new Rectangle(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y + lineHeight - height,
                        charWidth, height);
            }
            case LINE_LEFT:
            case DOUBLE_LEFT: {
                return new Rectangle(cursorPoint.x - scrollPoint.x, cursorPoint.y - scrollPoint.y,
                        cursorShape == CursorShape.LINE_LEFT ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH, lineHeight);
            }
            case LINE_RIGHT:
            case DOUBLE_RIGHT: {
                int width = cursorShape == CursorShape.LINE_RIGHT ? LINE_CURSOR_WIDTH : DOUBLE_CURSOR_WIDTH;
                return new Rectangle(cursorPoint.x - scrollPoint.x + charWidth - width, cursorPoint.y - scrollPoint.y,
                        width, lineHeight);
            }
            default: {
                throw new IllegalStateException("Unexpected cursor shape type " + cursorShape.name());
            }
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
            codeArea.paintImmediately(cursorRect);
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

    public CursorShape getInsertCursorShape() {
        return insertCursorShape;
    }

    public void setInsertCursorShape(CursorShape insertCursorShape) {
        if (insertCursorShape == null) {
            throw new NullPointerException("Insert cursor shape cannot be null");
        }
        this.insertCursorShape = insertCursorShape;
        cursorRepaint();
    }

    public CursorShape getOverwriteCursorShape() {
        return overwriteCursorShape;
    }

    public void setOverwriteCursorShape(CursorShape overwriteCursorShape) {
        if (overwriteCursorShape == null) {
            throw new NullPointerException("Override cursor shape cannot be null");
        }
        this.overwriteCursorShape = overwriteCursorShape;
        cursorRepaint();
    }

    public CursorRenderingMode getRenderingMode() {
        return renderingMode;
    }

    public void setRenderingMode(CursorRenderingMode renderingMode) {
        if (renderingMode == null) {
            throw new NullPointerException("Cursor rendering mode cannot be null");
        }
        this.renderingMode = renderingMode;
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

    public static enum CursorShape {
        LINE_BOTTOM, LINE_TOP, LINE_LEFT, LINE_RIGHT,
        DOUBLE_BOTTOM, DOUBLE_TOP, DOUBLE_LEFT, DOUBLE_RIGHT,
        BOX,
        /**
         * Frame and corners mode is not recommended for negative rendering
         * modes.
         */
        FRAME, CORNERS
    }

    public static enum CursorRenderingMode {
        /**
         * Cursor is just painted.
         */
        PAINT,
        /**
         * Cursor is painted using pixels inversion.
         */
        XOR,
        /**
         * Underlying character is painted using cursor negative color.
         */
        NEGATIVE
    }
}
