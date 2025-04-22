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
package org.exbin.bined.swing.section;

import org.exbin.bined.section.SectionCodeAreaStructure;
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
import java.util.HashMap;
import java.util.Map;
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
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.section.caret.DefaultExtendedCodeAreaCaretsProfile;
import org.exbin.bined.section.capability.PositionCodeTypeCapable;
import org.exbin.bined.section.theme.SectionBackgroundPaintMode;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.AntialiasingMode;
import org.exbin.bined.swing.basic.BasicCodeAreaMetrics;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret;
import org.exbin.bined.swing.capability.AntialiasingCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.section.color.ColorsProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.section.color.SectionCodeAreaColorProfile;
import org.exbin.bined.swing.section.layout.DefaultSectionCodeAreaLayoutProfile;
import org.exbin.bined.swing.section.layout.LayoutProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.bined.swing.section.theme.ThemeProfileCapableCodeAreaPainter;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.section.layout.PositionIterator;
import org.exbin.bined.swing.basic.DefaultCodeAreaMouseListener;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.basic.ScrollViewDimension;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.section.SectionHorizontalScrollUnit;
import org.exbin.bined.section.caret.CodeAreaCaretShape;
import org.exbin.bined.section.caret.CodeAreaCaretType;
import org.exbin.bined.section.layout.SpaceType;
import org.exbin.bined.swing.CodeAreaSwingControl;
import org.exbin.bined.swing.section.caret.CaretsProfileCapableCodeAreaPainter;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.swing.section.caret.SectionCodeAreaCaretsProfile;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.swing.CodeAreaCharAssessor;
import org.exbin.bined.swing.CodeAreaColorAssessor;
import org.exbin.bined.swing.CodeAreaPaintState;
import org.exbin.bined.swing.basic.DefaultCodeAreaCharAssessor;
import org.exbin.bined.swing.basic.DefaultCodeAreaColorAssessor;
import org.exbin.bined.swing.capability.CharAssessorPainterCapable;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;

