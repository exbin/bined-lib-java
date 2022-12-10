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
package org.exbin.bined.javafx.basic;

import com.sun.javafx.tk.FontMetrics;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import javafx.scene.text.Text;
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
    private FontMetrics fontMetrics;

    private int rowHeight;
    private int characterWidth;
    private int fontHeight;
    private int maxBytesPerChar;
    private int subFontSpace = 0;

    public void recomputeMetrics(@Nullable FontMetrics fontMetrics, Charset charset) {
        this.fontMetrics = fontMetrics;
        if (fontMetrics == null) {
            characterWidth = 0;
            fontHeight = 0;
        } else {
            fontHeight = (int) Math.ceil(fontMetrics.getAscent() + fontMetrics.getDescent());
            rowHeight = fontHeight;

            /*
             * Use small 'w' character to guess normal font width.
             */
            Text text = new Text("w");
            text.setFont(fontMetrics.getFont());
            characterWidth = (int) Math.ceil(text.getBoundsInLocal().getWidth());
            int fontSize = (int) fontMetrics.getFont().getSize();
            subFontSpace = fontHeight - fontSize;
            try {
                CharsetEncoder encoder = charset.newEncoder();
                maxBytesPerChar = (int) encoder.maxBytesPerChar();
            } catch (UnsupportedOperationException ex) {
                maxBytesPerChar = CharsetStreamTranslator.DEFAULT_MAX_BYTES_PER_CHAR;
            }

//            Utils.
        }
    }

    public boolean isInitialized() {
        return rowHeight != 0 && characterWidth != 0;
    }

    @Nullable
    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public double getCharWidth(char value) {
        Text text = new Text(Character.toString(value));
        text.setFont(fontMetrics.getFont());
        return text.getBoundsInLocal().getWidth();
    }

    public double getCharsWidth(char[] data, int offset, int length) {
        Text text = new Text(String.valueOf(data, offset, length));
        text.setFont(fontMetrics.getFont());
        return text.getBoundsInLocal().getWidth();
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
