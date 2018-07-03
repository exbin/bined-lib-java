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
package org.exbin.bined.swing.basic;

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
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.RowWrappingCapable.RowWrappingMode;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.swing.CodeArea;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.CodeAreaWorker;
import org.exbin.bined.swing.MovementDirection;
import org.exbin.bined.swing.ScrollingDirection;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret.CursorRenderingMode;
import org.exbin.bined.swing.capability.BackgroundPaintCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.capability.ScrollingCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2018/07/03
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    @Nonnull
    protected final CodeAreaWorker worker;
    private boolean initialized = false;
    private boolean fontChanged = false;
    private volatile ScrollingState scrollingState = ScrollingState.NO_SCROLLING;

    @Nonnull
    private final JPanel dataView;
    @Nonnull
    private final JScrollPane scrollPanel;

    @Nullable
    private CodeAreaViewMode viewMode;
    private final CodeAreaCaretPosition caretPosition = new CodeAreaCaretPosition();
    @Nullable
    private SelectionRange selectionRange = null;
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    private ScrollBarVerticalScale scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;

    @Nullable
    private VerticalScrollUnit verticalScrollUnit;
    @Nonnull
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nullable
    private HorizontalScrollUnit horizontalScrollUnit;
    @Nonnull
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private final Colors colors = new Colors();
    private long dataSize;
    @Nonnull
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int maxBytesPerLine;
    private int wrappingBytesGroupSize;

    private int componentWidth;
    private int componentHeight;
    private int dataViewX;
    private int dataViewY;
    private final int dataViewOffsetX = 0;
    private final int dataViewOffsetY = 0;
    private int scrollPanelWidth;
    private int scrollPanelHeight;
    private int dataViewWidth;
    private int dataViewHeight;

    private int rowPositionNumberLength;
    private int rowPositionLength;
    private int rowPositionAreaWidth;
    private int headerAreaHeight;
    private int rowHeight;
    private int rowsPerPage;
    private int rowsPerRect;
    private long rowsPerDocument;
    private int bytesPerRow;
    private int charactersPerPage;
    private int charactersPerRect;
    private int charactersPerRow;
    @Nullable
    private CodeAreaScrollPosition maximumScrollPosition;

    @Nullable
    private CodeType codeType;
    @Nullable
    private CodeCharactersCase hexCharactersCase;
    @Nullable
    private EditationMode editationMode;
    @Nullable
    private BasicBackgroundPaintMode backgroundPaintMode;
    private boolean showMirrorCursor;

    private int codeLastCharPos;
    private int previewCharPos;
    private int previewRelativeX;
    private int visibleCharStart;
    private int visibleCharEnd;
    private int visiblePreviewStart;
    private int visiblePreviewEnd;
    private int visibleCodeStart;
    private int visibleCodeEnd;

    @Nonnull
    private Charset charset;
    @Nullable
    private Font font;
    private int maxBytesPerChar;

    @Nullable
    private RowDataCache rowDataCache = null;
    @Nullable
    private CursorDataCache cursorDataCache = null;

    // TODO replace with computation
    private final int subFontSpace = 3;

    @Nullable
    private Charset charMappingCharset = null;
    private final char[] charMapping = new char[256];

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
        dataView.setPreferredSize(new Dimension(0, 0));
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
        scrollPanel.setViewportView(dataView);
        scrollPanel.setViewportBorder(null);
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
        resetColors();
        resetFont();
        updateLayout();
    }

    @Override
    public void resetFont() {
        fontChanged = true;
    }

    @Override
    public void updateLayout() {
        resetSizes();

        viewMode = ((ViewModeCapable) worker).getViewMode();
        codeType = ((CodeTypeCapable) worker).getCodeType();
        hexCharactersCase = ((CodeCharactersCaseCapable) worker).getCodeCharactersCase();
        editationMode = ((EditationModeCapable) worker).getEditationMode();
        caretPosition.setPosition(((CaretCapable) worker).getCaret().getCaretPosition());
        selectionRange = ((SelectionCapable) worker).getSelection();
        backgroundPaintMode = ((BackgroundPaintCapable) worker).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) worker).isShowMirrorCursor();
        dataSize = worker.getCodeArea().getDataSize();
        rowWrapping = ((RowWrappingCapable) worker).isRowWrapping();
        maxBytesPerLine = ((RowWrappingCapable) worker).getMaxBytesPerRow();
        wrappingBytesGroupSize = ((RowWrappingCapable) worker).getWrappingBytesGroupSize();
        rowPositionNumberLength = ((RowWrappingCapable) worker).getRowPositionNumberLength();

        rowsPerRect = computeRowsPerRectangle();
        rowsPerPage = computeRowsPerPage();

        charactersPerPage = computeCharactersPerPage();
        bytesPerRow = computeBytesPerRow();
        charactersPerRow = computeCharactersPerRow();
        rowsPerDocument = computeRowsPerDocument();
        maximumScrollPosition = computeMaximumScrollPosition();

        resetScrollState();
    }

    private void resetCharPositions() {
        charactersPerRect = computeCharactersPerRectangle();

        // Compute first and last visible character of the code area
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            codeLastCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1) - 1;
        } else {
            codeLastCharPos = 0;
        }

        if (viewMode == CodeAreaViewMode.DUAL) {
            previewCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1);
        } else {
            previewCharPos = 0;
        }
        previewRelativeX = previewCharPos * characterWidth;

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            visibleCharStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            visibleCharEnd = ((scrollPosition.getCharPosition() + charactersPerRect) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleCharEnd > charactersPerRow) {
                visibleCharEnd = charactersPerRow;
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
            visiblePreviewStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewStart < 0) {
                visiblePreviewStart = 0;
            }
            if (visibleCodeEnd < 0) {
                visibleCharStart = visiblePreviewStart + previewCharPos;
            }
            visiblePreviewEnd = (dataViewWidth + (scrollPosition.getCharPosition() + 1) * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewEnd > bytesPerRow) {
                visiblePreviewEnd = bytesPerRow;
            }
            if (visiblePreviewEnd >= 0) {
                visibleCharEnd = visiblePreviewEnd + previewCharPos;
            }
        } else {
            visiblePreviewStart = 0;
            visiblePreviewEnd = -1;
        }

        updateRowDataCache();
    }

    private void updateRowDataCache() {
        if (rowDataCache == null) {
            rowDataCache = new RowDataCache();
        }

        rowDataCache.rowData = new byte[bytesPerRow + maxBytesPerChar - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[charactersPerRow];
    }

    public void resetFont(@Nonnull Graphics g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) worker).getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        maxBytesPerChar = (int) encoder.maxBytesPerChar();

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
        rowHeight = fontSize + subFontSpace;

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

        if (characterWidth > 0) {
            resetCharPositions();
        }

        verticalScrollUnit = ((ScrollingCapable) worker).getVerticalScrollUnit();
        verticalScrollBarVisibility = ((ScrollingCapable) worker).getVerticalScrollBarVisibility();
        horizontalScrollUnit = ((ScrollingCapable) worker).getHorizontalScrollUnit();
        horizontalScrollBarVisibility = ((ScrollingCapable) worker).getHorizontalScrollBarVisibility();

        if (rowHeight > 0 && characterWidth > 0) {
            int documentDataWidth = charactersPerRow * characterWidth;
            long rowsPerData = (dataSize / bytesPerRow) + 1;

            int documentDataHeight;
            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                documentDataHeight = Integer.MAX_VALUE;
            } else {
                scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                documentDataHeight = (int) (rowsPerData * rowHeight);
            }

            dataView.setPreferredSize(new Dimension(documentDataWidth, documentDataHeight));
        }

        // TODO on resize only
        scrollPanel.setBounds(getScrollPanelRectangle());
        scrollPanel.revalidate();
    }

    private void resetSizes() {
        if (fontMetrics == null) {
            headerAreaHeight = 0;
        } else {
            int fontHeight = fontMetrics.getHeight();
            headerAreaHeight = fontHeight + fontHeight / 4;
        }

        componentWidth = worker.getCodeArea().getWidth();
        componentHeight = worker.getCodeArea().getHeight();
        rowPositionAreaWidth = characterWidth * (rowPositionLength + 1);
        rowPositionLength = getRowPositionLength();
        dataViewX = rowPositionAreaWidth;
        dataViewY = headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - getVerticalScrollBarSize();
        dataViewHeight = scrollPanelHeight - getHorizontalScrollBarSize();
    }

    @Override
    public void resetColors() {
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
        colors.selectionMirrorBackground = CodeAreaSwingUtils.computeGrayColor(colors.selectionBackground);
        colors.cursor = UIManager.getColor("TextArea.caretForeground");
        if (colors.cursor == null) {
            colors.cursor = Color.BLACK;
        }
        colors.negativeCursor = CodeAreaSwingUtils.createNegativeColor(colors.cursor);
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
        if (rowDataCache == null) {
            return;
        }
        if (scrollingState == ScrollingState.SCROLLING_BY_SCROLLBAR) {
            return;
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintRowPosition(g);
        paintMainArea(g);
    }

    public void paintOutsiteArea(@Nonnull Graphics g) {
        g.setColor(colors.background);
        g.fillRect(0, 0, componentWidth, headerAreaHeight);

        // Decoration lines
        g.setColor(colors.decorationLine);
        g.drawLine(0, headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1);

        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, 0, lineX, headerAreaHeight);
        }
    }

    public void paintHeader(@Nonnull Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle headerArea = new Rectangle(rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        g.setClip(clipBounds != null ? headerArea.intersection(clipBounds) : headerArea);

        g.setFont(font);
        g.setColor(colors.background);
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        // Decoration lines
        g.setColor(colors.decorationLine);
        g.fillRect(0, headerAreaHeight - 1, componentWidth, 1);
        int lineX = dataViewX + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, 0, lineX, headerAreaHeight);
        }

        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int charactersPerCodeArea = computeFirstCodeCharacterPos(bytesPerRow);
            int headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            int headerY = rowHeight - subFontSpace;

            int visibleHeaderCharStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleHeaderCharStart < 0) {
                visibleHeaderCharStart = 0;
            }
            int visibleHeaderCharEnd = (dataViewWidth + (scrollPosition.getCharPosition() + charactersPerCodeArea) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleHeaderCharEnd > charactersPerCodeArea) {
                visibleHeaderCharEnd = charactersPerCodeArea;
            }
            int visibleStart = computePositionByte(visibleHeaderCharStart);
            int visibleEnd = computePositionByte(visibleHeaderCharEnd - 1) + 1;

            g.setColor(colors.foreground);
            char[] headerChars = new char[charactersPerCodeArea];
            Arrays.fill(headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            for (int index = visibleStart; index < visibleEnd; index++) {
                int codePos = computeFirstCodeCharacterPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, hexCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int renderOffset = visibleHeaderCharStart;
            Color renderColor = null;
            for (int characterOnRow = visibleHeaderCharStart; characterOnRow < visibleHeaderCharEnd; characterOnRow++) {
                int byteOnRow;
                byteOnRow = computePositionByte(characterOnRow);
                boolean sequenceBreak = false;
                boolean nativeWidth = true;

                int currentCharWidth = 0;
                char currentChar = headerChars[characterOnRow];
                if (currentChar == ' ' && renderOffset == characterOnRow) {
                    renderOffset++;
                    continue;
                }
                if (monospaceFont) { // characterRenderingMode == CharacterRenderingMode.AUTO && 
                    // Detect if character is in unicode range covered by monospace fonts
                    if (CodeAreaSwingUtils.isMonospaceFullWidthCharater(currentChar)) {
                        currentCharWidth = characterWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = fontMetrics.charWidth(currentChar);
                    nativeWidth = currentCharWidth == characterWidth;
                }

                Color color = colors.foreground;
//                getHeaderPositionColor(byteOnRow, charOnRow);
//                if (renderColorType == null) {
//                    renderColorType = colorType;
//                    renderColor = color;
//                    g.setColor(color);
//                }

                if (!nativeWidth || !CodeAreaSwingUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        g.drawChars(headerChars, renderOffset, characterOnRow - renderOffset, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setColor(color);
                    }

                    if (!nativeWidth) {
                        renderOffset = characterOnRow + 1;
                        int positionX = headerX + characterOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
                        drawShiftedChar(g, headerChars, characterOnRow, characterWidth, positionX, headerY);
                    } else {
                        renderOffset = characterOnRow;
                    }
                }
            }

            if (renderOffset < charactersPerCodeArea) {
                g.drawChars(headerChars, renderOffset, charactersPerCodeArea - renderOffset, headerX + renderOffset * characterWidth, headerY);
            }
        }

        g.setClip(clipBounds);
    }

    public void paintRowPosition(@Nonnull Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        Rectangle rowPositionsArea = new Rectangle(0, headerAreaHeight, rowPositionAreaWidth, componentHeight - headerAreaHeight - getHorizontalScrollBarSize());
        g.setClip(clipBounds != null ? rowPositionsArea.intersection(clipBounds) : rowPositionsArea);

        g.setFont(font);
        g.setColor(colors.background);
        g.fillRect(rowPositionsArea.x, rowPositionsArea.y, rowPositionsArea.width, rowPositionsArea.height);

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colors.stripes);
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize - bytesPerRow) {
                    break;
                }

                g.fillRect(0, stripePositionY, rowPositionAreaWidth, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        int positionY = headerAreaHeight + rowHeight - subFontSpace - scrollPosition.getRowOffset();
        g.setColor(colors.foreground);
        Rectangle compRect = new Rectangle();
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, codeType.getBase(), rowPositionLength, true, CodeCharactersCase.UPPER);
            for (int digitIndex = 0; digitIndex < rowPositionLength; digitIndex++) {
                drawCenteredChar(g, rowDataCache.rowPositionCode, digitIndex, characterWidth, compRect.x + characterWidth * digitIndex, positionY);
            }

            positionY += rowHeight;
            dataPosition += bytesPerRow;
        }

        g.setColor(colors.decorationLine);
        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, dataViewY, lineX, dataViewY + dataViewHeight);
        }
        g.drawLine(dataViewX, dataViewY - 1, dataViewX + dataViewWidth, dataViewY - 1);

        g.setClip(clipBounds);
    }

    @Override
    public void paintMainArea(@Nonnull Graphics g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            resetFont(g);
            fontChanged = false;
        }

        Rectangle clipBounds = g.getClipBounds();
        Rectangle mainArea = getMainAreaRect();
        g.setClip(clipBounds != null ? mainArea.intersection(clipBounds) : mainArea);
        paintBackground(g);

        g.setColor(colors.decorationLine);
        int lineX = dataViewX + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX + dataViewOffsetX, dataViewY + dataViewOffsetY, lineX + dataViewOffsetX, dataViewY + dataViewOffsetY + dataViewHeight);
        }

        paintRows(g);
        g.setClip(clipBounds);
        paintCursor(g);

        // TODO: Remove later
