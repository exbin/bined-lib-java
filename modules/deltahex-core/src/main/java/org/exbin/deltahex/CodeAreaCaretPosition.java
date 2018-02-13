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

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Specifies caret position as combination of data position, section and code
 * offset of code representation.
 *
 * @version 0.2.0 2018/02/13
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaCaretPosition implements CaretPosition {

    private long dataPosition = 0;
    private int codeOffset = 0;
    @Nonnull
    private CodeAreaSection section = CodeAreaSection.CODE_MATRIX;

    public CodeAreaCaretPosition() {
    }

    public CodeAreaCaretPosition(long dataPosition, int codeOffset, @Nullable CodeAreaSection section) {
        this.dataPosition = dataPosition;
        this.codeOffset = codeOffset;
        this.section = section == null ? CodeAreaSection.CODE_MATRIX : section;
    }

    @Override
    public long getDataPosition() {
        return dataPosition;
    }

    public void setDataPosition(long dataPosition) {
        this.dataPosition = dataPosition;
    }

    @Override
    public int getCodeOffset() {
        return codeOffset;
    }

    public void setCodeOffset(int codeOffset) {
        this.codeOffset = codeOffset;
    }

    @Nonnull
    @Override
    public CodeAreaSection getSection() {
        return section;
    }

    public void setSection(@Nonnull CodeAreaSection section) {
        Objects.requireNonNull(section, "section cannot be null");

        this.section = section;
    }

    /**
     * Sets caret position according to given position.
     *
     * @param position source position
     */
    public void setPosition(@Nonnull CaretPosition position) {
        dataPosition = position.getDataPosition();
        codeOffset = position.getCodeOffset();
        section = position.getSection();
    }

    public void clear() {
        this.dataPosition = 0;
        this.codeOffset = 0;
        this.section = section == null ? CodeAreaSection.CODE_MATRIX : section;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataPosition, codeOffset, section);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CodeAreaCaretPosition other = (CodeAreaCaretPosition) obj;
        if (this.dataPosition != other.dataPosition) {
            return false;
        }
        if (this.codeOffset != other.codeOffset) {
            return false;
        }
        return this.section == other.section;
    }
}
