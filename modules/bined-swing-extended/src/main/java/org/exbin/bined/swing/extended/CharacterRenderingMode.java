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
package org.exbin.bined.swing.extended;

/**
 * Character rendering mode.
 *
 * @version 0.2.0 2017/07/26
 * @author ExBin Project (https://exbin.org)
 */
public enum CharacterRenderingMode {
    /**
     * Centers characters if width is not default and detects monospace fonts to
     * render characters as string if possible
     */
    AUTO,
    /**
     * Render sequence of characters from top left corner of the line ignoring
     * character width. It's fastest, but render correctly only for monospaced
     * fonts and charsets where all characters have same width
     */
    LINE_AT_ONCE,
    /**
     * Render each character from top left corner of it's position
     */
    TOP_LEFT,
    /**
     * Centers each character in it's area
     */
    CENTER
}
