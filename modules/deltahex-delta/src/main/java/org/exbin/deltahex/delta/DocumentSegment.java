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
package org.exbin.deltahex.delta;

/**
 * Data segment pointing to file.
 *
 * @version 0.1.0 2016/06/07
 * @author ExBin Project (http://exbin.org)
 */
public class DocumentSegment extends DataSegment {

    private long startPosition;
    private long length;

    public DocumentSegment(long startPosition, long length) {
        this.startPosition = startPosition;
        this.length = length;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public DataSegment copy() {
        return new DocumentSegment(startPosition, length);
    }
}
