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
package org.exbin.bined.javafx.basic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Timer;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;

/**
 * Default implementation of code area caret.
 *
 * @version 0.2.0 2021/07/28
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCaret implements CodeAreaCaret {

    private static final int DOUBLE_CURSOR_WIDTH = 2;
    private static final int DEFAULT_BLINK_RATE = 450;

    @Nonnull
    private final CaretChangeListener changeListener;
    private final DefaultCodeAreaCaretPosition caretPosition = new DefaultCodeAreaCaretPosition();

    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private boolean cursorVisible = true;

    @Nonnull
    private CursorRenderingMode renderingMode = CursorRenderingMode.PAINT;

    public DefaultCodeAreaCaret(CaretChangeListener changeListener) {
        CodeAreaUtils.requireNonNull(changeListener, "Change listener cannot be null");

        this.changeListener = changeListener;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public static int getCursorThickness(CursorShape cursorShape, int characterWidth, int lineHeight) {
        switch (cursorShape) {
            case INSERT:
                return DOUBLE_CURSOR_WIDTH;
            case OVERWRITE:
            case MIRROR:
                return characterWidth;
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
        changeListener.notifyCaretChanged();
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
        Optional<CodeAreaSection> section = caretPosition.getSection();
        return section.orElse(BasicCodeAreaSection.CODE_MATRIX);
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

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    @Nonnull
    public CursorRenderingMode getRenderingMode() {
        return renderingMode;
    }

    public void setRenderingMode(CursorRenderingMode renderingMode) {
        CodeAreaUtils.requireNonNull(renderingMode, "Cursor rendering mode cannot be null");

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
    public enum CursorShape {
        INSERT, OVERWRITE, MIRROR
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

    /**
     * Interface for changes listener.
     */
    public interface CaretChangeListener {

        /**
         * Caret changed.
         */
        void notifyCaretChanged();
    }
}
