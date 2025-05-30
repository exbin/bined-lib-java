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
package org.exbin.bined.swt.basic;

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
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.swt.CodeAreaCommandHandler;
import org.exbin.bined.swt.CodeAreaCore;
import org.exbin.bined.swt.CodeAreaSwtUtils;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.ScrollBarOrientation;
import org.exbin.bined.basic.EnterKeyHandlingMode;
import org.exbin.bined.basic.SelectingMode;
import org.exbin.bined.capability.EditModeCapable;

/**
 * Default binary editor command handler.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int LAST_CONTROL_CODE = 31;
    protected static final char DELETE_CHAR = (char) 0x7f;

    private final int metaMask = CodeAreaSwtUtils.getMetaMask();

    @Nonnull
    protected final CodeAreaCore codeArea;
    @Nonnull
    protected EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    protected final boolean codeTypeSupported;
    protected final boolean viewModeSupported;

    protected Clipboard clipboard;
    protected boolean canPaste = false;
    protected DataFlavor binaryDataFlavor;
    protected CodeAreaSwtUtils.ClipboardData currentClipboardData = null;

    public DefaultCodeAreaCommandHandler(CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        codeTypeSupported = codeArea instanceof CodeTypeCapable;
        viewModeSupported = codeArea instanceof ViewModeCapable;

        clipboard = CodeAreaSwtUtils.getClipboard();
        try {
            try {
                binaryDataFlavor = new DataFlavor(CodeAreaUtils.MIME_CLIPBOARD_BINARY);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            updateCanPaste();
            clipboard.addFlavorListener((FlavorEvent e) -> {
                updateCanPaste();
            });
        } catch (IllegalStateException ex) {
            canPaste = false;
        } catch (java.awt.HeadlessException ex) {
            Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Nonnull
    public static CodeAreaCommandHandler.CodeAreaCommandHandlerFactory createDefaultCodeAreaCommandHandlerFactory() {
        return DefaultCodeAreaCommandHandler::new;
    }

    @Override
    public void sequenceBreak() {
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
                move(isSelectingMode(keyEvent), MovementDirection.LEFT);
                sequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.ARROW_RIGHT: {
                move(isSelectingMode(keyEvent), MovementDirection.RIGHT);
                sequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.ARROW_UP: {
                move(isSelectingMode(keyEvent), MovementDirection.UP);
                sequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.ARROW_DOWN: {
                move(isSelectingMode(keyEvent), MovementDirection.DOWN);
                sequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.HOME: {
                if ((keyEvent.stateMask & metaMask) > 0) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_START);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_START);
                }
                sequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.END: {
                if ((keyEvent.stateMask & metaMask) > 0) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_END);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_END);
                }
                sequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_UP);
                sequenceBreak();
                revealCursor();
                keyEvent.doit = false;
                break;
            }
            case SWT.PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_DOWN);
                sequenceBreak();
                keyEvent.doit = false;
                break;
            }
            case SWT.INSERT: {
                EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
                if (editMode == EditMode.EXPANDING || editMode == EditMode.CAPPED) {
                    EditOperation editOperation = ((EditModeCapable) codeArea).getEditOperation();
                    switch (editOperation) {
                        case INSERT: {
                            ((EditModeCapable) codeArea).setEditOperation(EditOperation.OVERWRITE);
                            keyEvent.doit = false;
                            break;
                        }
                        case OVERWRITE: {
                            ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);
                            keyEvent.doit = false;
                            break;
                        }
                    }
                }
                break;
            }
            case SWT.TAB: {
                if (viewModeSupported && ((ViewModeCapable) codeArea).getViewMode() == CodeAreaViewMode.DUAL) {
                    move(isSelectingMode(keyEvent), MovementDirection.SWITCH_SECTION);
                    sequenceBreak();
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
        if (!checkEditAllowed()) {
            return;
        }

        if (((CaretCapable) codeArea).getActiveSection() == BasicCodeAreaSection.CODE_MATRIX) {
            pressedCharAsCode(keyValue);
        } else {
            if (keyValue > LAST_CONTROL_CODE && keyValue != DELETE_CHAR) {
                pressedCharInPreview(keyValue);
            }
        }
        codeArea.repaint();
    }

    private void pressedCharAsCode(char keyChar) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        CodeType codeType = getCodeType();
        boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyChar, codeOffset, codeType);
        if (validKey) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                deleteSelection();
                sequenceBreak();
            }

            int value;
            if (keyChar >= '0' && keyChar <= '9') {
                value = keyChar - '0';
            } else {
                value = Character.toLowerCase(keyChar) - 'a' + 10;
            }

            BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
            EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.INSERT) {
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
                            throw CodeAreaUtils.getInvalidTypeException(codeType);
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
            } else {
                if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE && dataPosition == codeArea.getDataSize()) {
                    ((EditableBinaryData) data).insert(dataPosition, 1);
                }
                if (editMode != EditMode.INPLACE || dataPosition < codeArea.getDataSize()) {
                    setCodeValue(value);
                }
            }
            codeArea.notifyDataChanged();
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void pressedCharInPreview(char keyChar) {
        if (isValidChar(keyChar)) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();

            long dataPosition = caretPosition.getDataPosition();
            byte[] bytes = charToBytes(keyChar);
            if (editMode == EditMode.INPLACE) {
                int length = bytes.length;
                if (dataPosition + length > codeArea.getDataSize()) {
                    return;
                }
            }
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                sequenceBreak();
                deleteSelection();
            }

            BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
            EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
            if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
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
            ((CaretCapable) codeArea).getCodeAreaCaret().setCaretPosition(dataPosition + bytes.length - 1);
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void setCodeValue(int value) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
        CodeType codeType = getCodeType();
        byte byteValue = data.getByte(dataPosition);
        byte outputValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) data).setByte(dataPosition, outputValue);
    }

    @Override
    public void enterPressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (((CaretCapable) codeArea).getActiveSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
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
        if (!checkEditAllowed()) {
            return;
        }
        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
            long dataPosition = ((CaretCapable) codeArea).getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                move(SelectingMode.NONE, MovementDirection.LEFT);
                caret.setCodeOffset(0);
                ((EditableBinaryData) data).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
                revealCursor();
                clearSelection();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
            revealCursor();
        } else {
            BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) data).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
                clearSelection();
                revealCursor();
            }
        }
    }

    private void deleteSelection() {
        BinaryData data = codeArea.getContentData();
        if (!(data instanceof EditableBinaryData)) {
            throw new IllegalStateException("Data is not editable");
        }

        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selection.isEmpty()) {
            return;
        }

        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        long first = selection.getFirst();
        long last = selection.getLast();
        long length = last - first + 1;
        if (editMode == EditMode.INPLACE) {
            ((EditableBinaryData) data).fillData(first, length);
        } else {
            ((EditableBinaryData) data).remove(first, length);
        }
        ((CaretCapable) codeArea).setActiveCaretPosition(first);
        clearSelection();
        revealCursor();
    }

    @Override
    public void delete() {
        if (!checkEditAllowed()) {
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
        if (!checkEditAllowed()) {
            return;
        }

        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            copy();
            if (editMode == EditMode.EXPANDING) {
                deleteSelection();
                codeArea.notifyDataChanged();
            }
        }
    }

    @Override
    public void paste() {
        if (!checkEditAllowed()) {
            return;
        }

        BinaryData data = codeArea.getContentData();
        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
        try {
            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                try {
                    Object object = clipboard.getData(binaryDataFlavor);
                    if (object instanceof BinaryData) {
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
                        long dataPosition = caret.getDataPosition();

                        BinaryData clipboardData = (BinaryData) object;
                        long dataSize = clipboardData.getDataSize();
                        long toRemove = dataSize;
                        if (editMode == EditMode.INPLACE) {
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) data).replace(dataPosition, clipboardData, 0, toRemove);
                        } else {
                            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) {
                                if (dataPosition + toRemove > codeArea.getDataSize()) {
                                    toRemove = codeArea.getDataSize() - dataPosition;
                                }
                                ((EditableBinaryData) data).remove(dataPosition, toRemove);
                            }

                            ((EditableBinaryData) data).insert(dataPosition, clipboardData);
                            caret.setCaretPosition(caret.getDataPosition() + dataSize);
                            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                        }

                        caret.setCodeOffset(0);
                        ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
                        sequenceBreak();
                        codeArea.notifyDataChanged();
                        revealCursor();
                        clearSelection();
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
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
                        long dataPosition = caret.getDataPosition();

                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName(CodeAreaSwtUtils.DEFAULT_ENCODING));
                        int length = bytes.length;
                        long toRemove = length;
                        if (editMode == EditMode.INPLACE) {
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) data).replace(dataPosition, bytes, 0, (int) toRemove);
                        } else {
                            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) {
                                if (dataPosition + toRemove > codeArea.getDataSize()) {
                                    toRemove = codeArea.getDataSize() - dataPosition;
                                }
                                ((EditableBinaryData) data).remove(dataPosition, toRemove);
                            }

                            ((EditableBinaryData) data).insert(dataPosition, bytes);
                            caret.setCaretPosition(caret.getDataPosition() + length);
                            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                        }

                        caret.setCodeOffset(0);
                        ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
                        sequenceBreak();
                        codeArea.notifyDataChanged();
                        revealCursor();
                        clearSelection();
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
        if (!checkEditAllowed()) {
            return;
        }

        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
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
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
                        long dataPosition = caret.getDataPosition();

                        CodeType codeType = getCodeType();
                        ByteArrayEditableData pastedData = new ByteArrayEditableData();
                        CodeAreaUtils.insertHexStringIntoData((String) insertedData, pastedData, codeType);

                        long length = pastedData.getDataSize();
                        long toRemove = length;
                        if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) data).remove(dataPosition, toRemove);
                        }
                        if (editMode == EditMode.INPLACE && length > toRemove) {
                            ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData, 0, toRemove);
                            caret.setCaretPosition(caret.getDataPosition() + toRemove);
                            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                        } else {
                            ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData);
                            caret.setCaretPosition(caret.getDataPosition() + length);
                            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                        }

                        caret.setCodeOffset(0);
                        ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
                        sequenceBreak();
                        codeArea.notifyDataChanged();
                        revealCursor();
                        clearSelection();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class
                            .getName()).log(Level.SEVERE, null, ex);
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

    @Nonnull
    public EnterKeyHandlingMode getEnterKeyHandlingMode() {
        return enterKeyHandlingMode;
    }

    public void setEnterKeyHandlingMode(EnterKeyHandlingMode enterKeyHandlingMode) {
        this.enterKeyHandlingMode = enterKeyHandlingMode;
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
        long dataPosition = ((CaretCapable) codeArea).getActiveCaretPosition().getDataPosition();
        ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);
    }

    public void updateSelection(SelectingMode selectingMode, CodeAreaCaretPosition caretPosition) {
        long dataPosition = ((CaretCapable) codeArea).getDataPosition();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selectingMode == SelectingMode.SELECTING) {
            ((SelectionCapable) codeArea).setSelection(selection.getStart(), dataPosition);
        } else {
            ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);
        }
    }

    private void updateCanPaste() {
        canPaste = CodeAreaSwtUtils.canPaste(clipboard, binaryDataFlavor);
    }

    @Override
    public void moveCaret(int positionX, int positionY, SelectingMode selecting) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).mousePositionToClosestCaretPosition(positionX, positionY, CaretOverlapMode.PARTIAL_OVERLAP);
        ((CaretCapable) codeArea).setActiveCaretPosition(caretPosition);
        updateSelection(selecting, caretPosition);

        sequenceBreak();
        codeArea.repaint();
    }

    public void move(SelectingMode selectingMode, MovementDirection direction) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        CodeAreaCaretPosition movePosition = ((CaretCapable) codeArea).computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            ((CaretCapable) codeArea).setActiveCaretPosition(movePosition);
            updateSelection(selectingMode, movePosition);
        }
    }

    public void scroll(ScrollingDirection direction) {
        CodeAreaScrollPosition sourcePosition = ((ScrollingCapable) codeArea).getScrollPosition();
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) codeArea).computeScrolling(sourcePosition, direction);
        if (!sourcePosition.equals(scrollPosition)) {
            ((ScrollingCapable) codeArea).setScrollPosition(scrollPosition);
            codeArea.resetPainter();
        }
    }

    @Override
    public void wheelScroll(int scrollSize, ScrollBarOrientation direction) {
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

    @Override
    public void dispose() {
    }

    @Override
    public boolean checkEditAllowed() {
        return codeArea.isEditable();
    }

    @Nonnull
    private static SelectingMode isSelectingMode(KeyEvent keyEvent) {
        return (keyEvent.stateMask & SWT.SHIFT) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE;
    }
}
