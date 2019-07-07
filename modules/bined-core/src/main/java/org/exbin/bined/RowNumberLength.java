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
package org.exbin.bined;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Row number length.
 *
 * @version 0.2.0 2019/03/15
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class RowNumberLength {

    @Nonnull
    private RowNumberType rowNumberType = RowNumberType.SPECIFIED;
    private int rowNumberLength = 8;

    public RowNumberLength() {
    }

    @Nonnull
    public RowNumberType getRowNumberType() {
        return rowNumberType;
    }

    public void setRowNumberType(RowNumberType rowNumberType) {
        if (rowNumberType == null) {
            throw new NullPointerException();
        }

        this.rowNumberType = rowNumberType;
    }

    public int getRowNumberLength() {
        return rowNumberLength;
    }

    public void setRowNumberLength(int rowNumberLength) {
        this.rowNumberLength = rowNumberLength;
    }

    /**
     * Enumeration of row number types.
     */
    public static enum RowNumberType {
        /**
         * Row number length is computed from data size and position code type.
         */
        AUTO,
        /**
         * Row number length is specified as fixed number of figures.
         */
        SPECIFIED
    }
}
