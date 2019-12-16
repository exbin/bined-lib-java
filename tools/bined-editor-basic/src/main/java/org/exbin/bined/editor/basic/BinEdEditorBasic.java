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
package org.exbin.bined.editor.basic;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.FlavorEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;

/**
 * Basic single jar swing version of BinEd Hexadecimal editor.
 *
 * @version 0.2.0 2019/09/25
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdEditorBasic extends javax.swing.JFrame {

    private static final String APPLICATION_VERSION = "0.2.0 DEV";
    private static final String APPLICATION_NAME = "BinEd";
    private static final String APPLICATION_DEFAULT_TITLE = APPLICATION_NAME + " Basic Editor";

    private static final String ICONS_DIRECTORY = "/org/exbin/bined/editor/basic/resources/icons/";
    private static final String ICON_APP = ICONS_DIRECTORY + "icon.png";
    private static final String ICON_FILE_NEW = ICONS_DIRECTORY + "document-new.png";
    private static final String ICON_FILE_OPEN = ICONS_DIRECTORY + "document-open.png";
    private static final String ICON_FILE_SAVE = ICONS_DIRECTORY + "document-save.png";
    private static final String ICON_FILE_SAVE_AS = ICONS_DIRECTORY + "document-save-as.png";
    private static final String ICON_EDIT_UNDO = ICONS_DIRECTORY + "edit-undo.png";
    private static final String ICON_EDIT_REDO = ICONS_DIRECTORY + "edit-redo.png";
    private static final String ICON_EDIT_CUT = ICONS_DIRECTORY + "edit-cut.png";
    private static final String ICON_EDIT_COPY = ICONS_DIRECTORY + "edit-copy.png";
    private static final String ICON_EDIT_PASTE = ICONS_DIRECTORY + "edit-paste.png";
    private static final String ICON_EDIT_DELETE = ICONS_DIRECTORY + "edit-delete.png";
    private static final String ICON_EDIT_SELECT_ALL = ICONS_DIRECTORY + "edit-select-all.png";

    private File file = null;
    private CodeArea codeArea;
    private CodeAreaUndoHandler undoHandler;
    private CodeAreaCommandHandler commandHandler;

    private Action newFileAction;
    private Action openFileAction;
    private Action saveFileAction;
    private Action saveAsFileAction;
    private Action exitAction;
    private Action undoEditAction;
    private Action redoEditAction;
    private Action cutEditAction;
    private Action copyEditAction;
    private Action pasteEditAction;
    private Action deleteEditAction;
    private Action selectAllAction;

    public BinEdEditorBasic() {
        init();
        initActions();
        initComponents();
        postInit();
    }

    private void init() {
        codeArea = new CodeArea();
        codeArea.setContentData(new ByteArrayEditableData());
        undoHandler = new CodeAreaUndoHandler(codeArea);
        commandHandler = new CodeAreaOperationCommandHandler(codeArea, undoHandler);
        codeArea.setCommandHandler(commandHandler);
        add(codeArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!releaseFile()) {
                    return;
                }

                System.exit(0);
            }
        });
    }

    private void initActions() {
        newFileAction = new AbstractAction(
                "New",
                new javax.swing.ImageIcon(getClass().getResource(ICON_FILE_NEW))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                newFileActionPerformed();
            }
        };
        newFileAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));

        openFileAction = new AbstractAction(
                "Open...",
                new javax.swing.ImageIcon(getClass().getResource(ICON_FILE_OPEN))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                openFileActionPerformed();
            }
        };
        openFileAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));

        saveFileAction = new AbstractAction(
                "Save",
                new javax.swing.ImageIcon(getClass().getResource(ICON_FILE_SAVE))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                saveFileActionPerformed();
            }
        };

        saveAsFileAction = new AbstractAction(
                "Save As...",
                new javax.swing.ImageIcon(getClass().getResource(ICON_FILE_SAVE_AS))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                saveAsFileActionPerformed();
            }
        };
        saveAsFileAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));

        exitAction = new AbstractAction(
                "Exit",
                null
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                exitActionPerformed();
            }
        };
        exitAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));

        undoEditAction = new AbstractAction(
                "Undo",
                new javax.swing.ImageIcon(getClass().getResource(ICON_EDIT_UNDO))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    undoHandler.performUndo();
                } catch (BinaryDataOperationException ex) {
                    Logger.getLogger(BinEdEditorBasic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        undoEditAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));

        redoEditAction = new AbstractAction(
                "Redo",
                new javax.swing.ImageIcon(getClass().getResource(ICON_EDIT_REDO))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    undoHandler.performRedo();
                } catch (BinaryDataOperationException ex) {
                    Logger.getLogger(BinEdEditorBasic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        redoEditAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));

        cutEditAction = new AbstractAction(
                "Cut",
                new javax.swing.ImageIcon(getClass().getResource(ICON_EDIT_CUT))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                codeArea.cut();
            }
        };
        cutEditAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));

        copyEditAction = new AbstractAction(
                "copy",
                new javax.swing.ImageIcon(getClass().getResource(ICON_EDIT_COPY))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                codeArea.copy();
            }
        };
        copyEditAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));

        pasteEditAction = new AbstractAction(
                "Paste",
                new javax.swing.ImageIcon(getClass().getResource(ICON_EDIT_PASTE))) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                codeArea.paste();
            }
        };
        pasteEditAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));

        deleteEditAction = new AbstractAction(
                "Delete",
                new javax.swing.ImageIcon(getClass().getResource(ICON_EDIT_DELETE))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                codeArea.delete();
            }
        };
        deleteEditAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));

        selectAllAction = new AbstractAction(
                "Select All",
                new javax.swing.ImageIcon(getClass().getResource(ICON_EDIT_SELECT_ALL))
        ) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                codeArea.selectAll();
            }
        };
        selectAllAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
    }

    private void postInit() {
        codeArea.setComponentPopupMenu(mainPopupMenu);
        setIconImage(new javax.swing.ImageIcon(getClass().getResource(ICON_APP)).getImage());
        undoHandler.addUndoUpdateListener(new BinaryDataUndoUpdateListener() {
            @Override
            public void undoCommandPositionChanged() {
                updateUndoState();
                codeArea.repaint();
            }

            @Override
            public void undoCommandAdded(@Nonnull BinaryDataCommand command) {
                updateUndoState();
                codeArea.repaint();
            }
        });
        ((EditationModeCapable) codeArea).addEditationModeChangedListener((EditationMode editationMode, EditationOperation editationOperation) -> {
            switch (editationOperation) {
                case INSERT: {
                    editationModeLabel.setText("INS");
                    break;
                }
                case OVERWRITE: {
                    editationModeLabel.setText("OVR");
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected editation mode " + editationOperation.name());
                }
            }
        });
        ((CaretCapable) codeArea).addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            positionLabel.setText(caretPosition.getDataPosition() + ":" + caretPosition.getCodeOffset());
        });
        ((SelectionCapable) codeArea).addSelectionChangedListener((SelectionRange selection) -> {
            updateClipboardState();
        });
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.addFlavorListener((FlavorEvent e) -> {
            updateClipboardState();
        });
        updateUndoState();
        updateClipboardState();
        openFileButton.setText("Open");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPopupMenu = new javax.swing.JPopupMenu();
        editUndoPopupMenuItem = new javax.swing.JMenuItem();
        editRedoPopupMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        editCutPopupMenuItem = new javax.swing.JMenuItem();
        editCopyPopupMenuItem = new javax.swing.JMenuItem();
        editPastePopupMenuItem = new javax.swing.JMenuItem();
        editDeletePopupMenuItem = new javax.swing.JMenuItem();
        selectAllPopupMenuItem = new javax.swing.JMenuItem();
        codeTypeButtonGroup = new javax.swing.ButtonGroup();
        hexCharacterCaseButtonGroup = new javax.swing.ButtonGroup();
        mainToolBar = new javax.swing.JToolBar();
        newFileButton = new javax.swing.JButton();
        openFileButton = new javax.swing.JButton();
        saveFileButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        undoEditButton = new javax.swing.JButton();
        redoEditButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        cutEditButton = new javax.swing.JButton();
        copyEditButton = new javax.swing.JButton();
        pasteEditButton = new javax.swing.JButton();
        deleteEditButton = new javax.swing.JButton();
        statusBarPanel = new javax.swing.JPanel();
        statusBarSeparator = new javax.swing.JSeparator();
        textStatusPanel = new javax.swing.JPanel();
        editationModeLabel = new javax.swing.JLabel();
        positionLabel = new javax.swing.JLabel();
        encodingLabel = new javax.swing.JLabel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newFileMenuItem = new javax.swing.JMenuItem();
        openFileMenuItem = new javax.swing.JMenuItem();
        saveFileMenuItem = new javax.swing.JMenuItem();
        saveAsFileMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        editUndoMenuItem = new javax.swing.JMenuItem();
        editRedoMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        editCutMenuItem = new javax.swing.JMenuItem();
        editCopyMenuItem = new javax.swing.JMenuItem();
        editPasteMenuItem = new javax.swing.JMenuItem();
        editDeleteMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        codeTypeMenu = new javax.swing.JMenu();
        binaryCodeTypeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        octalCodeTypeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        decimalCodeTypeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        hexadecimalCodeTypeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        hexCharacterCaseMenu = new javax.swing.JMenu();
        upperCaseRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        lowerCaseRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        chooseEncodingMenuItem = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        editUndoPopupMenuItem.setAction(undoEditAction);
        editUndoPopupMenuItem.setText("Undo");
        mainPopupMenu.add(editUndoPopupMenuItem);

        editRedoPopupMenuItem.setAction(redoEditAction);
        editRedoPopupMenuItem.setText("Redo");
        mainPopupMenu.add(editRedoPopupMenuItem);
        mainPopupMenu.add(jSeparator5);

        editCutPopupMenuItem.setAction(cutEditAction);
        editCutPopupMenuItem.setText("Cut");
        mainPopupMenu.add(editCutPopupMenuItem);

        editCopyPopupMenuItem.setAction(copyEditAction);
        editCopyPopupMenuItem.setText("Copy");
        mainPopupMenu.add(editCopyPopupMenuItem);

        editPastePopupMenuItem.setAction(pasteEditAction);
        editPastePopupMenuItem.setText("Paste");
        mainPopupMenu.add(editPastePopupMenuItem);

        editDeletePopupMenuItem.setAction(deleteEditAction);
        editDeletePopupMenuItem.setText("Delete");
        mainPopupMenu.add(editDeletePopupMenuItem);

        selectAllPopupMenuItem.setAction(selectAllAction);
        selectAllPopupMenuItem.setText("Select All");
        mainPopupMenu.add(selectAllPopupMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(APPLICATION_DEFAULT_TITLE);

        mainToolBar.setRollover(true);

        newFileButton.setAction(newFileAction);
        newFileButton.setFocusable(false);
        newFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(newFileButton);

        openFileButton.setAction(openFileAction);
        openFileButton.setFocusable(false);
        openFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(openFileButton);

        saveFileButton.setAction(saveFileAction);
        saveFileButton.setFocusable(false);
        saveFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(saveFileButton);
        mainToolBar.add(jSeparator3);

        undoEditButton.setAction(undoEditAction);
        undoEditButton.setFocusable(false);
        undoEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        undoEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(undoEditButton);

        redoEditButton.setAction(redoEditAction);
        redoEditButton.setFocusable(false);
        redoEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        redoEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(redoEditButton);
        mainToolBar.add(jSeparator4);

        cutEditButton.setAction(cutEditAction);
        cutEditButton.setFocusable(false);
        cutEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cutEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(cutEditButton);

        copyEditButton.setAction(copyEditAction);
        copyEditButton.setFocusable(false);
        copyEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        copyEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(copyEditButton);

        pasteEditButton.setAction(pasteEditAction);
        pasteEditButton.setFocusable(false);
        pasteEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pasteEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(pasteEditButton);

        deleteEditButton.setAction(deleteEditAction);
        deleteEditButton.setFocusable(false);
        deleteEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(deleteEditButton);

        getContentPane().add(mainToolBar, java.awt.BorderLayout.NORTH);

        statusBarPanel.setLayout(new java.awt.BorderLayout());

        statusBarSeparator.setAutoscrolls(true);
        statusBarPanel.add(statusBarSeparator, java.awt.BorderLayout.PAGE_START);

        editationModeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        editationModeLabel.setText("OVR");
        editationModeLabel.setToolTipText("Current editation mode");
        editationModeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        editationModeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editationModeLabelMouseClicked(evt);
            }
        });

        positionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        positionLabel.setText("0:0");
        positionLabel.setToolTipText("Current cursor position");
        positionLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        encodingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        encodingLabel.setText("UTF-8");
        encodingLabel.setToolTipText("Currently active encoding");
        encodingLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        encodingLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encodingLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout textStatusPanelLayout = new javax.swing.GroupLayout(textStatusPanel);
        textStatusPanel.setLayout(textStatusPanelLayout);
        textStatusPanelLayout.setHorizontalGroup(
            textStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, textStatusPanelLayout.createSequentialGroup()
                .addGap(0, 354, Short.MAX_VALUE)
                .addComponent(encodingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(positionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(editationModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        textStatusPanelLayout.setVerticalGroup(
            textStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editationModeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(positionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(encodingLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        statusBarPanel.add(textStatusPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(statusBarPanel, java.awt.BorderLayout.PAGE_END);

        fileMenu.setText("File");

        newFileMenuItem.setAction(newFileAction);
        newFileMenuItem.setText("New");
        fileMenu.add(newFileMenuItem);

        openFileMenuItem.setAction(openFileAction);
        openFileMenuItem.setText("Open...");
        fileMenu.add(openFileMenuItem);

        saveFileMenuItem.setAction(saveFileAction);
        saveFileMenuItem.setText("Save");
        fileMenu.add(saveFileMenuItem);

        saveAsFileMenuItem.setAction(saveAsFileAction);
        saveAsFileMenuItem.setText("Save As...");
        fileMenu.add(saveAsFileMenuItem);
        fileMenu.add(jSeparator1);

        exitMenuItem.setAction(exitAction);
        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        mainMenuBar.add(fileMenu);

        editMenu.setText("Edit");

        editUndoMenuItem.setAction(undoEditAction);
        editUndoMenuItem.setText("Undo");
        editMenu.add(editUndoMenuItem);

        editRedoMenuItem.setAction(redoEditAction);
        editRedoMenuItem.setText("Redo");
        editMenu.add(editRedoMenuItem);
        editMenu.add(jSeparator2);

        editCutMenuItem.setAction(cutEditAction);
        editCutMenuItem.setText("Cut");
        editMenu.add(editCutMenuItem);

        editCopyMenuItem.setAction(copyEditAction);
        editCopyMenuItem.setText("Copy");
        editMenu.add(editCopyMenuItem);

        editPasteMenuItem.setAction(pasteEditAction);
        editPasteMenuItem.setText("Paste");
        editMenu.add(editPasteMenuItem);

        editDeleteMenuItem.setAction(deleteEditAction);
        editDeleteMenuItem.setText("Delete");
        editMenu.add(editDeleteMenuItem);

        selectAllMenuItem.setAction(selectAllAction);
        selectAllMenuItem.setText("Select All");
        editMenu.add(selectAllMenuItem);

        mainMenuBar.add(editMenu);

        viewMenu.setText("View");

        codeTypeMenu.setText("Code Type");

        codeTypeButtonGroup.add(binaryCodeTypeRadioButtonMenuItem);
        binaryCodeTypeRadioButtonMenuItem.setText("Binary");
        binaryCodeTypeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                binaryCodeTypeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        codeTypeMenu.add(binaryCodeTypeRadioButtonMenuItem);

        codeTypeButtonGroup.add(octalCodeTypeRadioButtonMenuItem);
        octalCodeTypeRadioButtonMenuItem.setText("Octal");
        octalCodeTypeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                octalCodeTypeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        codeTypeMenu.add(octalCodeTypeRadioButtonMenuItem);

        codeTypeButtonGroup.add(decimalCodeTypeRadioButtonMenuItem);
        decimalCodeTypeRadioButtonMenuItem.setText("Decimal");
        decimalCodeTypeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decimalCodeTypeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        codeTypeMenu.add(decimalCodeTypeRadioButtonMenuItem);

        codeTypeButtonGroup.add(hexadecimalCodeTypeRadioButtonMenuItem);
        hexadecimalCodeTypeRadioButtonMenuItem.setSelected(true);
        hexadecimalCodeTypeRadioButtonMenuItem.setText("Hexadecimal");
        hexadecimalCodeTypeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexadecimalCodeTypeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        codeTypeMenu.add(hexadecimalCodeTypeRadioButtonMenuItem);

        viewMenu.add(codeTypeMenu);

        hexCharacterCaseMenu.setText("Hex Character Case");

        hexCharacterCaseButtonGroup.add(upperCaseRadioButtonMenuItem);
        upperCaseRadioButtonMenuItem.setSelected(true);
        upperCaseRadioButtonMenuItem.setText("Upper Case");
        upperCaseRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upperCaseRadioButtonMenuItemActionPerformed(evt);
            }
        });
        hexCharacterCaseMenu.add(upperCaseRadioButtonMenuItem);

        hexCharacterCaseButtonGroup.add(lowerCaseRadioButtonMenuItem);
        lowerCaseRadioButtonMenuItem.setText("Lower Case");
        lowerCaseRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowerCaseRadioButtonMenuItemActionPerformed(evt);
            }
        });
        hexCharacterCaseMenu.add(lowerCaseRadioButtonMenuItem);

        viewMenu.add(hexCharacterCaseMenu);

        chooseEncodingMenuItem.setText("Choose Encoding...");
        chooseEncodingMenuItem.setToolTipText("");
        chooseEncodingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseEncodingMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(chooseEncodingMenuItem);

        mainMenuBar.add(viewMenu);

        aboutMenu.setText("About");

        aboutMenuItem.setText("About...");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        aboutMenu.add(aboutMenuItem);

        mainMenuBar.add(aboutMenu);

        setJMenuBar(mainMenuBar);

        setSize(new java.awt.Dimension(715, 481));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(this,
                APPLICATION_NAME + " Hexadecimal Editor - Basic Editor\nVersion " + APPLICATION_VERSION + "\nhttp://bined.exbin.org",
                "About application",
                JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void editationModeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editationModeLabelMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1) {
            EditationOperation editationOperation;
            if (((EditationModeCapable) codeArea).getEditationOperation() == EditationOperation.INSERT) {
                editationOperation = EditationOperation.OVERWRITE;
            } else {
                editationOperation = EditationOperation.INSERT;
            }
            ((EditationModeCapable) codeArea).setEditationOperation(editationOperation);
        }
    }//GEN-LAST:event_editationModeLabelMouseClicked

    private void encodingLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encodingLabelMouseClicked
        chooseEncoding();
    }//GEN-LAST:event_encodingLabelMouseClicked

    private void chooseEncodingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseEncodingMenuItemActionPerformed
        chooseEncoding();
    }//GEN-LAST:event_chooseEncodingMenuItemActionPerformed

    private void binaryCodeTypeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_binaryCodeTypeRadioButtonMenuItemActionPerformed
        codeArea.setCodeType(CodeType.BINARY);
    }//GEN-LAST:event_binaryCodeTypeRadioButtonMenuItemActionPerformed

    private void octalCodeTypeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_octalCodeTypeRadioButtonMenuItemActionPerformed
        codeArea.setCodeType(CodeType.OCTAL);
    }//GEN-LAST:event_octalCodeTypeRadioButtonMenuItemActionPerformed

    private void decimalCodeTypeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decimalCodeTypeRadioButtonMenuItemActionPerformed
        codeArea.setCodeType(CodeType.DECIMAL);
    }//GEN-LAST:event_decimalCodeTypeRadioButtonMenuItemActionPerformed

    private void hexadecimalCodeTypeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexadecimalCodeTypeRadioButtonMenuItemActionPerformed
        codeArea.setCodeType(CodeType.HEXADECIMAL);
    }//GEN-LAST:event_hexadecimalCodeTypeRadioButtonMenuItemActionPerformed

    private void upperCaseRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upperCaseRadioButtonMenuItemActionPerformed
        codeArea.setCodeCharactersCase(CodeCharactersCase.UPPER);
    }//GEN-LAST:event_upperCaseRadioButtonMenuItemActionPerformed

    private void lowerCaseRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowerCaseRadioButtonMenuItemActionPerformed
        codeArea.setCodeCharactersCase(CodeCharactersCase.LOWER);
    }//GEN-LAST:event_lowerCaseRadioButtonMenuItemActionPerformed

    private void newFileActionPerformed() {
        if (!releaseFile()) {
            return;
        }

        Objects.requireNonNull((EditableBinaryData) codeArea.getContentData()).clear();
        codeArea.notifyDataChanged();
        codeArea.repaint();
        undoHandler.clear();
        updateUndoState();
        updateClipboardState();
        file = null;
        setTitle(APPLICATION_DEFAULT_TITLE);
    }

    private void openFileActionPerformed() {
        if (releaseFile()) {
            JFileChooser fileChooser = new JFileChooser();
            int chooserResult = fileChooser.showOpenDialog(this);
            if (chooserResult == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                try (FileInputStream stream = new FileInputStream(file)) {
                    Objects.requireNonNull((EditableBinaryData) codeArea.getContentData()).loadFromStream(stream);
                    codeArea.notifyDataChanged();
                    codeArea.repaint();
                    undoHandler.clear();
                    updateUndoState();
                    updateClipboardState();
                    updateTitle();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(BinEdEditorBasic.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(BinEdEditorBasic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void saveFileActionPerformed() {
        if (file != null) {
            saveAsFileActionPerformed();
        } else {
            saveToFile();
        }
    }

    private void saveAsFileActionPerformed() {
        JFileChooser fileChooser = new JFileChooser();
        int chooserResult = fileChooser.showSaveDialog(this);
        if (chooserResult == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (file.exists()) {
                if (!overwriteFile()) {
                    return;
                }
            }

            saveToFile();
        }
    }

    /**
     * Asks whether it's allowed to overwrite file.
     *
     * @return true if allowed
     */
    private boolean overwriteFile() {
        Object[] options = {"Overwrite", "Cancel"};

        int result = JOptionPane.showOptionDialog(
                this,
                "This file aready exists! Do you wish to overwrite it?",
                "Overwrite File?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (result == JOptionPane.YES_OPTION) {
            return true;
        }
        if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return false;
        }

        return false;
    }

    private void exitActionPerformed() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public void saveToFile() {
        if (file == null) {
            saveAsFileActionPerformed();
        } else {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                Objects.requireNonNull((EditableBinaryData) codeArea.getContentData()).saveToStream(stream);
                undoHandler.setSyncPoint();
                updateTitle();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BinEdEditorBasic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BinEdEditorBasic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean releaseFile() {
        while (isModified()) {
            Object[] options = {"Save", "Discard", "Cancel"};
            int result = JOptionPane.showOptionDialog(this,
                    "Document was modified! Do you wish to save it?",
                    "Save File?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (result == JOptionPane.NO_OPTION) {
                return true;
            }
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return false;
            }

            saveAsFileActionPerformed();
        }

        return true;
    }

    private void chooseEncoding() {
        EncodingSelectionDialog dialog = new EncodingSelectionDialog(this, true);
        dialog.setEncoding(((CharsetCapable) codeArea).getCharset().name());
        dialog.setVisible(true);
        if (dialog.getReturnStatus() == EncodingSelectionDialog.ReturnStatus.OK) {
            String encoding = dialog.getEncoding();
            if (encoding != null) {
                ((CharsetCapable) codeArea).setCharset(Charset.forName(encoding));
                codeArea.repaint();
                encodingLabel.setText(encoding);
            }
        }
    }

    private void updateUndoState() {
        undoEditAction.setEnabled(undoHandler.canUndo());
        redoEditAction.setEnabled(undoHandler.canRedo());
    }

    private void updateClipboardState() {
        cutEditAction.setEnabled(codeArea.hasSelection());
        copyEditAction.setEnabled(codeArea.hasSelection());
        deleteEditAction.setEnabled(codeArea.hasSelection());
        pasteEditAction.setEnabled(codeArea.canPaste());
    }

    private void updateTitle() {
        setTitle(APPLICATION_NAME + " - " + file.getName());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        switchLookAndFeel();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new BinEdEditorBasic().setVisible(true);
        });
    }

    private static void switchLookAndFeel() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.startsWith("windows") && !osName.startsWith("mac")) {
            // Try "GTK+" on linux
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                return;
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            }
        }

        // Try system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(BinEdEditorBasic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isModified() {
        return undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JRadioButtonMenuItem binaryCodeTypeRadioButtonMenuItem;
    private javax.swing.JMenuItem chooseEncodingMenuItem;
    private javax.swing.ButtonGroup codeTypeButtonGroup;
    private javax.swing.JMenu codeTypeMenu;
    private javax.swing.JButton copyEditButton;
    private javax.swing.JButton cutEditButton;
    private javax.swing.JRadioButtonMenuItem decimalCodeTypeRadioButtonMenuItem;
    private javax.swing.JButton deleteEditButton;
    private javax.swing.JMenuItem editCopyMenuItem;
    private javax.swing.JMenuItem editCopyPopupMenuItem;
    private javax.swing.JMenuItem editCutMenuItem;
    private javax.swing.JMenuItem editCutPopupMenuItem;
    private javax.swing.JMenuItem editDeleteMenuItem;
    private javax.swing.JMenuItem editDeletePopupMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editPasteMenuItem;
    private javax.swing.JMenuItem editPastePopupMenuItem;
    private javax.swing.JMenuItem editRedoMenuItem;
    private javax.swing.JMenuItem editRedoPopupMenuItem;
    private javax.swing.JMenuItem editUndoMenuItem;
    private javax.swing.JMenuItem editUndoPopupMenuItem;
    private javax.swing.JLabel editationModeLabel;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.ButtonGroup hexCharacterCaseButtonGroup;
    private javax.swing.JMenu hexCharacterCaseMenu;
    private javax.swing.JRadioButtonMenuItem hexadecimalCodeTypeRadioButtonMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JRadioButtonMenuItem lowerCaseRadioButtonMenuItem;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPopupMenu mainPopupMenu;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JButton newFileButton;
    private javax.swing.JMenuItem newFileMenuItem;
    private javax.swing.JRadioButtonMenuItem octalCodeTypeRadioButtonMenuItem;
    private javax.swing.JButton openFileButton;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JButton pasteEditButton;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JButton redoEditButton;
    private javax.swing.JMenuItem saveAsFileMenuItem;
    private javax.swing.JButton saveFileButton;
    private javax.swing.JMenuItem saveFileMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem selectAllPopupMenuItem;
    private javax.swing.JPanel statusBarPanel;
    private javax.swing.JSeparator statusBarSeparator;
    private javax.swing.JPanel textStatusPanel;
    private javax.swing.JButton undoEditButton;
    private javax.swing.JRadioButtonMenuItem upperCaseRadioButtonMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

}
