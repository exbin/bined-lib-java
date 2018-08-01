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
package org.exbin.bined.javafx.basic;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.awt.BasicStroke;
import javafx.scene.paint.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.text.Font;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.javafx.CodeArea;
import org.exbin.bined.javafx.CodeAreaPainter;
import org.exbin.bined.javafx.CodeAreaJavaFxUtils;
import org.exbin.bined.javafx.CodeAreaWorker;
import org.exbin.bined.javafx.basic.DefaultCodeAreaCaret.CursorRenderingMode;
import org.exbin.bined.javafx.capability.BackgroundPaintCapable;
import org.exbin.bined.javafx.capability.FontCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2018/08/01
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    @Nonnull
    protected final CodeAreaWorker worker;
    private volatile boolean initialized = false;
    private volatile boolean adjusting = false;
    private volatile boolean fontChanged = false;
    private volatile boolean updateLayout = true;
    private volatile boolean resetColors = true;

    @Nonnull
    private final Canvas dataView;
    @Nonnull
    private final ScrollPane scrollPanel;

    @Nonnull
    private final BasicCodeAreaStructure structure = new BasicCodeAreaStructure();
    @Nonnull
    private final BasicCodeAreaScrolling scrolling = new BasicCodeAreaScrolling();
    @Nonnull
    private volatile ScrollingState scrollingState = ScrollingState.NO_SCROLLING;

    private final Colors colors = new Colors();

    private double componentWidth;
    private double componentHeight;
    private double dataViewX;
    private double dataViewY;
    private final double dataViewOffsetX = 0;
    private final double dataViewOffsetY = 0;
    private double scrollPanelWidth;
    private double scrollPanelHeight;
    private double dataViewWidth;
    private double dataViewHeight;

    private int rowPositionNumberLength;
    private int rowPositionLength;
    private int rowPositionAreaWidth;
    private int headerAreaHeight;
    private int rowHeight;
    private int rowsPerRect;
    private int charactersPerPage;
    private int charactersPerRect;
    private int charactersPerCodeArea;

    @Nullable
    private CodeCharactersCase hexCharactersCase;
    @Nullable
    private EditationMode editationMode;
    @Nullable
    private BasicBackgroundPaintMode backgroundPaintMode;
    private boolean showMirrorCursor;

    private double previewRelativeX;
    private int visibleCharStart;
    private int visibleCharEnd;
    private int visibleMatrixCharEnd;
    private int visiblePreviewStart;
    private int visiblePreviewEnd;
    private int visibleCodeStart;
    private int visibleCodeEnd;
    private int visibleMatrixCodeEnd;

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
    private int subFontSpace = 3;

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
        dataView = new Canvas();
//        dataView.setBorder(null);
        dataView.setVisible(false);
//        dataView.setLayout(null);
//        dataView.setOpaque(false);
        // Fill whole area, no more suitable method found so far
        codeArea.getChildren().add(dataView);
        scrollPanel = new ScrollPane();
        scrollPanel.setBorder(Border.EMPTY);
        scrollPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPanel.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
//        scrollPanel.setIgnoreRepaint(true);
//        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
//        verticalScrollBar.setIgnoreRepaint(true);
//        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
//        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
//        horizontalScrollBar.setIgnoreRepaint(true);
//        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
        codeArea.getChildren().add(scrollPanel);
//        scrollPanel.setOpaque(false);
//        scrollPanel.setViewportView(dataView);
        scrollPanel.setMinViewportWidth(200);
        scrollPanel.setMinViewportHeight(200);
