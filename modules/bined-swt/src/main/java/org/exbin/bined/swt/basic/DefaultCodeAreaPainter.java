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
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationOperation;
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
import org.exbin.bined.capability.BackgroundPaintCapable;
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
import org.exbin.bined.swt.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.swt.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.swt.capability.FontCapable;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.basic.BasicCodeAreaLayout;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2019/09/17
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
    private volatile boolean childPaint = false;

    @Nonnull
    private final Composite dataView;
    @Nonnull
    private final ScrolledComposite scrollPanel;
    @Nonnull
    private final PaintListener codeAreaPaintListener;
    @Nonnull
    private final DefaultCodeAreaMouseListener codeAreaMouseListener;
    @Nonnull
    private final ControlListener codeAreaControlListener;
    @Nonnull
    private final DataChangedListener codeAreaDataChangeListener;

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

        dataView = new Composite(scrollPanel, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.NO_MERGE_PAINTS);
        codeAreaPaintListener = (PaintEvent paintEvent) -> {
            GC g = paintEvent.gc;
            if (g == null) {
                return;
            }

            paintMainArea(g);

            if (childPaint) {
                childPaint = false;
            } else {
                Rectangle codeAreaBounds = codeArea.getBounds();
                codeArea.redraw(0, 0, codeAreaBounds.width, codeAreaBounds.height, true);
            }
        };
//        dataView.setVisible(false);
//        dataView.setLayout(null);
        dataView.setBackgroundMode(SWT.INHERIT_NONE);
        // Fill whole area, no more suitable method found so far
        dataView.setSize(0, 0);

        codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
        codeAreaControlListener = new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent ce) {
                if (metrics.isInitialized()) {
                    recomputeLayout();
                }
            }
        };
        codeAreaDataChangeListener = () -> {
            validateCaret();
            recomputeLayout();
        };

//        dataView.layout();
        codeArea.update();
