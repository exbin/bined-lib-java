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
package org.exbin.bined.section.layout;

import javax.annotation.Nonnull;
import org.exbin.bined.basic.BasicCodeAreaSection;

/**
 * Iterator for layout character position iteration.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface PositionIterator {

    /**
     * Resets iterator to the begining of the document.
     */
    void reset();

    /**
     * Returns space type after current position.
     *
     * @return space type
     */
    @Nonnull
    SpaceType nextSpaceType();

    /**
     * Returns current iteration step position.
     *
     * @return iteration step position
     */
    int getPosition();

    /**
     * Returns byte on row offset position.
     *
     * @return byte on row offset position
     */
    int getBytePosition();

    /**
     * Returns offset in the code position.
     *
     * @return offset in the code position
     */
    int getCodeOffset();

    /**
     * Returns character position with half width character size.
     *
     * @return half character position
     */
    int getHalfCharPosition();

    /**
     * Returns section for the current position
     *
     * @return code area section
     */
    @Nonnull
    BasicCodeAreaSection getSection();

    /**
     * Returns true if end of document is reached.
     *
     * @return true if end of document is reached
     */
    boolean isEndReached();

    /**
     * Skips given number of positions.
     *
     * @param count number of positions
     */
    void skip(int count);
}
