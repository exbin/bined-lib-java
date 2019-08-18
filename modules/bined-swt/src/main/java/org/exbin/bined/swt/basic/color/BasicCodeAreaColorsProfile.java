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
package org.exbin.bined.swt.basic.color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;
import org.exbin.bined.swt.CodeAreaSwtUtils;

/**
 * Basic code area set of colors.
 *
 * @version 0.2.0 2018/12/25
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
    public Color getColor(CodeAreaColorType colorType, CodeAreaBasicColors basicAltColor) {
        Color color = getColor(colorType);
        return (color == null) ? getColor(basicAltColor) : color;
    }

    @Override
    public void reinitialize() {
        Display display = Display.getCurrent();

        textColor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        if (textColor == null) {
            textColor = display.getSystemColor(SWT.COLOR_BLACK);
        }

        textBackground = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        if (textBackground == null) {
            textBackground = display.getSystemColor(SWT.COLOR_WHITE);
        }
        selectionColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
        if (selectionColor == null) {
            selectionColor = display.getSystemColor(SWT.COLOR_WHITE);
        }
        selectionBackground = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
        if (selectionBackground == null) {
            selectionBackground = new Color(display, 96, 96, 255);
        }
        selectionMirrorColor = selectionColor;
        selectionMirrorBackground = CodeAreaSwtUtils.computeGrayColor(selectionBackground);
        cursorColor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        if (cursorColor == null) {
            cursorColor = display.getSystemColor(SWT.COLOR_BLACK);
        }
        cursorNegativeColor = CodeAreaSwtUtils.createNegativeColor(cursorColor);
        decorationLine = display.getSystemColor(SWT.COLOR_GRAY);

        alternateColor = textColor;
        alternateBackground = CodeAreaSwtUtils.createOddColor(textBackground);
    }

    public void dispose() {
        // TODO fix colors resource handling
        textColor.dispose();
        textBackground.dispose();
        selectionColor.dispose();
        selectionBackground.dispose();
        selectionMirrorColor.dispose();
        selectionMirrorBackground.dispose();
        alternateColor.dispose();
        alternateBackground.dispose();
        cursorColor.dispose();
        cursorNegativeColor.dispose();
        decorationLine.dispose();
    }
}
