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
package org.exbin.bined.extended.capability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.basic.VerticalScrollUnit;
import org.exbin.bined.extended.ExtendedHorizontalScrollUnit;

/**
 * Support for code type capability for extended code area.
 *
 * @version 0.2.0 2019/02/19
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface ExtendedScrollingCapable {

    /**
     * Returns vertical scrollbar visibility mode.
     *
     * @return scrollbar visibility mode
     */
    @Nonnull
    ScrollBarVisibility getVerticalScrollBarVisibility();

    /**
     * Sets vertical scrollbar visibility mode.
     *
     * @param verticalScrollBarVisibility scrollbar visibility mode
     */
    void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility);

    /**
     * Returns vertical scrolling unit.
     *
     * @return vertical scrolling unit
     */
    @Nonnull
    VerticalScrollUnit getVerticalScrollUnit();

    /**
     * Sets vertical scrolling unit.
     *
     * @param verticalScrollUnit vertical scrolling unit
     */
    void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit);

    /**
     * Returns horizotal scrollbar visibility mode.
     *
     * @return scrollbar visibility mode
     */
    @Nonnull
    ScrollBarVisibility getHorizontalScrollBarVisibility();

    /**
     * Sets horizotal scrollbar visibility mode.
     *
     * @param horizontalScrollBarVisibility scrollbar visibility mode
     */
    void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility);

    /**
     * Returns horizontal scrolling unit.
     *
     * @return horizontal scrolling unit
     */
    @Nonnull
    ExtendedHorizontalScrollUnit getHorizontalScrollUnit();

    /**
     * Sets horizontal scrolling unit.
     *
     * @param horizontalScrollUnit horizontal scrolling unit
     */
    void setHorizontalScrollUnit(ExtendedHorizontalScrollUnit horizontalScrollUnit);
}
