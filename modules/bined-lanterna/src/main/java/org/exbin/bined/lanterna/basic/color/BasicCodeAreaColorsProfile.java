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
package org.exbin.bined.lanterna.basic.color;

import java.awt.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.UIManager;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;
import org.exbin.bined.lanterna.CodeAreaLanternaUtils;

/**
 * Basic code area set of colors.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaColorsProfile implements CodeAreaColorsProfile {

    @Nullable
    private Color textColor;
    @Nullable
    private Color textBackground;
    @Nullable
    private Color selectionColor;
    @Nullable
    private Color selectionBackground;
    @Nullable
    private Color selectionMirrorColor;
    @Nullable
    private Color selectionMirrorBackground;
    @Nullable
    private Color alternateColor;
    @Nullable
    private Color alternateBackground;
    @Nullable
    private Color cursorColor;
    @Nullable
    private Color cursorNegativeColor;
    @Nullable
    private Color decorationLine;

    public BasicCodeAreaColorsProfile() {
    }

    @Nonnull
    public Color getTextColor() {
        return CodeAreaUtils.requireNonNull(textColor);
    }

    @Nonnull
    public Color getTextBackground() {
        return CodeAreaUtils.requireNonNull(textBackground);
    }

    @Nonnull
    public Color getSelectionColor() {
        return CodeAreaUtils.requireNonNull(selectionColor);
    }

    @Nonnull
    public Color getSelectionBackground() {
        return CodeAreaUtils.requireNonNull(selectionBackground);
    }

    @Nonnull
    public Color getSelectionMirrorColor() {
        return CodeAreaUtils.requireNonNull(selectionMirrorColor);
    }

    @Nonnull
    public Color getSelectionMirrorBackground() {
        return CodeAreaUtils.requireNonNull(selectionMirrorBackground);
    }

    @Nonnull
    public Color getAlternateColor() {
        return CodeAreaUtils.requireNonNull(alternateColor);
    }

    @Nonnull
    public Color getAlternateBackground() {
        return CodeAreaUtils.requireNonNull(alternateBackground);
    }

    @Nonnull
    public Color getCursorColor() {
        return CodeAreaUtils.requireNonNull(cursorColor);
    }

    @Nonnull
    public Color getCursorNegativeColor() {
        return CodeAreaUtils.requireNonNull(cursorNegativeColor);
    }

    @Nonnull
    public Color getDecorationLine() {
        return CodeAreaUtils.requireNonNull(decorationLine);
    }

    @Nullable
    @Override
    public Color getColor(CodeAreaColorType colorType) {
        if (colorType == CodeAreaBasicColors.TEXT_COLOR) {
            return textColor;
        }
        if (colorType == CodeAreaBasicColors.TEXT_BACKGROUND) {
            return textBackground;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_COLOR) {
            return selectionColor;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_BACKGROUND) {
            return selectionBackground;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_MIRROR_COLOR) {
            return selectionMirrorColor;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND) {
            return selectionMirrorBackground;
        }
        if (colorType == CodeAreaBasicColors.ALTERNATE_COLOR) {
            return alternateColor;
        }
        if (colorType == CodeAreaBasicColors.ALTERNATE_BACKGROUND) {
            return alternateBackground;
        }
        if (colorType == CodeAreaBasicColors.CURSOR_COLOR) {
            return cursorColor;
        }
        if (colorType == CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR) {
            return cursorNegativeColor;
        }
        if (colorType == BasicCodeAreaDecorationColorType.LINE) {
            return decorationLine;
        }

        return null;
    }

    @Nullable
    @Override
    public Color getColor(CodeAreaColorType colorType, @Nullable CodeAreaBasicColors basicAltColor) {
        Color color = getColor(colorType);
        return (color == null) ? (basicAltColor == null ? null : getColor(basicAltColor)) : color;
    }

    @Override
    public void reinitialize() {
        textColor = UIManager.getColor("TextArea.foreground");
        if (textColor == null) {
            textColor = Color.BLACK;
        }

        textBackground = UIManager.getColor("TextArea.background");
        if (textBackground == null) {
            textBackground = Color.WHITE;
        }
        selectionColor = UIManager.getColor("TextArea.selectionForeground");
        if (selectionColor == null) {
            selectionColor = Color.WHITE;
        }
        selectionBackground = UIManager.getColor("TextArea.selectionBackground");
        if (selectionBackground == null) {
            selectionBackground = new Color(96, 96, 255);
        }
        selectionMirrorColor = selectionColor;
        selectionMirrorBackground = CodeAreaLanternaUtils.computeGrayColor(selectionBackground);
        cursorColor = UIManager.getColor("TextArea.caretForeground");
        if (cursorColor == null) {
            cursorColor = Color.BLACK;
        }
        cursorNegativeColor = CodeAreaLanternaUtils.createNegativeColor(cursorColor);
        decorationLine = Color.GRAY;

        alternateColor = textColor;
        alternateBackground = CodeAreaLanternaUtils.createOddColor(textBackground);
    }
}
