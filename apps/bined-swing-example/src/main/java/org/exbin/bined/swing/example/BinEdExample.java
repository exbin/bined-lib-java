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
package org.exbin.bined.swing.example;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;

/**
 * BinEd component usage example.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdExample {

    public static void main(String[] args) {
        // You can use this component for your own project using one of the
        // following options:
        //
        // - Download library and include it with your project
        // - Download sources and modify it for your needs
        // - Import library using Maven
        //
        // Libraries (groupId:artifactId:version):
        //   org.exbin.bined:bined-core:0.2.2
        //   org.exbin.bined:bined-swing:0.2.2
        //   org.exbin.auxiliary:binary_data:0.2.2
        //   org.exbin.auxiliary:binary_data-array:0.2.2

        // Create component instance
        CodeArea codeArea = new CodeArea();

        // Fill it with some data
        codeArea.setContentData(new ByteArrayEditableData(new byte[]{1, 2, 3, 0x45, 0x58, 0x41, 0x4D, 0x50, 0x4C, 0x45}));

        // Add it to frame to display it
        JFrame frame = new JFrame("BinEd Example");
        frame.add(codeArea);
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
