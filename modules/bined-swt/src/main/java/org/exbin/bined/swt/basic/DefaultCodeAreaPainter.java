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
package org.exbin.bined.swt.basic;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
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
import org.exbin.bined.ScrollBarVisibility;
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
import org.exbin.bined.swt.CodeAreaCore;
import org.exbin.bined.swt.CodeAreaPainter;
import org.exbin.bined.swt.CodeAreaSwtUtils;
import org.exbin.bined.swt.basic.DefaultCodeAreaCaret.CursorRenderingMode;
import org.exbin.bined.swt.capability.BackgroundPaintCapable;
import org.exbin.bined.swt.capability.FontCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2018/09/04
 * @author ExBin Project (https://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    @Nonnull
    protected final CodeAreaCore codeArea;
    private volatile boolean initialized = false;
    private volatile boolean adjusting = false;
    private volatile boolean fontChanged = false;
    private volatile boolean resetColors = true;

    @Nonnull
    private final Composite dataView;
    @Nonnull
    private final ScrolledComposite scrollPanel;

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

    private BasicCodeAreaColors colors = new BasicCodeAreaColors();

    @Nullable
    private CodeCharactersCase codeCharactersCase;
    @Nullable
    private EditationMode editationMode;
    @Nullable
    private BasicBackgroundPaintMode backgroundPaintMode;
    private boolean showMirrorCursor;

    private int maxBytesPerChar;
    private int rowPositionLength;
    private int rowPositionNumberLength;

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
    // Debug
    private long paintCounter = 0;

    public DefaultCodeAreaPainter(@Nonnull CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        codeArea.setLayout(null);

        scrollPanel = new ScrolledComposite(codeArea, SWT.H_SCROLL | SWT.V_SCROLL);
        ScrollBar verticalScrollBar = scrollPanel.getVerticalBar();
        verticalScrollBar.addSelectionListener(new VerticalSelectionListener());
        ScrollBar horizontalScrollBar = scrollPanel.getHorizontalBar();
        horizontalScrollBar.addSelectionListener(new HorizontalSelectionListener());
//        codeArea.add(scrollPanel);
        scrollPanel.setBackgroundMode(SWT.INHERIT_NONE);
//        scrollPanel.setViewportBorder(null);
//        scrollPanel.getViewport().setOpaque(false);

        dataView = new Composite(scrollPanel, SWT.NONE);
        dataView.addPaintListener((@Nonnull PaintEvent paintEvent) -> {
            GC g = paintEvent.gc;
            if (g == null) {
                return;
            }
            
            paintMainArea(g);
        });
//        dataView.setVisible(false);
//        dataView.setLayout(null);
        dataView.setBackgroundMode(SWT.INHERIT_NONE);
        // Fill whole area, no more suitable method found so far
        dataView.setSize(0, 0);

        scrollPanel.setContent(dataView);

        DefaultCodeAreaMouseListener codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
        codeArea.addMouseListener(codeAreaMouseListener);
        codeArea.addMouseMoveListener(codeAreaMouseListener);
        codeArea.addMouseWheelListener(codeAreaMouseListener);
        codeArea.addMouseTrackListener(codeAreaMouseListener);
        dataView.addMouseListener(codeAreaMouseListener);
        dataView.addMouseMoveListener(codeAreaMouseListener);
        dataView.addMouseWheelListener(codeAreaMouseListener);
        dataView.addMouseTrackListener(codeAreaMouseListener);
//        dataView.layout();
        codeArea.update();
//        codeArea.layout();
    }

    @Override
    public void reset() {
        resetColors();
        resetFont();
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
        rowPositionLength = getRowPositionLength();
        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        dimensions.recomputeSizes(metrics, codeArea.getSize().x, codeArea.getSize().y, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
        int charactersPerPage = dimensions.getCharactersPerPage();

        structure.updateCache(codeArea, charactersPerPage);
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        editationMode = ((EditationModeCapable) codeArea).getEditationMode();
        backgroundPaintMode = ((BackgroundPaintCapable) codeArea).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();
        rowPositionNumberLength = ((RowWrappingCapable) codeArea).getRowPositionNumberLength();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int charactersPerRow = structure.getCharactersPerRow();
        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        resetScrollState();
    }

    public void resetCharPositions() {
        visibility.recomputeCharPositions(metrics, structure, dimensions, scrolling.getScrollPosition());
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

    public void resetFont(@Nonnull GC g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        maxBytesPerChar = (int) encoder.maxBytesPerChar();

        font = ((FontCapable) codeArea).getFont();
        if (font == null) {
            font = g.getFont();
        } else {
            g.setFont(font);
        }
        metrics.recomputeMetrics(g);

        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        dimensions.recomputeSizes(metrics, codeArea.getSize().x, codeArea.getSize().y, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
        resetCharPositions();
        initialized = true;
    }

    public void dataViewScrolled(@Nonnull GC g) {
        if (!isInitialized()) {
            return;
        }

        resetScrollState();
        if (metrics.getCharacterWidth() > 0) {
            resetCharPositions();
            paintComponent(g);
        }
    }

    private void resetScrollState() {
        scrolling.setScrollPosition(((ScrollingCapable) codeArea).getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

        if (characterWidth > 0) {
            resetCharPositions();
        }

        if (rowHeight > 0 && characterWidth > 0) {
            int documentDataWidth = structure.getCharactersPerRow() * characterWidth;
            long rowsPerData = (structure.getDataSize() / structure.getBytesPerRow()) + 1;
            scrolling.updateCache(codeArea);

            int documentDataHeight;
            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.SCALED);
                documentDataHeight = Integer.MAX_VALUE;
            } else {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.NORMAL);
                documentDataHeight = (int) (rowsPerData * rowHeight);
            }

//            dataView.setLocation(rowPositionAreaWidth, headerAreaHeight);
            dataView.setLocation(0, 0);
            dataView.setSize(documentDataWidth, documentDataHeight);
//            scrollPanel.getHorizontalBar().setMaximum(documentDataWidth);
//            scrollPanel.getVerticalBar().setMaximum(documentDataHeight);
//            codeArea.layout();
        }

//        dataView.setBounds(dimensions.getDataViewRectangle());
//            dataView.setLocation(dataViewXrowPositionAreaWidth, headerAreaHeight);
//            dataView.setSize(documentDataWidth, documentDataHeight);
        // TODO on resize only
        scrollPanel.setBounds(dimensions.getScrollPanelRectangle());
        scrollPanel.redraw();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void paintComponent(@Nonnull GC g) {
        if (!initialized) {
            reset();
        }
        updateCache();
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
//        paintMainArea(g);
//        scrollPanel.paintComponents(g);
        paintCounter++;
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;

            Display display = Display.getCurrent();
            Color foreground = codeArea.getForeground();
            if (foreground == null) {
                foreground = display.getSystemColor(SWT.COLOR_BLACK);
            }

            Color background = codeArea.getBackground();
            if (background == null) {
                background = display.getSystemColor(SWT.COLOR_WHITE);
            }
            Color selectionForeground = display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
            if (selectionForeground == null) {
                selectionForeground = display.getSystemColor(SWT.COLOR_WHITE);
            }
            Color selectionBackground = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
            if (selectionBackground == null) {
                selectionBackground = new Color(display, 96, 96, 255);
            }
            Color selectionMirrorForeground = selectionForeground;
            Color selectionMirrorBackground = CodeAreaSwtUtils.computeGrayColor(selectionBackground);
            Color cursor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
            if (cursor == null) {
                cursor = display.getSystemColor(SWT.COLOR_BLACK);
            }
            Color negativeCursor = CodeAreaSwtUtils.createNegativeColor(cursor);
            Color cursorMirror = cursor;
            Color negativeCursorMirror = negativeCursor;
            Color decorationLine = display.getSystemColor(SWT.COLOR_GRAY);

            Color stripes = CodeAreaSwtUtils.createOddColor(background);

            colors.setForeground(foreground);
            colors.setBackground(background);
            colors.setSelectionForeground(selectionForeground);
            colors.setSelectionBackground(selectionBackground);
            colors.setSelectionMirrorForeground(selectionMirrorForeground);
            colors.setSelectionMirrorBackground(selectionMirrorBackground);
            colors.setCursor(cursor);
            colors.setCursorMirror(cursorMirror);
            colors.setNegativeCursor(negativeCursor);
            colors.setNegativeCursorMirror(negativeCursorMirror);
            colors.setDecorationLine(decorationLine);
            colors.setStripes(stripes);
        }
    }

    @Override
    public void repaint() {
        scrollPanel.redraw();
        dataView.redraw();
    }

    boolean test = false;

    public void paintOutsiteArea(@Nonnull GC g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int componentWidth = dimensions.getComponentWidth();
        int characterWidth = metrics.getCharacterWidth();
        g.setBackground(colors.getBackground());
        g.fillRectangle(0, 0, componentWidth, headerAreaHeight);

        // Decoration lines
        g.setForeground(colors.getDecorationLine());
        g.drawLine(0, headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1);

        {
            int lineX = rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= 0) {
                g.drawLine(lineX, 0, lineX, headerAreaHeight);
            }
        }
    }

    public void paintHeader(@Nonnull GC g) {
        int charactersPerCodeSection = structure.getCharactersPerCodeSection();
        Rectangle clipBounds = g.getClipping();
        Rectangle headerArea = dimensions.getHeaderAreaRectangle();
        g.setClipping(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getDataViewX();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int componentWidth = dimensions.getComponentWidth();

        g.setBackground(colors.getBackground());
        g.fillRectangle(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        // Decoration lines
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.setBackground(colors.getDecorationLine());
        g.fillRectangle(0, headerAreaHeight - 1, componentWidth, 1);
        int lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, 0, lineX, headerAreaHeight);
        }

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            int headerY = 0;

            g.setForeground(colors.getForeground());
            char[] headerChars = new char[charactersPerCodeSection];
            Arrays.fill(headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            int visibleCodeStart = visibility.getVisibleCodeStart();
            int visibleMatrixCodeEnd = visibility.getVisibleMatrixCodeEnd();
            for (int index = visibleCodeStart; index < visibleMatrixCodeEnd; index++) {
                int codePos = structure.computeFirstCodeCharacterPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int visibleCharStart = visibility.getVisibleCharStart();
            int visibleMatrixCharEnd = visibility.getVisibleMatrixCharEnd();
            int renderOffset = visibleCharStart;
//            ColorsGroup.ColorType renderColorType = null;
            Color renderColor = null;
            for (int characterOnRow = visibleCharStart; characterOnRow < visibleMatrixCharEnd; characterOnRow++) {
                int byteOnRow;
                byteOnRow = structure.computePositionByte(characterOnRow);
                boolean sequenceBreak = false;
                boolean nativeWidth = true;

                int currentCharWidth = 0;
//                ColorsGroup.ColorType colorType = ColorsGroup.ColorType.TEXT;
//                if (characterRenderingMode != CharacterRenderingMode.LINE_AT_ONCE) {
                char currentChar = ' ';
//                    if (colorType == ColorsGroup.ColorType.TEXT) {
                currentChar = headerChars[characterOnRow];
//                    }
                if (currentChar == ' ' && renderOffset == characterOnRow) {
                    renderOffset++;
                    continue;
                }
                if (metrics.isMonospaceFont()) { // characterRenderingMode == CharacterRenderingMode.AUTO && 
                    // Detect if character is in unicode range covered by monospace fonts
                    if (CodeAreaSwtUtils.isMonospaceFullWidthCharater(currentChar)) {
                        currentCharWidth = characterWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = g.textExtent(String.valueOf(currentChar)).x;
                    nativeWidth = currentCharWidth == characterWidth;
                }
//                } else {
//                currentCharWidth = characterWidth;
//                }

                Color color = colors.getForeground();
//                getHeaderPositionColor(byteOnRow, charOnRow);
//                if (renderColorType == null) {
//                    renderColorType = colorType;
//                    renderColor = color;
//                    g.setForeground(color);
//                }

                if (!nativeWidth || !CodeAreaSwtUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        // TODO optimize
                        char[] subArray = Arrays.copyOfRange(headerChars, renderOffset, characterOnRow);
                        g.drawString(String.valueOf(subArray), headerX + renderOffset * characterWidth, headerY, true);
                    }

//                    if (!colorType.equals(renderColorType)) {
//                        renderColorType = colorType;
//                    }
                    if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setForeground(color);
                    }

                    if (!nativeWidth) {
                        renderOffset = characterOnRow + 1;
//                        if (characterRenderingMode == CharacterRenderingMode.TOP_LEFT) {
//                            g.drawChars(headerChars, characterOnRow, 1, headerX + characterOnRow * characterWidth, headerY);
//                        } else {
                        int positionX = headerX + characterOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
                        drawShiftedChar(g, headerChars, characterOnRow, characterWidth, positionX, headerY);
//                        }
                    } else {
                        renderOffset = characterOnRow;
                    }
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                char[] subArray = Arrays.copyOfRange(headerChars, renderOffset, charactersPerCodeSection);
                g.drawString(String.valueOf(subArray), headerX + renderOffset * characterWidth, headerY, true);
            }
        }

        g.setClipping(clipBounds);
    }

    public void paintRowPosition(@Nonnull GC g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        Rectangle clipBounds = g.getClipping();
        Rectangle rowPositionsArea = dimensions.getRowPositionAreaRectangle();
        g.setClipping(clipBounds != null ? clipBounds.intersection(rowPositionsArea) : rowPositionsArea);

        g.setBackground(colors.getBackground());
        g.fillRectangle(rowPositionsArea.x, rowPositionsArea.y, rowPositionsArea.width, rowPositionsArea.height);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setBackground(colors.getStripes());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.fillRectangle(0, stripePositionY, rowPositionAreaWidth, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        int positionY = headerAreaHeight - scrollPosition.getRowOffset();
        g.setForeground(colors.getForeground());
        Rectangle compRect = new Rectangle(0, 0, 0, 0);
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, structure.getCodeType().getBase(), rowPositionLength, true, CodeCharactersCase.UPPER);
//            if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
//            } else {
            for (int digitIndex = 0; digitIndex < rowPositionLength; digitIndex++) {
                drawCenteredChar(g, rowDataCache.rowPositionCode, digitIndex, characterWidth, compRect.x + characterWidth * digitIndex, positionY);
            }
//            }

            positionY += rowHeight;
            dataPosition += bytesPerRow;
        }

        g.setForeground(colors.getDecorationLine());
        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }
        g.drawLine(dataViewRectangle.x, dataViewRectangle.y - 1, dataViewRectangle.x + dataViewRectangle.width, dataViewRectangle.y - 1);

        g.setClipping(clipBounds);
    }

    /**
     * Paints main area.
     *
     * @param g GC of dataView
     */
    @Override
    public void paintMainArea(@Nonnull GC g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            resetFont(g);
            fontChanged = false;
        }

        Rectangle clipBounds = g.getClipping();
        Rectangle mainArea = dimensions.getMainAreaRect();
        mainArea.x = 0;
        mainArea.y = 0;
        g.setClipping(clipBounds != null ? clipBounds.intersection(mainArea) : mainArea);
        paintBackground(g);

        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        int characterWidth = metrics.getCharacterWidth();
        int previewRelativeX = visibility.getPreviewRelativeX();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.setForeground(colors.getDecorationLine());
        int lineX = previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewRectangle.x) {
            g.drawLine(lineX, 0, lineX, dataViewRectangle.height);
        }

        paintRows(g);
        g.setClipping(clipBounds);
        paintCursor(g);

        // TODO: Remove later
        int x = dimensions.getComponentWidth() - dimensions.getRowPositionAreaWidth() - 500;
        int y = dimensions.getComponentHeight() - dimensions.getHeaderAreaHeight() - 20;
        Display display = Display.getCurrent();
        g.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
        g.fillRectangle(x, y, 200, 16);
        g.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        char[] headerCode = (String.valueOf(scrollPosition.getCharPosition()) + "+" + String.valueOf(scrollPosition.getCharOffset()) + " : " + String.valueOf(scrollPosition.getRowPosition()) + "+" + String.valueOf(scrollPosition.getRowOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
        g.drawString(String.valueOf(headerCode), x, y, true);

        paintCounter++;
    }

    public void paintBackground(@Nonnull GC g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int rowsPerRect = dimensions.getRowsPerRect();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowPositionX = 0;
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.setBackground(colors.getBackground());
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.fillRectangle(rowPositionX, headerAreaHeight, dataViewWidth, dataViewHeight);
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + (int) ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setBackground(colors.getStripes());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize - bytesPerRow) {
                    break;
                }

                g.fillRectangle(rowPositionX, stripePositionY, dataViewWidth, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(@Nonnull GC g) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getDataViewX();
        int dataViewY = dimensions.getDataViewY();
        int rowsPerRect = dimensions.getRowsPerRect();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int rowPositionX = -scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = scrollPosition.getRowOffset();
        g.setForeground(colors.getForeground());
        for (int row = 0; row <= rowsPerRect; row++) {
            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
            paintRowText(g, dataPosition, rowPositionX, rowPositionY);

            rowPositionY += rowHeight;
            dataPosition += bytesPerRow;
        }
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
            BinaryData content = codeArea.getContentData();
            if (content == null) {
                throw new IllegalStateException("Missing data on nonzero data size");
            }
            content.copyToArray(dataPosition + rowStart, rowDataCache.rowData, rowStart, rowDataSize - rowStart);
            if (dataPosition + rowBytesLimit > dataSize) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int visibleCodeStart = visibility.getVisibleCodeStart();
            int visibleCodeEnd = visibility.getVisibleCodeEnd();
            for (int byteOnRow = Math.max(visibleCodeStart, rowStart); byteOnRow < Math.min(visibleCodeEnd, rowBytesLimit); byteOnRow++) {
                byte dataByte = rowDataCache.rowData[byteOnRow];
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(byteOnRow), codeCharactersCase);
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
            }
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            int visiblePreviewStart = visibility.getVisiblePreviewStart();
            int visiblePreviewEnd = visibility.getVisiblePreviewEnd();
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
    public void paintRowBackground(@Nonnull GC g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = structure.getPreviewCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getCharactersPerRow();
        int visibleCharStart = visibility.getVisibleCharStart();
        int visibleCharEnd = visibility.getVisibleCharEnd();
        int renderOffset = visibleCharStart;
        Color renderColor = null;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
            int section;
            int byteOnRow;
            if (charOnRow >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
            }
            boolean sequenceBreak = false;

            Color color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, rowPositionY);
                    }
                }

                if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.setBackground(color);
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
            return section == caretPosition.getSection() ? colors.getSelectionBackground() : colors.getSelectionMirrorBackground();
        }

        return null;
    }

    @Nullable
    @Override
    public CodeAreaScrollPosition computeRevealScrollPosition(@Nonnull CaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = structure.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getCharactersPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, previewCharPos, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = structure.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        int charactersPerRect = dimensions.getCharactersPerRect();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeCenterOnScrollPosition(rowPosition, charPosition, bytesPerRow, previewCharPos, rowsPerRect, charactersPerRect, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    /**
     * Paints row text.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowText(@Nonnull GC g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = structure.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        boolean monospaceFont = metrics.isMonospaceFont();

        int positionY = rowPositionY;

        Color lastColor = null;
        Color renderColor = null;

        int visibleCharStart = visibility.getVisibleCharStart();
        int visibleCharEnd = visibility.getVisibleCharEnd();
        int renderOffset = visibleCharStart;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
            int section;
            int byteOnRow;
            if (charOnRow >= previewCharPos) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
            }

            Color color = getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (color == null) {
                color = colors.getForeground();
            }

            boolean sequenceBreak = false;
            if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) {
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
                if (CodeAreaSwtUtils.isMonospaceFullWidthCharater(currentChar)) {
                    currentCharWidth = characterWidth;
                }
            }

            boolean nativeWidth = true;
            if (currentCharWidth == 0) {
                currentCharWidth = g.textExtent(String.valueOf(currentChar)).x;
                nativeWidth = currentCharWidth == characterWidth;
            }

            if (!nativeWidth) {
                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaSwtUtils.areSameColors(lastColor, renderColor)) {
                    g.setForeground(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    renderCharSequence(g, renderOffset, charOnRow, rowPositionX, positionY);
                }

                renderColor = color;
                if (!CodeAreaSwtUtils.areSameColors(lastColor, renderColor)) {
                    g.setForeground(renderColor);
                    lastColor = renderColor;
                }

                if (!nativeWidth) {
                    renderOffset = charOnRow + 1;
                    int positionX = rowPositionX + charOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
                    drawShiftedChar(g, rowDataCache.rowCharacters, charOnRow, characterWidth, positionX, positionY);
                } else {
                    renderOffset = charOnRow;
                }
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaSwtUtils.areSameColors(lastColor, renderColor)) {
                g.setForeground(renderColor);
            }

            renderCharSequence(g, renderOffset, charactersPerRow, rowPositionX, positionY);
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
        SelectionRange selectionRange = structure.getSelectionRange();
        CodeAreaCaretPosition caretPosition = structure.getCaretPosition();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection) {
            return section == caretPosition.getSection() ? colors.getSelectionForeground() : colors.getSelectionMirrorForeground();
        }

        return null;
    }

    @Override
    public void paintCursor(@Nonnull GC g) {
        if (!codeArea.isFocusControl()) {
//            return;
        }

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
        Rectangle cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        Rectangle clipBounds = g.getClipping();
        Rectangle mainAreaRect = dimensions.getMainAreaRect();
        mainAreaRect.x = 0;
        mainAreaRect.y = 0;
        Rectangle intersection = cursorRect.intersection(mainAreaRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.setClipping(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setBackground(colors.getCursor());

//            if (renderingMode == DefaultCodeAreaCaret.CursorRenderingMode.XOR) {
//                g.setXORMode(true); // Color.WHITE
//            }
            paintCursorRect(g, intersection.x, intersection.y, intersection.width, intersection.height, renderingMode, caret);

//            if (renderingMode == DefaultCodeAreaCaret.CursorRenderingMode.XOR) {
//                throw new UnsupportedOperationException("Not supported yet.");
//                // TODO g.setPaintMode();
//            }
            g.setClipping(clipBounds);
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            Rectangle mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            g.setBackground(colors.getCursorMirror());
            g.setForeground(colors.getCursorMirror());
            if (mirrorCursorRect != null) {
                intersection = mirrorCursorRect.intersection(mainAreaRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.setLineStyle(SWT.LINE_DASH);
                    g.drawRectangle(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width, mirrorCursorRect.height);
                }
            }
        }
    }

    private void paintCursorRect(@Nonnull GC g, int cursorX, int cursorY, int width, int height, @Nonnull CursorRenderingMode renderingMode, @Nonnull DefaultCodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRectangle(cursorX, cursorY, width, height);
                break;
            }
            case XOR: {
                Rectangle rect = new Rectangle(cursorX, cursorY, width, height);
                Rectangle intersection = g.getClipping().intersection(rect);
                if (!intersection.isEmpty()) {
                    g.fillRectangle(intersection.x, intersection.y, intersection.width, intersection.height);
                }
                break;
            }
            case NEGATIVE: {
                /*                Rectangle rect = new Rectangle(x, y, width, height);
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
                int scrolledX = x + scrollPosition.getCharPosition() * charWidth + scrollPosition.getCharOffset();
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
                    int posX = previewX + charPos * charWidth - scrollPosition.getCharPosition() * charWidth - scrollPosition.getCharOffset();
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
                    int posX = codeRect.x + codeCharPos * charWidth - scrollPosition.getCharPosition() * charWidth - scrollPosition.getCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
                        g.drawText(String.valueOf(lineChars[charsOffset]), posX + (charsOffset * charWidth), posY);
                    } else {
                        drawCenteredChar(g, lineChars, charsOffset, charWidth, posX + (charsOffset * charWidth), posY);
                    }
                }
                g.setClipping(clip); */
                break;
            }
        }
    }

    @Nonnull
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, @Nonnull PositionOverflowMode overflowMode) {
        CodeAreaCaretPosition caret = new CodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

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
    public CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementDirection direction) {
        return structure.computeMovePosition(position, direction, dimensions.getRowsPerPage());
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull ScrollingDirection direction) {
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
    public Point getPositionPoint(long dataPosition, int codeOffset, int section) {
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

        Rectangle dataViewRect = dimensions.getDataViewRectangle();
        dataViewRect.x = 0;
        dataViewRect.y = 0;
        int caretY = (int) (dataViewRect.y + row * rowHeight) - scrollPosition.getRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            caretX = dataViewRect.x + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
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

        // TODO Cache
        Rectangle mirrorCursorRect = new Rectangle(mirrorCursorPoint.x, mirrorCursorPoint.y, metrics.getCharacterWidth() * (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection() ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        return mirrorCursorRect;
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        int dataViewX = dimensions.getDataViewX();
        int dataViewY = dimensions.getDataViewY();
        int scrollPanelWidth = dimensions.getScrollPanelWidth();
        int scrollPanelHeight = dimensions.getScrollPanelHeight();
        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
            return SWT.CURSOR_IBEAM;
        }

        return SWT.CURSOR_ARROW;
    }

    @Override
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        return dimensions.getPositionZone(positionX, positionY);
    }

    @Nonnull
    @Override
    public BasicCodeAreaColors getBasicColors() {
        return colors;
    }

    @Override
    public void setBasicColors(@Nonnull BasicCodeAreaColors colors) {
        this.colors = colors;
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
    protected void drawCenteredChar(GC g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        int charWidth = g.textExtent(String.valueOf(drawnChars[charOffset])).x;
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, positionX + ((charWidthSpace + 1 - charWidth) >> 1), positionY);
    }

    protected void drawShiftedChar(GC g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        g.drawString(String.valueOf(drawnChars[charOffset]), positionX, positionY, true);
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
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
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
    private void renderCharSequence(@Nonnull GC g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        int characterWidth = metrics.getCharacterWidth();
        char[] subArray = Arrays.copyOfRange(rowDataCache.rowCharacters, startOffset, endOffset);
        g.drawString(String.valueOf(subArray), rowPositionX + startOffset * characterWidth, positionY, true);
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(@Nonnull GC g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        g.fillRectangle(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    @Override
    public void updateScrollBars() {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        long rowsPerDocument = structure.getRowsPerDocument();

        adjusting = true;
        ScrollBar verticalScrollBar = scrollPanel.getVerticalBar();
        ScrollBar horizontalScrollBar = scrollPanel.getHorizontalBar();

        boolean alwaysShowVerticalScrollBar = scrolling.getVerticalScrollBarVisibility() == ScrollBarVisibility.ALWAYS;
        boolean verticalScrollBarVisible = scrolling.getVerticalScrollBarVisibility() != ScrollBarVisibility.NEVER;
        scrollPanel.getVerticalBar().setVisible(verticalScrollBarVisible);
        boolean alwaysShowHorizontalScrollBar = scrolling.getHorizontalScrollBarVisibility() == ScrollBarVisibility.ALWAYS;
        boolean horizontalScrollBarVisible = scrolling.getHorizontalScrollBarVisibility() != ScrollBarVisibility.NEVER;
        scrollPanel.getHorizontalBar().setVisible(horizontalScrollBarVisible);
        scrollPanel.setAlwaysShowScrollBars(alwaysShowVerticalScrollBar || alwaysShowHorizontalScrollBar);

        int verticalScrollValue = scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument);
        verticalScrollBar.setSelection(verticalScrollValue);

        int horizontalScrollValue = scrolling.getHorizontalScrollValue(characterWidth);
        horizontalScrollBar.setSelection(horizontalScrollValue);

        adjusting = false;
    }

    private int getHorizontalScrollBarSize() {
        ScrollBar horizontalScrollBar = scrollPanel.getHorizontalBar();
        int size;
        if (horizontalScrollBar.isVisible()) {
            size = horizontalScrollBar.getSize().y;
        } else {
            size = 0;
        }

        return size;
    }

    private int getVerticalScrollBarSize() {
        ScrollBar verticalScrollBar = scrollPanel.getVerticalBar();
        int size;
        if (verticalScrollBar.isVisible()) {
            size = verticalScrollBar.getSize().x;
        } else {
            size = 0;
        }

        return size;
    }

    private class VerticalSelectionListener implements SelectionListener {

        public VerticalSelectionListener() {
        }

        @Override
        public void widgetSelected(SelectionEvent se) {
            if (se == null || adjusting) {
                return;
            }

            int scrollBarValue = scrollPanel.getVerticalBar().getSelection();
            int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalBar().getMaximum();
            long rowsPerDocumentToLastPage = structure.getRowsPerDocument() - dimensions.getRowsPerRect();
            scrolling.updateVerticalScrollBarValue(scrollBarValue, metrics.getRowHeight(), maxValue, rowsPerDocumentToLastPage);
            ((ScrollingCapable) codeArea).setScrollPosition(scrolling.getScrollPosition());
            notifyScrolled();
            repaint();
//            dataViewScrolled(codeArea.getGraphics());
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent se) {
        }
    }

    private class HorizontalSelectionListener implements SelectionListener {

        public HorizontalSelectionListener() {
        }

        @Override
        public void widgetSelected(SelectionEvent se) {
            if (se == null || adjusting) {
                return;
            }

            int scrollBarValue = scrollPanel.getHorizontalBar().getSelection();
            scrolling.updateHorizontalScrollBarValue(scrollBarValue, metrics.getCharacterWidth());
            ((ScrollingCapable) codeArea).setScrollPosition(scrolling.getScrollPosition());
            notifyScrolled();
            repaint();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent se) {
        }
    }

    private void notifyScrolled() {
        resetScrollState();
        ((ScrollingCapable) codeArea).notifyScrolled();
    }

    @Override
    public void dispose() {
        colors.dispose();
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
