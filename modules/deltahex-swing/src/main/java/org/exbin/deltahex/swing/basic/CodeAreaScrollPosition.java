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
package org.exbin.deltahex.swing.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Code Area scrolling position.
 *
 * @version 0.2.0 2018/01/06
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaScrollPosition {

    /**
     * Scrollbar document line position.
     */
    private long scrollLinePosition = 0;

    /**
     * Scrollbar document line pixel offset position.
     */
    private int scrollLineOffset = 0;

    /**
     * Scrollbar document character position.
     */
    private int scrollCharPosition = 0;

    /**
     * Scrollbar document character pixel offset position.
     */
    private int scrollCharOffset = 0;

    /**
     * Relative line data offset.
     */
    private int lineDataOffset = 0;

    @Nonnull
    private VerticalOverflowMode verticalOverflowMode = VerticalOverflowMode.NORMAL;

    public long getScrollLinePosition() {
        return scrollLinePosition;
    }

    public int getScrollLineOffset() {
        return scrollLineOffset;
    }

    public int getScrollCharPosition() {
        return scrollCharPosition;
    }

    public int getScrollCharOffset() {
        return scrollCharOffset;
    }

    public void setScrollLinePosition(long scrollLinePosition) {
        this.scrollLinePosition = scrollLinePosition;
    }

    public void setScrollLineOffset(int scrollLineOffset) {
        this.scrollLineOffset = scrollLineOffset;
    }

    public void setScrollCharPosition(int scrollCharPosition) {
        this.scrollCharPosition = scrollCharPosition;
    }

    public void setScrollCharOffset(int scrollCharOffset) {
        this.scrollCharOffset = scrollCharOffset;
    }

    public int getLineDataOffset() {
        return lineDataOffset;
    }

    public void setLineDataOffset(int lineDataOffset) {
        this.lineDataOffset = lineDataOffset;
    }

    @Nonnull
    public VerticalOverflowMode getVerticalOverflowMode() {
        return verticalOverflowMode;
    }

    public void setVerticalOverflowMode(@Nonnull VerticalOverflowMode verticalOverflowMode) {
        this.verticalOverflowMode = verticalOverflowMode;
    }

    public void setScrollPosition(@Nullable CodeAreaScrollPosition scrollPosition) {
        if (scrollPosition == null) {
            reset();
        } else {
            scrollLinePosition = scrollPosition.getScrollLinePosition();
            scrollLineOffset = scrollPosition.getScrollLineOffset();
            scrollCharPosition = scrollPosition.getScrollCharPosition();
            scrollCharOffset = scrollPosition.getScrollCharOffset();
            lineDataOffset = scrollPosition.getLineDataOffset();
        }
    }

    /**
     * Resets scrolling position to top left corner.
     */
    public void reset() {
        scrollLinePosition = 0;
        scrollLineOffset = 0;
        scrollCharPosition = 0;
        scrollCharOffset = 0;
        lineDataOffset = 0;
    }

    /**
     * Enumeration of vertical overflow modes.
     */
    public enum VerticalOverflowMode {
        /**
         * Normal ratio 1 on 1.
         */
        NORMAL,
        /**
         * Height is more than available precision and scaled.
         */
        OVERFLOW
    }
}
