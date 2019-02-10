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
package org.exbin.bined.extended.layout;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.BasicCodeAreaSection;

/**
 * Iterator for layout character position iteration.
 *
 * @version 0.2.0 2019/02/10
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface PositionIterator {

    void reset();

    @Nonnull
    SpaceType nextSpaceType();

    int getCharPosition();

    int getBytePosition();

    int getCodeOffset();

    int getHalfCharPosition();

    @Nonnull
    BasicCodeAreaSection getSection();

    boolean isEndReached();

    void skip(int count);
}
