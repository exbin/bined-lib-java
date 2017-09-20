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
package org.exbin.deltahex.swing;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeAreaUtils;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.CodeAreaViewMode;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.2.0 2017/09/20
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int NO_MODIFIER = 0;
    public static final String MIME_CLIPBOARD_BINARY = "application/octet-stream";
    public static final String FALLBACK_CLIPBOARD = "clipboard";
    public static final String DEFAULT_ENCODING = "UTF-8";
    private static final int CODE_BUFFER_LENGTH = 16;

    private final int metaMask;

    private final CodeArea codeArea;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;
    private ClipboardData currentClipboardData = null;

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
            clipboard = new Clipboard(FALLBACK_CLIPBOARD);
        }
        try {
            clipboard.addFlavorListener(new FlavorListener() {
                @Override
                public void flavorsChanged(FlavorEvent e) {
                    updateCanPaste();
                }
            });
            try {
                binaryDataFlavor = new DataFlavor(MIME_CLIPBOARD_BINARY);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            updateCanPaste();
        } catch (IllegalStateException ex) {
            canPaste = false;
        } catch (java.awt.HeadlessException ex) {
            Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateCanPaste() {
        try {
            canPaste = clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (IllegalStateException ex) {
            canPaste = false;
        }
    }

    @Override
    public void sequenceBreak() {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll position offset instead of cursor
                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                    if (scrollPosition.getLineDataOffset() < codeArea.getBytesPerLine() - 1) {
                        scrollPosition.setLineDataOffset(scrollPosition.getLineDataOffset() + 1);
                    } else {
                        if (scrollPosition.getScrollLinePosition() > 0) {
                            scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - 1);
                        }
                        scrollPosition.setLineDataOffset(0);
                    }

                    codeArea.getCaret().resetBlink();
                    codeArea.resetPainter();
                    codeArea.notifyScrolled();
                    codeArea.repaint();
                } else {
                    moveLeft(keyEvent.getModifiersEx());
                    sequenceBreak();
                    codeArea.revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll position offset instead of cursor
                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                    if (scrollPosition.getLineDataOffset() > 0) {
                        scrollPosition.setLineDataOffset(scrollPosition.getLineDataOffset() - 1);
                    } else {
                        long dataSize = codeArea.getDataSize();
                        if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine()) {
                            scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
                        }
                        scrollPosition.setLineDataOffset(codeArea.getBytesPerLine() - 1);
                    }

                    codeArea.getCaret().resetBlink();
                    codeArea.resetPainter();
                    codeArea.notifyScrolled();
                    codeArea.repaint();
                } else {
                    moveRight(keyEvent.getModifiersEx());
                    sequenceBreak();
                    codeArea.revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_UP: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll page instead of cursor
                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                    if (scrollPosition.getScrollLinePosition() > 0) {
                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - 1);
                        codeArea.updateScrollBars();
                        codeArea.notifyScrolled();
                    }
                } else {
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesPerLine) {
                            codeArea.getCaret().setCaretPosition(caretPosition.getDataPosition() - bytesPerLine, caretPosition.getCodeOffset());
                            codeArea.notifyCaretMoved();
                        }
                        updateSelection(keyEvent.getModifiersEx(), caretPosition);
                    }
                    sequenceBreak();
                    codeArea.revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DOWN: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                long dataSize = codeArea.getDataSize();
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll page instead of cursor
                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                    if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine()) {
                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
                        codeArea.updateScrollBars();
                        codeArea.notifyScrolled();
                    }
                } else {
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesPerLine < dataSize
                                || (caretPosition.getDataPosition() + bytesPerLine == dataSize && caretPosition.getCodeOffset() == 0)) {
                            codeArea.getCaret().setCaretPosition(caretPosition.getDataPosition() + bytesPerLine, caretPosition.getCodeOffset());
                            codeArea.notifyCaretMoved();
                        }
                        updateSelection(keyEvent.getModifiersEx(), caretPosition);
                    }
                    sequenceBreak();
                    codeArea.revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                if (caretPosition.getDataPosition() > 0 || caretPosition.getCodeOffset() != 0) {
                    long targetPosition;
                    if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                        targetPosition = 0;
                    } else {
                        targetPosition = ((caretPosition.getDataPosition() + scrollPosition.getLineDataOffset()) / bytesPerLine) * bytesPerLine - scrollPosition.getLineDataOffset();
                        if (targetPosition < 0) {
                            targetPosition = 0;
                        }
                    }
                    codeArea.getCaret().setCaretPosition(targetPosition);
                    sequenceBreak();
                    codeArea.notifyCaretMoved();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                codeArea.revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                long dataSize = codeArea.getDataSize();
                if (caretPosition.getDataPosition() < dataSize) {
                    if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                        codeArea.getCaret().setCaretPosition(codeArea.getDataSize());
                    } else if (codeArea.getActiveSection() == CodeAreaSection.CODE_MATRIX) {
                        long newPosition = (((caretPosition.getDataPosition() + scrollPosition.getLineDataOffset()) / bytesPerLine) + 1) * bytesPerLine - 1 - scrollPosition.getLineDataOffset();
                        codeArea.getCaret().setCaretPosition(newPosition < dataSize ? newPosition : dataSize, newPosition < dataSize ? codeArea.getCodeType().getMaxDigitsForByte() - 1 : 0);
                    } else {
                        long newPosition = (((caretPosition.getDataPosition() + scrollPosition.getLineDataOffset()) / bytesPerLine) + 1) * bytesPerLine - 1 - scrollPosition.getLineDataOffset();
                        codeArea.getCaret().setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
                    }
                    sequenceBreak();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                codeArea.revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRectangle();
                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                if (scrollPosition.getScrollLinePosition() > codeArea.getLinesPerRectangle()) {
                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - codeArea.getLinesPerRectangle());
                    codeArea.updateScrollBars();
                    codeArea.notifyScrolled();
                }
                if (caretPosition.getDataPosition() > 0) {
                    if (caretPosition.getDataPosition() >= bytesStep) {
                        codeArea.getCaret().setCaretPosition(caretPosition.getDataPosition() - bytesStep, caretPosition.getCodeOffset());
                    } else if (caretPosition.getDataPosition() >= codeArea.getBytesPerLine()) {
                        codeArea.getCaret().setCaretPosition(caretPosition.getDataPosition() % codeArea.getBytesPerLine(), caretPosition.getCodeOffset());
                    }
                    sequenceBreak();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                codeArea.revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRectangle();
                long dataSize = codeArea.getDataSize();
                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine() - codeArea.getLinesPerRectangle() * 2) {
                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + codeArea.getLinesPerRectangle());
                    codeArea.updateScrollBars();
                    codeArea.notifyScrolled();
                }
                if (caretPosition.getDataPosition() < dataSize) {
                    if (caretPosition.getDataPosition() + bytesStep < dataSize) {
                        codeArea.getCaret().setCaretPosition(caretPosition.getDataPosition() + bytesStep, caretPosition.getCodeOffset());
                    } else if (caretPosition.getDataPosition() + codeArea.getBytesPerLine() <= dataSize) {
                        long dataPosition = dataSize
                                - dataSize % codeArea.getBytesPerLine()
                                - ((caretPosition.getDataPosition() % codeArea.getBytesPerLine() <= dataSize % codeArea.getBytesPerLine()) ? 0 : codeArea.getBytesPerLine())
                                + (caretPosition.getDataPosition() % codeArea.getBytesPerLine());
                        codeArea.getCaret().setCaretPosition(dataPosition, dataPosition == dataSize ? 0 : caretPosition.getCodeOffset());
                    }
                    sequenceBreak();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                codeArea.revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_INSERT: {
                switch (codeArea.getEditationMode()) {
                    case INSERT: {
                        codeArea.setEditationMode(EditationMode.OVERWRITE);
                        keyEvent.consume();
                        break;
                    }
                    case OVERWRITE: {
                        codeArea.setEditationMode(EditationMode.INSERT);
                        keyEvent.consume();
                        break;
                    }
                }
                break;
            }
            case KeyEvent.VK_TAB: {
                if (codeArea.getViewMode() == CodeAreaViewMode.DUAL) {
                    CodeAreaSection activeSection = codeArea.getActiveSection() == CodeAreaSection.CODE_MATRIX ? CodeAreaSection.TEXT_PREVIEW : CodeAreaSection.CODE_MATRIX;
                    if (activeSection == CodeAreaSection.TEXT_PREVIEW) {
                        codeArea.getCaretPosition().setCodeOffset(0);
                    }
                    codeArea.getCaret().setSection(activeSection);
                    codeArea.revealCursor();
                    codeArea.repaint();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DELETE: {
                deletePressed();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                backSpacePressed();
                keyEvent.consume();
                break;
            }
            default: {
                if (codeArea.isHandleClipboard()) {
                    if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_C) {
                        copy();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_X) {
                        cut();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_V) {
                        paste();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_A) {
                        codeArea.selectAll();
                        keyEvent.consume();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        char keyValue = keyEvent.getKeyChar();
        // TODO Add support for high unicode codes
        if (keyValue == KeyEvent.CHAR_UNDEFINED) {
            return;
        }
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.getActiveSection() == CodeAreaSection.CODE_MATRIX) {
            long dataPosition = codeArea.getDataPosition();
            int codeOffset = codeArea.getCodeOffset();
            CodeType codeType = codeArea.getCodeType();
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
                if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
                    if (dataPosition == codeArea.getDataSize()) {
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
                moveRight(NO_MODIFIER);
                codeArea.revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && isValidChar(keyValue)) {
                BinaryData data = codeArea.getData();
                CaretPosition caretPosition = codeArea.getCaretPosition();
                long dataPosition = caretPosition.getDataPosition();
                byte[] bytes = charToBytes(keyChar);
                if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
                    if (dataPosition < codeArea.getDataSize()) {
                        int length = bytes.length;
                        if (dataPosition + length > codeArea.getDataSize()) {
                            length = (int) (codeArea.getDataSize() - dataPosition);
                        }
                        ((EditableBinaryData) data).remove(dataPosition, length);
                    }
                }
                ((EditableBinaryData) data).insert(dataPosition, bytes);
                codeArea.notifyDataChanged();
                codeArea.getCaret().setCaretPosition(dataPosition + bytes.length - 1);
                moveRight(NO_MODIFIER);
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
        CodeType codeType = codeArea.getCodeType();
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
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                moveLeft(NO_MODIFIER);
                caret.setCodeOffset(0);
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition - 1, 1);
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
            codeArea.updateScrollBars();
            codeArea.notifyCaretMoved();
            codeArea.revealCursor();
        } else {
            CodeAreaCaret caret = codeArea.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                codeArea.updateScrollBars();
                codeArea.notifyCaretMoved();
                codeArea.revealCursor();
            }
        }
    }

    private void deleteSelection() {
        SelectionRange selection = codeArea.getSelection();
        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) codeArea.getData()).remove(first, last - first + 1);
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

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy);
            setClipboardContent(binaryData);
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            CodeDataClipboardData binaryData = new CodeDataClipboardData(copy);
            setClipboardContent(binaryData);
        }
    }

    private void setClipboardContent(ClipboardData content) {
        clearClipboardData();
        try {
            currentClipboardData = content;
            clipboard.setContents(content, content);
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore and clear
            clearClipboardData();
        }
    }

    private void clearClipboardData() {
        if (currentClipboardData != null) {
            currentClipboardData.dispose();
            currentClipboardData = null;
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
                        if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
                            long toRemove = dataSize;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getData()).insert(codeArea.getDataPosition(), data);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + dataSize);
                        caret.setCodeOffset(0);
                        codeArea.updateScrollBars();
                        codeArea.notifyCaretMoved();
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

                Object insertedData;
                try {
                    insertedData = clipboard.getData(DataFlavor.stringFlavor);
                    if (insertedData instanceof String) {
                        CodeAreaCaret caret = codeArea.getCaret();
                        long dataPosition = caret.getDataPosition();

                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName(DEFAULT_ENCODING));
                        int length = bytes.length;
                        if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
                            long toRemove = length;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getData()).insert(codeArea.getDataPosition(), bytes);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + length);
                        caret.setCodeOffset(0);
                        codeArea.updateScrollBars();
                        codeArea.notifyCaretMoved();
                        codeArea.revealCursor();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    @Override
    public void pasteFromCode() {
        if (!codeArea.isEditable()) {
            return;
        }

        try {
            if (!clipboard.isDataFlavorAvailable(binaryDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return;
            }
        } catch (IllegalStateException ex) {
            return;
        }

        try {
            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                paste();
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

                        CodeType codeType = codeArea.getCodeType();
                        ByteArrayEditableData data = new ByteArrayEditableData();
                        insertHexStringIntoData((String) insertedData, data, codeType);

                        long length = data.getDataSize();
                        if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
                            long toRemove = length;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getData()).insert(codeArea.getDataPosition(), data);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + length);
                        caret.setCodeOffset(0);
                        codeArea.updateScrollBars();
                        codeArea.notifyCaretMoved();
                        codeArea.revealCursor();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    private void insertHexStringIntoData(String insertedString, EditableBinaryData data, CodeType codeType) {
        int maxDigits = codeType.getMaxDigitsForByte();
        byte[] buffer = new byte[CODE_BUFFER_LENGTH];
        int bufferUsage = 0;
        int offset = 0;
        for (int i = 0; i < insertedString.length(); i++) {
            char charAt = insertedString.charAt(i);
            if ((charAt == ' ' || charAt == '\t') && offset == i) {
                offset++;
            } else if (charAt == ' ' || charAt == '\t' || charAt == ',' || charAt == ';' || charAt == ':') {
                byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset, i), codeType);
                if (bufferUsage < CODE_BUFFER_LENGTH) {
                    buffer[bufferUsage] = value;
                    bufferUsage++;
                } else {
                    data.insert(data.getDataSize(), buffer, 0, bufferUsage);
                    bufferUsage = 0;
                }
                offset = i + 1;
            } else if (i == offset + maxDigits) {
                byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset, i), codeType);
                if (bufferUsage < CODE_BUFFER_LENGTH) {
                    buffer[bufferUsage] = value;
                    bufferUsage++;
                } else {
                    data.insert(data.getDataSize(), buffer, 0, bufferUsage);
                    bufferUsage = 0;
                }
                offset = i;
            }
        }

        if (offset < insertedString.length()) {
            byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset), codeType);
            if (bufferUsage < CODE_BUFFER_LENGTH) {
                buffer[bufferUsage] = value;
                bufferUsage++;
            } else {
                data.insert(data.getDataSize(), buffer, 0, bufferUsage);
                bufferUsage = 0;
            }
        }

        if (bufferUsage > 0) {
            data.insert(data.getDataSize(), buffer, 0, bufferUsage);
        }
    }

    @Override
    public boolean canPaste() {
        return canPaste;
    }

    @Override
    public void selectAll() {
        long dataSize = codeArea.getDataSize();
        if (dataSize > 0) {
            codeArea.setSelection(new SelectionRange(0, dataSize - 1));
            codeArea.notifySelectionChanged();
            codeArea.repaint();
        }
    }

    @Override
    public void clearSelection() {
        codeArea.setSelection(new SelectionRange());
    }

    public void updateSelection(int modifiers, CaretPosition caretPosition) {
        CodeAreaCaret caret = codeArea.getCaret();
        SelectionRange selection = codeArea.getSelection();
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0) {
            long currentPosition = caret.getDataPosition();
            long end = currentPosition;
            long start;
            if (selection != null) {
                start = selection.getStart();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    codeArea.setSelection(new SelectionRange(selection.getStart(), start < currentPosition ? end - 1 : end));
                }
            } else {
                start = caretPosition.getDataPosition();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    codeArea.setSelection(new SelectionRange(start, start < currentPosition ? end - 1 : end));
                }
            }
        } else {
            clearSelection();
        }
        codeArea.repaint();
    }

    @Override
    public void moveCaret(MouseEvent me, int modifiers) {
        CodeAreaPainter painter = codeArea.getPainter();
        Rectangle hexRect = codeArea.getDataViewRectangle();
        CodeAreaViewMode viewMode = codeArea.getViewMode();
        CodeType codeType = codeArea.getCodeType();
        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
        CodeAreaCaret caret = codeArea.getCaret();
        int bytesPerLine = codeArea.getBytesPerLine();
        int mouseX = me.getX();
        if (mouseX < hexRect.x) {
            mouseX = hexRect.x;
        }
        int cursorCharX = codeArea.computeCodeAreaCharacter(mouseX - hexRect.x + scrollPosition.getScrollCharOffset()) + scrollPosition.getScrollCharPosition();
        long cursorLineY = codeArea.computeCodeAreaLine(me.getY() - hexRect.y + scrollPosition.getScrollLineOffset()) + scrollPosition.getScrollLinePosition();
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnLine;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < painter.getPreviewFirstChar()) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(CodeAreaSection.CODE_MATRIX);
            byteOnLine = painter.computePositionByte(cursorCharX);
            if (byteOnLine >= bytesPerLine) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - painter.computeFirstCodeCharPos(byteOnLine);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(CodeAreaSection.TEXT_PREVIEW);
            byteOnLine = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnLine -= painter.getPreviewFirstChar();
            }
        }

        if (byteOnLine >= bytesPerLine) {
            byteOnLine = bytesPerLine - 1;
        }

        dataPosition = byteOnLine + (cursorLineY * bytesPerLine) - scrollPosition.getLineDataOffset();
        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        long dataSize = codeArea.getDataSize();
        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, codeOffset);
        codeArea.notifyCaretMoved();
        sequenceBreak();

        updateSelection(modifiers, caretPosition);
    }

    public void moveRight(int modifiers) {
        CodeAreaCaret caret = codeArea.getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        CodeType codeType = codeArea.getCodeType();
        if (caretPosition.getDataPosition() < codeArea.getDataSize()) {
            if (caret.getSection() == CodeAreaSection.CODE_MATRIX) {
                int codeOffset = caret.getCodeOffset();
                if (caretPosition.getDataPosition() < codeArea.getDataSize()) {
                    if (codeOffset < codeType.getMaxDigitsForByte() - 1) {
                        caret.setCodeOffset(codeOffset + 1);
                    } else {
                        caret.setCaretPosition(caretPosition.getDataPosition() + 1, 0);
                    }
                    updateSelection(modifiers, caretPosition);
                    codeArea.notifyCaretMoved();
                }
            } else {
                caret.setCaretPosition(caretPosition.getDataPosition() + 1);
                updateSelection(modifiers, caretPosition);
                codeArea.notifyCaretMoved();
            }
        }
    }

    public void moveLeft(int modifiers) {
        CodeAreaCaret caret = codeArea.getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        CodeType codeType = codeArea.getCodeType();
        if (caret.getSection() == CodeAreaSection.CODE_MATRIX) {
            int codeOffset = caret.getCodeOffset();
            if (codeOffset > 0) {
                caret.setCodeOffset(codeOffset - 1);
                updateSelection(modifiers, caretPosition);
                codeArea.notifyCaretMoved();
            } else if (caretPosition.getDataPosition() > 0) {
                caret.setCaretPosition(caretPosition.getDataPosition() - 1, codeType.getMaxDigitsForByte() - 1);
                updateSelection(modifiers, caretPosition);
                codeArea.notifyCaretMoved();
            }
        } else if (caretPosition.getDataPosition() > 0) {
            caret.setCaretPosition(caretPosition.getDataPosition() - 1);
            updateSelection(modifiers, caretPosition);
            codeArea.notifyCaretMoved();
        }
    }

    public boolean isValidChar(char value) {
        return codeArea.getCharset().canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = codeArea.getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    public class BinaryDataClipboardData implements ClipboardData {

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
                return byteArrayStream.toString(DEFAULT_ENCODING);
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }

        @Override
        public void dispose() {
            data.dispose();
        }
    }

    private class CodeDataClipboardData implements ClipboardData {

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
                int charsPerByte = codeArea.getCodeType().getMaxDigitsForByte() + 1;
                int textLength = (int) (data.getDataSize() * charsPerByte);
                if (textLength > 0) {
                    textLength--;
                }

                char[] targetData = new char[textLength];
                Arrays.fill(targetData, ' ');
                throw new UnsupportedOperationException("Not supported yet.");
//                for (int i = 0; i < data.getDataSize(); i++) {
//                    CodeAreaUtils.byteToCharsCode(data.getByte(i), codeArea.getCodeType(), targetData, i * charsPerByte, codeArea.getHexCharactersCase());
//                }
//                return new String(targetData);
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }

        @Override
        public void dispose() {
            data.dispose();
        }
    }

    private static interface ClipboardData extends Transferable, ClipboardOwner {

        void dispose();
    }
}
