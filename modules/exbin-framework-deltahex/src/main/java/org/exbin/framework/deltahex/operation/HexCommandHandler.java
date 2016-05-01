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
package org.exbin.framework.deltahex.operation;

import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.EditableHexadecimalData;
import org.exbin.deltahex.HexadecimalCommandHandler;
import org.exbin.deltahex.HexadecimalData;
import org.exbin.deltahex.component.Hexadecimal;
import org.exbin.deltahex.component.HexadecimalCaret;
import org.exbin.xbup.operation.Command;
import org.exbin.xbup.operation.undo.XBUndoHandler;

/**
 * Command handler for undo/redo aware hexadecimal editor editing.
 *
 * @version 0.1.0 2016/05/01
 * @author ExBin Project (http://exbin.org)
 */
public class HexCommandHandler implements HexadecimalCommandHandler {

    private final Hexadecimal hexadecimal;
    private final XBUndoHandler undoHandler;
    private Command continousEditing = null;

    public HexCommandHandler(Hexadecimal hexadecimal, XBUndoHandler undoHandler) {
        this.hexadecimal = hexadecimal;
        this.undoHandler = undoHandler;
    }

    @Override
    public void caretMoved() {
        continousEditing = null;
    }

    @Override
    public void keyPressed(char keyValue) {
        if (!hexadecimal.isEditable()) {
            return;
        }

        if (hexadecimal.getActiveSection() == HexadecimalCaret.Section.HEXADECIMAL) {
            if ((keyValue >= '0' && keyValue <= '9')
                    || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F')) {
                if (hexadecimal.hasSelection()) {
                    // deleteSelection();
                }

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

//                if (continousEditing != null && continousEditing.getEditationMode() == hexadecimal.getEditationMode()) {
//                    continousEditing.addKey(value);
//                } else {
//                    continousEditing = new CharWritingCommand(hexadecimal, hexadecimal.getEditationMode());
//                    undoHandler.addCommand(continousEditing);
//                    continousEditing.addKey(value);
//                }
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && keyChar < 255) {
                HexadecimalData data = hexadecimal.getData();
                CaretPosition caretPosition = hexadecimal.getCaretPosition();
                long dataPosition = caretPosition.getDataPosition();
                if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                    if (dataPosition == hexadecimal.getData().getDataSize()) {
                        ((EditableHexadecimalData) data).insert(dataPosition, 1);
                    }
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

    @Override
    public void backSpacePressed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deletePressed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cut() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void paste() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canPaste() {
        return false;
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
}
