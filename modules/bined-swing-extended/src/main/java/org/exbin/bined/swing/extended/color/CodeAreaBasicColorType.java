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

import org.exbin.bined.color.CodeAreaColorType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.basic.BasicCodeAreaColorGroup;
import org.exbin.bined.color.CodeAreaColorGroup;

/**
 * Enumeration of basic color types.
 *
 * @version 0.2.0 2018/11/13
 * @author ExBin Project (https://exbin.org)
 */
public enum CodeAreaBasicColorType implements CodeAreaColorType {

    COLOR("basic.color", BasicCodeAreaColorGroup.MAIN),
    BACKGROUND("basic.background", BasicCodeAreaColorGroup.MAIN),
    SELECTION("basic.selection", BasicCodeAreaColorGroup.SELECTION),
    MIRROR_SELECTION("basic.mirror_selection", null);

    @Nonnull
    private final String typeId;
    private final CodeAreaColorGroup group;

    private CodeAreaBasicColorType(@Nonnull String typeId, @Nullable CodeAreaColorGroup group) {
        this.typeId = typeId;
        this.group = group;
    }

    @Nonnull
    @Override
    public String getId() {
        return typeId;
    }

    @Nullable
    @Override
    public CodeAreaColorGroup getGroup() {
        return group;
    }
}
