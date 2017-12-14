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
package org.exbin.deltahex.swing.basic;

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
import javax.annotation.Nonnull;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaCaret;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeAreaUtils;
import org.exbin.deltahex.CodeAreaViewMode;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.swing.CodeAreaCommandHandler;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.deltahex.capability.CaretCapable;
import org.exbin.deltahex.capability.CharsetCapable;
import org.exbin.deltahex.capability.CodeTypeCapable;
import org.exbin.deltahex.capability.EditationModeCapable;
import org.exbin.deltahex.capability.SelectionCapable;
import org.exbin.deltahex.capability.ViewModeCapable;
import org.exbin.deltahex.swing.CodeAreaWorker;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.2.0 2017/12/14
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int NO_MODIFIER = 0;
    public static final String MIME_CLIPBOARD_BINARY = "application/octet-stream";
    public static final String FALLBACK_CLIPBOARD = "clipboard";
    public static final String DEFAULT_ENCODING = "UTF-8";

    private final int metaMask;

    @Nonnull
    private final CodeArea codeArea;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;
    private ClipboardData currentClipboardData = null;

    public DefaultCodeAreaCommandHandler(@Nonnull CodeArea codeArea) {
        this.codeArea = codeArea;
        CodeAreaWorker worker = codeArea.getWorker();
        codeTypeSupported = worker instanceof CodeTypeCapable;
        viewModeSupported = worker instanceof ViewModeCapable;

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
    public void keyPressed(@Nonnull KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll position offset instead of cursor
//                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getLineDataOffset() < codeArea.getBytesPerLine() - 1) {
//                        scrollPosition.setLineDataOffset(scrollPosition.getLineDataOffset() + 1);
//                    } else {
//                        if (scrollPosition.getScrollLinePosition() > 0) {
//                            scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - 1);
//                        }
//                        scrollPosition.setLineDataOffset(0);
//                    }
//
//                    codeArea.getCaret().resetBlink();
//                    codeArea.resetPainter();
//                    codeArea.notifyScrolled();
//                    codeArea.repaint();
                } else {
                    moveLeft(keyEvent.getModifiersEx());
                    sequenceBreak();
                    revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll position offset instead of cursor
//                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getLineDataOffset() > 0) {
//                        scrollPosition.setLineDataOffset(scrollPosition.getLineDataOffset() - 1);
//                    } else {
//                        long dataSize = codeArea.getDataSize();
//                        if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine()) {
//                            scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
//                        }
//                        scrollPosition.setLineDataOffset(codeArea.getBytesPerLine() - 1);
//                    }
//
//                    codeArea.getCaret().resetBlink();
//                    codeArea.resetPainter();
//                    codeArea.notifyScrolled();
//                    codeArea.repaint();
                } else {
                    moveRight(keyEvent.getModifiersEx());
                    sequenceBreak();
                    revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_UP: {
                CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll page instead of cursor
//                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getScrollLinePosition() > 0) {
//                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - 1);
//                        codeArea.getPainter().updateScrollBars();
//                        codeArea.notifyScrolled();
//                    }
                } else {
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesPerLine) {
                            ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(new CaretPosition(caretPosition.getDataPosition() - bytesPerLine, caretPosition.getCodeOffset()));
                            notifyCaretMoved();
                        }
                        updateSelection(keyEvent.getModifiersEx(), caretPosition);
                    }
                    sequenceBreak();
                    revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DOWN: {
                CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                long dataSize = codeArea.getDataSize();
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll page instead of cursor
//                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine()) {
//                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
//                        codeArea.getPainter().updateScrollBars();
//                        codeArea.notifyScrolled();
//                    }
                } else {
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesPerLine < dataSize
                                || (caretPosition.getDataPosition() + bytesPerLine == dataSize && caretPosition.getCodeOffset() == 0)) {
                            ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(new CaretPosition(caretPosition.getDataPosition() + bytesPerLine, caretPosition.getCodeOffset()));
                            notifyCaretMoved();
                        }
                        updateSelection(keyEvent.getModifiersEx(), caretPosition);
                    }
                    sequenceBreak();
                    revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {
                CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                if (caretPosition.getDataPosition() > 0 || caretPosition.getCodeOffset() != 0) {
                    long targetPosition;
                    if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                        targetPosition = 0;
                    } else {
//                        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                        targetPosition = ((caretPosition.getDataPosition() + scrollPosition.getLineDataOffset()) / bytesPerLine) * bytesPerLine - scrollPosition.getLineDataOffset();
//                        if (targetPosition < 0) {
                        targetPosition = 0;
//                        }
                    }
                    ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(targetPosition);
                    sequenceBreak();
                    notifyCaretMoved();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
                CodeAreaCaret caret = ((CaretCapable) codeArea.getWorker()).getCaret();
                CaretPosition caretPosition = caret.getCaretPosition();
                int bytesPerLine = codeArea.getBytesPerLine();
                long dataSize = codeArea.getDataSize();
                if (caretPosition.getDataPosition() < dataSize) {
                    if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                        caret.setCaretPosition(codeArea.getDataSize());
                    } else if (codeArea.getActiveSection() == CodeAreaSection.CODE_MATRIX) {
                        long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                        caret.setCaretPosition(new CaretPosition(newPosition < dataSize ? newPosition : dataSize, newPosition < dataSize ? getMaxDigitsForByte() - 1 : 0));
                    } else {
                        long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                        caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
                    }
                    sequenceBreak();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRectangle();
//                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                if (scrollPosition.getScrollLinePosition() > codeArea.getLinesPerRectangle()) {
//                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - codeArea.getLinesPerRectangle());
//                    codeArea.getPainter().updateScrollBars();
//                    codeArea.notifyScrolled();
//                }
                if (caretPosition.getDataPosition() > 0) {
                    if (caretPosition.getDataPosition() >= bytesStep) {
                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition.getDataPosition() - bytesStep, caretPosition.getCodeOffset());
                    } else if (caretPosition.getDataPosition() >= codeArea.getBytesPerLine()) {
                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition.getDataPosition() % codeArea.getBytesPerLine(), caretPosition.getCodeOffset());
                    }
                    sequenceBreak();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                CaretPosition caretPosition = codeArea.getCaretPosition();
                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRectangle();
                long dataSize = codeArea.getDataSize();
