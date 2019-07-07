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
package org.exbin.bined.capability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.basic.HorizontalScrollUnit;
import org.exbin.bined.basic.VerticalScrollUnit;

/**
 * Support for position revealing.
 *
 * @version 0.2.0 2019/02/19
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BasicScrollingCapable {

    @Nonnull
    ScrollBarVisibility getVerticalScrollBarVisibility();

    void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility);

    @Nonnull
    VerticalScrollUnit getVerticalScrollUnit();

    void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit);

    @Nonnull
    ScrollBarVisibility getHorizontalScrollBarVisibility();

    void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility);

    @Nonnull
    HorizontalScrollUnit getHorizontalScrollUnit();

    void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit);
}
