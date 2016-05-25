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
import org.exbin.deltahex.HexadecimalCommandHandler;
import org.exbin.deltahex.DefaultCommandHandler;
import org.exbin.deltahex.Hexadecimal;
import org.exbin.deltahex.HexadecimalCaret;
import org.exbin.deltahex.delta.MemoryHexadecimalData;
import org.exbin.deltahex.operation.command.HexCommandType;
import org.exbin.deltahex.operation.command.EditCharDataCommand;
import org.exbin.deltahex.operation.command.EditDataCommand;
import org.exbin.deltahex.operation.command.EditHexDataCommand;
import org.exbin.deltahex.operation.command.HexCommand;
import org.exbin.deltahex.operation.command.HexCompoundCommand;
import org.exbin.deltahex.operation.command.InsertDataCommand;
import org.exbin.deltahex.operation.command.ModifyDataCommand;
import org.exbin.deltahex.operation.command.RemoveDataCommand;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.xbup.operation.undo.XBUndoHandler;

/**
 * Command handler for undo/redo aware hexadecimal editor editing.
 *
 * @version 0.1.0 2016/05/17
 * @author ExBin Project (http://exbin.org)
 */
public class HexCommandHandler implements HexadecimalCommandHandler {

    public static final String MIME_CLIPBOARD_HEXADECIMAL = "application/x-deltahex";
    public static final String MIME_CLIPBOARD_BINARY = "application/octet-stream";
    private static final char BACKSPACE_CHAR = '\b';
    private static final char DELETE_CHAR = (char) 0x7f;

    private final Hexadecimal hexadecimal;
    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor appDataFlavor;

    private final XBUndoHandler undoHandler;
    private EditDataCommand editCommand = null;

