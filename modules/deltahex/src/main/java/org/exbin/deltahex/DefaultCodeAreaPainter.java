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
 * Code area component default painter.
 *
 * @version 0.1.0 2016/06/24
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    public static final int MIN_MONOSPACE_CODE_POINT = 0x19;
    public static final int MAX_MONOSPACE_CODE_POINT = 0x1C3;
    public static final int INV_SPACE_CODE_POINT = 0x7f;
    public static final int EXCEPTION1_CODE_POINT = 0x8e;
    public static final int EXCEPTION2_CODE_POINT = 0x9e;

    protected final CodeArea codeArea;

    private Charset charMappingCharset = null;
    protected final char[] charMapping = new char[256];
    private char[] hexCharacters = CodeAreaUtils.UPPER_HEX_CODES;
    protected Map<Character, Character> unprintableCharactersMapping = null;

    public DefaultCodeAreaPainter(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void paintOverall(Graphics g) {
        // Fill header area background
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle hexRect = codeArea.getCodeSectionRectangle();
        if (compRect.y < hexRect.y) {
            g.setColor(codeArea.getBackground());
            g.fillRect(compRect.x, compRect.y, compRect.x + compRect.width, hexRect.y - compRect.y);
        }

        // Draw decoration lines
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
                CodeArea.ColorsGroup stripColors = codeArea.getAlternateColors();
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
        CodeArea.ColorsGroup mainColors = codeArea.getMainColors();
        CodeArea.ColorsGroup stripColors = codeArea.getAlternateColors();
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        if (codeArea.getBackgroundMode() != CodeArea.BackgroundMode.NONE) {
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
            g.setColor(stripColors.getBackgroundColor());

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

        // Draw decoration lines
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
            g.setColor(paintData.alternateColors.getBackgroundColor());
            int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.scrollByteOffset - paintData.scrollPosition.scrollBytePosition * paintData.charWidth;
            for (int i = 0; i < paintData.bytesPerLine / 2; i++) {
                g.fillRect(positionX + paintData.charWidth * (paintData.charsPerByte + i * paintData.charsPerByte * 2), paintData.codeSectionRect.y, paintData.charWidth * paintData.codeDigits, paintData.codeSectionRect.height);
            }
        }

        int positionY = paintData.codeSectionRect.y - paintData.scrollPosition.scrollLineOffset;
        long line = paintData.scrollPosition.scrollLinePosition;
        int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.scrollBytePosition * paintData.charWidth - paintData.scrollPosition.scrollByteOffset;
        paintData.lineDataPosition = line * paintData.bytesPerLine;

        do {
            if (paintData.showUnprintableCharacters) {
                Arrays.fill(paintData.unprintableChars, ' ');
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
                paintData.previewCharPos = 0;
                if (paintData.viewMode == CodeArea.ViewMode.DUAL) {
                    paintData.previewCharPos = paintData.bytesPerLine * paintData.charsPerByte;
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
                            paintData.lineChars[paintData.previewCharPos + byteOnLine] = displayString.charAt(0);
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != paintData.charset) {
                            for (int i = 0; i < 256; i++) {
                                charMapping[i] = new String(new byte[]{(byte) i}, paintData.charset).charAt(0);
                            }
                            charMappingCharset = paintData.charset;
                        }

                        paintData.lineChars[paintData.previewCharPos + byteOnLine] = charMapping[dataByte & 0xFF];
                    }

                    if (paintData.showUnprintableCharacters || paintData.charRenderingMode == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                        if (unprintableCharactersMapping == null) {
                            unprintableCharactersMapping = new HashMap<>();
                            // Unicode control characters, might not be supported by font
                            for (int i = 0; i < 32; i++) {
                                unprintableCharactersMapping.put((char) i, Character.toChars(9216 + i)[0]);
                            }
                            // Space -> Middle Dot
                            unprintableCharactersMapping.put(' ', Character.toChars(183)[0]);
                            // Tab -> Right-Pointing Double Angle Quotation Mark
                            unprintableCharactersMapping.put('\t', Character.toChars(187)[0]);
                            // Line Feed -> Currency Sign
                            unprintableCharactersMapping.put('\r', Character.toChars(164)[0]);
                            // Carriage Return -> Pilcrow Sign
                            unprintableCharactersMapping.put('\n', Character.toChars(182)[0]);
                            // Ideographic Space -> Degree Sign
                            unprintableCharactersMapping.put(Character.toChars(127)[0], Character.toChars(176)[0]);
                        }
                        Character replacement = unprintableCharactersMapping.get(paintData.lineChars[paintData.previewCharPos + byteOnLine]);
                        if (replacement != null) {
                            if (paintData.showUnprintableCharacters) {
                                paintData.unprintableChars[paintData.previewCharPos + byteOnLine] = replacement;
                            }
                            paintData.lineChars[paintData.previewCharPos + byteOnLine] = ' ';
                        }
                    }
                }
                if (paintData.bytesPerLine > lineBytesLimit) {
                    Arrays.fill(paintData.lineChars, paintData.previewCharPos + lineBytesLimit, paintData.previewCharPos + paintData.bytesPerLine, ' ');
                }
            }
            paintLineBackground(g, line, positionX, positionY, paintData);
            paintLineText(g, line, positionX, positionY, paintData);
            paintData.lineDataPosition += paintData.bytesPerLine;
            line++;
            positionY += paintData.lineHeight;
        } while (positionY - paintData.lineHeight < paintData.codeSectionRect.y + paintData.codeSectionRect.height);

        // Draw decoration lines
        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) {
            int lineX = codeArea.getPreviewX() - paintData.scrollPosition.scrollBytePosition * codeArea.getCharWidth() - paintData.scrollPosition.scrollByteOffset - codeArea.getCharWidth() / 2;
            if (lineX >= paintData.codeSectionRect.x) {
                g.setColor(codeArea.getDecorationLineColor());
                g.drawLine(lineX, paintData.codeSectionRect.y, lineX, paintData.codeSectionRect.y + paintData.codeSectionRect.height);
            }
        }
    }

    public void paintLineBackground(Graphics g, long line, int linePositionX, int linePositionY, PaintData paintData) {
        int renderOffset = 0;
        CodeArea.ColorType renderColorType = null;
        Color renderColor = null;
        for (int charOnLine = 0; charOnLine < paintData.charsPerLine; charOnLine++) {
            CodeArea.Section section;
            int byteOnLine;
            if (charOnLine >= paintData.previewCharPos) {
                byteOnLine = charOnLine - paintData.previewCharPos;
                section = CodeArea.Section.TEXT_PREVIEW;
            } else {
                byteOnLine = charOnLine / paintData.charsPerByte;
                section = CodeArea.Section.CODE_MATRIX;
            }
            boolean sequenceBreak = false;

            CodeArea.ColorType colorType = CodeArea.ColorType.BACKGROUND;
            if (paintData.showUnprintableCharacters) {
                if (paintData.unprintableChars[charOnLine] != ' ') {
                    colorType = CodeArea.ColorType.UNPRINTABLES_BACKGROUND;
                }
            }

            Color color = getPositionColor(paintData.lineDataPosition, line, byteOnLine, charOnLine, section, colorType, paintData);
            if (renderColor == null) {
                renderColor = color;
                renderColorType = colorType;
                g.setColor(color);
            }

            if (!color.equals(renderColor) || !colorType.equals(renderColorType)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnLine) {
                    renderBackgroundSequence(g, renderOffset, charOnLine, linePositionX, linePositionY, paintData);
                }

                if (!colorType.equals(renderColorType)) {
                    renderColorType = colorType;
                }
                if (!color.equals(renderColor)) {
                    renderColor = color;
                    g.setColor(color);
                }

                renderOffset = charOnLine;
            }
        }

        if (renderOffset < paintData.charsPerLine) {
            renderBackgroundSequence(g, renderOffset, paintData.charsPerLine, linePositionX, linePositionY, paintData);
        }
    }

    public void paintLineText(Graphics g, long line, int linePositionX, int linePositionY, PaintData paintData) {
        int positionY = linePositionY + paintData.lineHeight - codeArea.getSubFontSpace();

        int renderOffset = 0;
        CodeArea.ColorType renderColorType = null;
        Color renderColor = null;
        for (int charOnLine = 0; charOnLine < paintData.charsPerLine; charOnLine++) {
            CodeArea.Section section;
            int byteOnLine;
            if (charOnLine >= paintData.previewCharPos) {
                byteOnLine = charOnLine - paintData.previewCharPos;
                section = CodeArea.Section.TEXT_PREVIEW;
            } else {
                byteOnLine = charOnLine / paintData.charsPerByte;
                section = CodeArea.Section.CODE_MATRIX;
            }
            boolean sequenceBreak = false;
            boolean nativeWidth = true;

            int currentCharWidth = 0;
            CodeArea.ColorType colorType = CodeArea.ColorType.TEXT;
            if (paintData.charRenderingMode != CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                char currentChar = ' ';
                if (paintData.showUnprintableCharacters) {
                    currentChar = paintData.unprintableChars[charOnLine];
                    if (currentChar != ' ') {
                        colorType = CodeArea.ColorType.UNPRINTABLES;
                    }
                }
                if (colorType == CodeArea.ColorType.TEXT) {
                    currentChar = paintData.lineChars[charOnLine];
                }
                if (currentChar == ' ' && renderOffset == charOnLine) {
                    renderOffset++;
                    continue;
                }
                if (paintData.charRenderingMode == CodeArea.CharRenderingMode.AUTO && paintData.monospaceFont) {
                    // Detect if character is in unicode range covered by monospace fonts
                    if (currentChar > MIN_MONOSPACE_CODE_POINT && (int) currentChar < MAX_MONOSPACE_CODE_POINT
                            && currentChar != INV_SPACE_CODE_POINT
                            && currentChar != EXCEPTION1_CODE_POINT && currentChar != EXCEPTION2_CODE_POINT) {
                        currentCharWidth = paintData.charWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = paintData.fontMetrics.charWidth(currentChar);
                    nativeWidth = currentCharWidth == paintData.charWidth;
                }
            } else {
                currentCharWidth = paintData.charWidth;
                if (paintData.showUnprintableCharacters) {
                    char currentChar = paintData.unprintableChars[charOnLine];
                    if (currentChar != ' ') {
                        colorType = CodeArea.ColorType.UNPRINTABLES;
                        currentCharWidth = paintData.fontMetrics.charWidth(currentChar);
                        nativeWidth = currentCharWidth == paintData.charWidth;
                    }
                }
            }

            Color color = getPositionColor(paintData.lineDataPosition, line, byteOnLine, charOnLine, section, colorType, paintData);
            if (renderColor == null) {
                renderColor = color;
                renderColorType = colorType;
                g.setColor(color);
            }

            if (!nativeWidth || !color.equals(renderColor) || !colorType.equals(renderColorType)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnLine) {
                    renderCharSequence(g, renderOffset, charOnLine, linePositionX, positionY, renderColorType, paintData);
                }

                if (!colorType.equals(renderColorType)) {
                    renderColorType = colorType;
                }
                if (!color.equals(renderColor)) {
                    renderColor = color;
                    g.setColor(color);
                }

                if (!nativeWidth) {
                    renderOffset = charOnLine + 1;
                    if (paintData.charRenderingMode == CodeArea.CharRenderingMode.TOP_LEFT) {
                        g.drawChars(
                                renderColorType == CodeArea.ColorType.UNPRINTABLES ? paintData.unprintableChars : paintData.lineChars,
                                charOnLine, 1, linePositionX + charOnLine * paintData.charWidth, positionY);
                    } else {
                        drawShiftedChar(g,
                                renderColorType == CodeArea.ColorType.UNPRINTABLES ? paintData.unprintableChars : paintData.lineChars,
                                charOnLine, paintData.charWidth, linePositionX + charOnLine * paintData.charWidth, positionY, (paintData.charWidth + 1 - currentCharWidth) >> 1);
                    }
                } else {
                    renderOffset = charOnLine;
                }
            }
        }

        if (renderOffset < paintData.charsPerLine) {
            renderCharSequence(g, renderOffset, paintData.charsPerLine, linePositionX, positionY, renderColorType, paintData);
        }
    }

    /**
     * Returns color of given type for specified position.
     *
     * Child implementation can override this to change rendering colors.
     *
     * @param lineDataPosition line data position
     * @param line line number
     * @param byteOnLine byte on line
     * @param charOnLine character on line
     * @param section rendering section
     * @param colorType color type
     * @param paintData cached paint data
     * @return color
     */
    public Color getPositionColor(long lineDataPosition, long line, int byteOnLine, int charOnLine, CodeArea.Section section, CodeArea.ColorType colorType, PaintData paintData) {
        long dataPosition = lineDataPosition + byteOnLine;
        CodeArea.SelectionRange selection = codeArea.getSelection();
        if (selection != null && dataPosition >= selection.getFirst() && dataPosition <= selection.getLast() && (section == CodeArea.Section.TEXT_PREVIEW || charOnLine < paintData.bytesPerLine * paintData.charsPerByte - 1)) {
            CodeArea.Section activeSection = codeArea.getActiveSection();
            if (activeSection == section) {
                return codeArea.getSelectionColors().getColor(colorType);
            } else {
                return codeArea.getMirrorSelectionColors().getColor(colorType);
            }
        }
        if ((line & 1) > 0) {
            return codeArea.getAlternateColors().getColor(colorType);
        } else {
            return codeArea.getMainColors().getColor(colorType);
        }
    }

    /**
     * Doesn't include character at offset end.
     */
    private void renderCharSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY, CodeArea.ColorType colorType, PaintData paintData) {
        if (colorType == CodeArea.ColorType.UNPRINTABLES) {
            g.drawChars(paintData.unprintableChars, startOffset, endOffset - startOffset, linePositionX + startOffset * paintData.charWidth, positionY);
        } else {
            g.drawChars(paintData.lineChars, startOffset, endOffset - startOffset, linePositionX + startOffset * paintData.charWidth, positionY);
        }
    }

    /**
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY, PaintData paintData) {
        g.fillRect(linePositionX + startOffset * paintData.charWidth, positionY, (endOffset - startOffset) * paintData.charWidth, paintData.lineHeight);
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
        paintData.alternateColors = codeArea.getAlternateColors();
        paintData.charRenderingMode = codeArea.getCharRenderingMode();
        paintData.fontMetrics = codeArea.getFontMetrics();
        paintData.monospaceFont = codeArea.isMonospaceFontDetected();

        CharsetEncoder encoder = paintData.charset.newEncoder();
        paintData.maxCharLength = (int) encoder.maxBytesPerChar();
        paintData.lineData = new byte[paintData.bytesPerLine + paintData.maxCharLength - 1];
        paintData.charsPerLine = paintData.bytesPerLine * (paintData.charsPerByte + 1);
        paintData.lineChars = new char[paintData.charsPerLine];
        Arrays.fill(paintData.lineChars, ' ');

        paintData.showUnprintableCharacters = codeArea.isShowUnprintableCharacters();
        if (paintData.showUnprintableCharacters) {
            paintData.unprintableChars = new char[paintData.charsPerLine];
        }

        return paintData;
    }

    /**
     * Paint cache data structure for single paint operation.
     *
     * Data copied from CodeArea for faster access + array space for line data.
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
        protected boolean showUnprintableCharacters;
        protected CodeArea.CharRenderingMode charRenderingMode;
        protected FontMetrics fontMetrics;
        protected boolean monospaceFont;
        protected int previewCharPos;

        protected CodeArea.ColorsGroup mainColors;
        protected CodeArea.ColorsGroup alternateColors;

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
         * Single line of unprintable characters.
         */
        protected char[] unprintableChars;

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

        public int getPreviewCharPos() {
            return previewCharPos;
        }

        public boolean isShowUnprintableCharacters() {
            return showUnprintableCharacters;
        }

        public CodeArea.ColorsGroup getMainColors() {
            return mainColors;
        }

        public CodeArea.ColorsGroup getStripColors() {
            return alternateColors;
        }

        public long getLineDataPosition() {
            return lineDataPosition;
        }
    }
}
