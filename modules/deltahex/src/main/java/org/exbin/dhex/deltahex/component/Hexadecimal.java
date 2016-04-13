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
package org.exbin.dhex.deltahex.component;

import org.exbin.dhex.deltahex.CaretPosition;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import org.exbin.dhex.deltahex.EditableHexadecimalData;
import org.exbin.dhex.deltahex.HexadecimalData;

/**
 * Hex editor component.
 *
 * @version 0.1.0 2016/04/13
 * @author ExBin Project (http://exbin.org)
 */
public class Hexadecimal extends JComponent {

    private HexadecimalData data;

    private HexadecimalCaret caret;
    private SelectionRange selection;
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();

    private ViewMode viewMode = ViewMode.DUAL;
    private BackgroundMode backgroundMode = BackgroundMode.STRIPPED;
    private DecorationMode decorationMode = DecorationMode.NONE;
    private Section activeSection = Section.HEXADECIMAL;
    private EditationMode editationMode = EditationMode.OVERWRITE;
    private CharRenderingMode charRenderingMode = CharRenderingMode.AUTO;
    private CharAntialiasingMode charAntialiasingMode = CharAntialiasingMode.AUTO;
    private int defaultBytesPerLine = 16;
    private boolean showHeader = true;
    private boolean showLineNumbers = true;
    private boolean mouseDown;
    private boolean editable = true;
    private boolean wrapMode = false;

    private VerticalScrollMode verticalScrollMode = VerticalScrollMode.PER_LINE;
    private HorizontalScrollMode horizontalScrollMode = HorizontalScrollMode.NONE;
    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private ScrollPosition scrollPosition = new ScrollPosition();

    private HexadecimalPainter painter;

    private Color oddForegroundColor;
    private Color oddBackgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    /**
     * Selection background color for selection in currently not active section.
     */
    private Color dualSelectionBackgroundColor;
    private Color cursorColor;
    private int subFontSpace = 3;

    private final DimensionsCache dimensionsCache = new DimensionsCache();

    public Hexadecimal() {
        super();
        caret = new HexadecimalCaret(this);
        painter = new DefaultHexadecimalPainter(this);

        super.setForeground(UIManager.getColor("TextArea.foreground"));
        super.setBackground(UIManager.getColor("TextArea.background"));
        oddBackgroundColor = new Color(240, 240, 240);
        selectionColor = UIManager.getColor("TextArea.selectionForeground");
        selectionBackgroundColor = UIManager.getColor("TextArea.selectionBackground");
        dualSelectionBackgroundColor = Color.LIGHT_GRAY;
        cursorColor = UIManager.getColor("TextArea.caretForeground");

        init();
    }

    private void init() {
        verticalScrollBar = new JScrollBar(Scrollbar.VERTICAL);
        verticalScrollBar.setVisible(false);
//        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
        add(verticalScrollBar);
        horizontalScrollBar = new JScrollBar(Scrollbar.HORIZONTAL);
//        horizontalScrollBar.setIgnoreRepaint(true);
        horizontalScrollBar.setVisible(false);
        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
        add(horizontalScrollBar);

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addComponentListener(new HexComponentListener());

        HexMouseListener hexMouseListener = new HexMouseListener();
        addMouseListener(hexMouseListener);
        addMouseMotionListener(hexMouseListener);
        addKeyListener(new HexKeyListener());
    }

    private void setHalfByte(int value) {
        CaretPosition caretPosition = caret.getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();

        byte byteValue = data.getByte(dataPosition);

        if (caretPosition.isLowerHalf()) {
            byteValue = (byte) ((byteValue & 0xf0) | value);
        } else {
            byteValue = (byte) ((byteValue & 0xf) | (value << 4));
        }

        ((EditableHexadecimalData) data).setByte(dataPosition, byteValue);
    }

