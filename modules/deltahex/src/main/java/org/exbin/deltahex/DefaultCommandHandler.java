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
import static org.exbin.deltahex.Hexadecimal.NO_MODIFIER;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.1.0 2016/05/24
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCommandHandler implements HexadecimalCommandHandler {

    private final Hexadecimal hexadecimal;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;

    public DefaultCommandHandler(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (java.awt.HeadlessException ex) {
            // Create clipboard if system one not available
            clipboard = new Clipboard("test");
        }
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
        canPaste = clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
    }

    @Override
    public void caretMoved() {
        // Do nothing
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
                    deleteSelection();
                }

                int value;
                if (keyValue >= '0' && keyValue <= '9') {
                    value = keyValue - '0';
                } else {
                    value = Character.toLowerCase(keyValue) - 'a' + 10;
                }

                BinaryData data = hexadecimal.getData();
                long dataPosition = hexadecimal.getDataPosition();
                if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                    if (dataPosition == hexadecimal.getData().getDataSize()) {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setHalfByte(value);
                } else {
                    if (hexadecimal.isLowerHalf()) {
                        byte lowerHalf = (byte) (data.getByte(dataPosition) & 0xf);
                        if (lowerHalf > 0) {
                            ((EditableBinaryData) data).insert(dataPosition + 1, 1);
                            ((EditableBinaryData) data).setByte(dataPosition + 1, lowerHalf);
                        }
                    } else {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setHalfByte(value);
                }
                hexadecimal.moveRight(Hexadecimal.NO_MODIFIER);
                hexadecimal.revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && hexadecimal.isValidChar(keyValue)) {
                BinaryData data = hexadecimal.getData();
                CaretPosition caretPosition = hexadecimal.getCaretPosition();
                long dataPosition = caretPosition.getDataPosition();
                byte[] bytes = hexadecimal.charToBytes(keyChar);
                if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                    if (dataPosition < hexadecimal.getData().getDataSize()) {
                        int length = bytes.length;
                        if (dataPosition + length > hexadecimal.getData().getDataSize()) {
                            length = (int) (hexadecimal.getData().getDataSize() - dataPosition);
                        }
                        ((EditableBinaryData) data).remove(dataPosition, length);
                    }
                }
                ((EditableBinaryData) data).insert(dataPosition, bytes);
                hexadecimal.getCaret().setCaretPosition(dataPosition + bytes.length - 1);
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
        BinaryData data = hexadecimal.getData();
        byte byteValue = data.getByte(dataPosition);

        if (lowerHalf) {
            byteValue = (byte) ((byteValue & 0xf0) | value);
        } else {
            byteValue = (byte) ((byteValue & 0xf) | (value << 4));
        }

        ((EditableBinaryData) data).setByte(dataPosition, byteValue);
    }

    @Override
    public void backSpacePressed() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        if (hexadecimal.hasSelection()) {
            deleteSelection();
        } else {
            HexadecimalCaret caret = hexadecimal.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= hexadecimal.getData().getDataSize()) {
                ((EditableBinaryData) hexadecimal.getData()).remove(dataPosition - 1, 1);
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
        if (!hexadecimal.isEditable()) {
            return;
        }

        if (hexadecimal.hasSelection()) {
            deleteSelection();
        } else {
            HexadecimalCaret caret = hexadecimal.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < hexadecimal.getData().getDataSize()) {
                ((EditableBinaryData) hexadecimal.getData()).remove(dataPosition, 1);
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
        ((EditableBinaryData) hexadecimal.getData()).remove(first, last - first + 1);
        hexadecimal.clearSelection();
        HexadecimalCaret caret = hexadecimal.getCaret();
        caret.setCaretPosition(first);
        hexadecimal.revealCursor();
        hexadecimal.computeDimensions();
        hexadecimal.updateScrollBars();
    }

    @Override
    public void delete() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        deleteSelection();
    }

    @Override
    public void copy() {
        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) hexadecimal.getData()).copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy);
            clipboard.setContents(binaryData, binaryData);
        }
    }

    @Override
    public void cut() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        if (selection != null) {
            copy();
            deleteSelection();
        }
    }

    @Override
    public void paste() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
            if (hexadecimal.hasSelection()) {
                deleteSelection();
            }

            try {
                Object object = clipboard.getData(binaryDataFlavor);
                if (object instanceof BinaryData) {
                    HexadecimalCaret caret = hexadecimal.getCaret();
                    long dataPosition = caret.getDataPosition();

                    BinaryData data = (BinaryData) object;
                    long dataSize = data.getDataSize();
                    if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                        long toRemove = dataSize;
                        if (dataPosition + toRemove > hexadecimal.getData().getDataSize()) {
                            toRemove = hexadecimal.getData().getDataSize() - dataPosition;
                        }
                        ((EditableBinaryData) hexadecimal.getData()).remove(dataPosition, toRemove);
                    }
                    ((EditableBinaryData) hexadecimal.getData()).insert(hexadecimal.getDataPosition(), data);

                    caret.setCaretPosition(caret.getDataPosition() + dataSize);
                    caret.setLowerHalf(false);
                    hexadecimal.computeDimensions();
                    hexadecimal.updateScrollBars();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
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
                        ((EditableBinaryData) hexadecimal.getData()).remove(dataPosition, toRemove);
                    }
                    ((EditableBinaryData) hexadecimal.getData()).insert(hexadecimal.getDataPosition(), bytes);

                    caret.setCaretPosition(caret.getDataPosition() + length);
                    caret.setLowerHalf(false);
                    hexadecimal.computeDimensions();
                    hexadecimal.updateScrollBars();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean canPaste() {
        return canPaste;
    }

    public class BinaryDataClipboardData implements Transferable, ClipboardOwner {

        private final BinaryData data;

        public BinaryDataClipboardData(BinaryData data) {
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
