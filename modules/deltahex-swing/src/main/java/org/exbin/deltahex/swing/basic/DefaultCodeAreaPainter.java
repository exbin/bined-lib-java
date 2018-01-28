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
package org.exbin.deltahex.swing.basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeAreaUtils;
import org.exbin.deltahex.CodeCharactersCase;
import org.exbin.deltahex.CodeAreaViewMode;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.capability.CaretCapable;
import org.exbin.deltahex.capability.CharsetCapable;
import org.exbin.deltahex.capability.CodeCharactersCaseCapable;
import org.exbin.deltahex.capability.CodeTypeCapable;
import org.exbin.deltahex.capability.EditationModeCapable;
import org.exbin.deltahex.capability.LineWrappingCapable;
import org.exbin.deltahex.capability.SelectionCapable;
import org.exbin.deltahex.swing.capability.ScrollingCapable;
import org.exbin.deltahex.capability.ViewModeCapable;
import org.exbin.deltahex.swing.CharacterRenderingMode;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.swing.CodeAreaPainter;
import org.exbin.deltahex.swing.CodeAreaSwingUtils;
import org.exbin.deltahex.swing.CodeAreaWorker;
import org.exbin.deltahex.swing.MovementShift;
import org.exbin.deltahex.swing.capability.AntialiasingCapable;
import org.exbin.deltahex.swing.capability.BorderPaintCapable;
import org.exbin.deltahex.swing.capability.FontCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2018/01/07
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    @Nonnull
    protected final CodeAreaWorker worker;
    private boolean initialized = false;

    @Nonnull
    private final JPanel dataView;
    @Nonnull
    private final JScrollPane scrollPanel;

    private CodeAreaViewMode viewMode;
    private final CaretPosition caretPosition = new CaretPosition();
    private SelectionRange selectionRange = null;
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    private VerticalScrollUnit verticalScrollUnit;
    private HorizontalScrollUnit horizontalScrollUnit;
    private int scrollX;
    private int scrollY;
    private final Colors colors = new Colors();
    private long dataSize;

    private int componentWidth;
    private int componentHeight;
    private int dataViewX;
    private int dataViewY;
    private int dataViewWidth;
    private int dataViewHeight;

    private int lineNumbersLength;
    private int lineNumbersAreaWidth;
    private int headerAreaHeight;
    private int lineHeight;
    private int linesPerRect;
    private int bytesPerLine;
    private int charactersPerRect;
    private int charactersPerLine;
    private CodeType codeType;
    private CodeCharactersCase hexCharactersCase;
    private EditationMode editationMode;
    private BasicBorderPaintMode borderPaintMode;
    private boolean showMirrorCursor;

    private int previewCharPos;
    private int previewRelativeX;
    private int visibleCharStart;
    private int visibleCharEnd;
    private int visiblePreviewStart;
    private int visiblePreviewEnd;
    private int visibleCodeStart;
    private int visibleCodeEnd;

    private Charset charset;
    @Nullable
    private Font font;
    @Nullable
    private CharacterRenderingMode characterRenderingMode;
    private int maxCharLength;

    private byte[] lineData;
    private char[] lineNumberCode;
    private char[] lineCharacters;

    private int subFontSpace = 3;

    @Nullable
    private Charset charMappingCharset = null;
    private final char[] charMapping = new char[256];
    // Debug
    private long paintCounter = 0;

    @Nullable
    private FontMetrics fontMetrics;
    private boolean monospaceFont;
    private int characterWidth;

    public DefaultCodeAreaPainter(@Nonnull CodeAreaWorker worker) {
        this.worker = worker;
        CodeArea codeArea = worker.getCodeArea();
        dataView = new JPanel();
        dataView.setBorder(null);
        dataView.setVisible(false);
        dataView.setLayout(null);
        dataView.setOpaque(false);
        // Fill whole area, no more suitable method found so far
        dataView.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        scrollPanel = new JScrollPane();
        scrollPanel.setBorder(null);
        scrollPanel.setIgnoreRepaint(true);
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        horizontalScrollBar.setIgnoreRepaint(true);
        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
        codeArea.add(scrollPanel);
        scrollPanel.setOpaque(false);
        scrollPanel.setBackground(Color.RED);
        scrollPanel.setViewportView(dataView);
        scrollPanel.getViewport().setOpaque(false);

        DefaultCodeAreaMouseListener codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
        codeArea.addMouseListener(codeAreaMouseListener);
        codeArea.addMouseMotionListener(codeAreaMouseListener);
        codeArea.addMouseWheelListener(codeAreaMouseListener);
        scrollPanel.addMouseListener(codeAreaMouseListener);
        scrollPanel.addMouseMotionListener(codeAreaMouseListener);
        scrollPanel.addMouseWheelListener(codeAreaMouseListener);
    }

    @Override
    public void reset() {
        resetSizes();
        resetScrollState();
        resetColors();

        viewMode = ((ViewModeCapable) worker).getViewMode();
        characterRenderingMode = ((AntialiasingCapable) worker).getCharacterRenderingMode();
        hexCharactersCase = ((CodeCharactersCaseCapable) worker).getCodeCharactersCase();
        editationMode = ((EditationModeCapable) worker).getEditationMode();
        caretPosition.setPosition(((CaretCapable) worker).getCaret().getCaretPosition());
        selectionRange = ((SelectionCapable) worker).getSelection();
        borderPaintMode = ((BorderPaintCapable) worker).getBorderPaintMode();
        showMirrorCursor = ((CaretCapable) worker).isShowMirrorCursor();
        dataSize = worker.getCodeArea().getDataSize();

        linesPerRect = computeLinesPerRectangle();
        bytesPerLine = computeBytesPerLine();

        codeType = ((CodeTypeCapable) worker).getCodeType();
        hexCharactersCase = CodeCharactersCase.UPPER;

        int charsPerLine = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerLine += computeLastCodeCharPos(bytesPerLine - 1) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerLine += bytesPerLine;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerLine++;
            }
        }
        charactersPerLine = charsPerLine;
    }

    private void resetCharPositions() {
        charactersPerRect = computeLastCodeCharPos(bytesPerLine - 1);
        // Compute first and last visible character of the code area
        if (viewMode == CodeAreaViewMode.DUAL) {
            previewCharPos = bytesPerLine * (codeType.getMaxDigitsForByte() + 1);
        } else {
            previewCharPos = 0;
        }
        previewRelativeX = previewCharPos * characterWidth;

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            visibleCharStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            visibleCharEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + charactersPerLine) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleCharEnd > charactersPerRect) {
                visibleCharEnd = charactersPerRect;
            }
            visibleCodeStart = computePositionByte(visibleCharStart);
            visibleCodeEnd = computePositionByte(visibleCharEnd - 1) + 1;
        } else {
            visibleCharStart = 0;
            visibleCharEnd = -1;
            visibleCodeStart = 0;
            visibleCodeEnd = -1;
        }

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            visiblePreviewStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewStart < 0) {
                visiblePreviewStart = 0;
            }
            if (visibleCodeEnd < 0) {
                visibleCharStart = visiblePreviewStart + previewCharPos;
            }
            visiblePreviewEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + 1) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth - previewCharPos;
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

        lineData = new byte[bytesPerLine + maxCharLength - 1];
        lineNumberCode = new char[lineNumbersLength];
        lineCharacters = new char[charactersPerLine];
    }

    public void resetFont(@Nonnull Graphics g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) worker).getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        maxCharLength = (int) encoder.maxBytesPerChar();

        font = ((FontCapable) worker).getFont();
        fontMetrics = g.getFontMetrics(font);
        /**
         * Use small 'w' character to guess normal font width.
         */
        characterWidth = fontMetrics.charWidth('w');
        /**
         * Compare it to small 'i' to detect if font is monospaced.
         *
         * TODO: Is there better way?
         */
        monospaceFont = characterWidth == fontMetrics.charWidth(' ') && characterWidth == fontMetrics.charWidth('i');
        int fontSize = font.getSize();
        lineHeight = fontSize + subFontSpace;

        lineNumbersLength = getLineNumberLength();
        resetSizes();
        resetCharPositions();
        initialized = true;
    }

    public void dataViewScrolled(@Nonnull Graphics g) {
        if (!isInitialized()) {
            return;
        }

        resetScrollState();
        if (characterWidth > 0) {
            resetCharPositions();
            paintComponent(g);
        }
    }

    private void resetScrollState() {
        scrollPosition.setScrollPosition(((ScrollingCapable) worker).getScrollPosition());
        verticalScrollUnit = ((ScrollingCapable) worker).getVerticalScrollUnit();
        horizontalScrollUnit = ((ScrollingCapable) worker).getHorizontalScrollUnit();

        // TODO Overflow mode
        switch (horizontalScrollUnit) {
            case CHARACTER: {
                scrollX = scrollPosition.getScrollCharPosition();
                break;
            }
            case PIXEL: {
                scrollX = scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset();
                break;
            }
        }

        switch (verticalScrollUnit) {
            case LINE: {
                scrollY = (int) scrollPosition.getScrollLinePosition();
                break;
            }
            case PIXEL: {
                scrollY = ((int) scrollPosition.getScrollLinePosition() * lineHeight) + scrollPosition.getScrollLineOffset();
                break;
            }
        }

        // TODO on resize only
        scrollPanel.setBounds(getDataViewRectangle());
        scrollPanel.revalidate();
    }

    private void resetSizes() {
        componentWidth = worker.getCodeArea().getWidth();
        componentHeight = worker.getCodeArea().getHeight();
        lineNumbersAreaWidth = characterWidth * (lineNumbersLength + 1);
        headerAreaHeight = 20;
        dataViewX = lineNumbersAreaWidth;
        dataViewY = headerAreaHeight;
        dataViewWidth = componentWidth - lineNumbersAreaWidth;
        dataViewHeight = componentHeight - headerAreaHeight;
    }

    private void resetColors() {
        CodeArea codeArea = worker.getCodeArea();
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
        colors.negativeCursor = CodeAreaSwingUtils.createNegativeColor(colors.cursor);
        if (colors.cursor == null) {
            colors.cursor = Color.BLACK;
        }
        colors.decorationLine = Color.GRAY;

        colors.stripes = CodeAreaSwingUtils.createOddColor(colors.background);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void paintComponent(@Nonnull Graphics g) {
        if (!initialized) {
            reset();
        }
        if (font == null) {
            resetFont(g);
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintLineNumbers(g);
        paintMainArea(g);
//        scrollPanel.paintComponents(g);
        paintCounter++;
    }

    public void paintOutsiteArea(@Nonnull Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, componentWidth, headerAreaHeight);
        g.setColor(Color.BLACK);
        g.fillRect(0, headerAreaHeight - 1, lineNumbersAreaWidth, 1);
    }

    public void paintHeader(@Nonnull Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle headerArea = new Rectangle(lineNumbersAreaWidth, 0, componentWidth - lineNumbersAreaWidth, headerAreaHeight);
        g.setClip(clipBounds != null ? headerArea.intersection(clipBounds) : headerArea);

        g.setColor(colors.background);
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        // Decoration line
        g.setColor(Color.BLACK);
        g.fillRect(0, headerAreaHeight - 1, componentWidth, 1);

        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int charsPerLine = computeFirstCodeCharPos(bytesPerLine);
            int headerX = dataViewX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
            int headerY = lineHeight - subFontSpace;

            int visibleCharStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            int visibleCharEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + charsPerLine) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleCharEnd > charsPerLine) {
                visibleCharEnd = charsPerLine;
            }
            int visibleStart = computePositionByte(visibleCharStart);
            int visibleEnd = computePositionByte(visibleCharEnd - 1) + 1;

            g.setColor(colors.foreground);
            char[] headerChars = new char[charsPerLine];
            Arrays.fill(headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            for (int index = visibleStart; index < visibleEnd; index++) {
                int codePos = computeFirstCodeCharPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(headerChars, codePos, index, 16, 2, true, hexCharactersCase);
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
                    if (characterRenderingMode == CharacterRenderingMode.AUTO && monospaceFont) {
                        // Detect if character is in unicode range covered by monospace fonts
                        if (CodeAreaSwingUtils.isMonospaceFullWidthCharater(currentChar)) {
                            currentCharWidth = characterWidth;
                        }
                    }

                    if (currentCharWidth == 0) {
                        currentCharWidth = fontMetrics.charWidth(currentChar);
                        nativeWidth = currentCharWidth == characterWidth;
                    }
                } else {
                    currentCharWidth = characterWidth;
                }

                Color color = Color.BLACK;
//                getHeaderPositionColor(byteOnLine, charOnLine);
//                if (renderColorType == null) {
//                    renderColorType = colorType;
//                    renderColor = color;
//                    g.setColor(color);
//                }

                if (!nativeWidth || !CodeAreaSwingUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnLine) {
                        g.drawChars(headerChars, renderOffset, characterOnLine - renderOffset, headerX + renderOffset * characterWidth, headerY);
                    }

//                    if (!colorType.equals(renderColorType)) {
//                        renderColorType = colorType;
//                    }
                    if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setColor(color);
                    }

                    if (!nativeWidth) {
                        renderOffset = characterOnLine + 1;
                        if (characterRenderingMode == CharacterRenderingMode.TOP_LEFT) {
                            g.drawChars(headerChars, characterOnLine, 1, headerX + characterOnLine * characterWidth, headerY);
                        } else {
                            drawShiftedChar(g, headerChars, characterOnLine, characterWidth, headerX + characterOnLine * characterWidth, headerY, (characterWidth + 1 - currentCharWidth) >> 1);
                        }
                    } else {
                        renderOffset = characterOnLine;
                    }
                }
            }

            if (renderOffset < charsPerLine) {
                g.drawChars(headerChars, renderOffset, charsPerLine - renderOffset, headerX + renderOffset * characterWidth, headerY);
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

    public void paintLineNumbers(@Nonnull Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle lineNumbersArea = new Rectangle(0, headerAreaHeight, lineNumbersAreaWidth, componentHeight - headerAreaHeight); // TODO minus scrollbar height
        g.setClip(clipBounds != null ? lineNumbersArea.intersection(clipBounds) : lineNumbersArea);

        g.setColor(colors.background);
        g.fillRect(lineNumbersArea.x, lineNumbersArea.y, lineNumbersArea.width, lineNumbersArea.height);

        int lineNumberLength = lineNumbersLength;

        if (borderPaintMode == BasicBorderPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getScrollLinePosition() * bytesPerLine;
            int stripePositionY = headerAreaHeight + ((scrollPosition.getScrollLinePosition() & 1) > 0 ? 0 : lineHeight);
            g.setColor(colors.stripes);
            for (int line = 0; line <= linesPerRect / 2; line++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.fillRect(0, stripePositionY, lineNumbersAreaWidth, lineHeight);
                stripePositionY += lineHeight * 2;
                dataPosition += bytesPerLine * 2;
            }
        }

        long dataPosition = bytesPerLine * scrollPosition.getScrollLinePosition();
        int positionY = headerAreaHeight + lineHeight - subFontSpace - scrollPosition.getScrollLineOffset();
        g.setColor(colors.foreground);
        Rectangle compRect = new Rectangle();
        for (int line = 0; line <= linesPerRect; line++) {
            if (dataPosition >= dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(lineNumberCode, 0, dataPosition < 0 ? 0 : dataPosition, codeType.getBase(), lineNumberLength, true, CodeCharactersCase.UPPER);
            if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
            } else {
                for (int digitIndex = 0; digitIndex < lineNumberLength; digitIndex++) {
                    drawCenteredChar(g, lineNumberCode, digitIndex, characterWidth, compRect.x + characterWidth * digitIndex, positionY);
                }
            }

            positionY += lineHeight;
            dataPosition += bytesPerLine;
        }
        g.setClip(clipBounds);
    }

    @Override
    public void paintMainArea(@Nonnull Graphics g) {
        if (!initialized) {
            reset();
        }
        if (font == null) {
            resetFont(g);
        }

        Rectangle clipBounds = g.getClipBounds();
        Rectangle mainArea = getMainAreaRect();
        g.setClip(clipBounds != null ? mainArea.intersection(clipBounds) : mainArea);
        paintBackground(g);
        paintLines(g);
        g.setClip(clipBounds);
        paintCursor(g);

        // TODO: Remove later
        int x = componentWidth - lineNumbersAreaWidth - 220;
        int y = componentHeight - headerAreaHeight - 20;
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 200, 16);
        g.setColor(Color.BLACK);
        char[] headerCode = (String.valueOf(scrollPosition.getScrollCharPosition()) + "+" + String.valueOf(scrollPosition.getScrollCharOffset()) + " : " + String.valueOf(scrollPosition.getScrollLinePosition()) + "+" + String.valueOf(scrollPosition.getScrollLineOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
        g.drawChars(headerCode, 0, headerCode.length, x, y + lineHeight);
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(@Nonnull Graphics g) {
        int linePositionX = lineNumbersAreaWidth;
        g.setColor(colors.background);
        if (borderPaintMode != BasicBorderPaintMode.TRANSPARENT) {
            g.fillRect(linePositionX, headerAreaHeight, dataViewWidth, dataViewHeight);
        }

        if (borderPaintMode == BasicBorderPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getScrollLinePosition() * bytesPerLine;
            int stripePositionY = headerAreaHeight + (int) ((scrollPosition.getScrollLinePosition() & 1) > 0 ? 0 : lineHeight);
            g.setColor(colors.stripes);
            for (int line = 0; line <= linesPerRect / 2; line++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.fillRect(linePositionX, stripePositionY, dataViewWidth, lineHeight);
                stripePositionY += lineHeight * 2;
                dataPosition += bytesPerLine * 2;
            }
        }
    }

    public void paintLines(@Nonnull Graphics g) {
        long dataPosition = scrollPosition.getScrollLinePosition() * bytesPerLine + scrollPosition.getLineDataOffset();
        int linePositionX = lineNumbersAreaWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
        int linePositionY = headerAreaHeight;
        g.setColor(Color.BLACK);
        for (int line = 0; line <= linesPerRect; line++) {
            prepareLineData(dataPosition);
            paintLineBackground(g, dataPosition, linePositionX, linePositionY);
            paintLineText(g, dataPosition, linePositionX, linePositionY);

            linePositionY += lineHeight;
            dataPosition += bytesPerLine;
        }
    }

    private void prepareLineData(long dataPosition) {
        int lineBytesLimit = bytesPerLine;
        int lineStart = 0;
        if (dataPosition < dataSize) {
            int lineDataSize = bytesPerLine + maxCharLength - 1;
            if (dataPosition + lineDataSize > dataSize) {
                lineDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                lineStart = (int) -dataPosition;
            }
            worker.getCodeArea().getData().copyToArray(dataPosition + lineStart, lineData, lineStart, lineDataSize - lineStart);
            if (dataPosition + lineBytesLimit > dataSize) {
                lineBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            lineBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            for (int byteOnLine = Math.max(visibleCodeStart, lineStart); byteOnLine < Math.min(visibleCodeEnd, lineBytesLimit); byteOnLine++) {
                byte dataByte = lineData[byteOnLine];
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, lineCharacters, computeFirstCodeCharPos(byteOnLine), hexCharactersCase);
            }
            if (bytesPerLine > lineBytesLimit) {
                Arrays.fill(lineCharacters, computePositionByte(lineBytesLimit), lineCharacters.length, ' ');
            }
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            for (int byteOnLine = visiblePreviewStart; byteOnLine < Math.min(visiblePreviewEnd, lineBytesLimit); byteOnLine++) {
                byte dataByte = lineData[byteOnLine];

                if (maxCharLength > 1) {
                    if (dataPosition + maxCharLength > dataSize) {
                        maxCharLength = (int) (dataSize - dataPosition);
                    }

                    int charDataLength = maxCharLength;
                    if (byteOnLine + charDataLength > lineData.length) {
                        charDataLength = lineData.length - byteOnLine;
                    }
                    String displayString = new String(lineData, byteOnLine, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        lineCharacters[previewCharPos + byteOnLine] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        buildCharMapping(charset);
                    }

                    lineCharacters[previewCharPos + byteOnLine] = charMapping[dataByte & 0xFF];
                }
            }
            if (bytesPerLine > lineBytesLimit) {
                Arrays.fill(lineCharacters, previewCharPos + lineBytesLimit, previewCharPos + bytesPerLine, ' ');
            }
        }
    }

    /**
     * Paints line background.
     *
     * @param g graphics
     * @param lineDataPosition line data position
     * @param linePositionX line position X
     * @param linePositionY line position Y
     */
    public void paintLineBackground(@Nonnull Graphics g, long lineDataPosition, int linePositionX, int linePositionY) {
        int renderOffset = visibleCharStart;
        Color renderColor = null;
        for (int charOnLine = visibleCharStart; charOnLine < visibleCharEnd; charOnLine++) {
            CodeAreaSection section;
            int byteOnLine;
            if (charOnLine >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnLine = charOnLine - previewCharPos;
                section = CodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnLine = computePositionByte(charOnLine);
                section = CodeAreaSection.CODE_MATRIX;
            }
            boolean sequenceBreak = false;

            Color color = getPositionBackgroundColor(lineDataPosition, byteOnLine, charOnLine, section);
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnLine) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnLine, linePositionX, linePositionY);
                    }
                }

                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.setColor(color);
                    }
                }

                renderOffset = charOnLine;
            }
        }

        if (renderOffset < charactersPerLine) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, charactersPerLine, linePositionX, linePositionY);
            }
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param lineDataPosition line data position
     * @param byteOnLine byte on current line
     * @param charOnLine character on current line
     * @param section current section
     * @return color or null for default color
     */
    @Nullable
    public Color getPositionBackgroundColor(long lineDataPosition, int byteOnLine, int charOnLine, @Nonnull CodeAreaSection section) {
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(lineDataPosition + byteOnLine);
        if (inSelection) {
            return section == caretPosition.getSection() ? colors.selectionBackground : colors.selectionMirrorBackground;
        }

        return null;
    }

    @Override
    public boolean revealPosition(@Nonnull CaretPosition caretPosition) {
        boolean scrolled = false;
        /*        Rectangle hexRect = getDataViewRectangle();
        int bytesPerRect = getBytesPerRectangle();
        int linesPerRect = getLinesPerRectangle();
        int bytesPerLine = getBytesPerLine();
        long caretLine = position / bytesPerLine;

        int positionByte = painter.computePositionByte((int) (position % bytesPerLine));

        if (caretLine <= scrollPosition.getScrollLinePosition()) {
            scrollPosition.setScrollLinePosition(caretLine);
            scrollPosition.setScrollLineOffset(0);
            scrolled = true;
        } else if (caretLine >= scrollPosition.getScrollLinePosition() + linesPerRect) {
            scrollPosition.setScrollLinePosition(caretLine - linesPerRect);
            if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
                scrollPosition.setScrollLineOffset(getLineHeight() - (hexRect.height % getLineHeight()));
            } else {
                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
            }
            scrolled = true;
        }
        if (positionByte <= scrollPosition.getScrollCharPosition()) {
            scrollPosition.setScrollCharPosition(positionByte);
            scrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else if (positionByte >= scrollPosition.getScrollCharPosition() + bytesPerRect) {
            scrollPosition.setScrollCharPosition(positionByte - bytesPerRect);
            if (horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
                scrollPosition.setScrollCharOffset(getCharacterWidth() - (hexRect.width % getCharacterWidth()));
            } else {
                scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() + 1);
            }
            scrolled = true;
        }
         */
        return scrolled;
    }

    /**
     * Paints line text.
     *
     * @param g graphics
     * @param lineDataPosition line data position
     * @param linePositionX line position X
     * @param linePositionY line position Y
     */
    public void paintLineText(@Nonnull Graphics g, long lineDataPosition, int linePositionX, int linePositionY) {
        int positionY = linePositionY + lineHeight - subFontSpace;

        Color renderColor = null;
        g.setColor(colors.foreground);
//        Rectangle dataViewRectangle = codeArea.getDataViewRectangle();
//        g.drawString("[" + String.valueOf(dataViewRectangle.x) + "," + String.valueOf(dataViewRectangle.y) + "," + String.valueOf(dataViewRectangle.width) + "," + String.valueOf(dataViewRectangle.height) + "]", linePositionX, positionY);

//    public void paintLineText(Graphics g, int linePositionX, int linePositionY, PaintDataCache paintData) {
//        int positionY = linePositionY + paintData.lineHeight - codeArea.getSubFontSpace();
//
        int renderOffset = visibleCharStart;
        for (int charOnLine = visibleCharStart; charOnLine < visibleCharEnd; charOnLine++) {
            CodeAreaSection section;
            int byteOnLine;
            if (charOnLine >= previewCharPos) {
                byteOnLine = charOnLine - previewCharPos;
                section = CodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnLine = computePositionByte(charOnLine);
                section = CodeAreaSection.CODE_MATRIX;
            }
            boolean sequenceBreak = false;
            boolean nativeWidth = true;

            Color color = getPositionTextColor(lineDataPosition, byteOnLine, charOnLine, section);
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }

            int currentCharWidth = 0;
            if (characterRenderingMode != CharacterRenderingMode.LINE_AT_ONCE) {
                char currentChar = lineCharacters[charOnLine];
                if (currentChar == ' ' && renderOffset == charOnLine) {
                    renderOffset++;
                    continue;
                }
                if (characterRenderingMode == CharacterRenderingMode.AUTO && monospaceFont) {
                    // Detect if character is in unicode range covered by monospace fonts
                    if (CodeAreaSwingUtils.isMonospaceFullWidthCharater(currentChar)) {
                        currentCharWidth = characterWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = fontMetrics.charWidth(currentChar);
                    nativeWidth = currentCharWidth == characterWidth;
                }
            } else {
                currentCharWidth = characterWidth;
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
                    if (characterRenderingMode == CharacterRenderingMode.TOP_LEFT) {
                        g.drawChars(lineCharacters, charOnLine, 1, linePositionX + charOnLine * characterWidth, positionY);
                    } else {
                        drawShiftedChar(g, lineCharacters, charOnLine, characterWidth, linePositionX + charOnLine * characterWidth, positionY, (characterWidth + 1 - currentCharWidth) >> 1);
                    }
                } else {
                    renderOffset = charOnLine;
                }

                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.setColor(color);
                    } else {
                        g.setColor(colors.foreground);
                    }
                }
            }
        }

        if (renderOffset < charactersPerLine) {
            renderCharSequence(g, renderOffset, charactersPerLine, linePositionX, positionY);
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param lineDataPosition line data position
     * @param byteOnLine byte on current line
     * @param charOnLine character on current line
     * @param section current section
     * @return color or null for default color
     */
    @Nullable
    public Color getPositionTextColor(long lineDataPosition, int byteOnLine, int charOnLine, @Nonnull CodeAreaSection section) {
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(lineDataPosition + byteOnLine);
        if (inSelection) {
            return section == caretPosition.getSection() ? colors.selectionForeground : colors.selectionMirrorForeground;
        }

        return null;
    }

    @Override
    public void paintCursor(@Nonnull Graphics g) {
        if (!worker.getCodeArea().hasFocus()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) worker).getCaret();
        Rectangle cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        Rectangle clipBounds = g.getClipBounds();
        Rectangle mainAreaRect = getMainAreaRect();
        Rectangle intersection = mainAreaRect.intersection(cursorRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.setClip(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setColor(colors.cursor);

            switch (renderingMode) {
                case PAINT: {
                    g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
                    break;
                }
                case XOR: {
                    g.setXORMode(Color.WHITE);
                    g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
                    g.setPaintMode();
                    break;
                }
                case NEGATIVE: {
                    g.fillRect(cursorRect.x, cursorRect.y, cursorRect.width, cursorRect.height);
                    g.setColor(colors.negativeCursor);
                    BinaryData codeAreaData = worker.getCodeArea().getData();
                    int line = (cursorRect.y + scrollPosition.getScrollLineOffset() - dataViewY) / lineHeight;
                    int scrolledX = cursorRect.x + scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset();
                    int posY = dataViewY + (line + 1) * lineHeight - subFontSpace - scrollPosition.getScrollLineOffset();
                    long dataPosition = caret.getDataPosition();
                    if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == CodeAreaSection.TEXT_PREVIEW) {
                        int charPos = (scrolledX - previewRelativeX) / characterWidth;
                        if (dataPosition >= dataSize) {
                            break;
                        }

                        char[] previewChars = new char[1];
                        byte[] data = new byte[maxCharLength];

                        if (maxCharLength > 1) {
                            int charDataLength = maxCharLength;
                            if (dataPosition + maxCharLength > dataSize) {
                                charDataLength = (int) (dataSize - dataPosition);
                            }

                            codeAreaData.copyToArray(dataPosition, data, 0, charDataLength);
                            String displayString = new String(data, 0, charDataLength, charset);
                            if (!displayString.isEmpty()) {
                                previewChars[0] = displayString.charAt(0);
                            }
                        } else {
                            if (charMappingCharset == null || charMappingCharset != charset) {
                                buildCharMapping(charset);
                            }

                            previewChars[0] = charMapping[codeAreaData.getByte(dataPosition) & 0xFF];
                        }
                        int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
                        if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                            g.drawChars(previewChars, 0, 1, posX, posY);
                        } else {
                            drawCenteredChar(g, previewChars, 0, characterWidth, posX, posY);
                        }
                    } else {
                        int charPos = (scrolledX - dataViewX) / characterWidth;
                        int byteOffset = computePositionByte(charPos);
                        int codeCharPos = computeFirstCodeCharPos(byteOffset);
                        char[] lineChars = new char[codeType.getMaxDigitsForByte()];

                        byte dataByte = codeAreaData.getByte(dataPosition);
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, lineChars, 0, hexCharactersCase);
                        int posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
                        int charsOffset = charPos - codeCharPos;
                        if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                            g.drawChars(lineChars, charsOffset, 1, posX + (charsOffset * characterWidth), posY);
                        } else {
                            drawCenteredChar(g, lineChars, charsOffset, characterWidth, posX + (charsOffset * characterWidth), posY);
                        }
                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
            }
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            Rectangle mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            if (mirrorCursorRect != null) {
                intersection = mainAreaRect.intersection(mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.setClip(intersection);
                    g.setColor(colors.cursor);
                    Graphics2D g2d = (Graphics2D) g.create();
                    Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
                    g2d.setStroke(dashed);
                    g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
                }
            }
        }
        g.setClip(clipBounds);
    }

    @Nonnull
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY) {
        CaretPosition caret = new CaretPosition();
        if (positionX < lineNumbersAreaWidth) {
            positionX = lineNumbersAreaWidth;
        }
        int cursorCharX = (positionX - lineNumbersAreaWidth + scrollPosition.getScrollCharOffset()) / characterWidth + scrollPosition.getScrollCharPosition();
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        if (positionY < headerAreaHeight) {
            positionY = headerAreaHeight;
        }
        long cursorLineY = (positionY - headerAreaHeight + scrollPosition.getScrollLineOffset()) / lineHeight + scrollPosition.getScrollLinePosition();
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnLine;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(CodeAreaSection.CODE_MATRIX);
            byteOnLine = computePositionByte(cursorCharX);
            if (byteOnLine >= bytesPerLine) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - computeFirstCodeCharPos(byteOnLine);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(CodeAreaSection.TEXT_PREVIEW);
            byteOnLine = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnLine -= previewCharPos;
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

        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        caret.setDataPosition(dataPosition);
        caret.setCodeOffset(codeOffset);
        return caret;
    }

    @Override
    public CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementShift direction) {
        CaretPosition target = new CaretPosition(position.getDataPosition(), position.getCodeOffset(), position.getSection());
        switch (direction) {
            case LEFT: {
                if (position.getSection() == CodeAreaSection.CODE_MATRIX) {
                    int codeOffset = position.getCodeOffset();
                    if (codeOffset > 0) {
                        target.setCodeOffset(codeOffset - 1);
                    } else if (position.getDataPosition() > 0) {
                        target.setDataPosition(position.getDataPosition() - 1);
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                } else if (position.getDataPosition() > 0) {
                    target.setDataPosition(position.getDataPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (position.getSection() == CodeAreaSection.CODE_MATRIX) {
                    int codeOffset = position.getCodeOffset();
                    if (codeOffset < codeType.getMaxDigitsForByte() - 1) {
                        target.setCodeOffset(codeOffset + 1);
                    } else if (position.getDataPosition() < dataSize) {
                        target.setDataPosition(position.getDataPosition() + 1);
                        target.setCodeOffset(0);
                    }
                } else if (position.getDataPosition() < dataSize) {
                    target.setDataPosition(position.getDataPosition() + 1);
                }
                break;
            }
            case UP: {
                if (position.getDataPosition() >= bytesPerLine) {
                    target.setDataPosition(position.getDataPosition() - bytesPerLine);
                }
                break;
            }
            case DOWN: {
                if (position.getDataPosition() + bytesPerLine < dataSize || (position.getDataPosition() + bytesPerLine == dataSize && position.getCodeOffset() == 0)) {
                    target.setDataPosition(position.getDataPosition() + bytesPerLine);
                }
                break;
            }
            case LINE_START: {
                long dataPosition = position.getDataPosition();
                dataPosition -= (dataPosition % bytesPerLine);
                target.setDataPosition(dataPosition);
                target.setCodeOffset(0);
                break;
            }
            case LINE_END: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerLine - 1 - (dataPosition % bytesPerLine);
                if (dataPosition > Long.MAX_VALUE - increment || dataPosition + increment > dataSize) {
                    target.setDataPosition(dataSize);
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                if (position.getSection() == CodeAreaSection.CODE_MATRIX) {
                    target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                }
                break;
            }
            case PAGE_UP: {
                break;
            }
            case PAGE_DOWN: {
                break;
            }
            case DOC_START: {
                target.setDataPosition(0);
                target.setCodeOffset(0);
                break;
            }
            case DOC_END: {
                target.setDataPosition(dataSize);
                target.setCodeOffset(0);
                break;
            }
            case SWITCH_SECTION: {
                CodeAreaSection activeSection = caretPosition.getSection() == CodeAreaSection.CODE_MATRIX ? CodeAreaSection.TEXT_PREVIEW : CodeAreaSection.CODE_MATRIX;
                if (activeSection == CodeAreaSection.TEXT_PREVIEW) {
                    target.setCodeOffset(0);
                }
                target.setSection(activeSection);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected movement direction " + direction.name());
            }
        }

        return target;
    }

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     * @param section section
     * @return cursor position or null
     */
    @Nullable
    public Point getPositionPoint(long dataPosition, int codeOffset, @Nonnull CodeAreaSection section) {
        long shiftedPosition = dataPosition + scrollPosition.getLineDataOffset();
        long line = shiftedPosition / bytesPerLine - scrollPosition.getScrollLinePosition();
        if (line < -1 || line > linesPerRect) {
            return null;
        }

        int byteOffset = (int) (shiftedPosition % bytesPerLine);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + line * lineHeight) - scrollPosition.getScrollLineOffset();
        int caretX;
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.x + previewRelativeX + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.x + characterWidth * (computeFirstCodeCharPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    @Nullable
    private Rectangle getMirrorCursorRect(long dataPosition, @Nonnull CodeAreaSection section) {
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == CodeAreaSection.CODE_MATRIX ? CodeAreaSection.TEXT_PREVIEW : CodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            return null;
        }

        Rectangle mirrorCursorRect = new Rectangle(mirrorCursorPoint.x, mirrorCursorPoint.y, characterWidth * (section == CodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), lineHeight);

        return mirrorCursorRect;
    }

    @Override
    public int getCursorShape(int positionX, int positionY) {
        if (positionX >= dataViewX && positionX < dataViewX + dataViewWidth
                && positionY >= dataViewY && positionY < dataViewY + dataViewHeight) {
            return Cursor.TEXT_CURSOR;
        }

        // TODO scrollbars
        return Cursor.DEFAULT_CURSOR;
    }

    @Nonnull
    public Rectangle getDataViewRectangle() {
        return new Rectangle(dataViewX, dataViewY, dataViewWidth, dataViewHeight);
    }

    public int computePositionByte(int lineCharPosition) {
        return lineCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeFirstCodeCharPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeLastCodeCharPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1) + codeType.getMaxDigitsForByte() - 1;
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
    protected void drawCenteredChar(@Nonnull Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY) {
        int charWidth = fontMetrics.charWidth(drawnChars[charOffset]);
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, startX, positionY, (charWidthSpace + 1 - charWidth) >> 1);
    }

    protected void drawShiftedChar(@Nonnull Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY, int shift) {
        g.drawChars(drawnChars, charOffset, 1, startX + shift, positionY);
    }

    private void buildCharMapping(@Nonnull Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private int getLineNumberLength() {
        return 8;
    }

    /**
     * Returns cursor rectangle.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     * @param section section
     * @return cursor rectangle or null
     */
    @Nullable
    public Rectangle getPositionRect(long dataPosition, int codeOffset, @Nonnull CodeAreaSection section) {
        Point cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            return null;
        }

        DefaultCodeAreaCaret.CursorShape cursorShape = editationMode == EditationMode.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
        int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, lineHeight);
        return new Rectangle(cursorPoint.x, cursorPoint.y, cursorThickness, lineHeight);
    }

    /**
     * Render sequence of characters.
     *
     * Doesn't include character at offset end.
     */
    private void renderCharSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY) {
        g.drawChars(lineCharacters, startOffset, endOffset - startOffset, linePositionX + startOffset * characterWidth, positionY);
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Graphics g, int startOffset, int endOffset, int linePositionX, int positionY) {
        g.fillRect(linePositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, lineHeight);
    }

    private int computeLinesPerRectangle() {
        return lineHeight == 0 ? 0 : (dataViewHeight + lineHeight - 1) / lineHeight;
    }

    private int computeBytesPerLine() {
        boolean lineWrapping = ((LineWrappingCapable) worker).isLineWrapping();
        int maxBytesPerLine = ((LineWrappingCapable) worker).getMaxBytesPerLine();

        int computedBytesPerLine = 16;
        if (lineWrapping) {

        }

        return computedBytesPerLine;
    }

    @Override
    public void updateScrollBars() {
        if (scrollPosition.getVerticalOverflowMode() == CodeAreaScrollPosition.VerticalOverflowMode.OVERFLOW) {
            long lines = ((dataSize + scrollPosition.getLineDataOffset()) / bytesPerLine) + 1;
            int scrollValue;
            if (scrollPosition.getScrollCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                scrollValue = (int) ((scrollPosition.getScrollLinePosition() * Integer.MAX_VALUE) / lines);
            } else {
                scrollValue = (int) (scrollPosition.getScrollLinePosition() / (lines / Integer.MAX_VALUE));
            }
            scrollPanel.getVerticalScrollBar().setValue(scrollValue);
        } else if (verticalScrollUnit == VerticalScrollUnit.LINE) {
            scrollPanel.getVerticalScrollBar().setValue((int) scrollPosition.getScrollLinePosition());
        } else {
            scrollPanel.getVerticalScrollBar().setValue((int) (scrollPosition.getScrollLinePosition() * lineHeight + scrollPosition.getScrollLineOffset()));
        }

        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition());
        } else {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset());
        }
    }

    private Rectangle getMainAreaRect() {
        return new Rectangle(lineNumbersAreaWidth, headerAreaHeight, componentWidth - lineNumbersAreaWidth - 20, componentHeight - headerAreaHeight - 20); // TODO minus scrollbar width
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
            if (scrollPosition.getVerticalOverflowMode() == CodeAreaScrollPosition.VerticalOverflowMode.OVERFLOW) {
                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
                long lines = ((dataSize + scrollPosition.getLineDataOffset()) / bytesPerLine) - computeLinesPerRectangle() + 1;
                long targetLine;
                if (scrollBarValue > 0 && lines > maxValue / scrollBarValue) {
                    targetLine = scrollBarValue * (lines / maxValue);
                    long rest = lines % maxValue;
                    targetLine += (rest * scrollBarValue) / maxValue;
                } else {
                    targetLine = (scrollBarValue * lines) / Integer.MAX_VALUE;
                }
                scrollPosition.setScrollLinePosition(targetLine);
                if (verticalScrollUnit != VerticalScrollUnit.LINE) {
                    scrollPosition.setScrollLineOffset(0);
                }
            } else if (verticalScrollUnit == VerticalScrollUnit.LINE) {
                scrollPosition.setScrollLinePosition(scrollBarValue);
            } else {
                scrollPosition.setScrollLinePosition(scrollBarValue / lineHeight);
                scrollPosition.setScrollLineOffset(scrollBarValue % lineHeight);
            }

            // TODO
            worker.getCodeArea().repaint();
//            dataViewScrolled(codeArea.getGraphics());
            notifyScrolled();
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                scrollPosition.setScrollCharPosition(scrollPanel.getHorizontalScrollBar().getValue());
            } else {
                if (characterWidth > 0) {
                    int horizontalScroll = scrollPanel.getHorizontalScrollBar().getValue();
                    scrollPosition.setScrollCharPosition(horizontalScroll / characterWidth);
                    scrollPosition.setScrollCharOffset(horizontalScroll % characterWidth);
                }
            }

            worker.getCodeArea().repaint();
//            dataViewScrolled(codeArea.getGraphics());
            notifyScrolled();
        }
    }

    private void notifyScrolled() {
        // TODO
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
}
