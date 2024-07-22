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
package org.exbin.bined.swing.example.panel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.swing.example.BinEdExampleBasicPanel;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditMode;
import org.exbin.bined.swing.example.BinEdExample;
import org.exbin.bined.swing.section.SectionCodeAreaPainter;

/**
 * Binary editor state panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatePanelEx extends javax.swing.JPanel {

    private SectCodeArea codeArea;

    public StatePanelEx() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataSizeLabel = new javax.swing.JLabel();
        dataSizeTextField = new javax.swing.JTextField();
        loadDataButton = new javax.swing.JButton();
        saveDataButton = new javax.swing.JButton();
        testDataComboBox = new javax.swing.JComboBox<>();
        activeOperationLabel = new javax.swing.JLabel();
        activeOperationComboBox = new javax.swing.JComboBox<>();
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

        testDataComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Load Test Data >>", "Max Scroll Data", "Min NonScroll Data", "Half Max Data", "Max Data" }));
        testDataComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                testDataComboBoxItemStateChanged(evt);
            }
        });

        activeOperationLabel.setText("Active Edit Operation");

        activeOperationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "INSERT", "OVERWRITE" }));
        activeOperationComboBox.setSelectedIndex(1);
        activeOperationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeOperationComboBoxActionPerformed(evt);
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
                        .addGap(0, 0, Short.MAX_VALUE))
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataSizeTextField)
                    .addComponent(testDataComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(selectionPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(positionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(activeOperationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dataSizeLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(loadDataButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveDataButton))
                            .addComponent(activeOperationLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(dataSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadDataButton)
                    .addComponent(saveDataButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testDataComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(activeOperationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(activeOperationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(positionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

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
                    codeArea.getContentData().saveToStream(stream);
                }
            } catch (IOException ex) {
                Logger.getLogger(BinEdExampleBasicPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_saveDataButtonActionPerformed

    private void activeSectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeSectionComboBoxActionPerformed
        codeArea.getCaret().setSection(BasicCodeAreaSection.values()[activeSectionComboBox.getSelectedIndex()]);
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
                    CodeAreaUtils.requireNonNull(((EditableBinaryData) codeArea.getContentData())).loadFromStream(stream);
                    codeArea.notifyDataChanged();
                    //                    codeArea.resetPosition();
                }
            } catch (IOException ex) {
                Logger.getLogger(BinEdExampleBasicPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_loadDataButtonActionPerformed

    private void activeOperationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeOperationComboBoxActionPerformed
        codeArea.setEditOperation(EditOperation.values()[activeOperationComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_activeOperationComboBoxActionPerformed

    private void testDataComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_testDataComboBoxItemStateChanged
        int selectedIndex = testDataComboBox.getSelectedIndex();
        if (selectedIndex > 0) {
            SectionCodeAreaPainter painter = (SectionCodeAreaPainter) codeArea.getPainter();
            int rowHeight = painter.getRowHeight();
            long bytesPerRow = painter.getBytesPerRow();
            switch (selectedIndex) {
                case 1: {
                    switch (codeArea.getVerticalScrollUnit()) {
                        case PIXEL: {
                            long dataSize = ((Integer.MAX_VALUE / rowHeight) - 1) * bytesPerRow;
                            codeArea.setContentData(BinEdExample.getBigSampleData(0, dataSize));
                            break;
                        }
                        case ROW: {
                            long dataSize = ((Integer.MAX_VALUE) - 1) * bytesPerRow;
                            codeArea.setContentData(BinEdExample.getBigSampleData(0, dataSize));
                            break;
                        }
                    }
                    break;
                }
                case 2: {
                    switch (codeArea.getVerticalScrollUnit()) {
                        case PIXEL: {
                            long dataSize = ((long) (Integer.MAX_VALUE / rowHeight) + 1) * bytesPerRow;
                            codeArea.setContentData(BinEdExample.getBigSampleData(0, dataSize));
                            break;
                        }
                        case ROW: {
                            long dataSize = ((long) (Integer.MAX_VALUE) + 1) * bytesPerRow;
                            codeArea.setContentData(BinEdExample.getBigSampleData(0, dataSize));
                            break;
                        }
                    }
                    break;
                }
                case 3: {
                    codeArea.setContentData(BinEdExample.getBigSampleData(0, Long.MAX_VALUE / 2));
                    break;
                }
                case 4: {
                    codeArea.setContentData(BinEdExample.getBigSampleData(0, Long.MAX_VALUE - 1));
                    break;
                }
            }
            codeArea.setEditMode(EditMode.READ_ONLY);
            testDataComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_testDataComboBoxItemStateChanged

    public void setCodeArea(SectCodeArea codeArea) {
        this.codeArea = codeArea;

        dataSizeTextField.setText(String.valueOf(codeArea.getDataSize()));

        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            positionTextField.setText(String.valueOf(caretPosition.getDataPosition()));
            codeOffsetTextField.setText(String.valueOf(caretPosition.getCodeOffset()));
            activeSectionComboBox.setSelectedIndex(getSection(caretPosition).ordinal());
        });
        ((SelectionCapable) codeArea).addSelectionChangedListener(() -> {
            SelectionRange selection = codeArea.getSelection();
            if (!selection.isEmpty()) {
                long first = selection.getFirst();
                selectionStartTextField.setText(String.valueOf(first));
                long last = selection.getLast();
                selectionEndTextField.setText(String.valueOf(last));
            } else {
                selectionStartTextField.setText("");
                selectionEndTextField.setText("");
            }
        });
        codeArea.addDataChangedListener(() -> {
            dataSizeTextField.setText(String.valueOf(codeArea.getDataSize()));
        });
        codeArea.addEditModeChangedListener((editMode, editOperation) -> {
            activeOperationComboBox.setSelectedIndex(editOperation.ordinal());
        });
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final JFrame frame = new JFrame("Panel");
        frame.setSize(1000, 600);
        frame.add(new StatePanelEx());
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> activeOperationComboBox;
    private javax.swing.JLabel activeOperationLabel;
    private javax.swing.JComboBox<String> activeSectionComboBox;
    private javax.swing.JLabel activeSectionLabel;
    private javax.swing.JLabel codeOffsetLabel;
    private javax.swing.JTextField codeOffsetTextField;
    private javax.swing.JLabel dataSizeLabel;
    private javax.swing.JTextField dataSizeTextField;
    private javax.swing.JButton loadDataButton;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JPanel positionPanel;
    private javax.swing.JTextField positionTextField;
    private javax.swing.JButton saveDataButton;
    private javax.swing.JLabel selectionEndLabel;
    private javax.swing.JTextField selectionEndTextField;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.JLabel selectionStartLabel;
    private javax.swing.JTextField selectionStartTextField;
    private javax.swing.JComboBox<String> testDataComboBox;
    // End of variables declaration//GEN-END:variables

    @Nonnull
    private BasicCodeAreaSection getSection(CodeAreaCaretPosition caretPosition) {
        return (BasicCodeAreaSection) caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
    }
}
