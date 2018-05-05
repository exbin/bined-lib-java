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

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
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
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.swt.CodeArea;
import org.exbin.bined.swt.CodeAreaPainter;
import org.exbin.bined.swt.CodeAreaSwtUtils;
import org.exbin.bined.swt.CodeAreaWorker;
import org.exbin.bined.swt.MovementDirection;
import org.exbin.bined.swt.ScrollingDirection;
import org.exbin.bined.swt.basic.DefaultCodeAreaCaret.CursorRenderingMode;
import org.exbin.bined.swt.capability.BackgroundPaintCapable;
import org.exbin.bined.swt.capability.FontCapable;
import org.exbin.bined.swt.capability.ScrollingCapable;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2018/05/05
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    @Nonnull
    protected final CodeAreaWorker worker;
    private boolean initialized = false;
    private boolean fontChanged = false;

    @Nonnull
    private final Composite dataView;
    @Nonnull
    private final ScrolledComposite scrollPanel;

    private CodeAreaViewMode viewMode;
    private final CodeAreaCaretPosition caretPosition = new CodeAreaCaretPosition();
    private SelectionRange selectionRange = null;
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    private ScrollBarVerticalScale scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;

    private VerticalScrollUnit verticalScrollUnit;
    private HorizontalScrollUnit horizontalScrollUnit;
    private final Colors colors = new Colors();
    private long dataSize;

    private int componentWidth;
    private int componentHeight;
    private int dataViewX;
    private int dataViewY;
    private int scrollPanelWidth;
    private int scrollPanelHeight;
    private int dataViewWidth;
    private int dataViewHeight;

    private int rowPositionLength;
    private int rowPositionAreaWidth;
    private int headerAreaHeight;
    private int rowHeight;
    private int rowsPerPage;
    private int rowsPerRect;
    private int bytesPerRow;
    private int charactersPerPage;
    private int charactersPerRect;
    private int charactersPerRow;
    private CodeType codeType;
    private CodeCharactersCase hexCharactersCase;
    private EditationMode editationMode;
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
    private int maxCharLength;

    private byte[] rowData;
    private char[] rowPositionCode;
    private char[] rowCharacters;

    // TODO replace with computation
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
        dataView = new Composite(codeArea, SWT.NONE);
        dataView.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(@Nonnull PaintEvent paintEvent) {
                GC g = paintEvent.gc;
                if (g == null) {
                    return;
                }

                paintComponent(g);
            }
        });
//        dataView.setVisible(false);
//        dataView.setLayout(null);
        dataView.setBackgroundMode(SWT.INHERIT_NONE);
        // Fill whole area, no more suitable method found so far
        dataView.setSize(0, 0);
        scrollPanel = new ScrolledComposite(codeArea, SWT.H_SCROLL | SWT.V_SCROLL);
        ScrollBar verticalScrollBar = scrollPanel.getVerticalBar();
        verticalScrollBar.addSelectionListener(new VerticalSelectionListener());
        ScrollBar horizontalScrollBar = scrollPanel.getHorizontalBar();
        horizontalScrollBar.addSelectionListener(new HorizontalSelectionListener());
//        codeArea.add(scrollPanel);
        scrollPanel.setBackgroundMode(SWT.INHERIT_NONE);
        scrollPanel.setContent(dataView);
