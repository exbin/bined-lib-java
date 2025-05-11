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
package org.exbin.bined.lanterna.example;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;
import org.exbin.bined.lanterna.basic.CodeArea;

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

        // Setup terminal and screen layers
        try {
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            // Create window to hold the panel
            BasicWindow window = new BasicWindow();

            // Create gui and start gui
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            gui.addWindowAndWait(window);
        } catch (IOException ex) {
            Logger.getLogger(BinEdExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
