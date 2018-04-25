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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Example Swing GUI demonstration application of the bined component.
 *
 * @version 0.2.0 2018/04/25
 * @author ExBin Project (http://exbin.org)
 */
public class BinEdExample {

    private static final String EXAMPLE_FILE_PATH = "/org/exbin/bined/swt/example/resources/lorem_1.txt";

    /**
     * Main method launching the application.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("BinEd Library Example");
        shell.setSize(1000, 600);
        shell.setLayout(new FillLayout());

//        shell.setLocation(point);
        final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
        TabItem basicTabItem = new TabItem(tabFolder, SWT.NULL);
        basicTabItem.setText("Basic");

        Text text = new Text(tabFolder, SWT.BORDER);
        text.setText("TEST");
        basicTabItem.setControl(text);

        TabItem extendedTabItem = new TabItem(tabFolder, SWT.NULL);
        extendedTabItem.setText("Extended");

//        final BinEdExampleBasicPanel basicPanel = new BinEdExampleBasicPanel();
//        final CodeArea basicCodeArea = new CodeArea();
//        ByteArrayEditableData basicData = new ByteArrayEditableData();
//        try {
//            basicData.loadFromStream(basicCodeArea.getClass().getResourceAsStream(EXAMPLE_FILE_PATH));
//        } catch (IOException ex) {
//            Logger.getLogger(BinEdExample.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        basicCodeArea.setData(basicData);
//        basicPanel.setCodeArea(basicCodeArea);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();

//
//        final JFrame frame = new JFrame("BinEd Library Example");
//        frame.setLocationByPlatform(true);
//        frame.setSize(1000, 600);
//        frame.setLocationRelativeTo(null);
//        final JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.setFocusable(false);
//
//        final BinEdExampleBasicPanel basicPanel = new BinEdExampleBasicPanel();
//        final CodeArea basicCodeArea = new CodeArea();
//        ByteArrayEditableData basicData = new ByteArrayEditableData();
//        try {
//            basicData.loadFromStream(basicCodeArea.getClass().getResourceAsStream(EXAMPLE_FILE_PATH));
//        } catch (IOException ex) {
//            Logger.getLogger(BinEdExample.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        basicCodeArea.setData(basicData);
//        basicPanel.setCodeArea(basicCodeArea);
//
//        final BinEdExampleExPanel extendedPanel = new BinEdExampleExPanel();
//        final CodeArea extendedCodeArea = new CodeArea(); // TODO
//        ExtCodeAreaWorker extendedWorker = new ExtCodeAreaWorker(extendedCodeArea);
//        extendedWorker.setPainter(new DefaultCodeAreaPainter(extendedWorker));
//        extendedCodeArea.setWorker(extendedWorker);
//        ByteArrayEditableData extendedData = new ByteArrayEditableData();
//        try {
//            extendedData.loadFromStream(extendedCodeArea.getClass().getResourceAsStream(EXAMPLE_FILE_PATH));
//        } catch (IOException ex) {
//            Logger.getLogger(BinEdExample.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        extendedCodeArea.setData(extendedData);
//        extendedPanel.setCodeArea(extendedCodeArea);
//
//        tabbedPane.addTab("Basic", basicPanel);
//        tabbedPane.addTab("Extended", extendedPanel);
//
//        // TODO Keep only current tab populated
//        tabbedPane.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                switch (tabbedPane.getSelectedIndex()) {
//                    case 0: {
//                        tabbedPane.setSelectedComponent(basicPanel);
//                        basicCodeArea.requestFocus();
//                        break;
//                    }
//                    case 1: {
//                        tabbedPane.setSelectedComponent(extendedPanel);
//                        extendedCodeArea.requestFocus();
//                        break;
//                    }
//                }
//            }
//        });
//        frame.add(tabbedPane);
//
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
//
//                frame.addWindowListener(new java.awt.event.WindowAdapter() {
//                    @Override
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                frame.setVisible(true);
//                basicCodeArea.requestFocus();
//            }
//        });
    }
}
