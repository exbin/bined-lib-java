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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.HorizontalScrollUnit;
import org.exbin.bined.basic.VerticalScrollUnit;
import org.exbin.bined.capability.BasicScrollingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.swt.basic.CodeArea;

/**
 * Binary editor scrolling options panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ScrollingPanel extends Composite {

    private CodeArea codeArea;

    private final Combo verticalScrollBarVisibilityCombo;
    private final Combo verticalScrollModeCombo;
    private final Text verticalPositionText;
    private final Combo horizontalScrollBarVisibilityCombo;
    private final Combo horizontalScrollModeCombo;
    private final Text horizontalPositionText;
    private final Text horizontalByteShiftText;

    public ScrollingPanel(Composite parent, int style) {
        super(parent, style);

        setLayout(new FormLayout());

        Group verticalGroup = new Group(this, SWT.NONE);
        verticalGroup.setText("Vertical");
        verticalGroup.setLayout(new FillLayout());
        FormData fd_verticalGroup = new FormData();
        fd_verticalGroup.top = new FormAttachment(0, 10);
        fd_verticalGroup.right = new FormAttachment(100, -10);
        fd_verticalGroup.left = new FormAttachment(0, 10);
        fd_verticalGroup.height = 200;
        verticalGroup.setLayoutData(fd_verticalGroup);

        Composite verticalGroupComposite = new Composite(verticalGroup, SWT.NONE);
        verticalGroupComposite.setLayout(new FormLayout());

        Label verticalScrollBarVisibilityLabel = new Label(verticalGroupComposite, SWT.NONE);
        FormData fd_verticalScrollBarVisibilityLabel = new FormData();
        fd_verticalScrollBarVisibilityLabel.top = new FormAttachment(0, 10);
        fd_verticalScrollBarVisibilityLabel.left = new FormAttachment(0, 10);
        fd_verticalScrollBarVisibilityLabel.right = new FormAttachment(100, -10);
        verticalScrollBarVisibilityLabel.setLayoutData(fd_verticalScrollBarVisibilityLabel);
        verticalScrollBarVisibilityLabel.setText("Vertical Scrollbar");

        verticalScrollBarVisibilityCombo = new Combo(verticalGroupComposite, SWT.READ_ONLY);
        verticalScrollBarVisibilityCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((BasicScrollingCapable) codeArea).setVerticalScrollBarVisibility(ScrollBarVisibility.values()[verticalScrollBarVisibilityCombo.getSelectionIndex()]);
            }
        });
        verticalScrollBarVisibilityCombo.setItems(new String[]{"NEVER", "IF_NEEDED", "ALWAYS"});
        verticalScrollBarVisibilityCombo.select(0);
        FormData fd_verticalScrollBarVisibilityCombo = new FormData();
        fd_verticalScrollBarVisibilityCombo.top = new FormAttachment(verticalScrollBarVisibilityLabel, 6);
        fd_verticalScrollBarVisibilityCombo.left = new FormAttachment(0, 10);
        fd_verticalScrollBarVisibilityCombo.right = new FormAttachment(100, -10);
        verticalScrollBarVisibilityCombo.setLayoutData(fd_verticalScrollBarVisibilityCombo);

        Label verticalScrollModeLabel = new Label(verticalGroupComposite, SWT.NONE);
        FormData fd_verticalScrollModeLabel = new FormData();
        fd_verticalScrollModeLabel.right = new FormAttachment(0, 370);
        fd_verticalScrollModeLabel.top = new FormAttachment(verticalScrollBarVisibilityCombo, 6);
        fd_verticalScrollModeLabel.left = new FormAttachment(0, 10);
        verticalScrollModeLabel.setLayoutData(fd_verticalScrollModeLabel);
        verticalScrollModeLabel.setText("Vertical Scroll Mode");

        verticalScrollModeCombo = new Combo(verticalGroupComposite, SWT.READ_ONLY);
        verticalScrollModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((BasicScrollingCapable) codeArea).setVerticalScrollUnit(VerticalScrollUnit.values()[verticalScrollModeCombo.getSelectionIndex()]);
            }
        });
        verticalScrollModeCombo.setItems(new String[]{"PER_ROW", "PIXEL"});
        verticalScrollModeCombo.select(0);
        FormData fd_verticalScrollModeCombo = new FormData();
        fd_verticalScrollModeCombo.top = new FormAttachment(verticalScrollModeLabel, 6);
        fd_verticalScrollModeCombo.left = new FormAttachment(0, 10);
        fd_verticalScrollModeCombo.right = new FormAttachment(100, -10);
        verticalScrollModeCombo.setLayoutData(fd_verticalScrollModeCombo);

        Label verticalPositionLabel = new Label(verticalGroupComposite, SWT.NONE);
        FormData fd_verticalPositionLabel = new FormData();
        fd_verticalPositionLabel.right = new FormAttachment(0, 370);
        fd_verticalPositionLabel.top = new FormAttachment(verticalScrollModeCombo, 6);
        fd_verticalPositionLabel.left = new FormAttachment(0, 10);
        verticalPositionLabel.setLayoutData(fd_verticalPositionLabel);
        verticalPositionLabel.setText("Vertical Scroll Position");

        verticalPositionText = new Text(verticalGroupComposite, SWT.BORDER);
        verticalPositionText.setEditable(false);
        FormData fd_verticalPositionText = new FormData();
        fd_verticalPositionText.top = new FormAttachment(verticalPositionLabel, 6);
        fd_verticalPositionText.left = new FormAttachment(0, 10);
        fd_verticalPositionText.right = new FormAttachment(100, -10);
        verticalPositionText.setLayoutData(fd_verticalPositionText);

        Group horizontalGroup = new Group(this, SWT.NONE);
        horizontalGroup.setText("Horizontal");
        horizontalGroup.setLayout(new FillLayout());
        FormData fd_horizontalGroup = new FormData();
        fd_horizontalGroup.top = new FormAttachment(verticalGroup, 6);
        fd_horizontalGroup.right = new FormAttachment(100, -10);
        fd_horizontalGroup.left = new FormAttachment(0, 10);
        fd_horizontalGroup.height = 200;
        horizontalGroup.setLayoutData(fd_horizontalGroup);

        Composite horizontalGroupComposite = new Composite(horizontalGroup, SWT.NONE);
        horizontalGroupComposite.setLayout(new FormLayout());

        Label horizontalScrollBarVisibilityLabel = new Label(horizontalGroupComposite, SWT.NONE);
        FormData fd_horizontalScrollBarVisibilityLabel = new FormData();
        fd_horizontalScrollBarVisibilityLabel.top = new FormAttachment(0, 10);
        fd_horizontalScrollBarVisibilityLabel.left = new FormAttachment(0, 10);
        fd_horizontalScrollBarVisibilityLabel.right = new FormAttachment(100, -10);
        horizontalScrollBarVisibilityLabel.setLayoutData(fd_horizontalScrollBarVisibilityLabel);
        horizontalScrollBarVisibilityLabel.setText("Horizontal Scrollbar");

        horizontalScrollBarVisibilityCombo = new Combo(horizontalGroupComposite, SWT.READ_ONLY);
        horizontalScrollBarVisibilityCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((BasicScrollingCapable) codeArea).setHorizontalScrollBarVisibility(ScrollBarVisibility.values()[horizontalScrollBarVisibilityCombo.getSelectionIndex()]);
            }
        });
        horizontalScrollBarVisibilityCombo.setItems(new String[]{"NEVER", "IF_NEEDED", "ALWAYS"});
        horizontalScrollBarVisibilityCombo.select(0);
        FormData fd_horizontalScrollBarVisibilityCombo = new FormData();
        fd_horizontalScrollBarVisibilityCombo.top = new FormAttachment(horizontalScrollBarVisibilityLabel, 6);
        fd_horizontalScrollBarVisibilityCombo.left = new FormAttachment(0, 10);
        fd_horizontalScrollBarVisibilityCombo.right = new FormAttachment(100, -10);
        horizontalScrollBarVisibilityCombo.setLayoutData(fd_horizontalScrollBarVisibilityCombo);

        Label horizontalScrollModeLabel = new Label(horizontalGroupComposite, SWT.NONE);
        FormData fd_horizontalScrollModeLabel = new FormData();
        fd_horizontalScrollModeLabel.right = new FormAttachment(0, 370);
        fd_horizontalScrollModeLabel.top = new FormAttachment(horizontalScrollBarVisibilityCombo, 6);
        fd_horizontalScrollModeLabel.left = new FormAttachment(0, 10);
        horizontalScrollModeLabel.setLayoutData(fd_horizontalScrollModeLabel);
        horizontalScrollModeLabel.setText("Horizontal Scroll Mode");

        horizontalScrollModeCombo = new Combo(horizontalGroupComposite, SWT.READ_ONLY);
        horizontalScrollModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ((BasicScrollingCapable) codeArea).setHorizontalScrollUnit(HorizontalScrollUnit.values()[horizontalScrollModeCombo.getSelectionIndex()]);
            }
        });
        horizontalScrollModeCombo.setItems(new String[]{"PER_CHAR", "PIXEL"});
        horizontalScrollModeCombo.select(0);
        FormData fd_horizontalScrollModeCombo = new FormData();
        fd_horizontalScrollModeCombo.top = new FormAttachment(horizontalScrollModeLabel, 6);
        fd_horizontalScrollModeCombo.left = new FormAttachment(0, 10);
        fd_horizontalScrollModeCombo.right = new FormAttachment(100, -10);
        horizontalScrollModeCombo.setLayoutData(fd_horizontalScrollModeCombo);

        Label horizontalPositionLabel = new Label(horizontalGroupComposite, SWT.NONE);
        FormData fd_horizontalPositionLabel = new FormData();
        fd_horizontalPositionLabel.right = new FormAttachment(0, 370);
        fd_horizontalPositionLabel.top = new FormAttachment(horizontalScrollModeCombo, 6);
        fd_horizontalPositionLabel.left = new FormAttachment(0, 10);
        horizontalPositionLabel.setLayoutData(fd_horizontalPositionLabel);
        horizontalPositionLabel.setText("Horizontal Scroll Position");

        horizontalPositionText = new Text(horizontalGroupComposite, SWT.BORDER);
        horizontalPositionText.setEditable(false);
        FormData fd_horizontalPositionText = new FormData();
        fd_horizontalPositionText.top = new FormAttachment(horizontalPositionLabel, 6);
        fd_horizontalPositionText.left = new FormAttachment(0, 10);
        fd_horizontalPositionText.right = new FormAttachment(100, -10);
        horizontalPositionText.setLayoutData(fd_horizontalPositionText);

        Label horizontalByteShiftLabel = new Label(this, SWT.NONE);
        FormData fd_horizontalByteShiftLabel = new FormData();
        fd_horizontalByteShiftLabel.top = new FormAttachment(horizontalGroup, 6);
        fd_horizontalByteShiftLabel.left = new FormAttachment(0, 10);
        fd_horizontalByteShiftLabel.right = new FormAttachment(100, -10);
        horizontalByteShiftLabel.setLayoutData(fd_horizontalByteShiftLabel);
        horizontalByteShiftLabel.setText("Maximum Bytes Per Row");

        horizontalByteShiftText = new Text(this, SWT.BORDER);
        horizontalByteShiftText.setEditable(false);
        horizontalByteShiftText.setText("0");
        FormData fd_horizontalByteShiftText = new FormData();
        fd_horizontalByteShiftText.top = new FormAttachment(horizontalByteShiftLabel, 6);
        fd_horizontalByteShiftText.left = new FormAttachment(0, 10);
        fd_horizontalByteShiftText.right = new FormAttachment(100, -10);
        horizontalByteShiftText.setLayoutData(fd_horizontalByteShiftText);
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;

        verticalScrollBarVisibilityCombo.select(((BasicScrollingCapable) codeArea).getVerticalScrollBarVisibility().ordinal());
        verticalScrollModeCombo.select(((BasicScrollingCapable) codeArea).getVerticalScrollUnit().ordinal());
        horizontalScrollBarVisibilityCombo.select(((BasicScrollingCapable) codeArea).getHorizontalScrollBarVisibility().ordinal());
        horizontalScrollModeCombo.select(((BasicScrollingCapable) codeArea).getHorizontalScrollUnit().ordinal());

        ((ScrollingCapable) codeArea).addScrollingListener(() -> {
            updatePosition();
        });
        updatePosition();
    }

    private void updatePosition() {
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) codeArea).getScrollPosition();
        verticalPositionText.setText(scrollPosition.getRowPosition() + ":" + scrollPosition.getRowOffset());
        horizontalPositionText.setText(scrollPosition.getCharPosition() + ":" + scrollPosition.getCharOffset());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
