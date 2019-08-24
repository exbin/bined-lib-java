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

import java.awt.Color;
import java.awt.Font;
import java.nio.charset.Charset;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JFrame;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicBorders;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.swing.basic.AntialiasingMode;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.bined.swing.capability.AntialiasingCapable;

/**
 * Binary editor mode options panel.
 *
 * @version 0.2.0 2018/12/11
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ModePanel extends javax.swing.JPanel {

    private CodeArea codeArea;

    public ModePanel() {
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

        editationModeLabel = new javax.swing.JLabel();
        editationModeComboBox = new javax.swing.JComboBox<>();
        fontPanel = new javax.swing.JPanel();
        fontFamilyLabel = new javax.swing.JLabel();
        fontFamilyComboBox = new javax.swing.JComboBox<>();
        fontSizeLabel = new javax.swing.JLabel();
        fontSizeComboBox = new javax.swing.JComboBox<>();
        viewModeScrollModeLabel = new javax.swing.JLabel();
        viewModeComboBox = new javax.swing.JComboBox<>();
        charsetLabel = new javax.swing.JLabel();
        charsetComboBox = new javax.swing.JComboBox<>();
        codeTypeLabel = new javax.swing.JLabel();
        codeTypeComboBox = new javax.swing.JComboBox<>();
        antialiasingLabel = new javax.swing.JLabel();
        antialiasingComboBox = new javax.swing.JComboBox<>();
        borderTypeLabel = new javax.swing.JLabel();
        borderTypeComboBox = new javax.swing.JComboBox<>();

        editationModeLabel.setText("Editation");

        editationModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "READ_ONLY", "EXPANDING", "CAPPED", "INPLACE" }));
        editationModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editationModeComboBoxActionPerformed(evt);
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
                    .addComponent(fontFamilyComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontSizeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        viewModeScrollModeLabel.setText("View Mode");

        viewModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DUAL", "HEXADECIMAL", "PREVIEW" }));
        viewModeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewModeComboBoxActionPerformed(evt);
            }
        });

        charsetLabel.setText("Charset");

        charsetComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTF-8", "UTF-16", "UTF-16BE", "US-ASCII", "IBM852", "ISO-8859-1" }));
        charsetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetComboBoxActionPerformed(evt);
            }
        });

        codeTypeLabel.setText("Code Type");

        codeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BINARY", "OCTAL", "DECIMAL", "HEXADECIMAL" }));
        codeTypeComboBox.setSelectedIndex(3);
        codeTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeTypeComboBoxActionPerformed(evt);
            }
        });

        antialiasingLabel.setText("Character Antialiasing");

        antialiasingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OFF", "AUTO", "DEFAULT", "BASIC", "GASP", "LCD_HRGB", "LCD_HBGR", "LCD_VRGB", "LCD_VBGR" }));
        antialiasingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                antialiasingComboBoxActionPerformed(evt);
            }
        });

        borderTypeLabel.setText("Border Type");

        borderTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "EMPTY BORDER", "MARGIN BORDER", "BEVEL BORDER - RAISED", "BEVEL BORDER - LOWERED", "ETCHED BORDER - RAISED", "ETCHED BORDER - LOWERED", "LINE BORDER" }));
        borderTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borderTypeComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(codeTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(antialiasingComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editationModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(charsetComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(borderTypeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewModeScrollModeLabel)
                            .addComponent(codeTypeLabel)
                            .addComponent(antialiasingLabel)
                            .addComponent(editationModeLabel)
                            .addComponent(charsetLabel)
                            .addComponent(borderTypeLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(viewModeScrollModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(antialiasingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(antialiasingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editationModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editationModeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charsetLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(charsetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(borderTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(borderTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void codeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeTypeComboBoxActionPerformed
        codeArea.setCodeType(CodeType.values()[codeTypeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_codeTypeComboBoxActionPerformed

    private void editationModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editationModeComboBoxActionPerformed
        ((EditationModeCapable) codeArea).setEditationMode(EditationMode.values()[editationModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_editationModeComboBoxActionPerformed

    private void fontFamilyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontFamilyComboBoxActionPerformed
        Font codeFont = codeArea.getCodeFont();
        int size = codeFont == null ? codeArea.getFont().getSize() : codeFont.getSize();
        switch (fontFamilyComboBox.getSelectedIndex()) {
            case 0: {
                codeArea.setCodeFont(new Font(Font.DIALOG, Font.PLAIN, size));
                break;
            }
            case 1: {
                codeArea.setCodeFont(new Font(Font.MONOSPACED, Font.PLAIN, size));
                break;
            }
            case 2: {
                codeArea.setCodeFont(new Font(Font.SERIF, Font.PLAIN, size));
                break;
            }
        }
    }//GEN-LAST:event_fontFamilyComboBoxActionPerformed

    private void fontSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeComboBoxActionPerformed
        Font font = codeArea.getCodeFont();
        Font derivedFont = font.deriveFont(Font.PLAIN, Integer.valueOf((String) fontSizeComboBox.getSelectedItem()));
        codeArea.setCodeFont(derivedFont);
    }//GEN-LAST:event_fontSizeComboBoxActionPerformed

    private void viewModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeComboBoxActionPerformed
        ((ViewModeCapable) codeArea).setViewMode(CodeAreaViewMode.values()[viewModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_viewModeComboBoxActionPerformed

    private void charsetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charsetComboBoxActionPerformed
        ((CharsetCapable) codeArea).setCharset(Charset.forName((String) charsetComboBox.getSelectedItem()));
    }//GEN-LAST:event_charsetComboBoxActionPerformed

    private void antialiasingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_antialiasingComboBoxActionPerformed
        ((AntialiasingCapable) codeArea).setAntialiasingMode(AntialiasingMode.values()[antialiasingComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_antialiasingComboBoxActionPerformed

    private void borderTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borderTypeComboBoxActionPerformed
        codeArea.setBorder(getBorderByType(borderTypeComboBox.getSelectedIndex()));
    }//GEN-LAST:event_borderTypeComboBoxActionPerformed

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        viewModeComboBox.setSelectedIndex(codeArea.getViewMode().ordinal());
        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());
        antialiasingComboBox.setSelectedIndex(((AntialiasingCapable) codeArea).getAntialiasingMode().ordinal());
        editationModeComboBox.setSelectedIndex(codeArea.getEditationMode().ordinal());
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final JFrame frame = new JFrame("Panel");
        frame.setSize(1000, 600);
        frame.add(new ModePanel());
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> antialiasingComboBox;
    private javax.swing.JLabel antialiasingLabel;
    private javax.swing.JComboBox<String> borderTypeComboBox;
    private javax.swing.JLabel borderTypeLabel;
    private javax.swing.JComboBox<String> charsetComboBox;
    private javax.swing.JLabel charsetLabel;
    private javax.swing.JComboBox<String> codeTypeComboBox;
    private javax.swing.JLabel codeTypeLabel;
    private javax.swing.JComboBox<String> editationModeComboBox;
    private javax.swing.JLabel editationModeLabel;
    private javax.swing.JComboBox<String> fontFamilyComboBox;
    private javax.swing.JLabel fontFamilyLabel;
    private javax.swing.JPanel fontPanel;
    private javax.swing.JComboBox<String> fontSizeComboBox;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JComboBox<String> viewModeComboBox;
    private javax.swing.JLabel viewModeScrollModeLabel;
    // End of variables declaration//GEN-END:variables

    @Nullable
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
