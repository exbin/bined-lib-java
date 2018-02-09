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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.exbin.deltahex.CaretPosition;
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
import org.exbin.deltahex.capability.ClipboardCapable;
import org.exbin.deltahex.capability.CodeTypeCapable;
import org.exbin.deltahex.capability.EditationModeCapable;
import org.exbin.deltahex.capability.SelectionCapable;
import org.exbin.deltahex.capability.ViewModeCapable;
import org.exbin.deltahex.swing.CodeAreaWorker;
import org.exbin.deltahex.swing.MovementShift;
import org.exbin.deltahex.swing.ScrollingShift;
import org.exbin.deltahex.swing.capability.ScrollingCapable;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.2.0 2018/01/01
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int NO_MODIFIER = 0;
    public static final String FALLBACK_CLIPBOARD = "clipboard";

    private final int metaMask;

    @Nonnull
    private final CodeArea codeArea;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;
    private CodeAreaUtils.ClipboardData currentClipboardData = null;

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
            clipboard.addFlavorListener((FlavorEvent e) -> {
                updateCanPaste();
            });
            try {
                binaryDataFlavor = new DataFlavor(CodeAreaUtils.MIME_CLIPBOARD_BINARY);
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

    private void updateScrollBars() {
        ((ScrollingCapable) codeArea.getWorker()).updateScrollBars();
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
                    move(keyEvent.getModifiersEx(), MovementShift.LEFT);
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
                    move(keyEvent.getModifiersEx(), MovementShift.RIGHT);
                    sequenceBreak();
                    revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_UP: {
                CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
//                int bytesPerLine = codeArea.getBytesPerLine();
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll page instead of cursor
//                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getScrollLinePosition() > 0) {
//                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - 1);
//                        codeArea.getPainter().updateScrollBars();
//                        codeArea.notifyScrolled();
//                    }
                } else {
                    move(keyEvent.getModifiersEx(), MovementShift.UP);
                    sequenceBreak();
                    revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DOWN: {
                CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
//                int bytesPerLine = codeArea.getBytesPerLine();
//                long dataSize = codeArea.getDataSize();
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    // Scroll page instead of cursor
//                    CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                    if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine()) {
//                        scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
//                        codeArea.getPainter().updateScrollBars();
//                        codeArea.notifyScrolled();
//                    }
                } else {
                    move(keyEvent.getModifiersEx(), MovementShift.DOWN);
                    sequenceBreak();
                    revealCursor();
                }
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {

//                CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
////                int bytesPerLine = codeArea.getBytesPerLine();
//                if (caretPosition.getDataPosition() > 0 || caretPosition.getCodeOffset() != 0) {
//                    long targetPosition;
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    move(keyEvent.getModifiersEx(), MovementShift.DOC_START);
                } else {
                    move(keyEvent.getModifiersEx(), MovementShift.LINE_START);
                }
                sequenceBreak();
                revealCursor();
//                    ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(targetPosition);
//                    sequenceBreak();
//                    notifyCaretMoved();
//                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
//                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
//                CodeAreaCaret caret = ((CaretCapable) codeArea.getWorker()).getCaret();
//                CaretPosition caretPosition = caret.getCaretPosition();
////                int bytesPerLine = codeArea.getBytesPerLine();
//                long dataSize = codeArea.getDataSize();
//                if (caretPosition.getDataPosition() < dataSize) {
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    move(keyEvent.getModifiersEx(), MovementShift.DOC_END);
////                        caret.setCaretPosition(codeArea.getDataSize());
////                    } else if (((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition().getSection() == CodeAreaSection.CODE_MATRIX) {
////                        long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
////                        caret.setCaretPosition(new CaretPosition(newPosition < dataSize ? newPosition : dataSize, newPosition < dataSize ? ((CodeTypeCapable) codeArea.getWorker()).getCodeType().getMaxDigitsForByte() - 1 : 0));
                } else {
                    move(keyEvent.getModifiersEx(), MovementShift.LINE_END);
                }
                sequenceBreak();
                revealCursor();
////                        long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
////                        caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
////                    }
//                    sequenceBreak();
//                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
//                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                scroll(ScrollingShift.PAGE_UP);
                move(keyEvent.getModifiersEx(), MovementShift.PAGE_UP);
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRectangle();
////                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
////                if (scrollPosition.getScrollLinePosition() > codeArea.getLinesPerRectangle()) {
////                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - codeArea.getLinesPerRectangle());
////                    codeArea.getPainter().updateScrollBars();
////                    codeArea.notifyScrolled();
////                }
//                if (caretPosition.getDataPosition() > 0) {
//                    if (caretPosition.getDataPosition() >= bytesStep) {
//                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition.getDataPosition() - bytesStep, caretPosition.getCodeOffset());
//                    } else if (caretPosition.getDataPosition() >= codeArea.getBytesPerLine()) {
//                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition.getDataPosition() % codeArea.getBytesPerLine(), caretPosition.getCodeOffset());
//                    }
                sequenceBreak();
//                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                scroll(ScrollingShift.PAGE_DOWN);
                move(keyEvent.getModifiersEx(), MovementShift.PAGE_DOWN);
//                CaretPosition caretPosition = codeArea.getCaretPosition();
//                int bytesStep = codeArea.getBytesPerLine() * codeArea.getLinesPerRectangle();
//                long dataSize = codeArea.getDataSize();
////                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
////                if (scrollPosition.getScrollLinePosition() < dataSize / codeArea.getBytesPerLine() - codeArea.getLinesPerRectangle() * 2) {
////                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + codeArea.getLinesPerRectangle());
////                    codeArea.getPainter().updateScrollBars();
////                    codeArea.notifyScrolled();
////                }
//                if (caretPosition.getDataPosition() < dataSize) {
//                    if (caretPosition.getDataPosition() + bytesStep < dataSize) {
//                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition.getDataPosition() + bytesStep, caretPosition.getCodeOffset());
//                    } else if (caretPosition.getDataPosition() + codeArea.getBytesPerLine() <= dataSize) {
//                        long dataPosition = dataSize
//                                - dataSize % codeArea.getBytesPerLine()
//                                - ((caretPosition.getDataPosition() % codeArea.getBytesPerLine() <= dataSize % codeArea.getBytesPerLine()) ? 0 : codeArea.getBytesPerLine())
//                                + (caretPosition.getDataPosition() % codeArea.getBytesPerLine());
//                        ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(dataPosition, dataPosition == dataSize ? 0 : caretPosition.getCodeOffset());
//                    }
                sequenceBreak();
//                    updateSelection(keyEvent.getModifiersEx(), caretPosition);
//                }
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
                if (viewModeSupported && ((ViewModeCapable) codeArea.getWorker()).getViewMode() == CodeAreaViewMode.DUAL) {
                    move(keyEvent.getModifiersEx(), MovementShift.SWITCH_SECTION);
                    sequenceBreak();
                    revealCursor();
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

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getSection() == CodeAreaSection.CODE_MATRIX) {
            long dataPosition = caretPosition.getDataPosition();
            int codeOffset = caretPosition.getCodeOffset();
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
                move(NO_MODIFIER, MovementShift.RIGHT);
                revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && isValidChar(keyValue)) {
                BinaryData data = codeArea.getData();
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
                move(NO_MODIFIER, MovementShift.RIGHT);
                revealCursor();
            }
        }
    }

    private void setCodeValue(int value) {
        CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
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
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                move(NO_MODIFIER, MovementShift.LEFT);
                caret.setCodeOffset(0);
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                revealCursor();
                updateScrollBars();
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
            updateScrollBars();
            notifyCaretMoved();
            revealCursor();
        } else {
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) codeArea.getData()).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                updateScrollBars();
                notifyCaretMoved();
                revealCursor();
            }
        }
    }

    private void deleteSelection() {
        BinaryData data = codeArea.getData();
        if (data == null) {
            return;
        }
        if (!(data instanceof EditableBinaryData)) {
            throw new IllegalStateException("Data is not editable");
        }

        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (selection.isEmpty()) {
            return;
        }

        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) codeArea.getData()).remove(first, last - first + 1);
        codeArea.clearSelection();
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        caret.setCaretPosition(first);
        revealCursor();
        updateScrollBars();
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
        if (!selection.isEmpty()) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            CodeAreaUtils.BinaryDataClipboardData binaryData = new CodeAreaUtils.BinaryDataClipboardData(copy, binaryDataFlavor);
            setClipboardContent(binaryData);
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (!selection.isEmpty()) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            CodeType codeType = ((CodeTypeCapable) codeArea.getWorker()).getCodeType();
            CodeAreaUtils.CodeDataClipboardData binaryData = new CodeAreaUtils.CodeDataClipboardData(copy, binaryDataFlavor, codeType);
            setClipboardContent(binaryData);
        }
    }

    private void setClipboardContent(@Nonnull CodeAreaUtils.ClipboardData content) {
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
        if (!selection.isEmpty()) {
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
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
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
                        ((EditableBinaryData) codeArea.getData()).insert(dataPosition, data);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + dataSize);
                        caret.setCodeOffset(0);
                        updateScrollBars();
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

                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName(CodeAreaUtils.DEFAULT_ENCODING));
                        int length = bytes.length;
                        if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
                            long toRemove = length;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getData()).insert(dataPosition, bytes);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + length);
                        caret.setCodeOffset(0);
                        updateScrollBars();
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
                        ((EditableBinaryData) codeArea.getData()).insert(caret.getDataPosition(), data);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + length);
                        caret.setCodeOffset(0);
                        updateScrollBars();
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
            ((SelectionCapable) codeArea.getWorker()).setSelection(0, dataSize - 1);
        }
    }

    @Override
    public void clearSelection() {
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        ((SelectionCapable) codeArea.getWorker()).setSelection(selection.getStart(), selection.getStart());
    }

    @Override
    public void wheelScroll(int scrollSize, ScrollingDirection direction) {
//        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//
//        if (e.isShiftDown() && codeArea.getPainter().isHorizontalScrollBarVisible()) {
//            if (e.getWheelRotation() > 0) {
//                if (codeArea.getBytesPerRectangle() < codeArea.getCharactersPerLine()) {
//                    int maxScroll = codeArea.getCharactersPerLine() - codeArea.getBytesPerRectangle();
//                    if (scrollPosition.getScrollCharPosition() < maxScroll - MOUSE_SCROLL_LINES) {
//                        scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() + MOUSE_SCROLL_LINES);
//                    } else {
//                        scrollPosition.setScrollCharPosition(maxScroll);
//                    }
//                    codeArea.getPainter().updateScrollBars();
//                    codeArea.notifyScrolled();
//                }
//            } else if (scrollPosition.getScrollCharPosition() > 0) {
//                if (scrollPosition.getScrollCharPosition() > MOUSE_SCROLL_LINES) {
//                    scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() - MOUSE_SCROLL_LINES);
//                } else {
//                    scrollPosition.setScrollCharPosition(0);
//                }
//                codeArea.getPainter().updateScrollBars();
//                codeArea.notifyScrolled();
//            }
//        } else if (e.getWheelRotation() > 0) {
//            long lines = (codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / codeArea.getBytesPerLine();
//            if (lines * codeArea.getBytesPerLine() < codeArea.getDataSize()) {
//                lines++;
//            }
//            lines -= codeArea.getLinesPerRectangle();
//            if (scrollPosition.getScrollLinePosition() < lines) {
//                if (scrollPosition.getScrollLinePosition() < lines - MOUSE_SCROLL_LINES) {
//                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + MOUSE_SCROLL_LINES);
//                } else {
//                    scrollPosition.setScrollLinePosition(lines);
//                }
//                codeArea.getPainter().updateScrollBars();
//                codeArea.notifyScrolled();
//            }
//        } else if (scrollPosition.getScrollLinePosition() > 0) {
//            if (scrollPosition.getScrollLinePosition() > MOUSE_SCROLL_LINES) {
//                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - MOUSE_SCROLL_LINES);
//            } else {
//                scrollPosition.setScrollLinePosition(0);
//            }
//            codeArea.getPainter().updateScrollBars();
//            codeArea.notifyScrolled();
//        }
    }

    public void updateSelection(boolean selecting, @Nonnull CaretPosition caretPosition) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (selecting) {
            ((SelectionCapable) codeArea.getWorker()).setSelection(selection.getStart(), caret.getDataPosition());

            /*            long currentPosition = caret.getDataPosition();
            long end = currentPosition;
            long start;
            if (!selection.isEmpty()) {
                start = selection.getStart();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    ((SelectionCapable) codeArea.getWorker()).setSelection(selection.getStart(), start < currentPosition ? end - 1 : end);
                }
            } else {
                start = caretPosition.getDataPosition();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    ((SelectionCapable) codeArea.getWorker()).setSelection(start, start < currentPosition ? end - 1 : end);
                }
            } */
        } else {
            ((SelectionCapable) codeArea.getWorker()).setSelection(caret.getDataPosition(), caret.getDataPosition());
        }
    }

    private void updateCanPaste() {
        canPaste = CodeAreaUtils.canPaste(clipboard, binaryDataFlavor);
    }

    @Override
    public void moveCaret(int positionX, int positionY, boolean selecting) {
        CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).mousePositionToClosestCaretPosition(positionX, positionY);
        if (caretPosition != null) {
            ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition);
            updateSelection(selecting, caretPosition);

            notifyCaretMoved();
            sequenceBreak();
            codeArea.repaint();
        }
    }

    public void move(int modifiers, @Nonnull MovementShift direction) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        CaretPosition movePosition = codeArea.getWorker().computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            caret.setCaretPosition(movePosition);
            updateSelection((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0, movePosition);
            notifyCaretMoved();
        }
    }

    public void scroll(@Nonnull ScrollingShift scrollingShift) {
        CodeAreaScrollPosition sourcePosition = ((ScrollingCapable) codeArea.getWorker()).getScrollPosition();
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) codeArea.getWorker()).computeScrolling(sourcePosition, scrollingShift);
        if (!sourcePosition.equals(scrollPosition)) {
            ((ScrollingCapable) codeArea.getWorker()).setScrollPosition(scrollPosition);
            updateScrollBars();
            notifyScrolled();
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
        codeArea.repaint();
    }

    private void notifyCaretMoved() {
        ((CaretCapable) codeArea.getWorker()).notifyCaretMoved();
    }

    private void notifyScrolled() {
        ((ScrollingCapable) codeArea.getWorker()).notifyScrolled();
    }
}
