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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hex editor painter.
 *
 * @version 0.1.0 2016/05/25
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultHexadecimalPainter implements HexadecimalPainter {

    protected final Hexadecimal hexadecimal;

    private Charset charMappingCharset = null;
    protected final char[] charMapping = new char[256];
    protected Map<Character, Character> nonprintingMapping = null;
    protected byte[] lineData;

    public DefaultHexadecimalPainter(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
    }

    @Override
    public void paintOverall(Graphics g) {
        Rectangle compRect = hexadecimal.getComponentRectangle();
        Rectangle hexRect = hexadecimal.getHexadecimalRectangle();
        int decorationMode = hexadecimal.getDecorationMode();
        if ((decorationMode & Hexadecimal.DECORATION_LINENUM_HEX_LINE) > 0) {
            g.setColor(Color.GRAY);
            int lineX = hexRect.x - hexadecimal.getCharWidth() / 2;
            g.drawLine(lineX, compRect.y, lineX, hexRect.y);
        }
        if ((decorationMode & Hexadecimal.DECORATION_BOX) > 0) {
            g.setColor(Color.GRAY);
            g.drawLine(hexRect.x - 1, hexRect.y - 1, hexRect.x + hexRect.width, hexRect.y - 1);
        }
    }

    @Override
    public void paintHeader(Graphics g) {
        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        Rectangle compRect = hexadecimal.getComponentRectangle();
        Rectangle hexRect = hexadecimal.getHexadecimalRectangle();
        if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.PREVIEW) {
            int charWidth = hexadecimal.getCharWidth();
            int bytesPerBounds = hexadecimal.getBytesPerLine();
            int headerX = hexRect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
            int headerY = hexadecimal.getLineHeight();

            if (hexadecimal.getBackgroundMode() == Hexadecimal.BackgroundMode.GRIDDED) {
                g.setColor(hexadecimal.getOddBackgroundColor());
                int positionX = hexRect.x - scrollPosition.scrollByteOffset - scrollPosition.scrollBytePosition * charWidth;
                for (int i = 0; i < bytesPerBounds / 2; i++) {
                    g.fillRect(positionX + charWidth * (3 + i * 6), hexRect.y, charWidth * 2, hexRect.height);
                }
            }

            g.setColor(hexadecimal.getForeground());
            char[] chars = new char[2];
            if (hexadecimal.isCharFixedMode()) {
                for (int i = 0; i < bytesPerBounds; i++) {
                    HexadecimalUtils.byteToHexChars(chars, (byte) i);
                    g.drawChars(chars, 0, 2, headerX + i * charWidth * 3, headerY);
                }
            } else {
                for (int i = 0; i < bytesPerBounds; i++) {
                    HexadecimalUtils.byteToHexChars(chars, (byte) i);
                    int startX = headerX + i * charWidth * 3;
                    drawCenteredChar(g, chars, 0, charWidth, startX, headerY);
                    drawCenteredChar(g, chars, 1, charWidth, startX, headerY);
                }
            }
        }

        int decorationMode = hexadecimal.getDecorationMode();
        if ((decorationMode & Hexadecimal.DECORATION_HEX_PREVIEW_LINE) > 0) {
            int lineX = hexadecimal.getPreviewX() - scrollPosition.scrollBytePosition * hexadecimal.getCharWidth() - scrollPosition.scrollByteOffset - hexadecimal.getCharWidth() / 2;
            if (lineX >= hexRect.x) {
                g.setColor(Color.GRAY);
                g.drawLine(lineX, compRect.y, lineX, hexRect.y);
            }
        }
    }

    @Override
    public void paintBackground(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle hexRect = hexadecimal.getHexadecimalRectangle();
        int bytesPerBounds = hexadecimal.getBytesPerLine();
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

            positionY = hexRect.y - scrollPosition.scrollLineOffset;
            if ((line & 1) == 0) {
                positionY += lineHeight;
                dataPosition += bytesPerBounds;
            }
            while (positionY <= maxY && dataPosition < maxDataPosition) {
                g.fillRect(0, positionY, hexRect.x + hexRect.width, lineHeight);
                positionY += lineHeight * 2;
                dataPosition += bytesPerBounds * 2;
            }
        }
    }

    @Override
    public void paintLineNumbers(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle compRect = hexadecimal.getComponentRectangle();
        Rectangle hexRect = hexadecimal.getHexadecimalRectangle();
        int bytesPerBounds = hexadecimal.getBytesPerLine();
        int lineHeight = hexadecimal.getLineHeight();

        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        long line = scrollPosition.scrollLinePosition;
        long maxDataPosition = hexadecimal.getData().getDataSize();
        int maxY = clipBounds.y + clipBounds.height + lineHeight;
        long dataPosition = line * bytesPerBounds;
        int charWidth = hexadecimal.getCharWidth();
        int positionY = hexRect.y - hexadecimal.getSubFontSpace() - scrollPosition.scrollLineOffset + hexadecimal.getLineHeight();

        g.setColor(hexadecimal.getForeground());
        char[] lineNumberCode = new char[8];
        while (positionY <= maxY && dataPosition <= maxDataPosition) {
            HexadecimalUtils.longToHexChars(lineNumberCode, dataPosition, 8);
            if (hexadecimal.isCharFixedMode()) {
                g.drawChars(lineNumberCode, 0, 8, compRect.x, positionY);
            } else {
                for (int i = 0; i < 8; i++) {
                    drawCenteredChar(g, lineNumberCode, i, charWidth, compRect.x, positionY);
                }
            }
            positionY += lineHeight;
            dataPosition += bytesPerBounds;
        }

        int decorationMode = hexadecimal.getDecorationMode();
        if ((decorationMode & Hexadecimal.DECORATION_LINENUM_HEX_LINE) > 0) {
            g.setColor(Color.GRAY);
            int lineX = hexRect.x - hexadecimal.getCharWidth() / 2;
            g.drawLine(lineX, compRect.y, lineX, hexRect.y + hexRect.height);
        }
        if ((decorationMode & Hexadecimal.DECORATION_BOX) > 0) {
            g.setColor(Color.GRAY);
            g.drawLine(hexRect.x - 1, hexRect.y - 1, hexRect.x - 1, hexRect.y + hexRect.height);
        }
    }

    @Override
    public void paintMainArea(Graphics g) {
        Rectangle hexRect = hexadecimal.getHexadecimalRectangle();
        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        int charWidth = hexadecimal.getCharWidth();
        int bytesPerLine = hexadecimal.getBytesPerLine();
        int lineHeight = hexadecimal.getLineHeight();

        if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.PREVIEW && hexadecimal.getBackgroundMode() == Hexadecimal.BackgroundMode.GRIDDED) {
            g.setColor(hexadecimal.getOddBackgroundColor());
            int positionX = hexRect.x - scrollPosition.scrollByteOffset - scrollPosition.scrollBytePosition * charWidth;
            for (int i = 0; i < bytesPerLine / 2; i++) {
                g.fillRect(positionX + charWidth * (3 + i * 6), hexRect.y, charWidth * 2, hexRect.height);
            }
        }

        lineData = new byte[bytesPerLine];

        int positionY = hexRect.y - scrollPosition.scrollLineOffset;
        long line = scrollPosition.scrollLinePosition;
        int positionX = hexRect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        long dataPosition = line * bytesPerLine;

        // Shared chars
        char[] chars = new char[2];
        do {
            paintLineBackground(g, line, positionY + lineHeight, dataPosition, bytesPerLine, lineHeight, charWidth, lineData);
            paintLineText(g, line, positionX, positionY, dataPosition, bytesPerLine, lineHeight, charWidth, lineData, chars);
            dataPosition += bytesPerLine;
            line++;
            positionY += lineHeight;
        } while (positionY - lineHeight < hexRect.y + hexRect.height);

        int decorationMode = hexadecimal.getDecorationMode();
        if ((decorationMode & Hexadecimal.DECORATION_HEX_PREVIEW_LINE) > 0) {
            int lineX = hexadecimal.getPreviewX() - scrollPosition.scrollBytePosition * hexadecimal.getCharWidth() - scrollPosition.scrollByteOffset - hexadecimal.getCharWidth() / 2;
            if (lineX >= hexRect.x) {
                g.setColor(Color.GRAY);
                g.drawLine(lineX, hexRect.y, lineX, hexRect.y + hexRect.height);
            }
        }
    }

    public void paintLineBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerBounds, int lineHeight, int charWidth, byte[] lineData) {
        paintSelectionBackground(g, line, positionY, dataPosition, bytesPerBounds, lineHeight, charWidth);
    }

    public void paintLineText(Graphics g, long line, int linePositionX, int linePositionY, long dataPosition, int bytesPerBounds, int lineHeight, int charWidth, byte[] lineData, char[] chars) {
        int bytesPerLine = hexadecimal.getBytesPerLine();
        long dataSize = hexadecimal.getData().getDataSize();
        for (int byteOnLine = 0; byteOnLine < bytesPerLine; byteOnLine++) {
            if (dataPosition < dataSize || (dataPosition == dataSize && byteOnLine == 0)) {
                paintText(g, line, linePositionX, byteOnLine, linePositionY + lineHeight, dataPosition, bytesPerLine, lineHeight, charWidth, chars);
            } else {
                break;
            }

            dataPosition++;
        }
    }

    public void paintText(Graphics g, long line, int linePositionX, int byteOnLine, int linePositionY, long dataPosition, int bytesPerBounds, int lineHeight, int charWidth, char[] chars) {
        BinaryData data = hexadecimal.getData();
        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        int positionY = linePositionY - hexadecimal.getSubFontSpace();
        g.setColor(hexadecimal.getForeground());
        if (dataPosition < data.getDataSize()) {
            byte dataByte = data.getByte(dataPosition);
            if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.PREVIEW) {
                int startX = linePositionX + byteOnLine * charWidth * 3;
                HexadecimalUtils.byteToHexChars(chars, dataByte);
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
                Charset charset = hexadecimal.getCharset();
                int charLength = 1;
                String charsetName = charset.name().toLowerCase();
                if (charsetName.startsWith("utf") || charsetName.startsWith("x-utf")) {
                    charLength = 4;
                }

                char[] previewChar = new char[]{' '};
                if (charLength > 1) {
                    if (dataPosition + charLength > data.getDataSize()) {
                        charLength = (int) (data.getDataSize() - dataPosition);
                    }

                    byte[] displayData = new byte[charLength];
                    for (int i = 0; i < displayData.length; i++) {
                        displayData[i] = data.getByte(dataPosition + i);
                    }
                    String displayString = new String(displayData, charset);
                    if (!displayString.isEmpty()) {
                        previewChar[0] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        for (int i = 0; i < 256; i++) {
                            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
                        }
                        charMappingCharset = charset;
                    }

                    previewChar[0] = (char) charMapping[dataByte & 0xFF];
                }

                Character replacement = null;
                if (hexadecimal.isShowNonprintingCharacters()) {
                    if (nonprintingMapping == null) {
                        nonprintingMapping = new HashMap<>();
                        // Unicode control characters, might not be supported by font
                        for (int i = 0; i < 32; i++) {
                            nonprintingMapping.put((char) i, Character.toChars(9216 + i)[0]);
                        }
                        // Space -> Middle Dot
                        nonprintingMapping.put(' ', Character.toChars(183)[0]);
                        // Tab -> Right-Pointing Double Angle Quotation Mark
                        nonprintingMapping.put('\t', Character.toChars(187)[0]);
                        // Line Feed -> Currency Sign
                        nonprintingMapping.put('\r', Character.toChars(164)[0]);
                        // Carriage Return -> Pilcrow Sign
                        nonprintingMapping.put('\n', Character.toChars(182)[0]);
                        // Ideographic Space -> Degree Sign
                        nonprintingMapping.put(Character.toChars(127)[0], Character.toChars(176)[0]);
                    }
                    replacement = nonprintingMapping.get(previewChar[0]);
                    if (replacement != null) {
                        previewChar[0] = replacement;
                    }
                }

                if (replacement != null) {
                    g.setColor(hexadecimal.getWhiteSpaceColor());
                }
                if (hexadecimal.isCharFixedMode()) {
                    g.drawChars(previewChar, 0, 1, startX, positionY);
                } else {
                    drawCenteredChar(g, previewChar, 0, charWidth, startX, positionY);
                }
                if (replacement != null) {
                    g.setColor(hexadecimal.getForeground());
                }
            }
        }
    }

    public void paintSelectionBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerBounds, int fontHeight, int charWidth) {
        Rectangle hexRect = hexadecimal.getHexadecimalRectangle();

        Hexadecimal.SelectionRange selection = hexadecimal.getSelection();
        if (selection == null) {
            return;
        }

        Hexadecimal.ScrollPosition scrollPosition = hexadecimal.getScrollPosition();
        int selectionStart = 0;
        int selectionEnd = 0;
        int selectionPreviewStart = 0;
        int selectionPreviewEnd = 0;
        int startX = hexRect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        int previewStartX = hexadecimal.getPreviewX() - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;

        long maxLinePosition = dataPosition + bytesPerBounds - 1;
        long selectionFirst = selection.getFirst();
        long selectionLast = selection.getLast();
        if (selectionFirst <= maxLinePosition) {
            if (selectionFirst >= dataPosition) {
                int linePosition = (int) (selectionFirst - dataPosition);
                selectionStart = startX + charWidth * (linePosition * 3);
                selectionPreviewStart = previewStartX + charWidth * linePosition;
            } else {
                selectionStart = startX;
                selectionPreviewStart = previewStartX;
            }
        }

        if (selectionLast >= dataPosition && selectionFirst <= maxLinePosition) {
            if (selectionLast >= maxLinePosition) {
                selectionEnd = startX + (bytesPerBounds * 3 - 1) * charWidth;
                selectionPreviewEnd = previewStartX + bytesPerBounds * charWidth;
            } else {
                int linePosition = (int) (selectionLast - dataPosition + 1);
                selectionEnd = startX + charWidth * (linePosition * 3);
                selectionPreviewEnd = previewStartX + charWidth * linePosition;
            }
        }

        if (selectionEnd > 0) {
            Color hexadecimalColor;
            Color previewColor;
            switch (hexadecimal.getActiveSection()) {
                case HEXADECIMAL: {
                    hexadecimalColor = hexadecimal.getSelectionBackgroundColor();
                    previewColor = hexadecimal.getMirrorSelectionBackgroundColor();
                    break;
                }
                case PREVIEW: {
                    hexadecimalColor = hexadecimal.getMirrorSelectionBackgroundColor();
                    previewColor = hexadecimal.getSelectionBackgroundColor();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected active section " + hexadecimal.getActiveSection().name());
                }
            }

            if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.PREVIEW) {
                g.setColor(hexadecimalColor);
                g.fillRect(selectionStart, positionY - fontHeight, selectionEnd - selectionStart, fontHeight);
            }

            if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.HEXADECIMAL) {
                g.setColor(previewColor);
                g.fillRect(selectionPreviewStart, positionY - fontHeight, selectionPreviewEnd - selectionPreviewStart, fontHeight);
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
