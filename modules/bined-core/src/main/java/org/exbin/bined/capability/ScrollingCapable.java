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
import org.exbin.bined.CaretPosition;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.ScrollingDirection;

/**
 * Support for code type capability.
 *
 * @version 0.2.0 2019/02/19
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface ScrollingCapable {

    @Nonnull
    CodeAreaScrollPosition getScrollPosition();

    void setScrollPosition(CodeAreaScrollPosition scrollPosition);

    void notifyScrolled();

    void addScrollingListener(ScrollingListener scrollingListener);

    void removeScrollingListener(ScrollingListener scrollingListener);

    @Nonnull
    CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction);

    void updateScrollBars();

    /**
     * Reveals scrolling area for current cursor position.
     */
    void revealCursor();

    /**
     * Reveals scrolling area for given caret position.
     *
     * @param caretPosition caret position
     */
    void revealPosition(CaretPosition caretPosition);

    /**
     * Scrolls scrolling area as centered as possible for current cursor
     * position.
     */
    void centerOnCursor();

    /**
     * Scrolls scrolling area as centered as possible for given caret position.
     *
     * @param caretPosition caret position
     */
    void centerOnPosition(CaretPosition caretPosition);

    public static class ScrollingCapability implements CodeAreaCapability {

    }
}