    public HexCommandHandler(Hexadecimal hexadecimal, XBUndoHandler undoHandler) {
        this.hexadecimal = hexadecimal;
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
            Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        canPaste = clipboard.isDataFlavorAvailable(appDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
    }

    @Override
    public void caretMoved() {
        editCommand = null;
    }

    @Override
    public void keyPressed(char keyValue) {
        if (!hexadecimal.isEditable()) {
            return;
        }

        if (hexadecimal.getActiveSection() == HexadecimalCaret.Section.HEXADECIMAL) {
            if ((keyValue >= '0' && keyValue <= '9')
                    || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F')) {
                DeleteSelectionCommand deleteCommand = null;
                if (hexadecimal.hasSelection()) {
                    deleteCommand = new DeleteSelectionCommand(hexadecimal);
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
                long dataPosition = hexadecimal.getDataPosition();
                if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                    if (editCommand == null || !(editCommand instanceof EditHexDataCommand) || editCommand.getCommandType() != EditDataCommand.EditCommandType.OVERWRITE) {
                        editCommand = new EditHexDataCommand(hexadecimal, EditHexDataCommand.EditCommandType.OVERWRITE, dataPosition, hexadecimal.isLowerHalf());
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(hexadecimal);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditHexDataCommand) editCommand).appendEdit((byte) value);
                } else {
                    if (editCommand == null || !(editCommand instanceof EditHexDataCommand) || editCommand.getCommandType() != EditHexDataCommand.EditCommandType.INSERT) {
                        editCommand = new EditHexDataCommand(hexadecimal, EditCharDataCommand.EditCommandType.INSERT, dataPosition, hexadecimal.isLowerHalf());
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(hexadecimal);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditHexDataCommand) editCommand).appendEdit((byte) value);
                }
                hexadecimal.moveRight(Hexadecimal.NO_MODIFIER);
                hexadecimal.revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && hexadecimal.isValidChar(keyValue)) {
                CaretPosition caretPosition = hexadecimal.getCaretPosition();

                if (editCommand != null && editCommand.wasReverted()) {
                    editCommand = null;
                }
                long dataPosition = caretPosition.getDataPosition();
                DeleteSelectionCommand deleteCommand = null;
                if (hexadecimal.hasSelection()) {
                    deleteCommand = new DeleteSelectionCommand(hexadecimal);
                }

                if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                    if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditHexDataCommand.EditCommandType.OVERWRITE) {
                        editCommand = new EditCharDataCommand(hexadecimal, EditHexDataCommand.EditCommandType.OVERWRITE, dataPosition);
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(hexadecimal);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCharDataCommand) editCommand).appendEdit(keyChar);
                } else {
                    if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditHexDataCommand.EditCommandType.INSERT) {
                        editCommand = new EditCharDataCommand(hexadecimal, EditHexDataCommand.EditCommandType.INSERT, dataPosition);
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(hexadecimal);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (Exception ex) {
                                Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCharDataCommand) editCommand).appendEdit(keyChar);
                }
                hexadecimal.revealCursor();
                hexadecimal.repaint();
            }
        }
    }

    @Override
    public void backSpacePressed() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        deletingAction(BACKSPACE_CHAR);
    }

    @Override
    public void deletePressed() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        deletingAction(DELETE_CHAR);
    }

    private void deletingAction(char keyChar) {
        if (hexadecimal.hasSelection()) {
            DeleteSelectionCommand deleteCommand = new DeleteSelectionCommand(hexadecimal);
            try {
                undoHandler.execute(deleteCommand);
            } catch (Exception ex) {
                Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (editCommand != null && editCommand.wasReverted()) {
                editCommand = null;
            }

            long dataPosition = hexadecimal.getDataPosition();
            if (hexadecimal.getActiveSection() == HexadecimalCaret.Section.HEXADECIMAL) {
                if (editCommand == null || !(editCommand instanceof EditHexDataCommand) || editCommand.getCommandType() != EditHexDataCommand.EditCommandType.DELETE) {
                    editCommand = new EditHexDataCommand(hexadecimal, EditHexDataCommand.EditCommandType.DELETE, dataPosition, false);
                    undoHandler.addCommand(editCommand);
                }

                ((EditHexDataCommand) editCommand).appendEdit((byte) keyChar);
            } else {
                if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditHexDataCommand.EditCommandType.DELETE) {
                    editCommand = new EditCharDataCommand(hexadecimal, EditCharDataCommand.EditCommandType.DELETE, dataPosition);
                    undoHandler.addCommand(editCommand);
                }

                ((EditCharDataCommand) editCommand).appendEdit(keyChar);
            }
        }
    }

    @Override
    public void delete() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        try {
            undoHandler.execute(new DeleteSelectionCommand(hexadecimal));
        } catch (Exception ex) {
            Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            try {
                undoHandler.execute(new DeleteSelectionCommand(hexadecimal));
            } catch (Exception ex) {
                Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void paste() {
        if (!hexadecimal.isEditable()) {
            return;
        }

        DeleteSelectionCommand deleteSelectionCommand = null;
        if (hexadecimal.hasSelection()) {
            try {
                deleteSelectionCommand = new DeleteSelectionCommand(hexadecimal);
            } catch (Exception ex) {
                Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (clipboard.isDataFlavorAvailable(appDataFlavor)) {
            try {
                Object object = clipboard.getData(appDataFlavor);
                if (object instanceof BinaryData) {
                    HexadecimalCaret caret = hexadecimal.getCaret();
                    long dataPosition = caret.getDataPosition();

                    HexCommand modifyCommand = null;
                    BinaryData pastedData = (BinaryData) object;
                    long dataSize = pastedData.getDataSize();
                    long insertionPosition = dataPosition;
                    if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                        BinaryData modifiedData = pastedData;
                        long toReplace = dataSize;
                        if (insertionPosition + toReplace > hexadecimal.getData().getDataSize()) {
                            toReplace = hexadecimal.getData().getDataSize() - insertionPosition;
                            modifiedData = pastedData.copy(0, toReplace);
                        }
                        if (toReplace > 0) {
                            modifyCommand = new ModifyDataCommand(hexadecimal, dataPosition, modifiedData);
                            pastedData = pastedData.copy(toReplace, pastedData.getDataSize() - toReplace);
                            insertionPosition += toReplace;
                        }
                    }

                    HexCommand insertCommand = null;
                    if (pastedData.getDataSize() > 0) {
                        insertCommand = new InsertDataCommand(hexadecimal, insertionPosition, pastedData);
                    }

                    HexCommand pasteCommand = HexCompoundCommand.buildCompoundCommand(hexadecimal, deleteSelectionCommand, modifyCommand, insertCommand);
                    try {
                        undoHandler.execute(pasteCommand);
                    } catch (Exception ex) {
                        Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    hexadecimal.computeDimensions();
                    hexadecimal.updateScrollBars();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(DefaultCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            Object insertedData;
            try {
                insertedData = clipboard.getData(DataFlavor.stringFlavor);
                if (insertedData instanceof String) {
                    HexadecimalCaret caret = hexadecimal.getCaret();
                    long dataPosition = caret.getDataPosition();

                    HexCommand modifyCommand = null;
                    byte[] bytes = ((String) insertedData).getBytes(hexadecimal.getCharset());
                    int dataSize = bytes.length;
                    BinaryData pastedData = new MemoryHexadecimalData(bytes);
                    long insertionPosition = dataPosition;
                    if (hexadecimal.getEditationMode() == Hexadecimal.EditationMode.OVERWRITE) {
                        BinaryData modifiedData = pastedData;
                        long toReplace = dataSize;
                        if (insertionPosition + toReplace > hexadecimal.getData().getDataSize()) {
                            toReplace = hexadecimal.getData().getDataSize() - insertionPosition;
                            modifiedData = pastedData.copy(0, toReplace);
                        }
                        if (toReplace > 0) {
                            modifyCommand = new ModifyDataCommand(hexadecimal, dataPosition, modifiedData);
                            pastedData = pastedData.copy(toReplace, pastedData.getDataSize() - toReplace);
                            insertionPosition += toReplace;
                        }
                    }

                    HexCommand insertCommand = null;
                    if (pastedData.getDataSize() > 0) {
                        insertCommand = new InsertDataCommand(hexadecimal, insertionPosition, pastedData);
                    }

                    HexCommand pasteCommand = HexCompoundCommand.buildCompoundCommand(hexadecimal, deleteSelectionCommand, modifyCommand, insertCommand);
                    try {
                        undoHandler.execute(pasteCommand);
                    } catch (Exception ex) {
                        Logger.getLogger(HexCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }

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

    private static class DeleteSelectionCommand extends HexCommand {

        private final RemoveDataCommand removeCommand;
        private final long position;
        private final long size;

        public DeleteSelectionCommand(Hexadecimal hexadecimal) {
            super(hexadecimal);
            Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
            position = selection.getFirst();
            size = selection.getLast() - position + 1;
            removeCommand = new RemoveDataCommand(hexadecimal, position, false, size);
        }

        @Override
        public void execute() throws Exception {
            super.execute();
        }

        @Override
        public void redo() throws Exception {
            removeCommand.redo();
            hexadecimal.clearSelection();
            HexadecimalCaret caret = hexadecimal.getCaret();
            caret.setCaretPosition(position);
            hexadecimal.revealCursor();
            hexadecimal.computeDimensions();
            hexadecimal.updateScrollBars();
        }

        @Override
        public void undo() throws Exception {
            removeCommand.undo();
            hexadecimal.clearSelection();
            HexadecimalCaret caret = hexadecimal.getCaret();
            caret.setCaretPosition(size);
            hexadecimal.revealCursor();
            hexadecimal.computeDimensions();
            hexadecimal.updateScrollBars();
        }

        @Override
        public HexCommandType getType() {
            return HexCommandType.DATA_REMOVED;
        }

        @Override
        public boolean canUndo() {
            return true;
        }
    }
}
