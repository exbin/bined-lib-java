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
package org.exbin.deltahex.example;

import javax.swing.JFrame;
import org.exbin.deltahex.component.Hexadecimal;
import org.exbin.framework.deltahex.XBHexadecimalData;
import org.exbin.xbup.core.type.XBData;

/**
 * Hexadecimal editor examples.
 *
 * @version 0.1.0 2016/04/09
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
        final Hexadecimal hexadecimal = new Hexadecimal();
//        hexadecimal.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        XBData data = new XBData();
        data.loadFromStream(hexadecimal.getClass().getResourceAsStream("/org/exbin/deltahex/example/resources/lorem_1.txt"));
        hexadecimal.setData(new XBHexadecimalData(data));
        panel.setHexadecimal(hexadecimal);

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
                hexadecimal.requestFocus();
            }
        });
    }
}
