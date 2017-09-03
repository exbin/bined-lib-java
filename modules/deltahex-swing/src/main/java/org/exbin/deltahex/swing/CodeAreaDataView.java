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
package org.exbin.deltahex.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 * Hexadecimal viewer/editor inner data view component.
 *
 * @version 0.2.0 2017/06/21
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaDataView extends JComponent {

    private final CodeArea codeArea;

    public CodeAreaDataView(CodeArea codeArea) {
        this.codeArea = codeArea;
        init();
    }

    private void init() {
        setBackground(Color.RED);
        setBorder(null);
        setLayout(null);
        
        Dimension dimension = new Dimension(5000, 5000);
        setPreferredSize(dimension);
    }

    @Override
    protected void paintComponent(Graphics g) {
        codeArea.getPainter().paintMainArea(g);
    }
}
