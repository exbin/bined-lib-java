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
package org.exbin.bined.swing.example.panel;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaLayoutProfile;

/**
 * Hexadecimal editor example panel.
 *
 * @version 0.2.0 2018/12/06
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class LayoutPanelEx extends javax.swing.JPanel {

    private final ExtCodeArea codeArea;

    public LayoutPanelEx(ExtCodeArea codeArea) {
        this.codeArea = codeArea;

        initComponents();
        wrapLineModeCheckBox.setSelected(codeArea.getRowWrapping() == RowWrappingCapable.RowWrappingMode.WRAPPING);
        maxBytesPerRowSpinner.setValue(codeArea.getMaxBytesPerRow());
        minRowPositionLengthSpinner.setValue(codeArea.getMinRowPositionLength());
        showHeaderCheckBox.setSelected(codeArea.getLayoutProfile().isShowHeader());
//        headerSpaceComboBox.setSelectedIndex(codeArea.getHeaderSpaceType().ordinal());
//        headerSpaceSpinner.setValue(codeArea.getHeaderSpaceSize());
        showRowPositionCheckBox.setSelected(codeArea.getLayoutProfile().isShowRowPosition());
//        rowPositionSpaceComboBox.setSelectedIndex(codeArea.getLineNumberSpaceType().ordinal());
//        rowPositionSpaceSpinner.setValue(codeArea.getLineNumberSpaceSize());
//        rowPositionLengthComboBox.setSelectedIndex(codeArea.getLineNumberType().ordinal());
//        rowPositionLengthSpinner.setValue(codeArea.getLineNumberSpecifiedLength());
//        byteGroupSizeSpinner.setValue(codeArea.getByteGroupSize());
//        spaceGroupSizeSpinner.setValue(codeArea.getSpaceGroupSize());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        maxBytesPerRowLabel = new javax.swing.JLabel();
        maxBytesPerRowSpinner = new javax.swing.JSpinner();
        headerPanel = new javax.swing.JPanel();
        showHeaderCheckBox = new javax.swing.JCheckBox();
        headerSpaceLabel = new javax.swing.JLabel();
        headerSpaceComboBox = new javax.swing.JComboBox<>();
        headerSpaceSpinner = new javax.swing.JSpinner();
        rowPositionPanel = new javax.swing.JPanel();
        showRowPositionCheckBox = new javax.swing.JCheckBox();
        minRowPositionLengthLabel = new javax.swing.JLabel();
        minRowPositionLengthSpinner = new javax.swing.JSpinner();
        rowPositionSpaceLabel = new javax.swing.JLabel();
        rowPositionSpaceComboBox = new javax.swing.JComboBox<>();
        rowPositionSpaceSpinner = new javax.swing.JSpinner();
        byteGroupSizeLabel = new javax.swing.JLabel();
        byteGroupSizeSpinner = new javax.swing.JSpinner();
        spaceGroupSizeLabel = new javax.swing.JLabel();
        spaceGroupSizeSpinner = new javax.swing.JSpinner();
        wrapLineModeCheckBox = new javax.swing.JCheckBox();

        maxBytesPerRowLabel.setText("Maximum Bytes Per Row");

        maxBytesPerRowSpinner.setModel(new javax.swing.SpinnerNumberModel(16, 1, null, 1));
        maxBytesPerRowSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxBytesPerRowSpinnerStateChanged(evt);
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
                        .addComponent(headerSpaceSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addComponent(headerSpaceLabel)
                        .addGap(0, 267, Short.MAX_VALUE))
                    .addComponent(showHeaderCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        rowPositionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Row Positions"));

        showRowPositionCheckBox.setText("Show Row Position");
        showRowPositionCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showRowPositionCheckBoxItemStateChanged(evt);
            }
        });

        minRowPositionLengthLabel.setText("Minimal Row Position Length");

        minRowPositionLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        minRowPositionLengthSpinner.setValue(8);
        minRowPositionLengthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minRowPositionLengthSpinnerStateChanged(evt);
            }
        });

        rowPositionSpaceLabel.setText("Row Position Space");

        rowPositionSpaceComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "SPECIFIED", "QUARTER_UNIT", "HALF_UNIT", "ONE_UNIT", "ONE_AND_HALF_UNIT", "DOUBLE_UNIT" }));
        rowPositionSpaceComboBox.setSelectedIndex(4);
        rowPositionSpaceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rowPositionSpaceComboBoxActionPerformed(evt);
            }
        });

        rowPositionSpaceSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        rowPositionSpaceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rowPositionSpaceSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout rowPositionPanelLayout = new javax.swing.GroupLayout(rowPositionPanel);
        rowPositionPanel.setLayout(rowPositionPanelLayout);
        rowPositionPanelLayout.setHorizontalGroup(
            rowPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rowPositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rowPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(minRowPositionLengthSpinner)
                    .addComponent(showRowPositionCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rowPositionPanelLayout.createSequentialGroup()
                        .addComponent(rowPositionSpaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rowPositionSpaceSpinner))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rowPositionPanelLayout.createSequentialGroup()
                        .addGroup(rowPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minRowPositionLengthLabel)
                            .addComponent(rowPositionSpaceLabel))
                        .addGap(0, 162, Short.MAX_VALUE)))
                .addContainerGap())
        );
        rowPositionPanelLayout.setVerticalGroup(
            rowPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rowPositionPanelLayout.createSequentialGroup()
                .addComponent(showRowPositionCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minRowPositionLengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minRowPositionLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rowPositionSpaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rowPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rowPositionSpaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rowPositionSpaceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        wrapLineModeCheckBox.setText("Wrap Line Mode");
        wrapLineModeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                wrapLineModeCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxBytesPerRowLabel)
                            .addComponent(wrapLineModeCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(spaceGroupSizeSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(byteGroupSizeSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headerPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(maxBytesPerRowSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rowPositionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(byteGroupSizeLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spaceGroupSizeLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wrapLineModeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxBytesPerRowLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxBytesPerRowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rowPositionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(byteGroupSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(byteGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spaceGroupSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spaceGroupSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rowPositionPanel.getAccessibleContext().setAccessibleName("Row Position");
    }// </editor-fold>//GEN-END:initComponents

    private void showHeaderCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showHeaderCheckBoxItemStateChanged
        ExtendedCodeAreaLayoutProfile layoutProfile = codeArea.getLayoutProfile();
        layoutProfile.setShowHeader(showHeaderCheckBox.isSelected());
        codeArea.setLayoutProfile(layoutProfile);
    }//GEN-LAST:event_showHeaderCheckBoxItemStateChanged

    private void headerSpaceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headerSpaceComboBoxActionPerformed
        //        codeArea.setHeaderSpaceType(SpaceType.values()[headerSpaceComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_headerSpaceComboBoxActionPerformed

    private void headerSpaceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_headerSpaceSpinnerStateChanged
        //        codeArea.setHeaderSpaceSize((Integer) headerSpaceSpinner.getValue());
    }//GEN-LAST:event_headerSpaceSpinnerStateChanged

    private void maxBytesPerRowSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxBytesPerRowSpinnerStateChanged
        //        int value = (Integer) lineLengthSpinner.getValue();
        //        if (value > 0) {
        //            codeArea.setLineLength(value);
        //        }
    }//GEN-LAST:event_maxBytesPerRowSpinnerStateChanged

    private void showRowPositionCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showRowPositionCheckBoxItemStateChanged
        ExtendedCodeAreaLayoutProfile layoutProfile = codeArea.getLayoutProfile();
        layoutProfile.setShowRowPosition(showRowPositionCheckBox.isSelected());
        codeArea.setLayoutProfile(layoutProfile);
    }//GEN-LAST:event_showRowPositionCheckBoxItemStateChanged

    private void minRowPositionLengthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minRowPositionLengthSpinnerStateChanged
        //        codeArea.setLineNumberSpecifiedLength((Integer) lineNumbersLengthSpinner.getValue());
    }//GEN-LAST:event_minRowPositionLengthSpinnerStateChanged

    private void rowPositionSpaceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rowPositionSpaceComboBoxActionPerformed
        //        codeArea.setLineNumberSpaceType(SpaceType.values()[lineNumbersSpaceComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_rowPositionSpaceComboBoxActionPerformed

    private void rowPositionSpaceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rowPositionSpaceSpinnerStateChanged
        //        codeArea.setLineNumberSpaceSize((Integer) lineNumbersSpaceSpinner.getValue());
    }//GEN-LAST:event_rowPositionSpaceSpinnerStateChanged

    private void byteGroupSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_byteGroupSizeSpinnerStateChanged
        //        codeArea.setByteGroupSize((Integer) byteGroupSizeSpinner.getValue());
    }//GEN-LAST:event_byteGroupSizeSpinnerStateChanged

    private void spaceGroupSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spaceGroupSizeSpinnerStateChanged
        //        codeArea.setSpaceGroupSize((Integer) spaceGroupSizeSpinner.getValue());
    }//GEN-LAST:event_spaceGroupSizeSpinnerStateChanged

    private void wrapLineModeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_wrapLineModeCheckBoxItemStateChanged
        codeArea.setRowWrapping(wrapLineModeCheckBox.isSelected() ? RowWrappingCapable.RowWrappingMode.NO_WRAPPING : RowWrappingCapable.RowWrappingMode.NO_WRAPPING);
    }//GEN-LAST:event_wrapLineModeCheckBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel byteGroupSizeLabel;
    private javax.swing.JSpinner byteGroupSizeSpinner;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JComboBox<String> headerSpaceComboBox;
    private javax.swing.JLabel headerSpaceLabel;
    private javax.swing.JSpinner headerSpaceSpinner;
    private javax.swing.JLabel maxBytesPerRowLabel;
    private javax.swing.JSpinner maxBytesPerRowSpinner;
    private javax.swing.JLabel minRowPositionLengthLabel;
    private javax.swing.JSpinner minRowPositionLengthSpinner;
    private javax.swing.JPanel rowPositionPanel;
    private javax.swing.JComboBox<String> rowPositionSpaceComboBox;
    private javax.swing.JLabel rowPositionSpaceLabel;
    private javax.swing.JSpinner rowPositionSpaceSpinner;
    private javax.swing.JCheckBox showHeaderCheckBox;
    private javax.swing.JCheckBox showRowPositionCheckBox;
    private javax.swing.JLabel spaceGroupSizeLabel;
    private javax.swing.JSpinner spaceGroupSizeSpinner;
    private javax.swing.JCheckBox wrapLineModeCheckBox;
    // End of variables declaration//GEN-END:variables
}
