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
package org.exbin.bined.swing.extended;

import org.exbin.bined.extended.ExtendedCodeAreaStructure;
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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.extended.capability.CodeAreaCaretsProfile;
import org.exbin.bined.extended.capability.PositionCodeTypeCapable;
import org.exbin.bined.extended.capability.ShowUnprintablesCapable;
import org.exbin.bined.extended.color.CodeAreaUnprintablesColorType;
import org.exbin.bined.extended.theme.ExtendedBackgroundPaintMode;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.AntialiasingMode;
import org.exbin.bined.swing.basic.BasicCodeAreaMetrics;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret;
import org.exbin.bined.swing.capability.AntialiasingCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.extended.color.ColorsProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.extended.color.ExtendedCodeAreaColorProfile;
import org.exbin.bined.swing.extended.layout.DefaultExtendedCodeAreaLayoutProfile;
import org.exbin.bined.swing.extended.layout.LayoutProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.bined.swing.extended.theme.ThemeProfileCapableCodeAreaPainter;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.bined.extended.layout.PositionIterator;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.swing.basic.DefaultCodeAreaMouseListener;

/**
 * Extended code area component default painter.
 *
 * @version 0.2.0 2019/02/18
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaPainter implements CodeAreaPainter, ColorsProfileCapableCodeAreaPainter, LayoutProfileCapableCodeAreaPainter, ThemeProfileCapableCodeAreaPainter {

    @Nonnull
    protected final CodeAreaCore codeArea;
    private volatile boolean initialized = false;
    private volatile boolean adjusting = false;

    private volatile boolean fontChanged = false;
    private volatile boolean layoutChanged = true;
    private volatile boolean caretChanged = true;
    private volatile boolean resetColors = true;

    @Nonnull
    private final JPanel dataView;
    @Nonnull
    private final JScrollPane scrollPanel;

    @Nonnull
    private final BasicCodeAreaMetrics metrics = new BasicCodeAreaMetrics();
    @Nonnull
    private final ExtendedCodeAreaStructure structure = new ExtendedCodeAreaStructure();
    @Nonnull
    private final ExtendedCodeAreaScrolling scrolling = new ExtendedCodeAreaScrolling();
    @Nonnull
    private final ExtendedCodeAreaDimensions dimensions = new ExtendedCodeAreaDimensions();
    @Nonnull
    private final ExtendedCodeAreaVisibility visibility = new ExtendedCodeAreaVisibility();
    @Nonnull
    private volatile ScrollingState scrollingState = ScrollingState.NO_SCROLLING;

    @Nonnull
    private ExtendedCodeAreaLayoutProfile layoutProfile = new DefaultExtendedCodeAreaLayoutProfile();
    @Nonnull
    private CodeAreaColorsProfile colorsProfile = new ExtendedCodeAreaColorProfile();
    @Nonnull
    private ExtendedCodeAreaThemeProfile themeProfile = new ExtendedCodeAreaThemeProfile();
    @Nonnull
    private CodeAreaCaretsProfile caretsProfile = new CodeAreaCaretsProfile();

    @Nullable
    private CodeCharactersCase codeCharactersCase;
    @Nullable
    private EditationOperation editationOperation;
    private PositionIterator positionIterator;
    private boolean showMirrorCursor;
    private boolean showUnprintables;
    @Nonnull
    private AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;

    private int rowPositionLength;
    private int minRowPositionLength;
    private int maxRowPositionLength;

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

    protected Map<Character, Character> unprintableCharactersMapping = null;

    // Debuging counter
//    private long paintCounter = 0;
    public ExtendedCodeAreaPainter(CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        codeArea.addDataChangedListener(this::recomputeLayout);

        dataView = new JPanel();
        dataView.setBorder(null);
        dataView.setVisible(false);
        dataView.setLayout(null);
        dataView.setOpaque(false);
        dataView.setInheritsPopupMenu(true);
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
        scrollPanel.setInheritsPopupMenu(true);
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

    private void recomputeLayout() {
        rowPositionLength = getRowPositionLength();
        recomputeDimensions();

        int halfCharsPerPage = dimensions.getHalfCharsPerPage();
        structure.updateCache(codeArea, halfCharsPerPage, layoutProfile);
        positionIterator = layoutProfile.createPositionIterator(structure.getCodeType(), structure.getViewMode(), structure.getBytesPerRow());
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        showUnprintables = ((ShowUnprintablesCapable) codeArea).isShowUnprintables();
        minRowPositionLength = ((RowWrappingCapable) codeArea).getMinRowPositionLength();
        maxRowPositionLength = ((RowWrappingCapable) codeArea).getMaxRowPositionLength();
        antialiasingMode = ((AntialiasingCapable) codeArea).getAntialiasingMode();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int halfCharsPerRow = structure.getHalfCharsPerRow();

        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, halfCharsPerRow, halfCharsPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        recomputeScrollState();
        layoutChanged = false;
    }

    private void updateCaret() {
        editationOperation = ((EditationModeCapable) codeArea).getEditationOperation();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();

        caretChanged = false;
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
        int maxCodeSectionChars = (structure.getHalfCharsPerCodeSection() >> 1) + 1;
        maxCodeSectionChars *= 2;
        rowDataCache.headerCodeData = new char[structure.getCodeType().getMaxDigitsForByte()];
        rowDataCache.headerChars = new char[maxCodeSectionChars];
        if (shifted) {
            rowDataCache.headerCharsShifted = new char[maxCodeSectionChars];
        }
        rowDataCache.rowCodeData = new char[structure.getCodeType().getMaxDigitsForByte()];
        rowDataCache.rowData = new byte[structure.getBytesPerRow() + metrics.getMaxBytesPerChar() - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        int maxRowDataChars = (structure.getHalfCharsPerRow() >> 1) + 1;
        maxRowDataChars *= 2;
        rowDataCache.rowCharacters = new char[maxRowDataChars];
        if (shifted) {
            rowDataCache.rowCharactersShifted = new char[maxRowDataChars];
        }
        rowDataCache.unprintables = new byte[(structure.getBytesPerRow() + 7) >> 3];
    }

    public void fontChanged(Graphics g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        font = ((FontCapable) codeArea).getCodeFont();
        metrics.recomputeMetrics(g.getFontMetrics(font), charset);

        recomputeDimensions();
        recomputeLayout();
        recomputeCharPositions();
        initialized = true;
    }

    public void dataViewScrolled(Graphics g) {
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
            int documentDataWidth = layoutProfile.computePositionX(structure.getHalfCharsPerRow(), characterWidth, characterWidth / 2);
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

            dataView.setPreferredSize(new Dimension(documentDataWidth, documentDataHeight));
        }

        // TODO on resize only
        scrollPanel.setBounds(dimensions.getScrollPanelRectangle());
        scrollPanel.revalidate();
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
            ((FontCapable) codeArea).setCodeFont(codeArea.getFont());
            fontChanged(g);
        }
        if (rowDataCache == null) {
            return;
        }
        if (scrollingState == ScrollingState.SCROLLING_BY_SCROLLBAR) {
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
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintRowPosition(g);
        paintMainArea(g);
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            colorsProfile.reinitialize();
        }
    }

    public void paintOutsiteArea(Graphics g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rectangle componentRect = dimensions.getComponentRect();
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

        int charactersPerCodeSection = structure.getHalfCharsPerCodeSection();
        Rectangle headerArea = dimensions.getHeaderAreaRectangle();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getDataViewX();

        g.setFont(font);
        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        CodeAreaViewMode viewMode = structure.getViewMode();
        if ((viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) && visibility.isCodeSectionVisible()) {
            int headerX = dataViewX - scrolling.getHorizontalScrollX(characterWidth);
            int headerY = headerArea.y + dimensions.getLayoutProfile().computeHeaderOffsetPositionY() + rowHeight - metrics.getSubFontSpace();

            Arrays.fill(rowDataCache.headerChars, ' ');
            if (layoutProfile.isHalfShiftedUsed()) {
                Arrays.fill(rowDataCache.headerCharsShifted, ' ');
            }

            int codeLength = structure.getCodeType().getMaxDigitsForByte();
            int base = structure.getPositionCodeType().getBase();
            g.setColor(colorsProfile.getColor(CodeAreaBasicColors.ALTERNATE_BACKGROUND));
            positionIterator.reset();
            positionIterator.skip(visibility.getSkipTo());
            int halfCharPos = positionIterator.getHalfCharPosition();
            boolean paintGrid = themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.GRIDDED
                    || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.CHESSBOARD;
            do {
                if (positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }

                int byteOffset = positionIterator.getBytePosition();
                int gridStartX = paintGrid ? layoutProfile.computePositionX(halfCharPos, characterWidth, halfSpaceWidth) : 0;
                int gridEndX = 0;
                CodeAreaUtils.longToBaseCode(rowDataCache.headerCodeData, 0, byteOffset, base, codeLength, true, codeCharactersCase);
                for (int i = positionIterator.getCodeOffset(); i < codeLength; i++) {
                    int charPos = halfCharPos >> 1;
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
                if (positionIterator.getPosition() == visibility.getSkipRestFrom() || positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }
            } while (!positionIterator.isEndReached());

            g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR));
            positionIterator.reset();
            positionIterator.skip(visibility.getSkipTo());
            halfCharPos = positionIterator.getHalfCharPosition();

            int renderOffset = halfCharPos >> 1;
            int renderOffsetShifted = renderOffset >> 1;
            Color renderColor = null;
            Color renderColorShifted = null;
            do {
                for (int i = 0; i < codeLength; i++) {
                    int charPos = halfCharPos >> 1;
                    boolean sequenceBreak = false;
                    boolean nonshifted = (halfCharPos & 1) == 0;

                    char currentChar;
                    if (nonshifted) {
                        currentChar = rowDataCache.headerChars[charPos];
                        if (currentChar == ' ' && renderOffset == charPos) {
                            renderOffset++;
                            continue;
                        }

                        Color color = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
                        if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                            sequenceBreak = true;
                        }
                        if (sequenceBreak) {
                            if (renderOffset < charPos) {
                                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charPos - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                            }

                            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                                renderColor = color;
                                g.setColor(color);
                            }

                            renderOffset = charPos;
                        }
                    } else {
                        currentChar = rowDataCache.headerCharsShifted[charPos];
                        if (currentChar == ' ' && renderOffsetShifted == charPos) {
                            renderOffsetShifted++;
                            continue;
                        }

                        Color color = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
                        if (!CodeAreaSwingUtils.areSameColors(color, renderColorShifted)) {
                            sequenceBreak = true;
                        }
                        if (sequenceBreak) {
                            if (renderOffsetShifted < charPos) {
                                drawCenteredChars(g, rowDataCache.headerCharsShifted, renderOffsetShifted, charPos - renderOffsetShifted, characterWidth, headerX + renderOffsetShifted * characterWidth + halfSpaceWidth, headerY);
                            }

                            if (!CodeAreaSwingUtils.areSameColors(color, renderColorShifted)) {
                                renderColorShifted = color;
                                g.setColor(color);
                            }

                            renderOffsetShifted = charPos;
                        }
                    }
                    halfCharPos += 2 + positionIterator.nextSpaceType().getHalfCharSize();
                }
                if (positionIterator.getPosition() == visibility.getSkipRestFrom() || positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    break;
                }
            } while (!positionIterator.isEndReached());

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
            if (layoutProfile.isHalfShiftedUsed() && renderOffsetShifted < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerCharsShifted, renderOffsetShifted, charactersPerCodeSection - renderOffsetShifted, characterWidth, headerX + renderOffsetShifted * characterWidth + halfSpaceWidth, headerY);
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

        g.setClip(clipBounds);
    }

    public void paintRowPosition(Graphics g) {
        if (!dimensions.getLayoutProfile().isShowRowPosition()) {
            return;
        }

        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
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
                && (themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.STRIPED
                || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.GRIDDED
                || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.CHESSBOARD)) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
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

        Rectangle mainAreaRect = dimensions.getMainAreaRect();
        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        int splitLinePos = visibility.getSplitLinePos();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(mainAreaRect) : mainAreaRect);
        paintBackground(g);

        int characterWidth = metrics.getCharacterWidth();
        g.setColor(colorsProfile.getColor(BasicCodeAreaDecorationColorType.LINE));
        int lineX = dataViewRectangle.x + splitLinePos - scrolling.getHorizontalScrollX(characterWidth);
        if (themeProfile.showSplitLine() && splitLinePos > 0) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }

        paintRows(g);
        g.setClip(clipBounds);
        paintCursor(g);

//        {
//            // Display debugging data
//            int rowHeight = metrics.getRowHeight();
//            int x = mainAreaRect.x + mainAreaRect.width - 220;
//            int y = mainAreaRect.y + mainAreaRect.height - 20;
//            g.setColor(Color.YELLOW);
//            g.fillRect(x, y, 200, 16);
//            g.setColor(Color.BLACK);
//            char[] headerCode = (String.valueOf(scrollPosition.getCharPosition()) + "+" + String.valueOf(scrollPosition.getCharOffset()) + " : " + String.valueOf(scrollPosition.getRowPosition()) + "+" + String.valueOf(scrollPosition.getRowOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
//            g.drawChars(headerCode, 0, headerCode.length, x, y + rowHeight);
//        }
//
//        paintCounter++;
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle dataViewRect = dimensions.getDataViewRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        g.setColor(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
        if (themeProfile.getBackgroundPaintMode() != ExtendedBackgroundPaintMode.TRANSPARENT) {
            g.fillRect(dataViewRect.x, dataViewRect.y, dataViewRect.width, dataViewRect.height);
        }

        if (themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.STRIPED
                || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.GRIDDED
                || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.CHESSBOARD) {
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
                        && (themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.STRIPED
                        || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.GRIDDED)) {
                    g.fillRect(dataViewRect.x, stripePositionY, dataViewRect.width, rowHeight);
                }

                if (themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.GRIDDED
                        || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.CHESSBOARD) {
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

                            if ((themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.GRIDDED && (bytePosition & 1) != 0 && !oddRow)
                                    || themeProfile.getBackgroundPaintMode() == ExtendedBackgroundPaintMode.CHESSBOARD && (bytePosition & 1) != ((row + rowAlternatingOffset) & 1)) {
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
        int dataViewX = dimensions.getDataViewX();
        int dataViewY = dimensions.getDataViewY();
        int rowsPerRect = dimensions.getRowsPerRect();
        long dataSize = structure.getDataSize();
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
            dataPosition += bytesPerRow;
        }
    }

    private void prepareRowData(long dataPosition) {
        int maxBytesPerChar = metrics.getMaxBytesPerChar();
//        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
//        int previewCharPos = structure.getPreviewHalfCharPos();
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

        if (rowBytesLimit < bytesPerRow) {
            Arrays.fill(rowDataCache.rowCharacters, ' ');
            if (layoutProfile.isHalfShiftedUsed()) {
                Arrays.fill(rowDataCache.rowCharactersShifted, ' ');
            }
        }

        if (showUnprintables) {
            Arrays.fill(rowDataCache.unprintables, (byte) 0);

            if (unprintableCharactersMapping == null) {
                buildUnprintableCharactersMapping();
            }
        }

        positionIterator.reset();
        positionIterator.skip(visibility.getSkipTo());
        char targetChar;
        Character replacement;
        int halfCharPos = positionIterator.getHalfCharPosition();
        boolean first = true;
        do {
            BasicCodeAreaSection section = positionIterator.getSection();
            if (positionIterator.getPosition() == visibility.getSkipRestFrom()) {
                break;
            }

            int byteOffset = positionIterator.getBytePosition();
            if (dataPosition > dataSize - byteOffset) {
                break;
            }

            int charPos = halfCharPos >> 1;
            int codeOffset = positionIterator.getCodeOffset();
            byte dataByte = rowDataCache.rowData[byteOffset];
            if (section == BasicCodeAreaSection.CODE_MATRIX) {
                if (dataPosition + byteOffset < dataSize) {
                    if (showUnprintables) {
                        int charDataLength = maxBytesPerChar;
                        if (byteOffset + charDataLength > rowDataCache.rowData.length) {
                            charDataLength = rowDataCache.rowData.length - byteOffset;
                        }
                        String displayString = new String(rowDataCache.rowData, byteOffset, charDataLength, charset);
                        if (!displayString.isEmpty()) {
                            targetChar = displayString.charAt(0);
                            replacement = unprintableCharactersMapping.get(targetChar);
                            if (replacement != null) {
                                rowDataCache.unprintables[byteOffset >> 3] |= 1 << (byteOffset & 7);
                            }
                        }
                    }

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
                if (dataPosition + byteOffset > dataSize) {
                    break;
                }

                if (maxBytesPerChar > 1) {
                    if (dataPosition + maxBytesPerChar > dataSize) {
                        maxBytesPerChar = (int) (dataSize - dataPosition);
                    }

                    int charDataLength = maxBytesPerChar;
                    if (byteOffset + charDataLength > rowDataCache.rowData.length) {
                        charDataLength = rowDataCache.rowData.length - byteOffset;
                    }
                    String displayString = new String(rowDataCache.rowData, byteOffset, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        targetChar = displayString.charAt(0);
                        if (showUnprintables) {
                            replacement = unprintableCharactersMapping.get(targetChar);
                            if (replacement != null) {
                                rowDataCache.unprintables[byteOffset >> 3] |= 1 << (byteOffset & 7);
                                targetChar = replacement;
                            }

                        }

                        if ((halfCharPos & 1) == 0) {
                            rowDataCache.rowCharacters[charPos] = targetChar;
                        } else {
                            rowDataCache.rowCharactersShifted[charPos] = targetChar;
                        }
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        buildCharMapping(charset);
                    }

                    targetChar = charMapping[dataByte & 0xFF];
                    if (showUnprintables) {
                        if (unprintableCharactersMapping == null) {
                            buildUnprintableCharactersMapping();
                        }
                        replacement = unprintableCharactersMapping.get(targetChar);
                        if (replacement != null) {
                            rowDataCache.unprintables[byteOffset >> 3] |= 1 << (byteOffset & 7);
                            targetChar = replacement;
                        }

                    }

                    if ((halfCharPos & 1) == 0) {
                        rowDataCache.rowCharacters[charPos + byteOffset] = targetChar;
                    } else {
                        rowDataCache.rowCharactersShifted[charPos + byteOffset] = targetChar;
                    }
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
//        int previewCharPos = structure.getPreviewHalfCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getHalfCharsPerRow();

        Color renderColor = null;
        positionIterator.reset();
        positionIterator.skip(visibility.getSkipTo());
        int renderOffset = positionIterator.getHalfCharPosition();
        int spaceSize = 0;
        int halfCharPos = positionIterator.getHalfCharPosition();
        boolean unprintable;
        do {
            CodeAreaSection section = positionIterator.getSection();
            if (positionIterator.getPosition() == visibility.getSkipRestFrom()) {
                break;
            }

            int byteOnRow = positionIterator.getBytePosition();
            int charPos = halfCharPos;

            boolean sequenceBreak = false;
            unprintable = showUnprintables && (rowDataCache.unprintables[byteOnRow >> 3] & (1 << (byteOnRow & 7))) != 0;
            Color color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charPos, section, unprintable);
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

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @param unprintable flag for unprintable characters
     * @return color or null for default color
     */
    @Nullable
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintable) {
        SelectionRange selectionRange = structure.getSelectionRange();
        int codeLastCharPos = structure.getCodeLastHalfCharPos();
        CaretPosition caretPosition = ((CaretCapable) codeArea).getCaret().getCaretPosition();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX)) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == caretPosition.getSection() ? colorsProfile.getColor(CodeAreaBasicColors.SELECTION_BACKGROUND) : colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND);
        }

        if (showUnprintables && unprintable) {
            return colorsProfile.getColor(CodeAreaUnprintablesColorType.UNPRINTABLES_BACKGROUND, null);
        }

        return null;
    }

    @Nullable
    @Override
    public PositionScrollVisibility computePositionScrollVisibility(CaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
//        int previewCharPos = structure.getPreviewHalfCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getHalfCharsPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = 0; // TODO previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeHalfCharPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computePositionScrollVisibility(rowPosition, charPosition, bytesPerRow, 0 /* TODO previewCharPos */, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Nullable
    @Override
    public CodeAreaScrollPosition computeRevealScrollPosition(CaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
//        int previewCharPos = structure.getPreviewHalfCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getHalfCharsPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = 0; // TODO previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeHalfCharPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, 0 /* TODO previewCharPos */, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
//        int previewCharPos = structure.getPreviewHalfCharPos();
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
            charPosition = 0; // TODO previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeHalfCharPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeCenterOnScrollPosition(rowPosition, charPosition, bytesPerRow, 0 /* TODO previewCharPos */, rowsPerRect, charactersPerRect, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
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
//        int previewCharPos = structure.getPreviewHalfCharPos();
        int charactersPerRow = structure.getHalfCharsPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        int subFontSpace = metrics.getSubFontSpace();

        g.setFont(font);
        int positionY = rowPositionY + rowHeight - subFontSpace;

        Color lastColor = null;
        Color renderColor = null;
        Color lastColorShifted = null;
        Color renderColorShifted = null;

        boolean unprintables = false;
        int codeLength = structure.getCodeType().getMaxDigitsForByte();
        int base = structure.getCodeType().getBase();
        positionIterator.reset();
        positionIterator.skip(visibility.getSkipTo());
        int halfCharPos = positionIterator.getHalfCharPosition();
        int renderOffset = halfCharPos >> 1;
        int renderOffsetShifted = renderOffset;
        char currentChar = ' ';
        do {
            CodeAreaSection section = positionIterator.getSection();
            int byteOffset = positionIterator.getBytePosition();

            boolean currentUnprintables = false;
            if (showUnprintables) {
                currentUnprintables = (rowDataCache.unprintables[byteOffset >> 3] & (1 << (byteOffset & 7))) != 0;
            }

            int charPos = halfCharPos >> 1;

            if ((halfCharPos & 1) == 0) {
                currentChar = rowDataCache.rowCharacters[charPos];

                if (currentChar == ' ' && renderOffset == charPos) {
                    renderOffset++;
                    continue;
                }

                Color color = getPositionTextColor(rowDataPosition, byteOffset, charPos, section, currentUnprintables);
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

                if (unprintables != currentUnprintables) {
                    sequenceBreak = true;
                }

                if (sequenceBreak) {
                    if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                        g.setColor(renderColor);
                        lastColor = renderColor;
                    }

                    if (charPos > renderOffset) {
                        drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charPos - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
                    }

                    renderColor = color;
                    if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                        g.setColor(renderColor);
                        lastColor = renderColor;
                    }

                    renderOffset = charPos;
                    unprintables = currentUnprintables;
                }
            } else {
                currentChar = rowDataCache.rowCharactersShifted[charPos];

                if (currentChar == ' ' && renderOffsetShifted == charPos) {
                    renderOffsetShifted++;
                    continue;
                }

                Color color = getPositionTextColor(rowDataPosition, byteOffset, charPos, section, currentUnprintables);
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

                if (unprintables != currentUnprintables) {
                    sequenceBreak = true;
                }

                if (sequenceBreak) {
                    if (!CodeAreaSwingUtils.areSameColors(lastColorShifted, renderColorShifted)) {
                        g.setColor(renderColorShifted);
                        lastColorShifted = renderColorShifted;
                    }

                    if (charPos > renderOffset) {
                        drawCenteredChars(g, rowDataCache.rowCharactersShifted, renderOffsetShifted, charPos - renderOffsetShifted, characterWidth, rowPositionX + renderOffsetShifted * characterWidth + halfSpaceWidth, positionY);
                    }

                    renderColorShifted = color;
                    if (!CodeAreaSwingUtils.areSameColors(lastColorShifted, renderColorShifted)) {
                        g.setColor(renderColorShifted);
                        lastColorShifted = renderColorShifted;
                    }

                    renderOffsetShifted = charPos;
                    unprintables = currentUnprintables;
                }
            }

            halfCharPos += 2 + positionIterator.nextSpaceType().getHalfCharSize();

            if (positionIterator.getPosition() == visibility.getSkipRestFrom()) {
                break;
            }
        } while (!positionIterator.isEndReached());

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                g.setColor(renderColor);
            }

            drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charactersPerRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
        }

        if (layoutProfile.isHalfShiftedUsed() && renderOffsetShifted < charactersPerRow) {
            if (!CodeAreaSwingUtils.areSameColors(lastColorShifted, renderColorShifted)) {
                g.setColor(renderColorShifted);
            }

            drawCenteredChars(g, rowDataCache.rowCharactersShifted, renderOffsetShifted, charactersPerRow - renderOffsetShifted, characterWidth, rowPositionX + renderOffsetShifted * characterWidth + halfSpaceWidth, positionY);
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param codeOffset character on current row
     * @param section current section
     * @param unprintable flag for unprintable characters
     * @return color or null for default color
     */
    @Nullable
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int codeOffset, CodeAreaSection section, boolean unprintable) {
        SelectionRange selectionRange = structure.getSelectionRange();
        CaretPosition caretPosition = ((CaretCapable) codeArea).getCaret().getCaretPosition();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);

        if (unprintable && section == BasicCodeAreaSection.TEXT_PREVIEW) {
            return colorsProfile.getColor(CodeAreaUnprintablesColorType.UNPRINTABLES_COLOR, CodeAreaBasicColors.TEXT_COLOR);
        }

        if (inSelection) {
            return section == caretPosition.getSection() ? colorsProfile.getColor(CodeAreaBasicColors.SELECTION_COLOR) : colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_COLOR);
        }

        return null;
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
        Rectangle mainAreaRect = dimensions.getMainAreaRect();
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
                    g.setColor(colorsProfile.getColor(CodeAreaBasicColors.CURSOR_COLOR));
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setStroke(cursorDataCache.dashedStroke);
                    g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
                }
            }
        }
        g.setClip(clipBounds);
    }

    private void paintCursorRect(Graphics g, int cursorX, int cursorY, int width, int height, DefaultCodeAreaCaret.CursorRenderingMode renderingMode, DefaultCodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRect(cursorX, cursorY, width, height);
                break;
            }
            case XOR: {
                g.setXORMode(colorsProfile.getColor(CodeAreaBasicColors.TEXT_BACKGROUND));
                g.fillRect(cursorX, cursorY, width, height);
                g.setPaintMode();
                break;
            }
            case NEGATIVE: {
                int characterWidth = metrics.getCharacterWidth();
                int rowHeight = metrics.getRowHeight();
                int maxBytesPerChar = metrics.getMaxBytesPerChar();
                int subFontSpace = metrics.getSubFontSpace();
                int dataViewX = dimensions.getDataViewX();
                int dataViewY = dimensions.getDataViewY();

                CodeAreaViewMode viewMode = structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
                long dataSize = structure.getDataSize();
                CodeType codeType = structure.getCodeType();
                g.fillRect(cursorX, cursorY, width, height);
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

                    if (maxBytesPerChar > 1) {
                        int charDataLength = maxBytesPerChar;
                        if (dataPosition + maxBytesPerChar > dataSize) {
                            charDataLength = (int) (dataSize - dataPosition);
                        }

                        if (contentData == null) {
                            cursorDataCache.cursorChars[0] = ' ';
                        } else {
                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            String displayString = new String(cursorDataCache.cursorData, 0, charDataLength, charset);
                            if (!displayString.isEmpty()) {
                                cursorDataCache.cursorChars[0] = displayString.charAt(0);
                            }
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != charset) {
                            buildCharMapping(charset);
                        }

                        if (contentData == null) {
                            cursorDataCache.cursorChars[0] = ' ';
                        } else {
                            cursorDataCache.cursorChars[0] = charMapping[contentData.getByte(dataPosition) & 0xFF];
                        }
                    }
//                    int posX = previewRelativeX + charPos * characterWidth - scrolling.getHorizontalScrollX(characterWidth);
                    drawCenteredChars(g, cursorDataCache.cursorChars, 0, 1, characterWidth, posX, posY);
                } else {
//                    int charPos = (scrolledX - dataViewX) / characterWidth;
//                    int byteOffset = structure.computePositionByte(charPos);
//                    int codeCharPos = structure.computeFirstCodeHalfCharPos(byteOffset);

                    if (contentData != null && dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, codeCharactersCase);
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
//                    int posX = dataViewX + codeCharPos * characterWidth - scrolling.getHorizontalScrollX(characterWidth);
//                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChars(g, cursorDataCache.cursorChars, codeOffset, 1, characterWidth, posX, posY);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
        }
    }

    @Nonnull
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, PositionOverflowMode overflowMode) {
        CodeAreaCaretPosition caret = new CodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
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
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
                diffY = 1;
            }
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getRowOffset()) / rowHeight + scrollPosition.getRowPosition() - diffY;
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
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
        caret.setCodeOffset(codeOffset);
        caret.setDataPosition(dataPosition);
        return caret;
    }

    @Nonnull
    @Override
    public CaretPosition computeMovePosition(CaretPosition position, MovementDirection direction) {
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
        halfCharPos += codeOffset * 2; // TODO
        caretX = dataViewRect.x + layoutProfile.computePositionX(halfCharPos, characterWidth, characterWidth / 2);
//        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
//            caretX = dataViewRect.x + visibility.getSplitLinePos() + characterWidth * byteOffset;
//        } else {
//            caretX = dataViewRect.x + characterWidth * (structure.computeFirstCodeHalfCharPos(byteOffset) + codeOffset);
//        }
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
        int dataViewX = dimensions.getDataViewX();
        int dataViewY = dimensions.getDataViewY();
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
    public ExtendedCodeAreaLayoutProfile getLayoutProfile() {
        return layoutProfile.createCopy();
    }

    @Override
    public void setLayoutProfile(ExtendedCodeAreaLayoutProfile layoutProfile) {
        this.layoutProfile = layoutProfile.createCopy();
        resetLayout();
    }

    @Nonnull
    @Override
    public ExtendedCodeAreaThemeProfile getThemeProfile() {
        return ExtendedCodeAreaThemeProfile.createCopy(themeProfile);
    }

    @Override
    public void setThemeProfile(ExtendedCodeAreaThemeProfile themeProfile) {
        this.themeProfile = ExtendedCodeAreaThemeProfile.createCopy(themeProfile);
        codeArea.repaint();
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
    protected void drawCenteredChars(Graphics g, char[] drawnChars, int charOffset, int length, int cellWidth, int positionX, int positionY) {
        int pos = 0;
        int group = 0;
        while (pos < length) {
            int charWidth = metrics.getCharWidth(drawnChars[charOffset + pos]);

            boolean groupable;
            if (metrics.hasUniformLineMetrics()) {
                groupable = charWidth == cellWidth;
            } else {
                int charsWidth = metrics.getCharsWidth(drawnChars, charOffset + pos - group, group + 1);
                groupable = charsWidth == cellWidth * (group + 1);
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

    protected void drawShiftedChars(Graphics g, char[] drawnChars, int charOffset, int length, int positionX, int positionY) {
        g.drawChars(drawnChars, charOffset, length, positionX, positionY);
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
            DefaultCodeAreaCaret.CursorShape cursorShape = editationOperation == EditationOperation.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
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

    private void buildUnprintableCharactersMapping() {
        unprintableCharactersMapping = new HashMap<>();
        // Unicode control characters, might not be supported by font
        for (int i = 0; i < 32; i++) {
            unprintableCharactersMapping.put((char) i, Character.toChars(9216 + i)[0]);
        }
        // Space -> Middle Dot
        unprintableCharactersMapping.put(' ', Character.toChars(183)[0]);
        // Tab -> Right-Pointing Double Angle Quotation Mark
        unprintableCharactersMapping.put('\t', Character.toChars(187)[0]);
        // Line Feed -> Currency Sign
        unprintableCharactersMapping.put('\r', Character.toChars(164)[0]);
        // Carriage Return -> Pilcrow Sign
        unprintableCharactersMapping.put('\n', Character.toChars(182)[0]);
        // Ideographic Space -> Degree Sign
        unprintableCharactersMapping.put(Character.toChars(127)[0], Character.toChars(176)[0]);
    }

    @Override
    public void updateScrollBars() {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        long rowsPerDocument = structure.getRowsPerDocument();
        recomputeScrollState();

        adjusting = true;
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        scrollPanel.setVerticalScrollBarPolicy(CodeAreaSwingUtils.getVerticalScrollBarPolicy(scrolling.getVerticalScrollBarVisibility()));
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        scrollPanel.setHorizontalScrollBarPolicy(CodeAreaSwingUtils.getHorizontalScrollBarPolicy(scrolling.getHorizontalScrollBarVisibility()));

        int verticalScrollValue = scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument);
        verticalScrollBar.setValue(verticalScrollValue);

        int horizontalScrollValue = scrolling.getHorizontalScrollValue(characterWidth);
        horizontalScrollBar.setValue(horizontalScrollValue);

        adjusting = false;
    }

    protected int getCharactersPerRow() {
        return structure.getHalfCharsPerRow();
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
            if (e == null || adjusting) {
                return;
            }

            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
            int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
            long rowsPerDocumentToLastPage = structure.getRowsPerDocument() - dimensions.getRowsPerRect();
            scrolling.updateVerticalScrollBarValue(scrollBarValue, metrics.getRowHeight(), maxValue, rowsPerDocumentToLastPage);
            ((ScrollingCapable) codeArea).setScrollPosition(scrolling.getScrollPosition());
            notifyScrolled();
            codeArea.repaint();
//            dataViewScrolled(codeArea.getGraphics());
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(@Nullable AdjustmentEvent e) {
            if (e == null || adjusting) {
                return;
            }

            int scrollBarValue = scrollPanel.getHorizontalScrollBar().getValue();
            scrolling.updateHorizontalScrollBarValue(scrollBarValue, metrics.getCharacterWidth());
            ((ScrollingCapable) codeArea).setScrollPosition(scrolling.getScrollPosition());
            notifyScrolled();
            codeArea.repaint();
//            dataViewScrolled(codeArea.getGraphics());
        }
    }

    private void notifyScrolled() {
        recomputeScrollState();
        ((ScrollingCapable) codeArea).notifyScrolled();
    }

    private static class RowDataCache {

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

    private static class CursorDataCache {

        Rectangle caretRect = new Rectangle();
        Rectangle mirrorCursorRect = new Rectangle();
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
