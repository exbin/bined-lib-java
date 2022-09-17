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
package org.exbin.bined.swt.basic;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.exbin.bined.CharsetStreamTranslator;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2018/12/25
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaMetrics {

    @Nullable
    private FontMetrics fontMetrics;
    @Nullable
    private GC gc;

    private int rowHeight;
    private int characterWidth;
    private int fontHeight;
    private int maxBytesPerChar;
    private int subFontSpace = 0;

    /**
     * GC is expected to have proper font set.
     *
     * @param gc graphics context
     * @param charset charset
     */
    public void recomputeMetrics(@Nullable GC gc, Charset charset) {
        this.gc = gc;
        if (gc == null) {
            fontMetrics = null;
            characterWidth = 0;
            fontHeight = 0;
        } else {
            fontMetrics = gc.getFontMetrics();
            fontHeight = fontMetrics.getHeight();
            rowHeight = fontHeight;

            /*
             * Use small 'w' character to guess normal font width.
             */
            characterWidth = gc.textExtent("w").x;
            int fontSize = fontMetrics.getHeight();
            subFontSpace = fontHeight - fontSize;
            try {
                CharsetEncoder encoder = charset.newEncoder();
                maxBytesPerChar = (int) encoder.maxBytesPerChar();
            } catch (UnsupportedOperationException ex) {
                maxBytesPerChar = CharsetStreamTranslator.DEFAULT_MAX_BYTES_PER_CHAR;
            }
        }
    }

    public boolean isInitialized() {
        return rowHeight != 0 && characterWidth != 0;
    }

    @Nullable
    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public int getCharWidth(GC gc, char value) {
        return gc.textExtent(String.valueOf(value)).x;
    }

    public int getCharsWidth(GC gc, char[] data, int offset, int length) {
        return gc.textExtent(String.valueOf(data, offset, length)).x;
    }

    public boolean hasUniformLineMetrics() {
        return false; // TODO fontMetrics.hasUniformLineMetrics();
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