    private void moveRight(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() < data.getDataSize()) {
            if (activeSection == Section.HEXADECIMAL) {
                boolean lowerHalf = caret.isLowerHalf();
                if (caretPosition.getDataPosition() < data.getDataSize()) {
                    if (!lowerHalf) {
                        caret.setLowerHalf(true);
                        updateSelection(modifiers, caretPosition);
                    } else {
                        caret.setCaretPosition(caretPosition.getDataPosition() + 1, false);
                        updateSelection(modifiers, caretPosition);
                    }
                    notifyCaretMoved();
                }
            } else {
                caret.setCaretPosition(caretPosition.getDataPosition() + 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            }
        }
    }

    private void moveCaret(MouseEvent me, int modifiers) {
        Point scrollPoint = getScrollPoint();
        int bytesPerLine = dimensionsCache.bytesPerLine;
        int cursorCharX = (me.getX() + scrollPoint.x) / dimensionsCache.charWidth;
        Rectangle rect = dimensionsCache.hexadecimalRectangle;
        int cursorY = (me.getY() - rect.y + scrollPoint.y) / dimensionsCache.lineHeight;
        if (cursorY < 0) {
            cursorY = 0;
        }
        if (showLineNumbers) {
            if (cursorCharX < 9) {
                cursorCharX = 0;
            } else {
                cursorCharX -= 9;
            }
        }

        long dataPosition;
        boolean lowerHalf = false;
        if (cursorCharX < bytesPerLine * 3 || viewMode == ViewMode.HEXADECIMAL) {
            setActiveSection(Section.HEXADECIMAL);
            int bytePosition = cursorCharX % 3;
            lowerHalf = bytePosition > 0;

            int cursorX = cursorCharX / 3;
            int byteOnLine = cursorX;
            if (byteOnLine >= bytesPerLine) {
                byteOnLine = bytesPerLine - 1;
            }
            dataPosition = byteOnLine + (cursorY * bytesPerLine);
        } else {
            setActiveSection(Section.PREVIEW);
            int byteOnLine = (cursorCharX - (bytesPerLine * 3));
            if (byteOnLine >= bytesPerLine) {
                byteOnLine = bytesPerLine - 1;
            }
            dataPosition = byteOnLine + (cursorY * bytesPerLine);
            if (dataPosition > data.getDataSize()) {
                dataPosition = data.getDataSize();
            }
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, lowerHalf);
        notifyCaretMoved();

        updateSelection(modifiers, caretPosition);
    }

    public Point getScrollPoint() {
        return new Point((int) scrollPosition.scrollBytePosition * dimensionsCache.charWidth + scrollPosition.scrollByteOffset, (int) scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset);
    }

    public ScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (charAntialiasingMode != CharAntialiasingMode.OFF && g instanceof Graphics2D) {
            Object antialiasingHint;
            switch (charAntialiasingMode) {
                case AUTO: {
                    // TODO detect if display is LCD?
                    if (((Graphics2D) g).getDeviceConfiguration().getDevice().getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                        antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                    } else {
                        antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
                    }
                    break;
                }
                case BASIC: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
                    break;
                }
                case GASP: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
                    break;
                }
                case DEFAULT: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
                    break;
                }
                case LCD_HRGB: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                    break;
                }
                case LCD_HBGR: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
                    break;
                }
                case LCD_VRGB: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
                    break;
                }
                case LCD_VBGR: {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected antialiasing type " + charAntialiasingMode.name());
                }
            }
            ((Graphics2D) g).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    antialiasingHint);
        }

        super.paintComponent(g);
        g.setFont(getFont());

        if (dimensionsCache.fontMetrics == null) {
            computeFontMetrics();
        }

        int bytesPerLine = dimensionsCache.bytesPerLine;
        Rectangle clipBounds = g.getClipBounds();

        int positionY = dimensionsCache.lineHeight;
        Rectangle rect = dimensionsCache.hexadecimalRectangle;
