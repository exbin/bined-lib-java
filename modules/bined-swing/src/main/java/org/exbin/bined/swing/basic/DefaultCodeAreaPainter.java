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

import org.exbin.bined.swing.basic.color.BasicCodeAreaColorsProfile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
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
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret.CursorRenderingMode;
import org.exbin.bined.swing.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.swing.capability.AntialiasingCapable;
import org.exbin.bined.swing.capability.BackgroundPaintCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2018/11/29
 * @author ExBin Project (https://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter, BasicColorsCapableCodeAreaPainter {

    @Nonnull
    protected final CodeAreaCore codeArea;
    private volatile boolean initialized = false;
    private volatile boolean adjusting = false;
    private volatile boolean fontChanged = false;
    private volatile boolean resetColors = true;

    @Nonnull
    private final JPanel dataView;
    @Nonnull
    private final JScrollPane scrollPanel;

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

    private BasicCodeAreaColorsProfile colorsProfile = new BasicCodeAreaColorsProfile();

    @Nullable
    private CodeCharactersCase codeCharactersCase;
    @Nullable
    private EditationMode editationMode;
    @Nullable
    private BasicBackgroundPaintMode backgroundPaintMode;
    private boolean showMirrorCursor;
    @Nonnull
    private AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;

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

    public DefaultCodeAreaPainter(@Nonnull CodeAreaCore codeArea) {
        this.codeArea = codeArea;
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
        updateLayout();
        resetScrollState();
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
        dimensions.recomputeSizes(metrics, codeArea.getWidth(), codeArea.getHeight(), rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
        int charactersPerPage = dimensions.getCharactersPerPage();

        structure.updateCache(codeArea, charactersPerPage);
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        editationMode = ((EditationModeCapable) codeArea).getEditationMode();
        backgroundPaintMode = ((BackgroundPaintCapable) codeArea).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();
        rowPositionNumberLength = ((RowWrappingCapable) codeArea).getRowPositionLength();
        antialiasingMode = ((AntialiasingCapable) codeArea).getAntialiasingMode();

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

        rowDataCache.headerChars = new char[structure.getCharactersPerCodeSection()];
        rowDataCache.rowData = new byte[structure.getBytesPerRow() + maxBytesPerChar - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[structure.getCharactersPerRow()];
    }

    public void fontChanged(@Nonnull Graphics g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        maxBytesPerChar = (int) encoder.maxBytesPerChar();

        font = ((FontCapable) codeArea).getCodeFont();
        metrics.recomputeMetrics(g.getFontMetrics(font));

        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        dimensions.recomputeSizes(metrics, codeArea.getWidth(), codeArea.getHeight(), rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
        resetCharPositions();
        initialized = true;
    }

    public void dataViewScrolled(@Nonnull Graphics g) {
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
    public void paintComponent(@Nonnull Graphics g) {
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

    public void paintOutsiteArea(@Nonnull Graphics g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int componentWidth = dimensions.getComponentWidth();
        int characterWidth = metrics.getCharacterWidth();
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(0, 0, componentWidth, headerAreaHeight);

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        g.drawLine(0, headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1);

        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, 0, lineX, headerAreaHeight);
        }
    }

    public void paintHeader(@Nonnull Graphics g) {
        int charactersPerCodeSection = structure.getCharactersPerCodeSection();
        Rectangle clipBounds = g.getClipBounds();
        Rectangle headerArea = dimensions.getHeaderAreaRectangle();
        g.setClip(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getDataViewX();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int componentWidth = dimensions.getComponentWidth();

        g.setFont(font);
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        // Decoration lines
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.setColor(colorsProfile.getDecorationLine());
        g.fillRect(0, headerAreaHeight - 1, componentWidth, 1);
        int lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, 0, lineX, headerAreaHeight);
        }

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            int headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            int headerY = rowHeight - metrics.getSubFontSpace();

            g.setColor(colorsProfile.getTextColor());
            Arrays.fill(rowDataCache.headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            int visibleCodeStart = visibility.getVisibleCodeStart();
            int visibleMatrixCodeEnd = visibility.getVisibleMatrixCodeEnd();
            for (int index = visibleCodeStart; index < visibleMatrixCodeEnd; index++) {
                int codePos = structure.computeFirstCodeCharacterPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(rowDataCache.headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int visibleCharStart = visibility.getVisibleCharStart();
            int visibleMatrixCharEnd = visibility.getVisibleMatrixCharEnd();
            int renderOffset = visibleCharStart;
            Color renderColor = null;
            for (int characterOnRow = visibleCharStart; characterOnRow < visibleMatrixCharEnd; characterOnRow++) {
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

        g.setClip(clipBounds);
    }

    public void paintRowPosition(@Nonnull Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        Rectangle clipBounds = g.getClipBounds();
        Rectangle rowPositionsArea = dimensions.getRowPositionAreaRectangle();
        g.setClip(clipBounds != null ? clipBounds.intersection(rowPositionsArea) : rowPositionsArea);

        g.setFont(font);
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(rowPositionsArea.x, rowPositionsArea.y, rowPositionsArea.width, rowPositionsArea.height);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colorsProfile.getAlternateBackground());
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
        g.setColor(colorsProfile.getTextColor());
        Rectangle compRect = new Rectangle();
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, codeCharactersCase);
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, compRect.x, positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
        }

        g.setColor(colorsProfile.getDecorationLine());
        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }
        g.drawLine(dataViewRectangle.x, dataViewRectangle.y - 1, dataViewRectangle.x + dataViewRectangle.width, dataViewRectangle.y - 1);

        g.setClip(clipBounds);
    }

    @Override
    public void paintMainArea(@Nonnull Graphics g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            fontChanged(g);
            fontChanged = false;
        }

        Rectangle clipBounds = g.getClipBounds();
        Rectangle mainArea = dimensions.getMainAreaRect();
        g.setClip(clipBounds != null ? clipBounds.intersection(mainArea) : mainArea);
        paintBackground(g);

        Rectangle dataViewRectangle = dimensions.getDataViewRectangle();
        int characterWidth = metrics.getCharacterWidth();
        int previewRelativeX = visibility.getPreviewRelativeX();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.setColor(colorsProfile.getDecorationLine());
        int lineX = dataViewRectangle.x + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewRectangle.x) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
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
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int rowsPerRect = dimensions.getRowsPerRect();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowPositionX = rowPositionAreaWidth;
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.setColor(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.fillRect(rowPositionX, headerAreaHeight, dataViewWidth, dataViewHeight);
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + (int) ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize - bytesPerRow) {
                    break;
                }

                g.fillRect(rowPositionX, stripePositionY, dataViewWidth, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(@Nonnull Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getDataViewX();
        int dataViewY = dimensions.getDataViewY();
        int rowsPerRect = dimensions.getRowsPerRect();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = dataViewY - scrollPosition.getRowOffset();
        g.setColor(colorsProfile.getTextColor());
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
            int visibleCodeStart = visibility.getVisibleCodeStart();
            int visibleCodeEnd = visibility.getVisibleCodeEnd();
            for (int byteOnRow = Math.max(visibleCodeStart, rowStart); byteOnRow < Math.min(visibleCodeEnd, rowBytesLimit); byteOnRow++) {
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
    public void paintRowBackground(@Nonnull Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = structure.getPreviewCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getCharactersPerRow();
        int visibleCharStart = visibility.getVisibleCharStart();
        int visibleCharEnd = visibility.getVisibleCharEnd();
        int renderOffset = visibleCharStart;
        Color renderColor = null;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
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
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, @Nonnull CodeAreaSection section) {
        SelectionRange selectionRange = structure.getSelectionRange();
        int codeLastCharPos = structure.getCodeLastCharPos();
        CodeAreaCaretPosition caretPosition = structure.getCaretPosition();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX)) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == caretPosition.getSection() ? colorsProfile.getSelectionBackground() : colorsProfile.getSelectionMirrorBackground();
        }

        return null;
    }

    @Nullable
    @Override
    public PositionScrollVisibility computePositionScrollVisibility(@Nonnull CaretPosition caretPosition) {
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
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computePositionScrollVisibility(rowPosition, charPosition, bytesPerRow, previewCharPos, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
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
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, previewCharPos, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(@Nonnull CaretPosition caretPosition) {
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
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
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
    public void paintRowText(@Nonnull Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = structure.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();

        g.setFont(font);
        int positionY = rowPositionY + rowHeight - subFontSpace;

        Color lastColor = null;
        Color renderColor = null;

        int visibleCharStart = visibility.getVisibleCharStart();
        int visibleCharEnd = visibility.getVisibleCharEnd();
        int renderOffset = visibleCharStart;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
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
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, @Nonnull CodeAreaSection section) {
        SelectionRange selectionRange = structure.getSelectionRange();
        CodeAreaCaretPosition caretPosition = structure.getCaretPosition();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection) {
            return section == caretPosition.getSection() ? colorsProfile.getSelectionColor() : colorsProfile.getSelectionMirrorColor();
        }

        return null;
    }

    @Override
    public void paintCursor(@Nonnull Graphics g) {
        if (!codeArea.hasFocus()) {
            return;
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

        Rectangle scrolledCursorRect = new Rectangle();
        scrolledCursorRect.x = cursorRect.x;
        scrolledCursorRect.y = cursorRect.y;
        scrolledCursorRect.width = cursorRect.width;
        scrolledCursorRect.height = cursorRect.height;

        Rectangle clipBounds = g.getClipBounds();
        Rectangle mainAreaRect = dimensions.getMainAreaRect();
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
            Rectangle mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            if (mirrorCursorRect != null) {
                intersection = mainAreaRect.intersection(mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.setClip(intersection);
                    g.setColor(colorsProfile.getCursorColor());
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
                g.setXORMode(colorsProfile.getTextBackground());
                g.fillRect(cursorX, cursorY, width, height);
                g.setPaintMode();
                break;
            }
            case NEGATIVE: {
                int characterWidth = metrics.getCharacterWidth();
                int rowHeight = metrics.getRowHeight();
                int subFontSpace = metrics.getSubFontSpace();
                int dataViewX = dimensions.getDataViewX();
                int dataViewY = dimensions.getDataViewY();
                int previewRelativeX = visibility.getPreviewRelativeX();

                CodeAreaViewMode viewMode = structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
                long dataSize = structure.getDataSize();
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
                    drawCenteredChars(g, previewChars, 0, 1, characterWidth, posX, posY);
                } else {
                    int charPos = (scrolledX - dataViewX) / characterWidth;
                    int byteOffset = structure.computePositionByte(charPos);
                    int codeCharPos = structure.computeFirstCodeCharacterPos(byteOffset);

                    if (contentData != null && dataPosition < dataSize) {
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
                throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
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
    public Point getPositionPoint(long dataPosition, int codeOffset, @Nonnull CodeAreaSection section) {
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

    @Nullable
    private Rectangle getMirrorCursorRect(long dataPosition, @Nonnull CodeAreaSection section) {
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
    public void setBasicColors(@Nonnull BasicCodeAreaColorsProfile colors) {
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
    protected void drawCenteredChars(@Nonnull Graphics g, char[] drawnChars, int charOffset, int length, int cellWidth, int positionX, int positionY) {
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

    protected void drawShiftedChars(@Nonnull Graphics g, char[] drawnChars, int charOffset, int length, int positionX, int positionY) {
        g.drawChars(drawnChars, charOffset, length, positionX, positionY);
    }

    /**
     * Precomputes widths for basic ascii characters.
     *
     * @param charset
     */
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
    public Rectangle getPositionRect(long dataPosition, int codeOffset, @Nonnull CodeAreaSection section) {
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
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(@Nonnull Graphics g, int startOffset, int endOffset, int rowPositionX, int positionY) {
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
        return structure.getCharactersPerRow();
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
        resetScrollState();
        ((ScrollingCapable) codeArea).notifyScrolled();
    }

    private static class RowDataCache {

        private char[] headerChars;
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
