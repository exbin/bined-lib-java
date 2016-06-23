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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Code area component painter.
 *
 * @version 0.1.0 2016/06/23
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    protected final CodeArea codeArea;

    private Charset charMappingCharset = null;
    protected final char[] charMapping = new char[256];
    private Charset charShiftsCharset = null;
    /**
     * Precomputed centering shifts for basic hexadecimal codes.
     */
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
        if ((decorationMode & CodeArea.DECORATION_LINENUM_LINE) > 0) {
            g.setColor(codeArea.getDecorationLineColor());
            int lineX = hexRect.x - 1 - codeArea.getLineNumberSpace() / 2;
            g.drawLine(lineX, compRect.y, lineX, hexRect.y);
        }
        if ((decorationMode & CodeArea.DECORATION_HEADER_LINE) > 0) {
            g.setColor(codeArea.getDecorationLineColor());
            g.drawLine(compRect.x, hexRect.y - 1, compRect.x + compRect.width, hexRect.y - 1);
        }
        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
            g.setColor(codeArea.getDecorationLineColor());
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
            int bytesPerLine = codeArea.getBytesPerLine();
            int headerX = hexRect.x - scrollPosition.scrollBytePosition * charWidth - scrollPosition.scrollByteOffset;
            int headerY = codeArea.getInsets().top + codeArea.getLineHeight() - codeArea.getSubFontSpace();

            if (codeArea.getBackgroundMode() == CodeArea.BackgroundMode.GRIDDED) {
                CodeArea.ColorsGroup stripColors = codeArea.getStripColors();
                g.setColor(stripColors.getBackgroundColor());
                int positionX = hexRect.x - scrollPosition.scrollByteOffset - scrollPosition.scrollBytePosition * charWidth;
                for (int i = 0; i < bytesPerLine / 2; i++) {
                    g.fillRect(positionX + charWidth * (charsPerByte + i * charsPerByte * 2), hexRect.y, charWidth * codeDigits, hexRect.height);
                }
            }

            g.setColor(codeArea.getForeground());
            char[] chars = new char[2];
            boolean upperCase = codeArea.getHexCharactersCase() == CodeArea.HexCharactersCase.UPPER;
            if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                for (int i = 0; i < bytesPerLine; i++) {
                    CodeAreaUtils.longToBaseCode(chars, i, codeArea.getPositionCodeType().base, 2, true, upperCase);
                    g.drawChars(chars, 0, 2, headerX + i * charWidth * charsPerByte, headerY);
                }
            } else {
                for (int i = 0; i < bytesPerLine; i++) {
                    CodeAreaUtils.longToBaseCode(chars, i, codeArea.getPositionCodeType().base, 2, true, upperCase);
                    int startX = headerX + i * charWidth * charsPerByte;
                    drawCenteredChar(g, chars, 0, charWidth, startX, headerY);
                    drawCenteredChar(g, chars, 1, charWidth, startX + charWidth, headerY);
                }
            }
        }

        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) {
            int lineX = codeArea.getPreviewX() - scrollPosition.scrollBytePosition * codeArea.getCharWidth() - scrollPosition.scrollByteOffset - codeArea.getCharWidth() / 2;
            if (lineX >= hexRect.x) {
                g.setColor(codeArea.getDecorationLineColor());
                g.drawLine(lineX, compRect.y, lineX, hexRect.y);
            }
        }
    }

    @Override
    public void paintBackground(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        if (codeArea.getBackgroundMode() != CodeArea.BackgroundMode.NONE) {
            CodeArea.ColorsGroup mainColors = codeArea.getMainColors();
            g.setColor(mainColors.getBackgroundColor());
            g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        }

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long line = scrollPosition.scrollLinePosition;
        long maxDataPosition = codeArea.getData().getDataSize();
        int maxY = clipBounds.y + clipBounds.height;

        int positionY;
        long dataPosition = line * bytesPerLine;
        if (codeArea.getBackgroundMode() != CodeArea.BackgroundMode.PLAIN) {
            g.setColor(codeArea.getBackground());

            positionY = hexRect.y - scrollPosition.scrollLineOffset;
            if ((line & 1) == 0) {
                positionY += lineHeight;
                dataPosition += bytesPerLine;
            }
            while (positionY <= maxY && dataPosition < maxDataPosition) {
                g.fillRect(0, positionY, hexRect.x + hexRect.width, lineHeight);
                positionY += lineHeight * 2;
                dataPosition += bytesPerLine * 2;
            }
        }
    }

    @Override
    public void paintLineNumbers(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long line = scrollPosition.scrollLinePosition;
        long maxDataPosition = codeArea.getData().getDataSize();
        int maxY = clipBounds.y + clipBounds.height + lineHeight;
        long dataPosition = line * bytesPerLine;
        int charWidth = codeArea.getCharWidth();
        int positionY = hexRect.y - codeArea.getSubFontSpace() - scrollPosition.scrollLineOffset + codeArea.getLineHeight();

        g.setColor(codeArea.getForeground());
        int lineNumberLength = codeArea.getLineNumberLength();
        char[] lineNumberCode = new char[lineNumberLength];
        boolean upperCase = codeArea.getHexCharactersCase() == CodeArea.HexCharactersCase.UPPER;
        while (positionY <= maxY && dataPosition <= maxDataPosition) {
            CodeAreaUtils.longToBaseCode(lineNumberCode, dataPosition, codeArea.getPositionCodeType().base, lineNumberLength, true, upperCase);
            if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
            } else {
                for (int i = 0; i < lineNumberLength; i++) {
                    drawCenteredChar(g, lineNumberCode, i, charWidth, compRect.x + charWidth * i, positionY);
                }
            }
            positionY += lineHeight;
            dataPosition += bytesPerLine;
        }

        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_LINENUM_LINE) > 0) {
            g.setColor(codeArea.getDecorationLineColor());
            int lineX = hexRect.x - 1 - codeArea.getLineNumberSpace() / 2;
            g.drawLine(lineX, compRect.y, lineX, hexRect.y + hexRect.height);
        }
        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
            g.setColor(codeArea.getDecorationLineColor());
            g.drawLine(hexRect.x - 1, hexRect.y - 1, hexRect.x - 1, hexRect.y + hexRect.height);
        }
    }

    @Override
    public void paintMainArea(Graphics g) {
        PaintData paintData = createPaintData();
        paintMainArea(g, paintData);
    }

    public void paintMainArea(Graphics g, PaintData paintData) {
        if (paintData.viewMode != CodeArea.ViewMode.TEXT_PREVIEW && codeArea.getBackgroundMode() == CodeArea.BackgroundMode.GRIDDED) {
            g.setColor(paintData.stripColors.getBackgroundColor());
            int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.scrollByteOffset - paintData.scrollPosition.scrollBytePosition * paintData.charWidth;
            for (int i = 0; i < paintData.bytesPerLine / 2; i++) {
                g.fillRect(positionX + paintData.charWidth * (paintData.charsPerByte + i * paintData.charsPerByte * 2), paintData.codeSectionRect.y, paintData.charWidth * paintData.codeDigits, paintData.codeSectionRect.height);
            }
        }

        FontMetrics fontMetrics = codeArea.getFontMetrics();
        if (charShiftsCharset == null || charShiftsCharset != paintData.charset) {
            for (int i = 0; i < 16; i++) {
                charShifts[i] = (byte) ((paintData.charWidth - fontMetrics.charWidth(hexCharacters[i])) >> 1);
            }
            charShiftsCharset = paintData.charset;
        }

        int positionY = paintData.codeSectionRect.y - paintData.scrollPosition.scrollLineOffset;
        long line = paintData.scrollPosition.scrollLinePosition;
        int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.scrollBytePosition * paintData.charWidth - paintData.scrollPosition.scrollByteOffset;
        paintData.lineDataPosition = line * paintData.bytesPerLine;

        do {
            if (paintData.showNonprintingCharacters) {
                Arrays.fill(paintData.nonprintableChars, ' ');
            }
            long dataSize = codeArea.getData().getDataSize();
            int lineBytesLimit = paintData.bytesPerLine;
            if (paintData.lineDataPosition < dataSize) {
                int lineDataSize = paintData.bytesPerLine + paintData.maxCharLength - 1;
                if (paintData.lineDataPosition + lineDataSize > dataSize) {
                    lineDataSize = (int) (dataSize - paintData.lineDataPosition);
                }
                codeArea.getData().copyToArray(paintData.lineDataPosition, paintData.lineData, 0, lineDataSize);
                if (paintData.lineDataPosition + lineBytesLimit > dataSize) {
                    lineBytesLimit = (int) (dataSize - paintData.lineDataPosition);
                }
            } else {
                lineBytesLimit = 0;
            }

            // Fill codes
            if (paintData.viewMode != CodeArea.ViewMode.TEXT_PREVIEW) {
                for (int byteOnLine = 0; byteOnLine < lineBytesLimit; byteOnLine++) {
                    byte dataByte = paintData.lineData[byteOnLine];
                    byteToCharsCode(dataByte, byteOnLine * paintData.charsPerByte, paintData);
                }
                if (paintData.bytesPerLine > lineBytesLimit) {
                    Arrays.fill(paintData.lineChars, lineBytesLimit * paintData.charsPerByte, paintData.bytesPerLine * paintData.charsPerByte, ' ');
                }
            }

            // Fill preview characters
            if (paintData.viewMode != CodeArea.ViewMode.CODE_MATRIX) {
                int previewCharPos = 0;
                if (paintData.viewMode == CodeArea.ViewMode.DUAL) {
                    previewCharPos = paintData.bytesPerLine * paintData.charsPerByte;
                }
                for (int byteOnLine = 0; byteOnLine < lineBytesLimit; byteOnLine++) {
                    byte dataByte = paintData.lineData[byteOnLine];

                    if (paintData.maxCharLength > 1) {
                        if (paintData.lineDataPosition + paintData.maxCharLength > dataSize) {
                            paintData.maxCharLength = (int) (dataSize - paintData.lineDataPosition);
                        }

                        int charDataLength = paintData.maxCharLength;
                        if (byteOnLine + charDataLength > paintData.lineData.length) {
                            charDataLength = paintData.lineData.length - byteOnLine;
                        }
                        String displayString = new String(paintData.lineData, byteOnLine, charDataLength, paintData.charset);
                        if (!displayString.isEmpty()) {
                            paintData.lineChars[previewCharPos + byteOnLine] = displayString.charAt(0);
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != paintData.charset) {
                            for (int i = 0; i < 256; i++) {
                                charMapping[i] = new String(new byte[]{(byte) i}, paintData.charset).charAt(0);
                            }
                            charMappingCharset = paintData.charset;
                        }

                        paintData.lineChars[previewCharPos + byteOnLine] = charMapping[dataByte & 0xFF];
                    }

                    if (paintData.showNonprintingCharacters || paintData.charRenderingMode == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
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
                        Character replacement = nonprintingMapping.get(paintData.lineChars[previewCharPos + byteOnLine]);
                        if (replacement != null) {
                            if (paintData.showNonprintingCharacters) {
                                paintData.nonprintableChars[previewCharPos + byteOnLine] = replacement;
                            }
                            paintData.lineChars[previewCharPos + byteOnLine] = ' ';
                        }
                    }
                }
                if (paintData.bytesPerLine > lineBytesLimit) {
                    Arrays.fill(paintData.lineChars, previewCharPos + lineBytesLimit, previewCharPos + paintData.bytesPerLine, ' ');
                }
            }
            paintLineBackground(g, line, positionY + paintData.lineHeight, paintData);
            paintLineText(g, positionX, positionY, paintData);
            if (paintData.showNonprintingCharacters) {
                paintLineNonprintables(g, positionX, positionY, paintData);
            }
            paintData.lineDataPosition += paintData.bytesPerLine;
            line++;
            positionY += paintData.lineHeight;
        } while (positionY - paintData.lineHeight < paintData.codeSectionRect.y + paintData.codeSectionRect.height);

        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) {
            int lineX = codeArea.getPreviewX() - paintData.scrollPosition.scrollBytePosition * codeArea.getCharWidth() - paintData.scrollPosition.scrollByteOffset - codeArea.getCharWidth() / 2;
            if (lineX >= paintData.codeSectionRect.x) {
                g.setColor(codeArea.getDecorationLineColor());
                g.drawLine(lineX, paintData.codeSectionRect.y, lineX, paintData.codeSectionRect.y + paintData.codeSectionRect.height);
            }
        }
    }

    public void paintLineBackground(Graphics g, long line, int positionY, PaintData paintData) {
        paintSelectionBackground(g, line, positionY, paintData);
    }

    public void paintLineText(Graphics g, int linePositionX, int linePositionY, PaintData paintData) {
        g.setColor(paintData.mainColors.getTextColor());
        int positionY = linePositionY + paintData.lineHeight - codeArea.getSubFontSpace();

        if (paintData.charRenderingMode == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
            g.drawChars(paintData.lineChars, 0, paintData.charsPerLine, linePositionX, positionY);
        } else {
            int renderOffset = 0;
            for (int charOnLine = 0; charOnLine < paintData.charsPerLine; charOnLine++) {
                boolean sequenceBreak = false;
                boolean nativeWidth = true;

                char currentChar = paintData.lineChars[charOnLine];
                if (currentChar == ' ' && renderOffset == charOnLine) {
                    renderOffset++;
                    continue;
                }
                int currentCharWidth = 0;
                if (paintData.charRenderingMode == CodeArea.CharRenderingMode.AUTO && paintData.monospaceFont) {
                    // Detect if character is in unicode range covered by monospace fonts
                    if ((int) currentChar < 0x24f) {
                        currentCharWidth = paintData.charWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = paintData.fontMetrics.charWidth(currentChar);
                    nativeWidth = currentCharWidth == paintData.charWidth;
                }

                if (!nativeWidth) {
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < charOnLine) {
                        renderCharSequence(g, renderOffset, charOnLine, linePositionX, positionY, paintData);
                    }

                    if (!nativeWidth) {
                        renderOffset = charOnLine + 1;
                        if (paintData.charRenderingMode == CodeArea.CharRenderingMode.TOP_LEFT) {
                            g.drawChars(paintData.lineChars, charOnLine, 1, linePositionX + charOnLine * paintData.charWidth, positionY);
                        } else {
                            drawShiftedChar(g, paintData.lineChars, charOnLine, paintData.charWidth, linePositionX + charOnLine * paintData.charWidth, positionY, (paintData.charWidth + 1 - currentCharWidth) >> 1);
                        }
                    } else {
                        renderOffset = charOnLine;
                    }
                }
            }

            if (renderOffset < paintData.charsPerLine) {
                renderCharSequence(g, renderOffset, paintData.charsPerLine, linePositionX, positionY, paintData);
            }
        }
    }

    /**
     * Doesn't include character at offset end.
     */
    private void renderCharSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY, PaintData paintData) {
        g.drawChars(paintData.lineChars, startOffset, endOffset - startOffset, linePositionX + startOffset * paintData.charWidth, positionY);
    }

    public void paintLineNonprintables(Graphics g, int linePositionX, int linePositionY, PaintData paintData) {
        g.setColor(paintData.mainColors.getNonprintableColor());
        int positionY = linePositionY + paintData.lineHeight - codeArea.getSubFontSpace();

        if (paintData.charRenderingMode == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
            g.drawChars(paintData.nonprintableChars, 0, paintData.charsPerLine, linePositionX, positionY);
        } else {
            int renderOffset = 0;
            for (int charOnLine = 0; charOnLine < paintData.charsPerLine; charOnLine++) {
                boolean sequenceBreak = false;
                boolean nativeWidth = true;

                char currentChar = paintData.nonprintableChars[charOnLine];
                if (currentChar == ' ' && renderOffset == charOnLine) {
                    renderOffset++;
                    continue;
                }
                int currentCharWidth = 0;
                if (paintData.charRenderingMode == CodeArea.CharRenderingMode.AUTO && paintData.monospaceFont) {
                    // Detect if character is in unicode range covered by monospace fonts
                    if ((int) currentChar < 0x24f) {
                        currentCharWidth = paintData.charWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = paintData.fontMetrics.charWidth(currentChar);
                    nativeWidth = currentCharWidth == paintData.charWidth;
                }

                if (!nativeWidth) {
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < charOnLine) {
                        renderNonprintableCharSequence(g, renderOffset, charOnLine, linePositionX, positionY, paintData);
                    }

                    if (!nativeWidth) {
                        renderOffset = charOnLine + 1;
                        if (paintData.charRenderingMode == CodeArea.CharRenderingMode.TOP_LEFT) {
                            g.drawChars(paintData.nonprintableChars, charOnLine, 1, linePositionX + charOnLine * paintData.charWidth, positionY);
                        } else {
                            drawShiftedChar(g, paintData.nonprintableChars, charOnLine, paintData.charWidth, linePositionX + charOnLine * paintData.charWidth, positionY, (paintData.charWidth + 1 - currentCharWidth) >> 1);
                        }
                    } else {
                        renderOffset = charOnLine;
                    }
                }
            }

            if (renderOffset < paintData.charsPerLine) {
                renderNonprintableCharSequence(g, renderOffset, paintData.charsPerLine, linePositionX, positionY, paintData);
            }
        }
    }

    /**
     * Doesn't include character at offset end.
     */
    private void renderNonprintableCharSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY, PaintData paintData) {
        g.drawChars(paintData.nonprintableChars, startOffset, endOffset - startOffset, linePositionX + startOffset * paintData.charWidth, positionY);
    }

    public void paintSelectionBackground(Graphics g, long line, int positionY, PaintData paintData) {
        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection == null) {
            return;
        }

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        int selectionStart = 0;
        int selectionEnd = 0;
        int selectionPreviewStart = 0;
        int selectionPreviewEnd = 0;
        int startX = paintData.codeSectionRect.x - scrollPosition.scrollBytePosition * paintData.charWidth - scrollPosition.scrollByteOffset;
        int previewStartX = codeArea.getPreviewX() - scrollPosition.scrollBytePosition * paintData.charWidth - scrollPosition.scrollByteOffset;
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        long maxLinePosition = paintData.lineDataPosition + paintData.bytesPerLine - 1;
        long selectionFirst = selection.getFirst();
        long selectionLast = selection.getLast();
        if (selectionFirst <= maxLinePosition) {
            if (selectionFirst >= paintData.lineDataPosition) {
                int linePosition = (int) (selectionFirst - paintData.lineDataPosition);
                selectionStart = startX + paintData.charWidth * (linePosition * charsPerByte);
                selectionPreviewStart = previewStartX + paintData.charWidth * linePosition;
            } else {
                selectionStart = startX;
                selectionPreviewStart = previewStartX;
            }
        }

        if (selectionLast >= paintData.lineDataPosition && selectionFirst <= maxLinePosition) {
            if (selectionLast >= maxLinePosition) {
                selectionEnd = startX + (paintData.bytesPerLine * charsPerByte - 1) * paintData.charWidth;
                selectionPreviewEnd = previewStartX + paintData.bytesPerLine * paintData.charWidth;
            } else {
                int linePosition = (int) (selectionLast - paintData.lineDataPosition + 1);
                selectionEnd = startX + paintData.charWidth * (linePosition * charsPerByte);
                selectionPreviewEnd = previewStartX + paintData.charWidth * linePosition;
            }
        }

        CodeArea.ColorsGroup selectionColors = codeArea.getSelectionColors();
        CodeArea.ColorsGroup mirrorSelectionColors = codeArea.getMirrorSelectionColors();
        if (selectionEnd > 0) {
            Color codeColor;
            Color previewColor;
            switch (codeArea.getActiveSection()) {
                case CODE_MATRIX: {
                    codeColor = selectionColors.getBackgroundColor();
                    previewColor = mirrorSelectionColors.getBackgroundColor();
                    break;
                }
                case TEXT_PREVIEW: {
                    codeColor = mirrorSelectionColors.getBackgroundColor();
                    previewColor = selectionColors.getBackgroundColor();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected active section " + codeArea.getActiveSection().name());
                }
            }

            if (paintData.viewMode != CodeArea.ViewMode.TEXT_PREVIEW) {
                g.setColor(codeColor);
                g.fillRect(selectionStart, positionY - paintData.lineHeight, selectionEnd - selectionStart, paintData.lineHeight);
            }

            if (paintData.viewMode != CodeArea.ViewMode.CODE_MATRIX) {
                g.setColor(previewColor);
                g.fillRect(selectionPreviewStart, positionY - paintData.lineHeight, selectionPreviewEnd - selectionPreviewStart, paintData.lineHeight);
            }
        }
    }

    public void byteToCharsCode(byte dataByte, int targetPosition, PaintData lineDataCache) {
        CodeArea.CodeType codeType = codeArea.getCodeType();
        switch (codeType) {
            case BINARY: {
                int bitMask = 0x80;
                for (int i = 0; i < 8; i++) {
                    int codeValue = (dataByte & bitMask) > 0 ? 1 : 0;
                    lineDataCache.lineChars[targetPosition + i] = hexCharacters[codeValue];
                    bitMask = bitMask >> 1;
                }
                break;
            }
            case DECIMAL: {
                int value = dataByte & 0xff;
                int codeValue0 = value / 100;
                lineDataCache.lineChars[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = (value / 10) % 10;
                lineDataCache.lineChars[targetPosition + 1] = hexCharacters[codeValue1];
                int codeValue2 = value % 10;
                lineDataCache.lineChars[targetPosition + 2] = hexCharacters[codeValue2];
                break;
            }
            case OCTAL: {
                int value = dataByte & 0xff;
                int codeValue0 = value / 64;
                lineDataCache.lineChars[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = (value / 8) & 7;
                lineDataCache.lineChars[targetPosition + 1] = hexCharacters[codeValue1];
                int codeValue2 = value % 8;
                lineDataCache.lineChars[targetPosition + 2] = hexCharacters[codeValue2];
                break;
            }
            case HEXADECIMAL: {
                int codeValue0 = (dataByte >> 4) & 15;
                lineDataCache.lineChars[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = dataByte & 15;
                lineDataCache.lineChars[targetPosition + 1] = hexCharacters[codeValue1];
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type: " + codeType.name());
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
        FontMetrics fontMetrics = codeArea.getFontMetrics();
        int charWidth = fontMetrics.charWidth(drawnChars[charOffset]);
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, startX, positionY, (charWidthSpace - charWidth) >> 1);
    }

    protected void drawShiftedChar(Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY, int shift) {
        g.drawChars(drawnChars, charOffset, 1, startX + shift, positionY);
    }

    private PaintData createPaintData() {
        PaintData paintData = new PaintData();
        paintData.viewMode = codeArea.getViewMode();
        paintData.codeSectionRect = codeArea.getCodeSectionRectangle();
        paintData.scrollPosition = codeArea.getScrollPosition();
        paintData.charWidth = codeArea.getCharWidth();
        paintData.bytesPerLine = codeArea.getBytesPerLine();
        paintData.lineHeight = codeArea.getLineHeight();
        paintData.codeDigits = codeArea.getCodeType().getMaxDigits();
        paintData.charsPerByte = paintData.codeDigits + 1;
        paintData.charset = codeArea.getCharset();
        paintData.mainColors = codeArea.getMainColors();
        paintData.stripColors = codeArea.getStripColors();
        paintData.charRenderingMode = codeArea.getCharRenderingMode();
        paintData.fontMetrics = codeArea.getFontMetrics();
        paintData.monospaceFont = codeArea.isMonospaceFontDetected();

        CharsetEncoder encoder = paintData.charset.newEncoder();
        paintData.maxCharLength = (int) encoder.maxBytesPerChar();
        paintData.lineData = new byte[paintData.bytesPerLine + paintData.maxCharLength - 1];
        paintData.charsPerLine = paintData.bytesPerLine * (paintData.charsPerByte + 1) + 1;
        paintData.lineChars = new char[paintData.charsPerLine];
        Arrays.fill(paintData.lineChars, ' ');

        paintData.showNonprintingCharacters = codeArea.isShowNonprintingCharacters();
        if (paintData.showNonprintingCharacters) {
            paintData.nonprintableChars = new char[paintData.charsPerLine];
        }

        return paintData;
    }

    /**
     * Paint data structure for single paint operation.
     */
    protected static class PaintData {

        protected CodeArea.ViewMode viewMode;
        protected Rectangle codeSectionRect;
        protected CodeArea.ScrollPosition scrollPosition;
        protected int charWidth;
        protected int bytesPerLine;
        protected int lineHeight;
        protected int codeDigits;
        protected int charsPerByte;
        protected int charsPerLine;
        protected Charset charset;
        protected int maxCharLength;
        protected boolean showNonprintingCharacters;
        protected CodeArea.CharRenderingMode charRenderingMode;
        protected FontMetrics fontMetrics;
        protected boolean monospaceFont;

        protected CodeArea.ColorsGroup mainColors;
        protected CodeArea.ColorsGroup stripColors;

        // Line related fields
        protected long lineDataPosition;

        /**
         * Line data cache.
         */
        protected byte[] lineData;

        /**
         * Single line of characters.
         */
        protected char[] lineChars;

        /**
         * Single line of nonprintable characters.
         */
        protected char[] nonprintableChars;

        public CodeArea.ViewMode getViewMode() {
            return viewMode;
        }

        public Rectangle getCodeSectionRect() {
            return codeSectionRect;
        }

        public CodeArea.ScrollPosition getScrollPosition() {
            return scrollPosition;
        }

        public CodeArea.CharRenderingMode getCharRenderingMode() {
            return charRenderingMode;
        }

        public int getCharWidth() {
            return charWidth;
        }

        public int getBytesPerLine() {
            return bytesPerLine;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public int getCodeDigits() {
            return codeDigits;
        }

        public int getCharsPerByte() {
            return charsPerByte;
        }

        public int getCharsPerLine() {
            return charsPerLine;
        }

        public Charset getCharset() {
            return charset;
        }

        public int getMaxCharLength() {
            return maxCharLength;
        }

        public boolean isShowNonprintingCharacters() {
            return showNonprintingCharacters;
        }

        public CodeArea.ColorsGroup getMainColors() {
            return mainColors;
        }

        public CodeArea.ColorsGroup getStripColors() {
            return stripColors;
        }

        public long getLineDataPosition() {
            return lineDataPosition;
        }
    }
}
