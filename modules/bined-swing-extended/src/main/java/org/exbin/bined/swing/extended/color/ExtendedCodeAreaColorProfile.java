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
package org.exbin.bined.swing.extended.color;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.UIManager;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.extended.color.CodeAreaUnprintablesColorType;
import org.exbin.bined.swing.CodeAreaSwingUtils;

/**
 * Color profile for extended code area.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaColorProfile implements CodeAreaColorsProfile {

    private final Map<CodeAreaColorType, Color> colors = new HashMap<>();
    private boolean inheritSystemColors = true;

    public ExtendedCodeAreaColorProfile() {
    }

    /**
     * Copy constructor.
     *
     * @return full copy of profile
     */
    @Nonnull
    public ExtendedCodeAreaColorProfile createCopy() {
        ExtendedCodeAreaColorProfile copy = new ExtendedCodeAreaColorProfile();
        copy.colors.putAll(colors);
        copy.inheritSystemColors = inheritSystemColors;
        return copy;
    }

    /**
     * Returns color of the specified type.
     *
     * @param colorType color type
     * @return color
     */
    @Nullable
    @Override
    public Color getColor(CodeAreaColorType colorType) {
        return colors.get(colorType);
    }

    /**
     * Returns color of the specified type.
     *
     * @param colorType color type
     * @return color
     */
    @Nullable
    @Override
    public Color getColor(CodeAreaColorType colorType, @Nullable CodeAreaBasicColors basicAltColor) {
        Color color = colors.get(colorType);
        return color == null ? (basicAltColor == null ? null : colors.get(basicAltColor)) : color;
    }

    /**
     * Sets color as replacement of the existing color.
     *
     * @param colorType color type
     * @param color color value
     */
    public void setColor(CodeAreaColorType colorType, Color color) {
        inheritSystemColors = false;
        colors.put(colorType, color);
    }

    public void removeColor(CodeAreaColorType colorType) {
        colors.remove(colorType);
    }

    public boolean isInheritSystemColors() {
        return inheritSystemColors;
    }

    public void setInheritSystemColors(boolean inheritSystemColors) {
        this.inheritSystemColors = inheritSystemColors;
        if (!inheritSystemColors) {
            reinitialize();
        }
    }

    @Override
    public void reinitialize() {
        if (inheritSystemColors) {
            Color textColor = UIManager.getColor("TextArea.foreground");
            if (textColor == null) {
                textColor = Color.BLACK;
            }
            setColor(CodeAreaBasicColors.TEXT_COLOR, textColor);

            Color textBackground = UIManager.getColor("TextArea.background");
            if (textBackground == null) {
                textBackground = Color.WHITE;
            }
            setColor(CodeAreaBasicColors.TEXT_BACKGROUND, textBackground);

            Color selectionColor = UIManager.getColor("TextArea.selectionForeground");
            if (selectionColor == null) {
                selectionColor = Color.WHITE;
            }
            setColor(CodeAreaBasicColors.SELECTION_COLOR, selectionColor);

            Color selectionBackground = UIManager.getColor("TextArea.selectionBackground");
            if (selectionBackground == null) {
                selectionBackground = new Color(96, 96, 255);
            }
            setColor(CodeAreaBasicColors.SELECTION_BACKGROUND, selectionBackground);

            Color selectionMirrorColor = selectionColor;
            setColor(CodeAreaBasicColors.SELECTION_MIRROR_COLOR, selectionMirrorColor);

            Color selectionMirrorBackground = CodeAreaSwingUtils.computeGrayColor(selectionBackground);
            setColor(CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND, selectionMirrorBackground);

            Color cursorColor = UIManager.getColor("TextArea.caretForeground");
            if (cursorColor == null) {
                cursorColor = Color.BLACK;
            }
            setColor(CodeAreaBasicColors.CURSOR_COLOR, cursorColor);

            Color cursorNegativeColor = CodeAreaSwingUtils.createNegativeColor(cursorColor);
            setColor(CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR, cursorNegativeColor);

            Color decorationLine = Color.GRAY;
            setColor(BasicCodeAreaDecorationColorType.LINE, decorationLine);

            Color alternateColor = textColor;
            setColor(CodeAreaBasicColors.ALTERNATE_COLOR, alternateColor);

            Color alternateBackground = CodeAreaSwingUtils.createOddColor(textBackground);
            setColor(CodeAreaBasicColors.ALTERNATE_BACKGROUND, alternateBackground);

            Color unprintablesColor = new Color(textColor.getRed(), textColor.getGreen(), (textColor.getBlue() + 196) % 256);
            setColor(CodeAreaUnprintablesColorType.UNPRINTABLES_COLOR, unprintablesColor);
        }
    }
}
