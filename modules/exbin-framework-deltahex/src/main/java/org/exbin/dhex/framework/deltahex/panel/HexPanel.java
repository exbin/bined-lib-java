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
package org.exbin.dhex.framework.deltahex.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.exbin.dhex.deltahex.EditableHexadecimalData;
import org.exbin.dhex.deltahex.HexadecimalData;
import org.exbin.xbup.core.block.declaration.local.XBLFormatDecl;
import org.exbin.xbup.core.parser.XBProcessingException;
import org.exbin.xbup.core.serial.XBPSerialReader;
import org.exbin.dhex.deltahex.component.Hexadecimal;
import org.exbin.dhex.framework.deltahex.dialog.FindHexDialog;
import org.exbin.framework.editor.text.dialog.TextFontDialog;
import org.exbin.framework.editor.text.panel.TextEncodingPanel;
import org.exbin.framework.gui.editor.api.XBEditorProvider;
import org.exbin.framework.gui.file.api.FileType;
import org.exbin.framework.gui.menu.api.ClipboardActionsUpdateListener;
import org.exbin.framework.gui.undo.api.UndoUpdateListener;
import org.exbin.framework.gui.menu.api.ClipboardActionsHandler;
import org.exbin.framework.gui.undo.api.UndoActionsHandler;
import org.exbin.dhex.framework.deltahex.HexStatusApi;
import org.exbin.dhex.framework.deltahex.XBHexadecimalData;
import org.exbin.xbup.core.type.XBData;

