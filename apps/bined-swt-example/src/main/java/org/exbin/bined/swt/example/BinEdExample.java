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
 * <p>
 * You can use this component for your own project using one of the following
 * methods:
 * <ul>
 * <li>Download library and include it with your project</li>
 * <li>Download sources and modify it for your needs</li>
 * <li>Import library using Maven
 * </ul>
 *
 * Libraries (groupId:artifactId:version):<br>
 * org.exbin.bined:bined-swt:0.2.2<br>
 * org.exbin.auxiliary:binary_data:0.2.2<br>
 * org.exbin.auxiliary:binary_data-array:0.2.2<br>
 * + SWT libraries<br>
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdExample {

    public static void main(String[] args) {
        // Prepare window shell
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("BinEd Library SWT Example");
        shell.setSize(1000, 600);
        shell.setLayout(new FillLayout());

        // Create component instance
        final CodeArea basicCodeArea = new CodeArea(shell, SWT.BORDER);

        // Fill it with some data
        basicCodeArea.setContentData(new ByteArrayEditableData(new byte[]{1, 2, 3}));

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
