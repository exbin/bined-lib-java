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
package org.exbin.bined.javafx;

import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.ScrollBarVisibility;

/**
 * Hexadecimal editor component JavaFX utilities.
 *
 * @version 0.2.0 2018/09/02
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaJavaFxUtils {

    public static final int MIN_MONOSPACE_CODE_POINT = 0x1F;
    public static final int MAX_MONOSPACE_CODE_POINT = 0x1C3;
    public static final int INV_SPACE_CODE_POINT = 0x7f;
    public static final int EXCEPTION1_CODE_POINT = 0x8e;
    public static final int EXCEPTION2_CODE_POINT = 0x9e;

    public static double MAX_COMPONENT_VALUE = 1;

    private CodeAreaJavaFxUtils() {
    }

    /**
     * Detect if character is in unicode range covered by monospace fonts width
     * exactly full width.
     *
     * @param character input character
     * @return true if character is suppose to have exactly full width
     */
    public static boolean isMonospaceFullWidthCharater(char character) {
        return (character > MIN_MONOSPACE_CODE_POINT && (int) character < MAX_MONOSPACE_CODE_POINT
                && character != INV_SPACE_CODE_POINT
                && character != EXCEPTION1_CODE_POINT && character != EXCEPTION2_CODE_POINT);
    }

    public static boolean areSameColors(@Nullable Color color, @Nullable Color comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    @Nonnull
    public static Color createOddColor(@Nonnull Color color) {
        return Color.color(
                computeOddColorComponent(color.getRed()),
                computeOddColorComponent(color.getGreen()),
                computeOddColorComponent(color.getBlue()));
    }

    public static double computeOddColorComponent(double colorComponent) {
        return colorComponent + (colorComponent > 0.25 ? -0.0625 : 0.0625);
    }

    @Nonnull
    public static Color createNegativeColor(@Nonnull Color color) {
        return Color.color(
                MAX_COMPONENT_VALUE - color.getRed(),
                MAX_COMPONENT_VALUE - color.getGreen(),
                MAX_COMPONENT_VALUE - color.getBlue());
    }

    @Nonnull
    public static Color computeGrayColor(@Nonnull Color color) {
        double grayLevel = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return Color.color(grayLevel, grayLevel, grayLevel);
    }

    @Nonnull
    public static ScrollPane.ScrollBarPolicy getVerticalScrollBarPolicy(@Nonnull ScrollBarVisibility scrollBarVisibility) {
        switch (scrollBarVisibility) {
            case NEVER:
                return ScrollPane.ScrollBarPolicy.NEVER;
            case ALWAYS:
                return ScrollPane.ScrollBarPolicy.ALWAYS;
            case IF_NEEDED:
                return ScrollPane.ScrollBarPolicy.AS_NEEDED;
            default:
                throw new IllegalStateException("Unexpected scrollBarVisibility type " + scrollBarVisibility.name());
        }
    }

    @Nonnull
    public static ScrollPane.ScrollBarPolicy getHorizontalScrollBarPolicy(@Nonnull ScrollBarVisibility scrollBarVisibility) {
        switch (scrollBarVisibility) {
            case NEVER:
                return ScrollPane.ScrollBarPolicy.NEVER;
            case ALWAYS:
                return ScrollPane.ScrollBarPolicy.ALWAYS;
            case IF_NEEDED:
                return ScrollPane.ScrollBarPolicy.AS_NEEDED;
            default:
                throw new IllegalStateException("Unexpected scrollBarVisibility type " + scrollBarVisibility.name());
        }
    }
}
