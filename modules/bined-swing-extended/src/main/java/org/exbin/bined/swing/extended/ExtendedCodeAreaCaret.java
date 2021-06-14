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
package org.exbin.bined.swing.extended;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Timer;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.bined.CodeAreaCaretPosition;

/**
 * Default implementation of code area caret.
 *
 * @version 0.2.0 2019/07/07
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaCaret implements CodeAreaCaret {

    private static final int LINE_CURSOR_WIDTH = 1;
    private static final int DOUBLE_CURSOR_WIDTH = 2;
    private static final int DEFAULT_BLINK_RATE = 450;

    @Nonnull
    private final CodeArea codeArea;
    private final DefaultCodeAreaCaretPosition caretPosition = new DefaultCodeAreaCaretPosition();

    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private boolean cursorVisible = true;

    @Nonnull
    private CursorShape insertCursorShape = CursorShape.DOUBLE_LEFT;
    @Nonnull
    private CursorShape overwriteCursorShape = CursorShape.BOX;
    @Nonnull
    private CursorRenderingMode renderingMode = CursorRenderingMode.PAINT; //NEGATIVE;

    public ExtendedCodeAreaCaret(CodeArea codeArea) {
        CodeAreaUtils.requireNonNull(codeArea);

        this.codeArea = codeArea;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public int getCursorThickness(CursorShape cursorShape, int characterWidth, int lineHeight) {
        switch (cursorShape.getWidth()) {
            case LINE:
                return LINE_CURSOR_WIDTH;
            case DOUBLE:
                return DOUBLE_CURSOR_WIDTH;
            case QUARTER: {
                if (cursorShape == CursorShape.QUARTER_LEFT || cursorShape == CursorShape.QUARTER_RIGHT) {
                    return characterWidth / 4;
                } else {
                    return lineHeight / 4;
                }
            }
            case HALF: {
                if (cursorShape == CursorShape.HALF_LEFT || cursorShape == CursorShape.HALF_RIGHT) {
                    return characterWidth / 2;
                } else {
                    return lineHeight / 2;
                }
            }
        }

        return -1;
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition getCaretPosition() {
        return caretPosition;
    }

    public void resetBlink() {
        if (blinkTimer != null) {
            cursorVisible = true;
            blinkTimer.restart();
        }
    }

    private void notifyCaredChanged() {
        ((CaretCapable) codeArea).notifyCaretChanged();
    }

    @Override
    public void setCaretPosition(@Nullable CodeAreaCaretPosition caretPosition) {
        if (caretPosition != null) {
            this.caretPosition.setPosition(caretPosition);
        } else {
            this.caretPosition.reset();
        }
        resetBlink();
    }

    @Override
    public void setCaretPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(0);
        resetBlink();
    }

    @Override
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

    @Nonnull
    @Override
    public CodeAreaSection getSection() {
        return caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
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

    @Nonnull
    public CursorShape getInsertCursorShape() {
        return insertCursorShape;
    }

    public void setInsertCursorShape(CursorShape insertCursorShape) {
        CodeAreaUtils.requireNonNull(insertCursorShape);

        this.insertCursorShape = insertCursorShape;
        notifyCaredChanged();
    }

    @Nonnull
    public CursorShape getOverwriteCursorShape() {
        return overwriteCursorShape;
    }

    public void setOverwriteCursorShape(CursorShape overwriteCursorShape) {
        CodeAreaUtils.requireNonNull(overwriteCursorShape);

        this.overwriteCursorShape = overwriteCursorShape;
        notifyCaredChanged();
    }

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    @Nonnull
    public CursorRenderingMode getRenderingMode() {
        return renderingMode;
    }

    public void setRenderingMode(CursorRenderingMode renderingMode) {
        CodeAreaUtils.requireNonNull(renderingMode);

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

    @ParametersAreNonnullByDefault
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
    @ParametersAreNonnullByDefault
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

        CursorShape(CursorShapeWidth width) {
            this.width = width;
        }

        @Nonnull
        public CursorShapeWidth getWidth() {
            return width;
        }
    }

    /**
     * Width of the cursor paint object.
     */
    public enum CursorShapeWidth {
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
    public enum CursorRenderingMode {
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
