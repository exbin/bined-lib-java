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
package org.exbin.bined.swing.extended.layout;

/**
 * Support for anti-aliasing capability.
 *
 * TODO
 *
 * @version 0.2.0 2018/11/21
 * @author ExBin Project (https://exbin.org)
 */
public class ExtendedCodeAreaDecorationsProfile {

    public enum DecorationType {
        ROW_NUMBER_LINE,
        HEADER_LINE,
        SPLIT_LINE,
        BOX_LINE
    }

    public boolean showRowNumberLine() {
        return true;
    }

    public boolean showHeaderLine() {
        return true;
    }

    public boolean showBoxLine() {
        return true;
    }
}
