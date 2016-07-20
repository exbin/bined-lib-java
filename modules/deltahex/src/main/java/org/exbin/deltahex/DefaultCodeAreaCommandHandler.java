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
import org.exbin.deltahex.CodeArea.Section;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.1.0 2016/07/12
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    private final CodeArea codeArea;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;

    public DefaultCodeAreaCommandHandler(CodeArea codeArea) {
        this.codeArea = codeArea;
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (java.awt.HeadlessException ex) {
            // Create clipboard if system one not available
            clipboard = new Clipboard("clipboard");
        }
        clipboard.addFlavorListener(new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                updateCanPaste();
            }
        });
        try {
            binaryDataFlavor = new DataFlavor("application/octet-stream");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateCanPaste();
    }

    private void updateCanPaste() {
        try {
            canPaste = clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (java.lang.IllegalStateException ex) {
            canPaste = false;
        }
    }

    @Override
    public void caretMoved() {
        // Do nothing
    }

    @Override
    public void keyPressed(char keyValue) {
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.getActiveSection() == Section.CODE_MATRIX) {
            long dataPosition = codeArea.getDataPosition();
            int codeOffset = codeArea.getCodeOffset();
            CodeArea.CodeType codeType = codeArea.getCodeType();
            boolean validKey = false;
            switch (codeType) {
                case BINARY: {
                    validKey = keyValue >= '0' && keyValue <= '1';
                    break;
                }
                case DECIMAL: {
                    validKey = codeOffset == 0
                            ? keyValue >= '0' && keyValue <= '2'
                            : keyValue >= '0' && keyValue <= '9';
                    break;
                }
                case OCTAL: {
                    validKey = codeOffset == 0
                            ? keyValue >= '0' && keyValue <= '3'
                            : keyValue >= '0' && keyValue <= '7';
                    break;
                }
                case HEXADECIMAL: {
                    validKey = (keyValue >= '0' && keyValue <= '9')
                            || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F');
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected code type " + codeType.name());
            }
            if (validKey) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                }

                int value;
                if (keyValue >= '0' && keyValue <= '9') {
                    value = keyValue - '0';
                } else {
                    value = Character.toLowerCase(keyValue) - 'a' + 10;
                }

                BinaryData data = codeArea.getData();
                if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                    if (dataPosition == codeArea.getData().getDataSize()) {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setCodeValue(value);
                } else {
                    if (codeOffset > 0) {
                        byte byteRest = data.getByte(dataPosition);
                        switch (codeType) {
                            case BINARY: {
                                byteRest = (byte) (byteRest & (0xff >> codeOffset));
                                break;
                            }
                            case DECIMAL: {
                                byteRest = (byte) (byteRest % (codeOffset == 1 ? 100 : 10));
                                break;
                            }
                            case OCTAL: {
                                byteRest = (byte) (byteRest % (codeOffset == 1 ? 64 : 8));
                                break;
                            }
                            case HEXADECIMAL: {
                                byteRest = (byte) (byteRest & 0xf);
                                break;
                            }
                            default:
                                throw new IllegalStateException("Unexpected code type " + codeType.name());
                        }
                        if (byteRest > 0) {
                            ((EditableBinaryData) data).insert(dataPosition + 1, 1);
                            ((EditableBinaryData) data).setByte(dataPosition, (byte) (data.getByte(dataPosition) - byteRest));
                            ((EditableBinaryData) data).setByte(dataPosition + 1, byteRest);
                        }
                    } else {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setCodeValue(value);
                }
                codeArea.notifyDataChanged();
                codeArea.moveRight(CodeArea.NO_MODIFIER);
                codeArea.revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && codeArea.isValidChar(keyValue)) {
                BinaryData data = codeArea.getData();
                CaretPosition caretPosition = codeArea.getCaretPosition();
                long dataPosition = caretPosition.getDataPosition();
                byte[] bytes = codeArea.charToBytes(keyChar);
                if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                    if (dataPosition < codeArea.getData().getDataSize()) {
                        int length = bytes.length;
                        if (dataPosition + length > codeArea.getData().getDataSize()) {
                            length = (int) (codeArea.getData().getDataSize() - dataPosition);
                        }
                        ((EditableBinaryData) data).remove(dataPosition, length);
                    }
                }
                ((EditableBinaryData) data).insert(dataPosition, bytes);
                codeArea.notifyDataChanged();
                codeArea.getCaret().setCaretPosition(dataPosition + bytes.length - 1);
                codeArea.moveRight(CodeArea.NO_MODIFIER);
                codeArea.revealCursor();
            }
        }
    }

    private void setCodeValue(int value) {
        long dataPosition = codeArea.getDataPosition();
        int codeOffset = codeArea.getCodeOffset();
        setCodeValue(dataPosition, value, codeOffset);
    }

    private void setCodeValue(long dataPosition, int value, int codeOffset) {
        CodeArea.CodeType codeType = codeArea.getCodeType();
        BinaryData data = codeArea.getData();

        byte byteValue = data.getByte(dataPosition);
        switch (codeType) {
            case BINARY: {
                int bitMask = 0x80 >> codeOffset;
                byteValue = (byte) (byteValue & (0xff - bitMask) | (value << (7 - codeOffset)));
                break;
            }
            case DECIMAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 100) + value * 100;
                        if (newValue > 255) {
                            newValue = 200;
                        }
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 100) * 100 + value * 10 + (newValue % 10);
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 10) * 10 + value;
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case OCTAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 64) + value * 64;
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 64) * 64 + value * 8 + (newValue % 8);
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 8) * 8 + value;
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case HEXADECIMAL: {
                if (codeOffset == 1) {
                    byteValue = (byte) ((byteValue & 0xf0) | value);
                } else {
                    byteValue = (byte) ((byteValue & 0xf) | (value << 4));
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type " + codeType.name());
        }

        ((EditableBinaryData) data).setByte(dataPosition, byteValue);
    }

    @Override
    public void backSpacePressed() {
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            CodeAreaCaret caret = codeArea.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getData().getDataSize()) {
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                caret.setCodeOffset(0);
                codeArea.moveLeft(CodeArea.NO_MODIFIER);
                caret.setCodeOffset(0);
                codeArea.revealCursor();
                codeArea.computePaintData();
                codeArea.updateScrollBars();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            CodeAreaCaret caret = codeArea.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getData().getDataSize()) {
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                codeArea.computePaintData();
                codeArea.updateScrollBars();
            }
        }
    }

    private void deleteSelection() {
        CodeArea.SelectionRange selection = codeArea.getSelection();
        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) codeArea.getData()).remove(first, last - first + 1);
        codeArea.clearSelection();
        CodeAreaCaret caret = codeArea.getCaret();
        caret.setCaretPosition(first);
        codeArea.revealCursor();
        codeArea.computePaintData();
        codeArea.updateScrollBars();
    }

    @Override
    public void delete() {
        if (!codeArea.isEditable()) {
            return;
        }

        deleteSelection();
        codeArea.notifyDataChanged();
    }

    @Override
    public void copy() {
        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy);
            try {
                clipboard.setContents(binaryData, binaryData);
            } catch (java.lang.IllegalStateException ex) {
                // Cannot copy
            }
        }
    }

    @Override
    public void cut() {
        if (!codeArea.isEditable()) {
            return;
        }

        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            copy();
            deleteSelection();
            codeArea.notifyDataChanged();
        }
    }

    @Override
    public void paste() {
        if (!codeArea.isEditable()) {
            return;
        }

        try {
            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                try {
                    Object object = clipboard.getData(binaryDataFlavor);
                    if (object instanceof BinaryData) {
                        CodeAreaCaret caret = codeArea.getCaret();
                        long dataPosition = caret.getDataPosition();

                        BinaryData data = (BinaryData) object;
                        long dataSize = data.getDataSize();
                        if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                            long toRemove = dataSize;
                            if (dataPosition + toRemove > codeArea.getData().getDataSize()) {
                                toRemove = codeArea.getData().getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getData()).insert(codeArea.getDataPosition(), data);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + dataSize);
                        caret.setCodeOffset(0);
                        codeArea.computePaintData();
                        codeArea.updateScrollBars();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                Object insertedData;
                try {
                    insertedData = clipboard.getData(DataFlavor.stringFlavor);
                    if (insertedData instanceof String) {
                        CodeAreaCaret caret = codeArea.getCaret();
                        long dataPosition = caret.getDataPosition();

                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName("UTF-8"));
                        int length = bytes.length;
                        if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                            long toRemove = length;
                            if (dataPosition + toRemove > codeArea.getData().getDataSize()) {
                                toRemove = codeArea.getData().getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getData()).insert(codeArea.getDataPosition(), bytes);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + length);
                        caret.setCodeOffset(0);
                        codeArea.computePaintData();
                        codeArea.updateScrollBars();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (java.lang.IllegalStateException ex) {
            // ignore
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
