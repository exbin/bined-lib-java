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

import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Spinner;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.swt.basic.CodeArea;

/**
 * Binary editor layout options panel.
 *
 * @version 0.2.0 2019/09/18
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class LayoutPanel extends Composite {

    private CodeArea codeArea;

    private final Button rowWrappingModeCheckButton;
    private final Spinner maxBytesPerRowSpinner;
    private final Spinner minRowPositionLengthSpinner;
    private final Spinner maxRowPositionLengthSpinner;

    public LayoutPanel(Composite parent, int style) {
        super(parent, style);

        setLayout(new FormLayout());

        rowWrappingModeCheckButton = new Button(this, SWT.CHECK);
        rowWrappingModeCheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.setRowWrapping(rowWrappingModeCheckButton.getSelection() ? RowWrappingMode.WRAPPING : RowWrappingMode.NO_WRAPPING);
            }
        });
        FormData fd_rowWrappingModeCheckButton = new FormData();
        fd_rowWrappingModeCheckButton.top = new FormAttachment(0, 10);
        fd_rowWrappingModeCheckButton.right = new FormAttachment(100, -10);
        fd_rowWrappingModeCheckButton.left = new FormAttachment(0, 10);
        rowWrappingModeCheckButton.setLayoutData(fd_rowWrappingModeCheckButton);
        rowWrappingModeCheckButton.setText("Row Wrapping Mode");

        Label maxBytesPerRowLabel = new Label(this, SWT.NONE);
        FormData fd_maxBytesPerRowLabel = new FormData();
        fd_maxBytesPerRowLabel.top = new FormAttachment(rowWrappingModeCheckButton, 6);
        fd_maxBytesPerRowLabel.left = new FormAttachment(0, 10);
        fd_maxBytesPerRowLabel.right = new FormAttachment(100, -10);
        maxBytesPerRowLabel.setLayoutData(fd_maxBytesPerRowLabel);
        maxBytesPerRowLabel.setText("Maximum Bytes Per Row");

        maxBytesPerRowSpinner = new Spinner(this, SWT.BORDER);
        maxBytesPerRowSpinner.setMaximum(Integer.MAX_VALUE);
        maxBytesPerRowSpinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.setMaxBytesPerRow(maxBytesPerRowSpinner.getSelection());
            }
        });
        FormData fd_maxBytesPerRowSpinner = new FormData();
        fd_maxBytesPerRowSpinner.top = new FormAttachment(maxBytesPerRowLabel, 6);
        fd_maxBytesPerRowSpinner.left = new FormAttachment(0, 10);
        fd_maxBytesPerRowSpinner.right = new FormAttachment(100, -10);
        maxBytesPerRowSpinner.setLayoutData(fd_maxBytesPerRowSpinner);

        Label minRowPositionLengthLabel = new Label(this, SWT.NONE);
        FormData fd_minRowPositionLengthLabel = new FormData();
        fd_minRowPositionLengthLabel.top = new FormAttachment(maxBytesPerRowSpinner, 6);
        fd_minRowPositionLengthLabel.left = new FormAttachment(0, 10);
        fd_minRowPositionLengthLabel.right = new FormAttachment(100, -10);
        minRowPositionLengthLabel.setLayoutData(fd_minRowPositionLengthLabel);
        minRowPositionLengthLabel.setText("Minimal Row Position Length");

        minRowPositionLengthSpinner = new Spinner(this, SWT.BORDER);
        minRowPositionLengthSpinner.setMaximum(Integer.MAX_VALUE);
        minRowPositionLengthSpinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.setMinRowPositionLength((Integer) minRowPositionLengthSpinner.getSelection());
            }
        });
        FormData fd_minRowPositionLengthSpinner = new FormData();
        fd_minRowPositionLengthSpinner.top = new FormAttachment(minRowPositionLengthLabel, 6);
        fd_minRowPositionLengthSpinner.left = new FormAttachment(0, 10);
        fd_minRowPositionLengthSpinner.right = new FormAttachment(100, -10);
        minRowPositionLengthSpinner.setLayoutData(fd_minRowPositionLengthSpinner);

        Label maxRowPositionLengthLabel = new Label(this, SWT.NONE);
        FormData fd_maxRowPositionLengthLabel = new FormData();
        fd_maxRowPositionLengthLabel.top = new FormAttachment(minRowPositionLengthSpinner, 6);
        fd_maxRowPositionLengthLabel.left = new FormAttachment(0, 10);
        fd_maxRowPositionLengthLabel.right = new FormAttachment(100, -10);
        maxRowPositionLengthLabel.setLayoutData(fd_maxRowPositionLengthLabel);
        maxRowPositionLengthLabel.setText("Maximal Row Position Length");

        maxRowPositionLengthSpinner = new Spinner(this, SWT.BORDER);
        maxRowPositionLengthSpinner.setMaximum(Integer.MAX_VALUE);
        maxRowPositionLengthSpinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.setMaxRowPositionLength((Integer) maxRowPositionLengthSpinner.getSelection());
            }
        });
        FormData fd_maxRowPositionLengthSpinner = new FormData();
        fd_maxRowPositionLengthSpinner.top = new FormAttachment(maxRowPositionLengthLabel, 6);
        fd_maxRowPositionLengthSpinner.left = new FormAttachment(0, 10);
        fd_maxRowPositionLengthSpinner.right = new FormAttachment(100, -10);
        maxRowPositionLengthSpinner.setLayoutData(fd_maxRowPositionLengthSpinner);
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        rowWrappingModeCheckButton.setSelection(codeArea.getRowWrapping() == RowWrappingMode.WRAPPING);
        maxBytesPerRowSpinner.setSelection(codeArea.getMaxBytesPerRow());
        minRowPositionLengthSpinner.setSelection(codeArea.getMinRowPositionLength());
        maxRowPositionLengthSpinner.setSelection(codeArea.getMaxRowPositionLength());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
