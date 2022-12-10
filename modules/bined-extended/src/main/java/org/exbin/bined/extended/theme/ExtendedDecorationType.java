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
package org.exbin.bined.extended.theme;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Enumeration of decoration types for extended code area.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum ExtendedDecorationType implements CodeAreaDecorationType {

    ROW_POSITION_LINE("rowPositionLine"),
    HEADER_LINE("headerLine"),
    SPLIT_LINE("sectionSplitLine"),
    BOX_LINE("dataBoxLine");

    private ExtendedDecorationType(String id) {
        this.id = id;
    }

    @Nonnull
    private final String id;

    @Nonnull
    @Override
    public String getId() {
        return id;
    }
}
