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
package org.exbin.bined.swing.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.capability.CodeAreaCapability;
import org.exbin.bined.swing.basic.color.BasicCodeAreaColorsProfile;

/**
 * Support for basic set of colors.
 *
 * @version 0.2.0 2018/11/28
 * @author ExBin Project (https://exbin.org)
 */
public interface BasicColorsCapable {

    @Nullable
    BasicCodeAreaColorsProfile getBasicColors();

    void setBasicColors(@Nonnull BasicCodeAreaColorsProfile colors);

    public static class BasicColorsCapability implements CodeAreaCapability {

    }
}
