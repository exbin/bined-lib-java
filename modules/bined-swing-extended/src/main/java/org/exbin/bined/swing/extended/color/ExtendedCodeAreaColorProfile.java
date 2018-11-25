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
package org.exbin.bined.swing.extended.color;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;
import org.exbin.bined.color.CodeAreaColorsProfile;

/**
 * Color profile for extended code area.
 *
 * @version 0.2.0 2018/11/18
 * @author ExBin Project (https://exbin.org)
 */
public class ExtendedCodeAreaColorProfile implements CodeAreaColorsProfile {

    private final Map<CodeAreaColorType, Color> colors = new HashMap<>();

    public ExtendedCodeAreaColorProfile() {
    }

    /**
     * Returns color of the specified type.
     *
     * @param colorType color type
     * @return color
     */
    @Nullable
    @Override
    public Color getColor(@Nonnull CodeAreaColorType colorType) {
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
    public Color getColor(@Nonnull CodeAreaColorType colorType, @Nonnull CodeAreaBasicColors basicAltColor) {
        Color color = colors.get(colorType);
        return color == null ? colors.get(basicAltColor) : color;
    }

    /**
     * Add new color or or replace existing color.
     *
     * @param colorType color type
     * @param color color value
     */
    public void addColor(@Nonnull CodeAreaColorType colorType, @Nonnull Color color) {
        colors.put(colorType, color);
    }
}
