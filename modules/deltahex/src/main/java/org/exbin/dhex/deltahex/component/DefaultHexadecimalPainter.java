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

import org.exbin.dhex.deltahex.HexadecimalUtils;
import org.exbin.dhex.deltahex.CaretPosition;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Hex editor line painter.
 *
 * @version 0.1.0 2016/04/09
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultHexadecimalPainter implements HexadecimalPainter {

    private final Hexadecimal hexadecimal;

    public DefaultHexadecimalPainter(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
    }

    @Override
    public void paintHeader(Graphics g, int positionY, int bytesPerLine, int charWidth) {
        int hexadecimalX = hexadecimal.getHexadecimalX();
        g.setColor(hexadecimal.getForeground());
        if (hexadecimal.isCharFixedMode()) {
            for (int i = 0; i < bytesPerLine; i++) {
                char[] chars = HexadecimalUtils.byteToHexChars((byte) i);
                g.drawChars(chars, 0, 2, hexadecimalX + i * charWidth * 3, positionY);
            }
        } else {
            for (int i = 0; i < bytesPerLine; i++) {
                char[] chars = HexadecimalUtils.byteToHexChars((byte) i);
                int startX = hexadecimalX + i * charWidth * 3;
                drawCenteredChar(g, chars, 0, charWidth, startX, positionY);
                drawCenteredChar(g, chars, 1, charWidth, startX, positionY);
            }
        }
    }

    @Override
    public void paintBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerLine, int fontHeight, int charWidth) {
        Hexadecimal.BackgroundMode backgroundMode = hexadecimal.getBackgroundMode();
        g.setColor((line & 1) == 0 && backgroundMode != Hexadecimal.BackgroundMode.PLAIN
                ? hexadecimal.getBackground() : hexadecimal.getOddBackgroundColor());
        g.fillRect(0, positionY - fontHeight, g.getClipBounds().width, fontHeight);
        if (backgroundMode == Hexadecimal.BackgroundMode.GRIDDED && (line & 1) == 0) {
            g.setColor(hexadecimal.getOddBackgroundColor());
            for (int i = 0; i < bytesPerLine / 2; i++) {
                g.fillRect(hexadecimal.getHexadecimalX() + charWidth * (3 + i * 6), positionY - fontHeight, charWidth * 2, fontHeight);
            }
        }

        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        if (selection == null) {
            return;
        }

        int selectionStart = 0;
        int selectionEnd = 0;
        int selectionPreviewStart = 0;
        int selectionPreviewEnd = 0;

        long maxLinePosition = dataPosition + bytesPerLine;
        CaretPosition selectionFirst = selection.getSelectionFirst();
        CaretPosition selectionLast = selection.getSelectionLast();
        if (selectionFirst.getDataPosition() < maxLinePosition) {
            if (selectionFirst.getDataPosition() > dataPosition) {
                int linePosition = (int) (selectionFirst.getDataPosition() - dataPosition);
                int halfPosition = selectionFirst.isLowerHalf() ? 1 : 0;
                selectionStart = hexadecimal.getHexadecimalX() + charWidth * (linePosition * 3 + halfPosition);
                selectionPreviewStart = hexadecimal.getPreviewX() + charWidth * linePosition;
            } else {
                selectionStart = hexadecimal.getHexadecimalX();
                selectionPreviewStart = hexadecimal.getPreviewX();
            }
        }

        if (selectionLast.getDataPosition() > dataPosition && selectionFirst.getDataPosition() < maxLinePosition) {
            if (selectionLast.getDataPosition() > maxLinePosition) {
                selectionEnd = hexadecimal.getHexadecimalX() + bytesPerLine * charWidth * 3;
                selectionPreviewEnd = hexadecimal.getPreviewX() + bytesPerLine * charWidth;
            } else {
                int linePosition = (int) (selectionLast.getDataPosition() - dataPosition);
                int halfPosition = selectionLast.isLowerHalf() ? 1 : 0;
                selectionEnd = hexadecimal.getHexadecimalX() + charWidth * (linePosition * 3 + halfPosition);
                selectionPreviewEnd = hexadecimal.getPreviewX() + charWidth * linePosition;
            }
        }

        if (selectionEnd > 0) {
            Color hexadecimalColor;
            Color previewColor;
            switch (hexadecimal.getActiveSection()) {
                case HEXADECIMAL: {
                    hexadecimalColor = hexadecimal.getSelectionBackgroundColor();
                    previewColor = hexadecimal.getDualBackgroundColor();
                    break;
                }
                case PREVIEW: {
                    hexadecimalColor = hexadecimal.getDualBackgroundColor();
                    previewColor = hexadecimal.getSelectionBackgroundColor();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected active section " + hexadecimal.getActiveSection().name());
                }
            }

            g.setColor(hexadecimalColor);
            g.fillRect(selectionStart, positionY - fontHeight, selectionEnd - selectionStart, fontHeight);

            if (hexadecimal.getViewMode() == Hexadecimal.ViewMode.DUAL) {
                g.setColor(previewColor);
                g.fillRect(selectionPreviewStart, positionY - fontHeight, selectionPreviewEnd - selectionPreviewStart, fontHeight);
            }
        }
    }

    @Override
    public void paintText(Graphics g, long line, int linePositionX, int byteOnLine, int linePositionY, long dataPosition, int bytesPerLine, int fontHeight, int charWidth) {
        int positionY = linePositionY - hexadecimal.getSubFontSpace();
        g.setColor(hexadecimal.getForeground());
        if (byteOnLine == 0 && hexadecimal.isShowLineNumbers()) {
            char[] lineNumberCode = HexadecimalUtils.longToHexChars(dataPosition);
            g.drawChars(lineNumberCode, 0, 8, 0, positionY);
        }
        if (dataPosition < hexadecimal.getData().getDataSize()) {
            byte dataByte = hexadecimal.getData().getByte(dataPosition);
            if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.PREVIEW) {
                int startX = linePositionX + byteOnLine * charWidth * 3;
                char[] chars = HexadecimalUtils.byteToHexChars(dataByte);
                if (hexadecimal.isCharFixedMode()) {
                    g.drawChars(chars, 0, 2, startX, positionY);
                } else {
                    drawCenteredChar(g, chars, 0, charWidth, startX, positionY);
                    drawCenteredChar(g, chars, 1, charWidth, startX, positionY);
                }
            }

            if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.HEXADECIMAL) {
                int startX = hexadecimal.getPreviewX() + byteOnLine * charWidth;
                char[] previewChar = new char[]{(char) dataByte};
                if (hexadecimal.isCharFixedMode()) {
                    g.drawChars(previewChar, 0, 1, startX, positionY);
                } else {
                    drawCenteredChar(g, previewChar, 0, charWidth, startX, positionY);
                }
            }
        }
    }

    /**
     * Draws char in array centering it in precomputed space.
     *
     * @param g graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param charWidthSpace default character width
     * @param startX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChar(Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY) {
        int charWidth = g.getFontMetrics().charWidth(drawnChars[charOffset]);
        int leftSpace = (charWidthSpace - charWidth) / 2;
        g.drawChars(drawnChars, charOffset, 1, startX + charWidthSpace * charOffset + leftSpace, positionY);
    }
}
