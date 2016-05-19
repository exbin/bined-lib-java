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
package org.exbin.deltahex.data;

/**
 * Hexadecimal editor component utilities.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class HexadecimalUtils {

    /**
     * Converts byte value to sequence of hexadecimal characters.
     *
     * @param value byte value
     * @return sequence of two hexadecimal chars with upper case
     */
    public static char[] byteToHexChars(byte value) {
        int firstChar = (value >> 4) & 15;
        int secondChar = value & 15;
        char[] result = new char[2];
        result[0] = intToHexChar(firstChar);
        result[1] = intToHexChar(secondChar);
        return result;
    }

    /**
     * Converts integer value in range 0 to 15 to single hexadecimal character.
     * No range checking.
     *
     * @param value integer value
     * @return character
     */
    public static char intToHexChar(int value) {
        if (value < 10) {
            return (char) (48 + value);
        } else {
            return (char) (55 + value);
        }
    }

    /**
     * Converts long value to sequence of 8 hexadecimal character. No range
     * checking.
     *
     * @param value long value
     * @return 8 hexadecimal characters
     */
    public static char[] longToHexChars(long value) {
        char[] result = new char[8];
        for (int i = 0; i < 8; i++) {
            int charValue = (int) (value & 15);
            if (charValue < 10) {
                result[7 - i] = (char) (48 + charValue);
            } else {
                result[7 - i] = (char) (55 + charValue);
            }

            value = value >> 4;
        }

        return result;
    }
}