/**
 * Section code area component default painter.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectionCodeAreaPainter implements CodeAreaPainter, ColorsProfileCapableCodeAreaPainter, LayoutProfileCapableCodeAreaPainter, ThemeProfileCapableCodeAreaPainter, CaretsProfileCapableCodeAreaPainter, CodeAreaPaintState, ColorAssessorPainterCapable, CharAssessorPainterCapable {

    @Nonnull
    protected final CodeAreaCore codeArea;
    protected volatile boolean initialized = false;

    protected volatile boolean fontChanged = false;
    protected volatile boolean layoutChanged = true;
    protected volatile boolean caretChanged = true;
    protected volatile boolean resetColors = true;

    @Nonnull
    protected final JComponent dataView;
    @Nonnull
    protected final SectionCodeAreaScrollPane scrollPanel;
    @Nonnull
    protected final DefaultCodeAreaMouseListener codeAreaMouseListener;
    @Nonnull
    protected final ComponentListener codeAreaComponentListener;
    @Nonnull
    protected final DataChangedListener codeAreaDataChangeListener;

    @Nonnull
    protected final BasicCodeAreaMetrics metrics = new BasicCodeAreaMetrics();
    @Nonnull
    protected final SectionCodeAreaStructure structure = new SectionCodeAreaStructure();
    @Nonnull
    protected final SectionCodeAreaScrolling scrolling = new SectionCodeAreaScrolling();
    @Nonnull
    protected final SectionCodeAreaDimensions dimensions = new SectionCodeAreaDimensions();
    @Nonnull
    protected final SectionCodeAreaVisibility visibility = new SectionCodeAreaVisibility();

    @Nonnull
    protected SectionCodeAreaLayoutProfile layoutProfile = new DefaultSectionCodeAreaLayoutProfile();
    @Nonnull
    protected CodeAreaColorsProfile colorsProfile = new SectionCodeAreaColorProfile();
    @Nonnull
    protected SectionCodeAreaThemeProfile themeProfile = new SectionCodeAreaThemeProfile();
    @Nonnull
    protected SectionCodeAreaCaretsProfile caretsProfile = new DefaultExtendedCodeAreaCaretsProfile();

    @Nullable
    protected CodeCharactersCase codeCharactersCase;
    @Nullable
    protected EditOperation editOperation;
    @Nullable
    protected PositionIterator positionIterator;
    protected final ScrollViewDimension viewDimension = new ScrollViewDimension();
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

    protected static final char SPACE_CHAR = ' '; //\u2003

    public SectionCodeAreaPainter(CodeAreaCore codeArea) {
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
        scrollPanel = new SectionCodeAreaScrollPane((CodeAreaSwingControl) codeArea, metrics, structure, scrolling, dimensions);
        scrollPanel.setViewportView(dataView);
        JViewport viewport = scrollPanel.getViewport();
        viewport.setOpaque(false);
        scrolling.setHorizontalExtentChangeListener(() -> horizontalExtentChanged());
        scrolling.setVerticalExtentChangeListener(() -> verticalExtentChanged());

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
        codeAreaDataChangeListener = () -> dataChanged();
        SectionCodeAreaPainter.this.rebuildColors();
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

        int halfCharsPerPage = dimensions.getHalfCharsPerPage();
        structure.updateCache(codeArea, halfCharsPerPage, layoutProfile);
        positionIterator = layoutProfile.createPositionIterator(structure.getCodeType(), structure.getViewMode(), structure.getBytesPerRow());
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();
        minRowPositionLength = ((RowWrappingCapable) codeArea).getMinRowPositionLength();
        maxRowPositionLength = ((RowWrappingCapable) codeArea).getMaxRowPositionLength();
        antialiasingMode = ((AntialiasingCapable) codeArea).getAntialiasingMode();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int halfCharsPerRow = structure.getHalfCharsPerRow();

        if (metrics.isInitialized()) {
            int characterWidth = metrics.getCharacterWidth();
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, halfCharsPerRow, halfCharsPerPage, dimensions.getHalfCharOffset(), dimensions.getRowOffset(), characterWidth);
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
        dimensions.recomputeSizes(metrics, insets.right, insets.top, componentWidth, componentHeight, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize, layoutProfile);
    }

    public void recomputeCharPositions() {
        visibility.recomputeCharPositions(metrics, structure, dimensions, layoutProfile, scrolling);
        updateRowDataCache();
    }

    private void updateRowDataCache() {
        if (rowDataCache == null) {
            rowDataCache = new RowDataCache();
        }

        boolean shifted = layoutProfile.isHalfShiftedUsed();
        int maxRowDataChars = visibility.getMaxRowDataChars();
        int codeLength = structure.getCodeType().getMaxDigitsForByte();
        rowDataCache.headerCodeData = new char[structure.getCodeType().getMaxDigitsForByte()];
        rowDataCache.headerChars = new char[maxRowDataChars + codeLength];
        rowDataCache.headerCharsShifted = shifted ? new char[maxRowDataChars + codeLength] : null;
        rowDataCache.rowCodeData = new char[structure.getCodeType().getMaxDigitsForByte()];
        rowDataCache.rowData = new byte[structure.getBytesPerRow() + metrics.getMaxBytesPerChar() - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[maxRowDataChars];
        rowDataCache.rowCharactersShifted = shifted ? new char[maxRowDataChars] : null;
        rowDataCache.unprintables = new byte[(structure.getBytesPerRow() + 7) >> 3];
    }

    public void fontChanged(Graphics g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        font = ((FontCapable) codeArea).getCodeFont();
        metrics.recomputeMetrics(g.getFontMetrics(font), charset);

        recomputeLayout();
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
        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
        g.fillRect(componentRect.x, componentRect.y, componentRect.width, headerAreaHeight);

        // Decoration lines
        g.setColor(colorsProfile.getColor(BasicCodeAreaDecorationColorType.LINE));
        if (themeProfile.showHeaderLine()) {
            g.drawLine(componentRect.x, componentRect.y + headerAreaHeight - 1, componentRect.x + rowPositionAreaWidth, componentRect.y + headerAreaHeight - 1);
        }

        if (themeProfile.showRowPositionLine()) {
            int lineX = componentRect.x + rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= componentRect.x) {
                g.drawLine(lineX, componentRect.y, lineX, componentRect.y + headerAreaHeight);
            }
        }

        if (themeProfile.showBoxLine()) {
            g.drawLine(rowPositionAreaWidth - 1, headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1);
        }
    }

    public void paintHeader(Graphics g) {
        if (!dimensions.getLayoutProfile().isShowHeader()) {
            return;
        }

        Rectangle headerArea = dimensions.getHeaderAreaRectangle();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();
        int skipRestFrom = visibility.getSkipRestFrom();
        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();

        g.setFont(font);
        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        CodeAreaViewMode viewMode = structure.getViewMode();
        if ((viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) && visibility.isCodeSectionVisible()) {
            int headerX = dataViewX - scrolling.getHorizontalScrollX(characterWidth);
            int headerY = headerArea.y + dimensions.getLayoutProfile().computeHeaderOffsetPositionY() + rowHeight - metrics.getSubFontSpace();

            Arrays.fill(rowDataCache.headerChars, SPACE_CHAR);
            if (layoutProfile.isHalfShiftedUsed()) {
                Arrays.fill(rowDataCache.headerCharsShifted, SPACE_CHAR);
            }

            int codeLength = structure.getCodeType().getMaxDigitsForByte();
            int base = structure.getPositionCodeType().getBase();
            g.setColor(colorsProfile.getColor(CodeAreaBasicColors.ALTERNATE_BACKGROUND));
            positionIterator.reset();
            positionIterator.skip(visibility.getSkipTo());
            int halfCharPos = positionIterator.getHalfCharPosition();
            boolean paintGrid = themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.GRIDDED
                    || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.CHESSBOARD;
            do {
                if (positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }

                int byteOffset = positionIterator.getBytePosition();
                int gridStartX = paintGrid ? layoutProfile.computePositionX(halfCharPos, characterWidth, halfSpaceWidth) : 0;
                int gridEndX = 0;
                CodeAreaUtils.longToBaseCode(rowDataCache.headerCodeData, 0, byteOffset, base, codeLength, true, codeCharactersCase);
                for (int i = positionIterator.getCodeOffset(); i < codeLength; i++) {
                    int charPos = halfCharPos / 2 - skipToChar;
                    if ((halfCharPos & 1) == 0) {
                        rowDataCache.headerChars[charPos] = rowDataCache.headerCodeData[i];
                    } else {
                        rowDataCache.headerCharsShifted[charPos] = rowDataCache.headerCodeData[i];
                    }

                    if (paintGrid && (i + 1 == codeLength)) {
                        gridEndX = layoutProfile.computePositionX(halfCharPos + 2, characterWidth, halfSpaceWidth);
                    }
                    halfCharPos += 2 + positionIterator.nextSpaceType().getHalfCharSize();
                }

                if (paintGrid) {
                    if ((byteOffset & 1) != 0) {
                        g.fillRect(headerX + gridStartX, headerArea.y, gridEndX - gridStartX, headerArea.height);
                    }
                }
                if ((positionIterator.getPosition() >= skipRestFrom && skipRestFrom >= 0) || positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }
            } while (!positionIterator.isEndReached());

            g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR));
            positionIterator.reset();
            positionIterator.skip(visibility.getSkipTo());
            halfCharPos = positionIterator.getHalfCharPosition();

            int renderCharOffset = halfCharPos / 2;
            int renderCharOffsetShifted = renderCharOffset / 2;
            Color renderColor = null;
            Color renderColorShifted = null;
            do {
                for (int i = 0; i < codeLength; i++) {
                    int charPos = halfCharPos / 2;
                    boolean sequenceBreak = false;
                    boolean nonshifted = (halfCharPos & 1) == 0;

                    char currentChar;
                    if (nonshifted) {
                        currentChar = rowDataCache.headerChars[charPos - skipToChar];
                        if (currentChar == ' ' && renderCharOffset == charPos) {
                            renderCharOffset++;
                            continue;
                        }

                        Color color = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
                        if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                            sequenceBreak = true;
                        }
                        if (sequenceBreak) {
                            if (renderCharOffset < charPos) {
                                drawCenteredChars(g, rowDataCache.headerChars, renderCharOffset - skipToChar, charPos - renderCharOffset, characterWidth, headerX + renderCharOffset * characterWidth, headerY);
                            }

                            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                                renderColor = color;
                                g.setColor(color);
                            }

                            renderCharOffset = charPos;
                        }
                    } else {
                        currentChar = rowDataCache.headerCharsShifted[charPos - skipToChar];
                        if (currentChar == ' ' && renderCharOffsetShifted == charPos) {
                            renderCharOffsetShifted++;
                            continue;
                        }

                        Color color = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
                        if (!CodeAreaSwingUtils.areSameColors(color, renderColorShifted)) {
                            sequenceBreak = true;
                        }
                        if (sequenceBreak) {
                            if (renderCharOffsetShifted < charPos) {
                                drawCenteredChars(g, rowDataCache.headerCharsShifted, renderCharOffsetShifted - skipToChar, charPos - renderCharOffsetShifted, characterWidth, headerX + renderCharOffsetShifted * characterWidth + halfSpaceWidth, headerY);
                            }

                            if (!CodeAreaSwingUtils.areSameColors(color, renderColorShifted)) {
                                renderColorShifted = color;
                                g.setColor(color);
                            }

                            renderCharOffsetShifted = charPos;
                        }
                    }
                    halfCharPos += 2 + positionIterator.nextSpaceType().getHalfCharSize();
                }
                if ((positionIterator.getPosition() >= skipRestFrom && skipRestFrom >= 0) || positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }
            } while (!positionIterator.isEndReached());

            if (renderCharOffset < skipRestFromChar) {
                drawCenteredChars(g, rowDataCache.headerChars, renderCharOffset - skipToChar, skipRestFromChar - renderCharOffset, characterWidth, headerX + renderCharOffset * characterWidth, headerY);
            }
            if (layoutProfile.isHalfShiftedUsed() && renderCharOffsetShifted < skipRestFromChar) {
                drawCenteredChars(g, rowDataCache.headerCharsShifted, renderCharOffsetShifted - skipToChar, skipRestFromChar - renderCharOffsetShifted, characterWidth, headerX + renderCharOffsetShifted * characterWidth + halfSpaceWidth, headerY);
            }
        }

        // Decoration lines
        g.setColor(colorsProfile.getColor(BasicCodeAreaDecorationColorType.LINE));

        if (themeProfile.showHeaderLine() || themeProfile.showBoxLine()) {
            g.drawLine(headerArea.x, headerArea.y + headerArea.height - 1, headerArea.x + headerArea.width, headerArea.y + headerArea.height - 1);
        }
        int splitLinePos = visibility.getSplitLinePos();
        if (themeProfile.showSplitLine() && splitLinePos > 0) {
            int lineX = dataViewX + splitLinePos - scrolling.getHorizontalScrollX(characterWidth);
            if (lineX >= dataViewX) {
                g.drawLine(lineX, headerArea.y, lineX, headerArea.y + headerArea.height);
            }
        }
        int groupSize = themeProfile.getVerticalLineByteGroupSize();
        if (groupSize > 0 && (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) && visibility.isCodeSectionVisible()) {
            positionIterator.reset();
            positionIterator.skip(visibility.getSkipTo());
            int halfCharPos = positionIterator.getHalfCharPosition();
            while (!positionIterator.isEndReached()) {
                SpaceType nextSpaceType = positionIterator.nextSpaceType();
                if (positionIterator.isEndReached() || positionIterator.getSection() != BasicCodeAreaSection.CODE_MATRIX) {
                    break;
                }

                int spaceHalfCharSize = nextSpaceType.getHalfCharSize();
                halfCharPos += 2;
                if (positionIterator.getCodeOffset() == 0 && positionIterator.getBytePosition() % groupSize == 0) {
                    int lineX = dataViewX + layoutProfile.computePositionX(halfCharPos, characterWidth, halfSpaceWidth) + (spaceHalfCharSize * halfSpaceWidth) / 2 - scrolling.getHorizontalScrollX(characterWidth);
                    g.drawLine(lineX, headerArea.y, lineX, headerArea.y + headerArea.height);
                }

                halfCharPos += spaceHalfCharSize;

                if ((positionIterator.getPosition() >= skipRestFrom && skipRestFrom >= 0) || positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }
            }
        }

        g.setClip(clipBounds);
    }

    public void paintRowPosition(Graphics g) {
        if (!dimensions.getLayoutProfile().isShowRowPosition()) {
            return;
        }

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
        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
        g.fillRect(rowPosRectangle.x, rowPosRectangle.y, rowPosRectangle.width, rowPosRectangle.height);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (themeProfile.isPaintRowPosBackground()
                && (themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.STRIPED
                || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.GRIDDED
                || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.CHESSBOARD)) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            int stripePositionY = rowPosRectangle.y - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colorsProfile.getColor(CodeAreaBasicColors.ALTERNATE_BACKGROUND));
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
        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR));
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, structure.getPositionCodeType().getBase(), rowPositionLength, true, CodeCharactersCase.UPPER);
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangle.x + dimensions.getLayoutProfile().computeRowPositionOffsetPositionX(), positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        g.setColor(colorsProfile.getColor(BasicCodeAreaDecorationColorType.LINE));
        if (themeProfile.showRowPositionLine()) {
            int lineX = rowPosRectangle.x + rowPosRectangle.width - (characterWidth / 2);
            if (lineX >= rowPosRectangle.x) {
                g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
            }
            g.drawLine(dataViewRectangle.x, dataViewRectangle.y - 1, dataViewRectangle.x + dataViewRectangle.width, dataViewRectangle.y - 1);
        }
        if (themeProfile.showBoxLine()) {
            if (rowPosRectangle.width >= 0) {
                g.drawLine(rowPosRectangle.width - 1, dataViewRectangle.y, rowPosRectangle.width - 1, dataViewRectangle.y + dataViewRectangle.height);
            }
        }

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
        int splitLinePos = visibility.getSplitLinePos();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(mainAreaRect) : mainAreaRect);
        colorAssessor.startPaint(this);
        charAssessor.startPaint(this);

        paintBackground(g);

        int characterWidth = metrics.getCharacterWidth();
        paintRows(g);

        g.setColor(colorsProfile.getColor(BasicCodeAreaDecorationColorType.LINE));
        {
            int lineX = dataViewRectangle.x + splitLinePos - scrolling.getHorizontalScrollX(characterWidth);
            if (themeProfile.showSplitLine() && splitLinePos > 0) {
                g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
            }
        }
        int groupSize = themeProfile.getVerticalLineByteGroupSize();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int skipRestFrom = visibility.getSkipRestFrom();
        if (groupSize > 0 && (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) && visibility.isCodeSectionVisible()) {
            int halfSpaceWidth = characterWidth / 2;
            positionIterator.reset();
            positionIterator.skip(visibility.getSkipTo());
            int halfCharPos = positionIterator.getHalfCharPosition();
            while (!positionIterator.isEndReached()) {
                SpaceType nextSpaceType = positionIterator.nextSpaceType();
                if (positionIterator.isEndReached() || positionIterator.getSection() != BasicCodeAreaSection.CODE_MATRIX) {
                    break;
                }

                int spaceHalfCharSize = nextSpaceType.getHalfCharSize();
                halfCharPos += 2;
                if (positionIterator.getBytePosition() > 0 && positionIterator.getCodeOffset() == 0 && positionIterator.getBytePosition() % groupSize == 0) {
                    int lineX = dataViewRectangle.x + layoutProfile.computePositionX(halfCharPos, characterWidth, halfSpaceWidth) + (spaceHalfCharSize * halfSpaceWidth) / 2 - scrolling.getHorizontalScrollX(characterWidth);
                    g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
                }

                halfCharPos += spaceHalfCharSize;

                if ((positionIterator.getPosition() >= skipRestFrom && skipRestFrom >= 0) || positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }
            }
        }

        g.setClip(clipBounds);
        paintCursor(g);

//        paintDebugInfo(g, mainAreaRect, scrolling.getScrollPosition());
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
    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle dataViewRect = dimensions.getDataViewRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
        if (themeProfile.getBackgroundPaintMode() != SectionBackgroundPaintMode.TRANSPARENT) {
            g.fillRect(dataViewRect.x, dataViewRect.y, dataViewRect.width, dataViewRect.height);
        }

        if (themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.STRIPED
                || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.GRIDDED
                || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.CHESSBOARD) {
            g.setColor(colorsProfile.getColor(CodeAreaBasicColors.ALTERNATE_BACKGROUND));
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int rowAlternatingOffset = (int) (scrollPosition.getRowPosition() & 1);
            int stripePositionY = dataViewRect.y - scrollPosition.getRowOffset();

            for (int row = 0; row <= rowsPerRect; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                boolean oddRow = (row & 1) != rowAlternatingOffset;
                if (oddRow
                        && (themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.STRIPED
                        || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.GRIDDED)) {
                    g.fillRect(dataViewRect.x, stripePositionY, dataViewRect.width, rowHeight);
                }

                if (themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.GRIDDED
                        || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.CHESSBOARD) {
                    positionIterator.reset();
                    positionIterator.skip(visibility.getSkipTo());
                    int gridStartX = 0;
                    int halfCharPos = 0;
                    int bytePosition = 0;
                    boolean first = true;
                    do {
                        if (first || positionIterator.getCodeOffset() == 0) {
                            int nextGridStartX = first ? 0 : layoutProfile.computePositionX(positionIterator.getHalfCharPosition(), characterWidth, halfSpaceWidth);
                            first = false;

                            if ((themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.GRIDDED && (bytePosition & 1) != 0 && !oddRow)
                                    || themeProfile.getBackgroundPaintMode() == SectionBackgroundPaintMode.CHESSBOARD && (bytePosition & 1) != ((row + rowAlternatingOffset) & 1)) {
                                int positionX = dataViewRect.x - scrolling.getHorizontalScrollX(characterWidth) + gridStartX;
                                int width = layoutProfile.computePositionX(halfCharPos, characterWidth, halfSpaceWidth) - gridStartX;
                                g.fillRect(positionX, stripePositionY, width, rowHeight);
                            }
                            gridStartX = nextGridStartX;
                        }

                        if (positionIterator.getPosition() == visibility.getSkipRestFrom() || positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                            break;
                        }

                        halfCharPos = positionIterator.getHalfCharPosition() + 2;
                        bytePosition = positionIterator.getBytePosition();
                        positionIterator.nextSpaceType();
                    } while (!positionIterator.isEndReached());
                }
                stripePositionY += rowHeight;
                dataPosition += bytesPerRow;
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
        int rowPositionX = dataViewX - scrolling.getHorizontalScrollX(characterWidth);
        int rowPositionY = dataViewY - scrollPosition.getRowOffset();

        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR));
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
        CodeType codeType = structure.getCodeType();

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
        }

        Arrays.fill(rowDataCache.rowCharacters, SPACE_CHAR);
        if (layoutProfile.isHalfShiftedUsed()) {
            Arrays.fill(rowDataCache.rowCharactersShifted, SPACE_CHAR);
        }

        positionIterator.reset();
        positionIterator.skip(visibility.getSkipTo());
        char targetChar;
        Character replacement;
        int skipToChar = visibility.getSkipToChar();
        int halfCharPos = positionIterator.getHalfCharPosition();
        boolean first = true;
        int byteOnRow;
        do {
            BasicCodeAreaSection section = positionIterator.getSection();
            if (positionIterator.getPosition() == visibility.getSkipRestFrom()) {
                break;
            }

            byteOnRow = positionIterator.getBytePosition();
            int charPos = halfCharPos / 2 - skipToChar;
            int codeOffset = positionIterator.getCodeOffset();
            byte dataByte = rowDataCache.rowData[byteOnRow];
            if (section == BasicCodeAreaSection.CODE_MATRIX) {
                if (dataPosition + byteOnRow < dataSize) {
//                    if (showUnprintables) {
//                        int charDataLength = maxBytesPerChar;
//                        if (byteOnRow + charDataLength > rowDataCache.rowData.length) {
//                            charDataLength = rowDataCache.rowData.length - byteOnRow;
//                        }
//                        String displayString = new String(rowDataCache.rowData, byteOnRow, charDataLength, charset);
//                        if (!displayString.isEmpty()) {
//                            targetChar = displayString.charAt(0);
//                            replacement = unprintableCharactersMapping.get(targetChar);
//                            if (replacement != null) {
//                                rowDataCache.unprintables[byteOnRow >> 3] |= 1 << (byteOnRow & 7);
//                            }
//                        }
//                    }

                    if (first || codeOffset == 0) {
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCodeData, 0, codeCharactersCase);
                        first = false;
                    }
                    if ((halfCharPos & 1) == 0) {
                        rowDataCache.rowCharacters[charPos] = rowDataCache.rowCodeData[codeOffset];
                    } else {
                        rowDataCache.rowCharactersShifted[charPos] = rowDataCache.rowCodeData[codeOffset];
                    }
                }
            } else {
                if (dataPosition + byteOnRow >= dataSize) {
                    break;
                }

                if (dataPosition + maxBytesPerChar > dataSize) {
                    maxBytesPerChar = (int) (dataSize - dataPosition);
                }

                int charDataLength = maxBytesPerChar;
                if (byteOnRow + charDataLength > rowDataCache.rowData.length) {
                    charDataLength = rowDataCache.rowData.length - byteOnRow;
                }

                targetChar = charAssessor.getPreviewCharacter(dataPosition, byteOnRow, charDataLength, BasicCodeAreaSection.TEXT_PREVIEW);

//                if (showUnprintables) {
//                    replacement = unprintableCharactersMapping.get(targetChar);
//                    if (replacement != null) {
//                        rowDataCache.unprintables[byteOnRow >> 3] |= 1 << (byteOnRow & 7);
//                        targetChar = replacement;
//                    }
//
//                }

                if ((halfCharPos & 1) == 0) {
                    rowDataCache.rowCharacters[charPos] = targetChar;
                } else {
                    rowDataCache.rowCharactersShifted[charPos] = targetChar;
                }
            }
            halfCharPos += 2 + positionIterator.nextSpaceType().getHalfCharSize();
        } while (!positionIterator.isEndReached());
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
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getHalfCharsPerRow();

        Color renderColor = null;
        positionIterator.reset();
        positionIterator.skip(visibility.getSkipTo());
        int renderOffset = positionIterator.getHalfCharPosition();
        int spaceSize = 0;
        int halfCharPos = positionIterator.getHalfCharPosition();
//        boolean unprintable;
        do {
            CodeAreaSection section = positionIterator.getSection();
            if (positionIterator.getPosition() == visibility.getSkipRestFrom()) {
                break;
            }

            int byteOnRow = positionIterator.getBytePosition();
            int charPos = halfCharPos;

            boolean sequenceBreak = false;
//            unprintable = showUnprintables && (rowDataCache.unprintables[byteOnRow >> 3] & (1 << (byteOnRow & 7))) != 0;
            CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
            boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
            Color color = colorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charPos, section, inSelection);
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            boolean splitSpace = viewMode == CodeAreaViewMode.DUAL && positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW && positionIterator.getBytePosition() == 0;
            if (splitSpace) {
                sequenceBreak = true;
                charPos = halfCharPos - spaceSize;
            }
            if (sequenceBreak) {
                if (renderOffset < charPos) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charPos, rowPositionX, rowPositionY);
                    }
                }

                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.setColor(color);
                    }
                }

                renderOffset = charPos;
            }
            if (splitSpace) {
                renderOffset += spaceSize;
            }

            spaceSize = positionIterator.nextSpaceType().getHalfCharSize();
            halfCharPos += 2 + spaceSize;
        } while (!positionIterator.isEndReached());

        if (renderOffset < charactersPerRow) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, charactersPerRow, rowPositionX, rowPositionY);
            }
        }
    }

//    /**
//     * Returns background color for particular code.
//     *
//     * @param rowDataPosition row data position
//     * @param byteOnRow byte on current row
//     * @param halfCharOnRow character on current row
//     * @param section current section
//     * @param unprintable flag for unprintable characters
//     * @return color or null for default color
//     */
//    @Nullable
//    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int halfCharOnRow, CodeAreaSection section, boolean unprintable) {
//        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
//        int codeLastCharPos = structure.getCodeLastHalfCharPos();
//        boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
//        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX)) {
//            if (halfCharOnRow == codeLastCharPos) {
//                inSelection = false;
//            }
//        }
//
//        if (inSelection) {
//            return section == ((CaretCapable) codeArea).getActiveSection() ? colorsProfile.getColor(CodeAreaBasicColors.SELECTION_BACKGROUND) : colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND);
//        }
//
//        if (showUnprintables && unprintable) {
//            return colorsProfile.getColor(CodeAreaUnprintablesColorType.UNPRINTABLES_BACKGROUND, null);
//        }
//
//        return null;
//    }

    @Nonnull
    @Override
    public PositionScrollVisibility computePositionScrollVisibility(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int rowOffset = dimensions.getRowOffset();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getHalfCharsPerPage();

        long dataPosition = caretPosition.getDataPosition();
        long rowPosition = dataPosition / bytesPerRow;
        int halfCharPosition = computeHalfCharPosition(dataPosition, bytesPerRow, caretPosition);

        return scrolling.computePositionScrollVisibility(rowPosition, halfCharPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth, rowOffset, characterWidth, rowHeight);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaScrollPosition> computeRevealScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int rowOffset = dimensions.getRowOffset();
        int rowsPerPage = dimensions.getRowsPerPage();
        int halfCharsPerPage = dimensions.getHalfCharsPerPage();

        long dataPosition = caretPosition.getDataPosition();
        long rowPosition = dataPosition / bytesPerRow;
        int halfCharPosition = computeHalfCharPosition(dataPosition, bytesPerRow, caretPosition);
        int halfCharOffset = dataViewWidth
                % (scrolling.getHorizontalScrollUnit() == SectionHorizontalScrollUnit.HALF_CHARACTER ? characterWidth : characterWidth / 2);

        return scrolling.computeRevealScrollPosition(rowPosition, halfCharPosition, bytesPerRow, rowsPerPage, halfCharsPerPage, halfCharOffset, rowOffset, characterWidth, rowHeight);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaScrollPosition> computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowOffset = dimensions.getRowOffset();
        int rowsPerRect = dimensions.getRowsPerRect();
        int halfCharsPerRect = dimensions.getHalfCharsPerRect();

        long dataPosition = caretPosition.getDataPosition();
        long rowPosition = dataPosition / bytesPerRow;
        int halfCharPosition = computeHalfCharPosition(dataPosition, bytesPerRow, caretPosition);

        return scrolling.computeCenterOnScrollPosition(rowPosition, halfCharPosition, bytesPerRow, rowsPerRect, halfCharsPerRect, dataViewWidth, dataViewHeight, rowOffset, characterWidth, rowHeight);
    }

    private int computeHalfCharPosition(long dataPosition, int bytesPerRow, CodeAreaCaretPosition caretPosition) {
        int byteOffset = (int) (dataPosition % bytesPerRow);
        return structure.computeFirstCodeHalfCharPos(byteOffset, getSection(caretPosition)) + caretPosition.getCodeOffset() * 2;
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
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        int subFontSpace = metrics.getSubFontSpace();
        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();

        g.setFont(font);
        int positionY = rowPositionY + rowHeight - subFontSpace;

        Color lastColor = null;
        Color renderColor = null;
        Color renderColorShifted = null;

//        boolean unprintables = false;
        positionIterator.reset();
        positionIterator.skip(visibility.getSkipTo());
        int halfCharPos = positionIterator.getHalfCharPosition();
        int renderCharOffset = halfCharPos / 2;
        int renderCharOffsetShifted = renderCharOffset;
        char currentChar;
        do {
            CodeAreaSection section = positionIterator.getSection();
            int byteOnRow = positionIterator.getBytePosition();

//            boolean currentUnprintables = false;
//            if (showUnprintables) {
//                currentUnprintables = (rowDataCache.unprintables[byteOffset >> 3] & (1 << (byteOffset & 7))) != 0;
//            }

            int charPos = halfCharPos / 2;

            if ((halfCharPos & 1) == 0) {
                currentChar = rowDataCache.rowCharacters[charPos - skipToChar];

                if (currentChar == SPACE_CHAR && renderCharOffset == charPos) {
                    renderCharOffset++;
                    continue;
                }

                CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
                boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
                Color color = colorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, halfCharPos, section, inSelection); // currentUnprintables
                if (color == null) {
                    color = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
                }

                boolean sequenceBreak = false;
                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                    if (renderColor == null) {
                        renderColor = color;
                    }

                    sequenceBreak = true;
                }

//                if (unprintables != currentUnprintables) {
//                    sequenceBreak = true;
//                }

                if (sequenceBreak) {
                    if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                        g.setColor(renderColor);
                        lastColor = renderColor;
                    }

                    if (charPos > renderCharOffset) {
                        drawCenteredChars(g, rowDataCache.rowCharacters, renderCharOffset - skipToChar, charPos - renderCharOffset, characterWidth, rowPositionX + renderCharOffset * characterWidth, positionY);
                    }

                    renderColor = color;
                    if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                        g.setColor(renderColor);
                        lastColor = renderColor;
                    }

                    renderCharOffset = charPos;
