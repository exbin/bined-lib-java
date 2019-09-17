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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
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
import org.eclipse.swt.layout.FillLayout;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.swt.basic.CodeArea;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Binary editor state options panel.
 *
 * @version 0.2.0 2019/09/12
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatePanel extends Composite {

    private CodeArea codeArea;

    private final Combo activeOperationCombo;
    private final Combo activeSectionCombo;
    private final Text dataSizeText;
    private final Text positionText;
    private final Text codeOffsetText;
    private final Text selectionStartText;
    private final Text selectionEndText;

    public StatePanel(Composite parent, int style) {
        super(parent, style);

        setLayout(new FormLayout());

        Label dataSizeLabel = new Label(this, SWT.NONE);
        FormData fd_dataSizeLabel = new FormData();
        fd_dataSizeLabel.top = new FormAttachment(0, 10);
        fd_dataSizeLabel.left = new FormAttachment(0, 10);
        fd_dataSizeLabel.right = new FormAttachment(100, -10);
        dataSizeLabel.setLayoutData(fd_dataSizeLabel);
        dataSizeLabel.setText("Data Size");

        dataSizeText = new Text(this, SWT.BORDER);
        dataSizeText.setEditable(false);
        FormData fd_dataSizeText = new FormData();
        fd_dataSizeText.top = new FormAttachment(0, 33);
        fd_dataSizeText.left = new FormAttachment(0, 10);
        fd_dataSizeText.right = new FormAttachment(100, -10);
        dataSizeText.setLayoutData(fd_dataSizeText);

        Button loadDataButton = new Button(this, SWT.NONE);
        FormData fd_loadDataButton = new FormData();
        fd_loadDataButton.top = new FormAttachment(dataSizeText, 6);
        fd_loadDataButton.left = new FormAttachment(0, 10);
        loadDataButton.setLayoutData(fd_loadDataButton);
        loadDataButton.setText("Load...");
        loadDataButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se) {
                Shell shell = getShell();
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterNames(new String[]{"All Files (*)"});
                dialog.setFilterExtensions(new String[]{"*"});
                String fileName = dialog.open();
                if (fileName != null) {
                    try {
                        File selectedFile = new File(fileName);
                        try (FileInputStream stream = new FileInputStream(selectedFile)) {
                            CodeAreaUtils.requireNonNull(((EditableBinaryData) codeArea.getContentData())).loadFromStream(stream);
                            codeArea.notifyDataChanged();
                            //                    codeArea.resetPosition();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(StatePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        Button saveDataButton = new Button(this, SWT.NONE);
        FormData fd_saveDataButton = new FormData();
        fd_saveDataButton.top = new FormAttachment(dataSizeText, 6);
        fd_saveDataButton.left = new FormAttachment(loadDataButton, 10);
        saveDataButton.setLayoutData(fd_saveDataButton);
        saveDataButton.setText("Save...");
        saveDataButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se) {
                Shell shell = getShell();
                FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                dialog.setFilterNames(new String[]{"All Files (*)"});
                dialog.setFilterExtensions(new String[]{"*"});
                String fileName = dialog.open();
                if (fileName != null) {
                    try {
                        File selectedFile = new File(fileName);
                        try (FileOutputStream stream = new FileOutputStream(selectedFile)) {
                            CodeAreaUtils.requireNonNull(codeArea.getContentData()).saveToStream(stream);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(StatePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        Label activeOperationLabel = new Label(this, SWT.NONE);
        FormData fd_activeOperationLabel = new FormData();
        fd_activeOperationLabel.top = new FormAttachment(loadDataButton, 6);
        fd_activeOperationLabel.left = new FormAttachment(0, 10);
        fd_activeOperationLabel.right = new FormAttachment(100, -10);
        activeOperationLabel.setLayoutData(fd_activeOperationLabel);
        activeOperationLabel.setText("Active Editation Operation");

        activeOperationCombo = new Combo(this, SWT.READ_ONLY);
        activeOperationCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.setEditationOperation(EditationOperation.values()[activeOperationCombo.getSelectionIndex()]);
            }
        });
        activeOperationCombo.setItems(new String[]{"INSERT", "OVERWRITE"});
        FormData fd_activeOperationCombo = new FormData();
        fd_activeOperationCombo.top = new FormAttachment(activeOperationLabel, 6);
        fd_activeOperationCombo.left = new FormAttachment(0, 10);
        fd_activeOperationCombo.right = new FormAttachment(100, -10);
        activeOperationCombo.setLayoutData(fd_activeOperationCombo);

        Group positionGroup = new Group(this, SWT.NONE);
        positionGroup.setText("Position");
        positionGroup.setLayout(new FillLayout(SWT.VERTICAL | SWT.HORIZONTAL));
        FormData fd_positionGroup = new FormData();
        fd_positionGroup.top = new FormAttachment(activeOperationCombo, 6);
        fd_positionGroup.left = new FormAttachment(0, 10);
        fd_positionGroup.right = new FormAttachment(100, -10);
        fd_positionGroup.height = 200;
        positionGroup.setLayoutData(fd_positionGroup);

        Composite fontGroupComposite = new Composite(positionGroup, SWT.NONE);
        fontGroupComposite.setLayout(new FormLayout());

        Label positionLabel = new Label(fontGroupComposite, SWT.NONE);
        positionLabel.setText("Data Position");
        FormData fd_positionLabel = new FormData();
        fd_positionLabel.top = new FormAttachment(0, 10);
        fd_positionLabel.left = new FormAttachment(0, 10);
        fd_positionLabel.right = new FormAttachment(100, -10);
        positionLabel.setLayoutData(fd_positionLabel);

        positionText = new Text(fontGroupComposite, SWT.BORDER);
        positionText.setEditable(false);
        FormData fd_positionText = new FormData();
        fd_positionText.top = new FormAttachment(positionLabel, 6);
        fd_positionText.left = new FormAttachment(0, 10);
        fd_positionText.right = new FormAttachment(100, -10);
        positionText.setLayoutData(fd_positionText);

        Label codeOffsetLabel = new Label(fontGroupComposite, SWT.NONE);
        codeOffsetLabel.setText("Code Offset");
        FormData fd_codeOffsetLabel = new FormData();
        fd_codeOffsetLabel.top = new FormAttachment(positionText, 6);
        fd_codeOffsetLabel.left = new FormAttachment(0, 10);
        fd_codeOffsetLabel.right = new FormAttachment(100, -10);
        codeOffsetLabel.setLayoutData(fd_codeOffsetLabel);

        codeOffsetText = new Text(fontGroupComposite, SWT.BORDER);
        codeOffsetText.setEditable(false);
        FormData fd_codeOffsetText = new FormData();
        fd_codeOffsetText.top = new FormAttachment(codeOffsetLabel, 6);
        fd_codeOffsetText.left = new FormAttachment(0, 10);
        fd_codeOffsetText.right = new FormAttachment(100, -10);
        codeOffsetText.setLayoutData(fd_codeOffsetText);

        Label activeSectionLabel = new Label(fontGroupComposite, SWT.NONE);
        FormData fd_activeSectionLabel = new FormData();
        fd_activeSectionLabel.top = new FormAttachment(codeOffsetText, 6);
        fd_activeSectionLabel.left = new FormAttachment(0, 10);
        fd_activeSectionLabel.right = new FormAttachment(100, -10);
        activeSectionLabel.setLayoutData(fd_activeSectionLabel);
        activeSectionLabel.setText("Active Section");

        activeSectionCombo = new Combo(fontGroupComposite, SWT.READ_ONLY);
        activeSectionCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                codeArea.getCaret().setSection(BasicCodeAreaSection.values()[activeSectionCombo.getSelectionIndex()]);
            }
        });
        activeSectionCombo.setItems(new String[]{"CODE_MATRIX", "TEXT_PREVIEW"});
        activeSectionCombo.select(0);
        FormData fd_activeSectionCombo = new FormData();
        fd_activeSectionCombo.top = new FormAttachment(activeSectionLabel, 6);
        fd_activeSectionCombo.left = new FormAttachment(0, 10);
        fd_activeSectionCombo.right = new FormAttachment(100, -10);
        activeSectionCombo.setLayoutData(fd_activeSectionCombo);

        Group selectionGroup = new Group(this, SWT.NONE);
        selectionGroup.setText("Selection");
        selectionGroup.setLayout(new FillLayout());
        FormData fd_selectionGroup = new FormData();
        fd_selectionGroup.top = new FormAttachment(positionGroup, 6);
        fd_selectionGroup.left = new FormAttachment(0, 10);
        fd_selectionGroup.right = new FormAttachment(100, -10);
        fd_selectionGroup.height = 140;
        selectionGroup.setLayoutData(fd_selectionGroup);

        Composite composite = new Composite(selectionGroup, SWT.NONE);
        composite.setLayout(new FormLayout());

        Label selectionStartLabel = new Label(composite, SWT.NONE);
        selectionStartLabel.setText("Selection Start");
        FormData fd_selectionStartLabel = new FormData();
        fd_selectionStartLabel.top = new FormAttachment(0, 10);
        fd_selectionStartLabel.left = new FormAttachment(0, 10);
        fd_selectionStartLabel.right = new FormAttachment(100, -10);
        selectionStartLabel.setLayoutData(fd_selectionStartLabel);

        selectionStartText = new Text(composite, SWT.BORDER);
        selectionStartText.setEditable(false);
        FormData fd_selectionStartText = new FormData();
        fd_selectionStartText.top = new FormAttachment(selectionStartLabel, 6);
        fd_selectionStartText.left = new FormAttachment(0, 10);
        fd_selectionStartText.right = new FormAttachment(100, -10);
        selectionStartText.setLayoutData(fd_selectionStartText);

        Label selectionEndLabel = new Label(composite, SWT.NONE);
        selectionEndLabel.setText("Selection End");
        FormData fd_selectionEndLabel = new FormData();
        fd_selectionEndLabel.top = new FormAttachment(selectionStartText, 6);
        fd_selectionEndLabel.left = new FormAttachment(0, 10);
        fd_selectionEndLabel.right = new FormAttachment(100, -10);
        selectionEndLabel.setLayoutData(fd_selectionEndLabel);

        selectionEndText = new Text(composite, SWT.BORDER);
        selectionEndText.setEditable(false);
        FormData fd_selectionEndText = new FormData();
        fd_selectionEndText.top = new FormAttachment(selectionEndLabel, 6);
        fd_selectionEndText.left = new FormAttachment(0, 10);
        fd_selectionEndText.right = new FormAttachment(100, -10);
        selectionEndText.setLayoutData(fd_selectionEndText);
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        dataSizeText.setText(String.valueOf(codeArea.getDataSize()));

        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            positionText.setText(String.valueOf(caretPosition.getDataPosition()));
            codeOffsetText.setText(String.valueOf(caretPosition.getCodeOffset()));
            activeSectionCombo.select(getSection(caretPosition).ordinal());
        });
        ((SelectionCapable) codeArea).addSelectionChangedListener((SelectionRange selection) -> {
            if (selection != null) {
                long first = ((SelectionCapable) codeArea).getSelection().getFirst();
                selectionStartText.setText(String.valueOf(first));
                long last = ((SelectionCapable) codeArea).getSelection().getLast();
                selectionEndText.setText(String.valueOf(last));
            } else {
                selectionStartText.setText("");
                selectionEndText.setText("");
            }
        });
        codeArea.addDataChangedListener(() -> {
            dataSizeText.setText(String.valueOf(codeArea.getDataSize()));
        });
        codeArea.addEditationModeChangedListener((editationMode, editationOperation) -> {
            activeOperationCombo.select(editationOperation.ordinal());
        });
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Nonnull
    private BasicCodeAreaSection getSection(CodeAreaCaretPosition caretPosition) {
        BasicCodeAreaSection section = (BasicCodeAreaSection) caretPosition.getSection();
        return section == null ? BasicCodeAreaSection.CODE_MATRIX : section;
    }
}