//        scrollPanel.getViewport().setOpaque(false);
//
//        DefaultCodeAreaMouseListener codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
//        codeArea.addMouseListener(codeAreaMouseListener);
//        codeArea.addMouseMotionListener(codeAreaMouseListener);
//        codeArea.addMouseWheelListener(codeAreaMouseListener);
//        scrollPanel.addMouseListener(codeAreaMouseListener);
//        scrollPanel.addMouseMotionListener(codeAreaMouseListener);
//        scrollPanel.addMouseWheelListener(codeAreaMouseListener);
    }

    @Override
    public void reset() {
        resetColors();
        resetFont();
        resetScrollState();
        updateLayout();
    }

    @Override
    public void resetColors() {
        resetColors = true;
    }

    @Override
    public void resetFont() {
        fontChanged = true;
    }

    @Override
    public void updateLayout() {
        updateLayout = true;
    }

    private void resetCharPositions() {
        charactersPerRect = computeCharactersPerRectangle();

        int previewCharPos = structure.getPreviewCharPos();
        previewRelativeX = previewCharPos * characterWidth;

        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        charactersPerCodeArea = structure.computeFirstCodeCharacterPos(structure.getBytesPerRow());
        int bytesPerRow = structure.getBytesPerRow();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            visibleCharStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            visibleCharEnd = ((scrollPosition.getCharPosition() + charactersPerRect) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleCharEnd > structure.getCharactersPerRow()) {
                visibleCharEnd = structure.getCharactersPerRow();
            }
            visibleMatrixCharEnd = (int) ((dataViewWidth + (scrollPosition.getCharPosition() + charactersPerCodeArea) * characterWidth + scrollPosition.getCharOffset()) / characterWidth);
            if (visibleMatrixCharEnd > charactersPerCodeArea) {
                visibleMatrixCharEnd = charactersPerCodeArea;
            }
            visibleCodeStart = structure.computePositionByte((int) visibleCharStart);
            visibleCodeEnd = structure.computePositionByte((int) (visibleCharEnd - 1)) + 1;
            visibleMatrixCodeEnd = structure.computePositionByte((int) (visibleMatrixCharEnd - 1)) + 1;
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
                visibleCharStart = (int) (visiblePreviewStart + previewCharPos);
            }
            visiblePreviewEnd = (int) ((dataViewWidth + (scrollPosition.getCharPosition() + 1) * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos);
            if (visiblePreviewEnd > bytesPerRow) {
                visiblePreviewEnd = bytesPerRow;
            }
            if (visiblePreviewEnd >= 0) {
                visibleCharEnd = (int) (visiblePreviewEnd + previewCharPos);
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

        rowDataCache.rowData = new byte[structure.getBytesPerRow() + maxBytesPerChar - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[structure.getCharactersPerRow()];
    }

    public void resetFont(@Nonnull GraphicsContext gc) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) worker).getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        maxBytesPerChar = (int) encoder.maxBytesPerChar();

        font = ((FontCapable) worker).getFont();
        fontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        /**
         * Use small 'w' character to guess normal font width.
         */
        characterWidth = (int) Math.ceil(fontMetrics.computeStringWidth("w"));
        /**
         * Compare it to small 'i' to detect if font is monospaced.
         *
         * TODO: Is there better way?
         */
        monospaceFont = characterWidth == (int) Math.ceil(fontMetrics.computeStringWidth(" ")) && characterWidth == (int) Math.ceil(fontMetrics.computeStringWidth("i"));
        int fontSize = (int) font.getSize();
        rowHeight = fontSize + subFontSpace;

        resetSizes();
        resetCharPositions();
        initialized = true;
    }

    public void dataViewScrolled() {
        if (!isInitialized()) {
            return;
        }

        resetScrollState();
        if (characterWidth > 0) {
            resetCharPositions();
            paintComponent();
        }
    }

    private void resetScrollState() {
        scrolling.setScrollPosition(((ScrollingCapable) worker).getScrollPosition());

        if (characterWidth > 0) {
            resetCharPositions();
        }

        if (rowHeight > 0 && characterWidth > 0) {
            int documentDataWidth = structure.getCharactersPerRow() * characterWidth;
            long rowsPerData = (structure.getDataSize() / structure.getBytesPerRow()) + 1;
            scrolling.updateCache(worker);

            int documentDataHeight;
            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.SCALED);
                documentDataHeight = Integer.MAX_VALUE;
            } else {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.NORMAL);
                documentDataHeight = (int) (rowsPerData * rowHeight);
            }

//            dataView.setPreferredSize(new Dimension(documentDataWidth, documentDataHeight));
        }

        // TODO on resize only
//        scrollPanel.setBounds(getScrollPanelRectangle());
//        scrollPanel.revalidate();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void paintComponent() {
        if (!initialized) {
            reset();
        }
        GraphicsContext gc = dataView.getGraphicsContext2D();
        if (font == null) {
            resetFont(gc);
        }

        paintOutsiteArea(gc);
        paintHeader(gc);
        paintRowPosition(gc);
        paintMainArea(gc);
//        scrollPanel.paintComponents(g);
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;

            CodeArea codeArea = worker.getCodeArea();
//            colors.foreground = codeArea.getForeground();
            if (colors.foreground == null) {
                colors.foreground = Color.BLACK;
            }

//            colors.background = codeArea.getBackground();
            if (colors.background == null) {
                colors.background = Color.WHITE;
            }
//            colors.selectionForeground = UIManager.getColor("TextArea.selectionForeground");
            if (colors.selectionForeground == null) {
                colors.selectionForeground = Color.WHITE;
            }
//            colors.selectionBackground = UIManager.getColor("TextArea.selectionBackground");
            if (colors.selectionBackground == null) {
                colors.selectionBackground = new Color(96, 96, 255, 255);
            }
            colors.selectionMirrorForeground = colors.selectionForeground;
            colors.selectionMirrorBackground = CodeAreaJavaFxUtils.computeGrayColor(colors.selectionBackground);
