/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.BackgroundPaintCapable;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret.CursorRenderingMode;
import org.exbin.bined.swing.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.swing.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.swing.capability.AntialiasingCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.basic.BasicCodeAreaLayout;
import org.exbin.bined.basic.ScrollViewDimension;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.swing.CodeAreaCharAssessor;
import org.exbin.bined.swing.CodeAreaPaintState;
import org.exbin.bined.swing.CodeAreaSwingControl;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.CodeAreaColorAssessor;

/**
 * Code area component default painter.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaPainter implements CodeAreaPainter, BasicColorsCapableCodeAreaPainter, CodeAreaPaintState {

    @Nonnull
    protected final CodeAreaCore codeArea;
    protected volatile boolean initialized = false;

    protected volatile boolean fontChanged = false;
    protected volatile boolean layoutChanged = true;
    protected volatile boolean resetColors = true;
    protected volatile boolean caretChanged = true;

    @Nonnull
    protected final JComponent dataView;
    @Nonnull
    protected final DefaultCodeAreaScrollPane scrollPanel;
    @Nonnull
    protected final DefaultCodeAreaMouseListener codeAreaMouseListener;
    @Nonnull
    protected final ComponentListener codeAreaComponentListener;
    @Nonnull
    protected final DataChangedListener codeAreaDataChangeListener;

    @Nonnull
    protected final BasicCodeAreaMetrics metrics = new BasicCodeAreaMetrics();
    @Nonnull
    protected final BasicCodeAreaStructure structure = new BasicCodeAreaStructure();
    @Nonnull
    protected final BasicCodeAreaScrolling scrolling = new BasicCodeAreaScrolling();
    @Nonnull
    protected final BasicCodeAreaDimensions dimensions = new BasicCodeAreaDimensions();
    @Nonnull
    protected final BasicCodeAreaVisibility visibility = new BasicCodeAreaVisibility();

    @Nonnull
    protected final BasicCodeAreaLayout layout = new BasicCodeAreaLayout();
    @Nonnull
    protected BasicCodeAreaColorsProfile colorsProfile = new BasicCodeAreaColorsProfile();

    @Nullable
    protected CodeCharactersCase codeCharactersCase;
    @Nullable
    protected EditOperation editOperation;
    @Nullable
    protected BasicBackgroundPaintMode backgroundPaintMode;
    @Nullable
    protected ScrollViewDimension viewDimension;
    protected boolean showMirrorCursor;
    @Nonnull
    protected AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;

    protected int rowPositionLength;
    protected int minRowPositionLength;
    protected int maxRowPositionLength;

    @Nullable
    protected Font font;
    @Nullable
    protected Charset charset;
    @Nonnull
    protected CodeAreaColorAssessor colorAssessor = null;
    @Nonnull
    protected CodeAreaCharAssessor charAssessor = null;

    @Nullable
    protected RowDataCache rowDataCache = null;
    @Nullable
    protected CursorDataCache cursorDataCache = null;

    public DefaultCodeAreaPainter(CodeAreaCore codeArea) {
        this.codeArea = codeArea;

        colorAssessor = new DefaultCodeAreaColorAssessor();
        charAssessor = new DefaultCodeAreaCharAssessor();

        dataView = new JComponent() {
        };
        dataView.setBorder(null);
        dataView.setVisible(false);
        dataView.setLayout(null);
        dataView.setOpaque(false);
        dataView.setInheritsPopupMenu(true);
        // Fill whole area, no more suitable method found so far
        dataView.setPreferredSize(new Dimension(0, 0));
        scrollPanel = new DefaultCodeAreaScrollPane((CodeAreaSwingControl) codeArea, metrics, structure, dimensions, scrolling);
        scrollPanel.setViewportView(dataView);
        JViewport viewport = scrollPanel.getViewport();
        viewport.setOpaque(false);
        scrolling.setHorizontalExtentChangeListener(this::horizontalExtentChanged);
        scrolling.setVerticalExtentChangeListener(this::verticalExtentChanged);

        codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
        viewport.addMouseListener(codeAreaMouseListener);
        viewport.addMouseMotionListener(codeAreaMouseListener);
        viewport.addMouseWheelListener(codeAreaMouseListener);
        viewport.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int verticalScrollBarSize = getVerticalScrollBarSize();
                int horizontalScrollBarSize = getHorizontalScrollBarSize();

                if (dimensions.getVerticalScrollBarSize() != verticalScrollBarSize || dimensions.getHorizontalScrollBarSize() != horizontalScrollBarSize) {
                    recomputeDimensions();
                    recomputeScrollState();
                }

                JViewport viewport = scrollPanel.getViewport();
                if (viewDimension != null && (viewDimension.getDataViewWidth() != viewport.getWidth() || viewDimension.getDataViewHeight() != viewport.getHeight())) {
                    updateScrollBars();
                }
            }
        });
        codeAreaComponentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recomputeLayout();
            }
        };
        codeAreaDataChangeListener = this::dataChanged;
        DefaultCodeAreaPainter.this.rebuildColors();
    }

    @Override
    public void attach() {
        codeArea.add(scrollPanel);
        codeArea.addMouseListener(codeAreaMouseListener);
        codeArea.addMouseMotionListener(codeAreaMouseListener);
        codeArea.addMouseWheelListener(codeAreaMouseListener);
        codeArea.addComponentListener(codeAreaComponentListener);
        codeArea.addDataChangedListener(codeAreaDataChangeListener);
    }

    @Override
    public void detach() {
        codeArea.remove(scrollPanel);
        codeArea.removeMouseListener(codeAreaMouseListener);
        codeArea.removeMouseMotionListener(codeAreaMouseListener);
        codeArea.removeMouseWheelListener(codeAreaMouseListener);
        codeArea.removeComponentListener(codeAreaComponentListener);
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

    @Override
    public void rebuildColors() {
        colorsProfile.reinitialize();
    }

    private void recomputeLayout() {
        rowPositionLength = getRowPositionLength();
        recomputeDimensions();

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(codeArea, charactersPerPage);
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        backgroundPaintMode = ((BackgroundPaintCapable) codeArea).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();
        antialiasingMode = ((AntialiasingCapable) codeArea).getAntialiasingMode();
        minRowPositionLength = ((RowWrappingCapable) codeArea).getMinRowPositionLength();
        maxRowPositionLength = ((RowWrappingCapable) codeArea).getMaxRowPositionLength();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int charactersPerRow = structure.getCharactersPerRow();

        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        updateScrollBars();

        layoutChanged = false;
    }

    private void updateCaret() {
        editOperation = ((EditModeCapable) codeArea).getActiveOperation();

        caretChanged = false;
    }

    private void validateCaret() {
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCodeAreaCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() > codeArea.getDataSize()) {
            caret.setCaretPosition(null);
        }
    }

    private void validateSelection() {
        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
        if (!selectionHandler.isEmpty()) {
            long dataSize = codeArea.getDataSize();
            if (dataSize == 0) {
                ((SelectionCapable) codeArea).clearSelection();
            } else {
                boolean selectionChanged = false;
                long start = selectionHandler.getStart();
                long end = selectionHandler.getEnd();
                if (start >= dataSize) {
                    start = dataSize;
                    selectionChanged = true;
                }
                if (end >= dataSize) {
                    end = dataSize;
                    selectionChanged = true;
                }

                if (selectionChanged) {
                    ((SelectionCapable) codeArea).setSelection(start, end);
                }
            }
        }
    }

    private void recomputeDimensions() {
        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        Insets insets = codeArea.getInsets();
        int componentWidth = codeArea.getWidth() - insets.left - insets.right;
        int componentHeight = codeArea.getHeight() - insets.top - insets.bottom;
        dimensions.recomputeSizes(metrics, insets.right, insets.top, componentWidth, componentHeight, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
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

    public void fontChanged(Graphics g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        font = ((FontCapable) codeArea).getCodeFont();
        metrics.recomputeMetrics(g.getFontMetrics(font), charset);

        recomputeDimensions();
        recomputeCharPositions();
        initialized = true;
    }

    private void recomputeScrollState() {
        scrolling.setScrollPosition(((ScrollingCapable) codeArea).getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();

        if (characterWidth > 0) {
            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

            recomputeCharPositions();
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void paintComponent(Graphics g) {
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

        if (antialiasingMode != AntialiasingMode.OFF && g instanceof Graphics2D) {
            Object antialiasingHint = antialiasingMode.getAntialiasingHint((Graphics2D) g);
            ((Graphics2D) g).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    antialiasingHint);
        }
        if (layoutChanged) {
            recomputeLayout();
            recomputeCharPositions();
        }

        paintOutsideArea(g);
        paintHeader(g);
        paintRowPosition(g);
        paintMainArea(g);
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            rebuildColors();
        }
    }

    public void paintOutsideArea(Graphics g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rectangle componentRect = dimensions.getComponentRectangle();
        int characterWidth = metrics.getCharacterWidth();
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(componentRect.x, componentRect.y, componentRect.width, headerAreaHeight);

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        g.drawLine(componentRect.x, componentRect.y + headerAreaHeight - 1, componentRect.x + rowPositionAreaWidth, componentRect.y + headerAreaHeight - 1);

        int lineX = componentRect.x + rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= componentRect.x) {
            g.drawLine(lineX, componentRect.y, lineX, componentRect.y + headerAreaHeight);
        }
    }

    public void paintHeader(Graphics g) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        Rectangle headerArea = dimensions.getHeaderAreaRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();

        g.setFont(font);
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            int headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            int headerY = headerArea.y + rowHeight - metrics.getSubFontSpace();

            g.setColor(colorsProfile.getTextColor());
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

                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        drawCenteredChars(g, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setColor(color);
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        g.drawLine(headerArea.x, headerArea.y + headerArea.height - 1, headerArea.x + headerArea.width, headerArea.y + headerArea.height - 1);
        int lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, headerArea.y, lineX, headerArea.y + headerArea.height);
        }

        g.setClip(clipBounds);
    }

    public void paintRowPosition(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle rowPosRectangle = dimensions.getRowPositionAreaRectangle();
        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(rowPosRectangle) : rowPosRectangle);

        g.setFont(font);
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(rowPosRectangle.x, rowPosRectangle.y, rowPosRectangle.width, rowPosRectangle.height);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            int stripePositionY = rowPosRectangle.y - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                g.fillRect(rowPosRectangle.x, stripePositionY, rowPosRectangle.width, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        int positionY = rowPosRectangle.y + rowHeight - subFontSpace - scrollPosition.getRowOffset();
        g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, codeCharactersCase);
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangle.x, positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        int lineX = rowPosRectangle.x + rowPosRectangle.width - (characterWidth / 2);
        if (lineX >= rowPosRectangle.x) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }
        g.drawLine(dataViewRectangle.x, dataViewRectangle.y - 1, dataViewRectangle.x + dataViewRectangle.width, dataViewRectangle.y - 1);

        g.setClip(clipBounds);
    }

    @Override
    public void paintMainArea(Graphics g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            fontChanged(g);
            fontChanged = false;
        }

        Rectangle mainAreaRect = dimensions.getMainAreaRectangle();
        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int previewRelativeX = visibility.getPreviewRelativeX();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(mainAreaRect) : mainAreaRect);
        colorAssessor.startPaint(this);
        charAssessor.startPaint(this);

        paintBackground(g);

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        int lineX = dataViewRectangle.x + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
        if (lineX >= dataViewRectangle.x) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }

        paintRows(g);
        g.setClip(clipBounds);
        paintCursor(g);

//        paintDebugInfo(g, mainAreaRect, scrollPosition);
    }

//    // Debugging counter
//    private long paintDebugCounter = 0;
//
//    private void paintDebugInfo(Graphics g, Rectangle mainAreaRect, CodeAreaScrollPosition scrollPosition) {
//        int rowHeight = metrics.getRowHeight();
//        int x = mainAreaRect.x + mainAreaRect.width - 220;
//        int y = mainAreaRect.y + mainAreaRect.height - 20;
//        g.setColor(Color.YELLOW);
//        g.fillRect(x, y, 200, 16);
//        g.setColor(Color.BLACK);
//        char[] headerCode = (String.valueOf(scrollPosition.getCharPosition()) + "+" + String.valueOf(scrollPosition.getCharOffset()) + " : " + String.valueOf(scrollPosition.getRowPosition()) + "+" + String.valueOf(scrollPosition.getRowOffset()) + " P: " + String.valueOf(paintDebugCounter)).toCharArray();
//        g.drawChars(headerCode, 0, headerCode.length, x, y + rowHeight);
//
//        paintDebugCounter++;
//    }
//
    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle dataViewRect = dimensions.getDataViewRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        g.setColor(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.fillRect(dataViewRect.x, dataViewRect.y, dataViewRect.width, dataViewRect.height);
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            int stripePositionY = dataViewRect.y - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                g.fillRect(dataViewRect.x, stripePositionY, dataViewRect.width, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();
        int dataViewY = dimensions.getScrollPanelY();
        int rowsPerRect = dimensions.getRowsPerRect();
        long dataSize = codeArea.getDataSize();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int characterWidth = metrics.getCharacterWidth();
        int rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = dataViewY - scrollPosition.getRowOffset();

        g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
            paintRowText(g, dataPosition, rowPositionX, rowPositionY);

            rowPositionY += rowHeight;
            if (Long.MAX_VALUE - dataPosition < bytesPerRow) {
                dataPosition = Long.MAX_VALUE;
            } else {
                dataPosition += bytesPerRow;
            }
        }
    }

    private void prepareRowData(long dataPosition) {
        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        int previewCharPos = visibility.getPreviewCharPos();
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();

        int rowBytesLimit = bytesPerRow;
        int rowStart = 0;
        if (dataPosition < dataSize) {
            int rowDataSize = bytesPerRow + maxBytesPerChar - 1;
            if (dataSize - dataPosition < rowDataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                rowStart = (int) -dataPosition;
            }
            BinaryData data = codeArea.getContentData();
            data.copyToArray(dataPosition + rowStart, rowDataCache.rowData, rowStart, rowDataSize - rowStart);
            if (dataSize - dataPosition < rowBytesLimit) {
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

                int byteRowPos = structure.computeFirstCodeCharacterPos(byteOnRow);
                if (byteRowPos > 0) {
                    rowDataCache.rowCharacters[byteRowPos - 1] = ' ';
                }
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, byteRowPos, codeCharactersCase);
            }

            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
            }
        }

        if (previewCharPos > 0) {
            rowDataCache.rowCharacters[previewCharPos - 1] = ' ';
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            int skipToPreview = visibility.getSkipToPreview();
            int skipRestFromPreview = visibility.getSkipRestFromPreview();
            int endPreview = Math.min(skipRestFromPreview, rowBytesLimit);
            for (int byteOnRow = skipToPreview; byteOnRow < endPreview; byteOnRow++) {
                rowDataCache.rowCharacters[previewCharPos + byteOnRow] = charAssessor.getPreviewCharacter(dataPosition, byteOnRow, previewCharPos, BasicCodeAreaSection.TEXT_PREVIEW);
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
    public void paintRowBackground(Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
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

            Color color = colorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, rowPositionY);
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
                renderBackgroundSequence(g, renderOffset, charactersPerRow, rowPositionX, rowPositionY);
            }
        }
    }

    @Nonnull
    @Override
    public PositionScrollVisibility computePositionScrollVisibility(CodeAreaCaretPosition caretPosition) {
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
        CodeAreaSection section = caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computePositionScrollVisibility(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaScrollPosition> computeRevealScrollPosition(CodeAreaCaretPosition caretPosition) {
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
        CodeAreaSection section = caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth % characterWidth, dataViewHeight % rowHeight, characterWidth, rowHeight);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaScrollPosition> computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition) {
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
        CodeAreaSection section = caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
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
    public void paintRowText(Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();

        g.setFont(font);
        int positionY = rowPositionY + rowHeight - subFontSpace;

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

            Color color = colorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (color == null) {
                color = colorsProfile.getTextColor();
            }

            boolean sequenceBreak = false;
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                    g.setColor(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charOnRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
                }

                renderColor = color;
                if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                    g.setColor(renderColor);
                    lastColor = renderColor;
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                g.setColor(renderColor);
            }

            drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charactersPerRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
        }
    }

    @Nonnull
    public CodeAreaColorAssessor getColorAssessor() {
        return colorAssessor;
    }

    public void setColorAssessor(CodeAreaColorAssessor colorAssessor) {
        this.colorAssessor = CodeAreaUtils.requireNonNull(colorAssessor);
    }

    @Nonnull
    public CodeAreaCharAssessor getCharAssessor() {
        return charAssessor;
    }

    public void setCharAssessor(CodeAreaCharAssessor charAssessor) {
        this.charAssessor = charAssessor;
    }

    @Override
    public void paintCursor(Graphics g) {
        if (!codeArea.hasFocus()) {
            return;
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

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
        Rectangle cursorRect = getCursorPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect.isEmpty()) {
            return;
        }

        Rectangle scrolledCursorRect = new Rectangle(cursorRect.x, cursorRect.y, cursorRect.width, cursorRect.height);
        Rectangle clipBounds = g.getClipBounds();
        Rectangle intersection = scrolledCursorRect.intersection(mainAreaRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.setClip(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setColor(colorsProfile.getCursorColor());

            paintCursorRect(g, intersection.x, intersection.y, intersection.width, intersection.height, renderingMode, caret);
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            updateMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            Rectangle mirrorCursorRect = cursorDataCache.mirrorCursorRect;
            if (!mirrorCursorRect.isEmpty()) {
                intersection = mainAreaRect.intersection(mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.setClip(intersection);
                    g.setColor(colorsProfile.getCursorColor());
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setStroke(cursorDataCache.dashedStroke);
                    g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
                    g2d.dispose();
                }
            }
        }
        g.setClip(clipBounds);
    }

    private void paintCursorRect(Graphics g, int cursorX, int cursorY, int width, int height, CursorRenderingMode renderingMode, DefaultCodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRect(cursorX, cursorY, width, height);
                break;
            }
            case XOR: {
                g.setXORMode(colorsProfile.getTextBackground());
                g.fillRect(cursorX, cursorY, width, height);
                g.setPaintMode();
                break;
            }
            case NEGATIVE: {
                int characterWidth = metrics.getCharacterWidth();
                int rowHeight = metrics.getRowHeight();
                int maxBytesPerChar = metrics.getMaxBytesPerChar();
                int subFontSpace = metrics.getSubFontSpace();
                int dataViewX = dimensions.getScrollPanelX();
                int dataViewY = dimensions.getScrollPanelY();
                int previewRelativeX = visibility.getPreviewRelativeX();

                CodeAreaViewMode viewMode = structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
                long dataSize = codeArea.getDataSize();
                CodeType codeType = structure.getCodeType();
                g.fillRect(cursorX, cursorY, width, height);
                g.setColor(colorsProfile.getCursorNegativeColor());
                BinaryData contentData = codeArea.getContentData();
                int row = (cursorY + scrollPosition.getRowOffset() - dataViewY) / rowHeight;
                int scrolledX = cursorX + scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
                int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    int charPos = (scrolledX - previewRelativeX) / characterWidth;
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    int byteOnRow = (int) (dataPosition % structure.getBytesPerRow());
                    int previewCharPos = visibility.getPreviewCharPos();

                    if (contentData.isEmpty()) {
                        cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, 0, BasicCodeAreaSection.TEXT_PREVIEW);
                    } else {
                        if (maxBytesPerChar > 1) {
                            int charDataLength = maxBytesPerChar;
                            if (dataPosition + maxBytesPerChar > dataSize) {
                                charDataLength = (int) (dataSize - dataPosition);
                            }

                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, charDataLength, BasicCodeAreaSection.TEXT_PREVIEW);
                        } else {
                            cursorDataCache.cursorData[0] = contentData.getByte(dataPosition);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, 1, BasicCodeAreaSection.TEXT_PREVIEW);
                        }
                    }
                    int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    drawCenteredChars(g, cursorDataCache.cursorChars, 0, 1, characterWidth, posX, posY);
                } else {
                    int charPos = (scrolledX - dataViewX) / characterWidth;
                    int byteOffset = structure.computePositionByte(charPos);
                    int codeCharPos = structure.computeFirstCodeCharacterPos(byteOffset);

                    if (dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, codeCharactersCase);
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
                    int posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChars(g, cursorDataCache.cursorChars, charsOffset, 1, characterWidth, posX + (charsOffset * characterWidth), posY);
                }
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(renderingMode);
        }
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        DefaultCodeAreaCaretPosition caret = new DefaultCodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
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
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
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
        long dataSize = codeArea.getDataSize();
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

        Rectangle dataViewRect = dimensions.getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + row * rowHeight) - scrollPosition.getRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.x + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.x + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new Point(caretX, caretY);
    }

    private void updateMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == BasicCodeAreaSection.CODE_MATRIX ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            cursorDataCache.mirrorCursorRect.setSize(0, 0);
        } else {
            cursorDataCache.mirrorCursorRect.setBounds(mirrorCursorPoint.x, mirrorCursorPoint.y, metrics.getCharacterWidth() * (section == BasicCodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        }
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        int dataViewX = dimensions.getScrollPanelX();
        int dataViewY = dimensions.getScrollPanelY();
        int scrollPanelWidth = dimensions.getScrollPanelWidth();
        int scrollPanelHeight = dimensions.getScrollPanelHeight();
        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
            return Cursor.TEXT_CURSOR;
        }

        return Cursor.DEFAULT_CURSOR;
    }

    @Nonnull
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
    public void setBasicColors(BasicCodeAreaColorsProfile colorsProfile) {
        this.colorsProfile = colorsProfile;
    }

    /**
     * Draws characters centering it to cells of the same width.
     *
     * @param g graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param length number of characters to draw
     * @param cellWidth width of cell to center into
     * @param positionX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChars(Graphics g, char[] drawnChars, int charOffset, int length, int cellWidth, int positionX, int positionY) {
        int pos = 0;
        int group = 0;
        while (pos < length) {
            char drawnChar = drawnChars[charOffset + pos];
            int charWidth = metrics.getCharWidth(drawnChar);

            boolean groupable;
            if (metrics.hasUniformLineMetrics()) {
                groupable = charWidth == cellWidth;
            } else {
                int charsWidth = metrics.getCharsWidth(drawnChars, charOffset + pos - group, group + 1);
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

    protected void drawShiftedChars(Graphics g, char[] drawnChars, int charOffset, int length, int positionX, int positionY) {
        g.drawChars(drawnChars, charOffset, length, positionX, positionY);
    }

    private int getRowPositionLength() {
        if (minRowPositionLength > 0 && minRowPositionLength == maxRowPositionLength) {
            return minRowPositionLength;
        }

        long dataSize = codeArea.getDataSize();
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
     * @return cursor rectangle or empty rectangle
     */
    @Nonnull
    public Rectangle getCursorPositionRect(long dataPosition, int codeOffset, CodeAreaSection section) {
        Rectangle rect = new Rectangle();
        updateRectToCursorPosition(rect, dataPosition, codeOffset, section);
        return rect;
    }

    protected void updateRectToCursorPosition(Rectangle rect, long dataPosition, int codeOffset, CodeAreaSection section) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        Point cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            rect.setBounds(0, 0, 0, 0);
        } else {
            DefaultCodeAreaCaret.CursorShape cursorShape = editOperation == EditOperation.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
            int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
            rect.setBounds(cursorPoint.x, cursorPoint.y, cursorThickness, rowHeight);
        }
    }

    /**
     * Renders sequence of background rectangles.
     * <p>
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Graphics g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        g.fillRect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    @Override
    public void updateScrollBars() {
        int verticalScrollBarPolicy = CodeAreaSwingUtils.getVerticalScrollBarPolicy(scrolling.getVerticalScrollBarVisibility());
        if (scrollPanel.getVerticalScrollBarPolicy() != verticalScrollBarPolicy) {
            scrollPanel.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
        }
        int horizontalScrollBarPolicy = CodeAreaSwingUtils.getHorizontalScrollBarPolicy(scrolling.getHorizontalScrollBarVisibility());
        if (scrollPanel.getHorizontalScrollBarPolicy() != horizontalScrollBarPolicy) {
            scrollPanel.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
        }

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        long rowsPerDocument = structure.getRowsPerDocument();

        recomputeScrollState();

        boolean revalidate = false;
        Rectangle scrollPanelRectangle = dimensions.getScrollPanelRectangle();
        Rectangle oldRect = scrollPanel.getBounds();
        if (!oldRect.equals(scrollPanelRectangle)) {
            scrollPanel.setBounds(scrollPanelRectangle);
            revalidate = true;
        }

        JViewport viewport = scrollPanel.getViewport();

        if (rowHeight > 0 && characterWidth > 0) {
            viewDimension = scrolling.computeViewDimension(viewport.getWidth(), viewport.getHeight(), layout, structure, characterWidth, rowHeight);
            if (dataView.getWidth() != viewDimension.getWidth() || dataView.getHeight() != viewDimension.getHeight()) {
                Dimension dataViewSize = new Dimension(viewDimension.getWidth(), viewDimension.getHeight());
                dataView.setPreferredSize(dataViewSize);
                dataView.setSize(dataViewSize);

                recomputeDimensions();

                scrollPanelRectangle = dimensions.getScrollPanelRectangle();
                if (!oldRect.equals(scrollPanelRectangle)) {
                    scrollPanel.setBounds(scrollPanelRectangle);
                }

                revalidate = true;
            }

            int verticalScrollValue = scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument);
            int horizontalScrollValue = scrolling.getHorizontalScrollValue(characterWidth);
            scrollPanel.updateScrollBars(verticalScrollValue, horizontalScrollValue);
        }

        if (revalidate) {
            horizontalExtentChanged();
            verticalExtentChanged();
            codeArea.revalidate();
        }
    }

    @Override
    public void scrollPositionModified() {
        scrolling.clearLastVerticalScrollingValue();
        recomputeScrollState();
    }

    @Override
    public void scrollPositionChanged() {
        recomputeScrollState();
        updateScrollBars();
    }

    private void horizontalExtentChanged() {
        scrollPanel.horizontalExtentChanged();
    }

    private void verticalExtentChanged() {
        scrollPanel.verticalExtentChanged();
    }

    private void dataChanged() {
        validateCaret();
        validateSelection();
        recomputeLayout();
    }

    @Override
    public int getCharactersPerRow() {
        return structure.getCharactersPerRow();
    }

    @Override
    public int getBytesPerRow() {
        return structure.getBytesPerRow();
    }

    @Nonnull
    @Override
    public CodeAreaSection getActiveSection() {
        return ((CaretCapable) codeArea).getActiveSection();
    }

    @Nonnull
    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public int getMaxBytesPerChar() {
        return metrics.getMaxBytesPerChar();
    }

    @Nonnull
    @Override
    public CodeAreaColorsProfile getColorsProfile() {
        return colorsProfile;
    }

    @Nonnull
    @Override
    public byte[] getRowData() {
        return rowDataCache.rowData;
    }

    @Override
    public int getCodeLastCharPos() {
        return visibility.getCodeLastCharPos();
    }

    @Override
    public long getDataSize() {
        return structure.getDataSize();
    }

    @Nonnull
    @Override
    public BinaryData getContentData() {
        return codeArea.getContentData();
    }

    @Nullable
    @Override
    public CodeAreaSelection getSelectionHandler() {
        return ((SelectionCapable) codeArea).getSelectionHandler();
    }

    public int getRowHeight() {
        return metrics.getRowHeight();
    }

    private int getHorizontalScrollBarSize() {
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        return horizontalScrollBar.isVisible() ? horizontalScrollBar.getHeight() : 0;
    }

    private int getVerticalScrollBarSize() {
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        return verticalScrollBar.isVisible() ? verticalScrollBar.getWidth() : 0;
    }

    protected static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
    }

    protected static class CursorDataCache {

        Rectangle caretRect = new Rectangle();
        Rectangle mirrorCursorRect = new Rectangle();
        final Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }
}
