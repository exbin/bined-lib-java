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
package org.exbin.deltahex;

import javax.annotation.concurrent.Immutable;

/**
 * Selection range is selection between two positions where begin represents
 * originating point and end of the selection can be before or after begin.
 *
 * @version 0.2.0 2018/01/06
 * @author ExBin Project (http://exbin.org)
 */
@Immutable
public class SelectionRange {

    private final long start;
    private final long end;

    /**
     * Creates empty selection range.
     */
    public SelectionRange() {
        this(0, 0);
    }

    /**
     * Creates selection range from start to end including start and not
     * including end position.
     *
     * @param start selection start position
     * @param end selection end position without actual end position itself
     */
    public SelectionRange(long start, long end) {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("Selection (" + start + ", " + end + ") with negative range is not allowed");
        }

        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    /**
     * Returns first data position of the selection.
     *
     * @return data position
     */
    public long getFirst() {
        return end < start ? end + 1 : start;
    }

    /**
     * Returns last data position of the selection.
     *
     * @return data position
     */
    public long getLast() {
        return end < start ? start : end - 1;
    }

    /**
     * Returns length of the selected area.
     *
     * @return length in bytes
     */
    public long getLength() {
        return end < start ? start - end : end - start;
    }

    /**
     * Returns true if selection is empty.
     *
     * @return true if selection is empty
     */
    public boolean isEmpty() {
        return start == end;
    }

    /**
     * Checks if position belongs to this selection.
     *
     * @param position position
     * @return true if position belongs to current selection range.
     */
    public boolean isInSelection(long position) {
        return end < start ? position > end && position <= start : position >= start && position < end;
    }
}