//        codeArea.layout();
    }

    @Override
    public void attach() {
        dataView.addPaintListener(codeAreaPaintListener);
        dataView.addMouseListener(codeAreaMouseListener);
        dataView.addMouseMoveListener(codeAreaMouseListener);
        dataView.addMouseWheelListener(codeAreaMouseListener);
        dataView.addMouseTrackListener(codeAreaMouseListener);
        codeArea.addMouseListener(codeAreaMouseListener);
        codeArea.addMouseMoveListener(codeAreaMouseListener);
        codeArea.addMouseWheelListener(codeAreaMouseListener);
        codeArea.addMouseTrackListener(codeAreaMouseListener);
        codeArea.addControlListener(codeAreaControlListener);
        codeArea.addDataChangedListener(codeAreaDataChangeListener);
        scrollPanel.setContent(dataView);
        codeArea.update();
    }

    @Override
    public void detach() {
        scrollPanel.setContent(null);
        dataView.removePaintListener(codeAreaPaintListener);
        dataView.removeMouseListener(codeAreaMouseListener);
        dataView.removeMouseMoveListener(codeAreaMouseListener);
        dataView.removeMouseWheelListener(codeAreaMouseListener);
        dataView.removeMouseTrackListener(codeAreaMouseListener);
        codeArea.removeMouseListener(codeAreaMouseListener);
        codeArea.removeMouseMoveListener(codeAreaMouseListener);
        codeArea.removeMouseWheelListener(codeAreaMouseListener);
        codeArea.removeMouseTrackListener(codeAreaMouseListener);
        codeArea.removeControlListener(codeAreaControlListener);
        codeArea.removeDataChangedListener(codeAreaDataChangeListener);
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
        dimensions.recomputeSizes(metrics, codeArea.getSize().x, codeArea.getSize().y, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
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

    public void fontChanged(GC g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        font = ((FontCapable) codeArea).getCodeFont();
        if (font == null) {
            font = g.getFont();
        } else {
            g.setFont(font);
        }
        metrics.recomputeMetrics(g, charset);

        recomputeDimensions();
        recomputeCharPositions();
        initialized = true;
    }

    public void dataViewScrolled(GC g) {
        if (!isInitialized()) {
            return;
        }

        recomputeScrollState();
        if (metrics.getCharacterWidth() > 0) {
            recomputeCharPositions();
            paintComponent(g);
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
    public void paintComponent(GC g) {
        if (!initialized) {
            reset();
        }
        updateCache();
        if (font == null) {
            fontChanged(g);
        }
        if (rowDataCache == null) {
            return;
        }
        if (scrollingState == ScrollingState.SCROLLING_BY_SCROLLBAR) {
            return;
        }
        if (layoutChanged) {
            recomputeLayout();
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintRowPosition(g);

        Rectangle dataViewBounds = dataView.getBounds();
        childPaint = true;
        dataView.redraw(0, 0, dataViewBounds.width, dataViewBounds.height, true);
//        dataView.get
//        paintMainArea(dataViewGC);
//        scrollPanel.paintComponents(g);
        paintCounter++;
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            colorsProfile.reinitialize();
        }
    }

    @Override
    public void repaint() {
        scrollPanel.layout(true);
        dataView.layout(true);
    }

    public void paintOutsiteArea(GC g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rectangle componentRect = dimensions.getComponentRectangle();
        int characterWidth = metrics.getCharacterWidth();
        g.setBackground(colorsProfile.getTextBackground());
        g.fillRectangle(componentRect.x, componentRect.y, componentRect.width, headerAreaHeight);

        // Decoration lines
        g.setForeground(colorsProfile.getDecorationLine());
        g.drawLine(componentRect.x, componentRect.y + headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1);

        {
            int lineX = componentRect.x + rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= componentRect.x) {
                g.drawLine(lineX, componentRect.y, lineX, headerAreaHeight);
            }
        }
    }

    public void paintHeader(GC g) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        Rectangle headerArea = dimensions.getHeaderAreaRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        Rectangle clipBounds = g.getClipping();
        g.setClipping(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();

        g.setBackground(colorsProfile.getTextBackground());
        g.fillRectangle(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            int headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            int headerY = 0;

            g.setForeground(colorsProfile.getTextColor());
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

                if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        drawCenteredChars(g, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setForeground(color);
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }

        // Decoration lines
        g.setForeground(colorsProfile.getDecorationLine());
        g.drawLine(headerArea.x, headerArea.y + headerArea.height - 1, headerArea.x + headerArea.width, headerArea.y + headerArea.height - 1);
        int lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, headerArea.y, lineX, headerArea.y + headerArea.height);
        }

        g.setClipping(clipBounds);
    }

    public void paintRowPosition(GC g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle rowPosRectangle = dimensions.getRowPositionAreaRectangle();
        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        Rectangle clipBounds = g.getClipping();
        g.setClipping(clipBounds != null ? clipBounds.intersection(rowPosRectangle) : rowPosRectangle);

        g.setBackground(colorsProfile.getTextBackground());
        g.fillRectangle(rowPosRectangle.x, rowPosRectangle.y, rowPosRectangle.width, rowPosRectangle.height);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            int stripePositionY = rowPosRectangle.y - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setBackground(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.fillRectangle(rowPosRectangle.x, stripePositionY, rowPosRectangle.width, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        int positionY = rowPosRectangle.y - scrollPosition.getRowOffset();
        g.setForeground(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, codeCharactersCase);
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangle.x, positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
        }

        // Decoration lines
        g.setForeground(colorsProfile.getDecorationLine());
        int lineX = rowPosRectangle.x + rowPosRectangle.width - (characterWidth / 2);
        if (lineX >= rowPosRectangle.x) {
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
    public void paintMainArea(GC g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            fontChanged(g);
            fontChanged = false;
        }

        Point location = scrollPanel.getLocation();
        Rectangle mainAreaRectSrc = dimensions.getMainAreaRectangle();
        Rectangle mainAreaRect = new Rectangle(mainAreaRectSrc.x - location.x, mainAreaRectSrc.y - location.y, mainAreaRectSrc.width, mainAreaRectSrc.height);
        Rectangle dataViewRectangle = dimensions.getDataViewInnerRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int previewRelativeX = visibility.getPreviewRelativeX();

//        Rectangle clipBounds = g.getClipping();
//        g.setClipping(clipBounds != null ? clipBounds.intersection(mainAreaRect) : mainAreaRect);
        paintBackground(g);

        // Decoration lines
        g.setForeground(colorsProfile.getDecorationLine());
        int lineX = dataViewRectangle.x + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewRectangle.x) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }

        paintRows(g);
//        g.setClipping(clipBounds);
        paintCursor(g);

        paintDebugInfo(g, mainAreaRect, scrollPosition);
    }
    
    // Debug
    private long paintCounter = 0;

    private void paintDebugInfo(GC g, Rectangle mainAreaRect, CodeAreaScrollPosition scrollPosition) {
        int x = mainAreaRect.x + mainAreaRect.width - 450;
        int y = mainAreaRect.y + mainAreaRect.height - dimensions.getHeaderAreaHeight() - 20;
        Display display = Display.getCurrent();
        g.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
        g.fillRectangle(x, y, 200, 16);
        g.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        char[] headerCode = (String.valueOf(scrollPosition.getCharPosition()) + "+" + String.valueOf(scrollPosition.getCharOffset()) + " : " + String.valueOf(scrollPosition.getRowPosition()) + "+" + String.valueOf(scrollPosition.getRowOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
        g.drawString(String.valueOf(headerCode), x, y, true);

        paintCounter++;
    }

    public void paintBackground(GC g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle dataViewRect = dimensions.getDataViewInnerRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        g.setBackground(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.fillRectangle(dataViewRect.x, dataViewRect.y, dataViewRect.width, dataViewRect.height);
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            int stripePositionY = dataViewRect.y - scrollPosition.getRowOffset() + (int) ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setBackground(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize - bytesPerRow) {
                    break;
                }

                g.fillRectangle(dataViewRect.x, stripePositionY, dataViewRect.width, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(GC g) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        Point location = scrollPanel.getLocation();
        int dataViewX = dimensions.getScrollPanelX() - location.x;
        int dataViewY = dimensions.getScrollPanelY() - location.y;
        int rowsPerRect = dimensions.getRowsPerRect();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = dataViewY + scrollPosition.getRowOffset();
        g.setForeground(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
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
    public void paintRowBackground(GC g, long rowDataPosition, int rowPositionX, int rowPositionY) {
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
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
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

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth % characterWidth, dataViewHeight % rowHeight, characterWidth, rowHeight);
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
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
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeCenterOnScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerRect, charactersPerRect, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    /**
     * Paints row text.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowText(GC g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();

        int positionY = rowPositionY;

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
            if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaSwtUtils.areSameColors(lastColor, renderColor)) {
                    g.setForeground(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charOnRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
                }

                renderColor = color;
                if (!CodeAreaSwtUtils.areSameColors(lastColor, renderColor)) {
                    g.setForeground(renderColor);
                    lastColor = renderColor;
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaSwtUtils.areSameColors(lastColor, renderColor)) {
                g.setForeground(renderColor);
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
    public void paintCursor(GC g) {
        if (!codeArea.isFocusControl()) {
//            return;
        }

        if (caretChanged) {
            updateCaret();
        }

        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        Rectangle mainAreaRect = dimensions.getMainAreaRectangle();
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
        mainAreaRect.x = 0;
        mainAreaRect.y = 0;
        Rectangle intersection = cursorRect.intersection(mainAreaRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.setClipping(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setBackground(colorsProfile.getCursorColor());

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
            g.setBackground(colorsProfile.getCursorColor());
            g.setForeground(colorsProfile.getCursorColor());
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

    private void paintCursorRect(GC g, int cursorX, int cursorY, int width, int height, CursorRenderingMode renderingMode, DefaultCodeAreaCaret caret) {
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
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, PositionOverflowMode overflowMode) {
        DefaultCodeAreaCaretPosition caret = new DefaultCodeAreaCaretPosition();
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

    @Nonnull
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
    public Point getPositionPoint(long dataPosition, int codeOffset, CodeAreaSection section) {
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

        int x = 0;
        int y = 0;
        int caretY = (int) (y + row * rowHeight) - scrollPosition.getRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            caretX = x + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
        } else {
            caretX = x + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new Point(caretX, caretY);
    }

    @Nullable
    private Rectangle getMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == BasicCodeAreaSection.CODE_MATRIX ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            return null;
        }

        // TODO Cache
        Rectangle mirrorCursorRect = new Rectangle(mirrorCursorPoint.x, mirrorCursorPoint.y, metrics.getCharacterWidth() * (section == BasicCodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        return mirrorCursorRect;
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        int dataViewX = dimensions.getScrollPanelX();
        int dataViewY = dimensions.getScrollPanelY();
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
    protected void drawCenteredChars(GC g, char[] drawnChars, int charOffset, int length, int cellWidth, int positionX, int positionY) {
        int pos = 0;
        int group = 0;
        while (pos < length) {
            char drawnChar = drawnChars[charOffset + pos];
            int charWidth = metrics.getCharWidth(g, drawnChar);

            boolean groupable;
            if (metrics.hasUniformLineMetrics()) {
                groupable = charWidth == cellWidth;
            } else {
                int charsWidth = metrics.getCharsWidth(g, drawnChars, charOffset + pos - group, group + 1);
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
                drawShiftedChars(g, drawnChars, charOffset + pos, 1, positionX + pos * cellWidth + ((cellWidth - charWidth) >> 1), positionY);
            }
            pos++;
        }
        if (group > 0) {
            drawShiftedChars(g, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
        }
    }

    protected void drawShiftedChars(GC g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        g.drawString(String.valueOf(drawnChars[charOffset]), positionX, positionY, true);
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
    public Rectangle getPositionRect(long dataPosition, int codeOffset, CodeAreaSection section) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        Point cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            return null;
        }

        DefaultCodeAreaCaret.CursorShape cursorShape = editationOperation == EditationOperation.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
        int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
        return new Rectangle(cursorPoint.x, cursorPoint.y, cursorThickness, rowHeight);
    }

    /**
     * Renders sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(GC g, int startOffset, int endOffset, int rowPositionX, int positionY) {
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
        recomputeScrollState();
        ((ScrollingCapable) codeArea).notifyScrolled();
    }

    @Override
    public void dispose() {
        colorsProfile.dispose();
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