//                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine() - codeArea.getLinesPerRectangle() * 2) {
//                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + codeArea.getLinesPerRectangle());
//                    codeArea.getPainter().updateScrollBars();
//                    codeArea.notifyScrolled();
//                }
                if (caretPosition.getDataPosition() < dataSize) {
                    if (caretPosition.getDataPosition() + bytesStep < dataSize) {
                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition.getDataPosition() + bytesStep, caretPosition.getCodeOffset());
                    } else if (caretPosition.getDataPosition() + codeArea.getBytesPerLine() <= dataSize) {
                        long dataPosition = dataSize
                                - dataSize % codeArea.getBytesPerLine()
                                - ((caretPosition.getDataPosition() % codeArea.getBytesPerLine() <= dataSize % codeArea.getBytesPerLine()) ? 0 : codeArea.getBytesPerLine())
                                + (caretPosition.getDataPosition() % codeArea.getBytesPerLine());
                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(dataPosition, dataPosition == dataSize ? 0 : caretPosition.getCodeOffset());
                    }
                    sequenceBreak();
                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
                }
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_INSERT: {
                EditationMode editationMode = ((EditationModeCapable) codeArea.getWorker()).getEditationMode();
                switch (editationMode) {
                    case INSERT: {
                        ((EditationModeCapable) codeArea.getWorker()).setEditationMode(EditationMode.OVERWRITE);
                        keyEvent.consume();
                        break;
                    }
                    case OVERWRITE: {
                        ((EditationModeCapable) codeArea.getWorker()).setEditationMode(EditationMode.INSERT);
                        keyEvent.consume();
                        break;
                    }
                }
                break;
            }
            case KeyEvent.VK_TAB: {
                if (viewModeSupported) {
                    if (((ViewModeCapable) codeArea.getWorker()).getViewMode() == CodeAreaViewMode.DUAL) {
                        CodeAreaSection activeSection = codeArea.getActiveSection() == CodeAreaSection.CODE_MATRIX ? CodeAreaSection.TEXT_PREVIEW : CodeAreaSection.CODE_MATRIX;
                        if (activeSection == CodeAreaSection.TEXT_PREVIEW) {
                            codeArea.getCaretPosition().setCodeOffset(0);
                        }
                        ((CaretCapable) codeArea.getWorker()).getCaret().setSection(activeSection);
                        revealCursor();
                        codeArea.repaint();
                    }
                    keyEvent.consume();
                }
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
                if (((ClipboardCapable) codeArea.getWorker()).isHandleClipboard()) {
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
    public void keyTyped(@Nonnull KeyEvent keyEvent) {
        char keyValue = keyEvent.getKeyChar();
        // TODO Add support for high unicode codes
        if (keyValue == KeyEvent.CHAR_UNDEFINED) {
            return;
        }
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        if (codeArea.getActiveSection() == CodeAreaSection.CODE_MATRIX) {
            long dataPosition = codeArea.getDataPosition();
            int codeOffset = codeArea.getCodeOffset();
            CodeType codeType = getCodeType();
            boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyValue, codeOffset, codeType);
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
                if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
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
                revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && isValidChar(keyValue)) {
                BinaryData data = codeArea.getData();
                CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
                long dataPosition = caretPosition.getDataPosition();
                byte[] bytes = charToBytes(keyChar);
                if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
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
                ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(dataPosition + bytes.length - 1);
                moveRight(NO_MODIFIER);
                revealCursor();
            }
        }
    }

    private void setCodeValue(int value) {
        long dataPosition = codeArea.getDataPosition();
        int codeOffset = codeArea.getCodeOffset();
        BinaryData data = codeArea.getData();
        CodeType codeType = getCodeType();
        byte byteValue = data.getByte(dataPosition);
        byte outputValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) data).setByte(dataPosition, outputValue);
    }

    @Override
    public void backSpacePressed() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            DefaultCodeAreaCaret caret = codeArea.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                moveLeft(NO_MODIFIER);
                caret.setCodeOffset(0);
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                revealCursor();
                codeArea.getPainter().updateScrollBars();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
            codeArea.getPainter().updateScrollBars();
            notifyCaretMoved();
            revealCursor();
        } else {
            DefaultCodeAreaCaret caret = codeArea.getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                codeArea.getPainter().updateScrollBars();
                notifyCaretMoved();
                revealCursor();
            }
        }
    }

    private void deleteSelection() {
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) codeArea.getData()).remove(first, last - first + 1);
        codeArea.clearSelection();
        DefaultCodeAreaCaret caret = codeArea.getCaret();
        caret.setCaretPosition(first);
        revealCursor();
        codeArea.getPainter().updateScrollBars();
    }

    @Override
    public void delete() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        deleteSelection();
        codeArea.notifyDataChanged();
    }

    @Override
    public void copy() {
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
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
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            CodeDataClipboardData binaryData = new CodeDataClipboardData(copy);
            setClipboardContent(binaryData);
        }
    }

    private void setClipboardContent(@Nonnull ClipboardData content) {
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
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (selection != null) {
            copy();
            deleteSelection();
            codeArea.notifyDataChanged();
        }
    }

    @Override
    public void paste() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
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
                        DefaultCodeAreaCaret caret = codeArea.getCaret();
                        long dataPosition = caret.getDataPosition();

                        BinaryData data = (BinaryData) object;
                        long dataSize = data.getDataSize();
                        if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
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
                        codeArea.getPainter().updateScrollBars();
                        notifyCaretMoved();
                        revealCursor();
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
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
                        long dataPosition = caret.getDataPosition();

                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName(DEFAULT_ENCODING));
                        int length = bytes.length;
                        if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
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
                        codeArea.getPainter().updateScrollBars();
                        notifyCaretMoved();
                        revealCursor();
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
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
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
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
                        long dataPosition = caret.getDataPosition();

                        CodeType codeType = getCodeType();
                        ByteArrayEditableData data = new ByteArrayEditableData();
                        CodeAreaUtils.insertHexStringIntoData((String) insertedData, data, codeType);

                        long length = data.getDataSize();
                        if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
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
                        codeArea.getWorker().updateScrollBars();
                        notifyCaretMoved();
                        revealCursor();
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
    public boolean canPaste() {
        return canPaste;
    }

    @Override
    public void selectAll() {
        long dataSize = codeArea.getDataSize();
        if (dataSize > 0) {
            ((SelectionCapable) codeArea.getWorker()).setSelection(new SelectionRange(0, dataSize - 1));
            // TODO ((SelectionCapable) codeArea.getWorker()).notifySelectionChanged();
            codeArea.repaint();
        }
    }

    @Override
    public void clearSelection() {
        ((SelectionCapable) codeArea.getWorker()).setSelection(new SelectionRange());
    }

    public void updateSelection(int modifiers, CaretPosition caretPosition) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0) {
            long currentPosition = caret.getDataPosition();
            long end = currentPosition;
            long start;
            if (selection != null) {
                start = selection.getStart();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    ((SelectionCapable) codeArea.getWorker()).setSelection(new SelectionRange(selection.getStart(), start < currentPosition ? end - 1 : end));
                }
            } else {
                start = caretPosition.getDataPosition();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    ((SelectionCapable) codeArea.getWorker()).setSelection(new SelectionRange(start, start < currentPosition ? end - 1 : end));
                }
            }
        } else {
            clearSelection();
        }
        codeArea.repaint();
    }

    @Override
    public void moveCaret(MouseEvent me, int modifiers) {
        notifyCaretMoved();
        sequenceBreak();

        CaretPosition caretPosition = codeArea.getPainter().mousePositionToCaretPosition(me.getX(), me.getY());
        updateSelection(modifiers, caretPosition);
    }

    public void moveRight(int modifiers) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() < codeArea.getDataSize()) {
            if (caret.getSection() == CodeAreaSection.CODE_MATRIX) {
                int codeOffset = caret.getCodeOffset();
                if (caretPosition.getDataPosition() < codeArea.getDataSize()) {
                    CodeType codeType = ((CodeTypeCapable) codeArea.getWorker()).getCodeType();
                    if (codeOffset < codeType.getMaxDigitsForByte() - 1) {
                        caret.setCodeOffset(codeOffset + 1);
                    } else {
                        caret.setCaretPosition(caretPosition.getDataPosition() + 1, 0);
                    }
                    updateSelection(modifiers, caretPosition);
                    notifyCaretMoved();
                }
            } else {
                caret.setCaretPosition(caretPosition.getDataPosition() + 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            }
        }
    }

    public void moveLeft(int modifiers) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caret.getSection() == CodeAreaSection.CODE_MATRIX) {
            int codeOffset = caret.getCodeOffset();
            if (codeOffset > 0) {
                caret.setCodeOffset(codeOffset - 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            } else if (caretPosition.getDataPosition() > 0) {
                CodeType codeType = ((CodeTypeCapable) codeArea.getWorker()).getCodeType();
                caret.setCaretPosition(caretPosition.getDataPosition() - 1, codeType.getMaxDigitsForByte() - 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            }
        } else if (caretPosition.getDataPosition() > 0) {
            caret.setCaretPosition(caretPosition.getDataPosition() - 1);
            updateSelection(modifiers, caretPosition);
            notifyCaretMoved();
        }
    }

    public boolean isValidChar(char value) {
        return ((CharsetCapable) codeArea.getWorker()).getCharset().canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = ((CharsetCapable) codeArea.getWorker()).getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Nonnull
    private CodeType getCodeType() {
        if (codeTypeSupported) {
            return ((CodeTypeCapable) codeArea.getWorker()).getCodeType();
        }

        return CodeType.HEXADECIMAL;
    }

    private void revealCursor() {
        ((CaretCapable) codeArea.getWorker()).revealCursor();
    }

    private void notifyCaretMoved() {
        ((CaretCapable) codeArea.getWorker()).notifyCaretMoved();
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
                int charsPerByte = getCodeType().getMaxDigitsForByte() + 1;
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
