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
package org.exbin.deltahex.swing.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicBorders;
import org.exbin.deltahex.CaretMovedListener;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaLineNumberLength;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.DataChangedListener;
import org.exbin.deltahex.EditationAllowed;
import org.exbin.deltahex.HexCharactersCase;
import org.exbin.deltahex.PositionCodeType;
import org.exbin.deltahex.ScrollBarVisibility;
import org.exbin.deltahex.ScrollingListener;
import org.exbin.deltahex.Section;
import org.exbin.deltahex.SelectionChangedListener;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.ViewMode;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.swing.CodeAreaCaret;
import org.exbin.deltahex.swing.CodeAreaSpace.SpaceType;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Hexadecimal editor example panel.
 *
 * @version 0.1.3 2017/03/02
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaHexExamplePanel extends javax.swing.JPanel {

    private CodeArea codeArea;
    private final Map<JPanel, JPanel> tabMap = new HashMap<>();
    private JPanel activeTab;

    public DeltaHexExamplePanel() {
        initComponents();
    }

    public void setCodeArea(final CodeArea codeArea) {
        this.codeArea = codeArea;
        splitPane.setRightComponent(codeArea);
        viewModeComboBox.setSelectedIndex(codeArea.getViewMode().ordinal());
        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());
        positionCodeTypeComboBox.setSelectedIndex(codeArea.getPositionCodeType().ordinal());
        activeSectionComboBox.setSelectedIndex(codeArea.getActiveSection().ordinal());
        backgroundModeComboBox.setSelectedIndex(codeArea.getBackgroundMode().ordinal());
        lineNumbersBackgroundCheckBox.setSelected(codeArea.isLineNumberBackground());
        charRenderingComboBox.setSelectedIndex(codeArea.getCharRenderingMode().ordinal());
        charAntialiasingComboBox.setSelectedIndex(codeArea.getCharAntialiasingMode().ordinal());
        verticalScrollBarVisibilityComboBox.setSelectedIndex(codeArea.getVerticalScrollBarVisibility().ordinal());
        verticalScrollModeComboBox.setSelectedIndex(codeArea.getVerticalScrollMode().ordinal());
        horizontalScrollBarVisibilityComboBox.setSelectedIndex(codeArea.getHorizontalScrollBarVisibility().ordinal());
        horizontalScrollModeComboBox.setSelectedIndex(codeArea.getHorizontalScrollMode().ordinal());
        hexCharactersModeComboBox.setSelectedIndex(codeArea.getHexCharactersCase().ordinal());
        showLineNumbersCheckBox.setSelected(codeArea.isShowLineNumbers());
        showHeaderCheckBox.setSelected(codeArea.isShowHeader());
        showNonprintableCharactersCheckBox.setSelected(codeArea.isShowUnprintableCharacters());
        showShadowCursorCheckBox.setSelected(codeArea.isShowShadowCursor());
        editationAllowedComboBox.setSelectedIndex(codeArea.getEditationAllowed().ordinal());
        wrapLineModeCheckBox.setSelected(codeArea.isWrapMode());
        lineLengthSpinner.setValue(codeArea.getLineLength());
        dataSizeTextField.setText(String.valueOf(codeArea.getDataSize()));
        headerSpaceComboBox.setSelectedIndex(codeArea.getHeaderSpaceType().ordinal());
        headerSpaceSpinner.setValue(codeArea.getHeaderSpaceSize());
        lineNumbersSpaceComboBox.setSelectedIndex(codeArea.getLineNumberSpaceType().ordinal());
        lineNumbersSpaceSpinner.setValue(codeArea.getLineNumberSpaceSize());
        lineNumbersLengthComboBox.setSelectedIndex(codeArea.getLineNumberType().ordinal());
        lineNumbersLengthSpinner.setValue(codeArea.getLineNumberSpecifiedLength());
        cursorRenderingModeComboBox.setSelectedIndex(codeArea.getCaret().getRenderingMode().ordinal());
        cursorInsertShapeComboBox.setSelectedIndex(codeArea.getCaret().getInsertCursorShape().ordinal());
        cursorOverwriteShapeComboBox.setSelectedIndex(codeArea.getCaret().getOverwriteCursorShape().ordinal());
        cursorBlinkingRateSpinner.setValue(codeArea.getCaret().getBlinkRate());
        byteGroupSizeSpinner.setValue(codeArea.getByteGroupSize());
        spaceGroupSizeSpinner.setValue(codeArea.getSpaceGroupSize());

        int decorationMode = codeArea.getDecorationMode();
        decoratorHeaderLineCheckBox.setSelected((decorationMode & CodeArea.DECORATION_HEADER_LINE) > 0);
        decoratorLineNumLineCheckBox.setSelected((decorationMode & CodeArea.DECORATION_LINENUM_LINE) > 0);
        decoratorSplitLineCheckBox.setSelected((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0);
        decoratorBoxCheckBox.setSelected((decorationMode & CodeArea.DECORATION_BOX) > 0);
        codeArea.addCaretMovedListener(new CaretMovedListener() {
            @Override
            public void caretMoved(CaretPosition caretPosition, Section section) {
                positionTextField.setText(String.valueOf(caretPosition.getDataPosition()));
                codeOffsetTextField.setText(String.valueOf(caretPosition.getCodeOffset()));
                activeSectionComboBox.setSelectedIndex(section.ordinal());
            }
        });
        codeArea.addSelectionChangedListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionRange selection) {
                if (selection != null) {
                    long first = codeArea.getSelection().getFirst();
                    selectionStartTextField.setText(String.valueOf(first));
                    long last = codeArea.getSelection().getLast();
                    selectionEndTextField.setText(String.valueOf(last));
                } else {
                    selectionStartTextField.setText("");
                    selectionEndTextField.setText("");
                }
            }
        });
        codeArea.addDataChangedListener(new DataChangedListener() {
            @Override
            public void dataChanged() {
                dataSizeTextField.setText(String.valueOf(codeArea.getDataSize()));
            }
        });
        codeArea.addScrollingListener(new ScrollingListener() {
            @Override
            public void scrolled() {
                CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
                verticalPositionTextField.setText(scrollPosition.getScrollLinePosition() + ":" + scrollPosition.getScrollLineOffset());
                horizontalPositionTextField.setText(scrollPosition.getScrollCharPosition() + ":" + scrollPosition.getScrollCharOffset());
                horizontalByteShiftTextField.setText(String.valueOf(scrollPosition.getLineByteShift()));
            }
        });

        tabMap.put(modeTab, modePanel);
        tabMap.put(stateTab, statePanel);
        tabMap.put(layoutTab, layoutPanel);
        tabMap.put(decorationTab, decorationPanel);
        tabMap.put(scrollingTab, scrollingPanel);
        tabMap.put(cursorTab, cursorPanel);

        activeTab = modeTab;
        modeTab.add(modePanel, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modePanel = new javax.swing.JPanel();
        viewModeScrollModeLabel = new javax.swing.JLabel();
        viewModeComboBox = new javax.swing.JComboBox<>();
        charRenderingScrollModeLabel = new javax.swing.JLabel();
        charRenderingComboBox = new javax.swing.JComboBox<>();
        charAntialiasingScrollModeLabel = new javax.swing.JLabel();
        charAntialiasingComboBox = new javax.swing.JComboBox<>();
        codeTypeScrollModeLabel = new javax.swing.JLabel();
        codeTypeComboBox = new javax.swing.JComboBox<>();
        editationAllowedLabel = new javax.swing.JLabel();
        editationAllowedComboBox = new javax.swing.JComboBox<>();
        fontPanel = new javax.swing.JPanel();
        fontFamilyLabel = new javax.swing.JLabel();
        fontFamilyComboBox = new javax.swing.JComboBox<>();
        fontSizeLabel = new javax.swing.JLabel();
        fontSizeComboBox = new javax.swing.JComboBox<>();
        charsetLabel = new javax.swing.JLabel();
        charsetComboBox = new javax.swing.JComboBox<>();
        showNonprintableCharactersCheckBox = new javax.swing.JCheckBox();
        statePanel = new javax.swing.JPanel();
        dataSizeLabel = new javax.swing.JLabel();
        dataSizeTextField = new javax.swing.JTextField();
        loadDataButton = new javax.swing.JButton();
        saveDataButton = new javax.swing.JButton();
        positionPanel = new javax.swing.JPanel();
        positionLabel = new javax.swing.JLabel();
        positionTextField = new javax.swing.JTextField();
        codeOffsetLabel = new javax.swing.JLabel();
        codeOffsetTextField = new javax.swing.JTextField();
        activeSectionLabel = new javax.swing.JLabel();
        activeSectionComboBox = new javax.swing.JComboBox<>();
        selectionPanel = new javax.swing.JPanel();
        selectionStartLabel = new javax.swing.JLabel();
        selectionStartTextField = new javax.swing.JTextField();
        selectionEndLabel = new javax.swing.JLabel();
        selectionEndTextField = new javax.swing.JTextField();
        layoutPanel = new javax.swing.JPanel();
        lineLengthLabel = new javax.swing.JLabel();
        lineLengthSpinner = new javax.swing.JSpinner();
        lineNumbersPanel = new javax.swing.JPanel();
        showLineNumbersCheckBox = new javax.swing.JCheckBox();
        lineNumbersLengthLabel = new javax.swing.JLabel();
        lineNumbersLengthComboBox = new javax.swing.JComboBox<>();
        lineNumbersLengthSpinner = new javax.swing.JSpinner();
        lineNumbersSpaceLabel = new javax.swing.JLabel();
        lineNumbersSpaceComboBox = new javax.swing.JComboBox<>();
        lineNumbersSpaceSpinner = new javax.swing.JSpinner();
        wrapLineModeCheckBox = new javax.swing.JCheckBox();
        headerPanel = new javax.swing.JPanel();
        showHeaderCheckBox = new javax.swing.JCheckBox();
        headerSpaceLabel = new javax.swing.JLabel();
        headerSpaceComboBox = new javax.swing.JComboBox<>();
        headerSpaceSpinner = new javax.swing.JSpinner();
        byteGroupSizeLabel = new javax.swing.JLabel();
        byteGroupSizeSpinner = new javax.swing.JSpinner();
        spaceGroupSizeLabel = new javax.swing.JLabel();
        spaceGroupSizeSpinner = new javax.swing.JSpinner();
        decorationPanel = new javax.swing.JPanel();
        backgroundModeLabel = new javax.swing.JLabel();
        backgroundModeComboBox = new javax.swing.JComboBox<>();
        lineNumbersBackgroundCheckBox = new javax.swing.JCheckBox();
        linesPanel = new javax.swing.JPanel();
        decoratorLineNumLineCheckBox = new javax.swing.JCheckBox();
        decoratorSplitLineCheckBox = new javax.swing.JCheckBox();
        decoratorBoxCheckBox = new javax.swing.JCheckBox();
        decoratorHeaderLineCheckBox = new javax.swing.JCheckBox();
        borderTypeLabel = new javax.swing.JLabel();
        borderTypeComboBox = new javax.swing.JComboBox<>();
        hexCharactersModeLabel = new javax.swing.JLabel();
        hexCharactersModeComboBox = new javax.swing.JComboBox<>();
        positionCodeTypeLabel = new javax.swing.JLabel();
        positionCodeTypeComboBox = new javax.swing.JComboBox<>();
        scrollingPanel = new javax.swing.JPanel();
        verticalPanel = new javax.swing.JPanel();
        verticalScrollBarVisibilityModeLabel = new javax.swing.JLabel();
        verticalScrollBarVisibilityComboBox = new javax.swing.JComboBox<>();
        verticalScrollModeLabel = new javax.swing.JLabel();
        verticalScrollModeComboBox = new javax.swing.JComboBox<>();
        verticalPositionLabel = new javax.swing.JLabel();
        verticalPositionTextField = new javax.swing.JTextField();
        horizontalPanel = new javax.swing.JPanel();
        horizontalScrollBarVisibilityLabel = new javax.swing.JLabel();
        horizontalScrollBarVisibilityComboBox = new javax.swing.JComboBox<>();
        horizontalScrollModeLabel = new javax.swing.JLabel();
        horizontalScrollModeComboBox = new javax.swing.JComboBox<>();
        horizontalPositionLabel = new javax.swing.JLabel();
        horizontalPositionTextField = new javax.swing.JTextField();
        horizontalByteShiftLabel = new javax.swing.JLabel();
        horizontalByteShiftTextField = new javax.swing.JTextField();
        cursorPanel = new javax.swing.JPanel();
        showShadowCursorCheckBox = new javax.swing.JCheckBox();
        cursorRenderingModeLabel = new javax.swing.JLabel();
        cursorRenderingModeComboBox = new javax.swing.JComboBox<>();
        cursorInsertShapeModeLabel = new javax.swing.JLabel();
        cursorInsertShapeComboBox = new javax.swing.JComboBox<>();
        cursorOverwriteShapeModeLabel = new javax.swing.JLabel();
        cursorOverwriteShapeComboBox = new javax.swing.JComboBox<>();
        cursorBlinkingRateLabel = new javax.swing.JLabel();
        cursorBlinkingRateSpinner = new javax.swing.JSpinner();
        splitPane = new javax.swing.JSplitPane();
        tabbedPane = new javax.swing.JTabbedPane();
        modeTab = new javax.swing.JPanel();
        stateTab = new javax.swing.JPanel();
        layoutTab = new javax.swing.JPanel();
        decorationTab = new javax.swing.JPanel();
        scrollingTab = new javax.swing.JPanel();
        cursorTab = new javax.swing.JPanel();

        viewModeScrollModeLabel.setText("View Mode");

        viewModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DUAL", "HEXADECIMAL", "PREVIEW" }));
        viewModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewModeComboBoxActionPerformed(evt);
            }
        });

        charRenderingScrollModeLabel.setText("Character Rendering");

        charRenderingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "LINE_AT_ONCE", "TOP_LEFT", "CENTER" }));
        charRenderingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charRenderingComboBoxActionPerformed(evt);
            }
        });

        charAntialiasingScrollModeLabel.setText("Character Antialiasing");

        charAntialiasingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OFF", "AUTO", "DEFAULT", "BASIC", "GASP", "LCD_HRGB", "LCD_HBGR", "LCD_VRGB", "LCD_VBGR" }));
        charAntialiasingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charAntialiasingComboBoxActionPerformed(evt);
            }
        });

        codeTypeScrollModeLabel.setText("Code Type");

        codeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BINARY", "OCTAL", "DECIMAL", "HEXADECIMAL" }));
        codeTypeComboBox.setSelectedIndex(3);
        codeTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeTypeComboBoxActionPerformed(evt);
            }
        });

        editationAllowedLabel.setText("Editation");

        editationAllowedComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "READ_ONLY", "OVERWRITE_ONLY", "ALLOWED" }));
        editationAllowedComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editationAllowedComboBoxActionPerformed(evt);
            }
        });

        fontPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Font"));

        fontFamilyLabel.setText("Font Family");

        fontFamilyComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DIALOG", "MONOSPACE", "SERIF" }));
        fontFamilyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontFamilyComboBoxActionPerformed(evt);
            }
        });

        fontSizeLabel.setText("Font Size");

        fontSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "8", "9", "10", "12", "14", "18", "22" }));
        fontSizeComboBox.setSelectedIndex(3);
        fontSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fontPanelLayout = new javax.swing.GroupLayout(fontPanel);
        fontPanel.setLayout(fontPanelLayout);
        fontPanelLayout.setHorizontalGroup(
            fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fontFamilyComboBox, 0, 264, Short.MAX_VALUE)
                    .addComponent(fontSizeComboBox, 0, 264, Short.MAX_VALUE)
                    .addGroup(fontPanelLayout.createSequentialGroup()
                        .addGroup(fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fontFamilyLabel)
                            .addComponent(fontSizeLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        fontPanelLayout.setVerticalGroup(
            fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fontFamilyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontFamilyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        charsetLabel.setText("Charset");

        charsetComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTF-8", "UTF-16", "UTF-16BE", "US-ASCII", "IBM852", "ISO-8859-1" }));
        charsetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetComboBoxActionPerformed(evt);
            }
        });

        showNonprintableCharactersCheckBox.setText("Show Nonprintable Characters");
        showNonprintableCharactersCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showNonprintableCharactersCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout modePanelLayout = new javax.swing.GroupLayout(modePanel);
        modePanel.setLayout(modePanelLayout);
        modePanelLayout.setHorizontalGroup(
            modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showNonprintableCharactersCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(charRenderingComboBox, 0, 286, Short.MAX_VALUE)
                    .addComponent(codeTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(charAntialiasingComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editationAllowedComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(charsetComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(modePanelLayout.createSequentialGroup()
                        .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewModeScrollModeLabel)
                            .addComponent(charRenderingScrollModeLabel)
                            .addComponent(charAntialiasingScrollModeLabel)
                            .addComponent(codeTypeScrollModeLabel)
                            .addComponent(editationAllowedLabel)
                            .addComponent(charsetLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        modePanelLayout.setVerticalGroup(
            modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(viewModeScrollModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showNonprintableCharactersCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeTypeScrollModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charRenderingScrollModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charRenderingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charAntialiasingScrollModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charAntialiasingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editationAllowedLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editationAllowedComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charsetLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charsetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        dataSizeLabel.setText("Data Size");

        dataSizeTextField.setEditable(false);
        dataSizeTextField.setText("0");

        loadDataButton.setText("Load...");
        loadDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDataButtonActionPerformed(evt);
            }
        });

        saveDataButton.setText("Save...");
        saveDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDataButtonActionPerformed(evt);
            }
        });

        positionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Position"));

        positionLabel.setText("Data Position");

        positionTextField.setEditable(false);

        codeOffsetLabel.setText("Code Offset Position");

        codeOffsetTextField.setEditable(false);

        activeSectionLabel.setText("Active Section");

        activeSectionComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CODE_MATRIX", "TEXT_PREVIEW" }));
        activeSectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeSectionComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout positionPanelLayout = new javax.swing.GroupLayout(positionPanel);
        positionPanel.setLayout(positionPanelLayout);
        positionPanelLayout.setHorizontalGroup(
            positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(positionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(codeOffsetTextField)
                    .addComponent(positionTextField)
                    .addGroup(positionPanelLayout.createSequentialGroup()
                        .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(positionLabel)
                            .addComponent(codeOffsetLabel)
                            .addComponent(activeSectionLabel))
                        .addGap(0, 94, Short.MAX_VALUE))
                    .addComponent(activeSectionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        positionPanelLayout.setVerticalGroup(
            positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(positionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(positionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(positionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeOffsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeOffsetTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(activeSectionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(activeSectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        selectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Selection"));

        selectionStartLabel.setText("Selection Start");

        selectionStartTextField.setEditable(false);

        selectionEndLabel.setText("Selection End");

        selectionEndTextField.setEditable(false);

        javax.swing.GroupLayout selectionPanelLayout = new javax.swing.GroupLayout(selectionPanel);
        selectionPanel.setLayout(selectionPanelLayout);
        selectionPanelLayout.setHorizontalGroup(
            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(selectionEndTextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectionStartTextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, selectionPanelLayout.createSequentialGroup()
                        .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectionEndLabel)
                            .addComponent(selectionStartLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        selectionPanelLayout.setVerticalGroup(
            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectionStartLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionStartTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionEndLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionEndTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout statePanelLayout = new javax.swing.GroupLayout(statePanel);
        statePanel.setLayout(statePanelLayout);
        statePanelLayout.setHorizontalGroup(
            statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(selectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(positionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dataSizeTextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, statePanelLayout.createSequentialGroup()
                        .addGroup(statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dataSizeLabel)
                            .addGroup(statePanelLayout.createSequentialGroup()
                                .addComponent(loadDataButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveDataButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        statePanelLayout.setVerticalGroup(
            statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statePanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(dataSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadDataButton)
                    .addComponent(saveDataButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(positionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(94, Short.MAX_VALUE))
        );

        lineLengthLabel.setText("Bytes Per Line");

        lineLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(16, 1, null, 1));
        lineLengthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineLengthSpinnerStateChanged(evt);
            }
        });

        lineNumbersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Line Numbers"));

        showLineNumbersCheckBox.setText("Show Line Numbers");
        showLineNumbersCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showLineNumbersCheckBoxItemStateChanged(evt);
            }
        });

        lineNumbersLengthLabel.setText("Line Numbers Length");

        lineNumbersLengthComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "SPECIFIED" }));
        lineNumbersLengthComboBox.setSelectedIndex(1);
        lineNumbersLengthComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineNumbersLengthComboBoxActionPerformed(evt);
            }
        });

        lineNumbersLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        lineNumbersLengthSpinner.setValue(8);
        lineNumbersLengthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineNumbersLengthSpinnerStateChanged(evt);
            }
        });

        lineNumbersSpaceLabel.setText("Line Numbers Space");

        lineNumbersSpaceComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "SPECIFIED", "QUARTER_UNIT", "HALF_UNIT", "ONE_UNIT", "ONE_AND_HALF_UNIT", "DOUBLE_UNIT" }));
        lineNumbersSpaceComboBox.setSelectedIndex(4);
        lineNumbersSpaceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineNumbersSpaceComboBoxActionPerformed(evt);
            }
        });

        lineNumbersSpaceSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        lineNumbersSpaceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineNumbersSpaceSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout lineNumbersPanelLayout = new javax.swing.GroupLayout(lineNumbersPanel);
        lineNumbersPanel.setLayout(lineNumbersPanelLayout);
        lineNumbersPanelLayout.setHorizontalGroup(
            lineNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lineNumbersPanelLayout.createSequentialGroup()
                .addComponent(showLineNumbersCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(lineNumbersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lineNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lineNumbersPanelLayout.createSequentialGroup()
                        .addComponent(lineNumbersLengthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lineNumbersLengthSpinner))
                    .addGroup(lineNumbersPanelLayout.createSequentialGroup()
                        .addGroup(lineNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lineNumbersLengthLabel)
                            .addComponent(lineNumbersSpaceLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(lineNumbersPanelLayout.createSequentialGroup()
                        .addComponent(lineNumbersSpaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lineNumbersSpaceSpinner)))
                .addContainerGap())
        );
        lineNumbersPanelLayout.setVerticalGroup(
            lineNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lineNumbersPanelLayout.createSequentialGroup()
                .addComponent(showLineNumbersCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineNumbersLengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lineNumbersLengthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lineNumbersLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineNumbersSpaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lineNumbersSpaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lineNumbersSpaceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        wrapLineModeCheckBox.setText("Wrap Line Mode");
        wrapLineModeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                wrapLineModeCheckBoxItemStateChanged(evt);
            }
        });

        headerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Header"));

        showHeaderCheckBox.setText("Show Header");
        showHeaderCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showHeaderCheckBoxItemStateChanged(evt);
            }
        });

        headerSpaceLabel.setText("Header Space");

        headerSpaceComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "SPECIFIED", "QUARTER_UNIT", "HALF_UNIT", "ONE_UNIT", "ONE_AND_HALF_UNIT", "DOUBLE_UNIT" }));
        headerSpaceComboBox.setSelectedIndex(2);
        headerSpaceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerSpaceComboBoxActionPerformed(evt);
            }
        });

        headerSpaceSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        headerSpaceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                headerSpaceSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addComponent(headerSpaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerSpaceSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(showHeaderCheckBox)
                            .addComponent(headerSpaceLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(showHeaderCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerSpaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(headerSpaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headerSpaceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        byteGroupSizeLabel.setText("Byte Group Size");

        byteGroupSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        byteGroupSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                byteGroupSizeSpinnerStateChanged(evt);
            }
        });

        spaceGroupSizeLabel.setText("Space Group Size");

        spaceGroupSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spaceGroupSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spaceGroupSizeSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layoutPanelLayout = new javax.swing.GroupLayout(layoutPanel);
        layoutPanel.setLayout(layoutPanelLayout);
        layoutPanelLayout.setHorizontalGroup(
            layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layoutPanelLayout.createSequentialGroup()
                        .addGroup(layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lineLengthLabel)
                            .addComponent(wrapLineModeCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layoutPanelLayout.createSequentialGroup()
                        .addGroup(layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(spaceGroupSizeSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(byteGroupSizeSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headerPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lineLengthSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lineNumbersPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layoutPanelLayout.createSequentialGroup()
                                .addGroup(layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(byteGroupSizeLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spaceGroupSizeLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layoutPanelLayout.setVerticalGroup(
            layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wrapLineModeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineLengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineNumbersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(byteGroupSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(byteGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spaceGroupSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spaceGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(87, Short.MAX_VALUE))
        );

        backgroundModeLabel.setText("Background Mode");

        backgroundModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "PLAIN", "STRIPPED", "GRIDDED" }));
        backgroundModeComboBox.setSelectedIndex(2);
        backgroundModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundModeComboBoxActionPerformed(evt);
            }
        });

        lineNumbersBackgroundCheckBox.setText("Include Line Numbers");
        lineNumbersBackgroundCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lineNumbersBackgroundCheckBoxItemStateChanged(evt);
            }
        });

        linesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Lines"));

        decoratorLineNumLineCheckBox.setText("LineNum Line");
        decoratorLineNumLineCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                decoratorLineNumLineCheckBoxItemStateChanged(evt);
            }
        });

        decoratorSplitLineCheckBox.setText("Split Line");
        decoratorSplitLineCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                decoratorSplitLineCheckBoxItemStateChanged(evt);
            }
        });

        decoratorBoxCheckBox.setText("Area Box");
        decoratorBoxCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                decoratorBoxCheckBoxItemStateChanged(evt);
            }
        });

        decoratorHeaderLineCheckBox.setText("Header Line");
        decoratorHeaderLineCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                decoratorHeaderLineCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout linesPanelLayout = new javax.swing.GroupLayout(linesPanel);
        linesPanel.setLayout(linesPanelLayout);
        linesPanelLayout.setHorizontalGroup(
            linesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(linesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(linesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(decoratorLineNumLineCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(decoratorSplitLineCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(decoratorBoxCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(decoratorHeaderLineCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        linesPanelLayout.setVerticalGroup(
            linesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(linesPanelLayout.createSequentialGroup()
                .addComponent(decoratorHeaderLineCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(decoratorLineNumLineCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(decoratorSplitLineCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(decoratorBoxCheckBox))
        );

        borderTypeLabel.setText("Border Type");

        borderTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "EMPTY BORDER", "MARGIN BORDER", "BEVEL BORDER - RAISED", "BEVEL BORDER - LOWERED", "ETCHED BORDER - RAISED", "ETCHED BORDER - LOWERED", "LINE BORDER" }));
        borderTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borderTypeComboBoxActionPerformed(evt);
            }
        });

        hexCharactersModeLabel.setText("Hex Chars Mode");

        hexCharactersModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LOWER", "UPPER" }));
        hexCharactersModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexCharactersModeComboBoxActionPerformed(evt);
            }
        });

        positionCodeTypeLabel.setText("Position Code Type");

        positionCodeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OCTAL", "DECIMAL", "HEXADECIMAL" }));
        positionCodeTypeComboBox.setSelectedIndex(2);
        positionCodeTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionCodeTypeComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout decorationPanelLayout = new javax.swing.GroupLayout(decorationPanel);
        decorationPanel.setLayout(decorationPanelLayout);
        decorationPanelLayout.setHorizontalGroup(
            decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(decorationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(decorationPanelLayout.createSequentialGroup()
                        .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hexCharactersModeLabel)
                            .addComponent(positionCodeTypeLabel))
                        .addGap(132, 132, 132))
                    .addGroup(decorationPanelLayout.createSequentialGroup()
                        .addGroup(decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(backgroundModeLabel)
                            .addComponent(borderTypeLabel)
                            .addComponent(lineNumbersBackgroundCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(backgroundModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(linesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(borderTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(hexCharactersModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(positionCodeTypeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        decorationPanelLayout.setVerticalGroup(
            decorationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(decorationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(backgroundModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backgroundModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineNumbersBackgroundCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(linesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(borderTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(borderTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hexCharactersModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hexCharactersModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(positionCodeTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(positionCodeTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(125, Short.MAX_VALUE))
        );

        verticalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Vertical"));

        verticalScrollBarVisibilityModeLabel.setText("Vertical Scrollbar");

        verticalScrollBarVisibilityComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NEVER", "IF_NEEDED", "ALWAYS" }));
        verticalScrollBarVisibilityComboBox.setSelectedIndex(1);
        verticalScrollBarVisibilityComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalScrollBarVisibilityComboBoxActionPerformed(evt);
            }
        });

        verticalScrollModeLabel.setText("Vertical Scroll Mode");

        verticalScrollModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PER_LINE", "PIXEL" }));
        verticalScrollModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalScrollModeComboBoxActionPerformed(evt);
            }
        });

        verticalPositionLabel.setText("Vertical Scroll Position");

        verticalPositionTextField.setEditable(false);
        verticalPositionTextField.setText("0:0");

        javax.swing.GroupLayout verticalPanelLayout = new javax.swing.GroupLayout(verticalPanel);
        verticalPanel.setLayout(verticalPanelLayout);
        verticalPanelLayout.setHorizontalGroup(
            verticalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(verticalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(verticalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(verticalScrollBarVisibilityComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(verticalScrollModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(verticalPositionTextField)
                    .addGroup(verticalPanelLayout.createSequentialGroup()
                        .addGroup(verticalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(verticalScrollBarVisibilityModeLabel)
                            .addComponent(verticalScrollModeLabel)
                            .addComponent(verticalPositionLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        verticalPanelLayout.setVerticalGroup(
            verticalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(verticalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(verticalScrollBarVisibilityModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verticalScrollBarVisibilityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verticalScrollModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verticalScrollModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verticalPositionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verticalPositionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        horizontalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Horizontal"));

        horizontalScrollBarVisibilityLabel.setText("Horizontal Scrollbar");

        horizontalScrollBarVisibilityComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NEVER", "IF_NEEDED", "ALWAYS" }));
        horizontalScrollBarVisibilityComboBox.setSelectedIndex(1);
        horizontalScrollBarVisibilityComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizontalScrollBarVisibilityComboBoxActionPerformed(evt);
            }
        });

        horizontalScrollModeLabel.setText("Horizontal Scroll Mode");

        horizontalScrollModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PER_CHAR", "PIXEL" }));
        horizontalScrollModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizontalScrollModeComboBoxActionPerformed(evt);
            }
        });

        horizontalPositionLabel.setText("Horizontal Scroll Position");

        horizontalPositionTextField.setEditable(false);
        horizontalPositionTextField.setText("0:0");

        javax.swing.GroupLayout horizontalPanelLayout = new javax.swing.GroupLayout(horizontalPanel);
        horizontalPanel.setLayout(horizontalPanelLayout);
        horizontalPanelLayout.setHorizontalGroup(
            horizontalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(horizontalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(horizontalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(horizontalScrollModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(horizontalScrollBarVisibilityComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(horizontalPanelLayout.createSequentialGroup()
                        .addGroup(horizontalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(horizontalScrollBarVisibilityLabel)
                            .addComponent(horizontalScrollModeLabel)
                            .addComponent(horizontalPositionLabel))
                        .addGap(0, 302, Short.MAX_VALUE))
                    .addComponent(horizontalPositionTextField))
                .addContainerGap())
        );
        horizontalPanelLayout.setVerticalGroup(
            horizontalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(horizontalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(horizontalScrollBarVisibilityLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalScrollBarVisibilityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalScrollModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalScrollModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalPositionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalPositionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        horizontalByteShiftLabel.setText("Horizontal Byte Shift");

        horizontalByteShiftTextField.setEditable(false);
        horizontalByteShiftTextField.setText("0");

        javax.swing.GroupLayout scrollingPanelLayout = new javax.swing.GroupLayout(scrollingPanel);
        scrollingPanel.setLayout(scrollingPanelLayout);
        scrollingPanelLayout.setHorizontalGroup(
            scrollingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scrollingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scrollingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(verticalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(horizontalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(horizontalByteShiftTextField)
                    .addGroup(scrollingPanelLayout.createSequentialGroup()
                        .addComponent(horizontalByteShiftLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        scrollingPanelLayout.setVerticalGroup(
            scrollingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scrollingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(verticalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalByteShiftLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalByteShiftTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(88, Short.MAX_VALUE))
        );

        showShadowCursorCheckBox.setText("Show Shadow Cursor");
        showShadowCursorCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showShadowCursorCheckBoxItemStateChanged(evt);
            }
        });

        cursorRenderingModeLabel.setText("Cursor Rendering Mode");

        cursorRenderingModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PAINT", "XOR", "NEGATIVE" }));
        cursorRenderingModeComboBox.setSelectedIndex(1);
        cursorRenderingModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cursorRenderingModeComboBoxActionPerformed(evt);
            }
        });

        cursorInsertShapeModeLabel.setText("Insert Cursor Shape");

        cursorInsertShapeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LINE_BOTTOM", "LINE_TOP", "LINE_LEFT", "LINE_RIGHT", "DOUBLE_BOTTOM", "DOUBLE_TOP", "DOUBLE_LEFT", "DOUBLE_RIGHT", "QUARTER_BOTTOM", "QUARTER_TOP", "QUARTER_LEFT", "QUARTER_RIGHT", "HALF_BOTTOM", "HALF_TOP", "HALF_LEFT", "HALF_RIGHT", "BOX", "FRAME", "CORNERS", "BOTTOM_CORNERS" }));
        cursorInsertShapeComboBox.setSelectedIndex(6);
        cursorInsertShapeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cursorInsertShapeComboBoxActionPerformed(evt);
            }
        });

        cursorOverwriteShapeModeLabel.setText("Overwrite Cursor Shape");

        cursorOverwriteShapeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LINE_BOTTOM", "LINE_TOP", "LINE_LEFT", "LINE_RIGHT", "DOUBLE_BOTTOM", "DOUBLE_TOP", "DOUBLE_LEFT", "DOUBLE_RIGHT", "QUARTER_BOTTOM", "QUARTER_TOP", "QUARTER_LEFT", "QUARTER_RIGHT", "HALF_BOTTOM", "HALF_TOP", "HALF_LEFT", "HALF_RIGHT", "BOX", "FRAME", "CORNERS", "BOTTOM_CORNERS" }));
        cursorOverwriteShapeComboBox.setSelectedIndex(16);
        cursorOverwriteShapeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cursorOverwriteShapeComboBoxActionPerformed(evt);
            }
        });

        cursorBlinkingRateLabel.setText("Cursor Blinking Rate");

        cursorBlinkingRateSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cursorBlinkingRateSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout cursorPanelLayout = new javax.swing.GroupLayout(cursorPanel);
        cursorPanel.setLayout(cursorPanelLayout);
        cursorPanelLayout.setHorizontalGroup(
            cursorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cursorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cursorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cursorRenderingModeComboBox, 0, 263, Short.MAX_VALUE)
                    .addComponent(cursorInsertShapeComboBox, 0, 263, Short.MAX_VALUE)
                    .addComponent(cursorOverwriteShapeComboBox, 0, 263, Short.MAX_VALUE)
                    .addGroup(cursorPanelLayout.createSequentialGroup()
                        .addGroup(cursorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cursorRenderingModeLabel)
                            .addComponent(cursorInsertShapeModeLabel)
                            .addComponent(cursorOverwriteShapeModeLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(showShadowCursorCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(cursorPanelLayout.createSequentialGroup()
                        .addComponent(cursorBlinkingRateLabel)
                        .addGap(63, 63, 63))
                    .addComponent(cursorBlinkingRateSpinner, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        cursorPanelLayout.setVerticalGroup(
            cursorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cursorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cursorBlinkingRateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorBlinkingRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorRenderingModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorRenderingModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorInsertShapeModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorInsertShapeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorOverwriteShapeModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cursorOverwriteShapeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showShadowCursorCheckBox)
                .addContainerGap(266, Short.MAX_VALUE))
        );

        setLayout(new java.awt.BorderLayout());

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        modeTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Mode", modeTab);

        stateTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("State", stateTab);

        layoutTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Layout", layoutTab);

        decorationTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Decoration", decorationTab);

        scrollingTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Scrolling", scrollingTab);

        cursorTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Cursor", cursorTab);

        splitPane.setLeftComponent(tabbedPane);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void viewModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeComboBoxActionPerformed
        codeArea.setViewMode(ViewMode.values()[viewModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_viewModeComboBoxActionPerformed

    private void lineLengthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineLengthSpinnerStateChanged
        int value = (Integer) lineLengthSpinner.getValue();
        if (value > 0) {
            codeArea.setLineLength(value);
        }
    }//GEN-LAST:event_lineLengthSpinnerStateChanged

    private void charRenderingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charRenderingComboBoxActionPerformed
        codeArea.setCharRenderingMode(CodeArea.CharRenderingMode.values()[charRenderingComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_charRenderingComboBoxActionPerformed

    private void backgroundModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundModeComboBoxActionPerformed
        codeArea.setBackgroundMode(CodeArea.BackgroundMode.values()[backgroundModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_backgroundModeComboBoxActionPerformed

    private void charAntialiasingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charAntialiasingComboBoxActionPerformed
        codeArea.setCharAntialiasingMode(CodeArea.CharAntialiasingMode.values()[charAntialiasingComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_charAntialiasingComboBoxActionPerformed

    private void decoratorLineNumLineCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_decoratorLineNumLineCheckBoxItemStateChanged
        int decorationMode = codeArea.getDecorationMode();
        boolean selected = decoratorLineNumLineCheckBox.isSelected();
        if (((decorationMode & CodeArea.DECORATION_LINENUM_LINE) > 0) != selected) {
            codeArea.setDecorationMode(decorationMode ^ CodeArea.DECORATION_LINENUM_LINE);
        }
    }//GEN-LAST:event_decoratorLineNumLineCheckBoxItemStateChanged

    private void decoratorSplitLineCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_decoratorSplitLineCheckBoxItemStateChanged
        int decorationMode = codeArea.getDecorationMode();
        boolean selected = decoratorSplitLineCheckBox.isSelected();
        if (((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) != selected) {
            codeArea.setDecorationMode(decorationMode ^ CodeArea.DECORATION_PREVIEW_LINE);
        }
    }//GEN-LAST:event_decoratorSplitLineCheckBoxItemStateChanged

    private void decoratorBoxCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_decoratorBoxCheckBoxItemStateChanged
        int decorationMode = codeArea.getDecorationMode();
        boolean selected = decoratorBoxCheckBox.isSelected();
        if (((decorationMode & CodeArea.DECORATION_BOX) > 0) != selected) {
            codeArea.setDecorationMode(decorationMode ^ CodeArea.DECORATION_BOX);
        }
    }//GEN-LAST:event_decoratorBoxCheckBoxItemStateChanged

    private void showHeaderCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showHeaderCheckBoxItemStateChanged
        codeArea.setShowHeader(showHeaderCheckBox.isSelected());
    }//GEN-LAST:event_showHeaderCheckBoxItemStateChanged

    private void showLineNumbersCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showLineNumbersCheckBoxItemStateChanged
        codeArea.setShowLineNumbers(showLineNumbersCheckBox.isSelected());
    }//GEN-LAST:event_showLineNumbersCheckBoxItemStateChanged

    private void wrapLineModeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_wrapLineModeCheckBoxItemStateChanged
        codeArea.setWrapMode(wrapLineModeCheckBox.isSelected());
    }//GEN-LAST:event_wrapLineModeCheckBoxItemStateChanged

    private void lineNumbersBackgroundCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lineNumbersBackgroundCheckBoxItemStateChanged
        codeArea.setLineNumberBackground(lineNumbersBackgroundCheckBox.isSelected());
    }//GEN-LAST:event_lineNumbersBackgroundCheckBoxItemStateChanged

    private void showShadowCursorCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showShadowCursorCheckBoxItemStateChanged
        codeArea.setShowShadowCursor(showShadowCursorCheckBox.isSelected());
    }//GEN-LAST:event_showShadowCursorCheckBoxItemStateChanged

    private void hexCharactersModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexCharactersModeComboBoxActionPerformed
        codeArea.setHexCharactersCase(HexCharactersCase.values()[hexCharactersModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_hexCharactersModeComboBoxActionPerformed

    private void codeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeTypeComboBoxActionPerformed
        codeArea.setCodeType(CodeType.values()[codeTypeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_codeTypeComboBoxActionPerformed

    private void activeSectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeSectionComboBoxActionPerformed
        codeArea.setActiveSection(Section.values()[activeSectionComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_activeSectionComboBoxActionPerformed

    private void loadDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDataButtonActionPerformed
        JFileChooser openFC = new JFileChooser();
        openFC.removeChoosableFileFilter(openFC.getAcceptAllFileFilter());
        openFC.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }

            @Override
            public String getDescription() {
                return "All Files (*)";
            }
        });
        if (openFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = openFC.getSelectedFile();
                try (FileInputStream stream = new FileInputStream(selectedFile)) {
                    ((EditableBinaryData) codeArea.getData()).loadFromStream(stream);
                    codeArea.notifyDataChanged();
                    codeArea.resetPosition();
                }
            } catch (IOException ex) {
                Logger.getLogger(DeltaHexExamplePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_loadDataButtonActionPerformed

    private void saveDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDataButtonActionPerformed
        JFileChooser saveFC = new JFileChooser();
        saveFC.removeChoosableFileFilter(saveFC.getAcceptAllFileFilter());
        saveFC.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }

            @Override
            public String getDescription() {
                return "All Files (*)";
            }
        });
        if (saveFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = saveFC.getSelectedFile();
                try (FileOutputStream stream = new FileOutputStream(selectedFile)) {
                    codeArea.getData().saveToStream(stream);
                }
            } catch (IOException ex) {
                Logger.getLogger(DeltaHexExamplePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_saveDataButtonActionPerformed

    private void borderTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borderTypeComboBoxActionPerformed
        codeArea.setBorder(getBorderByType(borderTypeComboBox.getSelectedIndex()));
    }//GEN-LAST:event_borderTypeComboBoxActionPerformed

    private void decoratorHeaderLineCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_decoratorHeaderLineCheckBoxItemStateChanged
        int decorationMode = codeArea.getDecorationMode();
        boolean selected = decoratorHeaderLineCheckBox.isSelected();
        if (((decorationMode & CodeArea.DECORATION_HEADER_LINE) > 0) != selected) {
            codeArea.setDecorationMode(decorationMode ^ CodeArea.DECORATION_HEADER_LINE);
        }
    }//GEN-LAST:event_decoratorHeaderLineCheckBoxItemStateChanged

    private void headerSpaceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headerSpaceComboBoxActionPerformed
        codeArea.setHeaderSpaceType(SpaceType.values()[headerSpaceComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_headerSpaceComboBoxActionPerformed

    private void lineNumbersSpaceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineNumbersSpaceComboBoxActionPerformed
        codeArea.setLineNumberSpaceType(SpaceType.values()[lineNumbersSpaceComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_lineNumbersSpaceComboBoxActionPerformed

    private void lineNumbersLengthComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineNumbersLengthComboBoxActionPerformed
        codeArea.setLineNumberType(CodeAreaLineNumberLength.LineNumberType.values()[lineNumbersLengthComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_lineNumbersLengthComboBoxActionPerformed

    private void positionCodeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionCodeTypeComboBoxActionPerformed
        codeArea.setPositionCodeType(PositionCodeType.values()[positionCodeTypeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_positionCodeTypeComboBoxActionPerformed

    private void headerSpaceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_headerSpaceSpinnerStateChanged
        codeArea.setHeaderSpaceSize((Integer) headerSpaceSpinner.getValue());
    }//GEN-LAST:event_headerSpaceSpinnerStateChanged

    private void lineNumbersSpaceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineNumbersSpaceSpinnerStateChanged
        codeArea.setLineNumberSpaceSize((Integer) lineNumbersSpaceSpinner.getValue());
    }//GEN-LAST:event_lineNumbersSpaceSpinnerStateChanged

    private void lineNumbersLengthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineNumbersLengthSpinnerStateChanged
        codeArea.setLineNumberSpecifiedLength((Integer) lineNumbersLengthSpinner.getValue());
    }//GEN-LAST:event_lineNumbersLengthSpinnerStateChanged

    private void fontFamilyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontFamilyComboBoxActionPerformed
        int size = codeArea.getFont().getSize();
        switch (fontFamilyComboBox.getSelectedIndex()) {
            case 0: {
                codeArea.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
                break;
            }
            case 1: {
                codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, size));
                break;
            }
            case 2: {
                codeArea.setFont(new Font(Font.SERIF, Font.PLAIN, size));
                break;
            }
        }
    }//GEN-LAST:event_fontFamilyComboBoxActionPerformed

    private void charsetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charsetComboBoxActionPerformed
        codeArea.setCharset(Charset.forName((String) charsetComboBox.getSelectedItem()));
    }//GEN-LAST:event_charsetComboBoxActionPerformed

    private void editationAllowedComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editationAllowedComboBoxActionPerformed
        codeArea.setEditationAllowed(EditationAllowed.values()[editationAllowedComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_editationAllowedComboBoxActionPerformed

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        Component tab = tabbedPane.getSelectedComponent();
        if (tab != null && tab != activeTab && !tabMap.isEmpty()) {
            if (activeTab != null) {
                ((JPanel) activeTab).remove(tabMap.get(activeTab));
            }

            ((JPanel) tab).add(tabMap.get((JPanel) tab), BorderLayout.CENTER);
            activeTab = (JPanel) tab;
        }
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void verticalScrollModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalScrollModeComboBoxActionPerformed
        codeArea.setVerticalScrollMode(CodeArea.VerticalScrollMode.values()[verticalScrollModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_verticalScrollModeComboBoxActionPerformed

    private void verticalScrollBarVisibilityComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalScrollBarVisibilityComboBoxActionPerformed
        codeArea.setVerticalScrollBarVisibility(ScrollBarVisibility.values()[verticalScrollBarVisibilityComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_verticalScrollBarVisibilityComboBoxActionPerformed

    private void horizontalScrollModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horizontalScrollModeComboBoxActionPerformed
        codeArea.setHorizontalScrollMode(CodeArea.HorizontalScrollMode.values()[horizontalScrollModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_horizontalScrollModeComboBoxActionPerformed

    private void horizontalScrollBarVisibilityComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horizontalScrollBarVisibilityComboBoxActionPerformed
        codeArea.setHorizontalScrollBarVisibility(ScrollBarVisibility.values()[horizontalScrollBarVisibilityComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_horizontalScrollBarVisibilityComboBoxActionPerformed

    private void cursorRenderingModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cursorRenderingModeComboBoxActionPerformed
        codeArea.getCaret().setRenderingMode(CodeAreaCaret.CursorRenderingMode.values()[cursorRenderingModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_cursorRenderingModeComboBoxActionPerformed

    private void cursorInsertShapeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cursorInsertShapeComboBoxActionPerformed
        codeArea.getCaret().setInsertCursorShape(CodeAreaCaret.CursorShape.values()[cursorInsertShapeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_cursorInsertShapeComboBoxActionPerformed

    private void cursorOverwriteShapeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cursorOverwriteShapeComboBoxActionPerformed
        codeArea.getCaret().setOverwriteCursorShape(CodeAreaCaret.CursorShape.values()[cursorOverwriteShapeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_cursorOverwriteShapeComboBoxActionPerformed

    private void cursorBlinkingRateSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cursorBlinkingRateSpinnerStateChanged
        codeArea.getCaret().setBlinkRate((Integer) cursorBlinkingRateSpinner.getValue());
    }//GEN-LAST:event_cursorBlinkingRateSpinnerStateChanged

    private void byteGroupSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_byteGroupSizeSpinnerStateChanged
        codeArea.setByteGroupSize((Integer) byteGroupSizeSpinner.getValue());
    }//GEN-LAST:event_byteGroupSizeSpinnerStateChanged

    private void spaceGroupSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spaceGroupSizeSpinnerStateChanged
        codeArea.setSpaceGroupSize((Integer) spaceGroupSizeSpinner.getValue());
    }//GEN-LAST:event_spaceGroupSizeSpinnerStateChanged

    private void fontSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeComboBoxActionPerformed
        Font font = codeArea.getFont();
        Font derivedFont = font.deriveFont(Font.PLAIN, Integer.valueOf((String) fontSizeComboBox.getSelectedItem()));
        codeArea.setFont(derivedFont);
    }//GEN-LAST:event_fontSizeComboBoxActionPerformed

    private void showNonprintableCharactersCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showNonprintableCharactersCheckBoxItemStateChanged
        codeArea.setShowUnprintableCharacters(showNonprintableCharactersCheckBox.isSelected());
    }//GEN-LAST:event_showNonprintableCharactersCheckBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> activeSectionComboBox;
    private javax.swing.JLabel activeSectionLabel;
    private javax.swing.JComboBox<String> backgroundModeComboBox;
    private javax.swing.JLabel backgroundModeLabel;
    private javax.swing.JComboBox<String> borderTypeComboBox;
    private javax.swing.JLabel borderTypeLabel;
    private javax.swing.JLabel byteGroupSizeLabel;
    private javax.swing.JSpinner byteGroupSizeSpinner;
    private javax.swing.JComboBox<String> charAntialiasingComboBox;
    private javax.swing.JLabel charAntialiasingScrollModeLabel;
    private javax.swing.JComboBox<String> charRenderingComboBox;
    private javax.swing.JLabel charRenderingScrollModeLabel;
    private javax.swing.JComboBox<String> charsetComboBox;
    private javax.swing.JLabel charsetLabel;
    private javax.swing.JLabel codeOffsetLabel;
    private javax.swing.JTextField codeOffsetTextField;
    private javax.swing.JComboBox<String> codeTypeComboBox;
    private javax.swing.JLabel codeTypeScrollModeLabel;
    private javax.swing.JLabel cursorBlinkingRateLabel;
    private javax.swing.JSpinner cursorBlinkingRateSpinner;
    private javax.swing.JComboBox<String> cursorInsertShapeComboBox;
    private javax.swing.JLabel cursorInsertShapeModeLabel;
    private javax.swing.JComboBox<String> cursorOverwriteShapeComboBox;
    private javax.swing.JLabel cursorOverwriteShapeModeLabel;
    private javax.swing.JPanel cursorPanel;
    private javax.swing.JComboBox<String> cursorRenderingModeComboBox;
    private javax.swing.JLabel cursorRenderingModeLabel;
    private javax.swing.JPanel cursorTab;
    private javax.swing.JLabel dataSizeLabel;
    private javax.swing.JTextField dataSizeTextField;
    private javax.swing.JPanel decorationPanel;
    private javax.swing.JPanel decorationTab;
    private javax.swing.JCheckBox decoratorBoxCheckBox;
    private javax.swing.JCheckBox decoratorHeaderLineCheckBox;
    private javax.swing.JCheckBox decoratorLineNumLineCheckBox;
    private javax.swing.JCheckBox decoratorSplitLineCheckBox;
    private javax.swing.JComboBox<String> editationAllowedComboBox;
    private javax.swing.JLabel editationAllowedLabel;
    private javax.swing.JComboBox<String> fontFamilyComboBox;
    private javax.swing.JLabel fontFamilyLabel;
    private javax.swing.JPanel fontPanel;
    private javax.swing.JComboBox<String> fontSizeComboBox;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JComboBox<String> headerSpaceComboBox;
    private javax.swing.JLabel headerSpaceLabel;
    private javax.swing.JSpinner headerSpaceSpinner;
    private javax.swing.JComboBox<String> hexCharactersModeComboBox;
    private javax.swing.JLabel hexCharactersModeLabel;
    private javax.swing.JLabel horizontalByteShiftLabel;
    private javax.swing.JTextField horizontalByteShiftTextField;
    private javax.swing.JPanel horizontalPanel;
    private javax.swing.JLabel horizontalPositionLabel;
    private javax.swing.JTextField horizontalPositionTextField;
    private javax.swing.JComboBox<String> horizontalScrollBarVisibilityComboBox;
    private javax.swing.JLabel horizontalScrollBarVisibilityLabel;
    private javax.swing.JComboBox<String> horizontalScrollModeComboBox;
    private javax.swing.JLabel horizontalScrollModeLabel;
    private javax.swing.JPanel layoutPanel;
    private javax.swing.JPanel layoutTab;
    private javax.swing.JLabel lineLengthLabel;
    private javax.swing.JSpinner lineLengthSpinner;
    private javax.swing.JCheckBox lineNumbersBackgroundCheckBox;
    private javax.swing.JComboBox<String> lineNumbersLengthComboBox;
    private javax.swing.JLabel lineNumbersLengthLabel;
    private javax.swing.JSpinner lineNumbersLengthSpinner;
    private javax.swing.JPanel lineNumbersPanel;
    private javax.swing.JComboBox<String> lineNumbersSpaceComboBox;
    private javax.swing.JLabel lineNumbersSpaceLabel;
    private javax.swing.JSpinner lineNumbersSpaceSpinner;
    private javax.swing.JPanel linesPanel;
    private javax.swing.JButton loadDataButton;
    private javax.swing.JPanel modePanel;
    private javax.swing.JPanel modeTab;
    private javax.swing.JComboBox<String> positionCodeTypeComboBox;
    private javax.swing.JLabel positionCodeTypeLabel;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JPanel positionPanel;
    private javax.swing.JTextField positionTextField;
    private javax.swing.JButton saveDataButton;
    private javax.swing.JPanel scrollingPanel;
    private javax.swing.JPanel scrollingTab;
    private javax.swing.JLabel selectionEndLabel;
    private javax.swing.JTextField selectionEndTextField;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.JLabel selectionStartLabel;
    private javax.swing.JTextField selectionStartTextField;
    private javax.swing.JCheckBox showHeaderCheckBox;
    private javax.swing.JCheckBox showLineNumbersCheckBox;
    private javax.swing.JCheckBox showNonprintableCharactersCheckBox;
    private javax.swing.JCheckBox showShadowCursorCheckBox;
    private javax.swing.JLabel spaceGroupSizeLabel;
    private javax.swing.JSpinner spaceGroupSizeSpinner;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JPanel statePanel;
    private javax.swing.JPanel stateTab;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel verticalPanel;
    private javax.swing.JLabel verticalPositionLabel;
    private javax.swing.JTextField verticalPositionTextField;
    private javax.swing.JComboBox<String> verticalScrollBarVisibilityComboBox;
    private javax.swing.JLabel verticalScrollBarVisibilityModeLabel;
    private javax.swing.JComboBox<String> verticalScrollModeComboBox;
    private javax.swing.JLabel verticalScrollModeLabel;
    private javax.swing.JComboBox<String> viewModeComboBox;
    private javax.swing.JLabel viewModeScrollModeLabel;
    private javax.swing.JCheckBox wrapLineModeCheckBox;
    // End of variables declaration//GEN-END:variables

    private Border getBorderByType(int borderTypeIndex) {
        switch (borderTypeIndex) {
            case 0: {
                return null;
            }
            case 1: {
                return new EmptyBorder(5, 5, 5, 5);
            }
            case 2: {
                return new BasicBorders.MarginBorder();
            }
            case 3: {
                return new BevelBorder(BevelBorder.RAISED);
            }
            case 4: {
                return new BevelBorder(BevelBorder.LOWERED);
            }
            case 5: {
                return new EtchedBorder(EtchedBorder.RAISED);
            }
            case 6: {
                return new EtchedBorder(EtchedBorder.LOWERED);
            }
            case 7: {
                return new LineBorder(Color.BLACK);
            }
        }

        return null;
    }
}
