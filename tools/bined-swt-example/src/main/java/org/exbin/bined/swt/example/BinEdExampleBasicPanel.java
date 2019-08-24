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
package org.exbin.bined.swt.example;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.exbin.bined.swt.basic.CodeArea;
import org.exbin.bined.swt.example.panel.ModePanel;

/**
 * Basic binary component example panel.
 *
 * @version 0.2.0 2019/08/24
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdExampleBasicPanel extends Composite {

    private TabFolder tabFolder;

    public BinEdExampleBasicPanel() {
        super(null, SWT.NULL);

        initComponents();
    }

    public BinEdExampleBasicPanel(Composite parent, int i) {
        super(parent, i);

        initComponents();
    }

    public void initComponents() {
        setLayout(new FillLayout());
        tabFolder = new TabFolder(this, SWT.BORDER);
        tabFolder.setLayout(new FillLayout());

        TabItem modeTabItem = new TabItem(tabFolder, SWT.NULL);
        modeTabItem.setText("Mode");
    }

    public void setCodeArea(CodeArea codeArea) {
        ScrolledComposite modePanelWrapper = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
        modePanelWrapper.setLayout(new FillLayout());
        modePanelWrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final ModePanel modePanel = new ModePanel(modePanelWrapper, SWT.NONE);
        modePanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        modePanelWrapper.setContent(modePanel);
        modePanelWrapper.setExpandHorizontal(true);
        modePanelWrapper.setExpandVertical(true);
        modePanelWrapper.setMinSize(modePanel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        modePanel.setCodeArea(codeArea);
    }
}