//        int linePositionX = dimensionsCache.hexadecimalX - scrollByteOffset;
        if (showHeader) {
            if (viewMode != ViewMode.PREVIEW && clipBounds.y < rect.y) {
                painter.paintHeader(g, positionY, bytesPerLine, dimensionsCache.charWidth);
            }
        }

        if (showLineNumbers) {
            g.setClip(0, rect.y, clipBounds.width, clipBounds.height);
            painter.paintLineNumbers(g, bytesPerLine, WIDTH);
        }

        g.setClip(rect.x, rect.y, clipBounds.width, clipBounds.height);
//        clipBounds.x, dimensionsCache.hexadecimalY, clipBounds.width, clipBounds.height + clipBounds.y + dimensionsCache.hexadecimalY);

        painter.paintHexadecimal(g, rect.x, rect.y, bytesPerLine, dimensionsCache.lineHeight, dimensionsCache.charWidth);
        // Render hexadecimal part
//        positionY += dimensionsCache.hexadecimalY - scrollLineOffset;
//        long line = scrollLinePosition;
//        int byteOnLine = 0;
//        long dataPosition = line * bytesPerLine;
//        long dataSize = data.getDataSize();
//        do {
//            if (byteOnLine == 0) {
//                painter.paintBackground(g, line, positionY, dataPosition, bytesPerLine, dimensionsCache.lineHeight, dimensionsCache.charWidth);
//            }
//
//            if (dataPosition < dataSize || (dataPosition == dataSize && byteOnLine == 0)) {
//                painter.paintText(g, line, linePositionX, byteOnLine, positionY, dataPosition, bytesPerLine, dimensionsCache.lineHeight, dimensionsCache.charWidth);
//            } else {
//                break;
//            }
//
//            byteOnLine++;
//            dataPosition++;
//
//            if (byteOnLine == bytesPerLine) {
//                byteOnLine = 0;
//                positionY += dimensionsCache.lineHeight;
//                line++;
//            }
//        } while (positionY - dimensionsCache.lineHeight < clipBounds.y + clipBounds.height);

        caret.paint(g, bytesPerLine, dimensionsCache.lineHeight, dimensionsCache.charWidth);
    }

    public void revealCursor() {
        boolean scrolled = false;
        CaretPosition caretPosition = caret.getCaretPosition();
        long caretLine = caretPosition.getDataPosition() / dimensionsCache.bytesPerLine;
        int caretByte = (int) (caretPosition.getDataPosition() % dimensionsCache.bytesPerLine);

        if (caretLine <= scrollPosition.scrollLinePosition) {
            scrollPosition.scrollLinePosition = caretLine;
            scrollPosition.scrollLineOffset = 0;
            scrolled = true;
        } else if (caretLine >= scrollPosition.scrollLinePosition + dimensionsCache.linesPerBounds) {
            scrollPosition.scrollLinePosition = caretLine - dimensionsCache.linesPerBounds + 1;
            scrollPosition.scrollLineOffset = 0; // TODO
            scrolled = true;
        }
        if (caretByte <= scrollPosition.scrollBytePosition) {
            scrollPosition.scrollBytePosition = caretByte;
            scrollPosition.scrollByteOffset = 0;
            scrolled = true;
        } else if (caretByte >= scrollPosition.scrollBytePosition + dimensionsCache.bytesPerLine) {
            scrollPosition.scrollBytePosition = caretByte - dimensionsCache.bytesPerLine;
            scrollPosition.scrollByteOffset = 0; // TODO
            scrolled = true;
        }

        if (scrolled) {
            updateScrollBars();
        }
    }

    private void updateScrollBars() {
        if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            verticalScrollBar.setValue((int) scrollPosition.scrollLinePosition);
        } else {
            verticalScrollBar.setValue((int) (scrollPosition.scrollLinePosition * dimensionsCache.lineHeight + scrollPosition.scrollLineOffset));
        }

        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            horizontalScrollBar.setValue((int) scrollPosition.scrollBytePosition);
        } else {
            horizontalScrollBar.setValue((int) (scrollPosition.scrollBytePosition * dimensionsCache.charWidth * 3 + scrollPosition.scrollByteOffset));
        }
        repaint();
    }

    private void updateSelection(int modifiers, CaretPosition caretPosition) {
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0) {
            if (selection != null) {
                selection.end = caret.getDataPosition();
            } else {
                selection = new SelectionRange(caretPosition.getDataPosition(), caret.getDataPosition());
            }
            notifySelectionChanged();
        } else {
            clearSelection();
        }
        repaint();
    }

    public SelectionRange getSelection() {
        return selection;
    }

    public void selectAll() {
        selection = new SelectionRange(0, data.getDataSize());
        notifySelectionChanged();
        repaint();
    }

    public void clearSelection() {
        selection = null;
        notifySelectionChanged();
        repaint();
    }

    private void notifySelectionChanged() {
        for (SelectionChangedListener selectionChangedListener : selectionChangedListeners) {
            selectionChangedListener.selectionChanged(selection);
        }
    }

    public boolean hasSelection() {
        return selection != null && (selection.begin != selection.end);
    }

    public void setSelection(SelectionRange selection) {
        this.selection = selection;
        notifySelectionChanged();
    }

    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    private void notifyCaretMoved() {
        for (CaretMovedListener caretMovedListener : caretMovedListeners) {
            caretMovedListener.caretMoved(caret.getCaretPosition(), activeSection);
        }
    }

    public void addCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    public void removeCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    /**
     * Returns main hexadecimal area rectangle.
     *
     * @return rectangle of main hexadecimal area
     */
    public Rectangle getHexadecimalRectangle() {
        return dimensionsCache.hexadecimalRectangle;
    }

    /**
     * Returns X start position of the ascii preview area.
     *
     * @return X position or -1 if area not present
     */
    public int getPreviewX() {
        return dimensionsCache.previewX;
    }

    public int getLineHeight() {
        return dimensionsCache.lineHeight;
    }

    public HexadecimalData getData() {
        return data;
    }

    public void setData(HexadecimalData data) {
        this.data = data;
        computeDimensions();
        repaint();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        computeFontMetrics();
    }

    private void computeFontMetrics() {
        Graphics g = getGraphics();
        if (g != null) {
            Font font = getFont();
            dimensionsCache.fontMetrics = g.getFontMetrics(font);
            /**
             * Use small 'w' character to guess normal font width.
             */
            dimensionsCache.charWidth = dimensionsCache.fontMetrics.charWidth('w');
            /**
             * Compare it to small 'i' to detect if font is monospaced.
             *
             * TODO: Is there better way?
             */
            dimensionsCache.monospaced = dimensionsCache.charWidth == dimensionsCache.fontMetrics.charWidth('i');
            int fontHeight = font.getSize();
            dimensionsCache.lineHeight = fontHeight + subFontSpace;
            computeDimensions();
        }
    }

    private void computeDimensions() {
        if (dimensionsCache.fontMetrics == null) {
            return;
        }

        int charsPerByte;
        switch (viewMode) {
            case HEXADECIMAL: {
                charsPerByte = 3;
                break;
            }
            case PREVIEW: {
                charsPerByte = 1;
                break;
            }
            case DUAL: {
                charsPerByte = 4;
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected viewMode " + viewMode.name());
            }
        }

        boolean verticalScrollBarVisible;
        boolean horizontalScrollBarVisible;

        Rectangle panelBounds = getBounds();
        int charsPerPanel = computeCharsPerPanel(panelBounds, 0);
        int bytesPerLine = charsPerPanel / charsPerByte;
        int lines = (int) (data.getDataSize() / bytesPerLine);

        Rectangle rect = dimensionsCache.hexadecimalRectangle;
        rect.y = showHeader ? dimensionsCache.lineHeight * 2 : 0;
        dimensionsCache.linesPerBounds = (panelBounds.height - rect.y) / dimensionsCache.lineHeight;

        verticalScrollBarVisible = verticalScrollMode != VerticalScrollMode.NONE && lines > dimensionsCache.linesPerBounds;
        if (verticalScrollBarVisible) {
            charsPerPanel = computeCharsPerPanel(panelBounds, dimensionsCache.scrollBarThickness);
            bytesPerLine = charsPerPanel / charsPerByte;
            lines = (int) (data.getDataSize() / bytesPerLine);
        }

        dimensionsCache.bytesPerLine = wrapMode ? (bytesPerLine > 0 ? bytesPerLine : 1) : defaultBytesPerLine;

        horizontalScrollBarVisible = horizontalScrollMode != HorizontalScrollMode.NONE && true; // dimensionsCache.bytesPerLine * charsPerByte > ;
        if (horizontalScrollBarVisible) {
            charsPerPanel = computeCharsPerPanel(panelBounds, dimensionsCache.scrollBarThickness);
            bytesPerLine = charsPerPanel / charsPerByte;
            lines = (int) (data.getDataSize() / bytesPerLine);
        }

        if (viewMode == ViewMode.PREVIEW) {
            rect.x = -1;
        } else {
            rect.x = 0;
            if (showLineNumbers) {
                rect.x += dimensionsCache.charWidth * 9;
            }
        }

        if (viewMode == ViewMode.HEXADECIMAL) {
            dimensionsCache.previewX = -1;
        } else {
            if (viewMode == ViewMode.PREVIEW) {
                dimensionsCache.previewX = 0;
            } else {
                dimensionsCache.previewX = dimensionsCache.bytesPerLine * dimensionsCache.charWidth * 3;
            }
            if (showLineNumbers) {
                dimensionsCache.previewX += dimensionsCache.charWidth * 9;
            }
        }

        Rectangle bounds = getBounds();
        verticalScrollBar.setVisible(verticalScrollBarVisible);
        if (verticalScrollBarVisible) {
            int verticalScrollBarHeight = bounds.height - rect.y;
            if (horizontalScrollBarVisible) {
                verticalScrollBarHeight -= dimensionsCache.scrollBarThickness + 1;
            }
            verticalScrollBar.setBounds(bounds.width - dimensionsCache.scrollBarThickness, rect.y, dimensionsCache.scrollBarThickness, verticalScrollBarHeight);
            int verticalMaximum = lines;
            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                verticalMaximum *= dimensionsCache.lineHeight;
            }
            verticalScrollBar.setMaximum(verticalMaximum);
        }

        horizontalScrollBar.setVisible(horizontalScrollBarVisible);
        if (horizontalScrollBarVisible) {
            horizontalScrollBar.setBounds(rect.x, bounds.height - dimensionsCache.scrollBarThickness, bounds.width - rect.x - dimensionsCache.scrollBarThickness + 1, dimensionsCache.scrollBarThickness);
        }
    }

    private int computeCharsPerPanel(Rectangle panelBounds, int rightSpace) {
        int width = panelBounds.width - rightSpace;
        if (showLineNumbers) {
            width -= dimensionsCache.charWidth * 9;
        }

        return width / dimensionsCache.charWidth;
    }

    public Color getOddBackgroundColor() {
        return oddBackgroundColor;
    }

    public void setBackgroundColor(Color oddBackgroundColor) {
        this.oddBackgroundColor = oddBackgroundColor;
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        this.selectionBackgroundColor = selectionBackgroundColor;
    }

    public Color getDualSelectionBackgroundColor() {
        return dualSelectionBackgroundColor;
    }

    public void setDualSelectionBackgroundColor(Color dualSelectionBackgroundColor) {
        this.dualSelectionBackgroundColor = dualSelectionBackgroundColor;
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        computeDimensions();
        repaint();
    }

    public BackgroundMode getBackgroundMode() {
        return backgroundMode;
    }

    public void setBackgroundMode(BackgroundMode backgroundMode) {
        this.backgroundMode = backgroundMode;
        repaint();
    }

    public DecorationMode getDecorationMode() {
        return decorationMode;
    }

    public void setDecorationMode(DecorationMode decorationMode) {
        this.decorationMode = decorationMode;
        repaint();
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }

    public void setSubFontSpace(int subFontSpace) {
        this.subFontSpace = subFontSpace;
    }

    public Section getActiveSection() {
        return activeSection;
    }

    public void setActiveSection(Section activeSection) {
        this.activeSection = activeSection;
        repaint();
    }

    public EditationMode getEditationMode() {
        return editationMode;
    }

    public void setEditationMode(EditationMode editationMode) {
        this.editationMode = editationMode;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        computeDimensions();
        repaint();
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
        computeDimensions();
        repaint();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        repaint();
    }

    public boolean isWrapMode() {
        return wrapMode;
    }

    public void setWrapMode(boolean wrapMode) {
        this.wrapMode = wrapMode;
        computeDimensions();
        repaint();
    }

    public int getBytesPerLine() {
        return defaultBytesPerLine;
    }

    public void setBytesPerLine(int bytesPerLine) {
        this.defaultBytesPerLine = bytesPerLine;
        if (!wrapMode) {
            computeDimensions();
            repaint();
        }
    }

    public CharRenderingMode getCharRenderingMode() {
        return charRenderingMode;
    }

    public boolean isCharFixedMode() {
        return charRenderingMode == CharRenderingMode.FIXED || (charRenderingMode == CharRenderingMode.AUTO && dimensionsCache.monospaced);
    }

    public void setCharRenderingMode(CharRenderingMode charRenderingMode) {
        this.charRenderingMode = charRenderingMode;
        computeFontMetrics();
        repaint();
    }

    public CharAntialiasingMode getCharAntialiasingMode() {
        return charAntialiasingMode;
    }

    public void setCharAntialiasingMode(CharAntialiasingMode charAntialiasingMode) {
        this.charAntialiasingMode = charAntialiasingMode;
        repaint();
    }

    public VerticalScrollMode getVerticalScrollMode() {
        return verticalScrollMode;
    }

    public void setVerticalScrollMode(VerticalScrollMode verticalScrollMode) {
        this.verticalScrollMode = verticalScrollMode;
        computeDimensions();
        updateScrollBars();
    }

    public HorizontalScrollMode getHorizontalScrollMode() {
        return horizontalScrollMode;
    }

    public void setHorizontalScrollMode(HorizontalScrollMode horizontalScrollMode) {
        this.horizontalScrollMode = horizontalScrollMode;
        computeDimensions();
        updateScrollBars();
    }

    /**
     * Selection range is selection between two points where begin represents
     * originating point. End of the selection can be before or after begin.
     */
    public static class SelectionRange {

        private long begin;
        private long end;

        public SelectionRange() {
        }

        public SelectionRange(long begin, long end) {
            this.begin = begin;
            this.end = end;
        }

        public long getBegin() {
            return begin;
        }

        public void setBegin(long begin) {
            this.begin = begin;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public long getSelectionFirst() {
            return end > begin ? begin : end;
        }

        public long getSelectionLast() {
            return end > begin ? end : begin;
        }
    }

    /**
     * Selection change listener.
     *
     * Event is fired each time selection range changes or selection is created
     * or cleared.
     */
    public interface SelectionChangedListener {

        void selectionChanged(SelectionRange selection);
    }

    /**
     * Caret moved listener.
     *
     * Event is fired each time caret is moved or section is changed.
     */
    public interface CaretMovedListener {

        void caretMoved(CaretPosition caretPosition, Section section);
    }

    public static enum ViewMode {
        HEXADECIMAL, PREVIEW, DUAL
    }

    public static enum BackgroundMode {
        PLAIN, STRIPPED, GRIDDED
    }

    public static enum DecorationMode {
        NONE, LINES, BOX
    }

    public static enum Section {
        HEXADECIMAL, PREVIEW
    }

    public static enum EditationMode {
        OVERWRITE, INSERT, READ_ONLY
    }

    public static enum VerticalScrollMode {
        NONE, PER_LINE, PIXEL
    }

    public static enum HorizontalScrollMode {
        NONE, PER_CHAR, PIXEL
    }

    public static enum CharRenderingMode {
        AUTO, DYNAMIC, FIXED
    }

    public static enum CharAntialiasingMode {
        OFF, AUTO, DEFAULT, BASIC, GASP, LCD_HRGB, LCD_HBGR, LCD_VRGB, LCD_VBGR
    }

    /**
     * Precomputed dimensions for the hexadecimal editor.
     */
    private static class DimensionsCache {

        FontMetrics fontMetrics = null;
        int charWidth;
        int lineHeight;
        boolean monospaced = false;

        final Rectangle hexadecimalRectangle = new Rectangle();
        int previewX;
        int bytesPerLine;
        int linesPerBounds;
        int scrollBarThickness = 17;
    }

    public static class ScrollPosition {

        long scrollLinePosition = 0;
        int scrollLineOffset = 0;
        int scrollBytePosition = 0;
        int scrollByteOffset = 0;
    }

    private class HexMouseListener extends MouseAdapter implements MouseMotionListener {

        private Cursor currentCursor = getCursor();
        private final Cursor defaultCursor = Cursor.getDefaultCursor();
        private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

        @Override
        public void mousePressed(MouseEvent me) {
            requestFocus();
            if (me.getButton() == MouseEvent.BUTTON1) {
                moveCaret(me, 0);
                revealCursor();
            }
            mouseDown = true;
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            mouseDown = false;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            currentCursor = defaultCursor;
            setCursor(defaultCursor);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            updateMouseCursor(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            updateMouseCursor(e);
        }

        private void updateMouseCursor(MouseEvent e) {
            Cursor newCursor = defaultCursor;
            Rectangle rect = dimensionsCache.hexadecimalRectangle;
            if (e.getX() >= rect.x && e.getY() >= rect.y) {
                newCursor = textCursor;
            }

            if (newCursor != currentCursor) {
                currentCursor = newCursor;
                setCursor(newCursor);
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            updateMouseCursor(me);
            if (mouseDown) {
                moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
                revealCursor();
            }
        }
    }

    private class HexKeyListener extends KeyAdapter {

        public HexKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    if (activeSection == Section.HEXADECIMAL) {
                        boolean lowerHalf = caret.isLowerHalf();
                        if (lowerHalf) {
                            caret.setLowerHalf(false);
                            updateSelection(e.getModifiersEx(), caretPosition);
                        } else if (caretPosition.getDataPosition() > 0) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - 1, true);
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                    } else if (caretPosition.getDataPosition() > 0) {
                        caret.setCaretPosition(caretPosition.getDataPosition() - 1);
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    notifyCaretMoved();
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    moveRight(e.getModifiersEx());
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_UP: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesPerLine) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesPerLine, caret.isLowerHalf());
                            notifyCaretMoved();
                        }
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesPerLine < dataSize
                                || (caretPosition.getDataPosition() + bytesPerLine == dataSize && !caretPosition.isLowerHalf())) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesPerLine, caret.isLowerHalf());
                            notifyCaretMoved();
                        }
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_HOME: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    if (caretPosition.getDataPosition() > 0 || caret.isLowerHalf()) {
                        long targetPosition;
                        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                            targetPosition = 0;
                        } else {
                            targetPosition = (caretPosition.getDataPosition() / bytesPerLine) * bytesPerLine;
                        }
                        caret.setCaretPosition(targetPosition);
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_END: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = dimensionsCache.bytesPerLine;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                            caret.setCaretPosition(data.getDataSize());
                        } else if (activeSection == Section.HEXADECIMAL) {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize, true);
                        } else {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
                        }
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    break;
                }
                case KeyEvent.VK_PAGE_UP: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesStep = dimensionsCache.bytesPerLine * dimensionsCache.linesPerBounds;
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesStep) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesStep, caret.isLowerHalf());
                        } else if (caretPosition.getDataPosition() >= dimensionsCache.bytesPerLine) {
                            caret.setCaretPosition(caretPosition.getDataPosition() % dimensionsCache.bytesPerLine, caret.isLowerHalf());
                        }
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    if (scrollPosition.scrollLinePosition > dimensionsCache.linesPerBounds) {
                        scrollPosition.scrollLinePosition -= dimensionsCache.linesPerBounds;
                    }
                    revealCursor();
                    updateScrollBars();
                    break;
                }
                case KeyEvent.VK_PAGE_DOWN: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesStep = dimensionsCache.bytesPerLine * dimensionsCache.linesPerBounds;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesStep < dataSize) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesStep, caret.isLowerHalf());
                        } else if (caretPosition.getDataPosition() + dimensionsCache.bytesPerLine < dataSize) {
                            caret.setCaretPosition(dataSize
                                    - (dataSize % dimensionsCache.bytesPerLine)
                                    + (caretPosition.getDataPosition() % dimensionsCache.bytesPerLine), caret.isLowerHalf());
                        }
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    if (scrollPosition.scrollLinePosition < data.getDataSize() / dimensionsCache.bytesPerLine - dimensionsCache.linesPerBounds * 2) {
                        scrollPosition.scrollLinePosition += dimensionsCache.linesPerBounds;
                    }
                    revealCursor();
                    updateScrollBars();
                    break;
                }
                case KeyEvent.VK_INSERT: {
                    if (editationMode != EditationMode.READ_ONLY) {
                        setEditationMode(editationMode == EditationMode.INSERT ? EditationMode.OVERWRITE : EditationMode.INSERT);
                        repaint();
                    }
                    break;
                }
                case KeyEvent.VK_TAB: {
                    activeSection = activeSection == Section.HEXADECIMAL ? Section.PREVIEW : Section.HEXADECIMAL;
                    if (activeSection == Section.PREVIEW) {
                        caret.setLowerHalf(false);
                    }
                    repaint();
                    break;
                }
                default: {
                    if (activeSection == Section.HEXADECIMAL) {
                        if ((e.getKeyChar() >= '0' && e.getKeyChar() <= '9')
                                || (e.getKeyChar() >= 'a' && e.getKeyChar() <= 'f')) {
                            int value;
                            if (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') {
                                value = e.getKeyChar() - '0';
                            } else {
                                value = e.getKeyChar() - 'a' + 10;
                            }
                            setHalfByte(value);
                            moveRight(0);
                            revealCursor();
                        }
                    } else {
                        char keyChar = e.getKeyChar();
                        if (keyChar > 31 && keyChar < 255) {
                            CaretPosition caretPosition = caret.getCaretPosition();
                            long dataPosition = caretPosition.getDataPosition();
                            ((EditableHexadecimalData) data).setByte(dataPosition, (byte) keyChar);
                            moveRight(0);
                            revealCursor();
                        }
                    }
                }
            }
        }
    }

    private class HexComponentListener implements ComponentListener {

        public HexComponentListener() {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            computeDimensions();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
                scrollPosition.scrollLinePosition = verticalScrollBar.getValue();
            } else {
                scrollPosition.scrollLinePosition = verticalScrollBar.getValue() / dimensionsCache.lineHeight;
                scrollPosition.scrollLineOffset = verticalScrollBar.getValue() % dimensionsCache.lineHeight;
            }
            repaint();
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
                scrollPosition.scrollBytePosition = horizontalScrollBar.getValue();
            } else {
                scrollPosition.scrollBytePosition = horizontalScrollBar.getValue() / dimensionsCache.charWidth;
                scrollPosition.scrollByteOffset = horizontalScrollBar.getValue() % dimensionsCache.charWidth;
            }
            repaint();
        }
    }
}
