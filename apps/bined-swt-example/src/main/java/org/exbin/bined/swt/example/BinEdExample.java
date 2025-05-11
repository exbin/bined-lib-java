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
package org.exbin.bined.swt.example;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.exbin.bined.swt.basic.CodeArea;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;

/**
 * BinEd component usage example.
 *
 * @author ExBin Project (https://exbin.org)
 */
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
        //   org.exbin.bined:bined-swt:0.2.2
        //   org.exbin.auxiliary:binary_data:0.2.2
        //   org.exbin.auxiliary:binary_data-array:0.2.2
        //   + SWT libraries

        // Prepare window shell
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("BinEd Library SWT Example");
        shell.setSize(1000, 600);
        shell.setLayout(new FillLayout());

        // Create component instance
        CodeArea codeArea = new CodeArea(shell, SWT.BORDER);

        // Fill it with some data
        codeArea.setContentData(new ByteArrayEditableData(new byte[]{1, 2, 3, 0x45, 0x58, 0x41, 0x4D, 0x50, 0x4C, 0x45}));

        // Display window
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
