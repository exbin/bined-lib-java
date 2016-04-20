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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.dhex.deltahex.CaretPosition;
import org.exbin.dhex.deltahex.EditableHexadecimalData;
import org.exbin.dhex.deltahex.HexadecimalCommandHandler;
import org.exbin.dhex.deltahex.HexadecimalData;
import static org.exbin.dhex.deltahex.component.Hexadecimal.NO_MODIFIER;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.1.0 2016/04/20
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCommandHandler implements HexadecimalCommandHandler {

    private final Hexadecimal hexadecimal;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;

    public DefaultCommandHandler(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.addFlavorListener(new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                canPaste = clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
            }
        });

        try {
            binaryDataFlavor = new DataFlavor("application/octet-stream");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void caretMoved() {
        // Do nothing
    }

    @Override
    public void keyPressed(char keyValue) {
        if (hexadecimal.getActiveSection() == Hexadecimal.Section.HEXADECIMAL) {
            if ((keyValue >= '0' && keyValue <= '9')
                    || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F')) {
                if (hexadecimal.hasSelection()) {
                    deleteSelection();
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

    @Override
    public void backSpacePressed() {
        if (hexadecimal.hasSelection()) {
            deleteSelection();
        } else {
            HexadecimalCaret caret = hexadecimal.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= hexadecimal.getData().getDataSize()) {
                ((EditableHexadecimalData) hexadecimal.getData()).remove(dataPosition - 1, 1);
                caret.setLowerHalf(false);
                hexadecimal.moveLeft(NO_MODIFIER);
                caret.setLowerHalf(false);
                hexadecimal.revealCursor();
                hexadecimal.repaint();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (hexadecimal.hasSelection()) {
            deleteSelection();
        } else {
            HexadecimalCaret caret = hexadecimal.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < hexadecimal.getData().getDataSize()) {
                ((EditableHexadecimalData) hexadecimal.getData()).remove(dataPosition, 1);
                if (caret.isLowerHalf()) {
                    caret.setLowerHalf(false);
                }
                hexadecimal.repaint();
            }
        }
    }

    private void deleteSelection() {
        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableHexadecimalData) hexadecimal.getData()).remove(first, last - first + 1);
        hexadecimal.clearSelection();
        HexadecimalCaret caret = hexadecimal.getCaret();
        caret.setCaretPosition(first);
        hexadecimal.revealCursor();
        hexadecimal.computeDimensions();
        hexadecimal.updateScrollBars();
    }

    @Override
    public void delete() {
        deleteSelection();
    }

    @Override
    public void copy() {
        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            HexadecimalData copy = ((EditableHexadecimalData) hexadecimal.getData()).copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy);
            clipboard.setContents(binaryData, binaryData);
        }
    }

    @Override
    public void cut() {
        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        if (selection != null) {
            copy();
            deleteSelection();
        }
    }

    @Override
    public void paste() {
//        DataFlavor[] availableDataFlavors = clipboard.getAvailableDataFlavors();
//        if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
//            if (hexadecimal.hasSelection()) {
//                deleteSelection();
//            }
//
//            try {
//                Object object = clipboard.getData(binaryDataFlavor);
//
//                hexadecimal.computeDimensions();
//                hexadecimal.updateScrollBars();
//            } catch (UnsupportedFlavorException ex) {
//                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } else
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            if (hexadecimal.hasSelection()) {
                deleteSelection();
            }

            Object insertedData;
            try {
                insertedData = clipboard.getData(DataFlavor.stringFlavor);
                if (insertedData instanceof String) {
                    HexadecimalCaret caret = hexadecimal.getCaret();
                    long dataPosition = caret.getDataPosition();

                    byte[] bytes = ((String) insertedData).getBytes(Charset.forName("UTF-8"));
                    int length = bytes.length;
                    if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                        long toRemove = length;
                        if (dataPosition + toRemove > hexadecimal.getData().getDataSize()) {
                            toRemove = hexadecimal.getData().getDataSize() - dataPosition;
                        }
                        ((EditableHexadecimalData) hexadecimal.getData()).remove(dataPosition, toRemove);
                    }
                    ((EditableHexadecimalData) hexadecimal.getData()).insert(hexadecimal.getDataPosition(), bytes);

                    caret.setCaretPosition(caret.getDataPosition() + length);
                    caret.setLowerHalf(false);
                    hexadecimal.computeDimensions();
                    hexadecimal.updateScrollBars();
                }
            } catch (UnsupportedFlavorException ex) {
                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean canPaste() {
        return canPaste;
    }

    public class BinaryDataClipboardData implements Transferable, ClipboardOwner {

        private HexadecimalData data;

        public BinaryDataClipboardData(HexadecimalData data) {
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{binaryDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binaryDataFlavor)) {
                return data;
            } else {
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                data.saveToStream(byteArrayStream);
                return byteArrayStream.toString("UTF-8");
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }
    }
}
