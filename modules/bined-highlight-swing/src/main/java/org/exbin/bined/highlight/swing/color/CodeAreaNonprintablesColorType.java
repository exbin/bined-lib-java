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
package org.exbin.bined.highlight.swing.color;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorGroup;
import org.exbin.bined.color.CodeAreaColorType;

/**
 * Enumeration of non-printable color types.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum CodeAreaNonprintablesColorType implements CodeAreaColorType {

    NONPRINTABLES_COLOR(CodeAreaBasicColors.TEXT_COLOR.getId(), NonprintableCodeAreaColorsGroup.NONPRINTABLES),
    NONPRINTABLES_BACKGROUND(CodeAreaBasicColors.TEXT_BACKGROUND.getId(), NonprintableCodeAreaColorsGroup.NONPRINTABLES);

    @Nonnull
    private final String typeId;
    @Nullable
    private final CodeAreaColorGroup group;

    private CodeAreaNonprintablesColorType(String typeId, @Nullable CodeAreaColorGroup group) {
        this.typeId = typeId;
        this.group = group;
    }

    @Nonnull
    @Override
    public String getId() {
        return typeId;
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorGroup> getGroup() {
        return Optional.ofNullable(group);
    }
}
