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

import org.exbin.deltahex.swing.color.CodeAreaColorProfile;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeAreaUtils;
import org.exbin.deltahex.HexCharactersCase;
import org.exbin.deltahex.CodeAreaViewMode;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.swing.color.CodeAreaColorType;
import org.exbin.utils.binary_data.OutOfBoundsException;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2017/08/23
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    protected final CodeArea codeArea;
    private CodeAreaColorProfile colorProfile = null;
    private int subFontSpace = 3;

    private PainterState state = null;

    private CharacterRenderingMode characterRenderingMode = CharacterRenderingMode.AUTO;
    private Charset charMappingCharset = null;
    private final char[] charMapping = new char[256];

    public DefaultCodeAreaPainter(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void reset() {
        if (state == null) {
            state = new PainterState();
        }

        state.areaWidth = codeArea.getWidth();
        state.areaHeight = codeArea.getHeight();
        resetScrollState();

        state.viewMode = codeArea.getViewMode();
        state.characterRenderingMode = characterRenderingMode;

        state.linesPerRect = getLinesPerRectangle();
        state.bytesPerLine = getBytesPerLine();
        state.charactersPerLine = getCharactersPerLine();
        state.codeType = codeArea.getCodeType();
        state.maxDigits = state.codeType.getMaxDigits();
        state.hexCharactersCase = HexCharactersCase.UPPER;
        state.dataSize = codeArea.getDataSize();
    }

    private void resetCharPositions() {
        state.charactersPerRect = computeLastCharPos(state.bytesPerLine - 1);
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
        updateSizes();
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
            paintOutsiteArea(g);
            paintHeader(g);
            paintLineNumbers(g);
        }
    }

    private void resetScrollState() {
        state.scrollPosition = codeArea.getScrollPosition();
    }

    private void updateSizes() {
        state.lineNumbersAreaWidth = state.characterWidth * (state.lineNumbersLength + 1);
        state.headerAreaHeight = 20;
        state.mainAreaWidth = state.areaWidth - state.lineNumbersAreaWidth;
        state.mainAreaHeight = state.areaHeight - state.headerAreaHeight;
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
    }

    public void paintOutsiteArea(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, state.headerAreaHeight - 1, state.lineNumbersAreaWidth, 1);
    }

    public void paintHeader(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle headerArea = new Rectangle(state.lineNumbersAreaWidth, 0, state.areaWidth - state.lineNumbersAreaWidth, state.headerAreaHeight); // TODO minus scrollbar width
        g.setClip(clipBounds != null ? headerArea.intersection(clipBounds) : headerArea);

        Color randomColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
        g.setColor(randomColor);
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        // Black line
        g.setColor(Color.BLACK);
        g.fillRect(0, state.headerAreaHeight - 1, state.areaWidth, 1);

        g.setColor(Color.BLACK);
        char[] headerCode = (String.valueOf(state.scrollPosition.getScrollCharPosition()) + "+" + String.valueOf(state.scrollPosition.getScrollCharOffset()) + " : " + String.valueOf(state.scrollPosition.getScrollLinePosition()) + "+" + String.valueOf(state.scrollPosition.getScrollLineOffset())).toCharArray();
        g.drawChars(headerCode, 0, headerCode.length, 100, state.lineHeight);
        g.setClip(clipBounds);
    }

    public void paintLineNumbers(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle lineNumbersArea = new Rectangle(0, state.headerAreaHeight, state.lineNumbersAreaWidth, state.areaHeight - state.headerAreaHeight); // TODO minus scrollbar height
        g.setClip(clipBounds != null ? lineNumbersArea.intersection(clipBounds) : lineNumbersArea);

        Color randomColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
        g.setColor(randomColor);
        g.fillRect(lineNumbersArea.x, lineNumbersArea.y, lineNumbersArea.width, lineNumbersArea.height);

        int lineNumberLength = state.lineNumbersLength;
        long dataPosition = state.bytesPerLine * state.scrollPosition.getScrollLinePosition();

        int stripPositionY = state.headerAreaHeight + state.lineHeight;
        g.setColor(Color.LIGHT_GRAY);
        for (int line = 0; line < state.linesPerRect; line += 2) {
            g.fillRect(0, stripPositionY, state.lineNumbersAreaWidth, state.lineHeight);
            stripPositionY += state.lineHeight * 2;
        }

        int positionY = state.headerAreaHeight + state.lineHeight - subFontSpace - state.scrollPosition.getScrollLineOffset();
        g.setColor(Color.BLACK);
        Rectangle compRect = new Rectangle();
        for (int line = 0; line < state.linesPerRect; line++) {
            CodeAreaUtils.longToBaseCode(state.lineNumberCode, 0, dataPosition < 0 ? 0 : dataPosition, 16, lineNumberLength, true, HexCharactersCase.UPPER);
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

        paintLines(g);
    }

    private void paintLines(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Color randomColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
        g.setColor(randomColor);
        if (clipBounds != null) {
            g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        } else {
            g.fillRect(0, 0, state.areaWidth, state.areaHeight);
        }

        int positionY = state.lineHeight - (int) (state.scrollPosition.getScrollLinePosition() * state.lineHeight); //codeRect.y - codeArea.getSubFontSpace() - scrollPosition.getScrollLineOffset() + codeArea.getLineHeight();
        g.setColor(Color.LIGHT_GRAY);
        for (int line = 0; line < state.linesPerRect; line += 2) {
            g.fillRect(0, positionY, state.mainAreaWidth, state.lineHeight);
            positionY += state.lineHeight * 2;
        }

        long dataPosition = 0;
        int linePositionY = state.lineHeight - subFontSpace - (int) (state.scrollPosition.getScrollLinePosition() * (state.lineHeight - 1));
        g.setColor(Color.BLACK);
        for (int line = 0; line < state.linesPerRect; line++) {
            prepareLineData(dataPosition);
            paintLineBackground(g, 0, linePositionY);
            paintLineText(g, 0, linePositionY);
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
                CodeAreaUtils.byteToCharsCode(dataByte, state.codeType, state.lineCharacters, computeFirstCharPos(byteOnLine), state.hexCharactersCase);
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
//        int codeDigits = codeArea.getCodeType().getMaxDigits();
//        Point cursorPoint = caret.getCursorPoint(bytesPerLine, lineHeight, charWidth, linesPerRect);
//        boolean cursorVisible = caret.isCursorVisible();
//        CodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
//
//        if (cursorVisible && cursorPoint != null) {
//            g.setColor(codeArea.getCursorColor());
//            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
//                g.setXORMode(Color.WHITE);
//            }
//
//            CodeAreaCaret.CursorShape cursorShape = codeArea.getEditationMode() == EditationMode.INSERT ? caret.getInsertCursorShape() : caret.getOverwriteCursorShape();
//            int cursorThickness = 0;
//            if (cursorShape.getWidth() != CodeAreaCaret.CursorShapeWidth.FULL) {
//                cursorThickness = caret.getCursorThickness(cursorShape, charWidth, lineHeight);
//            }
//            switch (cursorShape) {
//                case LINE_TOP:
//                case DOUBLE_TOP:
//                case QUARTER_TOP:
//                case HALF_TOP: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
//                            charWidth, cursorThickness, renderingMode);
//                    break;
//                }
//                case LINE_BOTTOM:
//                case DOUBLE_BOTTOM:
//                case QUARTER_BOTTOM:
//                case HALF_BOTTOM: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y + lineHeight - cursorThickness,
//                            charWidth, cursorThickness, renderingMode);
//                    break;
//                }
//                case LINE_LEFT:
//                case DOUBLE_LEFT:
//                case QUARTER_LEFT:
//                case HALF_LEFT: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
//                    break;
//                }
//                case LINE_RIGHT:
//                case DOUBLE_RIGHT:
//                case QUARTER_RIGHT:
//                case HALF_RIGHT: {
//                    paintCursorRect(g, cursorPoint.x + charWidth - cursorThickness, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
//                    break;
//                }
//                case BOX: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
//                            charWidth, lineHeight, renderingMode);
//                    break;
//                }
//                case FRAME: {
//                    g.drawRect(cursorPoint.x, cursorPoint.y, charWidth, lineHeight - 1);
//                    break;
//                }
//                case BOTTOM_CORNERS:
//                case CORNERS: {
//                    int quarterWidth = charWidth / 4;
//                    int quarterLine = lineHeight / 4;
//                    if (cursorShape == CodeAreaCaret.CursorShape.CORNERS) {
//                        g.drawLine(cursorPoint.x, cursorPoint.y,
//                                cursorPoint.x + quarterWidth, cursorPoint.y);
//                        g.drawLine(cursorPoint.x + charWidth - quarterWidth, cursorPoint.y,
//                                cursorPoint.x + charWidth, cursorPoint.y);
//
//                        g.drawLine(cursorPoint.x, cursorPoint.y + 1,
//                                cursorPoint.x, cursorPoint.y + quarterLine);
//                        g.drawLine(cursorPoint.x + charWidth, cursorPoint.y + 1,
//                                cursorPoint.x + charWidth, cursorPoint.y + quarterLine);
//                    }
//
//                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - quarterLine - 1,
//                            cursorPoint.x, cursorPoint.y + lineHeight - 2);
//                    g.drawLine(cursorPoint.x + charWidth, cursorPoint.y + lineHeight - quarterLine - 1,
//                            cursorPoint.x + charWidth, cursorPoint.y + lineHeight - 2);
//
//                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - 1,
//                            cursorPoint.x + quarterWidth, cursorPoint.y + lineHeight - 1);
//                    g.drawLine(cursorPoint.x + charWidth - quarterWidth, cursorPoint.y + lineHeight - 1,
//                            cursorPoint.x + charWidth, cursorPoint.y + lineHeight - 1);
//                    break;
//                }
//                default: {
//                    throw new IllegalStateException("Unexpected cursor shape type " + cursorShape.name());
//                }
//            }
//
//            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
//                g.setPaintMode();
//            }
//        }
//
//        // Paint shadow cursor
//        if (codeArea.getViewMode() == ViewMode.DUAL && codeArea.isShowShadowCursor()) {
//            g.setColor(codeArea.getCursorColor());
//            Point shadowCursorPoint = caret.getShadowCursorPoint(bytesPerLine, lineHeight, charWidth, linesPerRect);
//            if (shadowCursorPoint != null) {
//                Graphics2D g2d = (Graphics2D) g.create();
//                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
//                g2d.setStroke(dashed);
//                g2d.drawRect(shadowCursorPoint.x, shadowCursorPoint.y,
//                        charWidth * (codeArea.getActiveSection() == Section.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
//            }
//        }
//    }
//
//    private void paintCursorRect(Graphics g, int x, int y, int width, int height, CodeAreaCaret.CursorRenderingMode renderingMode) {
//        switch (renderingMode) {
//            case PAINT: {
//                g.fillRect(x, y, width, height);
//                break;
//            }
//            case XOR: {
//                Rectangle rect = new Rectangle(x, y, width, height);
//                Rectangle intersection = rect.intersection(g.getClipBounds());
//                if (!intersection.isEmpty()) {
//                    g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
//                }
//                break;
//            }
//            case NEGATIVE: {
//                Rectangle rect = new Rectangle(x, y, width, height);
//                Rectangle intersection = rect.intersection(g.getClipBounds());
//                if (intersection.isEmpty()) {
//                    break;
//                }
//                Shape clip = g.getClip();
//                g.setClip(intersection.x, intersection.y, intersection.width, intersection.height);
//                CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//                g.fillRect(x, y, width, height);
//                g.setColor(codeArea.getNegativeCursorColor());
//                Rectangle codeRect = codeArea.getCodeSectionRectangle();
//                int previewX = codeArea.getPreviewX();
//                int charWidth = codeArea.getCharWidth();
//                int lineHeight = codeArea.getLineHeight();
//                int line = (y + scrollPosition.getScrollLineOffset() - codeRect.y) / lineHeight;
//                int scrolledX = x + scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();
//                int posY = codeRect.y + (line + 1) * lineHeight - codeArea.getSubFontSpace() - scrollPosition.getScrollLineOffset();
//                if (codeArea.getViewMode() != ViewMode.CODE_MATRIX && scrolledX >= previewX) {
//                    int charPos = (scrolledX - previewX) / charWidth;
//                    long dataSize = codeArea.getDataSize();
//                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + charPos - scrollPosition.getLineByteShift();
//                    if (dataPosition >= dataSize) {
//                        g.setClip(clip);
//                        break;
//                    }
//
//                    char[] previewChars = new char[1];
//                    Charset charset = codeArea.getCharset();
//                    CharsetEncoder encoder = charset.newEncoder();
//                    int maxCharLength = (int) encoder.maxBytesPerChar();
//                    byte[] data = new byte[maxCharLength];
//
//                    if (maxCharLength > 1) {
//                        int charDataLength = maxCharLength;
//                        if (dataPosition + maxCharLength > dataSize) {
//                            charDataLength = (int) (dataSize - dataPosition);
//                        }
//
//                        codeArea.getData().copyToArray(dataPosition, data, 0, charDataLength);
//                        String displayString = new String(data, 0, charDataLength, charset);
//                        if (!displayString.isEmpty()) {
//                            previewChars[0] = displayString.charAt(0);
//                        }
//                    } else {
//                        if (charMappingCharset == null || charMappingCharset != charset) {
//                            buildCharMapping(charset);
//                        }
//
//                        previewChars[0] = charMapping[codeArea.getData().getByte(dataPosition) & 0xFF];
//                    }
//
//                    if (codeArea.isShowUnprintableCharacters()) {
//                        if (unprintableCharactersMapping == null) {
//                            buildUnprintableCharactersMapping();
//                        }
//                        Character replacement = unprintableCharactersMapping.get(previewChars[0]);
//                        if (replacement != null) {
//                            previewChars[0] = replacement;
//                        }
//                    }
//                    int posX = previewX + charPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
//                    if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                        g.drawChars(previewChars, 0, 1, posX, posY);
//                    } else {
//                        drawCenteredChar(g, previewChars, 0, charWidth, posX, posY);
//                    }
//                } else {
//                    int charPos = (scrolledX - codeRect.x) / charWidth;
//                    int byteOffset = codeArea.computeByteOffsetPerCodeCharOffset(charPos);
//                    int codeCharPos = codeArea.computeByteCharPos(byteOffset);
//                    char[] lineChars = new char[codeArea.getCodeType().getMaxDigits()];
//                    long dataSize = codeArea.getDataSize();
//                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + byteOffset - scrollPosition.getLineByteShift();
//                    if (dataPosition >= dataSize) {
//                        g.setClip(clip);
//                        break;
//                    }
//
//                    byte dataByte = codeArea.getData().getByte(dataPosition);
//                    CodeAreaUtils.byteToCharsCode(dataByte, codeArea.getCodeType(), lineChars, 0, codeArea.getHexCharactersCase());
//                    int posX = codeRect.x + codeCharPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
//                    int charsOffset = charPos - codeCharPos;
//                    if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                        g.drawChars(lineChars, charsOffset, 1, posX + (charsOffset * charWidth), posY);
//                    } else {
//                        drawCenteredChar(g, lineChars, charsOffset, charWidth, posX + (charsOffset * charWidth), posY);
//                    }
//                }
//                g.setClip(clip);
//                break;
//            }
//        }
//    }
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
        return 0;
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
        return 128;
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
        return 16;
    }

    @Override
    public int computeCodeAreaLine(int pixelY) {
        return 16;
    }

    @Override
    public int computeFirstCharPos(int byteOffset) {
        return byteOffset * (state.maxDigits + 1);
    }

    @Override
    public int computeLastCharPos(int byteOffset) {
        return computeFirstCharPos(byteOffset + 1) - 2;
    }

    @Override
    public long cursorPositionToDataPosition(long line, int byteOffset) throws OutOfBoundsException {
        return 16;
    }

    @Override
    public CaretPosition mousePositionToCaretPosition(long mouseX, long mouseY) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rectangle getDataViewRect() {
        // TODO cache
        return new Rectangle(state.lineNumbersAreaWidth, state.headerAreaHeight, state.areaWidth - state.lineNumbersAreaWidth, state.areaHeight - state.headerAreaHeight);
    }

    private int getLineNumberLength() {
        return 8;
    }

//    @Override
//    public void paintOverall(Graphics g) {
//        // Fill header area background
//        Rectangle compRect = codeArea.getComponentRectangle();
//        Rectangle codeRect = codeArea.getCodeSectionRectangle();
//        if (compRect.y < codeRect.y) {
//            g.setColor(codeArea.getBackground());
//            g.fillRect(compRect.x, compRect.y, compRect.x + compRect.width, codeRect.y - compRect.y);
//        }
//    }
//
//    @Override
//    public void paintBackground(Graphics g) {
//        Rectangle clipBounds = g.getClipBounds();
//        Rectangle codeRect = codeArea.getCodeSectionRectangle();
//        CodeAreaColorsGroup mainColors = codeArea.getMainColors();
//        CodeAreaColorsGroup stripColors = codeArea.getAlternateColors();
//        int bytesPerLine = codeArea.getBytesPerLine();
//        int lineHeight = codeArea.getLineHeight();
//        int startX = clipBounds.x;
//        int width = clipBounds.width;
//        if (!codeArea.isLineNumberBackground() && codeArea.isShowLineNumbers()) {
//            int lineNumberWidth = codeRect.x - 1 - codeArea.getLineNumberSpace() / 2;
//            if (startX < lineNumberWidth) {
//                int diff = lineNumberWidth - startX;
//                startX = lineNumberWidth;
//                width -= diff;
//            }
//        }
//        if (codeArea.getBackgroundMode() != CodeArea.BackgroundMode.NONE) {
//            g.setColor(mainColors.getBackgroundColor());
//            g.fillRect(startX, clipBounds.y, width, clipBounds.height);
//        }
//
//        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//        long line = scrollPosition.getScrollLinePosition();
//    }
//
//
//    @Override
//    public void paintMainArea(Graphics g) {
//        PaintDataCache paintData = new PaintDataCache(codeArea);
//        paintMainArea(g, paintData);
//    }
//
//    public void paintMainArea(Graphics g, PaintDataCache paintData) {
//        int positionY = paintData.codeSectionRect.y - paintData.scrollPosition.getScrollLineOffset();
//        paintData.line = paintData.scrollPosition.getScrollLinePosition();
//        int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.getScrollCharPosition() * paintData.charWidth - paintData.scrollPosition.getScrollCharOffset();
//        paintData.lineDataPosition = paintData.line * paintData.bytesPerLine - paintData.scrollPosition.getLineByteShift();
//        long dataSize = codeArea.getDataSize();
//
//        do {
//            if (paintData.showUnprintableCharacters) {
//                Arrays.fill(paintData.unprintableChars, ' ');
//            }
//            int lineBytesLimit = paintData.bytesPerLine;
//            if (paintData.lineDataPosition < dataSize) {
//                int lineDataSize = paintData.bytesPerLine + paintData.maxCharLength - 1;
//                if (paintData.lineDataPosition + lineDataSize > dataSize) {
//                    lineDataSize = (int) (dataSize - paintData.lineDataPosition);
//                }
//                if (paintData.lineDataPosition < 0) {
//                    paintData.lineStart = (int) -paintData.lineDataPosition;
//                } else {
//                    paintData.lineStart = 0;
//                }
//                codeArea.getData().copyToArray(paintData.lineDataPosition + paintData.lineStart, paintData.lineData, paintData.lineStart, lineDataSize - paintData.lineStart);
//                if (paintData.lineDataPosition + lineBytesLimit > dataSize) {
//                    lineBytesLimit = (int) (dataSize - paintData.lineDataPosition);
//                }
//            } else {
//                lineBytesLimit = 0;
//            }
//
//            // Fill codes
//            if (paintData.viewMode != ViewMode.TEXT_PREVIEW) {
//                for (int byteOnLine = Math.max(paintData.visibleCodeStart, paintData.lineStart); byteOnLine < Math.min(paintData.visibleCodeEnd, lineBytesLimit); byteOnLine++) {
//                    byte dataByte = paintData.lineData[byteOnLine];
//                    CodeAreaUtils.byteToCharsCode(dataByte, codeArea.getCodeType(), paintData.lineChars, codeArea.computeByteCharPos(byteOnLine), codeArea.getHexCharactersCase());
//                }
//                if (paintData.bytesPerLine > lineBytesLimit) {
//                    Arrays.fill(paintData.lineChars, codeArea.computeByteCharPos(lineBytesLimit), paintData.lineChars.length, ' ');
//                }
//            }
//
//            // Fill preview characters
//            if (paintData.viewMode != ViewMode.CODE_MATRIX) {
//                for (int byteOnLine = paintData.visiblePreviewStart; byteOnLine < Math.min(paintData.visiblePreviewEnd, lineBytesLimit); byteOnLine++) {
//                    byte dataByte = paintData.lineData[byteOnLine];
//
//                    if (paintData.maxCharLength > 1) {
//                        if (paintData.lineDataPosition + paintData.maxCharLength > dataSize) {
//                            paintData.maxCharLength = (int) (dataSize - paintData.lineDataPosition);
//                        }
//
//                        int charDataLength = paintData.maxCharLength;
//                        if (byteOnLine + charDataLength > paintData.lineData.length) {
//                            charDataLength = paintData.lineData.length - byteOnLine;
//                        }
//                        String displayString = new String(paintData.lineData, byteOnLine, charDataLength, paintData.charset);
//                        if (!displayString.isEmpty()) {
//                            paintData.lineChars[paintData.previewCharPos + byteOnLine] = displayString.charAt(0);
//                        }
//                    } else {
//                        if (charMappingCharset == null || charMappingCharset != paintData.charset) {
//                            buildCharMapping(paintData.charset);
//                        }
//
//                        paintData.lineChars[paintData.previewCharPos + byteOnLine] = charMapping[dataByte & 0xFF];
//                    }
//
//                    if (paintData.showUnprintableCharacters || paintData.charRenderingMode == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                        if (unprintableCharactersMapping == null) {
//                            buildUnprintableCharactersMapping();
//                        }
//                        Character replacement = unprintableCharactersMapping.get(paintData.lineChars[paintData.previewCharPos + byteOnLine]);
//                        if (replacement != null) {
//                            if (paintData.showUnprintableCharacters) {
//                                paintData.unprintableChars[paintData.previewCharPos + byteOnLine] = replacement;
//                            }
//                            paintData.lineChars[paintData.previewCharPos + byteOnLine] = ' ';
//                        }
//                    }
//                }
//                if (paintData.bytesPerLine > lineBytesLimit) {
//                    Arrays.fill(paintData.lineChars, paintData.previewCharPos + lineBytesLimit, paintData.previewCharPos + paintData.bytesPerLine, ' ');
//                }
//            }
//            paintLineBackground(g, positionX, positionY, paintData);
//            paintLineText(g, positionX, positionY, paintData);
//            paintData.lineDataPosition += paintData.bytesPerLine;
//            paintData.line++;
//            positionY += paintData.lineHeight;
//        } while (positionY - paintData.lineHeight < paintData.codeSectionRect.y + paintData.codeSectionRect.height);
//    }
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

//    @Override
//    public void paintCursor() {
//        int bytesPerLine = codeArea.getBytesPerLine();
//        if (bytesPerLine > 0) {
//            int lineHeight = codeArea.getLineHeight();
//            int charWidth = codeArea.getCharWidth();
//            int linesPerRect = codeArea.getLinesPerRect();
//            Rectangle cursorRect = getCursorRect(bytesPerLine, lineHeight, charWidth, linesPerRect);
//            if (cursorRect != null) {
//                codeArea.paintImmediately(cursorRect);
//            }
//        }
//    }
//
//    @Override
//    public void clearCache() {
//        computePaintData();
//        validateLineOffset();
//    }
//
//    @Override
//    public int getPositionByte(int lineCharPosition) {
//        int positionByte;
//        if (codeArea.getActiveSection() == CodeAreaSection.CODE_MATRIX) {
//            positionByte = computeByteCharPos(lineCharPosition) + codeArea.getCaret().getCodeOffset();
//        } else {
//            positionByte = lineCharPosition;
//            if (codeArea.getViewMode() == ViewMode.DUAL) {
//                positionByte += paintDataCache.previewStartChar;
//            }
//        }
//
//        return positionByte;
//    }
//
//    private void computePaintData() {
//        if (paintDataCache.fontMetrics == null) {
//            return;
//        }
//
//        boolean verticalScrollBarVisible;
//        boolean horizontalScrollBarVisible;
//
//        Insets insets = getInsets();
//        Dimension size = getSize();
//        Rectangle compRect = paintDataCache.componentRectangle;
//        compRect.x = insets.left;
//        compRect.y = insets.top;
//        compRect.width = size.width - insets.left - insets.right;
//        compRect.height = size.height - insets.top - insets.bottom;
//
//        switch (lineNumberLength.getLineNumberType()) {
//            case AUTO: {
//                long dataSize = getDataSize();
//                if (dataSize > 0) {
//                    double natLog = Math.log(dataSize);
//                    paintDataCache.lineNumbersLength = (int) Math.ceil(natLog / positionCodeType.getBaseLog());
//                    if (paintDataCache.lineNumbersLength == 0) {
//                        paintDataCache.lineNumbersLength = 1;
//                    }
//                } else {
//                    paintDataCache.lineNumbersLength = 1;
//                }
//                break;
//            }
//            case SPECIFIED: {
//                paintDataCache.lineNumbersLength = lineNumberLength.getLineNumberLength();
//                break;
//            }
//        }
//
//        int charsPerRect = computeCharsPerRect(compRect.width);
//        int bytesPerLine;
//        if (wrapMode) {
//            bytesPerLine = computeFittingBytes(charsPerRect);
//            if (bytesPerLine == 0) {
//                bytesPerLine = 1;
//            }
//        } else {
//            bytesPerLine = lineLength;
//        }
//        long lines = ((data.getDataSize() + scrollPosition.lineByteShift) / bytesPerLine) + 1;
//        CodeAreaSpace.SpaceType headerSpaceType = headerSpace.getSpaceType();
//        switch (headerSpaceType) {
//            case NONE: {
//                paintDataCache.headerSpace = 0;
//                break;
//            }
//            case SPECIFIED: {
//                paintDataCache.headerSpace = headerSpace.getSpaceSize();
//                break;
//            }
//            case QUARTER_UNIT: {
//                paintDataCache.headerSpace = paintDataCache.lineHeight / 4;
//                break;
//            }
//            case HALF_UNIT: {
//                paintDataCache.headerSpace = paintDataCache.lineHeight / 2;
//                break;
//            }
//            case ONE_UNIT: {
//                paintDataCache.headerSpace = paintDataCache.lineHeight;
//                break;
//            }
//            case ONE_AND_HALF_UNIT: {
//                paintDataCache.headerSpace = (int) (paintDataCache.lineHeight * 1.5f);
//                break;
//            }
//            case DOUBLE_UNIT: {
//                paintDataCache.headerSpace = paintDataCache.lineHeight * 2;
//                break;
//            }
//            default:
//                throw new IllegalStateException("Unexpected header space type " + headerSpaceType.name());
//        }
//
//        CodeAreaSpace.SpaceType lineNumberSpaceType = lineNumberSpace.getSpaceType();
//        switch (lineNumberSpaceType) {
//            case NONE: {
//                paintDataCache.lineNumberSpace = 0;
//                break;
//            }
//            case SPECIFIED: {
//                paintDataCache.lineNumberSpace = lineNumberSpace.getSpaceSize();
//                break;
//            }
//            case QUARTER_UNIT: {
//                paintDataCache.lineNumberSpace = paintDataCache.charWidth / 4;
//                break;
//            }
//            case HALF_UNIT: {
//                paintDataCache.lineNumberSpace = paintDataCache.charWidth / 2;
//                break;
//            }
//            case ONE_UNIT: {
//                paintDataCache.lineNumberSpace = paintDataCache.charWidth;
//                break;
//            }
//            case ONE_AND_HALF_UNIT: {
//                paintDataCache.lineNumberSpace = (int) (paintDataCache.charWidth * 1.5f);
//                break;
//            }
//            case DOUBLE_UNIT: {
//                paintDataCache.lineNumberSpace = paintDataCache.charWidth * 2;
//                break;
//            }
//            default:
//                throw new IllegalStateException("Unexpected line number space type " + lineNumberSpaceType.name());
//        }
//
//        Rectangle hexRect = paintDataCache.codeSectionRectangle;
//        hexRect.y = insets.top + (showHeader ? paintDataCache.lineHeight + paintDataCache.headerSpace : 0);
//        hexRect.x = insets.left + (showLineNumbers ? paintDataCache.charWidth * paintDataCache.lineNumbersLength + paintDataCache.lineNumberSpace : 0);
//
//        if (verticalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
//            verticalScrollBarVisible = lines > paintDataCache.linesPerRect;
//        } else {
//            verticalScrollBarVisible = verticalScrollBarVisibility == ScrollBarVisibility.ALWAYS;
//        }
//        if (verticalScrollBarVisible) {
//            charsPerRect = computeCharsPerRect(compRect.x + compRect.width - paintDataCache.scrollBarThickness);
//            if (wrapMode) {
//                bytesPerLine = computeFittingBytes(charsPerRect);
//                if (bytesPerLine <= 0) {
//                    bytesPerLine = 1;
//                }
//                lines = ((data.getDataSize() + scrollPosition.lineByteShift) / bytesPerLine) + 1;
//            }
//        }
//
//        paintDataCache.bytesPerLine = bytesPerLine;
//        paintDataCache.charsPerLine = computeCharsPerLine(bytesPerLine);
//
//        int maxWidth = compRect.x + compRect.width - hexRect.x;
//        if (verticalScrollBarVisible) {
//            maxWidth -= paintDataCache.scrollBarThickness;
//        }
//
//        if (horizontalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
//            horizontalScrollBarVisible = paintDataCache.charsPerLine * paintDataCache.charWidth > maxWidth;
//        } else {
//            horizontalScrollBarVisible = horizontalScrollBarVisibility == ScrollBarVisibility.ALWAYS;
//        }
//        if (horizontalScrollBarVisible) {
//            paintDataCache.linesPerRect = (hexRect.height - paintDataCache.scrollBarThickness) / paintDataCache.lineHeight;
//        }
//
//        hexRect.width = compRect.x + compRect.width - hexRect.x;
//        if (verticalScrollBarVisible) {
//            hexRect.width -= paintDataCache.scrollBarThickness;
//        }
//        hexRect.height = compRect.y + compRect.height - hexRect.y;
//        if (horizontalScrollBarVisible) {
//            hexRect.height -= paintDataCache.scrollBarThickness;
//        }
//
//        paintDataCache.bytesPerRect = hexRect.width / paintDataCache.charWidth;
//        paintDataCache.linesPerRect = hexRect.height / paintDataCache.lineHeight;
//
//        // Compute sections positions
//        paintDataCache.previewStartChar = 0;
//        if (viewMode == ViewMode.CODE_MATRIX) {
//            paintDataCache.previewX = -1;
//        } else {
//            paintDataCache.previewX = hexRect.x;
//            if (viewMode == ViewMode.DUAL) {
//                paintDataCache.previewStartChar = paintDataCache.charsPerLine - paintDataCache.bytesPerLine;
//                paintDataCache.previewX += (paintDataCache.charsPerLine - paintDataCache.bytesPerLine) * paintDataCache.charWidth;
//            }
//        }
//
//        // Compute scrollbar positions
//        boolean scrolled = false;
//        verticalScrollBar.setVisible(verticalScrollBarVisible);
//        if (verticalScrollBarVisible) {
//            int verticalScrollBarHeight = compRect.y + compRect.height - hexRect.y;
//            if (horizontalScrollBarVisible) {
//                verticalScrollBarHeight -= paintDataCache.scrollBarThickness - 2;
//            }
//            verticalScrollBar.setBounds(compRect.x + compRect.width - paintDataCache.scrollBarThickness, hexRect.y, paintDataCache.scrollBarThickness, verticalScrollBarHeight);
//
//            int verticalVisibleAmount;
//            scrollPosition.verticalMaxMode = false;
//            int verticalMaximum;
//            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
//                if (lines * paintDataCache.lineHeight > Integer.MAX_VALUE) {
//                    scrollPosition.verticalMaxMode = true;
//                    verticalMaximum = Integer.MAX_VALUE;
//                    verticalVisibleAmount = (int) (hexRect.height * Integer.MAX_VALUE / lines);
//                } else {
//                    verticalMaximum = (int) (lines * paintDataCache.lineHeight);
//                    verticalVisibleAmount = hexRect.height;
//                }
//            } else if (lines > Integer.MAX_VALUE) {
//                scrollPosition.verticalMaxMode = true;
//                verticalMaximum = Integer.MAX_VALUE;
//                verticalVisibleAmount = (int) (hexRect.height * Integer.MAX_VALUE / paintDataCache.lineHeight / lines);
//            } else {
//                verticalMaximum = (int) lines;
//                verticalVisibleAmount = hexRect.height / paintDataCache.lineHeight;
//            }
//            if (verticalVisibleAmount == 0) {
//                verticalVisibleAmount = 1;
//            }
//            verticalScrollBar.setMaximum(verticalMaximum);
//            verticalScrollBar.setVisibleAmount(verticalVisibleAmount);
//
//            // Cap vertical scrolling
//            if (!scrollPosition.verticalMaxMode && verticalVisibleAmount < verticalMaximum) {
//                long maxLineScroll = verticalMaximum - verticalVisibleAmount;
//                if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
//                    long lineScroll = scrollPosition.scrollLinePosition;
//                    if (lineScroll > maxLineScroll) {
//                        scrollPosition.scrollLinePosition = maxLineScroll;
//                        scrolled = true;
//                    }
//                } else {
//                    long lineScroll = scrollPosition.scrollLinePosition * paintDataCache.lineHeight + scrollPosition.scrollLineOffset;
//                    if (lineScroll > maxLineScroll) {
//                        scrollPosition.scrollLinePosition = maxLineScroll / paintDataCache.lineHeight;
//                        scrollPosition.scrollLineOffset = (int) (maxLineScroll % paintDataCache.lineHeight);
//                        scrolled = true;
//                    }
//                }
//            }
//        } else if (scrollPosition.scrollLinePosition > 0 || scrollPosition.scrollLineOffset > 0) {
//            scrollPosition.scrollLinePosition = 0;
//            scrollPosition.scrollLineOffset = 0;
//            scrolled = true;
//        }
//
//        horizontalScrollBar.setVisible(horizontalScrollBarVisible);
//        if (horizontalScrollBarVisible) {
//            int horizontalScrollBarWidth = compRect.x + compRect.width - hexRect.x;
//            if (verticalScrollBarVisible) {
//                horizontalScrollBarWidth -= paintDataCache.scrollBarThickness - 2;
//            }
//            horizontalScrollBar.setBounds(hexRect.x, compRect.y + compRect.height - paintDataCache.scrollBarThickness, horizontalScrollBarWidth, paintDataCache.scrollBarThickness);
//
//            int horizontalVisibleAmount;
//            int horizontalMaximum = paintDataCache.charsPerLine;
//            if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
//                horizontalVisibleAmount = hexRect.width;
//                horizontalMaximum *= paintDataCache.charWidth;
//            } else {
//                horizontalVisibleAmount = hexRect.width / paintDataCache.charWidth;
//            }
//            horizontalScrollBar.setMaximum(horizontalMaximum);
//            horizontalScrollBar.setVisibleAmount(horizontalVisibleAmount);
//
//            // Cap horizontal scrolling
//            int maxByteScroll = horizontalMaximum - horizontalVisibleAmount;
//            if (horizontalVisibleAmount < horizontalMaximum) {
//                if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
//                    int byteScroll = scrollPosition.scrollCharPosition * paintDataCache.charWidth + scrollPosition.scrollCharOffset;
//                    if (byteScroll > maxByteScroll) {
//                        scrollPosition.scrollCharPosition = maxByteScroll / paintDataCache.charWidth;
//                        scrollPosition.scrollCharOffset = maxByteScroll % paintDataCache.charWidth;
//                        scrolled = true;
//                    }
//                } else {
//                    int byteScroll = scrollPosition.scrollCharPosition;
//                    if (byteScroll > maxByteScroll) {
//                        scrollPosition.scrollCharPosition = maxByteScroll;
//                        scrolled = true;
//                    }
//                }
//            }
//        } else if (scrollPosition.scrollCharPosition > 0 || scrollPosition.scrollCharOffset > 0) {
//            scrollPosition.scrollCharPosition = 0;
//            scrollPosition.scrollCharOffset = 0;
//            scrolled = true;
//        }
//
//        if (scrolled) {
//            updateScrollBars();
//            notifyScrolled();
//        }
//    }
    private static class PainterState {

        boolean monospaceFont;
        int characterWidth;

        int areaWidth;
        int areaHeight;
        int mainAreaWidth;
        int mainAreaHeight;
        CodeAreaViewMode viewMode;
        CodeAreaScrollPosition scrollPosition;
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
}