/**
 * Hexadecimal editor panel.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class HexPanel extends javax.swing.JPanel implements XBEditorProvider, ClipboardActionsHandler, UndoActionsHandler {

    private Hexadecimal hexadecimal;
    private final HexPanelCompoundUndoManager undoManagement = new HexPanelCompoundUndoManager();
    private UndoUpdateListener undoUpdateListener = null;
    private String fileName;
    private FileType fileType;
    private boolean modified = false;
    private Object highlight;
    private Color foundTextBackgroundColor;
    private Charset charset;
    private Font defaultFont;
    private Color[] defaultColors;
    private PropertyChangeListener propertyChangeListener;
    private CharsetChangeListener charsetChangeListener = null;
    private HexStatusApi textStatus = null;
    private ClipboardActionsUpdateListener clipboardActionsUpdateListener;

    public HexPanel() {
        initComponents();
        init();
    }

    private void init() {
        hexadecimal = new Hexadecimal();
        hexadecimal.setData(new XBHexadecimalData(new XBData()));
        hexadecimal.addSelectionChangedListener(new Hexadecimal.SelectionChangedListener() {
            @Override
            public void selectionChanged(Hexadecimal.SelectionRange selection) {
                if (clipboardActionsUpdateListener != null) {
                    clipboardActionsUpdateListener.stateChanged();
                }
            }
        });
        // TODO use listener in hexadecimal instead
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.addFlavorListener(new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                if (clipboardActionsUpdateListener != null) {
                    clipboardActionsUpdateListener.stateChanged();
                }
            }
        });
        clipboardActionsUpdateListener.stateChanged();
        
        textAreaScrollPane.setViewportView(hexadecimal);
        fileName = "";
        highlight = null;
        foundTextBackgroundColor = Color.YELLOW;
        charset = Charset.forName(TextEncodingPanel.ENCODING_UTF8);
        defaultFont = hexadecimal.getFont();
        defaultColors = new Color[5];
        defaultColors[0] = new Color(hexadecimal.getForeground().getRGB());
        defaultColors[1] = new Color(SystemColor.text.getRGB()); // Patch on wrong value in textArea.getBackground()
        defaultColors[2] = new Color(hexadecimal.getSelectionColor().getRGB());
        defaultColors[3] = new Color(hexadecimal.getSelectionBackgroundColor().getRGB());
        defaultColors[4] = foundTextBackgroundColor;

        // if the document is ever edited, assume that it needs to be saved
//        textArea.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                setModified(true);
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                setModified(true);
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                setModified(true);
//            }
//        });

        // Listener for undoManagement and redo events
//        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
//            @Override
//            public void undoableEditHappened(UndoableEditEvent evt) {
//                undoManagement.undoableEditHappened(evt);
//
//                if (undoUpdateListener != null) {
//                    undoUpdateListener.undoChanged();
//                }
//            }
//        });

        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (propertyChangeListener != null) {
                    propertyChangeListener.propertyChange(evt);
                }
            }
        });
    }

    public boolean changeLineWrap() {
        return false;
//        textArea.setLineWrap(!textArea.getLineWrap());
//        return textArea.getLineWrap();
    }

    public boolean getWordWrapMode() {
        return false;
//        return textArea.getLineWrap();
    }

    public void setWordWrapMode(boolean mode) {
//        if (textArea.getLineWrap() != mode) {
//            changeLineWrap();
//        }
    }

    public void findText(FindHexDialog dialog) {
//        String text = textArea.getText();
//        int pos = textArea.getCaretPosition();
//        if (highlight != null) {
//            if (((Highlight) highlight).getStartOffset() == pos) {
//                pos++;
//            }
//            textArea.getHighlighter().removeHighlight(highlight);
//        } else if (dialog.getSearchFromStart()) {
//            pos = 0;
//        }
//        String findText = dialog.getFindText();
//        pos = text.indexOf(findText, pos);
//        if (pos >= 0) {
//            try {
//                int toPos;
//                if (dialog.getShallReplace()) {
//                    String replaceText = dialog.getReplaceText();
//                    textArea.replaceRange(replaceText, pos, pos + findText.length());
//                    toPos = pos + replaceText.length();
//                } else {
//                    toPos = pos + findText.length();
//                }
//                highlight = textArea.getHighlighter().addHighlight(pos, toPos, new DefaultHighlighter.DefaultHighlightPainter(foundTextBackgroundColor));
//                textArea.setCaretPosition(pos);
//                return;
//            } catch (BadLocationException ex) {
//                Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        JOptionPane.showMessageDialog(null, "String was not found", "Find text", JOptionPane.INFORMATION_MESSAGE); // getFrame
//        highlight = null;
    }

    public Color[] getCurrentColors() {
        Color[] colors = new Color[5];
        colors[0] = hexadecimal.getForeground();
        colors[1] = hexadecimal.getBackground();
        colors[2] = hexadecimal.getSelectionColor();
        colors[3] = hexadecimal.getSelectionBackgroundColor();
        colors[4] = getFoundTextBackgroundColor();
        return colors;
    }

    public Color[] getDefaultColors() {
        return defaultColors;
    }

    public void setCurrentColors(Color[] colors) {
        if (colors[0] != null) {
            hexadecimal.setForeground(colors[0]);
        }
        if (colors[1] != null) {
            hexadecimal.setBackground(colors[1]);
        }
        if (colors[2] != null) {
            hexadecimal.setSelectionColor(colors[2]);
        }
        if (colors[3] != null) {
            hexadecimal.setSelectionBackgroundColor(colors[3]);
        }
        if (colors[4] != null) {
            setFoundTextBackgroundColor(colors[4]);
        }
    }

    public Document getDocument() {
        return null; // textArea.getDocument();
    }

    public int getLineCount() {
        return 0; // textArea.getLineCount();
    }

    public String getText() {
        return ""; // textArea.getText();
    }

    public void setNoBorder() {
        textAreaScrollPane.setBorder(null);
    }

    public void gotoLine(int line) {
//        try {
//            textArea.setCaretPosition(textArea.getLineStartOffset(line - 1));
//        } catch (BadLocationException ex) {
//            Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void gotoRelative(int charPos) {
//        textArea.setCaretPosition(textArea.getCaretPosition() + charPos - 1);
    }

    @Override
    public void performCopy() {
        hexadecimal.copy();
    }

    @Override
    public void performCut() {
        hexadecimal.cut();
    }

    @Override
    public void performDelete() {
        hexadecimal.delete();
    }

    @Override
    public void performPaste() {
        hexadecimal.paste();
    }

    @Override
    public void performSelectAll() {
        hexadecimal.selectAll();
    }

    @Override
    public void performUndoManager() {
        // TODO
    }

    @Override
    public boolean isSelection() {
        return hexadecimal.hasSelection();
    }

    public void printFile() {
//        try {
//            textArea.print();
//        } catch (PrinterException ex) {
//            Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void setCurrentFont(Font font) {
        hexadecimal.setFont(font);
    }

    public Font getCurrentFont() {
        return hexadecimal.getFont();
    }

    public void showFontDialog(TextFontDialog dlg) {
        dlg.setStoredFont(hexadecimal.getFont());
        dlg.setVisible(true);
        if (dlg.getDialogOption() == JOptionPane.OK_OPTION) {
            hexadecimal.setFont(dlg.getStoredFont());
        }
    }

    public Color getFoundTextBackgroundColor() {
        return foundTextBackgroundColor;
    }

    public void setFoundTextBackgroundColor(Color color) {
        foundTextBackgroundColor = color;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textAreaScrollPane = new javax.swing.JScrollPane();

        setInheritsPopupMenu(true);
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        textAreaScrollPane.setName("textAreaScrollPane"); // NOI18N
        add(textAreaScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane textAreaScrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        if (highlight != null) {
//            textArea.getHighlighter().removeHighlight(highlight);
            highlight = null;
        }
        boolean oldValue = this.modified;
        this.modified = modified;
        firePropertyChange("modified", oldValue, this.modified);
    }

    @Override
    public void loadFromFile() {
        File file = new File(getFileName());
//        switch (fileType.getFileTypeId()) {
//            case DeltaHexModule.XBT_FILE_TYPE: {
//                try {
//                    XBPCatalog catalog = new XBPCatalog();
//                    catalog.addFormatDecl(getContextFormatDecl());
//                    XBLFormatDecl formatDecl = new XBLFormatDecl(XBEncodingText.XBUP_FORMATREV_CATALOGPATH);
//                    XBEncodingText encodingText = new XBEncodingText();
//                    XBDeclaration declaration = new XBDeclaration(formatDecl, encodingText);
//                    XBTPullTypeDeclaringFilter typeProcessing = new XBTPullTypeDeclaringFilter(catalog);
//                    typeProcessing.attachXBTPullProvider(new XBToXBTPullConvertor(new XBPullReader(new FileInputStream(getFileName()))));
//                    XBPSerialReader reader = new XBPSerialReader(typeProcessing);
//                    reader.read(declaration);
//                    changeCharset(encodingText.getCharset());
//                    textArea.setText(encodingText.getValue());
//                } catch (XBProcessingException | IOException ex) {
//                    Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//            }
//            case DeltaHexModule.TXT_FILE_TYPE: {
                try {
                    FileInputStream fileStream = new FileInputStream(file);
                    HexadecimalData data = hexadecimal.getData();
                    ((EditableHexadecimalData) data).loadFromStream(fileStream);
                    hexadecimal.setData(data);
                    fileStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
//                break;
//            }
//        }

        setModified(false);
    }

    @Override
    public void saveToFile() {
        File file = new File(getFileName());
//        switch (fileType.getFileTypeId()) {
//            case DeltaHexModule.XBT_FILE_TYPE: {
//                try {
//                    XBEncodingText encodingString = new XBEncodingText();
//                    encodingString.setValue(textArea.getText());
//                    encodingString.setCharset(charset);
//
//                    try (FileOutputStream output = new FileOutputStream(file)) {
//                        XBPCatalog catalog = new XBPCatalog();
//                        catalog.addFormatDecl(getContextFormatDecl());
//                        XBLFormatDecl formatDecl = new XBLFormatDecl(XBEncodingText.XBUP_FORMATREV_CATALOGPATH);
//                        XBDeclaration declaration = new XBDeclaration(formatDecl, encodingString);
//                        declaration.realignReservation(catalog);
//                        XBTTypeUndeclaringFilter typeProcessing = new XBTTypeUndeclaringFilter(catalog);
//                        typeProcessing.attachXBTListener(new XBTEventListenerToListener(new XBTToXBEventConvertor(new XBEventWriter(output))));
//                        XBPSerialWriter writer = new XBPSerialWriter(new XBTListenerToEventListener(typeProcessing));
//                        writer.write(declaration);
//                    }
//                } catch (XBProcessingException | IOException ex) {
//                    Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//            }
//            default: // TODO detect extension
//            case DeltaHexModule.TXT_FILE_TYPE: {
                try {
                    hexadecimal.getData().saveToStream(new FileOutputStream(file));
                } catch (IOException ex) {
                    Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
//                break;
//            }
//        }

        setModified(false);
    }

    /**
     * Returns local format declaration when catalog or service is not
     * available.
     *
     * @return local format declaration
     */
    public XBLFormatDecl getContextFormatDecl() {
        /*XBLFormatDef formatDef = new XBLFormatDef();
         List<XBFormatParam> groups = formatDef.getFormatParams();
         XBLGroupDecl stringGroup = new XBLGroupDecl(new XBLGroupDef());
         List<XBGroupParam> stringBlocks = stringGroup.getGroupDef().getGroupParams();
         stringBlocks.add(new XBGroupParamConsist(new XBLBlockDecl(new long[]{1, 3, 1, 2, 0, 0})));
         stringBlocks.add(new XBGroupParamConsist(new XBLBlockDecl(new long[]{1, 3, 1, 1, 1, 0})));
         stringBlocks.add(new XBGroupParamConsist(new XBLBlockDecl(new long[]{1, 3, 1, 2, 2, 0})));
         stringBlocks.add(new XBGroupParamConsist(new XBLBlockDecl(new long[]{1, 3, 1, 2, 3, 0})));
         stringBlocks.add(new XBGroupParamConsist(new XBLBlockDecl(new long[]{1, 3, 1, 2, 4, 0})));
         ((XBLGroupDef) stringGroup.getGroupDef()).provideRevision();
         groups.add(new XBFormatParamConsist(stringGroup));
         formatDef.realignRevision();

         XBLFormatDecl formatDecl = new XBLFormatDecl(formatDef);
         formatDecl.setCatalogPath(XBEncodingText.XBUP_FORMATREV_CATALOGPATH);
         return formatDecl;*/

        XBPSerialReader reader = new XBPSerialReader(ClassLoader.class.getResourceAsStream("/org/exbin/dhex/framework/deltahex/resources/xbt_format_decl.xb"));
        XBLFormatDecl formatDecl = new XBLFormatDecl();
        try {
            reader.read(formatDecl);
        } catch (XBProcessingException | IOException ex) {
            return null;
        }
        return formatDecl;
    }

    @Override
    public void newFile() {
        ((EditableHexadecimalData) hexadecimal.getData()).setDataSize(0);
        setModified(false);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public UndoableEdit getUndo() {
        return undoManagement;
    }

    public void setPopupMenu(JPopupMenu menu) {
        hexadecimal.setComponentPopupMenu(menu);
    }

    @Override
    public void setUndoUpdateListener(UndoUpdateListener undoUpdateListener) {
        this.undoUpdateListener = undoUpdateListener;
    }

    public Point getCaretPosition() {
//        int line;
//        int caretPosition = textArea.getCaretPosition();
//        javax.swing.text.Element root = textArea.getDocument().getDefaultRootElement();
//        line = root.getElementIndex(caretPosition);
//        try {
//            return new Point(caretPosition - textArea.getLineStartOffset(line) + 1, line + 1);
//        } catch (BadLocationException ex) {
//            Logger.getLogger(HexPanel.class.getName()).log(Level.SEVERE, null, ex);
//            return new Point(0, 0);
//        }
        return new Point();
    }

    public void attachCaretListener(ChangeListener listener) {
//        textArea.getCaret().addChangeListener(listener);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Font getDefaultFont() {
        return defaultFont;
    }

    public void setText(String text) {
//        textArea.setText(text);
    }

    @Override
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    @Override
    public Boolean canUndo() {
        return getUndo().canUndo();
    }

    @Override
    public Boolean canRedo() {
        return getUndo().canRedo();
    }

    @Override
    public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeListener = propertyChangeListener;
    }

    @Override
    public void performUndo() {
        getUndo().undo();
    }

    @Override
    public void performRedo() {
        getUndo().redo();
    }

    @Override
    public String getWindowTitle(String frameTitle) {
        if (!"".equals(fileName)) {
            int pos;
            int newpos = 0;
            do {
                pos = newpos;
                newpos = fileName.indexOf(File.separatorChar, pos) + 1;
            } while (newpos > 0);
            return fileName.substring(pos) + " - " + frameTitle;
        }

        return frameTitle;
    }

    public void setCharsetChangeListener(CharsetChangeListener charsetChangeListener) {
        this.charsetChangeListener = charsetChangeListener;
    }

    private void changeCharset(Charset charset) {
        this.charset = charset;
        if (charsetChangeListener != null) {
            charsetChangeListener.charsetChanged();
        }
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    public void registerTextStatus(HexStatusApi textStatusApi) {
        this.textStatus = textStatusApi;
        attachCaretListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Point pos = getCaretPosition();
                String textPosition = Long.toString((long) pos.getX()) + ":" + Long.toString((long) pos.getY());
                textStatus.setTextPosition(textPosition);
            }
        });
        setCharsetChangeListener(new HexPanel.CharsetChangeListener() {

            @Override
            public void charsetChanged() {
                textStatus.setEncoding(getCharset().name());
            }
        });
    }

    @Override
    public void setUpdateListener(ClipboardActionsUpdateListener updateListener) {
        clipboardActionsUpdateListener = updateListener;
    }

    @Override
    public boolean isEditable() {
        return hexadecimal.isEditable();
    }

    @Override
    public boolean canSelectAll() {
        return true; // textArea.getSelectionEnd() > textArea.getSelectionStart();
    }

    @Override
    public boolean canPaste() {
        return hexadecimal.canPaste();
    }

    public interface CharsetChangeListener {

        public void charsetChanged();
    }
}