//        int x = componentWidth - rowPositionAreaWidth - 220;
//        int y = componentHeight - headerAreaHeight - 20;
//        g.setColor(Color.YELLOW);
//        g.fillRect(x, y, 200, 16);
//        g.setColor(Color.BLACK);
//        char[] headerCode = (String.valueOf(scrollPosition.getScrollCharPosition()) + "+" + String.valueOf(scrollPosition.getScrollCharOffset()) + " : " + String.valueOf(scrollPosition.getScrollRowPosition()) + "+" + String.valueOf(scrollPosition.getScrollRowOffset()) + " P: " + String.valueOf(rowsPerRect)).toCharArray();
//        g.drawChars(headerCode, 0, headerCode.length, x, y + rowHeight);
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(@Nonnull Graphics g) {
        int rowPositionX = rowPositionAreaWidth;
        g.setColor(colors.background);
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.fillRect(rowPositionX, headerAreaHeight, dataViewWidth, dataViewHeight);
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + (int) ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colors.stripes);
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize - bytesPerRow) {
                    break;
                }

                g.fillRect(dataViewOffsetX + rowPositionX, dataViewOffsetY + stripePositionY, dataViewWidth, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(@Nonnull Graphics g) {
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int rowPositionX = rowPositionAreaWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = headerAreaHeight - scrollPosition.getRowOffset();
        g.setColor(colors.foreground);
        for (int row = 0; row <= rowsPerRect; row++) {
            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
            paintRowText(g, dataPosition, rowPositionX, rowPositionY);

            rowPositionY += rowHeight;
            dataPosition += bytesPerRow;
        }
    }

    private void prepareRowData(long dataPosition) {
        int rowBytesLimit = bytesPerRow;
        int rowStart = 0;
        if (dataPosition < dataSize) {
            int rowDataSize = bytesPerRow + maxBytesPerChar - 1;
            if (dataPosition + rowDataSize > dataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                rowStart = (int) -dataPosition;
            }
            BinaryData data = worker.getCodeArea().getContentData();
            if (data == null) {
                throw new IllegalStateException("Missing data on nonzero data size");
            }
            data.copyToArray(dataPosition + rowStart, rowDataCache.rowData, rowStart, rowDataSize - rowStart);
            if (dataPosition + rowBytesLimit > dataSize) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            for (int byteOnRow = Math.max(visibleCodeStart, rowStart); byteOnRow < Math.min(visibleCodeEnd, rowBytesLimit); byteOnRow++) {
                byte dataByte = rowDataCache.rowData[byteOnRow];
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, computeFirstCodeCharacterPos(byteOnRow), hexCharactersCase);
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
            }
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            for (int byteOnRow = visiblePreviewStart; byteOnRow < Math.min(visiblePreviewEnd, rowBytesLimit); byteOnRow++) {
                byte dataByte = rowDataCache.rowData[byteOnRow];

                if (maxBytesPerChar > 1) {
                    if (dataPosition + maxBytesPerChar > dataSize) {
                        maxBytesPerChar = (int) (dataSize - dataPosition);
                    }

                    int charDataLength = maxBytesPerChar;
                    if (byteOnRow + charDataLength > rowDataCache.rowData.length) {
                        charDataLength = rowDataCache.rowData.length - byteOnRow;
                    }
                    String displayString = new String(rowDataCache.rowData, byteOnRow, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        rowDataCache.rowCharacters[previewCharPos + byteOnRow] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        buildCharMapping(charset);
                    }

                    rowDataCache.rowCharacters[previewCharPos + byteOnRow] = charMapping[dataByte & 0xFF];
                }
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, previewCharPos + rowBytesLimit, previewCharPos + bytesPerRow, ' ');
            }
        }
    }

    /**
     * Paints row background.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowBackground(@Nonnull Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int renderOffset = visibleCharStart;
        Color renderColor = null;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
            int section;
            int byteOnRow;
            if (charOnRow >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
            } else {
                byteOnRow = computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
            }
            boolean sequenceBreak = false;

            Color color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, dataViewOffsetX + renderOffset, dataViewOffsetY + charOnRow, rowPositionX, rowPositionY);
                    }
                }

                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.setColor(color);
                    }
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, charactersPerRow, dataViewOffsetX + rowPositionX, dataViewOffsetY + rowPositionY);
            }
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @return color or null for default color
     */
    @Nullable
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, int section) {
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX.getSection())) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == caretPosition.getSection() ? colors.selectionBackground : colors.selectionMirrorBackground;
        }

        return null;
    }

    @Nullable
    @Override
    public CodeAreaScrollPosition computeRevealScrollPosition(@Nonnull CaretPosition caretPosition) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);
        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        boolean scrolled = false;
        if (rowPosition < scrollPosition.getRowPosition()) {
            // Scroll row up
            targetScrollPosition.setRowPosition(rowPosition);
            targetScrollPosition.setRowOffset(0);
            scrolled = true;
        } else if ((rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() > 0)) {
            // Scroll row offset up
            targetScrollPosition.setRowOffset(0);
            scrolled = true;
        } else {
            int bottomRowOffset;
            if (verticalScrollUnit == VerticalScrollUnit.ROW) {
                bottomRowOffset = 0;
            } else {
                if (dataViewHeight < rowHeight) {
                    bottomRowOffset = 0;
                } else {
                    bottomRowOffset = dataViewHeight % rowHeight;
                }
            }

            if (rowPosition > scrollPosition.getRowPosition() + rowsPerPage) {
                // Scroll row down
                long targetRowPosition = rowPosition - rowsPerPage;
                if (verticalScrollUnit == VerticalScrollUnit.ROW && (dataViewHeight % rowHeight) > 0) {
                    targetRowPosition++;
                }
                targetScrollPosition.setRowPosition(targetRowPosition);
                targetScrollPosition.setRowOffset(bottomRowOffset);
                scrolled = true;
            } else if (rowPosition == scrollPosition.getRowPosition() + rowsPerPage && scrollPosition.getRowOffset() > bottomRowOffset) {
                // Scroll row offset down
                targetScrollPosition.setRowOffset(bottomRowOffset);
                scrolled = true;
            }
        }

        if (charPosition < scrollPosition.getCharPosition()) {
            // Scroll characters left
            targetScrollPosition.setCharPosition(charPosition);
            targetScrollPosition.setCharOffset(0);
            scrolled = true;
        } else if (charPosition == scrollPosition.getCharPosition() && scrollPosition.getCharOffset() > 0) {
            // Scroll character offset left
            targetScrollPosition.setCharOffset(0);
            scrolled = true;
        } else {
            int rightCharOffset;
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                rightCharOffset = 0;
            } else {
                if (dataViewWidth < characterWidth) {
                    rightCharOffset = 0;
                } else {
                    rightCharOffset = dataViewWidth % characterWidth;
                }
            }

            if (charPosition > scrollPosition.getCharPosition() + charactersPerPage) {
                // Scroll character right
                targetScrollPosition.setCharPosition(charPosition - charactersPerPage);
                targetScrollPosition.setCharOffset(rightCharOffset);
                scrolled = true;
            } else if (charPosition == scrollPosition.getCharPosition() + charactersPerPage && scrollPosition.getCharOffset() > rightCharOffset) {
                // Scroll row offset down
                targetScrollPosition.setCharOffset(rightCharOffset);
                scrolled = true;
            }
        }
        return scrolled ? targetScrollPosition : null;
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(@Nonnull CaretPosition caretPosition) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);
        long shiftedPosition = caretPosition.getDataPosition();
        long currentRowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int currentCharPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            currentCharPosition = previewCharPos + byteOffset;
        } else {
            currentCharPosition = computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        long rowPosition = currentRowPosition - rowsPerRect / 2;
        int rowCorrection = (rowsPerRect & 1) == 0 ? rowHeight : 0;
        int heightDiff = (rowsPerRect * rowHeight + rowCorrection - dataViewHeight) / 2;
        int rowOffset;
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            rowOffset = 0;
        } else {
            if (heightDiff > 0) {
                rowOffset = heightDiff;
            } else {
                rowOffset = 0;
            }
        }
        if (rowPosition < 0) {
            rowPosition = 0;
            rowOffset = 0;
        } else if (rowPosition > maximumScrollPosition.getRowPosition() || (rowPosition == maximumScrollPosition.getRowPosition() && rowOffset > maximumScrollPosition.getRowOffset())) {
            rowPosition = maximumScrollPosition.getRowPosition();
            rowOffset = maximumScrollPosition.getRowOffset();
        }
        targetScrollPosition.setRowPosition(rowPosition);
        targetScrollPosition.setRowOffset(rowOffset);

        int charPosition = currentCharPosition - charactersPerRect / 2;
        int charCorrection = (charactersPerRect & 1) == 0 ? rowHeight : 0;
        int widthDiff = (charactersPerRect * characterWidth + charCorrection - dataViewWidth) / 2;
        int charOffset;
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            charOffset = 0;
        } else {
            if (widthDiff > 0) {
                charOffset = widthDiff;
            } else {
                charOffset = 0;
            }
        }
        if (charPosition < 0) {
            charPosition = 0;
            charOffset = 0;
        } else if (charPosition > maximumScrollPosition.getCharPosition() || (charPosition == maximumScrollPosition.getCharPosition() && charOffset > maximumScrollPosition.getCharOffset())) {
            charPosition = maximumScrollPosition.getCharPosition();
            charOffset = maximumScrollPosition.getCharOffset();
        }
        targetScrollPosition.setCharPosition(charPosition);
        targetScrollPosition.setCharOffset(charOffset);
        return targetScrollPosition;
    }

    /**
     * Paints row text.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowText(@Nonnull Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        g.setFont(font);
        int positionY = rowPositionY + rowHeight - subFontSpace;

        Color lastColor = null;
        Color renderColor = null;

        int renderOffset = visibleCharStart;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
            int section;
            int byteOnRow;
            if (charOnRow >= previewCharPos) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
            } else {
                byteOnRow = computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
            }

            Color color = getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (color == null) {
                color = colors.foreground;
            }

            boolean sequenceBreak = false;
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            int currentCharWidth = 0;
            char currentChar = rowDataCache.rowCharacters[charOnRow];
            if (currentChar == ' ' && renderOffset == charOnRow) {
                renderOffset++;
                continue;
            }

            if (monospaceFont) {
                // Detect if character is in unicode range covered by monospace fonts
                if (CodeAreaSwingUtils.isMonospaceFullWidthCharater(currentChar)) {
                    currentCharWidth = characterWidth;
                }
            }

            boolean nativeWidth = true;
            if (currentCharWidth == 0) {
                currentCharWidth = fontMetrics.charWidth(currentChar);
                nativeWidth = currentCharWidth == characterWidth;
            }

            if (!nativeWidth) {
                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                    g.setColor(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    renderCharSequence(g, renderOffset, charOnRow, dataViewOffsetX + rowPositionX, dataViewOffsetY + positionY);
                }

                renderColor = color;
                if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                    g.setColor(renderColor);
                    lastColor = renderColor;
                }

                if (!nativeWidth) {
                    renderOffset = charOnRow + 1;
                    int positionX = rowPositionX + charOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
                    drawShiftedChar(g, rowDataCache.rowCharacters, charOnRow, characterWidth, dataViewOffsetX + positionX, dataViewOffsetY + positionY);
                } else {
                    renderOffset = charOnRow;
                }
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                g.setColor(renderColor);
            }

            renderCharSequence(g, renderOffset, charactersPerRow, rowPositionX + dataViewOffsetX, positionY + dataViewOffsetY);
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @return color or null for default color
     */
    @Nullable
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, int section) {
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
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
        if (cursorDataCache == null) {
            cursorDataCache = new CursorDataCache();
        }
        int cursorCharsLength = codeType.getMaxDigitsForByte();
        if (cursorDataCache.cursorCharsLength != cursorCharsLength) {
            cursorDataCache.cursorCharsLength = cursorCharsLength;
            cursorDataCache.cursorChars = new char[cursorCharsLength];
        }
        int cursorDataLength = maxBytesPerChar;
        if (cursorDataCache.cursorDataLength != cursorDataLength) {
            cursorDataCache.cursorDataLength = cursorDataLength;
            cursorDataCache.cursorData = new byte[cursorDataLength];
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) worker).getCaret();
        Rectangle cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        Rectangle scrolledCursorRect = new Rectangle();
        scrolledCursorRect.x = cursorRect.x + dataViewOffsetX;
        scrolledCursorRect.y = cursorRect.y + dataViewOffsetY;
        scrolledCursorRect.width = cursorRect.width;
        scrolledCursorRect.height = cursorRect.height;

        Rectangle clipBounds = g.getClipBounds();
        Rectangle mainAreaRect = getMainAreaRect();
        mainAreaRect.x += dataViewOffsetX;
        mainAreaRect.y += dataViewOffsetY;
        Rectangle intersection = mainAreaRect.intersection(scrolledCursorRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.setClip(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setColor(colors.cursor);

            paintCursorRect(g, intersection.x, intersection.y, intersection.width, intersection.height, renderingMode, caret);
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
                    g2d.setStroke(cursorDataCache.dashedStroke);
                    g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
                }
            }
        }
        g.setClip(clipBounds);
    }

    private void paintCursorRect(@Nonnull Graphics g, int cursorX, int cursorY, int width, int height, @Nonnull CursorRenderingMode renderingMode, @Nonnull DefaultCodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRect(cursorX, cursorY, width, height);
                break;
            }
            case XOR: {
                g.setXORMode(colors.background);
                g.fillRect(cursorX, cursorY, width, height);
                g.setPaintMode();
                break;
            }
            case NEGATIVE: {
                g.fillRect(cursorX, cursorY, width, height);
                g.setColor(colors.negativeCursor);
                BinaryData contentData = worker.getCodeArea().getContentData();
                int row = (cursorY + scrollPosition.getRowOffset() - dataViewY) / rowHeight;
                int scrolledX = cursorX + scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
                int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
                    int charPos = (scrolledX - previewRelativeX) / characterWidth;
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    char[] previewChars = new char[1];

                    if (maxBytesPerChar > 1) {
                        int charDataLength = maxBytesPerChar;
                        if (dataPosition + maxBytesPerChar > dataSize) {
                            charDataLength = (int) (dataSize - dataPosition);
                        }

                        if (contentData == null) {
                            previewChars[0] = ' ';
                        } else {
                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            String displayString = new String(cursorDataCache.cursorData, 0, charDataLength, charset);
                            if (!displayString.isEmpty()) {
                                previewChars[0] = displayString.charAt(0);
                            }
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != charset) {
                            buildCharMapping(charset);
                        }

                        if (contentData == null) {
                            previewChars[0] = ' ';
                        } else {
                            previewChars[0] = charMapping[contentData.getByte(dataPosition) & 0xFF];
                        }
                    }
                    int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    drawCenteredChar(g, previewChars, 0, characterWidth, dataViewOffsetX + posX, dataViewOffsetY + posY);
                } else {
                    int charPos = (scrolledX - dataViewX) / characterWidth;
                    int byteOffset = computePositionByte(charPos);
                    int codeCharPos = computeFirstCodeCharacterPos(byteOffset);

                    if (contentData != null && dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, hexCharactersCase);
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
                    int posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChar(g, cursorDataCache.cursorChars, charsOffset, characterWidth, dataViewOffsetX + posX + (charsOffset * characterWidth), dataViewOffsetY + posY);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
        }
    }

    @Nonnull
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY) {
        CodeAreaCaretPosition caret = new CodeAreaCaretPosition();
        if (positionX < rowPositionAreaWidth) {
            positionX = rowPositionAreaWidth;
        }
        int cursorCharX = (positionX - rowPositionAreaWidth + scrollPosition.getCharOffset()) / characterWidth + scrollPosition.getCharPosition();
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        if (positionY < headerAreaHeight) {
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getRowOffset()) / rowHeight + scrollPosition.getRowPosition();
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnRow;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(BasicCodeAreaSection.CODE_MATRIX.getSection());
            byteOnRow = computePositionByte(cursorCharX);
            if (byteOnRow >= bytesPerRow) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - computeFirstCodeCharacterPos(byteOnRow);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(BasicCodeAreaSection.TEXT_PREVIEW.getSection());
            byteOnRow = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnRow -= previewCharPos;
            }
        }

        if (byteOnRow >= bytesPerRow) {
            byteOnRow = bytesPerRow - 1;
        }

        dataPosition = byteOnRow + (cursorRowY * bytesPerRow);
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
    public CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementDirection direction) {
        CodeAreaCaretPosition target = new CodeAreaCaretPosition(position.getDataPosition(), position.getCodeOffset(), position.getSection());
        switch (direction) {
            case LEFT: {
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
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
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
                    int codeOffset = position.getCodeOffset();
                    if (position.getDataPosition() < dataSize && codeOffset < codeType.getMaxDigitsForByte() - 1) {
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
                if (position.getDataPosition() >= bytesPerRow) {
                    target.setDataPosition(position.getDataPosition() - bytesPerRow);
                }
                break;
            }
            case DOWN: {
                if (position.getDataPosition() + bytesPerRow < dataSize || (position.getDataPosition() + bytesPerRow == dataSize && position.getCodeOffset() == 0)) {
                    target.setDataPosition(position.getDataPosition() + bytesPerRow);
                }
                break;
            }
            case ROW_START: {
                long dataPosition = position.getDataPosition();
                dataPosition -= (dataPosition % bytesPerRow);
                target.setDataPosition(dataPosition);
                target.setCodeOffset(0);
                break;
            }
            case ROW_END: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow - 1 - (dataPosition % bytesPerRow);
                if (dataPosition > Long.MAX_VALUE - increment || dataPosition + increment > dataSize) {
                    target.setDataPosition(dataSize);
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
                    if (target.getDataPosition() == dataSize) {
                        target.setCodeOffset(0);
                    } else {
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                }
                break;
            }
            case PAGE_UP: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow * rowsPerPage;
                if (dataPosition < increment) {
                    target.setDataPosition(dataPosition % bytesPerRow);
                } else {
                    target.setDataPosition(dataPosition - increment);
                }
                break;
            }
            case PAGE_DOWN: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow * rowsPerPage;
                if (dataPosition > dataSize - increment) {
                    long positionOnRow = dataPosition % bytesPerRow;
                    long rowDataStart = dataSize / bytesPerRow;
                    if (rowDataStart == dataSize - positionOnRow) {
                        target.setDataPosition(dataSize);
                        target.setCodeOffset(0);
                    } else if (rowDataStart > dataSize - positionOnRow) {
                        if (rowDataStart > bytesPerRow) {
                            rowDataStart -= bytesPerRow;
                            target.setDataPosition(rowDataStart + positionOnRow);
                        }
                    } else {
                        target.setDataPosition(rowDataStart + positionOnRow);
                    }
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
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
                int activeSection = caretPosition.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection() ? BasicCodeAreaSection.TEXT_PREVIEW.getSection() : BasicCodeAreaSection.CODE_MATRIX.getSection();
                if (activeSection == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
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

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull ScrollingDirection direction) {
        CodeAreaScrollPosition targetPosition = new CodeAreaScrollPosition();
        targetPosition.setScrollPosition(startPosition);

        switch (direction) {
            case UP: {
                if (startPosition.getRowPosition() == 0) {
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - 1);
                }
                break;
            }
            case DOWN: {
                if (maximumScrollPosition.isRowPositionGreaterThan(startPosition)) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + 1);
                }
                break;
            }
            case LEFT: {
                if (startPosition.getCharPosition() == 0) {
                    targetPosition.setCharOffset(0);
                } else {
                    targetPosition.setCharPosition(startPosition.getCharPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (maximumScrollPosition.isCharPositionGreaterThan(startPosition)) {
                    targetPosition.setCharPosition(startPosition.getCharPosition() + 1);
                }
                break;
            }
            case PAGE_UP: {
                if (startPosition.getRowPosition() < rowsPerPage) {
                    targetPosition.setRowPosition(0);
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - rowsPerPage);
                }
                break;
            }
            case PAGE_DOWN: {
                if (startPosition.getRowPosition() <= rowsPerDocument - rowsPerPage * 2) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + rowsPerPage);
                } else if (rowsPerDocument > rowsPerPage) {
                    targetPosition.setRowPosition(rowsPerDocument - rowsPerPage);
                } else {
                    targetPosition.setRowPosition(0);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected scrolling direction type: " + direction.name());
        }

        return targetPosition;
    }

    @Nonnull
    private CodeAreaScrollPosition computeMaximumScrollPosition() {
        CodeAreaScrollPosition position = new CodeAreaScrollPosition();
        if (rowHeight == 0 || characterWidth == 0) {
            return position;
        }
        if (rowsPerDocument > rowsPerPage) {
            position.setRowPosition(rowsPerDocument - rowsPerPage);
        }
        if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
            position.setRowOffset(dataViewHeight % rowHeight);
        }

        if (charactersPerRow > charactersPerPage) {
            position.setCharPosition(charactersPerRow - charactersPerPage);
        }
        if (horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
            position.setCharOffset(dataViewWidth % characterWidth);
        }

        return position;
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
    public Point getPositionPoint(long dataPosition, int codeOffset, int section) {
        long row = dataPosition / bytesPerRow - scrollPosition.getRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + row * rowHeight) - scrollPosition.getRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            caretX = dataViewRect.x + previewRelativeX + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.x + characterWidth * (computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new Point(caretX, caretY);
    }

    @Nullable
    private Rectangle getMirrorCursorRect(long dataPosition, int section) {
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == BasicCodeAreaSection.CODE_MATRIX.getSection() ? BasicCodeAreaSection.TEXT_PREVIEW.getSection() : BasicCodeAreaSection.CODE_MATRIX.getSection());
        if (mirrorCursorPoint == null) {
            return null;
        }

        Rectangle mirrorCursorRect = new Rectangle(mirrorCursorPoint.x, mirrorCursorPoint.y, characterWidth * (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection() ? codeType.getMaxDigitsForByte() : 1), rowHeight);
        return mirrorCursorRect;
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
            return Cursor.TEXT_CURSOR;
        }

        return Cursor.DEFAULT_CURSOR;
    }

    @Override
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= headerAreaHeight) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            return BasicCodeAreaZone.ROW_POSITIONS;
        }

        if (positionX >= dataViewX + scrollPanelWidth && positionY < dataViewY + scrollPanelHeight) {
            return BasicCodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= dataViewY + scrollPanelHeight) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.BOTTOM_LEFT_CORNER;
            } else if (positionX >= dataViewX + scrollPanelWidth) {
                return BasicCodeAreaZone.SCROLLBAR_CORNER;
            }

            return BasicCodeAreaZone.HORIZONTAL_SCROLLBAR;
        }

        return BasicCodeAreaZone.CODE_AREA;
    }

    @Nonnull
    private Rectangle getScrollPanelRectangle() {
        return new Rectangle(dataViewX, dataViewY, scrollPanelWidth, scrollPanelHeight);
    }

    @Nonnull
    public Rectangle getDataViewRectangle() {
        return new Rectangle(dataViewX, dataViewY, dataViewWidth, dataViewHeight);
    }

    public int computePositionByte(int rowCharPosition) {
        return rowCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
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
     * @param positionX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChar(@Nonnull Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        int charWidth = fontMetrics.charWidth(drawnChars[charOffset]);
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, positionX + ((charWidthSpace + 1 - charWidth) >> 1), positionY);
    }

    protected void drawShiftedChar(@Nonnull Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        g.drawChars(drawnChars, charOffset, 1, positionX, positionY);
    }

    private void buildCharMapping(@Nonnull Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private int getRowPositionLength() {
        if (rowPositionNumberLength <= 0) {
            if (dataSize == 0) {
                return 1;
            }

            double natLog = Math.log(dataSize == Long.MAX_VALUE ? dataSize : dataSize + 1);
            int numberLength = (int) Math.ceil(natLog / PositionCodeType.HEXADECIMAL.getBaseLog());
            return numberLength == 0 ? 1 : numberLength;
        }
        return rowPositionNumberLength;
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
    public Rectangle getPositionRect(long dataPosition, int codeOffset, int section) {
        Point cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            return null;
        }

        DefaultCodeAreaCaret.CursorShape cursorShape = editationMode == EditationMode.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
        int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
        return new Rectangle(cursorPoint.x, cursorPoint.y, cursorThickness, rowHeight);
    }

    /**
     * Render sequence of characters.
     *
     * Doesn't include character at offset end.
     */
    private void renderCharSequence(@Nonnull Graphics g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        g.drawChars(rowDataCache.rowCharacters, startOffset, endOffset - startOffset, rowPositionX + startOffset * characterWidth, positionY);
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(@Nonnull Graphics g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        g.fillRect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    private int computeRowsPerRectangle() {
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage() {
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }

    private int computeBytesPerRow() {
        int computedBytesPerRow;
        if (rowWrapping == RowWrappingMode.WRAPPING) {
            int charactersPerByte = 0;
            if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
                charactersPerByte += codeType.getMaxDigitsForByte();
            }
            if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
                charactersPerByte++;
            }
            computedBytesPerRow = (charactersPerPage - (viewMode == CodeAreaViewMode.DUAL ? 1 : 0)) / charactersPerByte;

            if (computedBytesPerRow > maxBytesPerLine) {
                computedBytesPerRow = maxBytesPerLine;
            }

            if (wrappingBytesGroupSize > 1) {
                int wrappingBytesGroupOffset = computedBytesPerRow % wrappingBytesGroupSize;
                if (wrappingBytesGroupOffset > 0) {
                    computedBytesPerRow -= wrappingBytesGroupOffset;
                }
            }
        } else {
            computedBytesPerRow = maxBytesPerLine;
        }

        if (computedBytesPerRow < 1) {
            computedBytesPerRow = 1;
        }

        return computedBytesPerRow;
    }

    private int computeCharactersPerRow() {
        int charsPerRow = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeCharPos(bytesPerRow - 1) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return charsPerRow;
    }

    private long computeRowsPerDocument() {
        return dataSize / bytesPerRow + (dataSize % bytesPerRow > 0 ? 1 : 0);
    }

    private int computeCharactersPerRectangle() {
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage() {
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    @Override
    public void updateScrollBars() {
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        scrollPanel.setVerticalScrollBarPolicy(CodeAreaSwingUtils.getVerticalScrollBarPolicy(verticalScrollBarVisibility));
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        scrollPanel.setHorizontalScrollBarPolicy(CodeAreaSwingUtils.getHorizontalScrollBarPolicy(horizontalScrollBarVisibility));

        if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
            int scrollValue;
            if (scrollPosition.getCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                scrollValue = (int) ((scrollPosition.getRowPosition() * Integer.MAX_VALUE) / rowsPerDocument);
            } else {
                scrollValue = (int) (scrollPosition.getRowPosition() / (rowsPerDocument / Integer.MAX_VALUE));
            }
            verticalScrollBar.setValue(scrollValue);
        } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            verticalScrollBar.setValue((int) scrollPosition.getRowPosition() * rowHeight);
        } else {
            verticalScrollBar.setValue((int) (scrollPosition.getRowPosition() * rowHeight + scrollPosition.getRowOffset()));
        }

        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            horizontalScrollBar.setValue(scrollPosition.getCharPosition() * characterWidth);
        } else {
            horizontalScrollBar.setValue(scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset());
        }
    }

    @Nonnull
    private Rectangle getMainAreaRect() {
        return new Rectangle(rowPositionAreaWidth, headerAreaHeight, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - headerAreaHeight - getHorizontalScrollBarSize());
    }

    private int getHorizontalScrollBarSize() {
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        int size;
        if (horizontalScrollBar.isVisible()) {
            size = horizontalScrollBar.getHeight();
        } else {
            size = 0;
        }

        return size;
    }

    private int getVerticalScrollBarSize() {
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        int size;
        if (verticalScrollBar.isVisible()) {
            size = verticalScrollBar.getWidth();
        } else {
            size = 0;
        }

        return size;
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
            if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
                long rowsPerDocumentToLastPage = rowsPerDocument - computeRowsPerRectangle();
                long targetRow;
                if (scrollBarValue > 0 && rowsPerDocumentToLastPage > maxValue / scrollBarValue) {
                    targetRow = scrollBarValue * (rowsPerDocumentToLastPage / maxValue);
                    long rest = rowsPerDocumentToLastPage % maxValue;
                    targetRow += (rest * scrollBarValue) / maxValue;
                } else {
                    targetRow = (scrollBarValue * rowsPerDocumentToLastPage) / Integer.MAX_VALUE;
                }
                scrollPosition.setRowPosition(targetRow);
                if (verticalScrollUnit != VerticalScrollUnit.ROW) {
                    scrollPosition.setRowOffset(0);
                }
            } else {
                if (rowHeight == 0) {
                    scrollPosition.setRowPosition(0);
                    scrollPosition.setRowOffset(0);
                } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
                    int rowPosition = scrollBarValue / rowHeight;
                    if (scrollBarValue % rowHeight > 0) {
                        rowPosition++;
                    }
                    scrollPosition.setRowPosition(rowPosition);
                    scrollPosition.setRowOffset(0);
                } else {
                    scrollPosition.setRowPosition(scrollBarValue / rowHeight);
                    scrollPosition.setRowOffset(scrollBarValue % rowHeight);
                }
            }

            // TODO
            ((ScrollingCapable) worker).setScrollPosition(scrollPosition);
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
            int scrollBarValue = scrollPanel.getHorizontalScrollBar().getValue();
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                scrollPosition.setCharPosition(scrollBarValue);
            } else {
                if (characterWidth == 0) {
                    scrollPosition.setCharPosition(0);
                    scrollPosition.setCharOffset(0);
                } else if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                    int charPosition = scrollBarValue / characterWidth;
                    if (scrollBarValue % characterWidth > 0) {
                        charPosition++;
                    }
                    scrollPosition.setCharPosition(charPosition);
                    scrollPosition.setCharOffset(0);
                } else {
                    scrollPosition.setCharPosition(scrollBarValue / characterWidth);
                    scrollPosition.setCharOffset(scrollBarValue % characterWidth);
                }
            }

            ((ScrollingCapable) worker).setScrollPosition(scrollPosition);
            notifyScrolled();
            worker.getCodeArea().repaint();
//            dataViewScrolled(codeArea.getGraphics());
        }
    }

    private void notifyScrolled() {
        resetScrollState();
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

    private static class RowDataCache {

        private byte[] rowData;
        private char[] rowPositionCode;
        private char[] rowCharacters;
    }

    private static class CursorDataCache {

        final Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }

    /**
     * Enumeration of vertical scaling modes.
     */
    protected enum ScrollBarVerticalScale {
        /**
         * Normal ratio 1 on 1.
         */
        NORMAL,
        /**
         * Height is more than available range and scaled.
         */
        SCALED
    }

    protected enum ScrollingState {
        NO_SCROLLING,
        SCROLLING_BY_SCROLLBAR,
        SCROLLING_BY_MOVEMENT
    }
}
