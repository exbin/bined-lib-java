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
package org.exbin.dhex.deltahex.component;

import org.exbin.dhex.deltahex.CaretPosition;
import org.exbin.dhex.deltahex.EditableHexadecimalData;
import org.exbin.dhex.deltahex.HexadecimalCommandHandler;
import org.exbin.dhex.deltahex.HexadecimalData;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.1.0 2016/04/17
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCommandHandler implements HexadecimalCommandHandler {

    private final Hexadecimal hexadecimal;

    public DefaultCommandHandler(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
    }

    @Override
    public void caretMoved() {
        // Do nothing
    }

    @Override
    public void keyPressed(char keyValue) {
        if (hexadecimal.hasSelection()) {
            deleteSelection();
        }

        if (hexadecimal.getActiveSection() == Hexadecimal.Section.HEXADECIMAL) {
            if ((keyValue >= '0' && keyValue <= '9')
                    || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F')) {
                int value;
                if (keyValue >= '0' && keyValue <= '9') {
                    value = keyValue - '0';
                } else {
                    value = Character.toLowerCase(keyValue) - 'a' + 10;
                }

                HexadecimalData data = hexadecimal.getData();
                long dataPosition = hexadecimal.getDataPosition();
                if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                    if (dataPosition == hexadecimal.getData().getDataSize()) {
                        ((EditableHexadecimalData) data).insert(dataPosition, 1);
                    }
                    setHalfByte(value);
                } else {
                    if (hexadecimal.isLowerHalf()) {
                        byte lowerHalf = (byte) (data.getByte(dataPosition) & 0xf);
                        if (lowerHalf > 0) {
                            ((EditableHexadecimalData) data).insert(dataPosition + 1, 1);
                            ((EditableHexadecimalData) data).setByte(dataPosition + 1, lowerHalf);
                        }
                    } else {
                        ((EditableHexadecimalData) data).insert(dataPosition, 1);
                    }
                    setHalfByte(value);
                }
                hexadecimal.moveRight(Hexadecimal.NO_MODIFIER);
                hexadecimal.revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && keyChar < 255) {
                HexadecimalData data = hexadecimal.getData();
                CaretPosition caretPosition = hexadecimal.getCaretPosition();
                long dataPosition = caretPosition.getDataPosition();
                if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                    ((EditableHexadecimalData) data).setByte(dataPosition, (byte) keyChar);
                } else {
                    ((EditableHexadecimalData) data).insert(dataPosition, 1);
                    ((EditableHexadecimalData) data).setByte(dataPosition, (byte) keyChar);
                }
                hexadecimal.moveRight(Hexadecimal.NO_MODIFIER);
                hexadecimal.revealCursor();
            }
        }
    }

    private void setHalfByte(int value) {
        CaretPosition caretPosition = hexadecimal.getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        setHalfByte(dataPosition, value, caretPosition.isLowerHalf());
    }

    private void setHalfByte(long dataPosition, int value, boolean lowerHalf) {
        HexadecimalData data = hexadecimal.getData();
        byte byteValue = data.getByte(dataPosition);

        if (lowerHalf) {
            byteValue = (byte) ((byteValue & 0xf0) | value);
        } else {
            byteValue = (byte) ((byteValue & 0xf) | (value << 4));
        }

        ((EditableHexadecimalData) data).setByte(dataPosition, byteValue);
    }
    
    private void deleteSelection() {
        
    }
}