//                    unprintables = currentUnprintables;
                }
            } else {
                currentChar = rowDataCache.rowCharactersShifted[charPos - skipToChar];

                if (currentChar == SPACE_CHAR && renderCharOffsetShifted == charPos) {
                    renderCharOffsetShifted++;
                    continue;
                }

                CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
                boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
                Color color = colorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, halfCharPos, section, inSelection); // currentUnprintables
                if (color == null) {
                    color = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
                }

                boolean sequenceBreak = false;
                if (!CodeAreaSwingUtils.areSameColors(color, renderColorShifted)) {
                    if (renderColorShifted == null) {
                        renderColorShifted = color;
                    }

                    sequenceBreak = true;
                }

//                if (unprintables != currentUnprintables) {
//                    sequenceBreak = true;
//                }

                if (sequenceBreak) {
                    if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColorShifted)) {
                        g.setColor(renderColorShifted);
                        lastColor = renderColorShifted;
                    }

                    if (charPos > renderCharOffsetShifted) {
                        drawCenteredChars(g, rowDataCache.rowCharactersShifted, renderCharOffsetShifted - skipToChar, charPos - renderCharOffsetShifted, characterWidth, rowPositionX + renderCharOffsetShifted * characterWidth + halfSpaceWidth, positionY);
                    }

                    renderColorShifted = color;
                    if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColorShifted)) {
                        g.setColor(renderColorShifted);
                        lastColor = renderColorShifted;
                    }

                    renderCharOffsetShifted = charPos;
