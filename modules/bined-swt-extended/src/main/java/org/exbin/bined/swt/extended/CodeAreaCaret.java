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
package org.exbin.bined.swt.extended;

import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditMode;
import org.exbin.bined.Section;
import org.exbin.bined.swt.CodeArea;

/**
 * Code area caret.
 *
 * @author ExBin Project (https://exbin.org)
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
    private CursorRenderingMode renderingMode = CursorRenderingMode.NEGATIVE;

    public CodeAreaCaret(CodeArea codeArea) {
        this.codeArea = codeArea;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public int getCursorThickness(CursorShape cursorShape, int charWidth, int lineHeight) {
        switch (cursorShape.getWidth()) {
            case LINE:
                return LINE_CURSOR_WIDTH;
            case DOUBLE:
                return DOUBLE_CURSOR_WIDTH;
            case QUARTER: {
                if (cursorShape == CursorShape.QUARTER_LEFT || cursorShape == CursorShape.QUARTER_RIGHT) {
                    return charWidth / 4;
                } else {
                    return lineHeight / 4;
                }
            }
            case HALF: {
                if (cursorShape == CursorShape.HALF_LEFT || cursorShape == CursorShape.HALF_RIGHT) {
                    return charWidth / 2;
                } else {
                    return lineHeight / 2;
                }
            }
        }

        return -1;
    }

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param bytesPerLine bytes per line
     * @param lineHeight line height
     * @param charWidth character width
     * @param linesPerRect lines per visible rectangle
     * @return cursor position or null
     */
    public Point getCursorPoint(int bytesPerLine, int lineHeight, int charWidth, int linesPerRect) {
        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long shiftedPosition = caretPosition.getDataPosition() + scrollPosition.getLineByteShift();
        long line = shiftedPosition / bytesPerLine - scrollPosition.getScrollLinePosition();
        if (line < -1 || line > linesPerRect) {
            return null;
        }

        int byteOffset = (int) (shiftedPosition % bytesPerLine);

        Rectangle rect = codeArea.getCodeSectionRectangle();
        int caretY = (int) (rect.y + line * lineHeight) - scrollPosition.getScrollLineOffset();
        int caretX;
        if (section == Section.TEXT_PREVIEW) {
            caretX = codeArea.getPreviewX() + charWidth * byteOffset;
        } else {
            caretX = rect.x + charWidth * (codeArea.computeByteCharPos(byteOffset) + getCodeOffset());
        }
        caretX -= scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    /**
     * Returns relative shadow cursor position in code area or null if cursor is
     * not visible.
     *
     * @param bytesPerLine bytes per line
     * @param lineHeight line height
     * @param charWidth character width
     * @param linesPerRect lines per visible rectangle
     * @return cursor position or null
     */
    public Point getShadowCursorPoint(int bytesPerLine, int lineHeight, int charWidth, int linesPerRect) {
        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long shiftedPosition = caretPosition.getDataPosition() + scrollPosition.getLineByteShift();
        long line = shiftedPosition / bytesPerLine - scrollPosition.getScrollLinePosition();
        if (line < -1 || line + 1 > linesPerRect) {
            return null;
        }

        int byteOffset = (int) (shiftedPosition % bytesPerLine);

        Rectangle rect = codeArea.getCodeSectionRectangle();
        int caretY = (int) (rect.y + line * lineHeight) - scrollPosition.getScrollLineOffset();
        int caretX;
        if (section == Section.TEXT_PREVIEW) {
            caretX = rect.x + charWidth * codeArea.computeByteCharPos(byteOffset);
        } else {
            caretX = codeArea.getPreviewX() + charWidth * byteOffset;
        }
        caretX -= scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    /**
     * Returns cursor rectangle.
     *
     * @param bytesPerLine bytes per line
     * @param lineHeight line height
     * @param charWidth character width
     * @param linesPerRect lines per visible rectangle
     * @return cursor rectangle or null
     */
    public Rectangle getCursorRect(int bytesPerLine, int lineHeight, int charWidth, int linesPerRect) {
        Point cursorPoint = getCursorPoint(bytesPerLine, lineHeight, charWidth, linesPerRect);
        if (cursorPoint == null) {
            return null;
        }

        CursorShape cursorShape = codeArea.getEditMode() == EditMode.INSERT ? insertCursorShape : overwriteCursorShape;
        int cursorThickness = 0;
        if (cursorShape.getWidth() != CursorShapeWidth.FULL) {
            cursorThickness = getCursorThickness(cursorShape, charWidth, lineHeight);
        }
        switch (cursorShape) {
            case BOX:
            case FRAME:
            case BOTTOM_CORNERS:
            case CORNERS: {
                int width = charWidth;
                if (cursorShape != CursorShape.BOX) {
                    width++;
                }
                return new Rectangle(cursorPoint.x, cursorPoint.y, width, lineHeight);
            }
            case LINE_TOP:
            case DOUBLE_TOP:
            case QUARTER_TOP:
            case HALF_TOP: {
                return new Rectangle(cursorPoint.x, cursorPoint.y,
                        charWidth, cursorThickness);
            }
            case LINE_BOTTOM:
            case DOUBLE_BOTTOM:
            case QUARTER_BOTTOM:
            case HALF_BOTTOM: {
                return new Rectangle(cursorPoint.x, cursorPoint.y + lineHeight - cursorThickness,
                        charWidth, cursorThickness);
            }
            case LINE_LEFT:
            case DOUBLE_LEFT:
            case QUARTER_LEFT:
            case HALF_LEFT: {
                return new Rectangle(cursorPoint.x, cursorPoint.y, cursorThickness, lineHeight);
            }
            case LINE_RIGHT:
            case DOUBLE_RIGHT:
            case QUARTER_RIGHT:
            case HALF_RIGHT: {
                return new Rectangle(cursorPoint.x + charWidth - cursorThickness, cursorPoint.y, cursorThickness, lineHeight);
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(cursorShape);
        }
    }

    public CaretPosition getCaretPosition() {
        return new CaretPosition(caretPosition.getDataPosition(), caretPosition.getCodeOffset());
    }

    public void resetBlink() {
        if (blinkTimer != null) {
            cursorVisible = true;
            blinkTimer.cancel();
            blinkTimer.schedule(new Blink(), blinkRate);
        }
    }

    private void cursorRepaint() {
        int bytesPerLine = codeArea.getBytesPerLine();
        if (bytesPerLine > 0) {
            int lineHeight = codeArea.getLineHeight();
            int charWidth = codeArea.getCharWidth();
            int linesPerRect = codeArea.getLinesPerRect();
            Rectangle cursorRect = getCursorRect(bytesPerLine, lineHeight, charWidth, linesPerRect);
            if (cursorRect != null) {
                codeArea.redraw(cursorRect.x, cursorRect.y, cursorRect.width, cursorRect.height, true);
            }
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

    public boolean isCursorVisible() {
        return cursorVisible;
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
                blinkTimer.cancel();
                blinkTimer = null;
                cursorVisible = true;
                cursorRepaint();
            } else {
                blinkTimer.cancel();
                blinkTimer.schedule(new Blink(), blinkRate);
            }
        } else if (blinkRate > 0) {
            blinkTimer = new java.util.Timer();
            blinkTimer.schedule(new Blink(), blinkRate);
        }
    }

    private class Blink extends TimerTask {

        @Override
        public void run() {
            cursorVisible = !cursorVisible;
            cursorRepaint();
            blinkTimer.schedule(new Blink(), blinkRate);
        }
    }

    public static enum CursorShape {
        /*
         * Single line cursor shapes.
         */
        LINE_BOTTOM(CursorShapeWidth.LINE),
        LINE_TOP(CursorShapeWidth.LINE),
        LINE_LEFT(CursorShapeWidth.LINE),
        LINE_RIGHT(CursorShapeWidth.LINE),
        /*
         * Double line cursor shapes.
         */
        DOUBLE_BOTTOM(CursorShapeWidth.DOUBLE),
        DOUBLE_TOP(CursorShapeWidth.DOUBLE),
        DOUBLE_LEFT(CursorShapeWidth.DOUBLE),
        DOUBLE_RIGHT(CursorShapeWidth.DOUBLE),
        /*
         * Quarter cursor shapes.
         */
        QUARTER_BOTTOM(CursorShapeWidth.QUARTER),
        QUARTER_TOP(CursorShapeWidth.QUARTER),
        QUARTER_LEFT(CursorShapeWidth.QUARTER),
        QUARTER_RIGHT(CursorShapeWidth.QUARTER),
        /*
         * Half cursor shapes.
         */
        HALF_BOTTOM(CursorShapeWidth.HALF),
        HALF_TOP(CursorShapeWidth.HALF),
        HALF_LEFT(CursorShapeWidth.HALF),
        HALF_RIGHT(CursorShapeWidth.HALF),
        /*
         * Full cursor shapes.
         * Frame and corners modes are always rendered using paint mode.
         */
        BOX(CursorShapeWidth.FULL),
        FRAME(CursorShapeWidth.FULL),
        CORNERS(CursorShapeWidth.FULL),
        BOTTOM_CORNERS(CursorShapeWidth.FULL);

        private final CursorShapeWidth width;

        private CursorShape(CursorShapeWidth width) {
            this.width = width;
        }

        public CursorShapeWidth getWidth() {
            return width;
        }
    }

    public static enum CursorShapeWidth {
        LINE, DOUBLE, QUARTER, HALF, FULL
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
         * Underlying character is painted using negative color to cursor
         * cursor.
         */
        NEGATIVE
    }
}
