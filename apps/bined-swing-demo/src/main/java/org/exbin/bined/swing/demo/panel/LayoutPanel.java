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
package org.exbin.bined.swing.demo.panel;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JFrame;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.swing.basic.CodeArea;

/**
 * Binary editor layout options panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class LayoutPanel extends javax.swing.JPanel {

    private CodeArea codeArea;

    public LayoutPanel() {
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

        rowWrappingModeCheckBox = new javax.swing.JCheckBox();
        maxBytesPerRowLabel = new javax.swing.JLabel();
        maxBytesPerRowSpinner = new javax.swing.JSpinner();
        minRowPositionLengthLabel = new javax.swing.JLabel();
        minRowPositionLengthSpinner = new javax.swing.JSpinner();
        maxRowPositionLengthLabel = new javax.swing.JLabel();
        maxRowPositionLengthSpinner = new javax.swing.JSpinner();

        rowWrappingModeCheckBox.setText("Row Wrapping Mode");
        rowWrappingModeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rowWrappingModeCheckBoxItemStateChanged(evt);
            }
        });

        maxBytesPerRowLabel.setText("Maximum Bytes Per Row");

        maxBytesPerRowSpinner.setModel(new javax.swing.SpinnerNumberModel(16, 0, null, 1));
        maxBytesPerRowSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxBytesPerRowSpinnerStateChanged(evt);
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

        maxRowPositionLengthLabel.setText("Maximal Row Position Length");

        maxRowPositionLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        maxRowPositionLengthSpinner.setValue(8);
        maxRowPositionLengthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxRowPositionLengthSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(maxRowPositionLengthSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minRowPositionLengthSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxBytesPerRowSpinner, javax.swing.GroupLayout.Alignment.LEADING))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxBytesPerRowLabel)
                            .addComponent(rowWrappingModeCheckBox)
                            .addComponent(minRowPositionLengthLabel)
                            .addComponent(maxRowPositionLengthLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rowWrappingModeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxBytesPerRowLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxBytesPerRowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minRowPositionLengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minRowPositionLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxRowPositionLengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxRowPositionLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void maxBytesPerRowSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxBytesPerRowSpinnerStateChanged
        int value = (Integer) maxBytesPerRowSpinner.getValue();
        codeArea.setMaxBytesPerRow(value);
    }//GEN-LAST:event_maxBytesPerRowSpinnerStateChanged

    private void minRowPositionLengthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minRowPositionLengthSpinnerStateChanged
        codeArea.setMinRowPositionLength((Integer) minRowPositionLengthSpinner.getValue());
    }//GEN-LAST:event_minRowPositionLengthSpinnerStateChanged

    private void rowWrappingModeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rowWrappingModeCheckBoxItemStateChanged
        codeArea.setRowWrapping(rowWrappingModeCheckBox.isSelected() ? RowWrappingMode.WRAPPING : RowWrappingMode.NO_WRAPPING);
    }//GEN-LAST:event_rowWrappingModeCheckBoxItemStateChanged

    private void maxRowPositionLengthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxRowPositionLengthSpinnerStateChanged
        codeArea.setMaxRowPositionLength((Integer) maxRowPositionLengthSpinner.getValue());
    }//GEN-LAST:event_maxRowPositionLengthSpinnerStateChanged

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        rowWrappingModeCheckBox.setSelected(codeArea.getRowWrapping() == RowWrappingMode.WRAPPING);
        maxBytesPerRowSpinner.setValue(codeArea.getMaxBytesPerRow());
        minRowPositionLengthSpinner.setValue(codeArea.getMinRowPositionLength());
        maxRowPositionLengthSpinner.setValue(codeArea.getMaxRowPositionLength());
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final JFrame frame = new JFrame("Panel");
        frame.setSize(1000, 600);
        frame.add(new LayoutPanel());
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel maxBytesPerRowLabel;
    private javax.swing.JSpinner maxBytesPerRowSpinner;
    private javax.swing.JLabel maxRowPositionLengthLabel;
    private javax.swing.JSpinner maxRowPositionLengthSpinner;
    private javax.swing.JLabel minRowPositionLengthLabel;
    private javax.swing.JSpinner minRowPositionLengthSpinner;
    private javax.swing.JCheckBox rowWrappingModeCheckBox;
    // End of variables declaration//GEN-END:variables
}
