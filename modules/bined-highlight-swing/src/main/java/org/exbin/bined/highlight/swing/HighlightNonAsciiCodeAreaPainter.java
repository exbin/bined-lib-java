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
package org.exbin.bined.highlight.swing;

import java.awt.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Experimental support for highlighting of non-ascii characters.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class HighlightNonAsciiCodeAreaPainter extends HighlightCodeAreaPainter {

    private Color controlCodes;
    private Color aboveCodes;
    private Color textColor;
    private boolean nonAsciiHighlightingEnabled = true;

    public HighlightNonAsciiCodeAreaPainter(CodeAreaCore codeArea) {
        super(codeArea);

        textColor = codeArea.getForeground(); //MainColors().getTextColor();
        if (textColor == null) {
            textColor = Color.BLACK;
        }

        int controlCodesRed = textColor.getRed();
        int controlCodesRedDiff = 0;
        if (controlCodesRed > 128) {
            if (controlCodesRed > 192) {
                controlCodesRedDiff = controlCodesRed - 192;
            }
            controlCodesRed = 255;
        } else {
            controlCodesRed += 127;
        }

        int controlCodesBlue = textColor.getBlue();
        int controlCodesBlueDiff = 0;
        if (controlCodesBlue > 128) {
            if (controlCodesBlue > 192) {
                controlCodesBlueDiff = controlCodesBlue - 192;
            }
            controlCodesBlue = 255;
        } else {
            controlCodesBlue += 127;
        }

        controlCodes = new Color(
                controlCodesRed,
                downShift(textColor.getGreen(), controlCodesBlueDiff + controlCodesRedDiff),
                controlCodesBlue);

        int aboveCodesGreen = textColor.getGreen();
        int aboveCodesGreenDiff = 0;
        if (aboveCodesGreen > 128) {
            if (aboveCodesGreen > 192) {
                aboveCodesGreenDiff = aboveCodesGreen - 192;
            }

            aboveCodesGreen = 255;
        } else {
            aboveCodesGreen += 127;
        }

        int aboveCodesBlue = textColor.getBlue();
        int aboveCodesBlueDiff = 0;
        if (aboveCodesBlue > 128) {
            if (aboveCodesBlue > 192) {
                aboveCodesBlueDiff = aboveCodesBlue - 192;
            }

            aboveCodesBlue = 255;
        } else {
            aboveCodesBlue += 127;
        }

        aboveCodes = new Color(
                downShift(textColor.getRed(), aboveCodesGreenDiff + aboveCodesBlueDiff),
                aboveCodesGreen, aboveCodesBlue);
    }

    private int downShift(int color, int diff) {
        if (color < diff) {
            return 0;
        }

        return color - diff;
    }

    @Nullable
    @Override
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, @Nonnull CodeAreaSection section) {
        Color color = super.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
        if (nonAsciiHighlightingEnabled && section == BasicCodeAreaSection.CODE_MATRIX) {
            if (color == null || textColor.equals(color)) {
                long dataPosition = rowDataPosition + byteOnRow;
                if (dataPosition < codeArea.getDataSize()) {
                    byte value = codeArea.getContentData().getByte(dataPosition);
                    if (value < 0) {
                        color = aboveCodes;
                    } else if (value < 0x20) {
                        color = controlCodes;
                    }
                }
            }
        }

        return color;
    }

    @Nonnull
    public Color getControlCodes() {
        return controlCodes;
    }

    public void setControlCodes(Color controlCodes) {
        this.controlCodes = controlCodes;
    }

    @Nonnull
    public Color getAboveCodes() {
        return aboveCodes;
    }

    public void setAboveCodes(Color aboveCodes) {
        this.aboveCodes = aboveCodes;
    }

    public boolean isNonAsciiHighlightingEnabled() {
        return nonAsciiHighlightingEnabled;
    }

    public void setNonAsciiHighlightingEnabled(boolean nonAsciiHighlightingEnabled) {
        this.nonAsciiHighlightingEnabled = nonAsciiHighlightingEnabled;
    }
}
