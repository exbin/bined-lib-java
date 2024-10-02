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
package org.exbin.bined.javafx.basic.color;

import javafx.scene.paint.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;
import org.exbin.bined.javafx.CodeAreaJavaFxUtils;

/**
 * Basic code area set of colors.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaColorsProfile implements CodeAreaColorsProfile {

    @Nullable
    protected Color textColor;
    @Nullable
    protected Color textBackground;
    @Nullable
    protected Color selectionColor;
    @Nullable
    protected Color selectionBackground;
    @Nullable
    protected Color selectionMirrorColor;
    @Nullable
    protected Color selectionMirrorBackground;
    @Nullable
    protected Color alternateColor;
    @Nullable
    protected Color alternateBackground;
    @Nullable
    protected Color cursorColor;
    @Nullable
    protected Color cursorNegativeColor;
    @Nullable
    protected Color decorationLine;

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
    public Color getColor(CodeAreaColorType colorType, CodeAreaBasicColors basicAltColor) {
        Color color = getColor(colorType);
        return (color == null) ? getColor(basicAltColor) : color;
    }

    @Override
    public void reinitialize() {
        //dataView.getStyleClass().contains("-fx-")
        textColor = null;
        if (textColor == null) {
            textColor = Color.BLACK;
        }

        textBackground = null;
        if (textBackground == null) {
            textBackground = Color.WHITE;
        }
        selectionColor = null;
        if (selectionColor == null) {
            selectionColor = Color.WHITE;
        }
        selectionBackground = null;
        if (selectionBackground == null) {
            selectionBackground = new Color(0.375, 0.375, 1, 1);
        }
        selectionMirrorColor = selectionColor;
        selectionMirrorBackground = CodeAreaJavaFxUtils.computeGrayColor(selectionBackground);
        cursorColor = null;
        if (cursorColor == null) {
            cursorColor = Color.BLACK;
        }
        cursorNegativeColor = CodeAreaJavaFxUtils.createNegativeColor(cursorColor);
        decorationLine = Color.GRAY;

        alternateColor = textColor;
        alternateBackground = CodeAreaJavaFxUtils.createOddColor(textBackground);
    }
}
