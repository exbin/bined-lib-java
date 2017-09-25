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
package org.exbin.deltahex.swing.example;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;

/**
 * Example Swing GUI demonstration application of the deltahex component.
 *
 * @version 0.2.0 2017/09/23
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaHexExample {

    /**
     * Main method launching the application.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        final JFrame frame = new JFrame("DeltaHex Library Example");
        frame.setLocationByPlatform(true);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        final JTabbedPane tabbedPane = new JTabbedPane();

        final DeltaHexExampleBasicPanel basicPanel = new DeltaHexExampleBasicPanel();
        final CodeArea basicCodeArea = new CodeArea();
        ByteArrayEditableData basicData = new ByteArrayEditableData();
        try {
            basicData.loadFromStream(basicCodeArea.getClass().getResourceAsStream("/org/exbin/deltahex/swing/example/resources/lorem_1.txt"));
        } catch (IOException ex) {
            Logger.getLogger(DeltaHexExample.class.getName()).log(Level.SEVERE, null, ex);
        }
        basicCodeArea.setData(basicData);
        basicPanel.setCodeArea(basicCodeArea);

        final DeltaHexExampleExPanel extendedPanel = new DeltaHexExampleExPanel();
        final CodeArea extendedCodeArea = new CodeArea();
        ByteArrayEditableData extendedData = new ByteArrayEditableData();
        try {
            extendedData.loadFromStream(extendedCodeArea.getClass().getResourceAsStream("/org/exbin/deltahex/swing/example/resources/lorem_1.txt"));
        } catch (IOException ex) {
            Logger.getLogger(DeltaHexExample.class.getName()).log(Level.SEVERE, null, ex);
        }
        extendedCodeArea.setData(extendedData);
        extendedPanel.setCodeArea(extendedCodeArea);

        tabbedPane.addTab("Basic", basicPanel);
        tabbedPane.addTab("Extended", extendedPanel);

        // TODO Keep only current tab populated
//        tabbedPane.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                switch (tabbedPane.getSelectedIndex()) {
//                    case 0: {
//                        tabbedPane.setSelectedComponent(basicPanel);
//                        break;
//                    }
//                    case 1: {
//                        tabbedPane.setSelectedComponent(extendedPanel);
//                        break;
//                    }
//                }
//            }
//        });
        frame.add(tabbedPane);
        basicCodeArea.requestFocus();

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                frame.setVisible(true);
                tabbedPane.requestFocus();
            }
        });
    }
}
