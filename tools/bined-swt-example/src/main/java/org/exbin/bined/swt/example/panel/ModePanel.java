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
package org.exbin.bined.swt.example.panel;

import java.nio.charset.Charset;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.swt.basic.CodeArea;

/**
 * Binary editor mode options panel.
 *
 * @version 0.2.0 2018/08/25
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ModePanel extends Composite {

    private CodeArea codeArea;

    private final Combo viewModeCombo;
    private final Combo codeTypeCombo;
    private final Combo antialiasingCombo;
    private final Combo editationModeCombo;
    private final Combo fontFamilyCombo;
    private final Combo fontSizeCombo;
    private final Combo charsetCombo;
    private final Combo borderTypeCombo;

    public ModePanel(Composite parent, int style) {
        super(parent, style);

        setLayout(new FormLayout());

        Label viewModeLabel = new Label(this, SWT.NONE);
        FormData fd_viewModeLabel = new FormData();
        fd_viewModeLabel.top = new FormAttachment(0, 10);
        fd_viewModeLabel.left = new FormAttachment(0, 10);
        fd_viewModeLabel.right = new FormAttachment(100, -10);
        viewModeLabel.setLayoutData(fd_viewModeLabel);
        viewModeLabel.setText("View Mode");

        viewModeCombo = new Combo(this, SWT.READ_ONLY);
        viewModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.setViewMode(CodeAreaViewMode.values()[viewModeCombo.getSelectionIndex()]);
            }
        });
        viewModeCombo.setItems(new String[]{"DUAL", "HEXADECIMAL", "PREVIEW"});
        FormData fd_viewModeCombo = new FormData();
        fd_viewModeCombo.top = new FormAttachment(viewModeLabel, 6);
        fd_viewModeCombo.left = new FormAttachment(0, 10);
        fd_viewModeCombo.right = new FormAttachment(100, -10);
        viewModeCombo.setLayoutData(fd_viewModeCombo);

        Label codeTypelabel = new Label(this, SWT.NONE);
        FormData fd_codeTypelabel = new FormData();
        fd_codeTypelabel.top = new FormAttachment(viewModeCombo, 6);
        fd_codeTypelabel.right = new FormAttachment(100, -10);
        fd_codeTypelabel.left = new FormAttachment(0, 10);
        codeTypelabel.setLayoutData(fd_codeTypelabel);
        codeTypelabel.setText("Code Type");

        codeTypeCombo = new Combo(this, SWT.READ_ONLY);
        codeTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.setCodeType(CodeType.values()[codeTypeCombo.getSelectionIndex()]);
            }
        });
        codeTypeCombo.setItems(new String[]{"BINARY", "OCTAL", "DECIMAL", "HEXADECIMAL"});
        FormData fd_codeTypeCombo = new FormData();
        fd_codeTypeCombo.top = new FormAttachment(codeTypelabel, 6);
        fd_codeTypeCombo.right = new FormAttachment(100, -10);
        fd_codeTypeCombo.left = new FormAttachment(0, 10);
        codeTypeCombo.setLayoutData(fd_codeTypeCombo);

        Label antialiasingLabel = new Label(this, SWT.NONE);
        antialiasingLabel.setText("Character Antialiasing");
        FormData fd_antialiasingLabel = new FormData();
        fd_antialiasingLabel.right = new FormAttachment(100, -10);
        fd_antialiasingLabel.top = new FormAttachment(codeTypeCombo, 6);
        fd_antialiasingLabel.left = new FormAttachment(0, 10);
        antialiasingLabel.setLayoutData(fd_antialiasingLabel);

        antialiasingCombo = new Combo(this, SWT.READ_ONLY);
        antialiasingCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // ((AntialiasingCapable) codeArea).setAntialiasingMode(AntialiasingMode.values()[antialiasingComboBox.getSelectedIndex()]);
            }
        });
        antialiasingCombo.setItems(new String[]{"OFF", "AUTO", "DEFAULT", "BASIC", "GASP", "LCD_HRGB", "LCD_HBGR", "LCD_VRGB", "LCD_VBGR"});
        antialiasingCombo.select(0);
        antialiasingCombo.setEnabled(false);
        FormData fd_antialiasingCombo = new FormData();
        fd_antialiasingCombo.top = new FormAttachment(antialiasingLabel, 6);
        fd_antialiasingCombo.left = new FormAttachment(0, 10);
        fd_antialiasingCombo.right = new FormAttachment(100, -10);
        antialiasingCombo.setLayoutData(fd_antialiasingCombo);

        Label editationModeLabel = new Label(this, SWT.NONE);
        editationModeLabel.setText("Editation");
        FormData fd_editationModeLabel = new FormData();
        fd_editationModeLabel.top = new FormAttachment(antialiasingCombo, 6);
        fd_editationModeLabel.right = new FormAttachment(100, -10);
        fd_editationModeLabel.left = new FormAttachment(0, 10);
        editationModeLabel.setLayoutData(fd_editationModeLabel);

        editationModeCombo = new Combo(this, SWT.READ_ONLY);
        editationModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((EditationModeCapable) codeArea).setEditationMode(EditationMode.values()[editationModeCombo.getSelectionIndex()]);
            }
        });
        editationModeCombo.setItems(new String[]{"READ_ONLY", "EXPANDING", "CAPPED", "INPLACE"});
        FormData fd_editationModeCombo = new FormData();
        fd_editationModeCombo.top = new FormAttachment(editationModeLabel, 6);
        fd_editationModeCombo.right = new FormAttachment(100, -10);
        fd_editationModeCombo.left = new FormAttachment(0, 10);
        editationModeCombo.setLayoutData(fd_editationModeCombo);

        Group fontGroup = new Group(this, SWT.NONE);
        fontGroup.setText("Font");
        fontGroup.setLayout(new FillLayout());
        FormData fd_fontGroup = new FormData();
        fd_fontGroup.top = new FormAttachment(editationModeCombo, 6);
        fd_fontGroup.right = new FormAttachment(100, -10);
        fd_fontGroup.left = new FormAttachment(0, 10);
        fd_fontGroup.height = 150;
        fontGroup.setLayoutData(fd_fontGroup);

        Composite fontGroupComposite = new Composite(fontGroup, SWT.NONE);
        fontGroupComposite.setLayout(new FormLayout());

        Label fontFamilyLabel = new Label(fontGroupComposite, SWT.NONE);
        FormData fd_fontFamilyLabel = new FormData();
        fd_fontFamilyLabel.top = new FormAttachment(0, 10);
        fd_fontFamilyLabel.left = new FormAttachment(0, 10);
        fd_fontFamilyLabel.right = new FormAttachment(100, -10);
        fontFamilyLabel.setLayoutData(fd_fontFamilyLabel);
        fontFamilyLabel.setText("Font Family");

        fontFamilyCombo = new Combo(fontGroupComposite, SWT.READ_ONLY);
        fontFamilyCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Font codeFont = codeArea.getCodeFont();
//                int size = codeFont == null ? 8 : codeFont.getSize();
//                switch (fontFamilyComboBox.getSelectedIndex()) {
//                    case 0: {
//                        codeArea.setCodeFont(new Font(Font.DIALOG, Font.PLAIN, size));
//                        break;
//                    }
//                    case 1: {
//                        codeArea.setCodeFont(new Font(Font.MONOSPACED, Font.PLAIN, size));
//                        break;
//                    }
//                    case 2: {
//                        codeArea.setCodeFont(new Font(Font.SERIF, Font.PLAIN, size));
//                        break;
//                    }
//                }
            }
        });
        fontFamilyCombo.setItems(new String[]{"DIALOG", "MONOSPACE", "SERIF"});
        fontFamilyCombo.select(0);
        FormData fd_fontFamilyCombo = new FormData();
        fd_fontFamilyCombo.top = new FormAttachment(fontFamilyLabel, 6);
        fd_fontFamilyCombo.left = new FormAttachment(0, 10);
        fd_fontFamilyCombo.right = new FormAttachment(100, -10);
        fontFamilyCombo.setLayoutData(fd_fontFamilyCombo);

        Label fontSizeLabel = new Label(fontGroupComposite, SWT.NONE);
        FormData fd_fontSizeLabel = new FormData();
        fd_fontSizeLabel.right = new FormAttachment(0, 370);
        fd_fontSizeLabel.top = new FormAttachment(fontFamilyCombo, 6);
        fd_fontSizeLabel.left = new FormAttachment(0, 10);
        fontSizeLabel.setLayoutData(fd_fontSizeLabel);
        fontSizeLabel.setText("Font Size");

        fontSizeCombo = new Combo(fontGroupComposite, SWT.READ_ONLY);
        fontSizeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Font codeFont = codeArea.getCodeFont();
                // TODO
            }
        });
        fontSizeCombo.setItems(new String[]{"8", "9", "10", "12", "14", "18", "22"});
        fontSizeCombo.select(0);
        FormData fd_fontSizeCombo = new FormData();
        fd_fontSizeCombo.top = new FormAttachment(fontSizeLabel, 6);
        fd_fontSizeCombo.left = new FormAttachment(0, 10);
        fd_fontSizeCombo.right = new FormAttachment(100, -10);
        fontSizeCombo.setLayoutData(fd_fontSizeCombo);

        Label charsetLabel = new Label(this, SWT.NONE);
        charsetLabel.setText("Charset");
        FormData fd_charsetLabel = new FormData();
        fd_charsetLabel.top = new FormAttachment(fontGroup, 6);
        fd_charsetLabel.right = new FormAttachment(100, -10);
        fd_charsetLabel.left = new FormAttachment(0, 10);
        charsetLabel.setLayoutData(fd_charsetLabel);

        charsetCombo = new Combo(this, SWT.READ_ONLY);
        charsetCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((CharsetCapable) codeArea).setCharset(Charset.forName((String) charsetCombo.getItem(charsetCombo.getSelectionIndex())));
            }
        });
        charsetCombo.setItems(new String[]{"UTF-8", "UTF-16", "UTF-16BE", "US-ASCII", "IBM852", "ISO-8859-1"});
        charsetCombo.select(0);
        FormData fd_charsetCombo = new FormData();
        fd_charsetCombo.top = new FormAttachment(charsetLabel, 6);
        fd_charsetCombo.left = new FormAttachment(0, 10);
        fd_charsetCombo.right = new FormAttachment(100, -10);
        charsetCombo.setLayoutData(fd_charsetCombo);

        Label borderTypeLabel = new Label(this, SWT.NONE);
        borderTypeLabel.setText("Border Type");
        FormData fd_borderTypeLabel = new FormData();
        fd_borderTypeLabel.top = new FormAttachment(charsetCombo, 6);
        fd_borderTypeLabel.right = new FormAttachment(100, -10);
        fd_borderTypeLabel.left = new FormAttachment(0, 10);
        borderTypeLabel.setLayoutData(fd_borderTypeLabel);

        borderTypeCombo = new Combo(this, SWT.READ_ONLY);
        borderTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO
            }
        });
        borderTypeCombo.setEnabled(false);
        FormData fd_borderTypeCombo = new FormData();
        fd_borderTypeCombo.top = new FormAttachment(borderTypeLabel, 6);
        fd_borderTypeCombo.left = new FormAttachment(0, 10);
        fd_borderTypeCombo.right = new FormAttachment(100, -10);
        borderTypeCombo.setLayoutData(fd_borderTypeCombo);

    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        viewModeCombo.select(codeArea.getViewMode().ordinal());
        codeTypeCombo.select(codeArea.getCodeType().ordinal());
        // antialiasingCombo.select(((AntialiasingCapable) codeArea).getAntialiasingMode().ordinal());
        editationModeCombo.select(codeArea.getEditationMode().ordinal());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
