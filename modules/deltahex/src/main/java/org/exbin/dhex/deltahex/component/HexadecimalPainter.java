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
package org.exbin.dhex.deltahex.component;

import java.awt.Graphics;

/**
 * Hexadecimal editor painter.
 *
 * @version 0.1.0 2016/04/10
 * @author ExBin Project (http://exbin.org)
 */
public interface HexadecimalPainter {

    /**
     * Paints header for hexadecimal editor.
     *
     * @param g graphics
     * @param positionY position of the bottom line of current line in pixels
     * @param bytesPerLine data bytes per line
     * @param charWidth font character width
     */
    void paintHeader(Graphics g, int positionY, int bytesPerLine, int charWidth);

    /**
     * Paints line number.
     *
     * @param g graphics
     * @param bytesPerLine data bytes per line
     * @param charWidth font character width
     */
    void paintLineNumbers(Graphics g, int bytesPerLine, int charWidth);

    void paintHexadecimal(Graphics g, int linePositionX, int linePositionY, int bytesPerLine, int fontHeight, int charWidth);
    
    /**
     * Paints line for given hexadecimal editor line.
     *
     * @param g graphics
     * @param line line number
     * @param linePositionX position of left side
     * @param byteOnLine number of byte on the current line
     * @param linePositionY position of the bottom line of current line in
     * pixels
     * @param dataPosition current data position
     * @param bytesPerLine data bytes per line
     * @param fontHeight font character height
     * @param charWidth font character width
     */
    void paintText(Graphics g, long line, int linePositionX, int byteOnLine, int linePositionY, long dataPosition, int bytesPerLine, int fontHeight, int charWidth);

    /**
     * Paints background for given hexadecimal editor line.
     *
     * @param g graphics
     * @param line line number
     * @param positionY position of the bottom line of current line in pixels
     * @param dataPosition current data position
     * @param bytesPerLine data bytes per line
     * @param fontHeight font character height
     * @param charWidth font character width
     */
    void paintBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerLine, int fontHeight, int charWidth);
}
