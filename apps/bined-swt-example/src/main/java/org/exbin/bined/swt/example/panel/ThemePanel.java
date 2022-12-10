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
package org.exbin.bined.swt.example.panel;

import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.swt.basic.CodeArea;
import org.exbin.bined.capability.BackgroundPaintCapable;

/**
 * Binary editor theme options panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ThemePanel extends Composite {

    private CodeArea codeArea;

    private final Combo backgroundModeCombo;

    public ThemePanel(Composite parent, int style) {
        super(parent, style);

        setLayout(new FormLayout());

        Label backgroundModeLabel = new Label(this, SWT.NONE);
        FormData fd_backgroundModeLabel = new FormData();
        fd_backgroundModeLabel.top = new FormAttachment(0, 10);
        fd_backgroundModeLabel.left = new FormAttachment(0, 10);
        fd_backgroundModeLabel.right = new FormAttachment(100, -10);
        backgroundModeLabel.setLayoutData(fd_backgroundModeLabel);
        backgroundModeLabel.setText("Background Mode");

        backgroundModeCombo = new Combo(this, SWT.READ_ONLY);
        backgroundModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((BackgroundPaintCapable) codeArea).setBackgroundPaintMode(BasicBackgroundPaintMode.values()[backgroundModeCombo.getSelectionIndex()]);
            }
        });
        backgroundModeCombo.setItems(new String[]{"NONE", "PLAIN", "STRIPED"});
        FormData fd_activeOperationCombo = new FormData();
        fd_activeOperationCombo.top = new FormAttachment(backgroundModeLabel, 6);
        fd_activeOperationCombo.left = new FormAttachment(0, 10);
        fd_activeOperationCombo.right = new FormAttachment(100, -10);
        backgroundModeCombo.setLayoutData(fd_activeOperationCombo);

    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        backgroundModeCombo.select(((BackgroundPaintCapable) codeArea).getBackgroundPaintMode().ordinal());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
