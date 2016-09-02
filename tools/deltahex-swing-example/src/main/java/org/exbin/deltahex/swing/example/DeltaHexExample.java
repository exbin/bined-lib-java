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
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;

/**
 * Hexadecimal editor examples.
 *
 * @version 0.1.0 2016/06/22
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
        final DeltaHexExamplePanel panel = new DeltaHexExamplePanel();
        frame.add(panel);
        final CodeArea codeArea = new CodeArea();
        ByteArrayEditableData data = new ByteArrayEditableData();
        try {
            data.loadFromStream(codeArea.getClass().getResourceAsStream("/org/exbin/deltahex/swing/example/resources/lorem_1.txt"));
        } catch (IOException ex) {
            Logger.getLogger(DeltaHexExample.class.getName()).log(Level.SEVERE, null, ex);
        }
        codeArea.setData(data);
        panel.setCodeArea(codeArea);

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
                codeArea.requestFocus();
            }
        });
    }
}
