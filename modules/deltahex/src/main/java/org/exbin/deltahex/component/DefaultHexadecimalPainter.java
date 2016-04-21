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
package org.exbin.deltahex.component;

import org.exbin.deltahex.HexadecimalUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Hex editor painter.
 *
 * @version 0.1.0 2016/04/18
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultHexadecimalPainter implements HexadecimalPainter {

    private final Hexadecimal hexadecimal;

    public DefaultHexadecimalPainter(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
    }

    @Override
    public void paintOverall(Graphics g) {
        Rectangle rect = hexadecimal.getHexadecimalRectangle();
        switch (hexadecimal.getDecorationMode()) {
            case LINES: {
                g.setColor(Color.GRAY);
                int lineX = rect.x - hexadecimal.getCharWidth() / 2;
                g.drawLine(lineX, 0, lineX, rect.y);
                break;
            }
            case BOX: {
                break;
            }
            default: {
                // Do nothing
            }
        }
    }

    @Override
    public void paintHeader(Graphics g) {
        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        Rectangle rect = hexadecimal.getHexadecimalRectangle();
        g.setColor(hexadecimal.getForeground());
        int charWidth = hexadecimal.getCharWidth();
        int bytesPerBounds = hexadecimal.getBytesPerBounds();
        int headerX = rect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        int headerY = hexadecimal.getLineHeight();
        if (hexadecimal.isCharFixedMode()) {
            for (int i = 0; i < bytesPerBounds; i++) {
                char[] chars = HexadecimalUtils.byteToHexChars((byte) i);
                g.drawChars(chars, 0, 2, headerX + i * charWidth * 3, headerY);
            }
        } else {
            for (int i = 0; i < bytesPerBounds; i++) {
                char[] chars = HexadecimalUtils.byteToHexChars((byte) i);
                int startX = headerX + i * charWidth * 3;
                drawCenteredChar(g, chars, 0, charWidth, startX, headerY);
                drawCenteredChar(g, chars, 1, charWidth, startX, headerY);
            }
        }

        if (hexadecimal.getDecorationMode() == Hexadecimal.DecorationMode.LINES) {
            int lineX = hexadecimal.getPreviewX() - scrollPosition.scrollBytePosition * hexadecimal.getCharWidth() - scrollPosition.scrollByteOffset - hexadecimal.getCharWidth() / 2;
            if (lineX >= rect.x) {
                g.setColor(Color.GRAY);
                g.drawLine(lineX, 0, lineX, rect.y);
            }
        }
    }

    @Override
    public void paintBackground(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle rect = hexadecimal.getHexadecimalRectangle();
        int charWidth = hexadecimal.getCharWidth();
        int bytesPerBounds = hexadecimal.getBytesPerBounds();
        int lineHeight = hexadecimal.getLineHeight();
        if (hexadecimal.getBackgroundMode() != Hexadecimal.BackgroundMode.NONE) {
            g.setColor(hexadecimal.getBackground());
            g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        }

        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        long line = scrollPosition.scrollLinePosition;
        long maxDataPosition = hexadecimal.getData().getDataSize();
        int maxY = clipBounds.y + clipBounds.height;

        int positionY;
        long dataPosition = line * bytesPerBounds;
        if (hexadecimal.getBackgroundMode() != Hexadecimal.BackgroundMode.PLAIN) {
            g.setColor(hexadecimal.getOddBackgroundColor());

            positionY = rect.y - scrollPosition.scrollLineOffset;
            if ((line & 1) == 0) {
                positionY += lineHeight;
                dataPosition += bytesPerBounds;
            }
            while (positionY <= maxY && dataPosition < maxDataPosition) {
                g.fillRect(0, positionY, rect.x + rect.width, lineHeight);
                positionY += lineHeight * 2;
                dataPosition += bytesPerBounds * 2;
            }

            if (hexadecimal.getBackgroundMode() == Hexadecimal.BackgroundMode.GRIDDED) {
                // TODO use while instead
                for (int i = 0; i < bytesPerBounds / 2; i++) {
                    g.fillRect(rect.x + charWidth * (3 + i * 6), clipBounds.y, charWidth * 2, clipBounds.height);
                }
            }
        }
    }

    @Override
    public void paintLineNumbers(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle rect = hexadecimal.getHexadecimalRectangle();
        int bytesPerBounds = hexadecimal.getBytesPerBounds();
        int lineHeight = hexadecimal.getLineHeight();

        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        long line = scrollPosition.scrollLinePosition;
        long maxDataPosition = hexadecimal.getData().getDataSize();
        int maxY = clipBounds.y + clipBounds.height + lineHeight;
        long dataPosition = line * bytesPerBounds;
        int positionY = rect.y - hexadecimal.getSubFontSpace() - scrollPosition.scrollLineOffset + hexadecimal.getLineHeight();

        g.setColor(hexadecimal.getForeground());
        while (positionY <= maxY && dataPosition < maxDataPosition) {
            char[] lineNumberCode = HexadecimalUtils.longToHexChars(dataPosition);
            g.drawChars(lineNumberCode, 0, 8, 0, positionY);
            positionY += lineHeight;
            dataPosition += bytesPerBounds;
        }

        switch (hexadecimal.getDecorationMode()) {
            case LINES: {
                g.setColor(Color.GRAY);
                int lineX = rect.x - hexadecimal.getCharWidth() / 2;
                g.drawLine(lineX, 0, lineX, rect.y + rect.height);
                break;
            }
            case BOX: {
                break;
            }
            default: {
                // Do nothing
            }
        }
    }

    public void paintSelectionBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerBounds, int fontHeight, int charWidth) {
        Rectangle rect = hexadecimal.getHexadecimalRectangle();

        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        if (selection == null) {
            return;
        }

        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        int selectionStart = 0;
        int selectionEnd = 0;
        int selectionPreviewStart = 0;
        int selectionPreviewEnd = 0;
        int startX = rect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        int previewX = hexadecimal.getPreviewX() - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;

        long maxLinePosition = dataPosition + bytesPerBounds;
        long selectionFirst = selection.getFirst();
        long selectionLast = selection.getLast();
        if (selectionFirst < maxLinePosition) {
            if (selectionFirst >= dataPosition) {
                int linePosition = (int) (selectionFirst - dataPosition);
                selectionStart = startX + charWidth * (linePosition * 3);
                selectionPreviewStart = previewX + charWidth * linePosition;
            } else {
                selectionStart = startX;
                selectionPreviewStart = previewX;
            }
        }

        if (selectionLast >= dataPosition && selectionFirst < maxLinePosition) {
            if (selectionLast > maxLinePosition) {
                selectionEnd = startX + bytesPerBounds * charWidth * 3;
                selectionPreviewEnd = previewX + bytesPerBounds * charWidth;
            } else {
                int linePosition = (int) (selectionLast - dataPosition + 1);
                selectionEnd = startX + charWidth * (linePosition * 3);
                selectionPreviewEnd = previewX + charWidth * linePosition;
            }
        }

        if (selectionEnd > 0) {
            Color hexadecimalColor;
            Color previewColor;
            switch (hexadecimal.getActiveSection()) {
                case HEXADECIMAL: {
                    hexadecimalColor = hexadecimal.getSelectionBackgroundColor();
                    previewColor = hexadecimal.getDualSelectionBackgroundColor();
                    break;
                }
                case PREVIEW: {
                    hexadecimalColor = hexadecimal.getDualSelectionBackgroundColor();
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
    public void paintHexadecimal(Graphics g) {
        Rectangle rect = hexadecimal.getHexadecimalRectangle();
        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        int charWidth = hexadecimal.getCharWidth();
        int bytesPerBounds = hexadecimal.getBytesPerBounds();
        int lineHeight = hexadecimal.getLineHeight();

        int positionY = rect.y - scrollPosition.scrollLineOffset;
        long line = scrollPosition.scrollLinePosition;
        int positionX = rect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        int byteOnLine = 0;
        long dataPosition = line * bytesPerBounds;
        long dataSize = hexadecimal.getData().getDataSize();
        do {
            if (byteOnLine == 0) {
                paintSelectionBackground(g, line, positionY + lineHeight, dataPosition, bytesPerBounds, lineHeight, charWidth);
            }

            if (dataPosition < dataSize || (dataPosition == dataSize && byteOnLine == 0)) {
                paintText(g, line, positionX, byteOnLine, positionY + lineHeight, dataPosition, bytesPerBounds, lineHeight, charWidth);
            } else {
                break;
            }

            byteOnLine++;
            dataPosition++;

            if (byteOnLine == bytesPerBounds) {
                byteOnLine = 0;
                positionY += lineHeight;
                line++;
            }
        } while (positionY - lineHeight < rect.y + rect.height);

        switch (hexadecimal.getDecorationMode()) {
            case LINES: {
                int lineX = hexadecimal.getPreviewX() - scrollPosition.scrollBytePosition * hexadecimal.getCharWidth() - scrollPosition.scrollByteOffset - hexadecimal.getCharWidth() / 2;
                if (lineX >= rect.x) {
                    g.setColor(Color.GRAY);
                    g.drawLine(lineX, rect.y, lineX, rect.y + rect.height);
                }

                break;
            }
            case BOX: {
                break;
            }
            default: {
                // Do nothing
            }
        }
    }

    public void paintText(Graphics g, long line, int linePositionX, int byteOnLine, int linePositionY, long dataPosition, int bytesPerBounds, int fontHeight, int charWidth) {
        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
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
                startX -= scrollPosition.scrollBytePosition * charWidth + scrollPosition.scrollByteOffset;
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