//        scrollPanel.setViewportBorder(null);
//        scrollPanel.getViewport().setOpaque(false);

        DefaultCodeAreaMouseListener codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
        codeArea.addMouseListener(codeAreaMouseListener);
        codeArea.addMouseMoveListener(codeAreaMouseListener);
        codeArea.addMouseWheelListener(codeAreaMouseListener);
        codeArea.addMouseTrackListener(codeAreaMouseListener);
        scrollPanel.addMouseListener(codeAreaMouseListener);
        scrollPanel.addMouseMoveListener(codeAreaMouseListener);
        scrollPanel.addMouseWheelListener(codeAreaMouseListener);
        scrollPanel.addMouseTrackListener(codeAreaMouseListener);
        dataView.layout();
        codeArea.layout();
    }

    @Override
    public void reset() {
        resetColors();
        resetFont();
        resetLayout();
    }

    @Override
    public void resetFont() {
        fontChanged = true;
    }

    @Override
    public void resetLayout() {
        resetSizes();

        viewMode = ((ViewModeCapable) worker).getViewMode();
        hexCharactersCase = ((CodeCharactersCaseCapable) worker).getCodeCharactersCase();
        editationMode = ((EditationModeCapable) worker).getEditationMode();
        caretPosition.setPosition(((CaretCapable) worker).getCaret().getCaretPosition());
        selectionRange = ((SelectionCapable) worker).getSelection();
        backgroundPaintMode = ((BackgroundPaintCapable) worker).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) worker).isShowMirrorCursor();
        dataSize = worker.getCodeArea().getDataSize();

        rowsPerRect = computeRowsPerRectangle();
        rowsPerPage = computeRowsPerPage();
        bytesPerRow = computeBytesPerRow();

        codeType = ((CodeTypeCapable) worker).getCodeType();
        hexCharactersCase = CodeCharactersCase.UPPER;

        charactersPerPage = computeCharactersPerPage();
        charactersPerRow = computeCharactersPerRow();

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
            visibleCharStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            visibleCharEnd = ((scrollPosition.getScrollCharPosition() + charactersPerRect) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
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
            visiblePreviewStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewStart < 0) {
                visiblePreviewStart = 0;
            }
            if (visibleCodeEnd < 0) {
                visibleCharStart = visiblePreviewStart + previewCharPos;
            }
            visiblePreviewEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + 1) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth - previewCharPos;
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

        rowData = new byte[bytesPerRow + maxCharLength - 1];
        rowPositionCode = new char[rowPositionLength];
        rowCharacters = new char[charactersPerRow];
    }

    public void resetFont(@Nonnull GC g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) worker).getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        maxCharLength = (int) encoder.maxBytesPerChar();

        font = ((FontCapable) worker).getFont();
        g.setFont(font);
        fontMetrics = g.getFontMetrics();
        /**
         * Use small 'w' character to guess normal font width.
         */
        characterWidth = g.textExtent("w").x;
        /**
         * Compare it to small 'i' to detect if font is monospaced.
         *
         * TODO: Is there better way?
         */
        monospaceFont = characterWidth == g.textExtent(" ").x && characterWidth == g.textExtent("i").x;
        int fontSize = fontMetrics.getHeight();
        rowHeight = fontSize + subFontSpace;

        rowPositionLength = getRowPositionLength();
        resetSizes();
        resetCharPositions();
        initialized = true;
    }

    public void dataViewScrolled(@Nonnull GC g) {
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
        horizontalScrollUnit = ((ScrollingCapable) worker).getHorizontalScrollUnit();

        if (rowHeight > 0 && characterWidth > 0) {
            int documentDataWidth = charactersPerRow * characterWidth;
            long rowsPerData = (dataSize + bytesPerRow - 1) / bytesPerRow;

            int documentDataHeight;
            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                documentDataHeight = Integer.MAX_VALUE;
            } else {
                scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                documentDataHeight = (int) (rowsPerData * rowHeight);
            }

            dataView.setSize(documentDataWidth, documentDataHeight);
            worker.getCodeArea().layout();
        }

        // TODO on resize only
        scrollPanel.setBounds(getScrollPanelRectangle());
        scrollPanel.redraw();
    }

    private void resetSizes() {
        if (fontMetrics == null) {
            headerAreaHeight = 0;
        } else {
            int fontHeight = fontMetrics.getHeight();
            headerAreaHeight = fontHeight + fontHeight / 4;
        }

        componentWidth = worker.getCodeArea().getSize().x;
        componentHeight = worker.getCodeArea().getSize().y;
        rowPositionAreaWidth = characterWidth * (rowPositionLength + 1);
        dataViewX = rowPositionAreaWidth;
        dataViewY = headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - getVerticalScrollBarSize();
        dataViewHeight = scrollPanelHeight - getHorizontalScrollBarSize();
    }

    private void resetColors() {
        Display display = Display.getCurrent();
        CodeArea codeArea = worker.getCodeArea();
        colors.foreground = codeArea.getForeground();
        if (colors.foreground == null) {
            colors.foreground = display.getSystemColor(SWT.COLOR_BLACK);
        }

        colors.background = codeArea.getBackground();
        if (colors.background == null) {
            colors.background = display.getSystemColor(SWT.COLOR_WHITE);
        }
        colors.selectionForeground = display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
        if (colors.selectionForeground == null) {
            colors.selectionForeground = display.getSystemColor(SWT.COLOR_WHITE);
        }
        colors.selectionBackground = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
        if (colors.selectionBackground == null) {
            colors.selectionBackground = new Color(display, 96, 96, 255);
        }
        colors.selectionMirrorForeground = colors.selectionForeground;
        colors.selectionMirrorBackground = CodeAreaSwtUtils.computeGrayColor(colors.selectionBackground);
        colors.cursor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        if (colors.cursor == null) {
            colors.cursor = display.getSystemColor(SWT.COLOR_BLACK);
        }
        colors.negativeCursor = CodeAreaSwtUtils.createNegativeColor(colors.cursor);
        colors.cursorMirror = colors.cursor;
        colors.negativeCursorMirror = colors.negativeCursor;
        colors.decorationLine = display.getSystemColor(SWT.COLOR_GRAY);

        colors.stripes = CodeAreaSwtUtils.createOddColor(colors.background);
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
        if (font == null) {
            resetFont(g);
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintRowPosition(g);
        paintMainArea(g);
//        scrollPanel.paintComponents(g);
        paintCounter++;
    }

    @Override
    public void repaint() {
        scrollPanel.redraw();
//        dataView.redraw();
    }

    public void paintOutsiteArea(@Nonnull GC g) {
        g.setForeground(colors.background);
        g.fillRectangle(0, 0, componentWidth, headerAreaHeight);

        // Decoration lines
        g.setForeground(colors.decorationLine);
        g.drawLine(0, headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1);

        {
            int lineX = rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= 0) {
                g.drawLine(lineX, 0, lineX, headerAreaHeight);
            }
        }
    }

    public void paintHeader(@Nonnull GC g) {
        Rectangle clipBounds = g.getClipping();
        Rectangle headerArea = new Rectangle(rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        g.setClipping(clipBounds != null ? headerArea.intersection(clipBounds) : headerArea);

        g.setForeground(colors.background);
        g.fillRectangle(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        // Decoration lines
        g.setForeground(colors.decorationLine);
        g.fillRectangle(0, headerAreaHeight - 1, componentWidth, 1);
        int lineX = dataViewX + previewRelativeX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, 0, lineX, headerAreaHeight);
        }

        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int charactersPerCodeArea = computeFirstCodeCharacterPos(bytesPerRow);
            int headerX = dataViewX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
            int headerY = rowHeight - subFontSpace;

            int visibleHeaderCharStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleHeaderCharStart < 0) {
                visibleHeaderCharStart = 0;
            }
            int visibleHeaderCharEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + charactersPerCodeArea) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleHeaderCharEnd > charactersPerCodeArea) {
                visibleHeaderCharEnd = charactersPerCodeArea;
            }
            int visibleStart = computePositionByte(visibleHeaderCharStart);
            int visibleEnd = computePositionByte(visibleHeaderCharEnd - 1) + 1;

            g.setForeground(colors.foreground);
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
//            ColorsGroup.ColorType renderColorType = null;
            Color renderColor = null;
            for (int characterOnRow = visibleHeaderCharStart; characterOnRow < visibleHeaderCharEnd; characterOnRow++) {
                int byteOnRow;
                byteOnRow = computePositionByte(characterOnRow);
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
                if (monospaceFont) { // characterRenderingMode == CharacterRenderingMode.AUTO && 
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

                Color color = colors.foreground;
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
                        char[] subArray = Arrays.copyOfRange(headerChars, renderOffset, characterOnRow - renderOffset);
                        g.drawString(String.valueOf(subArray), headerX + renderOffset * characterWidth, headerY);
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

            if (renderOffset < charactersPerCodeArea) {
                char[] subArray = Arrays.copyOfRange(headerChars, renderOffset, charactersPerCodeArea - renderOffset);
                g.drawString(String.valueOf(subArray), headerX + renderOffset * characterWidth, headerY);
            }
        }

        g.setClipping(clipBounds);
    }

    public void paintRowPosition(@Nonnull GC g) {
        Rectangle clipBounds = g.getClipping();
        Rectangle rowPositionsArea = new Rectangle(0, headerAreaHeight, rowPositionAreaWidth, componentHeight - headerAreaHeight - getHorizontalScrollBarSize());
        g.setClipping(clipBounds != null ? rowPositionsArea.intersection(clipBounds) : rowPositionsArea);

        g.setForeground(colors.background);
        g.fillRectangle(rowPositionsArea.x, rowPositionsArea.y, rowPositionsArea.width, rowPositionsArea.height);

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight + ((scrollPosition.getScrollRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setForeground(colors.stripes);
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.fillRectangle(0, stripePositionY, rowPositionAreaWidth, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getScrollRowPosition();
        int positionY = headerAreaHeight + rowHeight - subFontSpace - scrollPosition.getScrollRowOffset();
        g.setForeground(colors.foreground);
        Rectangle compRect = new Rectangle(0, 0, 0, 0);
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, codeType.getBase(), rowPositionLength, true, CodeCharactersCase.UPPER);
//            if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
//            } else {
            for (int digitIndex = 0; digitIndex < rowPositionLength; digitIndex++) {
                drawCenteredChar(g, rowPositionCode, digitIndex, characterWidth, compRect.x + characterWidth * digitIndex, positionY);
            }
//            }

            positionY += rowHeight;
            dataPosition += bytesPerRow;
        }

        g.setForeground(colors.decorationLine);
        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, dataViewY, lineX, dataViewY + dataViewHeight);
        }
        g.drawLine(dataViewX, dataViewY - 1, dataViewX + dataViewWidth, dataViewY - 1);

        g.setClipping(clipBounds);
    }

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
        Rectangle mainArea = getMainAreaRect();
        g.setClipping(clipBounds != null ? mainArea.intersection(clipBounds) : mainArea);
        paintBackground(g);

        g.setForeground(colors.decorationLine);
        int lineX = dataViewX + previewRelativeX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, dataViewY, lineX, dataViewY + dataViewHeight);
        }

        paintRows(g);
        g.setClipping(clipBounds);
        paintCursor(g);

        // TODO: Remove later
        int x = componentWidth - rowPositionAreaWidth - 220;
        int y = componentHeight - headerAreaHeight - 20;
        Display display = Display.getCurrent();
        g.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
        g.fillRectangle(x, y, 200, 16);
        g.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        char[] headerCode = (String.valueOf(scrollPosition.getScrollCharPosition()) + "+" + String.valueOf(scrollPosition.getScrollCharOffset()) + " : " + String.valueOf(scrollPosition.getScrollRowPosition()) + "+" + String.valueOf(scrollPosition.getScrollRowOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
        g.drawString(String.valueOf(headerCode), x, y + rowHeight);
    }

    public void paintBackground(@Nonnull GC g) {
//        Rectangle clipBounds = g.getClipping();
//        Rectangle codeRect = codeArea.getCodeSectionRectangle();
//        ColorsGroup mainColors = codeArea.getMainColors();
//        ColorsGroup stripColors = codeArea.getAlternateColors();
//        int bytesPerLine = codeArea.getBytesPerLine();
//        int lineHeight = codeArea.getLineHeight();
//        int startX = clipBounds.x;
//        int width = clipBounds.width;
//        if (!codeArea.isLineNumberBackground() && codeArea.isShowLineNumbers()) {
//            int lineNumberWidth = codeRect.x - 1 - codeArea.getLineNumberSpace() / 2;
//            if (startX < lineNumberWidth) {
//                int diff = lineNumberWidth - startX;
//                startX = lineNumberWidth;
//                width -= diff;
//            }
//        }
//        if (codeArea.getBackgroundPaintMode() != CodeArea.BackgroundPaintMode.NONE) {
//            g.setForeground(mainColors.getBackgroundColor());
//            g.fillRectangle(startX, clipBounds.y, width, clipBounds.height);
//        }
//
//        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//        long line = scrollPosition.getScrollLinePosition();
//        long maxDataPosition = codeArea.getDataSize();
//        int maxY = clipBounds.y + clipBounds.height;
//
//        int positionY;
//        long dataPosition = line * bytesPerLine;
//        if (codeArea.getBackgroundPaintMode() == CodeArea.BackgroundPaintMode.STRIPPED || codeArea.getBackgroundPaintMode() == CodeArea.BackgroundPaintMode.GRIDDED) {
//            g.setForeground(stripColors.getBackgroundColor());
//
//            positionY = codeRect.y - scrollPosition.getScrollLineOffset();
//            if ((line & 1) == 0) {
//                positionY += lineHeight;
//                dataPosition += bytesPerLine;
//            }
//            while (positionY <= maxY && dataPosition < maxDataPosition) {
//                g.fillRectangle(startX, positionY, width, lineHeight);
//                positionY += lineHeight * 2;
//                dataPosition += bytesPerLine * 2;
//            }
//        }
    }

    public void paintRows(@Nonnull GC g) {
        long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
        int rowPositionX = rowPositionAreaWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
        int rowPositionY = headerAreaHeight;
        g.setForeground(colors.foreground);
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
            int rowDataSize = bytesPerRow + maxCharLength - 1;
            if (dataPosition + rowDataSize > dataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                rowStart = (int) -dataPosition;
            }
            worker.getCodeArea().getContentData().copyToArray(dataPosition + rowStart, rowData, rowStart, rowDataSize - rowStart);
            if (dataPosition + rowBytesLimit > dataSize) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            for (int byteOnRow = Math.max(visibleCodeStart, rowStart); byteOnRow < Math.min(visibleCodeEnd, rowBytesLimit); byteOnRow++) {
                byte dataByte = rowData[byteOnRow];
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowCharacters, computeFirstCodeCharacterPos(byteOnRow), hexCharactersCase);
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowCharacters, computeFirstCodeCharacterPos(rowBytesLimit), rowCharacters.length, ' ');
            }
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            for (int byteOnRow = visiblePreviewStart; byteOnRow < Math.min(visiblePreviewEnd, rowBytesLimit); byteOnRow++) {
                byte dataByte = rowData[byteOnRow];

                if (maxCharLength > 1) {
                    if (dataPosition + maxCharLength > dataSize) {
                        maxCharLength = (int) (dataSize - dataPosition);
                    }

                    int charDataLength = maxCharLength;
                    if (byteOnRow + charDataLength > rowData.length) {
                        charDataLength = rowData.length - byteOnRow;
                    }
                    String displayString = new String(rowData, byteOnRow, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        rowCharacters[previewCharPos + byteOnRow] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        buildCharMapping(charset);
                    }

                    rowCharacters[previewCharPos + byteOnRow] = charMapping[dataByte & 0xFF];
                }
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowCharacters, previewCharPos + rowBytesLimit, previewCharPos + bytesPerRow, ' ');
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
                        g.setForeground(color);
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
        if (rowPosition < scrollPosition.getScrollRowPosition()) {
            // Scroll row up
            targetScrollPosition.setScrollRowPosition(rowPosition);
            targetScrollPosition.setScrollRowOffset(0);
            scrolled = true;
        } else if ((rowPosition == scrollPosition.getScrollRowPosition() && scrollPosition.getScrollRowOffset() > 0)) {
            // Scroll row offset up
            targetScrollPosition.setScrollRowOffset(0);
            scrolled = true;
        } else {
            int bottomRowOffset;
            if (verticalScrollUnit == VerticalScrollUnit.ROW) {
                bottomRowOffset = 0;
            } else {
                if (dataViewHeight < rowHeight) {
                    throw new UnsupportedOperationException("Not supported yet.");
                } else {
                    bottomRowOffset = dataViewHeight % rowHeight;
                }
            }

            if (rowPosition > scrollPosition.getScrollRowPosition() + rowsPerPage) {
                // Scroll row down
                targetScrollPosition.setScrollRowPosition(rowPosition - rowsPerPage);
                targetScrollPosition.setScrollRowOffset(bottomRowOffset);
                scrolled = true;
            } else if (rowPosition == scrollPosition.getScrollRowPosition() + rowsPerPage && scrollPosition.getScrollRowOffset() > bottomRowOffset) {
                // Scroll row offset down
                targetScrollPosition.setScrollRowOffset(bottomRowOffset);
                scrolled = true;
            }
        }

        if (charPosition < scrollPosition.getScrollCharPosition()) {
            // Scroll characters left
            targetScrollPosition.setScrollCharPosition(charPosition);
            targetScrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else if (charPosition == scrollPosition.getScrollCharPosition() && scrollPosition.getScrollCharOffset() > 0) {
            // Scroll character offset left
            targetScrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else {
            int rightCharOffset;
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                rightCharOffset = 0;
            } else {
                if (dataViewWidth < characterWidth) {
                    throw new UnsupportedOperationException("Not supported yet.");
                } else {
                    rightCharOffset = dataViewWidth % characterWidth;
                }
            }

            if (charPosition > scrollPosition.getScrollCharPosition() + charactersPerPage) {
                // Scroll character right
                targetScrollPosition.setScrollCharPosition(charPosition - charactersPerPage);
                targetScrollPosition.setScrollCharOffset(rightCharOffset);
                scrolled = true;
            } else if (charPosition == scrollPosition.getScrollCharPosition() + charactersPerPage && scrollPosition.getScrollCharOffset() > rightCharOffset) {
                // Scroll row offset down
                targetScrollPosition.setScrollCharOffset(rightCharOffset);
                scrolled = true;
            }
        }
        return scrolled ? targetScrollPosition : null;
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CaretPosition caretPosition) {
        throw new UnsupportedOperationException("Not supported yet.");
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
            if (!CodeAreaSwtUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            int currentCharWidth = 0;
            char currentChar = rowCharacters[charOnRow];
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
                    drawShiftedChar(g, rowCharacters, charOnRow, characterWidth, positionX, positionY);
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

//    @Override
//    public void paintLineNumbers(GC g) {
//        Rectangle clipBounds = g.getClipping();
//        Rectangle compRect = codeArea.getComponentRectangle();
//        Rectangle codeRect = codeArea.getCodeSectionRectangle();
//        int bytesPerLine = codeArea.getBytesPerLine();
//        int lineHeight = codeArea.getLineHeight();
//
//        CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//        long line = scrollPosition.getScrollLinePosition();
//        long maxDataPosition = codeArea.getDataSize();
//        int maxY = clipBounds.y + clipBounds.height + lineHeight;
//        long dataPosition = line * bytesPerLine - scrollPosition.getLineByteShift();
//        int charWidth = codeArea.getCharWidth();
//        int positionY = codeRect.y - codeArea.getSubFontSpace() - scrollPosition.getScrollLineOffset() + codeArea.getLineHeight();
//
//        g.setForeground(codeArea.getForeground());
//        int lineNumberLength = codeArea.getLineNumberLength();
//        char[] lineNumberCode = new char[lineNumberLength];
//        boolean upperCase = codeArea.getHexCharactersCase() == HexCharactersCase.UPPER;
//        while (positionY <= maxY && dataPosition <= maxDataPosition) {
//            CodeAreaUtils.longToBaseCode(lineNumberCode, 0, dataPosition < 0 ? 0 : dataPosition, codeArea.getPositionCodeType().getBase(), lineNumberLength, true, upperCase);
//            if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                g.drawText(String.valueOf(lineNumberCode).substring(0, lineNumberLength - 1), compRect.x, positionY);
//            } else {
//                for (int i = 0; i < lineNumberLength; i++) {
//                    drawCenteredChar(g, lineNumberCode, i, charWidth, compRect.x + charWidth * i, positionY);
//                }
//            }
//            positionY += lineHeight;
//            dataPosition += bytesPerLine;
//        }
//
//        // Draw decoration lines
//        int decorationMode = codeArea.getDecorationMode();
//        if ((decorationMode & CodeArea.DECORATION_LINENUM_LINE) > 0) {
//            g.setForeground(codeArea.getDecorationLineColor());
//            int lineX = codeRect.x - 1 - codeArea.getLineNumberSpace() / 2;
//            g.drawLine(lineX, compRect.y, lineX, codeRect.y + codeRect.height);
//        }
//        if ((decorationMode & CodeArea.DECORATION_BOX) > 0) {
//            g.setForeground(codeArea.getDecorationLineColor());
//            g.drawLine(codeRect.x - 1, codeRect.y - 1, codeRect.x - 1, codeRect.y + codeRect.height);
//        }
//    }
//
//    @Override
//    public void paintMainArea(GC g) {
//        PaintData paintData = new PaintData(codeArea);
//        paintMainArea(g, paintData);
//    }
//
//    public void paintMainArea(GC g, PaintData paintData) {
//        if (paintData.viewMode != ViewMode.TEXT_PREVIEW && codeArea.getBackgroundPaintMode() == CodeArea.BackgroundPaintMode.GRIDDED) {
//            g.setForeground(paintData.alternateColors.getBackgroundColor());
//            int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.getScrollCharOffset() - paintData.scrollPosition.getScrollCharPosition() * paintData.charWidth;
//            for (int i = paintData.visibleCodeStart / 2; i < paintData.visibleCodeEnd / 2; i++) {
//                g.fillRectangle(positionX + paintData.charWidth * codeArea.computeByteCharPos(i * 2 + 1), paintData.codeSectionRect.y, paintData.charWidth * paintData.codeDigits, paintData.codeSectionRect.height);
//            }
//        }
//
//        int positionY = paintData.codeSectionRect.y - paintData.scrollPosition.getScrollLineOffset();
//        paintData.line = paintData.scrollPosition.getScrollLinePosition();
//        int positionX = paintData.codeSectionRect.x - paintData.scrollPosition.getScrollCharPosition() * paintData.charWidth - paintData.scrollPosition.getScrollCharOffset();
//        paintData.lineDataPosition = paintData.line * paintData.bytesPerLine - paintData.scrollPosition.getLineByteShift();
//        long dataSize = codeArea.getDataSize();
//
//        do {
//            if (paintData.showUnprintableCharacters) {
//                Arrays.fill(paintData.unprintableChars, ' ');
//            }
//            int lineBytesLimit = paintData.bytesPerLine;
//            if (paintData.lineDataPosition < dataSize) {
//                int lineDataSize = paintData.bytesPerLine + paintData.maxCharLength - 1;
//                if (paintData.lineDataPosition + lineDataSize > dataSize) {
//                    lineDataSize = (int) (dataSize - paintData.lineDataPosition);
//                }
//                if (paintData.lineDataPosition < 0) {
//                    paintData.lineStart = (int) -paintData.lineDataPosition;
//                } else {
//                    paintData.lineStart = 0;
//                }
//                codeArea.getBinaryData().copyToArray(paintData.lineDataPosition + paintData.lineStart, paintData.lineData, paintData.lineStart, lineDataSize - paintData.lineStart);
//                if (paintData.lineDataPosition + lineBytesLimit > dataSize) {
//                    lineBytesLimit = (int) (dataSize - paintData.lineDataPosition);
//                }
//            } else {
//                lineBytesLimit = 0;
//            }
//
//            // Fill codes
//            if (paintData.viewMode != ViewMode.TEXT_PREVIEW) {
//                for (int byteOnLine = Math.max(paintData.visibleCodeStart, paintData.lineStart); byteOnLine < Math.min(paintData.visibleCodeEnd, lineBytesLimit); byteOnLine++) {
//                    byte dataByte = paintData.lineData[byteOnLine];
//                    CodeAreaUtils.byteToCharsCode(dataByte, codeArea.getCodeType(), paintData.lineChars, codeArea.computeByteCharPos(byteOnLine), codeArea.getHexCharactersCase());
//                }
//                if (paintData.bytesPerLine > lineBytesLimit) {
//                    Arrays.fill(paintData.lineChars, codeArea.computeByteCharPos(lineBytesLimit), paintData.lineChars.length, ' ');
//                }
//            }
//
//            // Fill preview characters
//            if (paintData.viewMode != ViewMode.CODE_MATRIX) {
//                for (int byteOnLine = paintData.visiblePreviewStart; byteOnLine < Math.min(paintData.visiblePreviewEnd, lineBytesLimit); byteOnLine++) {
//                    byte dataByte = paintData.lineData[byteOnLine];
//
//                    if (paintData.maxCharLength > 1) {
//                        if (paintData.lineDataPosition + paintData.maxCharLength > dataSize) {
//                            paintData.maxCharLength = (int) (dataSize - paintData.lineDataPosition);
//                        }
//
//                        int charDataLength = paintData.maxCharLength;
//                        if (byteOnLine + charDataLength > paintData.lineData.length) {
//                            charDataLength = paintData.lineData.length - byteOnLine;
//                        }
//                        String displayString = new String(paintData.lineData, byteOnLine, charDataLength, paintData.charset);
//                        if (!displayString.isEmpty()) {
//                            paintData.lineChars[paintData.previewCharPos + byteOnLine] = displayString.charAt(0);
//                        }
//                    } else {
//                        if (charMappingCharset == null || charMappingCharset != paintData.charset) {
//                            buildCharMapping(paintData.charset);
//                        }
//
//                        paintData.lineChars[paintData.previewCharPos + byteOnLine] = charMapping[dataByte & 0xFF];
//                    }
//
//                    if (paintData.showUnprintableCharacters || paintData.charRenderingMode == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                        if (unprintableCharactersMapping == null) {
//                            buildUnprintableCharactersMapping();
//                        }
//                        Character replacement = unprintableCharactersMapping.get(paintData.lineChars[paintData.previewCharPos + byteOnLine]);
//                        if (replacement != null) {
//                            if (paintData.showUnprintableCharacters) {
//                                paintData.unprintableChars[paintData.previewCharPos + byteOnLine] = replacement;
//                            }
//                            paintData.lineChars[paintData.previewCharPos + byteOnLine] = ' ';
//                        }
//                    }
//                }
//                if (paintData.bytesPerLine > lineBytesLimit) {
//                    Arrays.fill(paintData.lineChars, paintData.previewCharPos + lineBytesLimit, paintData.previewCharPos + paintData.bytesPerLine, ' ');
//                }
//            }
//            paintLineBackground(g, positionX, positionY, paintData);
//            paintLineText(g, positionX, positionY, paintData);
//            paintData.lineDataPosition += paintData.bytesPerLine;
//            paintData.line++;
//            positionY += paintData.lineHeight;
//        } while (positionY - paintData.lineHeight < paintData.codeSectionRect.y + paintData.codeSectionRect.height);
//
//        // Draw decoration lines
//        int decorationMode = codeArea.getDecorationMode();
//        if ((decorationMode & CodeArea.DECORATION_PREVIEW_LINE) > 0) {
//            int lineX = codeArea.getPreviewX() - paintData.scrollPosition.getScrollCharPosition() * codeArea.getCharWidth() - paintData.scrollPosition.getScrollCharOffset() - codeArea.getCharWidth() / 2;
//            if (lineX >= paintData.codeSectionRect.x) {
//                g.setForeground(codeArea.getDecorationLineColor());
//                g.drawLine(lineX, paintData.codeSectionRect.y, lineX, paintData.codeSectionRect.y + paintData.codeSectionRect.height);
//            }
//        }
//    }
//    public void paintLineBackground(GC g, int linePositionX, int linePositionY, PaintData paintData) {
//        int renderOffset = paintData.visibleCharStart;
//        ColorsGroup.ColorType renderColorType = null;
//        Color renderColor = null;
//        for (int charOnLine = paintData.visibleCharStart; charOnLine < paintData.visibleCharEnd; charOnLine++) {
//            Section section;
//            int byteOnLine;
//            if (charOnLine >= paintData.previewCharPos && paintData.viewMode != ViewMode.CODE_MATRIX) {
//                byteOnLine = charOnLine - paintData.previewCharPos;
//                section = Section.TEXT_PREVIEW;
//            } else {
//                byteOnLine = codeArea.computeByteOffsetPerCodeCharOffset(charOnLine, false);
//                section = Section.CODE_MATRIX;
//            }
//            boolean sequenceBreak = false;
//
//            ColorsGroup.ColorType colorType = ColorsGroup.ColorType.BACKGROUND;
//            if (paintData.showUnprintableCharacters) {
//                if (paintData.unprintableChars[charOnLine] != ' ') {
//                    colorType = ColorsGroup.ColorType.UNPRINTABLES_BACKGROUND;
//                }
//            }
//
//            Color color = getPositionColor(byteOnLine, charOnLine, section, colorType, paintData);
//            if (renderColorType == null) {
//                renderColorType = colorType;
//                renderColor = color;
//                g.setForeground(color);
//            }
//
//            if (!areSameColors(color, renderColor) || !colorType.equals(renderColorType)) {
//                sequenceBreak = true;
//            }
//            if (sequenceBreak) {
//                if (renderOffset < charOnLine) {
//                    if (renderColor != null) {
//                        renderBackgroundSequence(g, renderOffset, charOnLine, linePositionX, linePositionY, paintData);
//                    }
//                }
//
//                if (!colorType.equals(renderColorType)) {
//                    renderColorType = colorType;
//                }
//                if (!areSameColors(color, renderColor)) {
//                    renderColor = color;
//                    g.setForeground(color);
//                }
//
//                renderOffset = charOnLine;
//            }
//        }
//
//        if (renderOffset < paintData.charsPerLine) {
//            if (renderColor != null) {
//                renderBackgroundSequence(g, renderOffset, paintData.charsPerLine, linePositionX, linePositionY, paintData);
//            }
//        }
//    }
//    public void paintLineText(GC g, int linePositionX, int linePositionY, PaintData paintData) {
//        int positionY = linePositionY + paintData.lineHeight - codeArea.getSubFontSpace();
//
//        int renderOffset = paintData.visibleCharStart;
//        ColorsGroup.ColorType renderColorType = null;
//        Color renderColor = null;
//        for (int charOnLine = paintData.visibleCharStart; charOnLine < paintData.visibleCharEnd; charOnLine++) {
//            Section section;
//            int byteOnLine;
//            if (charOnLine >= paintData.previewCharPos) {
//                byteOnLine = charOnLine - paintData.previewCharPos;
//                section = Section.TEXT_PREVIEW;
//            } else {
//                byteOnLine = codeArea.computeByteOffsetPerCodeCharOffset(charOnLine, false);
//                section = Section.CODE_MATRIX;
//            }
//            boolean sequenceBreak = false;
//            boolean nativeWidth = true;
//
//            int currentCharWidth = 0;
//            ColorsGroup.ColorType colorType = ColorsGroup.ColorType.TEXT;
//            if (paintData.charRenderingMode != CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                char currentChar = ' ';
//                if (paintData.showUnprintableCharacters) {
//                    currentChar = paintData.unprintableChars[charOnLine];
//                    if (currentChar != ' ') {
//                        colorType = ColorsGroup.ColorType.UNPRINTABLES;
//                    }
//                }
//                if (colorType == ColorsGroup.ColorType.TEXT) {
//                    currentChar = paintData.lineChars[charOnLine];
//                }
//                if (currentChar == ' ' && renderOffset == charOnLine) {
//                    renderOffset++;
//                    continue;
//                }
//                if (paintData.charRenderingMode == CodeArea.CharRenderingMode.AUTO && paintData.monospaceFont) {
//                    // Detect if character is in unicode range covered by monospace fonts
//                    if (currentChar > MIN_MONOSPACE_CODE_POINT && (int) currentChar < MAX_MONOSPACE_CODE_POINT
//                            && currentChar != INV_SPACE_CODE_POINT
//                            && currentChar != EXCEPTION1_CODE_POINT && currentChar != EXCEPTION2_CODE_POINT) {
//                        currentCharWidth = paintData.charWidth;
//                    }
//                }
//
//                if (currentCharWidth == 0) {
//                    currentCharWidth = paintData.charWidth(currentChar);
//                    nativeWidth = currentCharWidth == paintData.charWidth;
//                }
//            } else {
//                currentCharWidth = paintData.charWidth;
//                if (paintData.showUnprintableCharacters) {
//                    char currentChar = paintData.unprintableChars[charOnLine];
//                    if (currentChar != ' ') {
//                        colorType = ColorsGroup.ColorType.UNPRINTABLES;
//                        currentCharWidth = paintData.charWidth(currentChar);
//                        nativeWidth = currentCharWidth == paintData.charWidth;
//                    }
//                }
//            }
//
//            Color color = getPositionColor(byteOnLine, charOnLine, section, colorType, paintData);
//            if (renderColorType == null) {
//                renderColorType = colorType;
//                renderColor = color;
//                g.setForeground(color);
//            }
//
//            if (!nativeWidth || !areSameColors(color, renderColor) || !colorType.equals(renderColorType)) {
//                sequenceBreak = true;
//            }
//            if (sequenceBreak) {
//                if (renderOffset < charOnLine) {
//                    renderCharSequence(g, renderOffset, charOnLine, linePositionX, positionY, renderColorType, paintData);
//                }
//
//                if (!colorType.equals(renderColorType)) {
//                    renderColorType = colorType;
//                }
//                if (!areSameColors(color, renderColor)) {
//                    renderColor = color;
//                    g.setForeground(color);
//                }
//
//                if (!nativeWidth) {
//                    renderOffset = charOnLine + 1;
//                    if (paintData.charRenderingMode == CodeArea.CharRenderingMode.TOP_LEFT) {
//                        g.drawString(
//                                String.valueOf(renderColorType == ColorsGroup.ColorType.UNPRINTABLES ? paintData.unprintableChars[charOnLine] : paintData.lineChars[charOnLine]),
//                                linePositionX + charOnLine * paintData.charWidth, positionY);
//                    } else {
//                        drawShiftedChar(g,
//                                renderColorType == ColorsGroup.ColorType.UNPRINTABLES ? paintData.unprintableChars : paintData.lineChars,
//                                charOnLine, paintData.charWidth, linePositionX + charOnLine * paintData.charWidth, positionY, (paintData.charWidth + 1 - currentCharWidth) >> 1);
//                    }
//                } else {
//                    renderOffset = charOnLine;
//                }
//            }
//        }
//
//        if (renderOffset < paintData.charsPerLine) {
//            renderCharSequence(g, renderOffset, paintData.charsPerLine, linePositionX, positionY, renderColorType, paintData);
//        }
//    }
//    /**
//     * Returns color of given type for specified position.
//     *
//     * Child implementation can override this to change rendering colors.
//     *
//     * @param byteOnLine byte on line
//     * @param charOnLine character on line
//     * @param section rendering section
//     * @param colorType color type
//     * @param paintData cached paint data
//     * @return color
//     */
//    public Color getPositionColor(int byteOnLine, int charOnLine, Section section, ColorsGroup.ColorType colorType, PaintData paintData) {
//        long dataPosition = paintData.lineDataPosition + byteOnLine;
//        SelectionRange selection = codeArea.getSelection();
//        if (selection != null && dataPosition >= selection.getFirst() && dataPosition <= selection.getLast() && (section == Section.TEXT_PREVIEW || charOnLine < paintData.charsPerCodeArea)) {
//            Section activeSection = codeArea.getActiveSection();
//            if (activeSection == section) {
//                return codeArea.getSelectionColors().getColor(colorType);
//            } else {
//                return codeArea.getMirrorSelectionColors().getColor(colorType);
//            }
//        }
//        if (colorType == ColorsGroup.ColorType.BACKGROUND) {
//            // Background is prepainted
//            return null;
//        }
//        if (((paintData.backgroundMode == CodeArea.BackgroundPaintMode.STRIPPED || paintData.backgroundMode == CodeArea.BackgroundPaintMode.GRIDDED) && (paintData.line & 1) > 0)
//                || (paintData.backgroundMode == CodeArea.BackgroundPaintMode.GRIDDED && ((byteOnLine & 1) > 0)) && section == Section.CODE_MATRIX) {
//            return codeArea.getAlternateColors().getColor(colorType);
//        }
//
//        return codeArea.getMainColors().getColor(colorType);
//    }
//    /**
//     * Render sequence of characters.
//     *
//     * Doesn't include character at offset end.
//     */
//    private void renderCharSequence(GC g, int startOffset, int endOffset, int linePositionX, int positionY, ColorsGroup.ColorType colorType, PaintData paintData) {
//        if (colorType == ColorsGroup.ColorType.UNPRINTABLES) {
//            g.drawString(String.valueOf(paintData.unprintableChars).substring(startOffset, endOffset), linePositionX + startOffset * paintData.charWidth, positionY);
//        } else {
//            g.drawString(String.valueOf(paintData.lineChars).substring(startOffset, endOffset), linePositionX + startOffset * paintData.charWidth, positionY);
//        }
//    }
//
//    /**
//     * Render sequence of background rectangles.
//     *
//     * Doesn't include character at offset end.
//     */
//    private void renderBackgroundSequence(GC g, int startOffset, int endOffset, int linePositionX, int positionY, PaintData paintData) {
//        g.fillRectangle(linePositionX + startOffset * paintData.charWidth, positionY, (endOffset - startOffset) * paintData.charWidth, paintData.lineHeight);
//    }
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
    public void paintCursor(@Nonnull GC g) {
        if (!worker.getCodeArea().isFocusControl()) {
//            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) worker).getCaret();
        Rectangle cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        Rectangle clipBounds = g.getClipping();
        Rectangle mainAreaRect = getMainAreaRect();
        Rectangle intersection = mainAreaRect.intersection(cursorRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.setClipping(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setForeground(colors.cursor);
            
//            if (renderingMode == DefaultCodeAreaCaret.CursorRenderingMode.XOR) {
//                g.setXORMode(true); // Color.WHITE
//            }

            paintCursorRect(g, intersection.x, intersection.y, intersection.width, intersection.height, renderingMode, caret);
            
//            if (renderingMode == DefaultCodeAreaCaret.CursorRenderingMode.XOR) {
//                throw new UnsupportedOperationException("Not supported yet.");
//                // TODO g.setPaintMode();
//            }
        }

        // Paint shadow cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            Rectangle mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            g.setForeground(colors.cursorMirror);
            if (mirrorCursorRect != null) {
                intersection = mainAreaRect.intersection(mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.setLineStyle(SWT.LINE_DASH);
                    g.drawRectangle(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width, mirrorCursorRect.height);
                }
            }
        }
        g.setClipping(clipBounds);
    }
        
    private void paintCursorRect(@Nonnull GC g, int x, int y, int width, int height, @Nonnull CursorRenderingMode renderingMode, @Nonnull DefaultCodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRectangle(x, y, width, height);
                break;
            }
            case XOR: {
                Rectangle rect = new Rectangle(x, y, width, height);
                Rectangle intersection = rect.intersection(g.getClipping());
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
                int scrolledX = x + scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();
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
                    int posX = previewX + charPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
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
                    int posX = codeRect.x + codeCharPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
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
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY) {
        CodeAreaCaretPosition caret = new CodeAreaCaretPosition();
        if (positionX < rowPositionAreaWidth) {
            positionX = rowPositionAreaWidth;
        }
        int cursorCharX = (positionX - rowPositionAreaWidth + scrollPosition.getScrollCharOffset()) / characterWidth + scrollPosition.getScrollCharPosition();
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        if (positionY < headerAreaHeight) {
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getScrollRowOffset()) / rowHeight + scrollPosition.getScrollRowPosition();
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
                if (startPosition.getScrollRowPosition() == 0) {
                    targetPosition.setScrollRowOffset(0);
                } else {
                    targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() - 1);
                }
                break;
            }
            case DOWN: {
//                if (startPosition.getScrollRowPosition() < rowsPerDocument) {
                targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() + 1);
//                }
                break;
            }
            case LEFT: {
                throw new UnsupportedOperationException("Not supported yet.");
                // break;
            }
            case RIGHT: {
                throw new UnsupportedOperationException("Not supported yet.");
                // break;
            }
            case PAGE_UP: {
                if (startPosition.getScrollRowPosition() < rowsPerPage) {
                    targetPosition.setScrollRowPosition(0);
                    targetPosition.setScrollRowOffset(0);
                } else {
                    targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() - rowsPerPage);
                }
                break;
            }
            case PAGE_DOWN: {
                long rowsPerDocument = dataSize / bytesPerRow;
                if (dataSize % bytesPerRow > 0) {
                    rowsPerDocument++;
                }
                if (startPosition.getScrollRowPosition() <= rowsPerDocument - rowsPerPage * 2) {
                    targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() + rowsPerPage);
                } else if (rowsPerDocument > rowsPerPage) {
                    targetPosition.setScrollRowPosition(rowsPerDocument - rowsPerPage);
                } else {
                    targetPosition.setScrollRowPosition(0);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected scrolling shift type: " + direction.name());
        }

        return targetPosition;
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
        long row = dataPosition / bytesPerRow - scrollPosition.getScrollRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + row * rowHeight) - scrollPosition.getScrollRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            caretX = dataViewRect.x + previewRelativeX + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.x + characterWidth * (computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset();

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
            return SWT.CURSOR_IBEAM;
        }

        return SWT.CURSOR_ARROW;
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
    protected void drawCenteredChar(GC g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        int charWidth = g.textExtent(String.valueOf(drawnChars[charOffset])).x;
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, positionX + (charWidthSpace + 1 - charWidth) >> 1, positionY);
    }

    protected void drawShiftedChar(GC g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        g.drawString(String.valueOf(drawnChars[charOffset]), positionX, positionY);
    }

    private void buildCharMapping(@Nonnull Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private int getRowPositionLength() {
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
    private void renderCharSequence(@Nonnull GC g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        char[] subArray = Arrays.copyOfRange(rowCharacters, startOffset, endOffset - startOffset);
        g.drawString(String.valueOf(subArray), rowPositionX + startOffset * characterWidth, positionY);
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(@Nonnull GC g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        g.fillRectangle(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    private int computeRowsPerRectangle() {
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage() {
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }

    private int computeBytesPerRow() {
        boolean rowWrapping = ((RowWrappingCapable) worker).isRowWrapping();
        int maxBytesPerLine = ((RowWrappingCapable) worker).getMaxBytesPerRow();

        int computedBytesPerRow = 16;
        if (rowWrapping) {
            // TODO
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

    private int computeCharactersPerRectangle() {
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage() {
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    @Override
    public void updateScrollBars() {
        ScrollBar verticalScrollBar = scrollPanel.getVerticalBar();
        ScrollBar horizontalScrollBar = scrollPanel.getHorizontalBar();

        if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
            long rowsPerDocument = (dataSize / bytesPerRow) + 1;
            int scrollValue;
            if (scrollPosition.getScrollCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                scrollValue = (int) ((scrollPosition.getScrollRowPosition() * Integer.MAX_VALUE) / rowsPerDocument);
            } else {
                scrollValue = (int) (scrollPosition.getScrollRowPosition() / (rowsPerDocument / Integer.MAX_VALUE));
            }
            verticalScrollBar.setSelection(scrollValue);
        } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            verticalScrollBar.setSelection((int) scrollPosition.getScrollRowPosition() * rowHeight);
        } else {
            verticalScrollBar.setSelection((int) (scrollPosition.getScrollRowPosition() * rowHeight + scrollPosition.getScrollRowOffset()));
        }

        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            horizontalScrollBar.setSelection(scrollPosition.getScrollCharPosition() * characterWidth);
        } else {
            horizontalScrollBar.setSelection(scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset());
        }
    }

    @Nonnull
    private Rectangle getMainAreaRect() {
        return new Rectangle(rowPositionAreaWidth, headerAreaHeight, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - headerAreaHeight - getHorizontalScrollBarSize());
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
//            int scrollBarValue = scrollPanel.getVerticalBar().getValue();
//            if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
//                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalBar().getVisibleAmount();
//                long rowsPerDocument = (dataSize / bytesPerRow) - computeRowsPerRectangle() + 1;
//                long targetRow;
//                if (scrollBarValue > 0 && rowsPerDocument > maxValue / scrollBarValue) {
//                    targetRow = scrollBarValue * (rowsPerDocument / maxValue);
//                    long rest = rowsPerDocument % maxValue;
//                    targetRow += (rest * scrollBarValue) / maxValue;
//                } else {
//                    targetRow = (scrollBarValue * rowsPerDocument) / Integer.MAX_VALUE;
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
//            worker.getCodeArea().redraw();
////            dataViewScrolled(codeArea.getGraphics());
//            notifyScrolled();
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
//            int scrollBarValue = scrollPanel.getHorizontalBar().getValue();
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
//            worker.getCodeArea().redraw();
////            dataViewScrolled(codeArea.getGraphics());
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent se) {
        }
    }

    private void notifyScrolled() {
        resetScrollState();
        // TODO
    }

    @Override
    public void dispose() {
        colors.dispose();
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

        private void dispose() {
            foreground.dispose();
            background.dispose();
            selectionForeground.dispose();
            selectionBackground.dispose();
            selectionMirrorForeground.dispose();
            selectionMirrorBackground.dispose();
            cursor.dispose();
            negativeCursor.dispose();
// TODO            cursorMirror.dispose();
// TODO           negativeCursorMirror.dispose();
            decorationLine.dispose();
            stripes.dispose();
        }
    }

    /**
     * Enumeration of vertical scalling modes.
     */
    public enum ScrollBarVerticalScale {
        /**
         * Normal ratio 1 on 1.
         */
        NORMAL,
        /**
         * Height is more than available range and scaled.
         */
        SCALED
    }
}
