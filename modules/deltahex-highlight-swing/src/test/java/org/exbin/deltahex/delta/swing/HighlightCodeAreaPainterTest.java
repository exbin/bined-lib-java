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
package org.exbin.deltahex.delta.swing;

import org.exbin.deltahex.highlight.swing.HighlightCodeAreaPainter;
import org.exbin.deltahex.swing.CodeArea;
import org.junit.Test;

/**
 * Tests for highlighting code area painter.
 *
 * @version 0.1.2 2016/12/10
 * @author ExBin Project (http://exbin.org)
 */
public class HighlightCodeAreaPainterTest {

    public HighlightCodeAreaPainterTest() {
    }

    @Test
    public void testSetPainter() {
        CodeArea codeArea = new CodeArea();
        HighlightCodeAreaPainter painter = new HighlightCodeAreaPainter(codeArea);
        codeArea.setPainter(painter);
    }
}
