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

import com.sun.javafx.tk.Toolkit;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Stroke;
import java.nio.charset.Charset;
import java.util.Arrays;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.javafx.CodeAreaCore;
import org.exbin.bined.javafx.CodeAreaJavaFxUtils;
import org.exbin.bined.javafx.CodeAreaPainter;
import org.exbin.bined.javafx.basic.DefaultCodeAreaCaret.CursorRenderingMode;
import org.exbin.bined.javafx.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.javafx.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.javafx.capability.BackgroundPaintCapable;
import org.exbin.bined.javafx.capability.FontCapable;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.basic.BasicCodeAreaLayout;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2019/08/02
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaPainter implements CodeAreaPainter, BasicColorsCapableCodeAreaPainter {

    @Nonnull
    protected final CodeAreaCore codeArea;
    private volatile boolean initialized = false;
    private volatile boolean adjusting = false;

    private volatile boolean fontChanged = false;
    private volatile boolean layoutChanged = true;
    private volatile boolean resetColors = true;
    private volatile boolean caretChanged = true;

    @Nonnull
    private final Canvas topCanvas;
    @Nonnull
    private final Canvas headerCanvas;
    @Nonnull
    private final Canvas rowPositionCanvas;
    @Nonnull
    private final Canvas dataView;
    @Nonnull
    private final ScrollPane scrollPanel;

    @Nonnull
    private final BasicCodeAreaMetrics metrics = new BasicCodeAreaMetrics();
    @Nonnull
    private final BasicCodeAreaStructure structure = new BasicCodeAreaStructure();
    @Nonnull
    private final BasicCodeAreaScrolling scrolling = new BasicCodeAreaScrolling();
    @Nonnull
    private final BasicCodeAreaDimensions dimensions = new BasicCodeAreaDimensions();
    @Nonnull
    private final BasicCodeAreaVisibility visibility = new BasicCodeAreaVisibility();
    @Nonnull
    private volatile ScrollingState scrollingState = ScrollingState.NO_SCROLLING;

    @Nonnull
    private final BasicCodeAreaLayout layout = new BasicCodeAreaLayout();
    private BasicCodeAreaColorsProfile colorsProfile = new BasicCodeAreaColorsProfile();

    @Nullable
    private CodeCharactersCase codeCharactersCase;
    @Nullable
    private EditationOperation editationOperation;
    @Nullable
    private BasicBackgroundPaintMode backgroundPaintMode;
    private boolean showMirrorCursor;

    private int minRowPositionLength;
    private int maxRowPositionLength;
    private int rowPositionLength;

    @Nullable
    private Font font;
    @Nonnull
    private Charset charset;

    @Nullable
    private RowDataCache rowDataCache = null;
    @Nullable
    private CursorDataCache cursorDataCache = null;

    @Nullable
    private Charset charMappingCharset = null;
    private final char[] charMapping = new char[256];

    public DefaultCodeAreaPainter(CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        codeArea.addDataChangedListener(() -> {
            validateCaret();
            recomputeLayout();
        });

        topCanvas = new Canvas();
        headerCanvas = new Canvas();
        rowPositionCanvas = new Canvas();
        dataView = new Canvas();

//        dataView.setBorder(null);
//        dataView.setVisible(false);
//        dataView.setLayout(null);
//        dataView.setOpaque(false);
        // Fill whole area, no more suitable method found so far
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
//        codeArea.getChildren().add(scrollPanel);
//        scrollPanel.setOpaque(false);
        scrollPanel.setContent(dataView);
//        scrollPanel.getViewport().setOpaque(false);

//        codeArea.getChildren().add(dataView);
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
    public void attach() {
        ObservableList<Node> children = codeArea.getChildren();
        children.add(topCanvas);
        children.add(headerCanvas);
        children.add(rowPositionCanvas);
        children.add(scrollPanel);
        onResize();
    }

    @Override
    public void detach() {
        ObservableList<Node> children = codeArea.getChildren();
        children.remove(topCanvas);
        children.remove(headerCanvas);
        children.remove(rowPositionCanvas);
        children.remove(scrollPanel);
    }

    @Override
    public void reset() {
        resetColors();
        resetFont();
        resetLayout();
        resetCaret();
    }

    @Override
    public void resetColors() {
        resetColors = true;
    }

    @Override
    public void resetFont() {
        fontChanged = true;
        resetLayout();
    }

    @Override
    public void resetLayout() {
        layoutChanged = true;
    }

    @Override
    public void resetCaret() {
        caretChanged = true;
    }

    public void recomputeLayout() {
        rowPositionLength = getRowPositionLength();
        recomputeDimensions();

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(codeArea, charactersPerPage);
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        backgroundPaintMode = ((BackgroundPaintCapable) codeArea).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();
        minRowPositionLength = ((RowWrappingCapable) codeArea).getMinRowPositionLength();
        maxRowPositionLength = ((RowWrappingCapable) codeArea).getMaxRowPositionLength();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int charactersPerRow = structure.getCharactersPerRow();

        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        recomputeScrollState();
        layoutChanged = false;
    }

    private void updateCaret() {
        editationOperation = ((EditationModeCapable) codeArea).getActiveOperation();

        caretChanged = false;
    }

    private void validateCaret() {
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() > codeArea.getDataSize()) {
            caret.setCaretPosition(null);
        }
    }

    private void recomputeDimensions() {
        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        Insets insets = codeArea.getInsets();
        double componentWidth = codeArea.getWidth() - insets.getLeft() - insets.getRight();
        double componentHeight = codeArea.getHeight() - insets.getTop() - insets.getBottom();
        dimensions.recomputeSizes(metrics, insets.getRight(), insets.getTop(), componentWidth, componentHeight, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
    }

    public void recomputeCharPositions() {
        visibility.recomputeCharPositions(metrics, structure, dimensions, layout, scrolling);
        updateRowDataCache();
    }

    private void updateRowDataCache() {
        if (rowDataCache == null) {
            rowDataCache = new RowDataCache();
        }

        rowDataCache.headerChars = new char[visibility.getCharactersPerCodeSection()];
        rowDataCache.rowData = new byte[structure.getBytesPerRow() + metrics.getMaxBytesPerChar() - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[structure.getCharactersPerRow()];
    }

    public void fontChanged(GraphicsContext gc) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        font = ((FontCapable) codeArea).getCodeFont();
        metrics.recomputeMetrics(Toolkit.getToolkit().getFontLoader().getFontMetrics(font), charset);

        recomputeDimensions();
        recomputeCharPositions();
        initialized = true;
    }

    public void dataViewScrolled() {
        if (!isInitialized()) {
            return;
        }

        recomputeScrollState();
        if (metrics.getCharacterWidth() > 0) {
            recomputeCharPositions();
            paintComponent();
        }
    }

    private void recomputeScrollState() {
        scrolling.setScrollPosition(((ScrollingCapable) codeArea).getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        if (characterWidth > 0) {
            recomputeCharPositions();
        }

        if (rowHeight > 0 && characterWidth > 0) {
            int documentDataWidth = structure.getCharactersPerRow() * characterWidth;
            long rowsPerData = (structure.getDataSize() / structure.getBytesPerRow()) + 1;
            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

            int documentDataHeight;
            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.SCALED);
                documentDataHeight = Integer.MAX_VALUE;
            } else {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.NORMAL);
                documentDataHeight = (int) (rowsPerData * rowHeight);
            }

            dataView.setWidth(documentDataWidth);
            dataView.setHeight(documentDataHeight);
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
        headerCanvas.getGraphicsContext2D();

        if (!initialized) {
            reset();
        }
        updateCache();
        GraphicsContext gc = dataView.getGraphicsContext2D();
        if (font == null) {
            fontChanged(gc);
        }
        if (layoutChanged) {
            recomputeLayout();
        }

//        gc.setFill(Color.GREEN);
//        gc.setStroke(Color.BLUE);
//        gc.fillRect(0, 0, 50, 50);
        paintOutsiteArea();
        paintHeader();
        paintRowPosition();
        paintMainArea();
//        scrollPanel.paintComponents(g);
    }

    @Override
    public void onResize() {
        double width = codeArea.getWidth();
        double height = codeArea.getWidth();

        double headerAreaHeight = dimensions.getHeaderAreaHeight();
        double rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();

        topCanvas.relocate(0, 0);
        topCanvas.setWidth(rowPositionAreaWidth);
        topCanvas.setHeight(headerAreaHeight);

        headerCanvas.relocate(rowPositionAreaWidth, 0);
        headerCanvas.setWidth(width - rowPositionAreaWidth);
        headerCanvas.setHeight(headerAreaHeight);

        rowPositionCanvas.relocate(0, headerAreaHeight);
        rowPositionCanvas.setWidth(rowPositionAreaWidth);
        rowPositionCanvas.setHeight(height - headerAreaHeight);

        scrollPanel.relocate(rowPositionAreaWidth, headerAreaHeight);
        scrollPanel.setMinWidth(width - rowPositionAreaWidth);
        scrollPanel.setMinHeight(height - headerAreaHeight);
        scrollPanel.setMaxWidth(width - rowPositionAreaWidth);
        scrollPanel.setMaxHeight(height - headerAreaHeight);
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            colorsProfile.reinitialize();
        }
    }

    public void paintOutsiteArea() {
        GraphicsContext gc = topCanvas.getGraphicsContext2D();
        double headerAreaHeight = dimensions.getHeaderAreaHeight();
        double rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rectangle2D componentRect = dimensions.getComponentRectangle();
        int characterWidth = metrics.getCharacterWidth();
        gc.setFill(colorsProfile.getTextBackground());
        gc.fillRect(componentRect.getMinX(), componentRect.getMinY(), componentRect.getWidth(), headerAreaHeight);

        // Decoration lines
        gc.setStroke(colorsProfile.getDecorationLine());
        gc.moveTo(0, headerAreaHeight - 1);
        gc.lineTo(rowPositionAreaWidth, headerAreaHeight - 1);

        {
            double lineX = rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= 0) {
                gc.moveTo(lineX, 0);
                gc.lineTo(lineX, headerAreaHeight);
            }
        }
    }

    public void paintHeader() {
        GraphicsContext g = headerCanvas.getGraphicsContext2D();
//        gc.setStroke(Color.YELLOW);
//        gc.setFill(Color.YELLOW);
//        gc.fillRect(0, 0, 500, 50);
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        Rectangle2D headerArea = dimensions.getHeaderAreaRectangle();
        double headerAreaX = 0;
        double headerAreaY = 0;
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        double dataViewX = dimensions.getScrollPanelX();

        g.setFill(colorsProfile.getTextBackground());
        g.fillRect(headerAreaX, headerAreaY, headerArea.getWidth(), headerArea.getHeight());
        g.setFont(font);

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            double headerX = -scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            double headerY = headerAreaY + rowHeight - metrics.getSubFontSpace();

            g.setFill(colorsProfile.getTextColor());
            Arrays.fill(rowDataCache.headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            int skipToCode = visibility.getSkipToCode();
            int skipRestFromCode = visibility.getSkipRestFromCode();
            for (int index = skipToCode; index < skipRestFromCode; index++) {
                int codePos = structure.computeFirstCodeCharacterPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(rowDataCache.headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int skipToChar = visibility.getSkipToChar();
            int skipRestFromChar = visibility.getSkipRestFromChar();
            int codeCharEnd = Math.min(skipRestFromChar, visibility.getCharactersPerCodeSection());
            int renderOffset = skipToChar;
            Color renderColor = null;
            for (int characterOnRow = skipToChar; characterOnRow < codeCharEnd; characterOnRow++) {
                boolean sequenceBreak = false;

                char currentChar = rowDataCache.headerChars[characterOnRow];
                if (currentChar == ' ' && renderOffset == characterOnRow) {
                    renderOffset++;
                    continue;
                }

                Color color = colorsProfile.getTextColor();

                if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        drawCenteredChars(g, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setFill(color);
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }

        // Decoration lines
        g.setStroke(colorsProfile.getDecorationLine());
        g.moveTo(headerAreaX, headerAreaY + headerArea.getHeight() - 1);
        g.lineTo(headerAreaX + headerArea.getWidth(), headerAreaY + headerArea.getHeight() - 1);
        double lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.moveTo(lineX, headerAreaY);
            g.lineTo(lineX, headerAreaY + headerArea.getHeight());
        }
    }

    public void paintRowPosition() {
        GraphicsContext g = rowPositionCanvas.getGraphicsContext2D();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle2D rowPosRectangle = dimensions.getRowPositionAreaRectangle();
        double rowPosRectangleX = 0;
        double rowPosRectangleY = 0;
        Rectangle2D dataViewRectangle = dimensions.getDataViewRectangle();
        double dataViewRectangleX = 0;
        double dataViewRectangleY = 0;

        g.setFill(colorsProfile.getTextBackground());
        g.fillRect(rowPosRectangleX, rowPosRectangleY, rowPosRectangle.getWidth(), rowPosRectangle.getHeight());
        g.setFont(font);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            double stripePositionY = rowPosRectangleY - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setFill(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.fillRect(rowPosRectangleX, stripePositionY, rowPosRectangle.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        double positionY = rowPosRectangleY + rowHeight - subFontSpace - scrollPosition.getRowOffset();
        g.setFill(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, codeCharactersCase);
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangleX, positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
        }

        // Decoration lines
        g.setStroke(colorsProfile.getDecorationLine());
        double lineX = rowPosRectangleX + rowPosRectangle.getWidth() - (characterWidth / 2);
        if (lineX >= rowPosRectangleX) {
            g.moveTo(lineX, rowPosRectangleY);
            g.lineTo(lineX, dataViewRectangleY + dataViewRectangle.getHeight());
        }
        g.moveTo(dataViewRectangleX, dataViewRectangleY - 1);
        g.lineTo(dataViewRectangleX + dataViewRectangle.getWidth(), dataViewRectangleY - 1);
    }

    @Override
    public void paintMainArea() {
        if (!initialized) {
            reset();
        }

        GraphicsContext g = dataView.getGraphicsContext2D();
        if (fontChanged) {
            fontChanged(g);
            fontChanged = false;
        }

//        Rectangle2D mainAreaRect = dimensions.getMainAreaRect();
        Rectangle2D dataViewRectangle = dimensions.getDataViewInnerRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        double previewRelativeX = visibility.getPreviewRelativeX();

        paintBackground(g);

        // Decoration lines
        g.setStroke(colorsProfile.getDecorationLine());
        double lineX = dataViewRectangle.getMinX() + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewRectangle.getMinX()) {
            g.moveTo(lineX, dataViewRectangle.getMinY());
            g.lineTo(lineX, dataViewRectangle.getMinY() + dataViewRectangle.getHeight());
        }

        paintRows(g);
//        g.setClip(clipBounds);
        paintCursor();

//        paintDebugInfo(g);
    }

//    private long paintDebugCounter = 0;
//    
//    private void paintDebugInfo(GraphicsContext g, Rectangle2D dataViewRectangle) {
//        int x = componentWidth - rowPositionAreaWidth - 220;
//        int y = componentHeight - headerAreaHeight - 20;
//        g.setColor(Color.YELLOW);
//        g.fillRect(x, y, 200, 16);
//        g.setColor(Color.BLACK);
//        char[] headerCode = (String.valueOf(scrollPosition.getScrollCharPosition()) + "+" + String.valueOf(scrollPosition.getScrollCharOffset()) + " : " + String.valueOf(scrollPosition.getScrollRowPosition()) + "+" + String.valueOf(scrollPosition.getScrollRowOffset()) + " P: " + String.valueOf(rowsPerRect)).toCharArray();
//        g.drawChars(headerCode, 0, headerCode.length, x, y + rowHeight);
//    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(GraphicsContext g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle2D dataViewRect = dimensions.getDataViewInnerRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        g.setFill(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.fillRect(dataViewRect.getMinX(), dataViewRect.getMinY(), dataViewRect.getWidth(), dataViewRect.getHeight());
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            double stripePositionY = dataViewRect.getMinY() - scrollPosition.getRowOffset() + (int) ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setFill(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.fillRect(dataViewRect.getMinX(), stripePositionY, dataViewRect.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(GraphicsContext g) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        double dataViewX = dimensions.getScrollPanelX();
        double dataViewY = dimensions.getScrollPanelY();
        int rowsPerRect = dimensions.getRowsPerRect();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        double rowPositionX = -scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        double rowPositionY = -scrollPosition.getRowOffset();

        g.setFill(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
            g.setFill(colorsProfile.getTextColor());
            paintRowText(g, dataPosition, rowPositionX, rowPositionY);

            rowPositionY += rowHeight;
            dataPosition += bytesPerRow;
        }
    }

    private void prepareRowData(long dataPosition) {
        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int previewCharPos = visibility.getPreviewCharPos();
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
            BinaryData data = codeArea.getContentData();
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
            int skipToCode = visibility.getSkipToCode();
            int skipRestFromCode = visibility.getSkipRestFromCode();
            int endCode = Math.min(skipRestFromCode, rowBytesLimit);
            for (int byteOnRow = Math.max(skipToCode, rowStart); byteOnRow < endCode; byteOnRow++) {
                byte dataByte = rowDataCache.rowData[byteOnRow];
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(byteOnRow), codeCharactersCase);
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
            }
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            int skipToPreview = visibility.getSkipToPreview();
            int skipRestFromPreview = visibility.getSkipRestFromPreview();
            int endPreview = Math.min(skipRestFromPreview, rowBytesLimit);
            for (int byteOnRow = skipToPreview; byteOnRow < endPreview; byteOnRow++) {
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
    public void paintRowBackground(GraphicsContext g, long rowDataPosition, double rowPositionX, double rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getCharactersPerRow();
        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();

        int renderOffset = skipToChar;
        Color renderColor = null;
        for (int charOnRow = skipToChar; charOnRow < skipRestFromChar; charOnRow++) {
            CodeAreaSection section;
            int byteOnRow;
            if (charOnRow >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX;
            }
            boolean sequenceBreak = false;

            Color color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, rowPositionY);
                    }
                }

                if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.setFill(color);
                    }
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, charactersPerRow, rowPositionX, rowPositionY);
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
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        SelectionRange selectionRange = structure.getSelectionRange();
        int codeLastCharPos = visibility.getCodeLastCharPos();
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX)) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == caret.getSection() ? colorsProfile.getSelectionBackground() : colorsProfile.getSelectionMirrorBackground();
        }

        return null;
    }

    @Nullable
    @Override
    public CodeAreaScrollPosition computeRevealScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        double dataViewWidth = dimensions.getDataViewWidth();
        double dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getCharactersPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, (int) dataViewWidth % characterWidth, (int) dataViewHeight % rowHeight, characterWidth, rowHeight);
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        double dataViewWidth = dimensions.getDataViewWidth();
        double dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        int charactersPerRect = dimensions.getCharactersPerRect();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeCenterOnScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerRect, charactersPerRect, (int) dataViewWidth, (int) dataViewHeight, characterWidth, rowHeight);
    }

    /**
     * Paints row text.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowText(GraphicsContext g, long rowDataPosition, double rowPositionX, double rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();

        g.setFont(font);
        double positionY = rowPositionY + rowHeight - subFontSpace;

        Color lastColor = null;
        Color renderColor = null;

        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();
        int renderOffset = skipToChar;
        for (int charOnRow = skipToChar; charOnRow < skipRestFromChar; charOnRow++) {
            CodeAreaSection section;
            int byteOnRow;
            if (charOnRow >= previewCharPos) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX;
            }

            char currentChar = rowDataCache.rowCharacters[charOnRow];
            if (currentChar == ' ' && renderOffset == charOnRow) {
                renderOffset++;
                continue;
            }

            Color color = getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (color == null) {
                color = colorsProfile.getTextColor();
            }

            boolean sequenceBreak = false;
            if (!CodeAreaJavaFxUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaJavaFxUtils.areSameColors(lastColor, renderColor)) {
                    g.setFill(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charOnRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
                }

                renderColor = color;
                if (!CodeAreaJavaFxUtils.areSameColors(lastColor, renderColor)) {
                    g.setFill(renderColor);
                    lastColor = renderColor;
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaJavaFxUtils.areSameColors(lastColor, renderColor)) {
                g.setFill(renderColor);
            }

            drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charactersPerRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
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
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        SelectionRange selectionRange = structure.getSelectionRange();
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection) {
            return section == caret.getSection() ? colorsProfile.getSelectionColor() : colorsProfile.getSelectionMirrorColor();
        }

        return null;
    }

    @Override
    public void paintCursor() {
//        if (!codeArea.hasFocus()) {
//            return;
//        }

        if (caretChanged) {
            updateCaret();
        }

        GraphicsContext g = dataView.getGraphicsContext2D();

        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        Rectangle2D mainAreaRect = dimensions.getMainAreaRectangle();
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

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        Rectangle2D cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        boolean cursorVisible = caret.isCursorVisible(); // && !intersection.isEmpty();

        if (cursorVisible) {
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setFill(colorsProfile.getCursorColor());

            paintCursorRect(g, cursorRect.getMinX(), cursorRect.getMinY(), cursorRect.getWidth(), cursorRect.getHeight(), renderingMode, caret);
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            Rectangle2D mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            if (mirrorCursorRect != null) {
                boolean mirrorCursorVisible = true; // !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.setFill(colorsProfile.getCursorColor());
                    // TODO Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
                    // TODO g2d.setStroke(dashed);
                    // TODO g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
                }
            }
        }
    }

    private void paintCursorRect(GraphicsContext g, double x, double y, double width, double height, CursorRenderingMode renderingMode, DefaultCodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRect(x, y, width, height);
                break;
            }
//            case XOR: {
//                g.setXORMode(colorsProfile.getTextBackground());
//                g.fillRect(x, y, width, height);
//                break;
//            }
//            case NEGATIVE: {
//                g.fillRect(x, y, width, height);
//                g.setColor(colors.negativeCursor);
//                BinaryData contentData = codeArea.getContentData();
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
//                    drawCenteredChar(g, previewChars, 0, characterWidth, posX, posY);
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
//                    drawCenteredChar(g, rowChars, charsOffset, characterWidth, posX + (charsOffset * characterWidth), posY);
//                }
//                break;
//            }
            default:
                throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
        }
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, PositionOverflowMode overflowMode) {
        DefaultCodeAreaCaretPosition caret = new DefaultCodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        double rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        double headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
                diffX = 1;
            }
            positionX = (int) rowPositionAreaWidth;
        }
        int cursorCharX = (int) ((positionX - rowPositionAreaWidth + scrollPosition.getCharOffset()) / characterWidth + scrollPosition.getCharPosition() - diffX);
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        int diffY = 0;
        if (positionY < headerAreaHeight) {
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
                diffY = 1;
            }
            positionY = (int) headerAreaHeight;
        }
        long cursorRowY = (long) ((positionY - headerAreaHeight + scrollPosition.getRowOffset()) / rowHeight + scrollPosition.getRowPosition() - diffY);
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        CodeAreaViewMode viewMode = structure.getViewMode();
        int previewCharPos = visibility.getPreviewCharPos();
        int bytesPerRow = structure.getBytesPerRow();
        CodeType codeType = structure.getCodeType();
        long dataSize = structure.getDataSize();
        long dataPosition;
        int codeOffset = 0;
        int byteOnRow;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(BasicCodeAreaSection.CODE_MATRIX);
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
            caret.setSection(BasicCodeAreaSection.TEXT_PREVIEW);
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
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return structure.computeMovePosition(position, direction, dimensions.getRowsPerPage());
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction) {
        int rowsPerPage = dimensions.getRowsPerPage();
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
    public Point2D getPositionPoint(long dataPosition, int codeOffset, CodeAreaSection section) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowsPerRect = dimensions.getRowsPerRect();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long row = dataPosition / bytesPerRow - scrollPosition.getRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        Rectangle2D dataViewRect = dimensions.getDataViewRectangle();
        double caretY = (int) (dataViewRect.getMinY() + row * rowHeight) - scrollPosition.getRowOffset();
        double caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            caretX = (int) (dataViewRect.getMinX() + visibility.getPreviewRelativeX() + characterWidth * byteOffset);
        } else {
            caretX = dataViewRect.getMinX() + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new Point2D(caretX, caretY);
    }

    @Nullable
    private Rectangle2D getMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        Point2D mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == BasicCodeAreaSection.CODE_MATRIX ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            return null;
        }

        Rectangle2D mirrorCursorRect = new Rectangle2D(mirrorCursorPoint.getX(), mirrorCursorPoint.getY(), metrics.getCharacterWidth() * (section == BasicCodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        return mirrorCursorRect;
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        double dataViewX = dimensions.getScrollPanelX();
        double dataViewY = dimensions.getScrollPanelY();
        double scrollPanelWidth = dimensions.getScrollPanelWidth();
        double scrollPanelHeight = dimensions.getScrollPanelHeight();
        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
            return Cursor.TEXT_CURSOR;
        }

        return Cursor.DEFAULT_CURSOR;
    }

    @Override
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        return dimensions.getPositionZone(positionX, positionY);
    }

    @Nonnull
    @Override
    public BasicCodeAreaColorsProfile getBasicColors() {
        return colorsProfile;
    }

    @Override
    public void setBasicColors(BasicCodeAreaColorsProfile colors) {
        this.colorsProfile = colors;
    }

    /**
     * Draws characters centering it to cells of the same width.
     *
     * @param g graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param length number of charaters to draw
     * @param cellWidth width of cell to center into
     * @param positionX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChars(GraphicsContext g, char[] drawnChars, int charOffset, int length, int cellWidth, double positionX, double positionY) {
        int pos = 0;
        int group = 0;
        while (pos < length) {
            char drawnChar = drawnChars[charOffset + pos];
            double charWidth = metrics.getCharWidth(drawnChar);

            boolean groupable;
            if (metrics.hasUniformLineMetrics()) {
                groupable = charWidth == cellWidth;
            } else {
                double charsWidth = metrics.getCharsWidth(drawnChars, charOffset + pos - group, group + 1);
                groupable = charsWidth == cellWidth * (group + 1);
            }

            switch (Character.getDirectionality(drawnChar)) {
                case Character.DIRECTIONALITY_UNDEFINED:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                case Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT:
                case Character.DIRECTIONALITY_BOUNDARY_NEUTRAL:
                case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                    groupable = false;
            }

            if (groupable) {
                group++;
            } else {
                if (group > 0) {
                    drawShiftedChars(g, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
                    group = 0;
                }
                drawShiftedChars(g, drawnChars, charOffset + pos, 1, positionX + pos * cellWidth + ((cellWidth - charWidth) / 2), positionY);
            }
            pos++;
        }
        if (group > 0) {
            drawShiftedChars(g, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
        }
    }

    protected void drawShiftedChars(GraphicsContext g, char[] drawnChars, int charOffset, int length, double positionX, double positionY) {
        g.fillText(String.copyValueOf(drawnChars, charOffset, length), positionX, positionY);
    }

    /**
     * Precomputes widths for basic ascii characters.
     *
     * @param charset
     */
    private void buildCharMapping(Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private int getRowPositionLength() {
        if (minRowPositionLength > 0 && minRowPositionLength == maxRowPositionLength) {
            return minRowPositionLength;
        }

        long dataSize = structure.getDataSize();
        if (dataSize == 0) {
            return 1;
        }

        double natLog = Math.log(dataSize == Long.MAX_VALUE ? dataSize : dataSize + 1);
        int positionLength = (int) Math.ceil(natLog / PositionCodeType.HEXADECIMAL.getBaseLog());
        if (minRowPositionLength > 0 && positionLength < minRowPositionLength) {
            positionLength = minRowPositionLength;
        }
        if (maxRowPositionLength > 0 && positionLength > maxRowPositionLength) {
            positionLength = maxRowPositionLength;
        }

        return positionLength == 0 ? 1 : positionLength;
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
    public Rectangle2D getPositionRect(long dataPosition, int codeOffset, CodeAreaSection section) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        Point2D cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            return null;
        }

        DefaultCodeAreaCaret.CursorShape cursorShape = editationOperation == EditationOperation.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
        int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
        return new Rectangle2D(cursorPoint.getX(), cursorPoint.getY(), cursorThickness, rowHeight);
    }

    /**
     * Renders sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(GraphicsContext g, int startOffset, int endOffset, double rowPositionX, double positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        g.fillRect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    @Override
    public void updateScrollBars() {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
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
//            codeArea.repaint();
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
//            codeArea.repaint();
////            dataViewScrolled(codeArea.getGraphicsContext());
//        }
//    }
    private void notifyScrolled() {
        recomputeScrollState();
        // TODO
    }

    private static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
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
