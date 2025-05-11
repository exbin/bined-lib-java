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
package org.exbin.bined.swt.demo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.exbin.bined.swt.basic.CodeArea;
import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;

/**
 * Binary component demo application for SWT.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdDemo {

    private static final String EXAMPLE_FILE_PATH = "/org/exbin/bined/swt/demo/resources/lorem_1.txt";

    public BinEdDemo() {
    }

    /**
     * Main method launching the application.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("BinEd Library SWT Demo");
        shell.setSize(1000, 600);
        shell.setLayout(new FillLayout());

//        shell.setLocation(point);
        final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);

        TabItem basicTabItem = new TabItem(tabFolder, SWT.NULL);
        basicTabItem.setText("Basic");
        SashForm basicForm = new SashForm(tabFolder, SWT.HORIZONTAL);
        basicForm.setLayout(new FillLayout());

        final BinEdDemoBasicPanel basicPanel = new BinEdDemoBasicPanel(basicForm, SWT.NONE);
        final CodeArea basicCodeArea = new CodeArea(basicForm, SWT.BORDER);
        ByteArrayEditableData basicData = new ByteArrayEditableData();
        try {
            basicData.loadFromStream(basicCodeArea.getClass().getResourceAsStream(EXAMPLE_FILE_PATH));
        } catch (IOException ex) {
            Logger.getLogger(BinEdDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        basicCodeArea.setContentData(basicData);
        basicPanel.setCodeArea(basicCodeArea);

        TabItem extendedTabItem = new TabItem(tabFolder, SWT.NULL);
        extendedTabItem.setText("Section");

//        basicTabItem.setControl(basicCodeArea);
        basicTabItem.setControl(basicForm);
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent se) {
                System.exit(0);
            }
        });
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }
}
