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
package org.exbin.bined.highlight.swing.section;

import java.awt.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.highlight.swing.color.CodeAreaColorizationColorType;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.binary_data.BinaryData;

/**
 * Experimental support for highlighting of non-ascii characters.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectionHighlightNonAsciiCodeAreaPainter extends SectionHighlightCodeAreaPainter {

    private Color controlCodes;
    private Color upperCodes;
    private Color textColor;
    private boolean nonAsciiHighlightingEnabled = true;

    public SectionHighlightNonAsciiCodeAreaPainter(CodeAreaCore codeArea) {
        super(codeArea);

        textColor = codeArea.getForeground(); //MainColors().getTextColor();
        if (textColor == null) {
            textColor = Color.BLACK;
        }

        int controlCodesRed = textColor.getRed();
        int controlCodesRedDiff = 0;
        if (controlCodesRed > 32) {
            if (controlCodesRed > 192) {
                controlCodesRedDiff = controlCodesRed - 192;
            }
            controlCodesRed = 255;
        } else {
            controlCodesRed += 224;
        }

        int controlCodesBlue = textColor.getBlue();
        int controlCodesBlueDiff = 0;
        if (controlCodesBlue > 32) {
            if (controlCodesBlue > 192) {
                controlCodesBlueDiff = controlCodesBlue - 192;
            }
            controlCodesBlue = 255;
        } else {
            controlCodesBlue += 224;
        }

        controlCodes = new Color(
                controlCodesRed,
                downShift(textColor.getGreen(), controlCodesBlueDiff + controlCodesRedDiff),
                controlCodesBlue);

        int upperCodesGreen = textColor.getGreen();
        int upperCodesGreenDiff = 0;
        if (upperCodesGreen > 64) {
            if (upperCodesGreen > 192) {
                upperCodesGreenDiff = upperCodesGreen - 192;
            }

            upperCodesGreen = 255;
        } else {
            upperCodesGreen += 192;
        }

        int upperCodesBlue = textColor.getBlue();
        int upperCodesBlueDiff = 0;
        if (upperCodesBlue > 64) {
            if (upperCodesBlue > 192) {
                upperCodesBlueDiff = upperCodesBlue - 192;
            }

            upperCodesBlue = 255;
        } else {
            upperCodesBlue += 192;
        }

        upperCodes = new Color(
                downShift(textColor.getRed(), upperCodesGreenDiff + upperCodesBlueDiff),
                upperCodesGreen, upperCodesBlue);
    }

    private int downShift(int color, int diff) {
        if (color < diff) {
            return 0;
        }

        return color - diff;
    }

    @Nullable
    @Override
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables) {
        Color color = super.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
        if (nonAsciiHighlightingEnabled && section == BasicCodeAreaSection.CODE_MATRIX) {
            if (color == null || textColor.equals(color)) {
                long dataPosition = rowDataPosition + byteOnRow;
                if (dataPosition < codeArea.getDataSize()) {
                    BinaryData contentData = codeArea.getContentData();
                    byte value = contentData.getByte(dataPosition);
                    if (value < 0) {
                        Color upperCodesColor = getColorsProfile().getColor(CodeAreaColorizationColorType.UPPER_CODES_COLOR);
                        color = upperCodesColor != null ? upperCodesColor : upperCodes;
                    } else if (value < 0x20) {
                        Color controlCodesColor = getColorsProfile().getColor(CodeAreaColorizationColorType.CONTROL_CODES_COLOR);
                        color = controlCodesColor != null ? controlCodesColor : controlCodes;
                    }
                }
            }
        }

        return color;
    }

    @Nullable
    @Override
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables) {
        Color color = super.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
        if (nonAsciiHighlightingEnabled && section == BasicCodeAreaSection.CODE_MATRIX) {
            if (color == null || textColor.equals(color)) {
                long dataPosition = rowDataPosition + byteOnRow;
                if (dataPosition < codeArea.getDataSize()) {
                    BinaryData contentData = codeArea.getContentData();
                    byte value = contentData.getByte(dataPosition);
                    if (value < 0) {
                        Color upperCodesBackground = getColorsProfile().getColor(CodeAreaColorizationColorType.UPPER_CODES_BACKGROUND);
                        if (upperCodesBackground != null) {
                            color = upperCodesBackground;
                        }
                    } else if (value < 0x20) {
                        Color controlCodesBackground = getColorsProfile().getColor(CodeAreaColorizationColorType.CONTROL_CODES_BACKGROUND);
                        if (controlCodesBackground != null) {
                            color = controlCodesBackground;
                        }
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
    public Color getUpperCodes() {
        return upperCodes;
    }

    public void setUpperCodes(Color upperCodes) {
        this.upperCodes = upperCodes;
    }

    public boolean isNonAsciiHighlightingEnabled() {
        return nonAsciiHighlightingEnabled;
    }

    public void setNonAsciiHighlightingEnabled(boolean nonAsciiHighlightingEnabled) {
        this.nonAsciiHighlightingEnabled = nonAsciiHighlightingEnabled;
    }
}
