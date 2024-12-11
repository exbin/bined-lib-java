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
package org.exbin.bined.editor.basic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JApplet;
import javax.swing.JToolBar;
import org.exbin.bined.swing.basic.CodeArea;

/**
 * Basic java applet simple version of BinEd binary/hex editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdEditorBasicApplet extends JApplet {

    /**
     * Initialization method that will be called after the applet is loaded into
     * the browser.
     */
    @Override
    public void init() {
        // TODO start asynchronous download of heavy resources
    }

    @Override
    public void start() {
        BinEdEditorBasic basicEditor = new BinEdEditorBasic();
        Container contentPane = basicEditor.getContentPane();
        Component[] components = contentPane.getComponents();
        contentPane.removeAll();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component instanceof CodeArea) {
                add(component, BorderLayout.CENTER);
            } else if (component instanceof JToolBar) {
                add(component, BorderLayout.NORTH);
            } else {
                add(component, java.awt.BorderLayout.PAGE_END);
            }
        }
    }
}
