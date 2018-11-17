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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;

/**
 * Color profile for extended code area.
 *
 * @version 0.2.0 2018/11/17
 * @author ExBin Project (https://exbin.org)
 */
public class ExtendedCodeAreaColorProfile implements CodeAreaColorProfile {

    /**
     * Returns color of the specified type.
     *
     * @param colorType color type
     * @return color
     */
    @Nullable
    @Override
    public Color getColor(@Nonnull CodeAreaColorType colorType) {
        return null;
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
        return null;
    }
}
