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
package org.exbin.bined.swing.extended.layout;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.extended.theme.CodeAreaDecorationType;

/**
 * Enumeration of supported decorations of extended code area.
 *
 * @version 0.2.0 2018/12/18
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum ExtendedCodeAreaDecorations implements CodeAreaDecorationType {

    ROW_POSITION_LINE("rowNumberLine"),
    HEADER_LINE("headerLine"),
    SPLIT_LINE("splitLine"),
    BOX_LINES("boxLines"),
    GROUP_LINES("groupLines");

    @Nonnull
    private final String id;

    ExtendedCodeAreaDecorations(String id) {
        this.id = id;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }
}
