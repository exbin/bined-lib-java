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
package org.exbin.bined.highlight.swing.color;

import javax.annotation.Nonnull;
import org.exbin.bined.color.CodeAreaColorGroup;

/**
 * Enumeration of color groups for code area colorization.
 *
 * @version 0.2.0 2018/11/30
 * @author ExBin Project (https://exbin.org)
 */
public enum ColorizationCodeAreaColorsGroup implements CodeAreaColorGroup {

    COLORIZATION("colorization");

    @Nonnull
    private final String groupId;

    private ColorizationCodeAreaColorsGroup(@Nonnull String groupId) {
        this.groupId = groupId;
    }

    @Nonnull
    @Override
    public String getId() {
        return groupId;
    }
}
