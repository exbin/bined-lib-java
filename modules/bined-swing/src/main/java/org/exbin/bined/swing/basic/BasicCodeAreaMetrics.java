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
package org.exbin.bined.swing.basic;

import java.awt.FontMetrics;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CharsetStreamTranslator;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaMetrics {

    @Nullable
    protected FontMetrics fontMetrics;

    protected int rowHeight;
    protected int characterWidth;
    protected int fontHeight;
    protected int maxBytesPerChar;
    protected int subFontSpace = 0;

    public void recomputeMetrics(@Nullable FontMetrics fontMetrics, Charset charset) {
        this.fontMetrics = fontMetrics;
        if (fontMetrics == null) {
            characterWidth = 0;
            fontHeight = 0;
        } else {
            fontHeight = fontMetrics.getHeight();
            rowHeight = fontHeight;

            /*
             * Use small 'm' character to guess normal font width.
             */
            characterWidth = fontMetrics.charWidth('m');
            int fontSize = fontMetrics.getFont().getSize();
            subFontSpace = rowHeight - fontSize;
        }

        try {
            CharsetEncoder encoder = charset.newEncoder();
            maxBytesPerChar = (int) encoder.maxBytesPerChar();
        } catch (UnsupportedOperationException ex) {
            maxBytesPerChar = CharsetStreamTranslator.DEFAULT_MAX_BYTES_PER_CHAR;
        }
    }

    public boolean isInitialized() {
        return rowHeight != 0 && characterWidth != 0;
    }

    @Nonnull
    public Optional<FontMetrics> getFontMetrics() {
        return Optional.ofNullable(fontMetrics);
    }

    public int getCharWidth(char value) {
        return fontMetrics.charWidth(value);
    }

    public int getCharsWidth(char[] data, int offset, int length) {
        // TODO use code point?
        try {
            return fontMetrics.charsWidth(data, offset, length);
        } catch (Throwable ex) {
            return characterWidth;
        }
    }

    public boolean hasUniformLineMetrics() {
        return fontMetrics.hasUniformLineMetrics();
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getCharacterWidth() {
        return characterWidth;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }

    public int getMaxBytesPerChar() {
        return maxBytesPerChar;
    }
}
