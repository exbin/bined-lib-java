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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Timer;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaCaret;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.capability.CaretCapable;
import org.exbin.deltahex.swing.CodeArea;

/**
 * Default implementation of code area caret.
 *
 * @version 0.2.0 2017/12/09
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaCaret implements CodeAreaCaret {

    private static final int LINE_CURSOR_WIDTH = 1;
    private static final int DOUBLE_CURSOR_WIDTH = 2;
    private static final int DEFAULT_BLINK_RATE = 450;

    private final CodeArea codeArea;
    private final CaretPosition caretPosition = new CaretPosition();

    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private boolean cursorVisible = true;
    private CursorShape insertCursorShape = CursorShape.DOUBLE_LEFT;
    private CursorShape overwriteCursorShape = CursorShape.BOX;
    private CursorRenderingMode renderingMode = CursorRenderingMode.PAINT; //NEGATIVE;

    public DefaultCodeAreaCaret(CodeArea codeArea) {
        this.codeArea = codeArea;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public int getCursorThickness(@Nonnull CursorShape cursorShape, int charWidth, int lineHeight) {
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

    @Nonnull
    @Override
    public CaretPosition getCaretPosition() {
        // TODO: Make immutable / cache?
        return new CaretPosition(caretPosition.getDataPosition(), caretPosition.getCodeOffset());
    }

    public void resetBlink() {
        if (blinkTimer != null) {
            cursorVisible = true;
            blinkTimer.restart();
        }
    }

    private void notifyCaredChanged() {
        ((CaretCapable) codeArea.getWorker()).notifyCaretChanged();
    }

    @Override
    public void setCaretPosition(@Nullable CaretPosition caretPosition) {
        this.caretPosition.setDataPosition(caretPosition == null ? 0 : caretPosition.getDataPosition());
        this.caretPosition.setCodeOffset(caretPosition == null ? 0 : caretPosition.getCodeOffset());
    }

    @Override
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

    public void setCaretPosition(long dataPosition, int codeOffset, CodeAreaSection section) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(codeOffset);
        caretPosition.setSection(section);
        resetBlink();
    }

    public long getDataPosition() {
        return caretPosition.getDataPosition();
    }

    public void setDataPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        resetBlink();
    }

    public int getCodeOffset() {
        return caretPosition.getCodeOffset();
    }

    public void setCodeOffset(int codeOffset) {
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    public CodeAreaSection getSection() {
        return caretPosition.getSection();
    }

    public void setSection(CodeAreaSection section) {
        caretPosition.setSection(section);
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
        notifyCaredChanged();
    }

    public CursorShape getOverwriteCursorShape() {
        return overwriteCursorShape;
    }

    public void setOverwriteCursorShape(CursorShape overwriteCursorShape) {
        if (overwriteCursorShape == null) {
            throw new NullPointerException("Override cursor shape cannot be null");
        }

        this.overwriteCursorShape = overwriteCursorShape;
        notifyCaredChanged();
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
        notifyCaredChanged();
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
                notifyCaredChanged();
            } else {
                blinkTimer.setDelay(blinkRate);
                blinkTimer.setInitialDelay(blinkRate);
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
            notifyCaredChanged();
        }
    }

    /**
     * Enumeration of supported cursor shapes.
     */
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

    /**
     * Width of the cursor paint object.
     */
    public static enum CursorShapeWidth {
        /**
         * Single pixel width line.
         */
        LINE,
        /**
         * Two pixels width line.
         */
        DOUBLE,
        /**
         * One quarter of cursor size.
         */
        QUARTER,
        /**
         * Half of cursor size.
         */
        HALF,
        /**
         * Full cursor size.
         */
        FULL
    }

    /**
     * Method for rendering cursor into CodeArea component.
     */
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
