/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.swt.extended;

import org.exbin.bined.swt.basic.*;
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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.swt.events.KeyEvent;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.swt.CodeArea;
import org.exbin.bined.swt.CodeAreaCommandHandler;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.capability.CaretCapable;

/**
 * Default binary editor command handler.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    private static final int CODE_BUFFER_LENGTH = 16;

    private final int metaMask;

    private final CodeArea codeArea;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;

    public DefaultCodeAreaCommandHandler(CodeArea codeArea) {
        this.codeArea = codeArea;

        int metaMaskInit;
        try {
            metaMaskInit = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMaskInit = java.awt.Event.CTRL_MASK;
        }
        this.metaMask = metaMaskInit;

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
    public void keyPressed(KeyEvent keyEvent) {
//        if (!codeArea.isEnabled()) {
//            return;
//        }
//
//        switch (keyEvent.keyCode) {
//            case KeyEvent.VK_LEFT: {
//                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
//                    // Scroll position offset instead of cursor
//                    CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getLineByteShift() < codeArea.getBytesPerLine() - 1) {
//                        scrollPosition.setLineByteShift(scrollPosition.getLineByteShift() + 1);
//                    } else {
//                        if (scrollPosition.getScrollLinePosition() > 0) {
//                            scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - 1);
//                        }
//                        scrollPosition.setLineByteShift(0);
//                    }
//
//                    codeArea.getCaret().resetBlink();
//                    codeArea.computePaintData();
//                    codeArea.notifyScrolled();
//                    codeArea.repaint();
//                } else {
//                    codeArea.moveLeft(keyEvent.getModifiersEx());
//                    caretMoved();
//                    codeArea.revealCursor();
//                }
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_RIGHT: {
//                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
//                    // Scroll position offset instead of cursor
//                    CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getLineByteShift() > 0) {
//                        scrollPosition.setLineByteShift(scrollPosition.getLineByteShift() - 1);
//                    } else {
//                        long dataSize = codeArea.getDataSize();
//                        if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine()) {
//                            scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
//                        }
//                        scrollPosition.setLineByteShift(codeArea.getBytesPerLine() - 1);
//                    }
//
//                    codeArea.getCaret().resetBlink();
//                    codeArea.computePaintData();
//                    codeArea.notifyScrolled();
//                    codeArea.repaint();
//                } else {
//                    codeArea.moveRight(keyEvent.getModifiersEx());
//                    caretMoved();
//                    codeArea.revealCursor();
//                }
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_UP: {
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesPerLine = codeArea.getBytesPerLine();
//                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
//                    // Scroll page instead of cursor
//                    CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getScrollLinePosition() > 0) {
//                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - 1);
//                        codeArea.updateScrollBars();
//                        codeArea.notifyScrolled();
//                    }
//                } else {
//                    if (caretPosition.getDataPosition() > 0) {
//                        if (caretPosition.getDataPosition() >= bytesPerLine) {
//                            codeArea.setCaretPosition(caretPosition.getDataPosition() - bytesPerLine, caretPosition.getCodeOffset());
//                            codeArea.notifyCaretMoved();
//                        }
//                        codeArea.updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                    }
//                    caretMoved();
//                    codeArea.revealCursor();
//                }
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_DOWN: {
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesPerLine = codeArea.getBytesPerLine();
//                long dataSize = codeArea.getDataSize();
//                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
//                    // Scroll page instead of cursor
//                    CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine()) {
//                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
//                        codeArea.updateScrollBars();
//                        codeArea.notifyScrolled();
//                    }
//                } else {
//                    if (caretPosition.getDataPosition() < dataSize) {
//                        if (caretPosition.getDataPosition() + bytesPerLine < dataSize
//                                || (caretPosition.getDataPosition() + bytesPerLine == dataSize && caretPosition.getCodeOffset() == 0)) {
//                            codeArea.setCaretPosition(caretPosition.getDataPosition() + bytesPerLine, caretPosition.getCodeOffset());
//                            codeArea.notifyCaretMoved();
//                        }
//                        codeArea.updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                    }
//                    caretMoved();
//                    codeArea.revealCursor();
//                }
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_HOME: {
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesPerLine = codeArea.getBytesPerLine();
//                CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                if (caretPosition.getDataPosition() > 0 || caretPosition.getCodeOffset() != 0) {
//                    long targetPosition;
//                    if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
//                        targetPosition = 0;
//                    } else {
//                        targetPosition = ((caretPosition.getDataPosition() + scrollPosition.getLineByteShift()) / bytesPerLine) * bytesPerLine - scrollPosition.getLineByteShift();
//                        if (targetPosition < 0) {
//                            targetPosition = 0;
//                        }
//                    }
//                    codeArea.setCaretPosition(targetPosition);
//                    caretMoved();
//                    codeArea.notifyCaretMoved();
//                    codeArea.updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
//                codeArea.revealCursor();
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_END: {
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesPerLine = codeArea.getBytesPerLine();
//                CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                long dataSize = codeArea.getDataSize();
//                if (caretPosition.getDataPosition() < dataSize) {
//                    if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
//                        codeArea.setCaretPosition(codeArea.getDataSize());
//                    } else if (codeArea.getActiveSection() == Section.CODE_MATRIX) {
//                        long newPosition = (((caretPosition.getDataPosition() + scrollPosition.getLineByteShift()) / bytesPerLine) + 1) * bytesPerLine - 1 - scrollPosition.getLineByteShift();
//                        codeArea.setCaretPosition(newPosition < dataSize ? newPosition : dataSize, newPosition < dataSize ? codeArea.getCodeType().getMaxDigits() - 1 : 0);
//                    } else {
//                        long newPosition = (((caretPosition.getDataPosition() + scrollPosition.getLineByteShift()) / bytesPerLine) + 1) * bytesPerLine - 1 - scrollPosition.getLineByteShift();
//                        codeArea.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
//                    }
//                    caretMoved();
//                    codeArea.updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
//                codeArea.revealCursor();
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_PAGE_UP: {
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRect();
//                CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                if (scrollPosition.getScrollLinePosition() > codeArea.getLinesPerRect()) {
//                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - codeArea.getLinesPerRect());
//                    codeArea.updateScrollBars();
//                    codeArea.notifyScrolled();
//                }
//                if (caretPosition.getDataPosition() > 0) {
//                    if (caretPosition.getDataPosition() >= bytesStep) {
//                        codeArea.setCaretPosition(caretPosition.getDataPosition() - bytesStep, caretPosition.getCodeOffset());
//                    } else if (caretPosition.getDataPosition() >= codeArea.getBytesPerLine()) {
//                        codeArea.setCaretPosition(caretPosition.getDataPosition() % codeArea.getBytesPerLine(), caretPosition.getCodeOffset());
//                    }
//                    caretMoved();
//                    codeArea.updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
//                codeArea.revealCursor();
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_PAGE_DOWN: {
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRect();
//                long dataSize = codeArea.getDataSize();
//                CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine() - codeArea.getLinesPerRect() * 2) {
//                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + codeArea.getLinesPerRect());
//                    codeArea.updateScrollBars();
//                    codeArea.notifyScrolled();
//                }
//                if (caretPosition.getDataPosition() < dataSize) {
//                    if (caretPosition.getDataPosition() + bytesStep < dataSize) {
//                        codeArea.setCaretPosition(caretPosition.getDataPosition() + bytesStep, caretPosition.getCodeOffset());
//                    } else if (caretPosition.getDataPosition() + codeArea.getBytesPerLine() <= dataSize) {
//                        long dataPosition = dataSize
//                                - dataSize % codeArea.getBytesPerLine()
//                                - ((caretPosition.getDataPosition() % codeArea.getBytesPerLine() <= dataSize % codeArea.getBytesPerLine()) ? 0 : codeArea.getBytesPerLine())
//                                + (caretPosition.getDataPosition() % codeArea.getBytesPerLine());
//                        codeArea.setCaretPosition(dataPosition, dataPosition == dataSize ? 0 : caretPosition.getCodeOffset());
//                    }
//                    caretMoved();
//                    codeArea.updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
//                codeArea.revealCursor();
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_INSERT: {
//                if (codeArea.getEditAllowed() == EditAllowed.ALLOWED) {
//                    codeArea.setEditMode(codeArea.getEditMode() == EditMode.INSERT ? EditMode.OVERWRITE : EditMode.INSERT);
//                }
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_TAB: {
//                if (codeArea.getViewMode() == ViewMode.DUAL) {
//                    Section activeSection = codeArea.getActiveSection() == Section.CODE_MATRIX ? Section.TEXT_PREVIEW : Section.CODE_MATRIX;
//                    if (activeSection == Section.TEXT_PREVIEW) {
//                        codeArea.getCaretPosition().setCodeOffset(0);
//                    }
//                    codeArea.setActiveSection(activeSection);
//                    codeArea.revealCursor();
//                    codeArea.repaint();
//                }
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_DELETE: {
//                deletePressed();
//                keyEvent.consume();
//                break;
//            }
//            case KeyEvent.VK_BACK_SPACE: {
//                backSpacePressed();
//                keyEvent.consume();
//                break;
//            }
//            default: {
//                if (codeArea.isHandleClipboard()) {
//                    if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_C) {
//                        copy();
//                        keyEvent.consume();
//                        break;
//                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_X) {
//                        cut();
//                        keyEvent.consume();
//                        break;
//                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_V) {
//                        paste();
//                        keyEvent.consume();
//                        break;
//                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_A) {
//                        codeArea.selectAll();
//                        keyEvent.consume();
//                        break;
//                    }
//                }
//            }
//        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
//        char keyValue = keyEvent.getKeyChar();
//        if (keyValue == 0xffff) {
//            return;
//        }
//        if (!codeArea.isEditable()) {
//            return;
//        }
//
//        if (codeArea.getActiveSection() == Section.CODE_MATRIX) {
//            long dataPosition = codeArea.getDataPosition();
//            int codeOffset = codeArea.getCodeOffset();
//            CodeType codeType = codeArea.getCodeType();
//            boolean validKey = false;
//            switch (codeType) {
//                case BINARY: {
//                    validKey = keyValue >= '0' && keyValue <= '1';
//                    break;
//                }
//                case DECIMAL: {
//                    validKey = codeOffset == 0
//                            ? keyValue >= '0' && keyValue <= '2'
//                            : keyValue >= '0' && keyValue <= '9';
//                    break;
//                }
//                case OCTAL: {
//                    validKey = codeOffset == 0
//                            ? keyValue >= '0' && keyValue <= '3'
//                            : keyValue >= '0' && keyValue <= '7';
//                    break;
//                }
//                case HEXADECIMAL: {
//                    validKey = (keyValue >= '0' && keyValue <= '9')
//                            || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F');
//                    break;
//                }
//                default:
//                    throw CodeAreaUtils.getInvalidTypeException(codeType);
//            }
//            if (validKey) {
//                if (codeArea.hasSelection()) {
//                    deleteSelection();
//                }
//
//                int value;
//                if (keyValue >= '0' && keyValue <= '9') {
//                    value = keyValue - '0';
//                } else {
//                    value = Character.toLowerCase(keyValue) - 'a' + 10;
//                }
//
//                BinaryData data = codeArea.getBinaryData();
//                if (codeArea.getEditMode() == EditMode.OVERWRITE) {
//                    if (dataPosition == codeArea.getDataSize()) {
//                        ((EditableBinaryData) data).insert(dataPosition, 1);
//                    }
//                    setCodeValue(value);
//                } else {
//                    if (codeOffset > 0) {
//                        byte byteRest = data.getByte(dataPosition);
//                        switch (codeType) {
//                            case BINARY: {
//                                byteRest = (byte) (byteRest & (0xff >> codeOffset));
//                                break;
//                            }
//                            case DECIMAL: {
//                                byteRest = (byte) (byteRest % (codeOffset == 1 ? 100 : 10));
//                                break;
//                            }
//                            case OCTAL: {
//                                byteRest = (byte) (byteRest % (codeOffset == 1 ? 64 : 8));
//                                break;
//                            }
//                            case HEXADECIMAL: {
//                                byteRest = (byte) (byteRest & 0xf);
//                                break;
//                            }
//                            default:
//                                throw CodeAreaUtils.getInvalidTypeException(codeType);
//                        }
//                        if (byteRest > 0) {
//                            ((EditableBinaryData) data).insert(dataPosition + 1, 1);
//                            ((EditableBinaryData) data).setByte(dataPosition, (byte) (data.getByte(dataPosition) - byteRest));
//                            ((EditableBinaryData) data).setByte(dataPosition + 1, byteRest);
//                        }
//                    } else {
//                        ((EditableBinaryData) data).insert(dataPosition, 1);
//                    }
//                    setCodeValue(value);
//                }
//                codeArea.notifyDataChanged();
//                codeArea.moveRight(CodeArea.NO_MODIFIER);
//                codeArea.revealCursor();
//            }
//        } else {
//            char keyChar = keyValue;
//            if (keyChar > 31 && codeArea.isValidChar(keyValue)) {
//                BinaryData data = codeArea.getBinaryData();
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                long dataPosition = caretPosition.getDataPosition();
//                byte[] bytes = codeArea.charToBytes(keyChar);
//                if (codeArea.getEditMode() == EditMode.OVERWRITE) {
//                    if (dataPosition < codeArea.getDataSize()) {
//                        int length = bytes.length;
//                        if (dataPosition + length > codeArea.getDataSize()) {
//                            length = (int) (codeArea.getDataSize() - dataPosition);
//                        }
//                        ((EditableBinaryData) data).remove(dataPosition, length);
//                    }
//                }
//                ((EditableBinaryData) data).insert(dataPosition, bytes);
//                codeArea.notifyDataChanged();
//                codeArea.getCaret().setCaretPosition(dataPosition + bytes.length - 1);
//                codeArea.moveRight(CodeArea.NO_MODIFIER);
//                codeArea.revealCursor();
//            }
//        }
    }

    private void setCodeValue(int value) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
        CodeType codeType = getCodeType();
        byte byteValue = data.getByte(dataPosition);
        byte outputValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) data).setByte(dataPosition, outputValue);
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
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                codeArea.moveLeft(CodeArea.NO_MODIFIER);
                caret.setCodeOffset(0);
                ((EditableBinaryData) codeArea.getBinaryData()).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                codeArea.revealCursor();
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
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) codeArea.getBinaryData()).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                codeArea.updateScrollBars();
            }
        }
    }

    private void deleteSelection() {
        SelectionRange selection = codeArea.getSelection();
        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) codeArea.getBinaryData()).remove(first, last - first + 1);
        codeArea.clearSelection();
        CodeAreaCaret caret = codeArea.getCaret();
        caret.setCaretPosition(first);
        codeArea.revealCursor();
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
        SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getBinaryData()).copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy);
            try {
                clipboard.setContents(binaryData, binaryData);
            } catch (java.lang.IllegalStateException ex) {
                // Cannot copy
            }
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getBinaryData()).copy(first, last - first + 1);

            CodeDataClipboardData binaryData = new CodeDataClipboardData(copy);
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

        SelectionRange selection = codeArea.getSelection();
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
                        if (codeArea.getEditMode() == EditMode.OVERWRITE) {
                            long toRemove = dataSize;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getBinaryData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getBinaryData()).insert(codeArea.getDataPosition(), data);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + dataSize);
                        caret.setCodeOffset(0);
                        codeArea.updateScrollBars();
                        codeArea.revealCursor();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

//                Object insertedData;
//                try {
//                    insertedData = clipboard.getBinaryData(DataFlavor.stringFlavor);
//                    if (insertedData instanceof String) {
//                        CodeAreaCaret caret = codeArea.getCaret();
//                        long dataPosition = caret.getDataPosition();
//
//                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName("UTF-8"));
//                        int length = bytes.length;
//                        if (codeArea.getEditMode() == EditMode.OVERWRITE) {
//                            long toRemove = length;
//                            if (dataPosition + toRemove > codeArea.getDataSize()) {
//                                toRemove = codeArea.getDataSize() - dataPosition;
//                            }
//                            ((EditableBinaryData) codeArea.getBinaryData()).remove(dataPosition, toRemove);
//                        }
//                        ((EditableBinaryData) codeArea.getBinaryData()).insert(codeArea.getDataPosition(), bytes);
//                        codeArea.notifyDataChanged();
//
//                        caret.setCaretPosition(caret.getDataPosition() + length);
//                        caret.setCodeOffset(0);
//                        codeArea.updateScrollBars();
//                        codeArea.revealCursor();
//                    }
//                } catch (UnsupportedFlavorException | IOException ex) {
//                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        } catch (java.lang.IllegalStateException ex) {
            // ignore
        }
    }

    @Override
    public void pasteFromCode() {
        if (!codeArea.isEditable()) {
            return;
        }

        if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
            paste();
        } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            if (codeArea.hasSelection()) {
                deleteSelection();
                codeArea.notifyDataChanged();
            }

//            Object insertedData;
//            try {
//                insertedData = clipboard.getBinaryData(DataFlavor.stringFlavor);
//                if (insertedData instanceof String) {
//                    CodeAreaCaret caret = codeArea.getCaret();
//                    long dataPosition = caret.getDataPosition();
//
//                    CodeType codeType = codeArea.getCodeType();
//                    String insertedString = (String) insertedData;
//                    ByteArrayEditableData data = new ByteArrayEditableData();
//                    CodeAreaUtils.insertHexStringIntoData(insertedString, clipData, codeType);
//
//                    long length = data.getDataSize();
//                    if (codeArea.getEditMode() == EditMode.OVERWRITE) {
//                        long toRemove = length;
//                        if (dataPosition + toRemove > codeArea.getDataSize()) {
//                            toRemove = codeArea.getDataSize() - dataPosition;
//                        }
//                        ((EditableBinaryData) codeArea.getBinaryData()).remove(dataPosition, toRemove);
//                    }
//                    ((EditableBinaryData) codeArea.getBinaryData()).insert(codeArea.getDataPosition(), data);
//                    codeArea.notifyDataChanged();
//
//                    caret.setCaretPosition(caret.getDataPosition() + length);
//                    caret.setCodeOffset(0);
//                    codeArea.updateScrollBars();
//                    codeArea.revealCursor();
//                }
//            } catch (UnsupportedFlavorException | IOException ex) {
//                Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
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

    public class CodeDataClipboardData implements Transferable, ClipboardOwner {

        private final BinaryData data;

        public CodeDataClipboardData(BinaryData data) {
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
                int charsPerByte = codeArea.getCodeType().getMaxDigits() + 1;
                int textLength = (int) (data.getDataSize() * charsPerByte);
                if (textLength > 0) {
                    textLength--;
                }

                char[] targetData = new char[textLength];
                Arrays.fill(targetData, ' ');
                for (int i = 0; i < data.getDataSize(); i++) {
                    CodeAreaUtils.byteToCharsCode(data.getByte(i), codeArea.getCodeType(), targetData, i * charsPerByte, codeArea.getHexCharactersCase());
                }
                return new String(targetData);
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }
    }
}
