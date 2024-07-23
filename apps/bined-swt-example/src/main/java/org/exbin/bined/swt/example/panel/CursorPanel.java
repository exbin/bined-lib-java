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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Spinner;
import org.exbin.bined.swt.basic.CodeArea;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.swt.basic.DefaultCodeAreaCaret;

/**
 * Binary editor cursor options panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CursorPanel extends Composite {

    private CodeArea codeArea;

    private final Spinner cursorBlinkingRateSpinner;
    private final Combo cursorRenderingModeCombo;
    private final Button showMirrorCursorCheckButton;

    public CursorPanel(Composite parent, int style) {
        super(parent, style);

        setLayout(new FormLayout());

        Label cursorBlinkingRateLabel = new Label(this, SWT.NONE);
        FormData fd_cursorBlinkingRateLabel = new FormData();
        fd_cursorBlinkingRateLabel.top = new FormAttachment(0, 10);
        fd_cursorBlinkingRateLabel.left = new FormAttachment(0, 10);
        fd_cursorBlinkingRateLabel.right = new FormAttachment(100, -10);
        cursorBlinkingRateLabel.setLayoutData(fd_cursorBlinkingRateLabel);
        cursorBlinkingRateLabel.setText("Cursor Blinking Rate");

        cursorBlinkingRateSpinner = new Spinner(this, SWT.BORDER);
        cursorBlinkingRateSpinner.setMaximum(Integer.MAX_VALUE);
        cursorBlinkingRateSpinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret()).setBlinkRate((Integer) cursorBlinkingRateSpinner.getSelection());
            }
        });
        FormData fd_cursorBlinkingRateSpinner = new FormData();
        fd_cursorBlinkingRateSpinner.top = new FormAttachment(cursorBlinkingRateLabel, 6);
        fd_cursorBlinkingRateSpinner.left = new FormAttachment(0, 10);
        fd_cursorBlinkingRateSpinner.right = new FormAttachment(100, -10);
        cursorBlinkingRateSpinner.setLayoutData(fd_cursorBlinkingRateSpinner);

        Label cursorRenderingModeLabel = new Label(this, SWT.NONE);
        FormData fd_cursorRenderingModeLabel = new FormData();
        fd_cursorRenderingModeLabel.top = new FormAttachment(cursorBlinkingRateSpinner, 6);
        fd_cursorRenderingModeLabel.left = new FormAttachment(0, 10);
        fd_cursorRenderingModeLabel.right = new FormAttachment(100, -10);
        cursorRenderingModeLabel.setLayoutData(fd_cursorRenderingModeLabel);
        cursorRenderingModeLabel.setText("Cursor Rendering Mode");

        cursorRenderingModeCombo = new Combo(this, SWT.READ_ONLY);
        cursorRenderingModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret()).setRenderingMode(DefaultCodeAreaCaret.CursorRenderingMode.values()[cursorRenderingModeCombo.getSelectionIndex()]);
            }
        });
        cursorRenderingModeCombo.setItems(new String[]{"PAINT", "XOR", "NEGATIVE"});
        FormData fd_cursorRenderingModeCombo = new FormData();
        fd_cursorRenderingModeCombo.top = new FormAttachment(cursorRenderingModeLabel, 6);
        fd_cursorRenderingModeCombo.left = new FormAttachment(0, 10);
        fd_cursorRenderingModeCombo.right = new FormAttachment(100, -10);
        cursorRenderingModeCombo.setLayoutData(fd_cursorRenderingModeCombo);

        showMirrorCursorCheckButton = new Button(this, SWT.CHECK);
        showMirrorCursorCheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((CaretCapable) codeArea).setShowMirrorCursor(showMirrorCursorCheckButton.getSelection());
            }
        });
        FormData fd_showMirrorCursorCheckButton = new FormData();
        fd_showMirrorCursorCheckButton.top = new FormAttachment(cursorRenderingModeCombo, 6);
        fd_showMirrorCursorCheckButton.right = new FormAttachment(100, -10);
        fd_showMirrorCursorCheckButton.left = new FormAttachment(0, 10);
        showMirrorCursorCheckButton.setLayoutData(fd_showMirrorCursorCheckButton);
        showMirrorCursorCheckButton.setText("Show Mirror Cursor");

        Button centerCursorButton = new Button(this, SWT.NONE);
        FormData fd_centerCursorButton = new FormData();
        fd_centerCursorButton.top = new FormAttachment(showMirrorCursorCheckButton, 6);
        fd_centerCursorButton.left = new FormAttachment(0, 10);
        centerCursorButton.setLayoutData(fd_centerCursorButton);
        centerCursorButton.setText("Center Cursor");
        centerCursorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se) {
                codeArea.centerOnCursor();
                codeArea.forceFocus();
            }
        });

        Button revealCursorButton = new Button(this, SWT.NONE);
        FormData fd_revealCursorButton = new FormData();
        fd_revealCursorButton.top = new FormAttachment(centerCursorButton, 6);
        fd_revealCursorButton.left = new FormAttachment(0, 10);
        revealCursorButton.setLayoutData(fd_revealCursorButton);
        revealCursorButton.setText("Reveal Cursor");
        revealCursorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se) {
                codeArea.revealCursor();
                codeArea.forceFocus();
            }
        });
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        cursorBlinkingRateSpinner.setSelection(((DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret()).getBlinkRate());
        cursorRenderingModeCombo.select(codeArea.getCodeAreaCaret().getRenderingMode().ordinal());
        showMirrorCursorCheckButton.setSelection(codeArea.isShowMirrorCursor());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
