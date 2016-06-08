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
 * Hexadecimal editor component utilities.
 *
 * @version 0.1.0 2016/06/08
 * @author ExBin Project (http://exbin.org)
 */
public class HexadecimalUtils {

    public static final char[] UPPER_HEX_CODES = "0123456789ABCDEF".toCharArray();
    public static final char[] LOWER_HEX_CODES = "0123456789abcdef".toCharArray();

    /**
     * Converts byte value to sequence of hexadecimal characters.
     *
     * @param value byte value
     * @return sequence of two hexadecimal chars with upper case
     */
    public static char[] byteToHexChars(byte value) {
        char[] result = new char[2];
        byteToHexChars(result, value);
        return result;
    }

    /**
     * Converts byte value to sequence of two hexadecimal characters.
     *
     * @param target target char array
     * @param value byte value
     */
    public static void byteToHexChars(char[] target, byte value) {
        target[0] = UPPER_HEX_CODES[(value >> 4) & 15];
        target[1] = UPPER_HEX_CODES[value & 15];
    }

    /**
     * Converts long value to sequence of hexadecimal character. No range
     * checking.
     *
     * @param value long value
     * @param length length of the target sequence
     * @return hexadecimal characters
     */
    public static char[] longToHexChars(long value, int length) {
        char[] result = new char[length];
        longToHexChars(result, value, length);
        return result;
    }

    /**
     * Converts long value to sequence of hexadecimal character. No range
     * checking.
     *
     * @param target target char array
     * @param value long value
     * @param length length of the target sequence
     */
    public static void longToHexChars(char[] target, long value, int length) {
        for (int i = length - 1; i >= 0; i--) {
            target[i] = UPPER_HEX_CODES[(int) (value & 15)];
            value = value >> 4;
        }
    }
}
