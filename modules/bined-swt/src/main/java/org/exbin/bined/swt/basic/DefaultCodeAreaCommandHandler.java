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
package org.exbin.bined.swt.basic;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.swt.CodeAreaCommandHandler;
import org.exbin.bined.swt.CodeAreaCore;
import org.exbin.bined.swt.CodeAreaSwtUtils;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayEditableData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.basic.EnterKeyHandlingMode;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.2.0 2019/11/02
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int NO_MODIFIER = 0;
    public static final String FALLBACK_CLIPBOARD = "clipboard";
    public static final int LAST_CONTROL_CODE = 31;
    private static final char DELETE_CHAR = (char) 0x7f;

    private final int metaMask;

    @Nonnull
    private final CodeAreaCore codeArea;
    @Nonnull
    private EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binaryDataFlavor;
    private CodeAreaSwtUtils.ClipboardData currentClipboardData = null;

    public DefaultCodeAreaCommandHandler(CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        codeTypeSupported = codeArea instanceof CodeTypeCapable;
        viewModeSupported = codeArea instanceof ViewModeCapable;

        int metaMaskInit;
        try {
            metaMaskInit = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMaskInit = SWT.CONTROL;
        }
        this.metaMask = metaMaskInit;

        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (java.awt.HeadlessException ex) {
            // Create clipboard if system one not available
            clipboard = new Clipboard("clipboard");
        }
        clipboard.addFlavorListener((FlavorEvent e) -> {
            updateCanPaste();
        });
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

    @Nonnull
    public static CodeAreaCommandHandler.CodeAreaCommandHandlerFactory createDefaultCodeAreaCommandHandlerFactory() {
        return (@Nonnull CodeAreaCore codeArea1) -> new DefaultCodeAreaCommandHandler(codeArea1);
    }

    @Override
    public void undoSequenceBreak() {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
//        if (!codeArea.isEnabled()) {
//            return;
//        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        switch (keyEvent.keyCode) {
            case SWT.ARROW_LEFT: {
                move(keyEvent.stateMask, MovementDirection.LEFT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.ARROW_RIGHT: {
                move(keyEvent.stateMask, MovementDirection.RIGHT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.ARROW_UP: {
                move(keyEvent.stateMask, MovementDirection.UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.ARROW_DOWN: {
                move(keyEvent.stateMask, MovementDirection.DOWN);
                undoSequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.HOME: {
                if ((keyEvent.stateMask & SWT.CONTROL) > 0) {
                    move(keyEvent.stateMask, MovementDirection.DOC_START);
                } else {
                    move(keyEvent.stateMask, MovementDirection.ROW_START);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.END: {
                if ((keyEvent.stateMask & SWT.CONTROL) > 0) {
                    move(keyEvent.stateMask, MovementDirection.DOC_END);
                } else {
                    move(keyEvent.stateMask, MovementDirection.ROW_END);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(keyEvent.stateMask, MovementDirection.PAGE_UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(keyEvent.stateMask, MovementDirection.PAGE_DOWN);
                undoSequenceBreak();
                keyEvent.doit = false;
                break;
            }
            case SWT.INSERT: {
                EditationMode editationMode = ((EditationModeCapable) codeArea).getEditationMode();
                if (editationMode == EditationMode.EXPANDING || editationMode == EditationMode.CAPPED) {
                    EditationOperation editationOperation = ((EditationModeCapable) codeArea).getEditationOperation();
                    switch (editationOperation) {
                        case INSERT: {
                            ((EditationModeCapable) codeArea).setEditationOperation(EditationOperation.OVERWRITE);
                            keyEvent.doit = false;
                            break;
                        }
                        case OVERWRITE: {
                            ((EditationModeCapable) codeArea).setEditationOperation(EditationOperation.INSERT);
                            keyEvent.doit = false;
                            break;
                        }
                    }
                }
                break;
            }
            case SWT.TAB: {
                if (viewModeSupported && ((ViewModeCapable) codeArea).getViewMode() == CodeAreaViewMode.DUAL) {
                    move(keyEvent.stateMask, MovementDirection.SWITCH_SECTION);
                    undoSequenceBreak();
                    revealCursor();
                    keyEvent.doit = false;
                }
                break;
            }
            case SWT.CR: {
                enterPressed();
                keyEvent.doit = false;
                break;
            }
            case SWT.DEL: {
                deletePressed();
                keyEvent.doit = false;
                break;
            }
            case SWT.BS: {
                backSpacePressed();
                keyEvent.doit = false;
                break;
            }
            default: {
                if (((ClipboardCapable) codeArea).getClipboardHandlingMode() == ClipboardHandlingMode.PROCESS) {
                    if ((keyEvent.stateMask & metaMask) > 0 && keyEvent.character == 'c') {
                        copy();
                        keyEvent.doit = false;
                        break;
                    } else if ((keyEvent.stateMask & metaMask) > 0 && keyEvent.character == 'x') {
                        cut();
                        keyEvent.doit = false;
                        break;
                    } else if ((keyEvent.stateMask & metaMask) > 0 && keyEvent.character == 'v') {
                        paste();
                        keyEvent.doit = false;
                        break;
                    } else if ((keyEvent.stateMask & metaMask) > 0 && keyEvent.character == 'a') {
                        codeArea.selectAll();
                        keyEvent.doit = false;
                        break;
                    }
                }
            }
        }

        if (!keyEvent.doit) {
            return;
        }

        char keyValue = keyEvent.character;
        // TODO Add support for high unicode codes
        if (keyValue == java.awt.event.KeyEvent.CHAR_UNDEFINED) {
            return;
        }
        if (!checkEditationAllowed()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        if (caret.getSection() == BasicCodeAreaSection.CODE_MATRIX) {
            pressedCharAsCode(keyValue);
        } else {
            if (keyValue > LAST_CONTROL_CODE && keyValue != DELETE_CHAR) {
                pressedCharInPreview(keyValue);
            }
        }
        codeArea.repaint();
    }

    private void pressedCharAsCode(char keyChar) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        CodeType codeType = getCodeType();
        boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyChar, codeOffset, codeType);
        if (validKey) {
            EditationMode editationMode = ((EditationModeCapable) codeArea).getEditationMode();
            if (codeArea.hasSelection() && editationMode != EditationMode.INPLACE) {
                deleteSelection();
            }

            int value;
            if (keyChar >= '0' && keyChar <= '9') {
                value = keyChar - '0';
            } else {
                value = Character.toLowerCase(keyChar) - 'a' + 10;
            }

            BinaryData data = CodeAreaUtils.requireNonNull(codeArea.getContentData(), "Content data is null");
            EditationOperation editationOperation = ((EditationModeCapable) codeArea).getActiveOperation();
            if (editationMode == EditationMode.EXPANDING && editationOperation == EditationOperation.OVERWRITE) {
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
            move(NO_MODIFIER, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void pressedCharInPreview(char keyChar) {
        if (isValidChar(keyChar)) {
            EditationMode editationMode = ((EditationModeCapable) codeArea).getEditationMode();
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
            CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
            EditationOperation editationOperation = ((EditationModeCapable) codeArea).getActiveOperation();
            BinaryData data = CodeAreaUtils.requireNonNull(codeArea.getContentData(), "Content data is null");

            long dataPosition = caretPosition.getDataPosition();
            byte[] bytes = charToBytes(keyChar);
            if ((editationMode == EditationMode.EXPANDING && editationOperation == EditationOperation.OVERWRITE) || editationMode == EditationMode.INPLACE) {
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
            ((CaretCapable) codeArea).getCaret().setCaretPosition(dataPosition + bytes.length - 1);
            move(NO_MODIFIER, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void setCodeValue(int value) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getCaret().getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        BinaryData data = CodeAreaUtils.requireNonNull(codeArea.getContentData(), "Content data is null");
        CodeType codeType = getCodeType();
        byte byteValue = data.getByte(dataPosition);
        byte outputValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) data).setByte(dataPosition, outputValue);
    }

    @Override
    public void enterPressed() {
        if (!checkEditationAllowed()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        if (caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            String sequence = enterKeyHandlingMode.getSequence();
            if (!sequence.isEmpty()) {
                pressedCharInPreview(sequence.charAt(0));
                if (sequence.length() == 2) {
                    pressedCharInPreview(sequence.charAt(1));
                }
            }
        }
    }

    @Override
    public void backSpacePressed() {
        if (!checkEditationAllowed()) {
            return;
        }
        BinaryData data = CodeAreaUtils.requireNonNull(codeArea.getContentData(), "Content data is null");

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                move(NO_MODIFIER, MovementDirection.LEFT);
                caret.setCodeOffset(0);
                ((EditableBinaryData) data).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                revealCursor();
                updateScrollBars();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (!checkEditationAllowed()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
            updateScrollBars();
            notifyCaretMoved();
            revealCursor();
        } else {
            BinaryData data = CodeAreaUtils.requireNonNull(codeArea.getContentData(), "Content data is null");
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) data).remove(dataPosition, 1);
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
        BinaryData data = codeArea.getContentData();
        if (data == null) {
            return;
        }
        if (!(data instanceof EditableBinaryData)) {
            throw new IllegalStateException("Data is not editable");
        }

        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selection.isEmpty()) {
            return;
        }

        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) data).remove(first, last - first + 1);
        codeArea.clearSelection();
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        caret.setCaretPosition(first);
        revealCursor();
        updateScrollBars();
    }

    @Override
    public void delete() {
        if (!checkEditationAllowed()) {
            return;
        }

        deleteSelection();
        codeArea.notifyDataChanged();
    }

    @Override
    public void copy() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();
            if (data == null) {
                return;
            }

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            CodeAreaSwtUtils.BinaryDataClipboardData binaryData = new CodeAreaSwtUtils.BinaryDataClipboardData(copy, binaryDataFlavor);
            setClipboardContent(binaryData);
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();
            if (data == null) {
                return;
            }

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            CodeType codeType = ((CodeTypeCapable) codeArea).getCodeType();
            CodeCharactersCase charactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
            CodeAreaSwtUtils.CodeDataClipboardData binaryData = new CodeAreaSwtUtils.CodeDataClipboardData(copy, binaryDataFlavor, codeType, charactersCase);
            setClipboardContent(binaryData);
        }
    }

    private void setClipboardContent(CodeAreaSwtUtils.ClipboardData content) {
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
        if (!checkEditationAllowed()) {
            return;
        }

        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            copy();
            deleteSelection();
            codeArea.notifyDataChanged();
        }
    }

    @Override
    public void paste() {
        if (!checkEditationAllowed()) {
            return;
        }

        BinaryData data = CodeAreaUtils.requireNonNull(codeArea.getContentData(), "Content data is null");
        EditationMode editationMode = ((EditationModeCapable) codeArea).getEditationMode();
        EditationOperation editationOperation = ((EditationModeCapable) codeArea).getActiveOperation();
        try {
            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                try {
                    Object object = clipboard.getData(binaryDataFlavor);
                    if (object instanceof BinaryData) {
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
                        long dataPosition = caret.getDataPosition();

                        BinaryData clipboardData = (BinaryData) object;
                        long dataSize = clipboardData.getDataSize();
                        long toRemove = dataSize;
                        if ((editationMode == EditationMode.EXPANDING && editationOperation == EditationOperation.OVERWRITE) || editationMode == EditationMode.INPLACE) {
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) data).remove(dataPosition, toRemove);
                        }
                        if (editationMode == EditationMode.INPLACE && dataSize > toRemove) {
                            ((EditableBinaryData) data).insert(dataPosition, clipboardData, 0, toRemove);
                            codeArea.notifyDataChanged();
                            caret.setCaretPosition(caret.getDataPosition() + toRemove);
                        } else {
                            ((EditableBinaryData) data).insert(dataPosition, clipboardData);
                            codeArea.notifyDataChanged();
                            caret.setCaretPosition(caret.getDataPosition() + dataSize);
                        }

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
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
                        long dataPosition = caret.getDataPosition();

                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName(CodeAreaSwtUtils.DEFAULT_ENCODING));
                        int length = bytes.length;
                        long toRemove = length;
                        if ((editationMode == EditationMode.EXPANDING && editationOperation == EditationOperation.OVERWRITE) || editationMode == EditationMode.INPLACE) {
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) data).remove(dataPosition, toRemove);
                        }
                        if (editationMode == EditationMode.INPLACE && length > toRemove) {
                            ((EditableBinaryData) data).insert(dataPosition, bytes, 0, (int) toRemove);
                            codeArea.notifyDataChanged();
                            caret.setCaretPosition(caret.getDataPosition() + length);
                        } else {
                            ((EditableBinaryData) data).insert(dataPosition, bytes);
                            codeArea.notifyDataChanged();
                            caret.setCaretPosition(caret.getDataPosition() + length);
                        }

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
        if (!checkEditationAllowed()) {
            return;
        }

        try {
            if (!clipboard.isDataFlavorAvailable(binaryDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return;
            }
        } catch (IllegalStateException ex) {
            return;
        }

        BinaryData data = CodeAreaUtils.requireNonNull(codeArea.getContentData(), "Content data is null");
        EditationMode editationMode = ((EditationModeCapable) codeArea).getEditationMode();
        EditationOperation editationOperation = ((EditationModeCapable) codeArea).getActiveOperation();
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
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
                        long dataPosition = caret.getDataPosition();

                        CodeType codeType = getCodeType();
                        ByteArrayEditableData pastedData = new ByteArrayEditableData();
                        CodeAreaUtils.insertHexStringIntoData((String) insertedData, pastedData, codeType);

                        long length = pastedData.getDataSize();
                        long toRemove = length;
                        if ((editationMode == EditationMode.EXPANDING && editationOperation == EditationOperation.OVERWRITE) || editationMode == EditationMode.INPLACE) {
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) data).remove(dataPosition, toRemove);
                        }
                        if (editationMode == EditationMode.INPLACE && length > toRemove) {
                            ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData, 0, toRemove);
                            codeArea.notifyDataChanged();
                            caret.setCaretPosition(caret.getDataPosition() + toRemove);
                        } else {
                            ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData);
                            codeArea.notifyDataChanged();
                            caret.setCaretPosition(caret.getDataPosition() + length);
                        }

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
            ((SelectionCapable) codeArea).setSelection(0, dataSize);
        }
    }

    @Override
    public void clearSelection() {
        ((SelectionCapable) codeArea).clearSelection();
    }

    public void updateSelection(SelectingMode selecting, CodeAreaCaretPosition caretPosition) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selecting == SelectingMode.SELECTING) {
            ((SelectionCapable) codeArea).setSelection(selection.getStart(), caret.getDataPosition());
        } else {
            ((SelectionCapable) codeArea).setSelection(caret.getDataPosition(), caret.getDataPosition());
        }
    }

    private void updateCanPaste() {
        canPaste = CodeAreaSwtUtils.canPaste(clipboard, binaryDataFlavor);
    }

    @Override
    public void moveCaret(int positionX, int positionY, SelectingMode selecting) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).mousePositionToClosestCaretPosition(positionX, positionY, PositionOverflowMode.OVERFLOW);
        if (caretPosition != null) {
            ((CaretCapable) codeArea).getCaret().setCaretPosition(caretPosition);
            updateSelection(selecting, caretPosition);

            notifyCaretMoved();
            undoSequenceBreak();
            codeArea.repaint();
        }
    }

    public void move(int modifiers, MovementDirection direction) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        CodeAreaCaretPosition movePosition = ((CaretCapable) codeArea).computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            caret.setCaretPosition(movePosition);
            updateSelection((modifiers & SWT.SHIFT) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE, movePosition);
            notifyCaretMoved();
        }
    }

    public void scroll(ScrollingDirection direction) {
        CodeAreaScrollPosition sourcePosition = ((ScrollingCapable) codeArea).getScrollPosition();
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) codeArea).computeScrolling(sourcePosition, direction);
        if (!sourcePosition.equals(scrollPosition)) {
            ((ScrollingCapable) codeArea).setScrollPosition(scrollPosition);
            codeArea.resetPainter();
            notifyScrolled();
            updateScrollBars();
        }
    }

    @Override
    public void wheelScroll(int scrollSize, ScrollbarOrientation direction) {
        // TODO
        scroll(ScrollingDirection.UP);
    }

    public boolean isValidChar(char value) {
        return ((CharsetCapable) codeArea).getCharset().canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = ((CharsetCapable) codeArea).getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Nonnull
    private CodeType getCodeType() {
        if (codeTypeSupported) {
            return ((CodeTypeCapable) codeArea).getCodeType();
        }

        return CodeType.HEXADECIMAL;
    }

    private void revealCursor() {
        ((ScrollingCapable) codeArea).revealCursor();
        codeArea.repaint();
    }

    private void notifyCaretMoved() {
        ((CaretCapable) codeArea).notifyCaretMoved();
    }

    private void notifyScrolled() {
        ((ScrollingCapable) codeArea).notifyScrolled();
    }

    private void updateScrollBars() {
        ((ScrollingCapable) codeArea).updateScrollBars();
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean checkEditationAllowed() {
        return ((EditationModeCapable) codeArea).isEditable();
    }
}
