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
package org.exbin.bined.swing.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.diff.ExtCodeAreaDiffPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;

/**
 * Example Swing GUI demonstration application of the bined component.
 *
 * @version 0.2.0 2019/02/23
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdExample {

    private static final String EXAMPLE_FILE_PATH = "/org/exbin/bined/swing/example/resources/lorem_1.txt";
    private static final String EXAMPLE_DIFF_FILE_PATH = "/org/exbin/bined/swing/example/resources/lorem_2.txt";

    public BinEdExample() {
    }

    /**
     * Main method launching the application.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        final JFrame frame = new JFrame("BinEd Library Swing Example");
        frame.setLocationByPlatform(true);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFocusable(false);

        final BinEdExampleBasicPanel basicPanel = new BinEdExampleBasicPanel();
        final CodeArea basicCodeArea = new CodeArea();
        basicCodeArea.setContentData(getSampleData());
        basicPanel.setCodeArea(basicCodeArea);

        final BinEdExampleExPanel extendedPanel = new BinEdExampleExPanel();
        final ExtCodeArea extendedCodeArea = new ExtCodeArea();
        extendedCodeArea.setContentData(getSampleData());
        extendedPanel.setCodeArea(extendedCodeArea);

        final BinEdExampleDiffPanel diffPanel = new BinEdExampleDiffPanel();
        final ExtCodeAreaDiffPanel diffCodeAreaPanel = new ExtCodeAreaDiffPanel();
        diffCodeAreaPanel.setLeftContentData(getSampleData());
        diffCodeAreaPanel.setRightContentData(getSampleDiffData());
        diffPanel.setDiffPanel(diffCodeAreaPanel);

        tabbedPane.addTab("Basic", basicPanel);
        tabbedPane.addTab("Extended", extendedPanel);
        tabbedPane.addTab("Diff", diffPanel);

        tabbedPane.addChangeListener((ChangeEvent e) -> {
            switch (tabbedPane.getSelectedIndex()) {
                case 0: {
                    tabbedPane.setSelectedComponent(basicPanel);
                    basicCodeArea.requestFocus();
                    break;
                }
                case 1: {
                    tabbedPane.setSelectedComponent(extendedPanel);
                    extendedCodeArea.requestFocus();
                    break;
                }
            }
        });
        frame.add(tabbedPane);

        java.awt.EventQueue.invokeLater(() -> {
            frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.setVisible(true);
            basicCodeArea.requestFocus();
        });
    }

    @Nonnull
    private static ByteArrayEditableData getSampleData() {
        ByteArrayEditableData data = new ByteArrayEditableData();
        try {
            data.loadFromStream(data.getClass().getResourceAsStream(EXAMPLE_FILE_PATH));
        } catch (IOException ex) {
            Logger.getLogger(BinEdExample.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    @Nonnull
    private static ByteArrayEditableData getSampleDiffData() {
        ByteArrayEditableData data = new ByteArrayEditableData();
        try {
            data.loadFromStream(data.getClass().getResourceAsStream(EXAMPLE_DIFF_FILE_PATH));
        } catch (IOException ex) {
            Logger.getLogger(BinEdExample.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    @Nonnull
    private static BinaryData getBigSampleData() {
        return getBigSampleData(0, Integer.MAX_VALUE / 2);
    }

    @Nonnull
    private static BinaryData getBigSampleData(int offset, long size) {
        BinaryData data = new BinaryData() {
            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getDataSize() {
                return size;
            }

            @Override
            public byte getByte(long l) {
                return (byte) ((l + offset) % 128);
            }

            @Override
            public BinaryData copy() {
                return getBigSampleData(offset, size);
            }

            @Override
            public BinaryData copy(long l, long l1) {
                return getBigSampleData((int) ((offset + l) % 128), l1);
            }

            @Override
            public void copyToArray(long l, byte[] bytes, int i, int i1) {
                for (int j = 0; j < i1; j++) {
                    bytes[i + j] = getByte(l + j);
                }
            }

            @Override
            public void saveToStream(OutputStream out) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public InputStream getDataInputStream() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void dispose() {
            }
        };
        return data;
    }
}