//            colors.cursor = UIManager.getColor("TextArea.caretForeground");
            if (colors.cursor == null) {
                colors.cursor = Color.BLACK;
            }
            colors.negativeCursor = CodeAreaJavaFxUtils.createNegativeColor(colors.cursor);
            colors.decorationLine = Color.GRAY;

            colors.stripes = CodeAreaJavaFxUtils.createOddColor(colors.background);
        }

        if (updateLayout) {
            updateLayout = false;

            resetSizes();

            hexCharactersCase = ((CodeCharactersCaseCapable) worker).getCodeCharactersCase();
            editationMode = ((EditationModeCapable) worker).getEditationMode();
            backgroundPaintMode = ((BackgroundPaintCapable) worker).getBackgroundPaintMode();
            showMirrorCursor = ((CaretCapable) worker).isShowMirrorCursor();

            rowsPerRect = computeRowsPerRectangle();

            hexCharactersCase = CodeCharactersCase.UPPER;

            charactersPerPage = computeCharactersPerPage();

            resetScrollState();
        }

    }

    private void resetSizes() {
        if (fontMetrics == null) {
            headerAreaHeight = 0;
        } else {
            int fontHeight = (int) Math.ceil(fontMetrics.getXheight());
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
        dataView.setWidth(componentWidth);
        dataView.setHeight(componentHeight);
    }

    public void paintOutsiteArea(@Nonnull GraphicsContext gc) {
        gc.setFill(colors.background);
        gc.fillRect(0, 0, componentWidth, headerAreaHeight);

        // Decoration lines
        gc.setFill(colors.decorationLine);
        gc.moveTo(0, headerAreaHeight - 1);
        gc.lineTo(rowPositionAreaWidth, headerAreaHeight - 1);

        {
            int lineX = rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= 0) {
                gc.moveTo(lineX, 0);
                gc.lineTo(lineX, headerAreaHeight);
            }
        }
    }

    public void paintHeader(@Nonnull GraphicsContext g) {
//        Rectangle clipBounds = g.getClipBounds();
//        Rectangle headerArea = new Rectangle(rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
//        g.setClip(clipBounds != null ? headerArea.intersection(clipBounds) : headerArea);
//
//        g.setColor(colors.background);
//        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);
//
//        // Decoration lines
//        g.setColor(colors.decorationLine);
//        g.fillRect(0, headerAreaHeight - 1, componentWidth, 1);
//        int lineX = dataViewX + previewRelativeX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset() - characterWidth / 2;
//        if (lineX >= dataViewX) {
//            g.drawLine(lineX, 0, lineX, headerAreaHeight);
//        }
//
//        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
//            int charactersPerCodeArea = computeFirstCodeCharacterPos(bytesPerRow);
//            int headerX = dataViewX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
//            int headerY = rowHeight - subFontSpace;
//
//            int visibleHeaderCharStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
//            if (visibleHeaderCharStart < 0) {
//                visibleHeaderCharStart = 0;
//            }
//            int visibleHeaderCharEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + charactersPerCodeArea) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
//            if (visibleHeaderCharEnd > charactersPerCodeArea) {
//                visibleHeaderCharEnd = charactersPerCodeArea;
//            }
//            int visibleStart = computePositionByte(visibleHeaderCharStart);
//            int visibleEnd = computePositionByte(visibleHeaderCharEnd - 1) + 1;
//
//            g.setColor(colors.foreground);
//            char[] headerChars = new char[charactersPerCodeArea];
//            Arrays.fill(headerChars, ' ');
//
//            boolean interleaving = false;
//            int lastPos = 0;
//            for (int index = visibleStart; index < visibleEnd; index++) {
//                int codePos = computeFirstCodeCharacterPos(index);
//                if (codePos == lastPos + 2 && !interleaving) {
//                    interleaving = true;
//                } else {
//                    CodeAreaUtils.longToBaseCode(headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, hexCharactersCase);
//                    lastPos = codePos;
//                    interleaving = false;
//                }
//            }
//
//            int renderOffset = visibleHeaderCharStart;
////            ColorsGroup.ColorType renderColorType = null;
//            Color renderColor = null;
//            for (int characterOnRow = visibleHeaderCharStart; characterOnRow < visibleHeaderCharEnd; characterOnRow++) {
//                int byteOnRow;
//                byteOnRow = computePositionByte(characterOnRow);
//                boolean sequenceBreak = false;
//                boolean nativeWidth = true;
//
//                int currentCharWidth = 0;
////                ColorsGroup.ColorType colorType = ColorsGroup.ColorType.TEXT;
////                if (characterRenderingMode != CharacterRenderingMode.LINE_AT_ONCE) {
//                char currentChar = ' ';
////                    if (colorType == ColorsGroup.ColorType.TEXT) {
//                currentChar = headerChars[characterOnRow];
////                    }
//                if (currentChar == ' ' && renderOffset == characterOnRow) {
//                    renderOffset++;
//                    continue;
//                }
//                if (monospaceFont) { // characterRenderingMode == CharacterRenderingMode.AUTO && 
//                    // Detect if character is in unicode range covered by monospace fonts
//                    if (CodeAreaJavaFxUtils.isMonospaceFullWidthCharater(currentChar)) {
//                        currentCharWidth = characterWidth;
//                    }
//                }
//
//                if (currentCharWidth == 0) {
//                    currentCharWidth = fontMetrics.charWidth(currentChar);
//                    nativeWidth = currentCharWidth == characterWidth;
//                }
////                } else {
////                currentCharWidth = characterWidth;
////                }
//
//                Color color = colors.foreground;
////                getHeaderPositionColor(byteOnRow, charOnRow);
////                if (renderColorType == null) {
////                    renderColorType = colorType;
////                    renderColor = color;
////                    g.setColor(color);
////                }
//
//                if (!nativeWidth || !CodeAreaJavaFxUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
//                    sequenceBreak = true;
//                }
//                if (sequenceBreak) {
//                    if (renderOffset < characterOnRow) {
//                        g.drawChars(headerChars, renderOffset, characterOnRow - renderOffset, headerX + renderOffset * characterWidth, headerY);
//                    }
//
////                    if (!colorType.equals(renderColorType)) {
////                        renderColorType = colorType;
////                    }
//                    if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
//                        renderColor = color;
//                        g.setColor(color);
//                    }
//
//                    if (!nativeWidth) {
//                        renderOffset = characterOnRow + 1;
////                        if (characterRenderingMode == CharacterRenderingMode.TOP_LEFT) {
////                            g.drawChars(headerChars, characterOnRow, 1, headerX + characterOnRow * characterWidth, headerY);
////                        } else {
//                        int positionX = headerX + characterOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
//                        drawShiftedChar(g, headerChars, characterOnRow, characterWidth, positionX, headerY);
////                        }
//                    } else {
//                        renderOffset = characterOnRow;
//                    }
//                }
//            }
//
//            if (renderOffset < charactersPerCodeArea) {
//                g.drawChars(headerChars, renderOffset, charactersPerCodeArea - renderOffset, headerX + renderOffset * characterWidth, headerY);
//            }
//        }
//
//        g.setClip(clipBounds);
    }

    public void paintRowPosition(@Nonnull GraphicsContext g) {
//        Rectangle clipBounds = g.getClipBounds();
//        Rectangle rowPositionsArea = new Rectangle(0, headerAreaHeight, rowPositionAreaWidth, componentHeight - headerAreaHeight - getHorizontalScrollBarSize());
//        g.setClip(clipBounds != null ? rowPositionsArea.intersection(clipBounds) : rowPositionsArea);
//
//        g.setColor(colors.background);
//        g.fillRect(rowPositionsArea.x, rowPositionsArea.y, rowPositionsArea.width, rowPositionsArea.height);
//
//        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
//            long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
//            int stripePositionY = headerAreaHeight + ((scrollPosition.getScrollRowPosition() & 1) > 0 ? 0 : rowHeight);
//            g.setColor(colors.stripes);
//            for (int row = 0; row <= rowsPerRect / 2; row++) {
//                if (dataPosition >= dataSize) {
//                    break;
//                }
//
//                g.fillRect(0, stripePositionY, rowPositionAreaWidth, rowHeight);
//                stripePositionY += rowHeight * 2;
//                dataPosition += bytesPerRow * 2;
//            }
//        }
//
//        long dataPosition = bytesPerRow * scrollPosition.getScrollRowPosition();
//        int positionY = headerAreaHeight + rowHeight - subFontSpace - scrollPosition.getScrollRowOffset();
//        g.setColor(colors.foreground);
//        Rectangle compRect = new Rectangle();
//        for (int row = 0; row <= rowsPerRect; row++) {
//            if (dataPosition > dataSize) {
//                break;
//            }
//
//            CodeAreaUtils.longToBaseCode(rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, codeType.getBase(), rowPositionLength, true, CodeCharactersCase.UPPER);
////            if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
////                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
////            } else {
//            for (int digitIndex = 0; digitIndex < rowPositionLength; digitIndex++) {
//                drawCenteredChar(g, rowPositionCode, digitIndex, characterWidth, compRect.x + characterWidth * digitIndex, positionY);
//            }
////            }
//
//            positionY += rowHeight;
//            dataPosition += bytesPerRow;
//        }
//
//        g.setColor(colors.decorationLine);
//        int lineX = rowPositionAreaWidth - (characterWidth / 2);
//        if (lineX >= 0) {
//            g.drawLine(lineX, dataViewY, lineX, dataViewY + dataViewHeight);
//        }
//        g.drawLine(dataViewX, dataViewY - 1, dataViewX + dataViewWidth, dataViewY - 1);
//
//        g.setClip(clipBounds);
    }

    @Override
    public void paintMainArea(@Nonnull GraphicsContext g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            resetFont(g);
            fontChanged = false;
        }