//                    unprintables = currentUnprintables;
                }
            }

            halfCharPos += 2 + positionIterator.nextSpaceType().getHalfCharSize();

            if (positionIterator.getPosition() == visibility.getSkipRestFrom()) {
                break;
            }
        } while (!positionIterator.isEndReached());

        if (renderCharOffset < skipRestFromChar) {
            if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                g.setColor(renderColor);
                lastColor = renderColor;
            }

            drawCenteredChars(g, rowDataCache.rowCharacters, renderCharOffset - skipToChar, skipRestFromChar - renderCharOffset, characterWidth, rowPositionX + renderCharOffset * characterWidth, positionY);
        }

        if (layoutProfile.isHalfShiftedUsed() && renderCharOffsetShifted < skipRestFromChar) {
            if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColorShifted)) {
                g.setColor(renderColorShifted);
            }

            drawCenteredChars(g, rowDataCache.rowCharactersShifted, renderCharOffsetShifted - skipToChar, skipRestFromChar - renderCharOffsetShifted, characterWidth, rowPositionX + renderCharOffsetShifted * characterWidth + halfSpaceWidth, positionY);
        }
    }

//    /**
//     * Returns background color for particular code.
//     *
//     * @param rowDataPosition row data position
//     * @param byteOnRow byte on current row
//     * @param halfCharOnRow character on current row
//     * @param section current section
//     * @param unprintable flag for unprintable characters
//     * @return color or null for default color
//     */
//    @Nullable
//    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int halfCharOnRow, CodeAreaSection section, boolean unprintable) {
//        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
//        int codeLastCharPos = structure.getCodeLastHalfCharPos();
//        boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
//        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX)) {
//            if (halfCharOnRow == codeLastCharPos) {
//                inSelection = false;
//            }
//        }
//
//        if (unprintable && section == BasicCodeAreaSection.TEXT_PREVIEW) {
//            return colorsProfile.getColor(CodeAreaUnprintablesColorType.UNPRINTABLES_COLOR, CodeAreaBasicColors.TEXT_COLOR);
//        }
//
//        if (inSelection) {
//            return section == ((CaretCapable) codeArea).getActiveSection() ? colorsProfile.getColor(CodeAreaBasicColors.SELECTION_COLOR) : colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_COLOR);
//        }
//
//        return null;
//    }

    @Nonnull
    @Override
    public CodeAreaColorAssessor getColorAssessor() {
        return colorAssessor;
    }

    @Override
    public void setColorAssessor(CodeAreaColorAssessor colorAssessor) {
        this.colorAssessor = CodeAreaUtils.requireNonNull(colorAssessor);
    }

    @Nonnull
    @Override
    public CodeAreaCharAssessor getCharAssessor() {
        return charAssessor;
    }

    @Override
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
            g.setColor(colorsProfile.getColor(CodeAreaBasicColors.CURSOR_COLOR));

            CodeAreaCaretShape caretShape = caretsProfile.identifyCaretShape(CodeAreaCaretType.INSERT);
            paintCursorRect(g, intersection.x, intersection.y, intersection.width, intersection.height, renderingMode, caret, caretShape);
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
                    g.setColor(colorsProfile.getColor(CodeAreaBasicColors.CURSOR_COLOR));
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setStroke(cursorDataCache.dashedStroke);
                    g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
                    g2d.dispose();
                }
            }
        }
        g.setClip(clipBounds);
    }

    private void paintCursorRect(Graphics g, int cursorX, int cursorY, int width, int height, DefaultCodeAreaCaret.CursorRenderingMode renderingMode, DefaultCodeAreaCaret caret, CodeAreaCaretShape caretShape) {
        switch (renderingMode) {
            case PAINT: {
                caretsProfile.paintCaret(g, cursorX, cursorY, width, height, caretShape);
                break;
            }
            case XOR: {
                g.setXORMode(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
                caretsProfile.paintCaret(g, cursorX, cursorY, width, height, caretShape);
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

                CodeAreaViewMode viewMode = structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
                long dataSize = codeArea.getDataSize();
                CodeType codeType = structure.getCodeType();
                caretsProfile.paintCaret(g, cursorX, cursorY, width, height, caretShape);
                g.setColor(colorsProfile.getColor(CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR));
                BinaryData contentData = codeArea.getContentData();
                int row = (cursorY + scrollPosition.getRowOffset() - dataViewY) / rowHeight;
                // int scrolledX = cursorX + scrolling.getHorizontalScrollX(characterWidth);
                int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                CodeAreaSection section = caret.getSection();
                int codeOffset = caret.getCodeOffset();
                int bytesPerRow = structure.getBytesPerRow();
                int byteOffset = (int) (dataPosition % bytesPerRow);

                int halfCharPos = layoutProfile.computeFirstByteHalfCharPos(byteOffset, section, structure);
                halfCharPos += codeOffset * 2; // TODO
                int relativeX = dataViewX - scrolling.getHorizontalScrollX(characterWidth);
                int posX = relativeX + layoutProfile.computePositionX(halfCharPos, characterWidth, characterWidth / 2);

                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
//                    int charPos = (scrolledX - previewRelativeX) / characterWidth;
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    int byteOnRow = (int) (dataPosition % structure.getBytesPerRow());
                    int previewCharPos = visibility.getPreviewCharPos();

                    if (contentData.isEmpty()) {
                        cursorDataCache.cursorChars[0] = SPACE_CHAR;
                    } else {
                        if (maxBytesPerChar > 1) {
                            int charDataLength = maxBytesPerChar;
                            if (dataPosition + maxBytesPerChar > dataSize) {
                                charDataLength = (int) (dataSize - dataPosition);
                            }

                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, charDataLength, BasicCodeAreaSection.TEXT_PREVIEW);
                            String displayString = new String(cursorDataCache.cursorData, 0, charDataLength, charset);
                            if (!displayString.isEmpty()) {
                                cursorDataCache.cursorChars[0] = displayString.charAt(0);
                            }
                        } else {
                            cursorDataCache.cursorData[0] = contentData.getByte(dataPosition);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, 1, BasicCodeAreaSection.TEXT_PREVIEW);
                        }
                    }
//                    int posX = previewRelativeX + charPos * characterWidth - scrolling.getHorizontalScrollX(characterWidth);
                    drawCenteredChars(g, cursorDataCache.cursorChars, 0, 1, characterWidth, posX, posY);
                } else {
//                    int charPos = (scrolledX - dataViewX) / characterWidth;
//                    int byteOffset = structure.computePositionByte(charPos);
//                    int codeCharPos = structure.computeFirstCodeHalfCharPos(byteOffset);

                    if (dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, codeCharactersCase);
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, SPACE_CHAR);
                    }
