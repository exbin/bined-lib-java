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
package org.exbin.deltahex.operation;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.DefaultCodeAreaCommandHandler;
import org.exbin.deltahex.CodeArea;
import org.exbin.deltahex.CodeArea.Section;
import org.exbin.deltahex.CodeAreaCaret;
import org.exbin.deltahex.delta.MemoryPagedData;
import org.exbin.deltahex.operation.command.CodeAreaCommandType;
import org.exbin.deltahex.operation.command.EditCharDataCommand;
import org.exbin.deltahex.operation.command.EditDataCommand;
import org.exbin.deltahex.operation.command.EditCodeDataCommand;
import org.exbin.deltahex.operation.command.CodeAreaCommand;
import org.exbin.deltahex.operation.command.HexCompoundCommand;
import org.exbin.deltahex.operation.command.InsertDataCommand;
import org.exbin.deltahex.operation.command.ModifyDataCommand;
import org.exbin.deltahex.operation.command.RemoveDataCommand;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.xbup.operation.undo.XBUndoHandler;
import org.exbin.deltahex.CodeAreaCommandHandler;

/**
 * Command handler for undo/redo aware hexadecimal editor editing.
 *
 * @version 0.1.0 2016/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class CodeCommandHandler implements CodeAreaCommandHandler {

    public static final String MIME_CLIPBOARD_HEXADECIMAL = "application/x-deltahex";
    public static final String MIME_CLIPBOARD_BINARY = "application/octet-stream";
    private static final char BACKSPACE_CHAR = '\b';
    private static final char DELETE_CHAR = (char) 0x7f;

    private final CodeArea codeArea;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor appDataFlavor;

    private final XBUndoHandler undoHandler;
    private EditDataCommand editCommand = null;

    public CodeCommandHandler(CodeArea codeArea, XBUndoHandler undoHandler) {
        this.codeArea = codeArea;
        this.undoHandler = undoHandler;

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.addFlavorListener(new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                canPaste = clipboard.isDataFlavorAvailable(appDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
            }
        });
        try {
            appDataFlavor = new DataFlavor(MIME_CLIPBOARD_HEXADECIMAL);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        canPaste = clipboard.isDataFlavorAvailable(appDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
    }

    @Override
    public void caretMoved() {
        editCommand = null;
    }

    @Override
    public void keyPressed(char keyValue) {
        if (!codeArea.isEditable()) {
            return;
        }

        if (codeArea.getActiveSection() == Section.CODE_MATRIX) {
            if ((keyValue >= '0' && keyValue <= '9')
                    || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F')) {
                DeleteSelectionCommand deleteCommand = null;
                if (codeArea.hasSelection()) {
                    deleteCommand = new DeleteSelectionCommand(codeArea);
                }

                int value;
                if (keyValue >= '0' && keyValue <= '9') {
                    value = keyValue - '0';
                } else {
                    value = Character.toLowerCase(keyValue) - 'a' + 10;
                }

                if (editCommand != null && editCommand.wasReverted()) {
                    editCommand = null;
                }
                long dataPosition = codeArea.getDataPosition();
                if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                    if (editCommand == null || !(editCommand instanceof EditCodeDataCommand) || editCommand.getCommandType() != EditDataCommand.EditCommandType.OVERWRITE) {
                        editCommand = new EditCodeDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition, codeArea.getCodeOffset());
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCodeDataCommand) editCommand).appendEdit((byte) value);
                } else {
                    if (editCommand == null || !(editCommand instanceof EditCodeDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.INSERT) {
                        editCommand = new EditCodeDataCommand(codeArea, EditCharDataCommand.EditCommandType.INSERT, dataPosition, codeArea.getCodeOffset());
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCodeDataCommand) editCommand).appendEdit((byte) value);
                }
                codeArea.moveRight(CodeArea.NO_MODIFIER);
                codeArea.revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && codeArea.isValidChar(keyValue)) {
                CaretPosition caretPosition = codeArea.getCaretPosition();

                if (editCommand != null && editCommand.wasReverted()) {
                    editCommand = null;
                }
                long dataPosition = caretPosition.getDataPosition();
                DeleteSelectionCommand deleteCommand = null;
                if (codeArea.hasSelection()) {
                    deleteCommand = new DeleteSelectionCommand(codeArea);
                }

                if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                    if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.OVERWRITE) {
                        editCommand = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition);
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCharDataCommand) editCommand).appendEdit(keyChar);
                } else {
                    if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.INSERT) {
                        editCommand = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.INSERT, dataPosition);
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCharDataCommand) editCommand).appendEdit(keyChar);
                }
                codeArea.revealCursor();
                codeArea.repaint();
            }
        }
    }

    @Override
    public void backSpacePressed() {
        if (!codeArea.isEditable()) {
            return;
        }

        deletingAction(BACKSPACE_CHAR);
    }

    @Override
    public void deletePressed() {
        if (!codeArea.isEditable()) {
            return;
        }

        deletingAction(DELETE_CHAR);
    }

    private void deletingAction(char keyChar) {
        if (codeArea.hasSelection()) {
            DeleteSelectionCommand deleteCommand = new DeleteSelectionCommand(codeArea);
            try {
                undoHandler.execute(deleteCommand);
            } catch (Exception ex) {
                Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (editCommand != null && editCommand.wasReverted()) {
                editCommand = null;
            }

            long dataPosition = codeArea.getDataPosition();
            if (codeArea.getActiveSection() == Section.CODE_MATRIX) {
                if (editCommand == null || !(editCommand instanceof EditCodeDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.DELETE) {
                    editCommand = new EditCodeDataCommand(codeArea, EditCodeDataCommand.EditCommandType.DELETE, dataPosition, 0);
                    undoHandler.addCommand(editCommand);
                }

                ((EditCodeDataCommand) editCommand).appendEdit((byte) keyChar);
            } else {
                if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.DELETE) {
                    editCommand = new EditCharDataCommand(codeArea, EditCharDataCommand.EditCommandType.DELETE, dataPosition);
                    undoHandler.addCommand(editCommand);
                }

                ((EditCharDataCommand) editCommand).appendEdit(keyChar);
            }
        }
    }

    @Override
    public void delete() {
        if (!codeArea.isEditable()) {
            return;
        }

        try {
            undoHandler.execute(new DeleteSelectionCommand(codeArea));
        } catch (Exception ex) {
            Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void copy() {
        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection != null) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = ((EditableBinaryData) codeArea.getData()).copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy);
            clipboard.setContents(binaryData, binaryData);
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
            try {
                undoHandler.execute(new DeleteSelectionCommand(codeArea));
            } catch (Exception ex) {
                Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void paste() {
        if (!codeArea.isEditable()) {
            return;
        }

        DeleteSelectionCommand deleteSelectionCommand = null;
        if (codeArea.hasSelection()) {
            try {
                deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
            } catch (Exception ex) {
                Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (clipboard.isDataFlavorAvailable(appDataFlavor)) {
            try {
                Object object = clipboard.getData(appDataFlavor);
                if (object instanceof BinaryData) {
                    CodeAreaCaret caret = codeArea.getCaret();
                    long dataPosition = caret.getDataPosition();

                    CodeAreaCommand modifyCommand = null;
                    BinaryData pastedData = (BinaryData) object;
                    long dataSize = pastedData.getDataSize();
                    long insertionPosition = dataPosition;
                    if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                        BinaryData modifiedData = pastedData;
                        long toReplace = dataSize;
                        if (insertionPosition + toReplace > codeArea.getData().getDataSize()) {
                            toReplace = codeArea.getData().getDataSize() - insertionPosition;
                            modifiedData = pastedData.copy(0, toReplace);
                        }
                        if (toReplace > 0) {
                            modifyCommand = new ModifyDataCommand(codeArea, dataPosition, modifiedData);
                            pastedData = pastedData.copy(toReplace, pastedData.getDataSize() - toReplace);
                            insertionPosition += toReplace;
                        }
                    }

                    CodeAreaCommand insertCommand = null;
                    if (pastedData.getDataSize() > 0) {
                        insertCommand = new InsertDataCommand(codeArea, insertionPosition, pastedData);
                    }

                    CodeAreaCommand pasteCommand = HexCompoundCommand.buildCompoundCommand(codeArea, deleteSelectionCommand, modifyCommand, insertCommand);
                    try {
                        undoHandler.execute(pasteCommand);
                    } catch (Exception ex) {
                        Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    codeArea.computeDimensions();
                    codeArea.updateScrollBars();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            Object insertedData;
            try {
                insertedData = clipboard.getData(DataFlavor.stringFlavor);
                if (insertedData instanceof String) {
                    CodeAreaCaret caret = codeArea.getCaret();
                    long dataPosition = caret.getDataPosition();

                    CodeAreaCommand modifyCommand = null;
                    byte[] bytes = ((String) insertedData).getBytes(codeArea.getCharset());
                    int dataSize = bytes.length;
                    BinaryData pastedData = new MemoryPagedData(bytes);
                    long insertionPosition = dataPosition;
                    if (codeArea.getEditationMode() == CodeArea.EditationMode.OVERWRITE) {
                        BinaryData modifiedData = pastedData;
                        long toReplace = dataSize;
                        if (insertionPosition + toReplace > codeArea.getData().getDataSize()) {
                            toReplace = codeArea.getData().getDataSize() - insertionPosition;
                            modifiedData = pastedData.copy(0, toReplace);
                        }
                        if (toReplace > 0) {
                            modifyCommand = new ModifyDataCommand(codeArea, dataPosition, modifiedData);
                            pastedData = pastedData.copy(toReplace, pastedData.getDataSize() - toReplace);
                            insertionPosition += toReplace;
                        }
                    }

                    CodeAreaCommand insertCommand = null;
                    if (pastedData.getDataSize() > 0) {
                        insertCommand = new InsertDataCommand(codeArea, insertionPosition, pastedData);
                    }

                    CodeAreaCommand pasteCommand = HexCompoundCommand.buildCompoundCommand(codeArea, deleteSelectionCommand, modifyCommand, insertCommand);
                    try {
                        undoHandler.execute(pasteCommand);
                    } catch (Exception ex) {
                        Logger.getLogger(CodeCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    codeArea.computeDimensions();
                    codeArea.updateScrollBars();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
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
            return new DataFlavor[]{appDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(appDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(appDataFlavor)) {
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

    private static class DeleteSelectionCommand extends CodeAreaCommand {

        private final RemoveDataCommand removeCommand;
        private final long position;
        private final long size;

        public DeleteSelectionCommand(CodeArea coreArea) {
            super(coreArea);
            CodeArea.SelectionRange selection = coreArea.getSelection();
            position = selection.getFirst();
            size = selection.getLast() - position + 1;
            removeCommand = new RemoveDataCommand(coreArea, position, 0, size);
        }

        @Override
        public void execute() throws Exception {
            super.execute();
        }

        @Override
        public void redo() throws Exception {
            removeCommand.redo();
            codeArea.clearSelection();
            CodeAreaCaret caret = codeArea.getCaret();
            caret.setCaretPosition(position);
            codeArea.revealCursor();
            codeArea.computeDimensions();
            codeArea.updateScrollBars();
        }

        @Override
        public void undo() throws Exception {
            removeCommand.undo();
            codeArea.clearSelection();
            CodeAreaCaret caret = codeArea.getCaret();
            caret.setCaretPosition(size);
            codeArea.revealCursor();
            codeArea.computeDimensions();
            codeArea.updateScrollBars();
        }

        @Override
        public CodeAreaCommandType getType() {
            return CodeAreaCommandType.DATA_REMOVED;
        }

        @Override
        public boolean canUndo() {
            return true;
        }
    }
}