//        Rectangle clipBounds = g.getClipBounds();
//        Rectangle mainArea = getMainAreaRect();
//        g.setClip(clipBounds != null ? mainArea.intersection(clipBounds) : mainArea);
//        paintBackground(g);
//
//        g.setColor(colors.decorationLine);
//        int lineX = dataViewX + previewRelativeX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset() - characterWidth / 2;
//        if (lineX >= dataViewX) {
//            g.drawLine(lineX, dataViewY, lineX, dataViewY + dataViewHeight);
//        }
//
//        paintRows(g);
//        g.setClip(clipBounds);
//        paintCursor(g);
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(@Nonnull GraphicsContext g) {
//        int rowPositionX = rowPositionAreaWidth;
//        g.setColor(colors.background);
//        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
//            g.fillRect(rowPositionX, headerAreaHeight, dataViewWidth, dataViewHeight);
//        }
//
//        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
//            long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
//            int stripePositionY = headerAreaHeight + (int) ((scrollPosition.getScrollRowPosition() & 1) > 0 ? 0 : rowHeight);
//            g.setColor(colors.stripes);
//            for (int row = 0; row <= rowsPerRect / 2; row++) {
//                if (dataPosition >= dataSize) {
//                    break;
//                }
//
//                g.fillRect(rowPositionX, stripePositionY, dataViewWidth, rowHeight);
//                stripePositionY += rowHeight * 2;
//                dataPosition += bytesPerRow * 2;
//            }
//        }
    }

    public void paintRows(@Nonnull GraphicsContext g) {
//        long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
//        int rowPositionX = rowPositionAreaWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
//        int rowPositionY = headerAreaHeight;
//        g.setColor(colors.foreground);
//        for (int row = 0; row <= rowsPerRect; row++) {
//            prepareRowData(dataPosition);
//            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
//            paintRowText(g, dataPosition, rowPositionX, rowPositionY);
//
//            rowPositionY += rowHeight;
//            dataPosition += bytesPerRow;
//        }
    }

    private void prepareRowData(long dataPosition) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int previewCharPos = structure.getPreviewCharPos();
        CodeType codeType = structure.getCodeType();
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
            BinaryData data = worker.getContentData();
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
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(byteOnRow), hexCharactersCase);
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
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
    public void paintRowBackground(@Nonnull GraphicsContext g, long rowDataPosition, int rowPositionX, int rowPositionY) {
//        int renderOffset = visibleCharStart;
//        Color renderColor = null;
//        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
//            int section;
//            int byteOnRow;
//            if (charOnRow >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
//                byteOnRow = charOnRow - previewCharPos;
//                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
//            } else {
//                byteOnRow = computePositionByte(charOnRow);
//                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
//            }
//            boolean sequenceBreak = false;
//
//            Color color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
//            if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
//                sequenceBreak = true;
//            }
//            if (sequenceBreak) {
//                if (renderOffset < charOnRow) {
//                    if (renderColor != null) {
//                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, rowPositionY);
//                    }
//                }
//
//                if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
//                    renderColor = color;
//                    if (color != null) {
//                        g.setColor(color);
//                    }
//                }
//
//                renderOffset = charOnRow;
//            }
//        }
//
//        if (renderOffset < charactersPerRow) {
//            if (renderColor != null) {
//                renderBackgroundSequence(g, renderOffset, charactersPerRow, rowPositionX, rowPositionY);
//            }
//        }
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
        SelectionRange selectionRange = structure.getSelectionRange();
        int codeLastCharPos = structure.getCodeLastCharPos();
        CodeAreaCaretPosition caretPosition = structure.getCaretPosition();
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
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = structure.getPreviewCharPos();
        int rowsPerPage = structure.getRowsPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, previewCharPos, rowsPerPage, charactersPerPage, (int) dataViewWidth, (int) dataViewHeight, characterWidth, rowHeight);
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(@Nonnull CaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = structure.getPreviewCharPos();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeCenterOnScrollPosition(rowPosition, charPosition, bytesPerRow, previewCharPos, rowsPerRect, charactersPerRect, (int) dataViewWidth, (int) dataViewHeight, characterWidth, rowHeight);
    }

    /**
     * Paints row text.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowText(@Nonnull GraphicsContext g, long rowDataPosition, int rowPositionX, int rowPositionY) {
//        int positionY = rowPositionY + rowHeight - subFontSpace;
//
//        Color lastColor = null;
//        Color renderColor = null;
//
//        int renderOffset = visibleCharStart;
//        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
//            int section;
//            int byteOnRow;
//            if (charOnRow >= previewCharPos) {
//                byteOnRow = charOnRow - previewCharPos;
//                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
//            } else {
//                byteOnRow = computePositionByte(charOnRow);
//                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
//            }
//
//            Color color = getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
//            if (color == null) {
//                color = colors.foreground;
//            }
//
//            boolean sequenceBreak = false;
//            if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
//                if (renderColor == null) {
//                    renderColor = color;
//                }
//
//                sequenceBreak = true;
//            }
//
//            int currentCharWidth = 0;
//            char currentChar = rowCharacters[charOnRow];
//            if (currentChar == ' ' && renderOffset == charOnRow) {
//                renderOffset++;
//                continue;
//            }
//
//            if (monospaceFont) {
//                // Detect if character is in unicode range covered by monospace fonts
//                if (CodeAreaJavaFxUtils.isMonospaceFullWidthCharater(currentChar)) {
//                    currentCharWidth = characterWidth;
//                }
//            }
//
//            boolean nativeWidth = true;
//            if (currentCharWidth == 0) {
//                currentCharWidth = fontMetrics.charWidth(currentChar);
//                nativeWidth = currentCharWidth == characterWidth;
//            }
//
//            if (!nativeWidth) {
//                sequenceBreak = true;
//            }
//
//            if (sequenceBreak) {
//                if (!CodeAreaJavaFxUtils.areSameColors(lastColor, renderColor)) {
//                    g.setColor(renderColor);
//                    lastColor = renderColor;
//                }
//
//                if (charOnRow > renderOffset) {
//                    renderCharSequence(g, renderOffset, charOnRow, rowPositionX, positionY);
//                }
//
//                renderColor = color;
//                if (!CodeAreaJavaFxUtils.areSameColors(lastColor, renderColor)) {
//                    g.setColor(renderColor);
//                    lastColor = renderColor;
//                }
//
//                if (!nativeWidth) {
//                    renderOffset = charOnRow + 1;
//                    int positionX = rowPositionX + charOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
//                    drawShiftedChar(g, rowCharacters, charOnRow, characterWidth, positionX, positionY);
//                } else {
//                    renderOffset = charOnRow;
//                }
//            }
//        }
//
//        if (renderOffset < charactersPerRow) {
//            if (!CodeAreaJavaFxUtils.areSameColors(lastColor, renderColor)) {
//                g.setColor(renderColor);
//            }
//
//            renderCharSequence(g, renderOffset, charactersPerRow, rowPositionX, positionY);
//        }
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
        SelectionRange selectionRange = structure.getSelectionRange();
        CodeAreaCaretPosition caretPosition = structure.getCaretPosition();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection) {
            return section == caretPosition.getSection() ? colors.selectionForeground : colors.selectionMirrorForeground;
        }

        return null;
    }

    @Override
    public void paintCursor(@Nonnull GraphicsContext g) {
//        if (!worker.getCodeArea().hasFocus()) {
//            return;
//        }

        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
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

//        g.restore();
//        Rectangle clipBounds = g.getClipBounds();
//        Rectangle mainAreaRect = getMainAreaRect();
//        Rectangle intersection = mainAreaRect.intersection(cursorRect);
//        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();
//
//        if (cursorVisible) {
//            g.setClip(intersection);
//            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
//            g.setColor(colors.cursor);
//
//            paintCursorRect(g, intersection.x, intersection.y, intersection.width, intersection.height, renderingMode, caret);
//        }
//
//        // Paint mirror cursor
//        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
//            Rectangle mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
//            if (mirrorCursorRect != null) {
//                intersection = mainAreaRect.intersection(mirrorCursorRect);
//                boolean mirrorCursorVisible = !intersection.isEmpty();
//                if (mirrorCursorVisible) {
//                    g.setClip(intersection);
//                    g.setColor(colors.cursor);
//                    GraphicsContext2D g2d = (GraphicsContext2D) g.create();
//                    Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
//                    g2d.setStroke(dashed);
//                    g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
//                }
//            }
//        }
//        g.setClip(clipBounds);
    }

    private void paintCursorRect(@Nonnull GraphicsContext g, int x, int y, int width, int height, @Nonnull CursorRenderingMode renderingMode, @Nonnull DefaultCodeAreaCaret caret) {
//        switch (renderingMode) {
//            case PAINT: {
//                g.fillRect(x, y, width, height);
//                break;
//            }
//            case XOR: {
//                g.setXORMode(colors.background);
//                g.fillRect(x, y, width, height);
//                g.setPaintMode();
//                break;
//            }
//            case NEGATIVE: {
//                g.fillRect(x, y, width, height);
//                g.setColor(colors.negativeCursor);
//                BinaryData contentData = worker.getCodeArea().getContentData();
//                int row = (y + scrollPosition.getScrollRowOffset() - dataViewY) / rowHeight;
//                int scrolledX = x + scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset();
//                int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getScrollRowOffset();
//                long dataPosition = caret.getDataPosition();
//                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
//                    int charPos = (scrolledX - previewRelativeX) / characterWidth;
//                    if (dataPosition >= dataSize) {
//                        break;
//                    }
//
//                    char[] previewChars = new char[1];
//                    byte[] data = new byte[maxCharLength];
//
//                    if (maxCharLength > 1) {
//                        int charDataLength = maxCharLength;
//                        if (dataPosition + maxCharLength > dataSize) {
//                            charDataLength = (int) (dataSize - dataPosition);
//                        }
//
//                        if (contentData == null) {
//                            previewChars[0] = ' ';
//                        } else {
//                            contentData.copyToArray(dataPosition, data, 0, charDataLength);
//                            String displayString = new String(data, 0, charDataLength, charset);
//                            if (!displayString.isEmpty()) {
//                                previewChars[0] = displayString.charAt(0);
//                            }
//                        }
//                    } else {
//                        if (charMappingCharset == null || charMappingCharset != charset) {
//                            buildCharMapping(charset);
//                        }
//
//                        if (contentData == null) {
//                            previewChars[0] = ' ';
//                        } else {
//                            previewChars[0] = charMapping[contentData.getByte(dataPosition) & 0xFF];
//                        }
//                    }
//                    int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
////                        if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
////                            g.drawChars(previewChars, 0, 1, posX, posY);
////                        } else {
//                    drawCenteredChar(g, previewChars, 0, characterWidth, posX, posY);
////                        }
//                } else {
//                    int charPos = (scrolledX - dataViewX) / characterWidth;
//                    int byteOffset = computePositionByte(charPos);
//                    int codeCharPos = computeFirstCodeCharacterPos(byteOffset);
//                    char[] rowChars = new char[codeType.getMaxDigitsForByte()];
//
//                    if (contentData != null && dataPosition < dataSize) {
//                        byte dataByte = contentData.getByte(dataPosition);
//                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowChars, 0, hexCharactersCase);
//                    } else {
//                        Arrays.fill(rowChars, ' ');
//                    }
//                    int posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
//                    int charsOffset = charPos - codeCharPos;
////                        if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
////                            g.drawChars(lineChars, charsOffset, 1, posX + (charsOffset * characterWidth), posY);
////                        } else {
//                    drawCenteredChar(g, rowChars, charsOffset, characterWidth, posX + (charsOffset * characterWidth), posY);
////                        }
//                }
//                break;
//            }
//            default:
//                throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
//        }
    }

    @Nonnull
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, @Nonnull PositionOverflowMode overflowMode) {
        CodeAreaCaretPosition caret = new CodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
                diffX = 1;
            }
            positionX = rowPositionAreaWidth;
        }
        int cursorCharX = (positionX - rowPositionAreaWidth + scrollPosition.getCharOffset()) / characterWidth + scrollPosition.getCharPosition() - diffX;
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        int diffY = 0;
        if (positionY < headerAreaHeight) {
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
                diffY = 1;
            }
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getRowOffset()) / rowHeight + scrollPosition.getRowPosition() - diffY;
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        CodeAreaViewMode viewMode = structure.getViewMode();
        int previewCharPos = structure.getPreviewCharPos();
        int bytesPerRow = structure.getBytesPerRow();
        CodeType codeType = structure.getCodeType();
        long dataSize = structure.getDataSize();
        long dataPosition;
        int codeOffset = 0;
        int byteOnRow;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(BasicCodeAreaSection.CODE_MATRIX.getSection());
            byteOnRow = structure.computePositionByte(cursorCharX);
            if (byteOnRow >= bytesPerRow) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - structure.computeFirstCodeCharacterPos(byteOnRow);
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
    public CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull org.exbin.bined.basic.MovementDirection direction) {
        return structure.computeMovePosition(position, direction);
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull org.exbin.bined.basic.ScrollingDirection direction) {
        int rowsPerPage = structure.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        return scrolling.computeScrolling(startPosition, direction, rowsPerPage, rowsPerDocument);
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
        int bytesPerRow = structure.getBytesPerRow();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long row = dataPosition / bytesPerRow - scrollPosition.getRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + row * rowHeight) - scrollPosition.getRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            caretX = (int) (dataViewRect.x + previewRelativeX + characterWidth * byteOffset);
        } else {
            caretX = dataViewRect.x + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new Point(caretX, caretY);
    }

    @Nullable
    private Rectangle getMirrorCursorRect(long dataPosition, int section) {
        CodeType codeType = structure.getCodeType();
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
        return new Rectangle((int) dataViewX, (int) dataViewY, (int) scrollPanelWidth, (int) scrollPanelHeight);
    }

    @Nonnull
    public Rectangle getDataViewRectangle() {
        return new Rectangle((int) dataViewX, (int) dataViewY, (int) dataViewWidth, (int) dataViewHeight);
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
    protected void drawCenteredChar(@Nonnull GraphicsContext g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        int charWidth = (int) Math.ceil(fontMetrics.computeStringWidth(String.valueOf(drawnChars[charOffset])));
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, positionX + ((charWidthSpace + 1 - charWidth) >> 1), positionY);
    }

    protected void drawShiftedChar(@Nonnull GraphicsContext g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        g.fillText(String.copyValueOf(drawnChars, charOffset, 1), positionX, positionY);
    }

    private void buildCharMapping(@Nonnull Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private int getRowPositionLength() {
        if (rowPositionNumberLength <= 0) {
            long dataSize = structure.getDataSize();
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
    private void renderCharSequence(@Nonnull GraphicsContext g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        g.fillText(String.copyValueOf(rowDataCache.rowCharacters, startOffset, endOffset - startOffset), rowPositionX + startOffset * characterWidth, positionY);
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(@Nonnull GraphicsContext g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        g.fillRect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    private int computeRowsPerRectangle() {
        return rowHeight == 0 ? 0 : ((int) dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage() {
        return rowHeight == 0 ? 0 : (int) dataViewHeight / rowHeight;
    }

    private int computeCharactersPerRectangle() {
        return characterWidth == 0 ? 0 : ((int) dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage() {
        return characterWidth == 0 ? 0 : (int) dataViewWidth / characterWidth;
    }

    @Override
    public void updateScrollBars() {
        long rowsPerDocument = structure.getRowsPerDocument();
        adjusting = true;
        scrollPanel.setVbarPolicy(CodeAreaJavaFxUtils.getVerticalScrollBarPolicy(scrolling.getVerticalScrollBarVisibility()));
        scrollPanel.setHbarPolicy(CodeAreaJavaFxUtils.getHorizontalScrollBarPolicy(scrolling.getHorizontalScrollBarVisibility()));

        int verticalScrollValue = scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument);
        scrollPanel.setHvalue(verticalScrollValue);

        int horizontalScrollValue = scrolling.getHorizontalScrollValue(characterWidth);
        scrollPanel.setVvalue(horizontalScrollValue);

        adjusting = false;
    }

    @Nonnull
    private Rectangle getMainAreaRect() {
        return new Rectangle((int) rowPositionAreaWidth, (int) headerAreaHeight, (int) componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), (int) componentHeight - headerAreaHeight - getHorizontalScrollBarSize());
    }

    private int getHorizontalScrollBarSize() {
        return 10;
//        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
//        int size;
//        if (horizontalScrollBar.isVisible()) {
//            size = horizontalScrollBar.getHeight();
//        } else {
//            size = 0;
//        }
//
//        return size;
    }

    private int getVerticalScrollBarSize() {
        return 10;
//        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
//        int size;
//        if (verticalScrollBar.isVisible()) {
//            size = verticalScrollBar.getWidth();
//        } else {
//            size = 0;
//        }
//
//        return size;
    }

//    private class VerticalAdjustmentListener implements AdjustmentListener {
//
//        public VerticalAdjustmentListener() {
//        }
//
//        @Override
//        public void adjustmentValueChanged(AdjustmentEvent e) {
//            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
//            if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
//                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
//                long rowsPerDocumentToLastPage = rowsPerDocument - computeRowsPerRectangle();
//                long targetRow;
//                if (scrollBarValue > 0 && rowsPerDocumentToLastPage > maxValue / scrollBarValue) {
//                    targetRow = scrollBarValue * (rowsPerDocumentToLastPage / maxValue);
//                    long rest = rowsPerDocumentToLastPage % maxValue;
//                    targetRow += (rest * scrollBarValue) / maxValue;
//                } else {
//                    targetRow = (scrollBarValue * rowsPerDocumentToLastPage) / Integer.MAX_VALUE;
//                }
//                scrollPosition.setScrollRowPosition(targetRow);
//                if (verticalScrollUnit != VerticalScrollUnit.ROW) {
//                    scrollPosition.setScrollRowOffset(0);
//                }
//            } else {
//                if (rowHeight == 0) {
//                    scrollPosition.setScrollRowPosition(0);
//                    scrollPosition.setScrollRowOffset(0);
//                } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
//                    scrollPosition.setScrollRowPosition(scrollBarValue / rowHeight);
//                    scrollPosition.setScrollRowOffset(0);
//                } else {
//                    scrollPosition.setScrollRowPosition(scrollBarValue / rowHeight);
//                    scrollPosition.setScrollRowOffset(scrollBarValue % rowHeight);
//                }
//            }
//
//            // TODO
//            ((ScrollingCapable) worker).setScrollPosition(scrollPosition);
//            worker.getCodeArea().repaint();
////            dataViewScrolled(codeArea.getGraphicsContext());
//            notifyScrolled();
//        }
//    }
//
//    private class HorizontalAdjustmentListener implements AdjustmentListener {
//
//        public HorizontalAdjustmentListener() {
//        }
//
//        @Override
//        public void adjustmentValueChanged(AdjustmentEvent e) {
//            int scrollBarValue = scrollPanel.getHorizontalScrollBar().getValue();
//            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
//                scrollPosition.setScrollCharPosition(scrollBarValue);
//            } else {
//                if (characterWidth == 0) {
//                    scrollPosition.setScrollCharPosition(0);
//                    scrollPosition.setScrollCharOffset(0);
//                } else if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
//                    scrollPosition.setScrollCharPosition(scrollBarValue / characterWidth);
//                    scrollPosition.setScrollCharOffset(0);
//                } else {
//                    scrollPosition.setScrollCharPosition(scrollBarValue / characterWidth);
//                    scrollPosition.setScrollCharOffset(scrollBarValue % characterWidth);
//                }
//            }
//
//            ((ScrollingCapable) worker).setScrollPosition(scrollPosition);
//            notifyScrolled();
//            worker.getCodeArea().repaint();
////            dataViewScrolled(codeArea.getGraphicsContext());
//        }
//    }
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

    protected enum ScrollingState {
        NO_SCROLLING,
        SCROLLING_BY_SCROLLBAR,
        SCROLLING_BY_MOVEMENT
    }
}