//                    int posX = dataViewX + codeCharPos * characterWidth - scrolling.getHorizontalScrollX(characterWidth);
//                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChars(g, cursorDataCache.cursorChars, codeOffset, 1, characterWidth, posX, posY);
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
        int halfSpaceWidth = characterWidth / 2;
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                diffX = characterWidth;
            }
            positionX = rowPositionAreaWidth;
        }
        int cursorX = (positionX - rowPositionAreaWidth) + scrolling.getHorizontalScrollX(characterWidth) - diffX;
        int halfCharPosX = 0;
        int codeOffset = 0;
        int byteOnRow = 0;
        CodeAreaSection section = null;
        positionIterator.reset();
        do {
            codeOffset = positionIterator.getCodeOffset();
            byteOnRow = positionIterator.getBytePosition();
            section = positionIterator.getSection();
            int nextSpaceSize = positionIterator.nextSpaceType().getHalfCharSize();
            int posX = layoutProfile.computePositionX(halfCharPosX + 2 + nextSpaceSize / 2, characterWidth, halfSpaceWidth);
            if (cursorX < posX) {
                break;
            }
            halfCharPosX += 2 + nextSpaceSize;
        } while (!positionIterator.isEndReached());

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

        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        long dataPosition = byteOnRow + (cursorRowY * bytesPerRow);

        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        caret.setSection(section);
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
        int halfCharPos = layoutProfile.computeFirstByteHalfCharPos(byteOffset, section, structure);
        halfCharPos += codeOffset * 2;
        caretX = dataViewRect.x + layoutProfile.computePositionX(halfCharPos, characterWidth, characterWidth / 2);
        caretX -= scrolling.getHorizontalScrollX(characterWidth);

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
    public CodeAreaColorsProfile getColorsProfile() {
        return colorsProfile;
    }

    @Override
    public void setColorsProfile(CodeAreaColorsProfile colorsProfile) {
        this.colorsProfile = colorsProfile;
        codeArea.repaint();
    }

    @Nonnull
    @Override
    public SectionCodeAreaLayoutProfile getLayoutProfile() {
        return layoutProfile.createCopy();
    }

    @Override
    public void setLayoutProfile(SectionCodeAreaLayoutProfile layoutProfile) {
        this.layoutProfile = layoutProfile.createCopy();
        resetLayout();
    }

    @Nonnull
    @Override
    public SectionCodeAreaThemeProfile getThemeProfile() {
        return themeProfile.createCopy();
    }

    @Override
    public void setThemeProfile(SectionCodeAreaThemeProfile themeProfile) {
        this.themeProfile = themeProfile.createCopy();
        codeArea.repaint();
    }

    @Nonnull
    @Override
    public SectionCodeAreaCaretsProfile getCaretsProfile() {
        return caretsProfile;
    }

    @Override
    public void setCaretsProfile(SectionCodeAreaCaretsProfile caretsProfile) {
        this.caretsProfile = caretsProfile;
        codeArea.repaint();
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

        PositionCodeType positionCodeType = ((PositionCodeTypeCapable) codeArea).getPositionCodeType();
        long dataSize = codeArea.getDataSize();
        if (dataSize == 0) {
            return 1;
        }

        double natLog = Math.log(dataSize == Long.MAX_VALUE ? dataSize : dataSize + 1);
        int positionLength = (int) Math.ceil(natLog / positionCodeType.getBaseLog());
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
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Graphics g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceSize = characterWidth / 2;
        int rowHeight = metrics.getRowHeight();
        g.fillRect(
                rowPositionX + layoutProfile.computePositionX(startOffset, characterWidth, halfSpaceSize), positionY,
                layoutProfile.computePositionX(endOffset - startOffset, characterWidth, halfSpaceSize), rowHeight
        );
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
            scrolling.computeViewDimension(viewDimension, viewport.getWidth(), viewport.getHeight(), layoutProfile, structure, characterWidth, rowHeight);
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
        return structure.getHalfCharsPerRow();
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

    @Nonnull
    private CodeAreaSection getSection(CodeAreaCaretPosition caretPosition) {
        return caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
    }

    protected static class RowDataCache {

        char[] headerCodeData;
        char[] headerChars;
        char[] headerCharsShifted;
        char[] rowCodeData;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
        char[] rowCharactersShifted;
        byte[] unprintables;
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
