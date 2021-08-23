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
package org.exbin.bined.swt.extended;

import org.exbin.bined.swt.basic.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.FontMetrics;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditMode;
import org.exbin.bined.HexCharactersCase;
import org.exbin.bined.Section;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.ViewMode;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swt.CodeArea;
import org.exbin.bined.swt.CodeAreaPainter;
import org.exbin.bined.swt.ColorsGroup;

/**
 * Code area component default painter.
 *
 * @version 0.1.2 2016/12/29
 * @author ExBin Project (https://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    protected final CodeArea codeArea;

    private Charset charMappingCharset = null;
    protected final char[] charMapping = new char[256];
    protected Map<Character, Character> unprintableCharactersMapping = null;

    public DefaultCodeAreaPainter(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void paintOverall(GC g) {
        // Fill header area background
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle codeRect = codeArea.getCodeSectionRectangle();
        if (compRect.y < codeRect.y) {
            g.setForeground(codeArea.getBackground());
            g.fillRectangle(compRect.x, compRect.y, compRect.x + compRect.width, codeRect.y - compRect.y);
        }

        // Draw decoration lines
        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_LINENUM_LINE) > 0) {
            g.setForeground(codeArea.getDecorationLineColor());
            int lineX = codeRect.x - 1 - codeArea.getLineNumberSpace() / 2;
            g.drawLine(lineX, compRect.y, lineX, codeRect.y);
        }
        if ((decorationMode & CodeArea.DECORATION_HEADER_LINE) > 0) {
            g.setForeground(codeArea.getDecorationLineColor());
            g.drawLine(compRect.x, codeRect.y - 1, compRect.x + compRect.width, codeRect.y - 1);
        }
        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
            g.setForeground(codeArea.getDecorationLineColor());
            g.drawLine(codeRect.x - 1, codeRect.y - 1, codeRect.x + codeRect.width, codeRect.y - 1);
        }
    }

    @Override
    public void paintHeader(GC g) {
        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle codeRect = codeArea.getCodeSectionRectangle();
        boolean monospaceFont = codeArea.isMonospaceFontDetected();
        FontMetrics fontMetrics = codeArea.getFontMetrics();
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        if (codeArea.getViewMode() != ViewMode.TEXT_PREVIEW) {
            int charWidth = codeArea.getCharWidth();
            int bytesPerLine = codeArea.getBytesPerLine();
            int charsPerLine = codeArea.computeByteCharPos(bytesPerLine, false);
            int headerX = codeRect.x - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
            int headerY = /* codeArea.getInsets().top + */ codeArea.getLineHeight() - codeArea.getSubFontSpace();

            int visibleCharStart = (scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset()) / charWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            int visibleCharEnd = (codeRect.width + (scrollPosition.getScrollCharPosition() + charsPerLine) * charWidth + scrollPosition.getScrollCharOffset()) / charWidth;
            if (visibleCharEnd > charsPerLine) {
                visibleCharEnd = charsPerLine;
            }
            int visibleStart = codeArea.computeByteOffsetPerCodeCharOffset(visibleCharStart, false);
            int visibleEnd = codeArea.computeByteOffsetPerCodeCharOffset(visibleCharEnd - 1, false) + 1;

            if (codeArea.getBackgroundPaintMode() == CodeArea.BackgroundPaintMode.GRIDDED) {
                ColorsGroup stripColors = codeArea.getAlternateColors();
                g.setForeground(stripColors.getBackgroundColor());
                int positionX = codeRect.x - scrollPosition.getScrollCharOffset() - scrollPosition.getScrollCharPosition() * charWidth;
                for (int i = visibleStart / 2; i < visibleEnd / 2; i++) {
                    g.fillRectangle(positionX + charWidth * codeArea.computeByteCharPos(i * 2 + 1), compRect.y, charWidth * codeDigits, codeRect.y - compRect.y);
                }
            }

            g.setForeground(codeArea.getForeground());
            char[] headerChars = new char[charsPerLine];
            Arrays.fill(headerChars, ' ');
            CodeArea.CharRenderingMode charRenderingMode = codeArea.getCharRenderingMode();

            boolean upperCase = codeArea.getHexCharactersCase() == HexCharactersCase.UPPER;
            boolean interleaving = false;
            int lastPos = 0;
            for (int index = visibleStart; index < visibleEnd; index++) {
                int codePos = codeArea.computeByteCharPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(headerChars, codePos, index, codeArea.getPositionCodeType().getBase(), 2, true, upperCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int renderOffset = visibleCharStart;
            ColorsGroup.ColorType renderColorType = null;
            Color renderColor = null;
            for (int charOnLine = visibleCharStart; charOnLine < visibleCharEnd; charOnLine++) {
                int byteOnLine;
                byteOnLine = codeArea.computeByteOffsetPerCodeCharOffset(charOnLine, false);
                boolean sequenceBreak = false;
                boolean nativeWidth = true;

                int currentCharWidth = 0;
                ColorsGroup.ColorType colorType = ColorsGroup.ColorType.TEXT;
                if (charRenderingMode != CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                    char currentChar = ' ';
                    if (colorType == ColorsGroup.ColorType.TEXT) {
                        currentChar = headerChars[charOnLine];
                    }
                    if (currentChar == ' ' && renderOffset == charOnLine) {
                        renderOffset++;
                        continue;
                    }
                    if (charRenderingMode == CodeArea.CharRenderingMode.AUTO && monospaceFont) {
                        // Detect if character is in unicode range covered by monospace fonts
                        if (currentChar > CodeAreaSwingUtils.MIN_MONOSPACE_CODE_POINT && (int) currentChar < CodeAreaSwingUtils.MAX_MONOSPACE_CODE_POINT
                                && currentChar != CodeAreaSwingUtils.INV_SPACE_CODE_POINT
                                && currentChar != CodeAreaSwingUtils.EXCEPTION1_CODE_POINT && currentChar != CodeAreaSwingUtils.EXCEPTION2_CODE_POINT) {
                            currentCharWidth = charWidth;
                        }
                    }

                    if (currentCharWidth == 0) {
                        // TODO
                        currentCharWidth = charWidth; // fontMetrics.charWidth(currentChar);
                        nativeWidth = currentCharWidth == charWidth;
                    }
                } else {
                    currentCharWidth = charWidth;
                }

                Color color = getHeaderPositionColor(byteOnLine, charOnLine);
                if (renderColorType == null) {
                    renderColorType = colorType;
                    renderColor = color;
                    g.setForeground(color);
                }

                if (!nativeWidth || !areSameColors(color, renderColor) || !colorType.equals(renderColorType)) {
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < charOnLine) {
                        g.drawText(String.valueOf(headerChars).substring(renderOffset, charOnLine), headerX + renderOffset * charWidth, headerY);
                    }

                    if (!colorType.equals(renderColorType)) {
                        renderColorType = colorType;
                    }
                    if (!areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setForeground(color);
                    }

                    if (!nativeWidth) {
                        renderOffset = charOnLine + 1;
                        if (charRenderingMode == CodeArea.CharRenderingMode.TOP_LEFT) {
                            g.drawText(String.valueOf(headerChars[charOnLine]), headerX + charOnLine * charWidth, headerY);
                        } else {
                            drawShiftedChar(g, headerChars, charOnLine, charWidth, headerX + charOnLine * charWidth, headerY, (charWidth + 1 - currentCharWidth) >> 1);
                        }
                    } else {
                        renderOffset = charOnLine;
                    }
                }
            }

            if (renderOffset < charsPerLine) {
                g.drawText(String.valueOf(headerChars).substring(renderOffset, charsPerLine), headerX + renderOffset * charWidth, headerY);
            }
        }

        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_HEADER_LINE) > 0) {
            g.setForeground(codeArea.getDecorationLineColor());
            g.drawLine(compRect.x, codeRect.y - 1, compRect.x + compRect.width, codeRect.y - 1);
        }
        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
            g.setForeground(codeArea.getDecorationLineColor());
            g.drawLine(codeRect.x - 1, codeRect.y - 1, codeRect.x + codeRect.width, codeRect.y - 1);
        }
        if ((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) {
            int lineX = codeArea.getPreviewX() - scrollPosition.getScrollCharPosition() * codeArea.getCharWidth() - scrollPosition.getScrollCharOffset() - codeArea.getCharWidth() / 2;
            if (lineX >= codeRect.x) {
                g.setForeground(codeArea.getDecorationLineColor());
                g.drawLine(lineX, compRect.y, lineX, codeRect.y);
            }
        }
    }

    public Color getHeaderPositionColor(int byteOnLine, int charOnLine) {
        return codeArea.getForeground();
    }

    @Override
    public void paintBackground(GC g) {
        Rectangle clipBounds = g.getClipping();
        Rectangle codeRect = codeArea.getCodeSectionRectangle();
        ColorsGroup mainColors = codeArea.getMainColors();
        ColorsGroup stripColors = codeArea.getAlternateColors();
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        int startX = clipBounds.x;
        int width = clipBounds.width;
        if (!codeArea.isLineNumberBackground() && codeArea.isShowLineNumbers()) {
            int lineNumberWidth = codeRect.x - 1 - codeArea.getLineNumberSpace() / 2;
            if (startX < lineNumberWidth) {
                int diff = lineNumberWidth - startX;
                startX = lineNumberWidth;
                width -= diff;
            }
        }
        if (codeArea.getBackgroundPaintMode() != CodeArea.BackgroundPaintMode.NONE) {
            g.setForeground(mainColors.getBackgroundColor());
            g.fillRectangle(startX, clipBounds.y, width, clipBounds.height);
        }

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long line = scrollPosition.getScrollLinePosition();
        long maxDataPosition = codeArea.getDataSize();
        int maxY = clipBounds.y + clipBounds.height;

        int positionY;
        long dataPosition = line * bytesPerLine;
        if (codeArea.getBackgroundPaintMode() == CodeArea.BackgroundPaintMode.STRIPPED || codeArea.getBackgroundPaintMode() == CodeArea.BackgroundPaintMode.GRIDDED) {
            g.setForeground(stripColors.getBackgroundColor());

            positionY = codeRect.y - scrollPosition.getScrollLineOffset();
            if ((line & 1) == 0) {
                positionY += lineHeight;
                dataPosition += bytesPerLine;
            }
            while (positionY <= maxY && dataPosition < maxDataPosition) {
                g.fillRectangle(startX, positionY, width, lineHeight);
                positionY += lineHeight * 2;
                dataPosition += bytesPerLine * 2;
            }
        }
    }

    @Override
    public void paintLineNumbers(GC g) {
        Rectangle clipBounds = g.getClipping();
        Rectangle compRect = codeArea.getComponentRectangle();
        Rectangle codeRect = codeArea.getCodeSectionRectangle();
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();

        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
        long line = scrollPosition.getScrollLinePosition();
        long maxDataPosition = codeArea.getDataSize();
        int maxY = clipBounds.y + clipBounds.height + lineHeight;
        long dataPosition = line * bytesPerLine - scrollPosition.getLineByteShift();
        int charWidth = codeArea.getCharWidth();
        int positionY = codeRect.y - codeArea.getSubFontSpace() - scrollPosition.getScrollLineOffset() + codeArea.getLineHeight();

        g.setForeground(codeArea.getForeground());
        int lineNumberLength = codeArea.getLineNumberLength();
        char[] lineNumberCode = new char[lineNumberLength];
        boolean upperCase = codeArea.getHexCharactersCase() == HexCharactersCase.UPPER;
        while (positionY <= maxY && dataPosition <= maxDataPosition) {
            CodeAreaUtils.longToBaseCode(lineNumberCode, 0, dataPosition < 0 ? 0 : dataPosition, codeArea.getPositionCodeType().getBase(), lineNumberLength, true, upperCase);
            if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                g.drawText(String.valueOf(lineNumberCode).substring(0, lineNumberLength - 1), compRect.x, positionY);
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
            g.setForeground(codeArea.getDecorationLineColor());
            int lineX = codeRect.x - 1 - codeArea.getLineNumberSpace() / 2;
            g.drawLine(lineX, compRect.y, lineX, codeRect.y + codeRect.height);
        }
        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
            g.setForeground(codeArea.getDecorationLineColor());
            g.drawLine(codeRect.x - 1, codeRect.y - 1, codeRect.x - 1, codeRect.y + codeRect.height);
        }
    }

    @Override
    public void paintMainArea(GC g) {
        PaintData paintData = new PaintData(codeArea);
        paintMainArea(g, paintData);
    }

    public void paintMainArea(GC g, PaintData paintData) {
        if (paintData.viewMode != ViewMode.TEXT_PREVIEW && codeArea.getBackgroundPaintMode() == CodeArea.BackgroundPaintMode.GRIDDED) {
            g.setForeground(paintData.alternateColors.getBackgroundColor());
            int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.getScrollCharOffset() - paintData.scrollPosition.getScrollCharPosition() * paintData.charWidth;
            for (int i = paintData.visibleCodeStart / 2; i < paintData.visibleCodeEnd / 2; i++) {
                g.fillRectangle(positionX + paintData.charWidth * codeArea.computeByteCharPos(i * 2 + 1), paintData.codeSectionRect.y, paintData.charWidth * paintData.codeDigits, paintData.codeSectionRect.height);
            }
        }

        int positionY = paintData.codeSectionRect.y - paintData.scrollPosition.getScrollLineOffset();
        paintData.line = paintData.scrollPosition.getScrollLinePosition();
        int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.getScrollCharPosition() * paintData.charWidth - paintData.scrollPosition.getScrollCharOffset();
        paintData.lineDataPosition = paintData.line * paintData.bytesPerLine - paintData.scrollPosition.getLineByteShift();
        long dataSize = codeArea.getDataSize();

        do {
            if (paintData.showUnprintableCharacters) {
                Arrays.fill(paintData.unprintableChars, ' ');
            }
            int lineBytesLimit = paintData.bytesPerLine;
            if (paintData.lineDataPosition < dataSize) {
                int lineDataSize = paintData.bytesPerLine + paintData.maxCharLength - 1;
                if (paintData.lineDataPosition + lineDataSize > dataSize) {
                    lineDataSize = (int) (dataSize - paintData.lineDataPosition);
                }
                if (paintData.lineDataPosition < 0) {
                    paintData.lineStart = (int) -paintData.lineDataPosition;
                } else {
                    paintData.lineStart = 0;
                }
                codeArea.getBinaryData().copyToArray(paintData.lineDataPosition + paintData.lineStart, paintData.lineData, paintData.lineStart, lineDataSize - paintData.lineStart);
                if (paintData.lineDataPosition + lineBytesLimit > dataSize) {
                    lineBytesLimit = (int) (dataSize - paintData.lineDataPosition);
                }
            } else {
                lineBytesLimit = 0;
            }

            // Fill codes
            if (paintData.viewMode != ViewMode.TEXT_PREVIEW) {
                for (int byteOnLine = Math.max(paintData.visibleCodeStart, paintData.lineStart); byteOnLine < Math.min(paintData.visibleCodeEnd, lineBytesLimit); byteOnLine++) {
                    byte dataByte = paintData.lineData[byteOnLine];
                    CodeAreaUtils.byteToCharsCode(dataByte, codeArea.getCodeType(), paintData.lineChars, codeArea.computeByteCharPos(byteOnLine), codeArea.getHexCharactersCase());
                }
                if (paintData.bytesPerLine > lineBytesLimit) {
                    Arrays.fill(paintData.lineChars, codeArea.computeByteCharPos(lineBytesLimit), paintData.lineChars.length, ' ');
                }
            }

            // Fill preview characters
            if (paintData.viewMode != ViewMode.CODE_MATRIX) {
                for (int byteOnLine = paintData.visiblePreviewStart; byteOnLine < Math.min(paintData.visiblePreviewEnd, lineBytesLimit); byteOnLine++) {
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
                            buildCharMapping(paintData.charset);
                        }

                        paintData.lineChars[paintData.previewCharPos + byteOnLine] = charMapping[dataByte & 0xFF];
                    }

                    if (paintData.showUnprintableCharacters || paintData.charRenderingMode == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                        if (unprintableCharactersMapping == null) {
                            buildUnprintableCharactersMapping();
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
            paintLineBackground(g, positionX, positionY, paintData);
            paintLineText(g, positionX, positionY, paintData);
            paintData.lineDataPosition += paintData.bytesPerLine;
            paintData.line++;
            positionY += paintData.lineHeight;
        } while (positionY - paintData.lineHeight < paintData.codeSectionRect.y + paintData.codeSectionRect.height);

        // Draw decoration lines
        int decorationMode = codeArea.getDecorationMode();
        if ((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) {
            int lineX = codeArea.getPreviewX() - paintData.scrollPosition.getScrollCharPosition() * codeArea.getCharWidth() - paintData.scrollPosition.getScrollCharOffset() - codeArea.getCharWidth() / 2;
            if (lineX >= paintData.codeSectionRect.x) {
                g.setForeground(codeArea.getDecorationLineColor());
                g.drawLine(lineX, paintData.codeSectionRect.y, lineX, paintData.codeSectionRect.y + paintData.codeSectionRect.height);
            }
        }
    }

    public void paintLineBackground(GC g, int linePositionX, int linePositionY, PaintData paintData) {
        int renderOffset = paintData.visibleCharStart;
        ColorsGroup.ColorType renderColorType = null;
        Color renderColor = null;
        for (int charOnLine = paintData.visibleCharStart; charOnLine < paintData.visibleCharEnd; charOnLine++) {
            Section section;
            int byteOnLine;
            if (charOnLine >= paintData.previewCharPos && paintData.viewMode != ViewMode.CODE_MATRIX) {
                byteOnLine = charOnLine - paintData.previewCharPos;
                section = Section.TEXT_PREVIEW;
            } else {
                byteOnLine = codeArea.computeByteOffsetPerCodeCharOffset(charOnLine, false);
                section = Section.CODE_MATRIX;
            }
            boolean sequenceBreak = false;

            ColorsGroup.ColorType colorType = ColorsGroup.ColorType.BACKGROUND;
            if (paintData.showUnprintableCharacters) {
                if (paintData.unprintableChars[charOnLine] != ' ') {
                    colorType = ColorsGroup.ColorType.UNPRINTABLES_BACKGROUND;
                }
            }

            Color color = getPositionColor(byteOnLine, charOnLine, section, colorType, paintData);
            if (renderColorType == null) {
                renderColorType = colorType;
                renderColor = color;
                g.setForeground(color);
            }

            if (!areSameColors(color, renderColor) || !colorType.equals(renderColorType)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnLine) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnLine, linePositionX, linePositionY, paintData);
                    }
                }

                if (!colorType.equals(renderColorType)) {
                    renderColorType = colorType;
                }
                if (!areSameColors(color, renderColor)) {
                    renderColor = color;
                    g.setForeground(color);
                }

                renderOffset = charOnLine;
            }
        }

        if (renderOffset < paintData.charsPerLine) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, paintData.charsPerLine, linePositionX, linePositionY, paintData);
            }
        }
    }

    private boolean areSameColors(Color color, Color comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    public void paintLineText(GC g, int linePositionX, int linePositionY, PaintData paintData) {
        int positionY = linePositionY + paintData.lineHeight - codeArea.getSubFontSpace();

        int renderOffset = paintData.visibleCharStart;
        ColorsGroup.ColorType renderColorType = null;
        Color renderColor = null;
        for (int charOnLine = paintData.visibleCharStart; charOnLine < paintData.visibleCharEnd; charOnLine++) {
            Section section;
            int byteOnLine;
            if (charOnLine >= paintData.previewCharPos) {
                byteOnLine = charOnLine - paintData.previewCharPos;
                section = Section.TEXT_PREVIEW;
            } else {
                byteOnLine = codeArea.computeByteOffsetPerCodeCharOffset(charOnLine, false);
                section = Section.CODE_MATRIX;
            }
            boolean sequenceBreak = false;
            boolean nativeWidth = true;

            int currentCharWidth = 0;
            ColorsGroup.ColorType colorType = ColorsGroup.ColorType.TEXT;
            if (paintData.charRenderingMode != CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                char currentChar = ' ';
                if (paintData.showUnprintableCharacters) {
                    currentChar = paintData.unprintableChars[charOnLine];
                    if (currentChar != ' ') {
                        colorType = ColorsGroup.ColorType.UNPRINTABLES;
                    }
                }
                if (colorType == ColorsGroup.ColorType.TEXT) {
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
                    currentCharWidth = paintData.charWidth(currentChar);
                    nativeWidth = currentCharWidth == paintData.charWidth;
                }
            } else {
                currentCharWidth = paintData.charWidth;
                if (paintData.showUnprintableCharacters) {
                    char currentChar = paintData.unprintableChars[charOnLine];
                    if (currentChar != ' ') {
                        colorType = ColorsGroup.ColorType.UNPRINTABLES;
                        currentCharWidth = paintData.charWidth(currentChar);
                        nativeWidth = currentCharWidth == paintData.charWidth;
                    }
                }
            }

            Color color = getPositionColor(byteOnLine, charOnLine, section, colorType, paintData);
            if (renderColorType == null) {
                renderColorType = colorType;
                renderColor = color;
                g.setForeground(color);
            }

            if (!nativeWidth || !areSameColors(color, renderColor) || !colorType.equals(renderColorType)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnLine) {
                    renderCharSequence(g, renderOffset, charOnLine, linePositionX, positionY, renderColorType, paintData);
                }

                if (!colorType.equals(renderColorType)) {
                    renderColorType = colorType;
                }
                if (!areSameColors(color, renderColor)) {
                    renderColor = color;
                    g.setForeground(color);
                }

                if (!nativeWidth) {
                    renderOffset = charOnLine + 1;
                    if (paintData.charRenderingMode == CodeArea.CharRenderingMode.TOP_LEFT) {
                        g.drawString(
                                String.valueOf(renderColorType == ColorsGroup.ColorType.UNPRINTABLES ? paintData.unprintableChars[charOnLine] : paintData.lineChars[charOnLine]),
                                linePositionX + charOnLine * paintData.charWidth, positionY);
                    } else {
                        drawShiftedChar(g,
                                renderColorType == ColorsGroup.ColorType.UNPRINTABLES ? paintData.unprintableChars : paintData.lineChars,
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
     * @param byteOnLine byte on line
     * @param charOnLine character on line
     * @param section rendering section
     * @param colorType color type
     * @param paintData cached paint data
     * @return color
     */
    public Color getPositionColor(int byteOnLine, int charOnLine, Section section, ColorsGroup.ColorType colorType, PaintData paintData) {
        long dataPosition = paintData.lineDataPosition + byteOnLine;
        SelectionRange selection = codeArea.getSelection();
        if (selection != null && dataPosition >= selection.getFirst() && dataPosition <= selection.getLast() && (section == Section.TEXT_PREVIEW || charOnLine < paintData.charsPerCodeArea)) {
            Section activeSection = codeArea.getActiveSection();
            if (activeSection == section) {
                return codeArea.getSelectionColors().getColor(colorType);
            } else {
                return codeArea.getMirrorSelectionColors().getColor(colorType);
            }
        }
        if (colorType == ColorsGroup.ColorType.BACKGROUND) {
            // Background is prepainted
            return null;
        }
        if (((paintData.backgroundMode == CodeArea.BackgroundPaintMode.STRIPPED || paintData.backgroundMode == CodeArea.BackgroundPaintMode.GRIDDED) && (paintData.line & 1) > 0)
                || (paintData.backgroundMode == CodeArea.BackgroundPaintMode.GRIDDED && ((byteOnLine & 1) > 0)) && section == Section.CODE_MATRIX) {
            return codeArea.getAlternateColors().getColor(colorType);
        }

        return codeArea.getMainColors().getColor(colorType);
    }

    /**
     * Render sequence of characters.
     *
     * Doesn't include character at offset end.
     */
    private void renderCharSequence(GC g, int startOffset, int endOffset, int linePositionX, int positionY, ColorsGroup.ColorType colorType, PaintData paintData) {
        if (colorType == ColorsGroup.ColorType.UNPRINTABLES) {
            g.drawString(String.valueOf(paintData.unprintableChars).substring(startOffset, endOffset), linePositionX + startOffset * paintData.charWidth, positionY);
        } else {
            g.drawString(String.valueOf(paintData.lineChars).substring(startOffset, endOffset), linePositionX + startOffset * paintData.charWidth, positionY);
        }
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(GC g, int startOffset, int endOffset, int linePositionX, int positionY, PaintData paintData) {
        g.fillRectangle(linePositionX + startOffset * paintData.charWidth, positionY, (endOffset - startOffset) * paintData.charWidth, paintData.lineHeight);
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
    protected void drawCenteredChar(GC g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY) {
        FontMetrics fontMetrics = codeArea.getFontMetrics();
        // TODO
        int charWidth = fontMetrics.getAverageCharWidth(); // fontMetrics.charWidth(drawnChars[charOffset]);
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, startX, positionY, (charWidthSpace + 1 - charWidth) >> 1);
    }

    protected void drawShiftedChar(GC g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY, int shift) {
        g.drawString(String.valueOf(drawnChars[charOffset]), startX + shift, positionY);
    }

    @Override
    public void paintCursor(GC g) {
        if (!codeArea.isFocusControl()) {
            return;
        }

        CodeAreaCaret caret = codeArea.getCaret();
        int bytesPerLine = codeArea.getBytesPerLine();
        int lineHeight = codeArea.getLineHeight();
        int charWidth = codeArea.getCharWidth();
        int linesPerRect = codeArea.getLinesPerRect();
        int codeDigits = codeArea.getCodeType().getMaxDigits();
        Point cursorPoint = caret.getCursorPoint(bytesPerLine, lineHeight, charWidth, linesPerRect);
        boolean cursorVisible = caret.isCursorVisible();
        CodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();

        if (cursorVisible && cursorPoint != null) {
            g.setForeground(codeArea.getCursorColor());
            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
                g.setXORMode(true); // Color.WHITE
            }

            CodeAreaCaret.CursorShape cursorShape = codeArea.getEditMode() == EditMode.INSERT ? caret.getInsertCursorShape() : caret.getOverwriteCursorShape();
            int cursorThickness = 0;
            if (cursorShape.getWidth() != CodeAreaCaret.CursorShapeWidth.FULL) {
                cursorThickness = caret.getCursorThickness(cursorShape, charWidth, lineHeight);
            }
            switch (cursorShape) {
                case LINE_TOP:
                case DOUBLE_TOP:
                case QUARTER_TOP:
                case HALF_TOP: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
                            charWidth, cursorThickness, renderingMode);
                    break;
                }
                case LINE_BOTTOM:
                case DOUBLE_BOTTOM:
                case QUARTER_BOTTOM:
                case HALF_BOTTOM: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y + lineHeight - cursorThickness,
                            charWidth, cursorThickness, renderingMode);
                    break;
                }
                case LINE_LEFT:
                case DOUBLE_LEFT:
                case QUARTER_LEFT:
                case HALF_LEFT: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
                    break;
                }
                case LINE_RIGHT:
                case DOUBLE_RIGHT:
                case QUARTER_RIGHT:
                case HALF_RIGHT: {
                    paintCursorRect(g, cursorPoint.x + charWidth - cursorThickness, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
                    break;
                }
                case BOX: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
                            charWidth, lineHeight, renderingMode);
                    break;
                }
                case FRAME: {
                    g.drawRectangle(cursorPoint.x, cursorPoint.y, charWidth, lineHeight - 1);
                    break;
                }
                case BOTTOM_CORNERS:
                case CORNERS: {
                    int quarterWidth = charWidth / 4;
                    int quarterLine = lineHeight / 4;
                    if (cursorShape == CodeAreaCaret.CursorShape.CORNERS) {
                        g.drawLine(cursorPoint.x, cursorPoint.y,
                                cursorPoint.x + quarterWidth, cursorPoint.y);
                        g.drawLine(cursorPoint.x + charWidth - quarterWidth, cursorPoint.y,
                                cursorPoint.x + charWidth, cursorPoint.y);

                        g.drawLine(cursorPoint.x, cursorPoint.y + 1,
                                cursorPoint.x, cursorPoint.y + quarterLine);
                        g.drawLine(cursorPoint.x + charWidth, cursorPoint.y + 1,
                                cursorPoint.x + charWidth, cursorPoint.y + quarterLine);
                    }

                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x, cursorPoint.y + lineHeight - 2);
                    g.drawLine(cursorPoint.x + charWidth, cursorPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x + charWidth, cursorPoint.y + lineHeight - 2);

                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - 1,
                            cursorPoint.x + quarterWidth, cursorPoint.y + lineHeight - 1);
                    g.drawLine(cursorPoint.x + charWidth - quarterWidth, cursorPoint.y + lineHeight - 1,
                            cursorPoint.x + charWidth, cursorPoint.y + lineHeight - 1);
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(cursorShape);
            }

            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
                throw new UnsupportedOperationException("Not supported yet.");
                // TODO g.setPaintMode();
            }
        }

        // Paint shadow cursor
        if (codeArea.getViewMode() == ViewMode.DUAL && codeArea.isShowShadowCursor()) {
            g.setForeground(codeArea.getCursorColor());
            Point shadowCursorPoint = caret.getShadowCursorPoint(bytesPerLine, lineHeight, charWidth, linesPerRect);
            if (shadowCursorPoint != null) {
                g.setLineStyle(SWT.LINE_DASH);
                g.drawRectangle(shadowCursorPoint.x, shadowCursorPoint.y,
                        charWidth * (codeArea.getActiveSection() == Section.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
            }
        }
    }

    private void paintCursorRect(GC g, int x, int y, int width, int height, CodeAreaCaret.CursorRenderingMode renderingMode) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRectangle(x, y, width, height);
                break;
            }
            case XOR: {
                Rectangle rect = new Rectangle(x, y, width, height);
                Rectangle intersection = rect.intersection(g.getClipping());
                if (!intersection.isEmpty()) {
                    g.fillRectangle(intersection.x, intersection.y, intersection.width, intersection.height);
                }
                break;
            }
            case NEGATIVE: {
                Rectangle rect = new Rectangle(x, y, width, height);
                Rectangle intersection = rect.intersection(g.getClipping());
                if (intersection.isEmpty()) {
                    break;
                }
                Rectangle clip = g.getClipping();
                g.setClipping(intersection.x, intersection.y, intersection.width, intersection.height);
                CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
                g.fillRectangle(x, y, width, height);
                g.setForeground(codeArea.getNegativeCursorColor());
                Rectangle codeRect = codeArea.getCodeSectionRectangle();
                int previewX = codeArea.getPreviewX();
                int charWidth = codeArea.getCharWidth();
                int lineHeight = codeArea.getLineHeight();
                int line = (y + scrollPosition.getScrollLineOffset() - codeRect.y) / lineHeight;
                int scrolledX = x + scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();
                int posY = codeRect.y + (line + 1) * lineHeight - codeArea.getSubFontSpace() - scrollPosition.getScrollLineOffset();
                if (codeArea.getViewMode() != ViewMode.CODE_MATRIX && scrolledX >= previewX) {
                    int charPos = (scrolledX - previewX) / charWidth;
                    long dataSize = codeArea.getDataSize();
                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + charPos - scrollPosition.getLineByteShift();
                    if (dataPosition >= dataSize) {
                        g.setClipping(clip);
                        break;
                    }

                    char[] previewChars = new char[1];
                    Charset charset = codeArea.getCharset();
                    CharsetEncoder encoder = charset.newEncoder();
                    int maxCharLength = (int) encoder.maxBytesPerChar();
                    byte[] data = new byte[maxCharLength];

                    if (maxCharLength > 1) {
                        int charDataLength = maxCharLength;
                        if (dataPosition + maxCharLength > dataSize) {
                            charDataLength = (int) (dataSize - dataPosition);
                        }

                        codeArea.getBinaryData().copyToArray(dataPosition, data, 0, charDataLength);
                        String displayString = new String(data, 0, charDataLength, charset);
                        if (!displayString.isEmpty()) {
                            previewChars[0] = displayString.charAt(0);
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != charset) {
                            buildCharMapping(charset);
                        }

                        previewChars[0] = charMapping[codeArea.getBinaryData().getByte(dataPosition) & 0xFF];
                    }

                    if (codeArea.isShowUnprintableCharacters()) {
                        if (unprintableCharactersMapping == null) {
                            buildUnprintableCharactersMapping();
                        }
                        Character replacement = unprintableCharactersMapping.get(previewChars[0]);
                        if (replacement != null) {
                            previewChars[0] = replacement;
                        }
                    }
                    int posX = previewX + charPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
                    if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                        g.drawText(String.valueOf(previewChars[0]), posX, posY);
                    } else {
                        drawCenteredChar(g, previewChars, 0, charWidth, posX, posY);
                    }
                } else {
                    int charPos = (scrolledX - codeRect.x) / charWidth;
                    int byteOffset = codeArea.computeByteOffsetPerCodeCharOffset(charPos, false);
                    int codeCharPos = codeArea.computeByteCharPos(byteOffset);
                    char[] lineChars = new char[codeArea.getCodeType().getMaxDigits()];
                    long dataSize = codeArea.getDataSize();
                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + byteOffset - scrollPosition.getLineByteShift();
                    if (dataPosition >= dataSize) {
                        g.setClipping(clip);
                        break;
                    }

                    byte dataByte = codeArea.getBinaryData().getByte(dataPosition);
                    CodeAreaUtils.byteToCharsCode(dataByte, codeArea.getCodeType(), lineChars, 0, codeArea.getHexCharactersCase());
                    int posX = codeRect.x + codeCharPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                        g.drawText(String.valueOf(lineChars[charsOffset]), posX + (charsOffset * charWidth), posY);
                    } else {
                        drawCenteredChar(g, lineChars, charsOffset, charWidth, posX + (charsOffset * charWidth), posY);
                    }
                }
                g.setClipping(clip);
                break;
            }
        }
    }

    private void buildCharMapping(Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private void buildUnprintableCharactersMapping() {
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

    /**
     * Paint cache data structure for single paint operation.
     *
     * Data copied from CodeArea for faster access + array space for line data.
     */
    protected static class PaintData {

        protected ViewMode viewMode;
        protected CodeArea.BackgroundPaintMode backgroundMode;
        protected Rectangle codeSectionRect;
        protected CodeArea.ScrollPosition scrollPosition;
        protected int charWidth;
        protected int bytesPerLine;
        protected int lineHeight;
        protected int codeDigits;
        protected int byteGroupSize;
        protected int spaceGroupSize;
        protected int charsPerLine;
        protected Charset charset;
        protected int maxCharLength;
        protected boolean showUnprintableCharacters;
        protected CodeArea.CharRenderingMode charRenderingMode;
        protected FontMetrics fontMetrics;
        protected boolean monospaceFont;
        protected int charsPerCodeArea;
        protected int previewCharPos;
        protected int visibleCharStart;
        protected int visibleCharEnd;
        protected int visibleCodeStart;
        protected int visibleCodeEnd;
        protected int visiblePreviewStart;
        protected int visiblePreviewEnd;

        protected ColorsGroup mainColors;
        protected ColorsGroup alternateColors;

        // Line related fields
        protected int lineStart;
        protected long lineDataPosition;
        protected long line;

        protected char[] lineChars;

        /**
         * Line data cache.
         */
        protected byte[] lineData;

        /**
         * Single line of unprintable characters.
         */
        protected char[] unprintableChars;

        public PaintData(CodeArea codeArea) {
            viewMode = codeArea.getViewMode();
            backgroundMode = codeArea.getBackgroundPaintMode();
            codeSectionRect = codeArea.getCodeSectionRectangle();
            scrollPosition = codeArea.getScrollPosition();
            charWidth = codeArea.getCharWidth();
            bytesPerLine = codeArea.getBytesPerLine();
            lineHeight = codeArea.getLineHeight();
            codeDigits = codeArea.getCodeType().getMaxDigits();
            charset = codeArea.getCharset();
            mainColors = codeArea.getMainColors();
            alternateColors = codeArea.getAlternateColors();
            charRenderingMode = codeArea.getCharRenderingMode();
            fontMetrics = codeArea.getFontMetrics();
            monospaceFont = codeArea.isMonospaceFontDetected();
            byteGroupSize = codeArea.getByteGroupSize();
            spaceGroupSize = codeArea.getSpaceGroupSize();

            CharsetEncoder encoder = charset.newEncoder();
            maxCharLength = (int) encoder.maxBytesPerChar();
            lineData = new byte[bytesPerLine + maxCharLength - 1];
            charsPerLine = codeArea.getCharsPerLine();

            lineChars = new char[charsPerLine];
            Arrays.fill(lineChars, ' ');

            showUnprintableCharacters = codeArea.isShowUnprintableCharacters();
            if (showUnprintableCharacters) {
                unprintableChars = new char[charsPerLine];
            }

            charsPerCodeArea = codeArea.computeByteCharPos(bytesPerLine, false);
            // Compute first and last visible character of the code area
            if (viewMode == ViewMode.DUAL) {
                previewCharPos = charsPerCodeArea + 1;
            } else {
                previewCharPos = 0;
            }

            if (viewMode == ViewMode.DUAL || viewMode == ViewMode.CODE_MATRIX) {
                visibleCharStart = (scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset()) / charWidth;
                if (visibleCharStart < 0) {
                    visibleCharStart = 0;
                }
                visibleCharEnd = (codeSectionRect.width + (scrollPosition.getScrollCharPosition() + charsPerLine) * charWidth + scrollPosition.getScrollCharOffset()) / charWidth;
                if (visibleCharEnd > charsPerCodeArea) {
                    visibleCharEnd = charsPerCodeArea;
                }
                visibleCodeStart = codeArea.computeByteOffsetPerCodeCharOffset(visibleCharStart, false);
                visibleCodeEnd = codeArea.computeByteOffsetPerCodeCharOffset(visibleCharEnd - 1, false) + 1;
            } else {
                visibleCharStart = 0;
                visibleCharEnd = -1;
                visibleCodeStart = 0;
                visibleCodeEnd = -1;
            }

            if (viewMode == ViewMode.DUAL || viewMode == ViewMode.TEXT_PREVIEW) {
                visiblePreviewStart = (scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset()) / charWidth - previewCharPos;
                if (visiblePreviewStart < 0) {
                    visiblePreviewStart = 0;
                }
                if (visibleCodeEnd < 0) {
                    visibleCharStart = visiblePreviewStart + previewCharPos;
                }
                visiblePreviewEnd = (codeSectionRect.width + (scrollPosition.getScrollCharPosition() + 1) * charWidth + scrollPosition.getScrollCharOffset()) / charWidth - previewCharPos;
                if (visiblePreviewEnd > bytesPerLine) {
                    visiblePreviewEnd = bytesPerLine;
                }
                if (visiblePreviewEnd >= 0) {
                    visibleCharEnd = visiblePreviewEnd + previewCharPos;
                }
            } else {
                visiblePreviewStart = 0;
                visiblePreviewEnd = -1;
            }
        }

        public int charWidth(char character) {
            return charWidth;
            // TODO
        }

        public ViewMode getViewMode() {
            return viewMode;
        }

        public CodeArea.BackgroundPaintMode getBackgroundPaintMode() {
            return backgroundMode;
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

        public int getByteGroupSize() {
            return byteGroupSize;
        }

        public int getSpaceGroupSize() {
            return spaceGroupSize;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public int getCodeDigits() {
            return codeDigits;
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

        public ColorsGroup getMainColors() {
            return mainColors;
        }

        public ColorsGroup getStripColors() {
            return alternateColors;
        }

        public long getLineDataPosition() {
            return lineDataPosition;
        }

        public long getLine() {
            return line;
        }

        public int getVisibleCharStart() {
            return visibleCharStart;
        }

        public int getVisibleCharEnd() {
            return visibleCharEnd;
        }

        public int getVisibleCodeStart() {
            return visibleCodeStart;
        }

        public int getVisibleCodeEnd() {
            return visibleCodeEnd;
        }

        public int getVisiblePreviewStart() {
            return visiblePreviewStart;
        }

        public int getVisiblePreviewEnd() {
            return visiblePreviewEnd;
        }
    }
}
