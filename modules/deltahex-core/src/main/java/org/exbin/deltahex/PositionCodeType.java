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

/**
 * Enumeration of supported position code types.
 *
 * @version 0.2.0 2017/04/09
 * @author ExBin Project (http://exbin.org)
 */
public enum PositionCodeType {

    /**
     * Represent code as number in base 8.
     *
     * Code is represented as characters of range 0 to 7.
     */
    OCTAL(8),
    /**
     * Represent code as number in base 10.
     *
     * Code is represented as characters of range 0 to 9.
     */
    DECIMAL(10),
    /**
     * Represent code as number in base 16.
     *
     * Code is represented as characters of range 0 to 9 and A to F.
     */
    HEXADECIMAL(16);

    private final int base;
    private final double baseLog;

    private PositionCodeType(int base) {
        this.base = base;
        baseLog = Math.log(base);
    }

    /**
     * Returns numerical base.
     *
     * @return numerical base
     */
    public int getBase() {
        return base;
    }

    /**
     * Returns natural logarithm of the base.
     *
     * @return natural logarithm of the base
     */
    public double getBaseLog() {
        return baseLog;
    }
}
