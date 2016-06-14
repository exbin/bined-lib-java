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
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal component painter.
 *
 * @version 0.1.0 2016/06/15
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    protected final CodeArea codeArea;

    private Charset charMappingCharset = null;
    protected final char[] charMapping = new char[256];
    private Charset charShiftsCharset = null;
    protected final byte[] charShifts = new byte[16];
    private char[] hexCharacters = CodeAreaUtils.UPPER_HEX_CODES;
    protected Map<Character, Character> nonprintingMapping = null;

    public DefaultCodeAreaPainter(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void paintOverall(Graphics g) {
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_LINENUM_HEX_LINE) > 0) {
            g.setColor(Color.GRAY);
            int lineX = hexRect.x - codeArea.getCharWidth() / 2;
            g.drawLine(lineX, compRect.y, lineX, hexRect.y);
        }
        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
            g.setColor(Color.GRAY);
            g.drawLine(hexRect.x - 1, hexRect.y - 1, hexRect.x + hexRect.width, hexRect.y - 1);
        }
    }

    @Override
    public void paintHeader(Graphics g) {
        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;
        if (codeArea.getViewMode() != CodeArea.ViewMode.TEXT_PREVIEW) {
            int charWidth = codeArea.getCharWidth();
            int bytesPerBounds = codeArea.getBytesPerLine();
            int headerX = hexRect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
            int headerY = codeArea.getLineHeight();

            if (codeArea.getBackgroundMode() == CodeArea.BackgroundMode.GRIDDED) {
                g.setColor(codeArea.getOddBackgroundColor());
                int positionX = hexRect.x - scrollPosition.scrollByteOffset - scrollPosition.scrollBytePosition * charWidth;
                for (int i = 0; i < bytesPerBounds / 2; i++) {
                    g.fillRect(positionX + charWidth * (3 + i * charsPerByte * 2), hexRect.y, charWidth * 2, hexRect.height);
                }
            }

            g.setColor(codeArea.getForeground());
            char[] chars = new char[2];
            if (codeArea.isCharFixedMode()) {
                for (int i = 0; i < bytesPerBounds; i++) {
                    CodeAreaUtils.byteToHexChars(chars, (byte) i);
                    g.drawChars(chars, 0, 2, headerX + i * charWidth * charsPerByte, headerY);
                }
            } else {
                for (int i = 0; i < bytesPerBounds; i++) {
                    CodeAreaUtils.byteToHexChars(chars, (byte) i);
                    int startX = headerX + i * charWidth * charsPerByte;
                    drawCenteredChar(g, chars, 0, charWidth, startX, headerY);
                    drawCenteredChar(g, chars, 1, charWidth, startX, headerY);
                }
            }
        }

        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_HEX_PREVIEW_LINE) > 0) {
            int lineX = codeArea.getPreviewX() - scrollPosition.scrollBytePosition * codeArea.getCharWidth() - scrollPosition.scrollByteOffset - codeArea.getCharWidth() / 2;
            if (lineX >= hexRect.x) {
                g.setColor(Color.GRAY);
                g.drawLine(lineX, compRect.y, lineX, hexRect.y);
            }
        }
    }

    @Override
    public void paintBackground(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        int bytesPerBounds = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        if (codeArea.getBackgroundMode() != CodeArea.BackgroundMode.NONE) {
            g.setColor(codeArea.getBackground());
            g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        }

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long line = scrollPosition.scrollLinePosition;
        long maxDataPosition = codeArea.getData().getDataSize();
        int maxY = clipBounds.y + clipBounds.height;

        int positionY;
        long dataPosition = line * bytesPerBounds;
        if (codeArea.getBackgroundMode() != CodeArea.BackgroundMode.PLAIN) {
            g.setColor(codeArea.getOddBackgroundColor());

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
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        int bytesPerBounds = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long line = scrollPosition.scrollLinePosition;
        long maxDataPosition = codeArea.getData().getDataSize();
        int maxY = clipBounds.y + clipBounds.height + lineHeight;
        long dataPosition = line * bytesPerBounds;
        int charWidth = codeArea.getCharWidth();
        int positionY = hexRect.y - codeArea.getSubFontSpace() - scrollPosition.scrollLineOffset + codeArea.getLineHeight();

        g.setColor(codeArea.getForeground());
        int headerChars = codeArea.getHeaderCharacters();
        char[] lineNumberCode = new char[headerChars];
        while (positionY <= maxY && dataPosition <= maxDataPosition) {
            CodeAreaUtils.longToHexChars(lineNumberCode, dataPosition, headerChars);
            if (codeArea.isCharFixedMode()) {
                g.drawChars(lineNumberCode, 0, headerChars, compRect.x, positionY);
            } else {
                for (int i = 0; i < headerChars; i++) {
                    drawCenteredChar(g, lineNumberCode, i, charWidth, compRect.x, positionY);
                }
            }
            positionY += lineHeight;
            dataPosition += bytesPerBounds;
        }

        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_LINENUM_HEX_LINE) > 0) {
            g.setColor(Color.GRAY);
            int lineX = hexRect.x - codeArea.getCharWidth() / 2;
            g.drawLine(lineX, compRect.y, lineX, hexRect.y + hexRect.height);
        }
        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
            g.setColor(Color.GRAY);
            g.drawLine(hexRect.x - 1, hexRect.y - 1, hexRect.x - 1, hexRect.y + hexRect.height);
        }
    }

    @Override
    public void paintMainArea(Graphics g) {
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        int charWidth = codeArea.getCharWidth();
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        if (codeArea.getViewMode() != CodeArea.ViewMode.TEXT_PREVIEW && codeArea.getBackgroundMode() == CodeArea.BackgroundMode.GRIDDED) {
            g.setColor(codeArea.getOddBackgroundColor());
            int positionX = hexRect.x - scrollPosition.scrollByteOffset - scrollPosition.scrollBytePosition * charWidth;
            for (int i = 0; i < bytesPerLine / 2; i++) {
                g.fillRect(positionX + charWidth * (3 + i * charsPerByte * 2), hexRect.y, charWidth * 2, hexRect.height);
            }
        }

        int positionY = hexRect.y - scrollPosition.scrollLineOffset;
        long line = scrollPosition.scrollLinePosition;
        int positionX = hexRect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        long dataPosition = line * bytesPerLine;

        Charset charset = codeArea.getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        int charLength = (int) encoder.maxBytesPerChar();
        LineDataCache lineDataCache = new LineDataCache();
        lineDataCache.lineData = new byte[bytesPerLine + charLength - 1];

        do {
            long dataSize = codeArea.getData().getDataSize();
            if (dataPosition < dataSize) {
                int lineDataSize = bytesPerLine + charLength - 1;
                if (dataPosition + lineDataSize > dataSize) {
                    lineDataSize = (int) (dataSize - dataPosition);
                }
                codeArea.getData().copyToArray(dataPosition, lineDataCache.lineData, 0, lineDataSize);
            }

            paintLineBackground(g, line, positionY + lineHeight, dataPosition, bytesPerLine, lineHeight, charWidth);
            paintLineText(g, line, positionX, positionY, dataPosition, bytesPerLine, lineHeight, charset, charWidth, charLength, lineDataCache);
            dataPosition += bytesPerLine;
            line++;
            positionY += lineHeight;
        } while (positionY - lineHeight < hexRect.y + hexRect.height);

        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_HEX_PREVIEW_LINE) > 0) {
            int lineX = codeArea.getPreviewX() - scrollPosition.scrollBytePosition * codeArea.getCharWidth() - scrollPosition.scrollByteOffset - codeArea.getCharWidth() / 2;
            if (lineX >= hexRect.x) {
                g.setColor(Color.GRAY);
                g.drawLine(lineX, hexRect.y, lineX, hexRect.y + hexRect.height);
            }
        }
    }

    public void paintLineBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerBounds, int lineHeight, int charWidth) {
        paintSelectionBackground(g, line, positionY, dataPosition, bytesPerBounds, lineHeight, charWidth);
    }

    public void paintLineText(Graphics g, long line, int linePositionX, int linePositionY, long dataPosition, int bytesPerBounds, int lineHeight, Charset charset, int charWidth, int charLength, LineDataCache lineDataCache) {
        int bytesPerLine = codeArea.getBytesPerLine();
        long dataSize = codeArea.getData().getDataSize();
        g.setColor(codeArea.getForeground());
        for (int byteOnLine = 0; byteOnLine < bytesPerLine; byteOnLine++) {
            if (dataPosition < dataSize || (dataPosition == dataSize && byteOnLine == 0)) {
                paintText(g, line, linePositionX, byteOnLine, linePositionY + lineHeight, dataPosition, bytesPerLine, lineHeight, charset, charWidth, charLength, lineDataCache);
            } else {
                break;
            }

            dataPosition++;
        }
    }

    public void paintText(Graphics g, long line, int linePositionX, int byteOnLine, int linePositionY, long dataPosition, int bytesPerBounds, int lineHeight, Charset charset, int charWidth, int charLength, LineDataCache lineDataCache) {
        if (charShiftsCharset == null || charShiftsCharset != charset) {
            for (int i = 0; i < 16; i++) {
                charShifts[i] = (byte) ((charWidth - g.getFontMetrics().charWidth(hexCharacters[i])) >> 1);
            }
            charShiftsCharset = charset;
        }

        int codeDigits = codeArea.getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;
        BinaryData data = codeArea.getData();
        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        int positionY = linePositionY - codeArea.getSubFontSpace();
        if (dataPosition < data.getDataSize()) {
            byte dataByte = lineDataCache.lineData[byteOnLine];
            if (codeArea.getViewMode() != CodeArea.ViewMode.TEXT_PREVIEW) {
                int startX = linePositionX + byteOnLine * charWidth * charsPerByte;
                byteToCharsCode(dataByte, lineDataCache);
                if (codeArea.isCharFixedMode()) {
                    g.drawChars(lineDataCache.chars, 0, codeDigits, startX, positionY);
                } else {
                    for (int i = 0; i < codeDigits; i++) {
                        drawShiftedChar(g, lineDataCache.chars, i, charWidth, startX, positionY, charShifts[lineDataCache.codeValues[i]]);
                    }
                }
            }

            if (codeArea.getViewMode() != CodeArea.ViewMode.CODE_MATRIX) {
                int startX = codeArea.getPreviewX() + byteOnLine * charWidth;
                startX -= scrollPosition.scrollBytePosition * charWidth + scrollPosition.scrollByteOffset;
                if (charLength > 1) {
                    if (dataPosition + charLength > data.getDataSize()) {
                        charLength = (int) (data.getDataSize() - dataPosition);
                    }

                    int charDataLength = charLength;
                    if (byteOnLine + charDataLength > lineDataCache.lineData.length) {
                        charDataLength = lineDataCache.lineData.length - byteOnLine;
                    }
                    String displayString = new String(lineDataCache.lineData, byteOnLine, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        lineDataCache.previewChar[0] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        for (int i = 0; i < 256; i++) {
                            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
                        }
                        charMappingCharset = charset;
                    }

                    lineDataCache.previewChar[0] = charMapping[dataByte & 0xFF];
                }

                Character replacement = null;
                if (codeArea.isShowNonprintingCharacters()) {
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
                    replacement = nonprintingMapping.get(lineDataCache.previewChar[0]);
                    if (replacement != null) {
                        lineDataCache.previewChar[0] = replacement;
                    }
                }

                if (replacement != null) {
                    g.setColor(codeArea.getWhiteSpaceColor());
                }
                if (codeArea.isCharFixedMode()) {
                    g.drawChars(lineDataCache.previewChar, 0, 1, startX, positionY);
                } else {
                    drawCenteredChar(g, lineDataCache.previewChar, 0, charWidth, startX, positionY);
                }
                if (replacement != null) {
                    g.setColor(codeArea.getForeground());
                }
            }
        }
    }

    public void byteToCharsCode(byte dataByte, LineDataCache lineDataCache) {
        CodeArea.CodeType codeType = codeArea.getCodeType();
        switch (codeType) {
            case BINARY: {
                int bitMask = 1;
                for (int i = 0; i < 8; i++) {
                    lineDataCache.codeValues[i] = (dataByte & bitMask) > 0 ? 1 : 0;
                    lineDataCache.chars[i] = hexCharacters[lineDataCache.codeValues[i]];
                    bitMask = bitMask << 1;
                }
                break;
            }
            case DECIMAL: {
                lineDataCache.codeValues[0] = dataByte / 100;
                lineDataCache.chars[0] = hexCharacters[lineDataCache.codeValues[0]];
                lineDataCache.codeValues[1] = (dataByte / 10) % 10;
                lineDataCache.chars[1] = hexCharacters[lineDataCache.codeValues[1]];
                lineDataCache.codeValues[2] = dataByte % 10;
                lineDataCache.chars[2] = hexCharacters[lineDataCache.codeValues[2]];
                break;
            }
            case OCTAL: {
                lineDataCache.codeValues[0] = dataByte / 64;
                lineDataCache.chars[0] = hexCharacters[lineDataCache.codeValues[0]];
                lineDataCache.codeValues[1] = (dataByte / 8) & 7;
                lineDataCache.chars[1] = hexCharacters[lineDataCache.codeValues[1]];
                lineDataCache.codeValues[2] = dataByte % 8;
                lineDataCache.chars[2] = hexCharacters[lineDataCache.codeValues[2]];
                break;
            }
            case HEXADECIMAL: {
                lineDataCache.codeValues[0] = (dataByte >> 4) & 15;
                lineDataCache.chars[0] = hexCharacters[lineDataCache.codeValues[0]];
                lineDataCache.codeValues[1] = dataByte & 15;
                lineDataCache.chars[1] = hexCharacters[lineDataCache.codeValues[1]];
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type: " + codeType.name());
        }
    }

    public void paintSelectionBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerBounds, int fontHeight, int charWidth) {
        Rectangle hexRect = codeArea.getCodeSectionRectangle();

        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection == null) {
            return;
        }

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        int selectionStart = 0;
        int selectionEnd = 0;
        int selectionPreviewStart = 0;
        int selectionPreviewEnd = 0;
        int startX = hexRect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        int previewStartX = codeArea.getPreviewX() - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        long maxLinePosition = dataPosition + bytesPerBounds - 1;
        long selectionFirst = selection.getFirst();
        long selectionLast = selection.getLast();
        if (selectionFirst <= maxLinePosition) {
            if (selectionFirst >= dataPosition) {
                int linePosition = (int) (selectionFirst - dataPosition);
                selectionStart = startX + charWidth * (linePosition * charsPerByte);
                selectionPreviewStart = previewStartX + charWidth * linePosition;
            } else {
                selectionStart = startX;
                selectionPreviewStart = previewStartX;
            }
        }

        if (selectionLast >= dataPosition && selectionFirst <= maxLinePosition) {
            if (selectionLast >= maxLinePosition) {
                selectionEnd = startX + (bytesPerBounds * charsPerByte - 1) * charWidth;
                selectionPreviewEnd = previewStartX + bytesPerBounds * charWidth;
            } else {
                int linePosition = (int) (selectionLast - dataPosition + 1);
                selectionEnd = startX + charWidth * (linePosition * charsPerByte);
                selectionPreviewEnd = previewStartX + charWidth * linePosition;
            }
        }

        if (selectionEnd > 0) {
            Color hexadecimalColor;
            Color previewColor;
            switch (codeArea.getActiveSection()) {
                case CODE_MATRIX: {
                    hexadecimalColor = codeArea.getSelectionBackgroundColor();
                    previewColor = codeArea.getMirrorSelectionBackgroundColor();
                    break;
                }
                case TEXT_PREVIEW: {
                    hexadecimalColor = codeArea.getMirrorSelectionBackgroundColor();
                    previewColor = codeArea.getSelectionBackgroundColor();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected active section " + codeArea.getActiveSection().name());
                }
            }

            if (codeArea.getViewMode() != CodeArea.ViewMode.TEXT_PREVIEW) {
                g.setColor(hexadecimalColor);
                g.fillRect(selectionStart, positionY - fontHeight, selectionEnd - selectionStart, fontHeight);
            }

            if (codeArea.getViewMode() != CodeArea.ViewMode.CODE_MATRIX) {
                g.setColor(previewColor);
                g.fillRect(selectionPreviewStart, positionY - fontHeight, selectionPreviewEnd - selectionPreviewStart, fontHeight);
            }
        }
    }

    @Override
    public char[] getHexCharacters() {
        return hexCharacters;
    }

    @Override
    public void setHexCharacters(char[] hexCharacters) {
        this.hexCharacters = hexCharacters;
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
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, startX, positionY, (charWidthSpace - charWidth) >> 1);
    }

    protected void drawShiftedChar(Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY, int shift) {
        g.drawChars(drawnChars, charOffset, 1, startX + charWidthSpace * charOffset + shift, positionY);
    }

    /**
     * Line data cache structure.
     *
     * Used for performance improvement.
     */
    public static class LineDataCache {

        /**
         * Characters cache.
         */
        public char[] chars = new char[8];

        /**
         * Code values cache.
         */
        public int[] codeValues = new int[8];
        /**
         * Preview character.
         */
        public char[] previewChar = new char[]{' '};
        /**
         * Line data cache.
         */
        public byte[] lineData;
    }
}
