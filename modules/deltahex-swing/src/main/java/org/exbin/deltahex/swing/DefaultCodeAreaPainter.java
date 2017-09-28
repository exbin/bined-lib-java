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
package org.exbin.deltahex.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.swing.UIManager;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeAreaUtils;
import org.exbin.deltahex.HexCharactersCase;
import org.exbin.deltahex.CodeAreaViewMode;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.swing.color.CodeAreaColorType;
import org.exbin.utils.binary_data.OutOfBoundsException;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2017/09/28
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    protected final CodeArea codeArea;
    private int subFontSpace = 3;

    private PainterState state = null;

    private BorderPaintMode borderPaintMode = BorderPaintMode.STRIPED;
    private CharacterRenderingMode characterRenderingMode = CharacterRenderingMode.AUTO;
    private HexCharactersCase hexCharactersCase = HexCharactersCase.UPPER;
    private boolean showShadowCursor = true;

    private Charset charMappingCharset = null;
    private final char[] charMapping = new char[256];
    private long paintCounter = 0;

    public DefaultCodeAreaPainter(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void reset() {
        if (state == null) {
            state = new PainterState();
        }

        resetSizes();
        resetScrollState();
        resetColors();

        state.viewMode = codeArea.getViewMode();
        state.characterRenderingMode = characterRenderingMode;
        state.hexCharactersCase = hexCharactersCase;
        state.borderPaintMode = borderPaintMode;
        state.dataSize = codeArea.getDataSize();

        state.linesPerRect = getLinesPerRectangle();
        state.bytesPerLine = getBytesPerLine();

        state.codeType = codeArea.getCodeType();
        state.maxDigits = state.codeType.getMaxDigitsForByte();

        int charactersPerLine = 0;
        if (state.viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charactersPerLine += computeLastCodeCharPos(state.bytesPerLine - 1) + 1;
        }
        if (state.viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charactersPerLine += state.bytesPerLine;
            if (state.viewMode == CodeAreaViewMode.DUAL) {
                charactersPerLine++;
            }
        }

        state.charactersPerLine = charactersPerLine;
        state.hexCharactersCase = HexCharactersCase.UPPER;
    }

    private void resetCharPositions() {
        state.charactersPerRect = computeLastCodeCharPos(state.bytesPerLine - 1);
        // Compute first and last visible character of the code area
        if (state.viewMode == CodeAreaViewMode.DUAL) {
            state.previewCharPos = state.charactersPerRect + 2;
        } else {
            state.previewCharPos = 0;
        }

        if (state.viewMode == CodeAreaViewMode.DUAL || state.viewMode == CodeAreaViewMode.CODE_MATRIX) {
            state.visibleCharStart = (state.scrollPosition.getScrollCharPosition() * state.characterWidth + state.scrollPosition.getScrollCharOffset()) / state.characterWidth;
            if (state.visibleCharStart < 0) {
                state.visibleCharStart = 0;
            }
            state.visibleCharEnd = (state.mainAreaWidth + (state.scrollPosition.getScrollCharPosition() + state.charactersPerLine) * state.characterWidth + state.scrollPosition.getScrollCharOffset()) / state.characterWidth;
            if (state.visibleCharEnd > state.charactersPerRect) {
                state.visibleCharEnd = state.charactersPerRect;
            }
            state.visibleCodeStart = computePositionByte(state.visibleCharStart);
            state.visibleCodeEnd = computePositionByte(state.visibleCharEnd - 1) + 1;
        } else {
            state.visibleCharStart = 0;
            state.visibleCharEnd = -1;
            state.visibleCodeStart = 0;
            state.visibleCodeEnd = -1;
        }

        if (state.viewMode == CodeAreaViewMode.DUAL || state.viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            state.visiblePreviewStart = (state.scrollPosition.getScrollCharPosition() * state.characterWidth + state.scrollPosition.getScrollCharOffset()) / state.characterWidth - state.previewCharPos;
            if (state.visiblePreviewStart < 0) {
                state.visiblePreviewStart = 0;
            }
            if (state.visibleCodeEnd < 0) {
                state.visibleCharStart = state.visiblePreviewStart + state.previewCharPos;
            }
            state.visiblePreviewEnd = (state.mainAreaWidth + (state.scrollPosition.getScrollCharPosition() + 1) * state.characterWidth + state.scrollPosition.getScrollCharOffset()) / state.characterWidth - state.previewCharPos;
            if (state.visiblePreviewEnd > state.bytesPerLine) {
                state.visiblePreviewEnd = state.bytesPerLine;
            }
            if (state.visiblePreviewEnd >= 0) {
                state.visibleCharEnd = state.visiblePreviewEnd + state.previewCharPos;
            }
        } else {
            state.visiblePreviewStart = 0;
            state.visiblePreviewEnd = -1;
        }

        state.lineData = new byte[state.bytesPerLine + state.maxCharLength - 1];
        state.lineNumberCode = new char[state.lineNumbersLength];
        state.lineCharacters = new char[state.charactersPerLine];
    }

    public void resetFont(@Nonnull Graphics g) {
        if (state == null) {
            reset();
        }

        state.charset = codeArea.getCharset();
        CharsetEncoder encoder = state.charset.newEncoder();
        state.maxCharLength = (int) encoder.maxBytesPerChar();

        state.font = codeArea.getFont();
        state.fontMetrics = g.getFontMetrics(state.font);
        /**
         * Use small 'w' character to guess normal font width.
         */
        state.characterWidth = state.fontMetrics.charWidth('w');
        /**
         * Compare it to small 'i' to detect if font is monospaced.
         *
         * TODO: Is there better way?
         */
        state.monospaceFont = state.characterWidth == state.fontMetrics.charWidth(' ') && state.characterWidth == state.fontMetrics.charWidth('i');
        int fontSize = state.font.getSize();
        state.lineHeight = fontSize + subFontSpace;

        state.lineNumbersLength = getLineNumberLength();
        resetSizes();
        resetCharPositions();
    }

    @Override
    public void dataViewScrolled(@Nonnull Graphics g) {
        if (!isInitialized()) {
            return;
        }

        resetScrollState();
        if (state.characterWidth > 0) {
            resetCharPositions();
            paintComponent(g);
        }
    }

    private void resetScrollState() {
        state.scrollPosition = codeArea.getScrollPosition();
        state.verticalScrollUnit = codeArea.getVerticalScrollUnit();
        state.horizontalScrollUnit = codeArea.getHorizontalScrollUnit();

        // TODO Overflow mode
        switch (state.horizontalScrollUnit) {
            case CHARACTER: {
                state.mainAreaX = state.scrollPosition.getScrollCharPosition();
                break;
            }
            case PIXEL: {
                state.mainAreaX = state.scrollPosition.getScrollCharPosition() * state.characterWidth + state.scrollPosition.getScrollCharOffset();
                break;
            }
        }

        switch (state.verticalScrollUnit) {
            case LINE: {
                state.mainAreaY = (int) state.scrollPosition.getScrollLinePosition();
                break;
            }
            case PIXEL: {
                state.mainAreaY = ((int) state.scrollPosition.getScrollLinePosition() * state.lineHeight) + state.scrollPosition.getScrollLineOffset();
                break;
            }
        }
    }

    private void resetSizes() {
        state.areaWidth = codeArea.getWidth();
        state.areaHeight = codeArea.getHeight();
        state.lineNumbersAreaWidth = state.characterWidth * (state.lineNumbersLength + 1);
        state.headerAreaHeight = 20;
        state.mainAreaWidth = state.areaWidth - state.lineNumbersAreaWidth;
        state.mainAreaHeight = state.areaHeight - state.headerAreaHeight;
    }

    private void resetColors() {
        Colors colors = state.colors;
        colors.foreground = codeArea.getForeground();
        if (colors.foreground == null) {
            colors.foreground = Color.BLACK;
        }

        colors.background = codeArea.getBackground();
        if (colors.background == null) {
            colors.background = Color.WHITE;
        }
        colors.selectionForeground = UIManager.getColor("TextArea.selectionForeground");
        if (colors.selectionForeground == null) {
            colors.selectionForeground = Color.WHITE;
        }
        colors.selectionBackground = UIManager.getColor("TextArea.selectionBackground");
        if (colors.selectionBackground == null) {
            colors.selectionBackground = new Color(96, 96, 255);
        }
        colors.selectionMirrorForeground = colors.selectionForeground;
        int grayLevel = (colors.selectionBackground.getRed() + colors.selectionBackground.getGreen() + colors.selectionBackground.getBlue()) / 3;
        colors.selectionMirrorBackground = new Color(grayLevel, grayLevel, grayLevel);
        colors.cursor = UIManager.getColor("TextArea.caretForeground");
        colors.negativeCursor = createNegativeColor(colors.cursor);
        if (colors.cursor == null) {
            colors.cursor = Color.BLACK;
        }
        colors.decorationLine = Color.GRAY;

        colors.stripes = createOddColor(colors.background);
    }

    @Override
    public boolean isInitialized() {
        return state != null;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (state == null) {
            reset();
        }
        if (state.font == null) {
            resetFont(g);
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintLineNumbers(g);
        paintMainArea(g);
        paintCounter++;
    }

    public void paintOutsiteArea(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, state.areaWidth, state.headerAreaHeight);
        g.setColor(Color.BLACK);
        g.fillRect(0, state.headerAreaHeight - 1, state.lineNumbersAreaWidth, 1);
    }

    public void paintHeader(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle headerArea = new Rectangle(state.lineNumbersAreaWidth, 0, state.areaWidth - state.lineNumbersAreaWidth, state.headerAreaHeight); // TODO minus scrollbar width
        g.setClip(clipBounds != null ? headerArea.intersection(clipBounds) : headerArea);

        g.setColor(state.colors.background);
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        // Black line
        g.setColor(Color.BLACK);
        g.fillRect(0, state.headerAreaHeight - 1, state.areaWidth, 1);

        if (state.viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int charsPerLine = computeFirstCodeCharPos(state.bytesPerLine);
            int headerX = state.lineNumbersAreaWidth + state.mainAreaX - state.scrollPosition.getScrollCharPosition() * state.characterWidth - state.scrollPosition.getScrollCharOffset();
            int headerY = codeArea.getInsets().top + state.lineHeight - subFontSpace;

            int visibleCharStart = (state.scrollPosition.getScrollCharPosition() * state.characterWidth + state.scrollPosition.getScrollCharOffset()) / state.characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            int visibleCharEnd = (state.mainAreaWidth + (state.scrollPosition.getScrollCharPosition() + charsPerLine) * state.characterWidth + state.scrollPosition.getScrollCharOffset()) / state.characterWidth;
            if (visibleCharEnd > charsPerLine) {
                visibleCharEnd = charsPerLine;
            }
            int visibleStart = computePositionByte(visibleCharStart);
            int visibleEnd = computePositionByte(visibleCharEnd - 1) + 1;

            g.setColor(codeArea.getForeground());
            char[] headerChars = new char[charsPerLine];
            Arrays.fill(headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            for (int index = visibleStart; index < visibleEnd; index++) {
                int codePos = computeFirstCodeCharPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(headerChars, codePos, index, 16, 2, true, state.hexCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int renderOffset = visibleCharStart;
//            ColorsGroup.ColorType renderColorType = null;
            Color renderColor = null;
            for (int characterOnLine = visibleCharStart; characterOnLine < visibleCharEnd; characterOnLine++) {
                int byteOnLine;
                byteOnLine = computePositionByte(characterOnLine);
                boolean sequenceBreak = false;
                boolean nativeWidth = true;

                int currentCharWidth = 0;
//                ColorsGroup.ColorType colorType = ColorsGroup.ColorType.TEXT;
                if (characterRenderingMode != CharacterRenderingMode.LINE_AT_ONCE) {
                    char currentChar = ' ';
//                    if (colorType == ColorsGroup.ColorType.TEXT) {
                    currentChar = headerChars[characterOnLine];
//                    }
                    if (currentChar == ' ' && renderOffset == characterOnLine) {
                        renderOffset++;
                        continue;
                    }
                    if (characterRenderingMode == CharacterRenderingMode.AUTO && state.monospaceFont) {
                        // Detect if character is in unicode range covered by monospace fonts
                        if (CodeAreaSwingUtils.isMonospaceFullWidthCharater(currentChar)) {
                            currentCharWidth = state.characterWidth;
                        }
                    }

                    if (currentCharWidth == 0) {
                        currentCharWidth = state.fontMetrics.charWidth(currentChar);
                        nativeWidth = currentCharWidth == state.characterWidth;
                    }
                } else {
                    currentCharWidth = state.characterWidth;
                }

                Color color = Color.BLACK;
//                getHeaderPositionColor(byteOnLine, charOnLine);
//                if (renderColorType == null) {
//                    renderColorType = colorType;
//                    renderColor = color;
//                    g.setColor(color);
//                }

                if (!nativeWidth || !areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnLine) {
                        g.drawChars(headerChars, renderOffset, characterOnLine - renderOffset, headerX + renderOffset * state.characterWidth, headerY);
                    }

//                    if (!colorType.equals(renderColorType)) {
//                        renderColorType = colorType;
//                    }
                    if (!areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setColor(color);
                    }

                    if (!nativeWidth) {
                        renderOffset = characterOnLine + 1;
                        if (characterRenderingMode == CharacterRenderingMode.TOP_LEFT) {
                            g.drawChars(headerChars, characterOnLine, 1, headerX + characterOnLine * state.characterWidth, headerY);
                        } else {
                            drawShiftedChar(g, headerChars, characterOnLine, state.characterWidth, headerX + characterOnLine * state.characterWidth, headerY, (state.characterWidth + 1 - currentCharWidth) >> 1);
                        }
                    } else {
                        renderOffset = characterOnLine;
                    }
                }
            }

            if (renderOffset < charsPerLine) {
                g.drawChars(headerChars, renderOffset, charsPerLine - renderOffset, headerX + renderOffset * state.characterWidth, headerY);
            }
        }

//        int decorationMode = codeArea.getDecorationMode();
//        if ((decorationMode & CodeArea.DECORATION_HEADER_LINE) > 0) {
//            g.setColor(codeArea.getDecorationLineColor());
//            g.drawLine(compRect.x, codeRect.y - 1, compRect.x + compRect.width, codeRect.y - 1);
//        }
//        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
//            g.setColor(codeArea.getDecorationLineColor());
//            g.drawLine(codeRect.x - 1, codeRect.y - 1, codeRect.x + codeRect.width, codeRect.y - 1);
//        }
//        if ((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) {
//            int lineX = codeArea.getPreviewX() - scrollPosition.getScrollCharPosition() * codeArea.getCharWidth() - scrollPosition.getScrollCharOffset() - codeArea.getCharWidth() / 2;
//            if (lineX >= codeRect.x) {
//                g.setColor(codeArea.getDecorationLineColor());
//                g.drawLine(lineX, compRect.y, lineX, codeRect.y);
//            }
//        }
        g.setClip(clipBounds);
    }

    public void paintLineNumbers(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle lineNumbersArea = new Rectangle(0, state.headerAreaHeight, state.lineNumbersAreaWidth, state.areaHeight - state.headerAreaHeight); // TODO minus scrollbar height
        g.setClip(clipBounds != null ? lineNumbersArea.intersection(clipBounds) : lineNumbersArea);

        g.setColor(state.colors.background);
        g.fillRect(lineNumbersArea.x, lineNumbersArea.y, lineNumbersArea.width, lineNumbersArea.height);

        int lineNumberLength = state.lineNumbersLength;

        if (state.borderPaintMode == BorderPaintMode.STRIPED) {
            long dataPosition = state.scrollPosition.getScrollLinePosition() * state.bytesPerLine;
            int stripePositionY = state.headerAreaHeight + ((state.scrollPosition.getScrollLinePosition() & 1) > 0 ? 0 : state.lineHeight);
            g.setColor(state.colors.stripes);
            for (int line = 0; line <= state.linesPerRect / 2; line++) {
                if (dataPosition >= state.dataSize) {
                    break;
                }

                g.fillRect(0, stripePositionY, state.lineNumbersAreaWidth, state.lineHeight);
                stripePositionY += state.lineHeight * 2;
                dataPosition += state.bytesPerLine * 2;
            }
        }

        long dataPosition = state.bytesPerLine * state.scrollPosition.getScrollLinePosition();
        int positionY = state.headerAreaHeight + state.lineHeight - subFontSpace - state.scrollPosition.getScrollLineOffset();
        g.setColor(state.colors.foreground);
        Rectangle compRect = new Rectangle();
        for (int line = 0; line <= state.linesPerRect; line++) {
            if (dataPosition >= state.dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(state.lineNumberCode, 0, dataPosition < 0 ? 0 : dataPosition, state.codeType.getBase(), lineNumberLength, true, HexCharactersCase.UPPER);
            if (state.characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                g.drawChars(state.lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
            } else {
                for (int digitIndex = 0; digitIndex < lineNumberLength; digitIndex++) {
                    drawCenteredChar(g, state.lineNumberCode, digitIndex, state.characterWidth, compRect.x + state.characterWidth * digitIndex, positionY);
                }
            }

            positionY += state.lineHeight;
            dataPosition += state.bytesPerLine;
        }
        g.setClip(clipBounds);
    }

    @Override
    public void paintMainArea(Graphics g) {
        if (state == null) {
            reset();
        }
        if (state.font == null) {
            resetFont(g);
        }

        Rectangle clipBounds = g.getClipBounds();
        Rectangle mainArea = new Rectangle(state.lineNumbersAreaWidth, state.headerAreaHeight, state.areaWidth - state.lineNumbersAreaWidth - 20, state.areaHeight - state.headerAreaHeight - 20);
        g.setClip(clipBounds != null ? mainArea.intersection(clipBounds) : mainArea);
        paintBackground(g);
        paintLines(g);
        g.setClip(clipBounds);
        paintCursor(g);

        // TODO: Remove later
        int x = state.areaWidth - state.lineNumbersAreaWidth - 220;
        int y = state.areaHeight - state.headerAreaHeight - 20;
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 200, 16);
        g.setColor(Color.BLACK);
        char[] headerCode = (String.valueOf(state.scrollPosition.getScrollCharPosition()) + "+" + String.valueOf(state.scrollPosition.getScrollCharOffset()) + " : " + String.valueOf(state.scrollPosition.getScrollLinePosition()) + "+" + String.valueOf(state.scrollPosition.getScrollLineOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
        g.drawChars(headerCode, 0, headerCode.length, x, y + state.lineHeight);
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(Graphics g) {
        int linePositionX = state.lineNumbersAreaWidth;
        g.setColor(state.colors.background);
        if (borderPaintMode != BorderPaintMode.TRANSPARENT) {
            g.fillRect(linePositionX, state.headerAreaHeight, state.mainAreaWidth, state.mainAreaHeight);
        }

        if (borderPaintMode == BorderPaintMode.STRIPED) {
            long dataPosition = state.scrollPosition.getScrollLinePosition() * state.bytesPerLine;
            int stripePositionY = state.headerAreaHeight + (int) ((state.scrollPosition.getScrollLinePosition() & 1) > 0 ? 0 : state.lineHeight);
            g.setColor(state.colors.stripes);
            for (int line = 0; line <= state.linesPerRect / 2; line++) {
                if (dataPosition >= state.dataSize) {
                    break;
                }

                g.fillRect(linePositionX, stripePositionY, state.mainAreaWidth, state.lineHeight);
                stripePositionY += state.lineHeight * 2;
                dataPosition += state.bytesPerLine * 2;
            }
        }
    }

    public void paintLines(Graphics g) {
        Color randomColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
        g.setColor(randomColor);
        long dataPosition = state.scrollPosition.getScrollLinePosition() * state.bytesPerLine + state.scrollPosition.getLineDataOffset();
        int linePositionX = state.lineNumbersAreaWidth - state.scrollPosition.getScrollCharPosition() * state.characterWidth - state.scrollPosition.getScrollCharOffset();
        int linePositionY = state.headerAreaHeight + state.lineHeight - subFontSpace;
        g.setColor(Color.BLACK);
        for (int line = 0; line <= state.linesPerRect; line++) {
            prepareLineData(dataPosition);
            paintLineBackground(g, linePositionX, linePositionY);
            paintLineText(g, linePositionX, linePositionY);
//            CodeAreaUtils.longToBaseCode(lineNumberCode, 0, dataPosition < 0 ? 0 : dataPosition, 16, lineNumberLength, true, HexCharactersCase.UPPER);
//            if (state.characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
//            } else {
//                for (int digitIndex = 0; digitIndex < lineNumberLength; digitIndex++) {
//                    drawCenteredChar(g, lineNumberCode, digitIndex, state.characterWidth, compRect.x + state.characterWidth * digitIndex, positionY);
//                }
//            }

            linePositionY += state.lineHeight;
            dataPosition += state.bytesPerLine;
        }
    }

    private void prepareLineData(long dataPosition) {
        int lineBytesLimit = state.bytesPerLine;
        int lineStart = 0;
        if (dataPosition < state.dataSize) {
            int lineDataSize = state.bytesPerLine + state.maxCharLength - 1;
            if (dataPosition + lineDataSize > state.dataSize) {
                lineDataSize = (int) (state.dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                lineStart = (int) -dataPosition;
            }
            codeArea.getData().copyToArray(dataPosition + lineStart, state.lineData, lineStart, lineDataSize - lineStart);
            if (dataPosition + lineBytesLimit > state.dataSize) {
                lineBytesLimit = (int) (state.dataSize - dataPosition);
            }
        } else {
            lineBytesLimit = 0;
        }

        // Fill codes
        if (state.viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            for (int byteOnLine = Math.max(state.visibleCodeStart, lineStart); byteOnLine < Math.min(state.visibleCodeEnd, lineBytesLimit); byteOnLine++) {
                byte dataByte = state.lineData[byteOnLine];
                CodeAreaUtils.byteToCharsCode(dataByte, state.codeType, state.lineCharacters, computeFirstCodeCharPos(byteOnLine), state.hexCharactersCase);
            }
            if (state.bytesPerLine > lineBytesLimit) {
                Arrays.fill(state.lineCharacters, computePositionByte(lineBytesLimit), state.lineCharacters.length, ' ');
            }
        }

        // Fill preview characters
        if (state.viewMode != CodeAreaViewMode.CODE_MATRIX) {
            for (int byteOnLine = state.visiblePreviewStart; byteOnLine < Math.min(state.visiblePreviewEnd, lineBytesLimit); byteOnLine++) {
                byte dataByte = state.lineData[byteOnLine];

                if (state.maxCharLength > 1) {
                    if (dataPosition + state.maxCharLength > state.dataSize) {
                        state.maxCharLength = (int) (state.dataSize - dataPosition);
                    }

                    int charDataLength = state.maxCharLength;
                    if (byteOnLine + charDataLength > state.lineData.length) {
                        charDataLength = state.lineData.length - byteOnLine;
                    }
                    String displayString = new String(state.lineData, byteOnLine, charDataLength, state.charset);
                    if (!displayString.isEmpty()) {
                        state.lineCharacters[state.previewCharPos + byteOnLine] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != state.charset) {
                        buildCharMapping(state.charset);
                    }

                    state.lineCharacters[state.previewCharPos + byteOnLine] = charMapping[dataByte & 0xFF];
                }
            }
            if (state.bytesPerLine > lineBytesLimit) {
                Arrays.fill(state.lineCharacters, state.previewCharPos + lineBytesLimit, state.previewCharPos + state.bytesPerLine, ' ');
            }
        }

    }

    private void paintLineBackground(Graphics g, int linePositionX, int linePositionY) {
        int renderOffset = state.visibleCharStart;
        Color renderColor = null;
        for (int charOnLine = state.visibleCharStart; charOnLine < state.visibleCharEnd; charOnLine++) {
            CodeAreaSection section;
            int byteOnLine;
            if (charOnLine >= state.previewCharPos && state.viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnLine = charOnLine - state.previewCharPos;
                section = CodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnLine = computePositionByte(charOnLine);
                section = CodeAreaSection.CODE_MATRIX;
            }
            boolean sequenceBreak = false;

            Color color = null; //Color.getPositionColor(byteOnLine, charOnLine, section, colorType, paintData);
//            if (renderColorType == null) {
//                renderColorType = colorType;
//                renderColor = color;
//                g.setColor(color);
//            }

            if (!areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnLine) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnLine, linePositionX, linePositionY);
                    }
                }

                if (!areSameColors(color, renderColor)) {
                    renderColor = color;
                    g.setColor(color);
                }

                renderOffset = charOnLine;
            }
        }

        if (renderOffset < state.charactersPerLine) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, state.charactersPerLine, linePositionX, linePositionY);
            }
        }
    }

    private void paintLineText(Graphics g, int linePositionX, int linePositionY) {
        int positionY = linePositionY; // - codeArea.getSubFontSpace();

        g.setColor(Color.BLACK);
//        Rectangle dataViewRectangle = codeArea.getDataViewRectangle();
//        g.drawString("[" + String.valueOf(dataViewRectangle.x) + "," + String.valueOf(dataViewRectangle.y) + "," + String.valueOf(dataViewRectangle.width) + "," + String.valueOf(dataViewRectangle.height) + "]", linePositionX, positionY);

//    public void paintLineText(Graphics g, int linePositionX, int linePositionY, PaintDataCache paintData) {
//        int positionY = linePositionY + paintData.lineHeight - codeArea.getSubFontSpace();
//
        int renderOffset = state.visibleCharStart;
        CodeAreaColorType renderColorType = null;
        for (int charOnLine = state.visibleCharStart; charOnLine < state.visibleCharEnd; charOnLine++) {
            CodeAreaSection section;
            int byteOnLine;
            if (charOnLine >= state.previewCharPos) {
                byteOnLine = charOnLine - state.previewCharPos;
                section = CodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnLine = computePositionByte(charOnLine);
                section = CodeAreaSection.CODE_MATRIX;
            }
            boolean sequenceBreak = false;
            boolean nativeWidth = true;

            int currentCharWidth = 0;
            if (state.characterRenderingMode != CharacterRenderingMode.LINE_AT_ONCE) {
                char currentChar = state.lineCharacters[charOnLine];
                if (currentChar == ' ' && renderOffset == charOnLine) {
                    renderOffset++;
                    continue;
                }
                if (state.characterRenderingMode == CharacterRenderingMode.AUTO && state.monospaceFont) {
                    // Detect if character is in unicode range covered by monospace fonts
                    if (CodeAreaSwingUtils.isMonospaceFullWidthCharater(currentChar)) {
                        currentCharWidth = state.characterWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = state.fontMetrics.charWidth(currentChar);
                    nativeWidth = currentCharWidth == state.characterWidth;
                }
            } else {
                currentCharWidth = state.characterWidth;
            }

            if (!nativeWidth) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnLine) {
                    renderCharSequence(g, renderOffset, charOnLine, linePositionX, positionY);
                }

                if (!nativeWidth) {
                    renderOffset = charOnLine + 1;
                    if (state.characterRenderingMode == CharacterRenderingMode.TOP_LEFT) {
                        g.drawChars(state.lineCharacters, charOnLine, 1, linePositionX + charOnLine * state.characterWidth, positionY);
                    } else {
                        drawShiftedChar(g, state.lineCharacters, charOnLine, state.characterWidth, linePositionX + charOnLine * state.characterWidth, positionY, (state.characterWidth + 1 - currentCharWidth) >> 1);
                    }
                } else {
                    renderOffset = charOnLine;
                }
            }
        }

        if (renderOffset < state.charactersPerLine) {
            renderCharSequence(g, renderOffset, state.charactersPerLine, linePositionX, positionY);
        }
    }

    @Override
    public void paintCursor(Graphics g) {
        if (!codeArea.hasFocus()) {
            return;
        }

        CodeAreaCaret caret = codeArea.getCaret();
        int codeDigits = codeArea.getCodeType().getMaxDigitsForByte();
        int bytesPerLine = getBytesPerLine();
        int lineHeight = getLineHeight();
        int characterWidth = getCharacterWidth();
        int linesPerRect = getLinesPerRectangle();
        Point cursorPoint = caret.getCursorPoint(bytesPerLine, lineHeight, characterWidth, linesPerRect);
        boolean cursorVisible = caret.isCursorVisible();
        CodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();

        if (cursorVisible && cursorPoint != null) {
            cursorPoint.setLocation(cursorPoint.x + state.lineNumbersAreaWidth, cursorPoint.y + state.headerAreaHeight);
            g.setColor(state.colors.cursor);
            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
                g.setXORMode(Color.WHITE);
            }

            CodeAreaCaret.CursorShape cursorShape = codeArea.getEditationMode() == EditationMode.INSERT ? caret.getInsertCursorShape() : caret.getOverwriteCursorShape();
            int cursorThickness = 0;
            if (cursorShape.getWidth() != CodeAreaCaret.CursorShapeWidth.FULL) {
                cursorThickness = caret.getCursorThickness(cursorShape, characterWidth, lineHeight);
            }
            switch (cursorShape) {
                case LINE_TOP:
                case DOUBLE_TOP:
                case QUARTER_TOP:
                case HALF_TOP: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
                            characterWidth, cursorThickness, renderingMode);
                    break;
                }
                case LINE_BOTTOM:
                case DOUBLE_BOTTOM:
                case QUARTER_BOTTOM:
                case HALF_BOTTOM: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y + lineHeight - cursorThickness,
                            characterWidth, cursorThickness, renderingMode);
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
                    paintCursorRect(g, cursorPoint.x + characterWidth - cursorThickness, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
                    break;
                }
                case BOX: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
                            characterWidth, lineHeight, renderingMode);
                    break;
                }
                case FRAME: {
                    g.drawRect(cursorPoint.x, cursorPoint.y, characterWidth, lineHeight - 1);
                    break;
                }
                case BOTTOM_CORNERS:
                case CORNERS: {
                    int quarterWidth = characterWidth / 4;
                    int quarterLine = lineHeight / 4;
                    if (cursorShape == CodeAreaCaret.CursorShape.CORNERS) {
                        g.drawLine(cursorPoint.x, cursorPoint.y,
                                cursorPoint.x + quarterWidth, cursorPoint.y);
                        g.drawLine(cursorPoint.x + characterWidth - quarterWidth, cursorPoint.y,
                                cursorPoint.x + characterWidth, cursorPoint.y);

                        g.drawLine(cursorPoint.x, cursorPoint.y + 1,
                                cursorPoint.x, cursorPoint.y + quarterLine);
                        g.drawLine(cursorPoint.x + characterWidth, cursorPoint.y + 1,
                                cursorPoint.x + characterWidth, cursorPoint.y + quarterLine);
                    }

                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x, cursorPoint.y + lineHeight - 2);
                    g.drawLine(cursorPoint.x + characterWidth, cursorPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x + characterWidth, cursorPoint.y + lineHeight - 2);

                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - 1,
                            cursorPoint.x + quarterWidth, cursorPoint.y + lineHeight - 1);
                    g.drawLine(cursorPoint.x + characterWidth - quarterWidth, cursorPoint.y + lineHeight - 1,
                            cursorPoint.x + characterWidth, cursorPoint.y + lineHeight - 1);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected cursor shape type " + cursorShape.name());
                }
            }

            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
                g.setPaintMode();
            }
        }

        // Paint shadow cursor
        if (codeArea.getViewMode() == CodeAreaViewMode.DUAL && showShadowCursor) {
            g.setColor(state.colors.cursor);
            Point shadowCursorPoint = caret.getShadowCursorPoint(bytesPerLine, lineHeight, characterWidth, linesPerRect);
            shadowCursorPoint.setLocation(shadowCursorPoint.x + state.lineNumbersAreaWidth, shadowCursorPoint.y + state.headerAreaHeight);
            Graphics2D g2d = (Graphics2D) g.create();
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
            g2d.setStroke(dashed);
            g2d.drawRect(shadowCursorPoint.x, shadowCursorPoint.y,
                    characterWidth * (codeArea.getActiveSection() == CodeAreaSection.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
        }
    }

    private void paintCursorRect(Graphics g, int x, int y, int width, int height, CodeAreaCaret.CursorRenderingMode renderingMode) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRect(x, y, width, height);
                break;
            }
            case XOR: {
                Rectangle rect = new Rectangle(x, y, width, height);
                Rectangle intersection = rect.intersection(g.getClipBounds());
                if (!intersection.isEmpty()) {
                    g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
                }
                break;
            }
            case NEGATIVE: {
                Rectangle rect = new Rectangle(x, y, width, height);
                Rectangle clipBounds = g.getClipBounds();
                Rectangle intersection;
                if (clipBounds != null) {
                    intersection = rect.intersection(clipBounds);
                } else {
                    intersection = rect;
                }
                if (intersection.isEmpty()) {
                    break;
                }
                Shape clip = g.getClip();
                g.setClip(intersection.x, intersection.y, intersection.width, intersection.height);
                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
                g.fillRect(x, y, width, height);
                g.setColor(state.colors.negativeCursor);
                Rectangle codeRect = getDataViewRect();
                int previewX = getPreviewX();
                int charWidth = getCharacterWidth();
                int lineHeight = getLineHeight();
                int line = (y + scrollPosition.getScrollLineOffset() - codeRect.y) / lineHeight;
                int scrolledX = x + scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();
                int posY = codeRect.y + (line + 1) * lineHeight - subFontSpace - scrollPosition.getScrollLineOffset();
                if (codeArea.getViewMode() != CodeAreaViewMode.CODE_MATRIX && scrolledX >= previewX) {
                    int charPos = (scrolledX - previewX) / charWidth;
                    long dataSize = codeArea.getDataSize();
                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + charPos - scrollPosition.getLineDataOffset();
                    if (dataPosition >= dataSize) {
                        g.setClip(clip);
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

                        codeArea.getData().copyToArray(dataPosition, data, 0, charDataLength);
                        String displayString = new String(data, 0, charDataLength, charset);
                        if (!displayString.isEmpty()) {
                            previewChars[0] = displayString.charAt(0);
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != charset) {
                            buildCharMapping(charset);
                        }

                        previewChars[0] = charMapping[codeArea.getData().getByte(dataPosition) & 0xFF];
                    }
                    int posX = previewX + charPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
                    if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                        g.drawChars(previewChars, 0, 1, posX, posY);
                    } else {
                        drawCenteredChar(g, previewChars, 0, charWidth, posX, posY);
                    }
                } else {
                    int charPos = (scrolledX - codeRect.x) / charWidth;
                    int byteOffset = computePositionByte(charPos);
                    int codeCharPos = computeFirstCodeCharPos(byteOffset);
                    char[] lineChars = new char[codeArea.getCodeType().getMaxDigitsForByte()];
                    long dataSize = codeArea.getDataSize();
                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + byteOffset - scrollPosition.getLineDataOffset();
                    if (dataPosition >= dataSize) {
                        g.setClip(clip);
                        break;
                    }

                    byte dataByte = codeArea.getData().getByte(dataPosition);
                    CodeAreaUtils.byteToCharsCode(dataByte, codeArea.getCodeType(), lineChars, 0, hexCharactersCase);
                    int posX = codeRect.x + codeCharPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                        g.drawChars(lineChars, charsOffset, 1, posX + (charsOffset * charWidth), posY);
                    } else {
                        drawCenteredChar(g, lineChars, charsOffset, charWidth, posX + (charsOffset * charWidth), posY);
                    }
                }
                g.setClip(clip);
                break;
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
        int charWidth = state.fontMetrics.charWidth(drawnChars[charOffset]);
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, startX, positionY, (charWidthSpace + 1 - charWidth) >> 1);
    }

    protected void drawShiftedChar(Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY, int shift) {
        g.drawChars(drawnChars, charOffset, 1, startX + shift, positionY);
    }

    private void buildCharMapping(Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    @Override
    public int getPreviewX() {
        return computeFirstCodeCharPos(getBytesPerLine()) * getCharacterWidth();
    }

    @Override
    public int getPreviewFirstChar() {
        return computeLastCodeCharPos(getBytesPerLine());
    }

    @Override
    public void rebuildColors() {
    }

    @Override
    public int getBytesPerRectangle() {
        return 100;
    }

    @Override
    public int getLinesPerRectangle() {
        if (state.lineHeight == 0) {
            return 0;
        }

        return state.areaHeight / state.lineHeight + 1;
    }

    @Override
    public int getBytesPerLine() {
        return 16;
    }

    @Override
    public int getCharactersPerLine() {
        return state.charactersPerLine;
    }

    @Override
    public int getLineHeight() {
        return state.lineHeight;
    }

    @Override
    public int getCharacterWidth() {
        return state.characterWidth;
    }

    @Override
    public int computePositionByte(int lineCharPosition) {
        return lineCharPosition / (state.maxDigits + 1);
    }

    @Override
    public int computeCodeAreaCharacter(int pixelX) {
        return pixelX / getCharacterWidth();
    }

    @Override
    public int computeCodeAreaLine(int pixelY) {
        return pixelY / getLineHeight();
    }

    @Override
    public int computeFirstCodeCharPos(int byteOffset) {
        return byteOffset * (state.maxDigits + 1);
    }

    @Override
    public int computeLastCodeCharPos(int byteOffset) {
        return computeFirstCodeCharPos(byteOffset + 1) - 2;
    }

    @Override
    public long cursorPositionToDataPosition(long line, int byteOffset) throws OutOfBoundsException {
        return 16;
    }

    @Override
    public CaretPosition mousePositionToCaretPosition(int mouseX, int mouseY) {
        CodeAreaPainter painter = codeArea.getPainter();
        Rectangle hexRect = codeArea.getDataViewRectangle();
        CodeAreaViewMode viewMode = codeArea.getViewMode();
        CodeType codeType = codeArea.getCodeType();
        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
        CodeAreaCaret caret = codeArea.getCaret();
        int bytesPerLine = codeArea.getBytesPerLine();
        if (mouseX < hexRect.x) {
            mouseX = hexRect.x;
        }
        int cursorCharX = codeArea.computeCodeAreaCharacter(mouseX - hexRect.x + scrollPosition.getScrollCharOffset()) + scrollPosition.getScrollCharPosition();
        long cursorLineY = codeArea.computeCodeAreaLine(mouseY - hexRect.y + scrollPosition.getScrollLineOffset()) + scrollPosition.getScrollLinePosition();
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnLine;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < painter.getPreviewFirstChar()) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(CodeAreaSection.CODE_MATRIX);
            byteOnLine = painter.computePositionByte(cursorCharX);
            if (byteOnLine >= bytesPerLine) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - painter.computeFirstCodeCharPos(byteOnLine);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(CodeAreaSection.TEXT_PREVIEW);
            byteOnLine = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnLine -= painter.getPreviewFirstChar();
            }
        }

        if (byteOnLine >= bytesPerLine) {
            byteOnLine = bytesPerLine - 1;
        }

        dataPosition = byteOnLine + (cursorLineY * bytesPerLine) - scrollPosition.getLineDataOffset();
        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        long dataSize = codeArea.getDataSize();
        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, codeOffset);

        return caretPosition;
    }

    @Override
    public Rectangle getDataViewRect() {
        // TODO cache
        return new Rectangle(state.lineNumbersAreaWidth, state.headerAreaHeight, state.areaWidth - state.lineNumbersAreaWidth, state.areaHeight - state.headerAreaHeight);
    }

    private int getLineNumberLength() {
        return 8;
    }

    private static boolean areSameColors(Color color, Color comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    /**
     * Render sequence of characters.
     *
     * Doesn't include character at offset end.
     */
    private void renderCharSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY) {
        g.drawChars(state.lineCharacters, startOffset, endOffset - startOffset, linePositionX + startOffset * state.characterWidth, positionY);
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY) {
        g.fillRect(linePositionX + startOffset * state.characterWidth, positionY, (endOffset - startOffset) * state.characterWidth, state.lineHeight);
    }

    private static Color createOddColor(Color color) {
        return new Color(
                computeOddColorComponent(color.getRed()),
                computeOddColorComponent(color.getGreen()),
                computeOddColorComponent(color.getBlue()));
    }

    private static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? - 16 : 16);
    }

    private static Color createNegativeColor(Color color) {
        return new Color(
                255 - color.getRed(),
                255 - color.getGreen(),
                255 - color.getBlue());
    }

    private static class Colors {

        Color foreground;
        Color background;
        Color selectionForeground;
        Color selectionBackground;
        Color selectionMirrorForeground;
        Color selectionMirrorBackground;
        Color cursor;
        Color negativeCursor;
        Color cursorMirror;
        Color negativeCursorMirror;
        Color decorationLine;
        Color stripes;
    }

    private static class PainterState {

        boolean monospaceFont;
        int characterWidth;

        int areaWidth;
        int areaHeight;
        int mainAreaX;
        int mainAreaY;
        int mainAreaWidth;
        int mainAreaHeight;
        CodeAreaViewMode viewMode;
        CodeAreaScrollPosition scrollPosition;
        CodeArea.VerticalScrollUnit verticalScrollUnit;
        CodeArea.HorizontalScrollUnit horizontalScrollUnit;
        Colors colors = new Colors();
        long dataSize;

        int lineNumbersLength;
        int lineNumbersAreaWidth;
        int headerAreaHeight;
        int lineHeight;
        int linesPerRect;
        int bytesPerLine;
        int charactersPerRect;
        int charactersPerLine;
        CodeType codeType;
        int maxDigits;
        HexCharactersCase hexCharactersCase;
        BorderPaintMode borderPaintMode;

        int previewCharPos;
        int visibleCharStart;
        int visibleCharEnd;
        int visiblePreviewStart;
        int visiblePreviewEnd;
        int visibleCodeStart;
        int visibleCodeEnd;

        Charset charset;
        Font font;
        FontMetrics fontMetrics;
        CharacterRenderingMode characterRenderingMode;
        int maxCharLength;

        byte[] lineData;
        char[] lineNumberCode;
        char[] lineCharacters;
    }

    public static enum BorderPaintMode {
        TRANSPARENT,
        PLAIN,
        STRIPED
    }
}
