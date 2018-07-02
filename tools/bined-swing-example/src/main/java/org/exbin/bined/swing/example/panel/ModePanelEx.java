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

import java.awt.Font;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.swing.CodeArea;
import org.exbin.bined.swing.extended.AntialiasingMode;
import org.exbin.bined.swing.extended.CharacterRenderingMode;
import org.exbin.bined.swing.extended.ExtCodeAreaWorker;
import org.exbin.bined.swing.extended.capability.AntialiasingCapable;

/**
 * Hexadecimal editor example panel.
 *
 * @version 0.2.0 2018/03/22
 * @author ExBin Project (http://exbin.org)
 */
public class ModePanelEx extends javax.swing.JPanel {

    private final CodeArea codeArea;
    private final ExtCodeAreaWorker worker;

    public ModePanelEx(@Nonnull CodeArea codeArea) {
        this.codeArea = codeArea;
        worker = (ExtCodeAreaWorker) codeArea.getWorker();

        initComponents();

        viewModeComboBox.setSelectedIndex(worker.getViewMode().ordinal());
        codeTypeComboBox.setSelectedIndex(worker.getCodeType().ordinal());
        charRenderingComboBox.setSelectedIndex(worker.getCharacterRenderingMode().ordinal());
        editationAllowedComboBox.setSelectedIndex(worker.getEditationMode().ordinal());
        charAntialiasingComboBox.setSelectedIndex(((AntialiasingCapable) worker).getAntialiasingMode().ordinal());
//        showNonprintableCharactersCheckBox.setSelected(codeArea.isShowUnprintableCharacters());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showNonprintableCharactersCheckBox = new javax.swing.JCheckBox();
        codeTypeComboBox = new javax.swing.JComboBox<>();
        editationAllowedLabel = new javax.swing.JLabel();
        editationAllowedComboBox = new javax.swing.JComboBox<>();
        fontPanel = new javax.swing.JPanel();
        fontFamilyLabel = new javax.swing.JLabel();
        fontFamilyComboBox = new javax.swing.JComboBox<>();
        fontSizeLabel = new javax.swing.JLabel();
        fontSizeComboBox = new javax.swing.JComboBox<>();
        viewModeScrollModeLabel = new javax.swing.JLabel();
        viewModeComboBox = new javax.swing.JComboBox<>();
        charRenderingScrollModeLabel = new javax.swing.JLabel();
        charRenderingComboBox = new javax.swing.JComboBox<>();
        charAntialiasingScrollModeLabel = new javax.swing.JLabel();
        charsetLabel = new javax.swing.JLabel();
        charAntialiasingComboBox = new javax.swing.JComboBox<>();
        charsetComboBox = new javax.swing.JComboBox<>();
        codeTypeScrollModeLabel = new javax.swing.JLabel();

        showNonprintableCharactersCheckBox.setText("Show Nonprintable Characters");
        showNonprintableCharactersCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showNonprintableCharactersCheckBoxItemStateChanged(evt);
            }
        });

        codeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BINARY", "OCTAL", "DECIMAL", "HEXADECIMAL" }));
        codeTypeComboBox.setSelectedIndex(3);
        codeTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeTypeComboBoxActionPerformed(evt);
            }
        });

        editationAllowedLabel.setText("Editation");

        editationAllowedComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "READ_ONLY", "INSERT", "OVERWRITE", "INPLACE" }));
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
                    .addComponent(fontFamilyComboBox, 0, 272, Short.MAX_VALUE)
                    .addComponent(fontSizeComboBox, 0, 272, Short.MAX_VALUE)
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

        charRenderingScrollModeLabel.setText("Character Rendering");

        charRenderingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "LINE_AT_ONCE", "TOP_LEFT", "CENTER" }));
        charRenderingComboBox.setEnabled(false);
        charRenderingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charRenderingComboBoxActionPerformed(evt);
            }
        });

        charAntialiasingScrollModeLabel.setText("Character Antialiasing");

        charsetLabel.setText("Charset");

        charAntialiasingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OFF", "AUTO", "DEFAULT", "BASIC", "GASP", "LCD_HRGB", "LCD_HBGR", "LCD_VRGB", "LCD_VBGR" }));
        charAntialiasingComboBox.setEnabled(false);
        charAntialiasingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charAntialiasingComboBoxActionPerformed(evt);
            }
        });

        charsetComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTF-8", "UTF-16", "UTF-16BE", "US-ASCII", "IBM852", "ISO-8859-1" }));
        charsetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetComboBoxActionPerformed(evt);
            }
        });

        codeTypeScrollModeLabel.setText("Code Type");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewModeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showNonprintableCharactersCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(charRenderingComboBox, 0, 294, Short.MAX_VALUE)
                    .addComponent(codeTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(charAntialiasingComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editationAllowedComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(charsetComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewModeScrollModeLabel)
                            .addComponent(charRenderingScrollModeLabel)
                            .addComponent(charAntialiasingScrollModeLabel)
                            .addComponent(codeTypeScrollModeLabel)
                            .addComponent(editationAllowedLabel)
                            .addComponent(charsetLabel))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showNonprintableCharactersCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showNonprintableCharactersCheckBoxItemStateChanged
        //        codeArea.setShowUnprintableCharacters(showNonprintableCharactersCheckBox.isSelected());
    }//GEN-LAST:event_showNonprintableCharactersCheckBoxItemStateChanged

    private void codeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeTypeComboBoxActionPerformed
        worker.setCodeType(CodeType.values()[codeTypeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_codeTypeComboBoxActionPerformed

    private void editationAllowedComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editationAllowedComboBoxActionPerformed
        ((EditationModeCapable) codeArea.getWorker()).setEditationMode(EditationMode.values()[editationAllowedComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_editationAllowedComboBoxActionPerformed

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

    private void fontSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeComboBoxActionPerformed
        Font font = codeArea.getFont();
        Font derivedFont = font.deriveFont(Font.PLAIN, Integer.valueOf((String) fontSizeComboBox.getSelectedItem()));
        codeArea.setFont(derivedFont);
    }//GEN-LAST:event_fontSizeComboBoxActionPerformed

    private void viewModeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeComboBoxActionPerformed
        ((ViewModeCapable) codeArea.getWorker()).setViewMode(CodeAreaViewMode.values()[viewModeComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_viewModeComboBoxActionPerformed

    private void charRenderingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charRenderingComboBoxActionPerformed
        worker.setCharacterRenderingMode(CharacterRenderingMode.values()[charRenderingComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_charRenderingComboBoxActionPerformed

    private void charAntialiasingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charAntialiasingComboBoxActionPerformed
        ((AntialiasingCapable) worker).setAntialiasingMode(AntialiasingMode.values()[charAntialiasingComboBox.getSelectedIndex()]);
    }//GEN-LAST:event_charAntialiasingComboBoxActionPerformed

    private void charsetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charsetComboBoxActionPerformed
        ((CharsetCapable) codeArea.getWorker()).setCharset(Charset.forName((String) charsetComboBox.getSelectedItem()));
    }//GEN-LAST:event_charsetComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> charAntialiasingComboBox;
    private javax.swing.JLabel charAntialiasingScrollModeLabel;
    private javax.swing.JComboBox<String> charRenderingComboBox;
    private javax.swing.JLabel charRenderingScrollModeLabel;
    private javax.swing.JComboBox<String> charsetComboBox;
    private javax.swing.JLabel charsetLabel;
    private javax.swing.JComboBox<String> codeTypeComboBox;
    private javax.swing.JLabel codeTypeScrollModeLabel;
    private javax.swing.JComboBox<String> editationAllowedComboBox;
    private javax.swing.JLabel editationAllowedLabel;
    private javax.swing.JComboBox<String> fontFamilyComboBox;
    private javax.swing.JLabel fontFamilyLabel;
    private javax.swing.JPanel fontPanel;
    private javax.swing.JComboBox<String> fontSizeComboBox;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JCheckBox showNonprintableCharactersCheckBox;
    private javax.swing.JComboBox<String> viewModeComboBox;
    private javax.swing.JLabel viewModeScrollModeLabel;
    // End of variables declaration//GEN-END:variables
}
