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
package org.exbin.deltahex;

import javax.swing.JComponent;

/**
 * Line number length.
 *
 * @version 0.1.0 2016/06/19
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaLineNumberLength extends JComponent {

    private LineNumberType lineNumberType;
    private int lineNumberLength;

    public LineNumberType getSpaceType() {
        return lineNumberType;
    }

    public void setSpaceType(LineNumberType lineNumberType) {
        this.lineNumberType = lineNumberType;
    }

    public int getLineNumberLength() {
        return lineNumberLength;
    }

    public void setLineNumberLength(int lineNumberLength) {
        this.lineNumberLength = lineNumberLength;
    }

    public static enum LineNumberType {
        /**
         * Line number is computed from data size and position code type.
         */
        AUTO,
        SPECIFIED
    }
}
