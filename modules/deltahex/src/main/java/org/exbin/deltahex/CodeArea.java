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
package org.exbin.deltahex;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * Also supports binary, octal and decimal codes.
 *
 * @version 0.1.1 2016/08/02
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends JComponent {

    public static final int NO_MODIFIER = 0;
    public static final int DECORATION_HEADER_LINE = 1;
    public static final int DECORATION_LINENUM_LINE = 2;
    public static final int DECORATION_PREVIEW_LINE = 4;
    public static final int DECORATION_BOX = 8;
    public static final int DECORATION_DEFAULT = DECORATION_PREVIEW_LINE | DECORATION_LINENUM_LINE | DECORATION_HEADER_LINE;
    public static final int MOUSE_SCROLL_LINES = 3;

    private int metaMask;

    private BinaryData data;
    private CodeAreaPainter painter;
    private CodeAreaCommandHandler commandHandler;
    private CodeAreaCaret caret;
    private SelectionRange selection;

    private ViewMode viewMode = ViewMode.DUAL;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;
    private BackgroundMode backgroundMode = BackgroundMode.STRIPPED;
    private Charset charset = Charset.defaultCharset();
    private int decorationMode = DECORATION_DEFAULT;
    private EditationMode editationMode = EditationMode.OVERWRITE;
    private CharRenderingMode charRenderingMode = CharRenderingMode.AUTO;
    private CharAntialiasingMode charAntialiasingMode = CharAntialiasingMode.AUTO;
    private HexCharactersCase hexCharactersCase = HexCharactersCase.UPPER;
    private final CodeAreaSpace headerSpace = new CodeAreaSpace(CodeAreaSpace.SpaceType.HALF_UNIT);
    private final CodeAreaSpace lineNumberSpace = new CodeAreaSpace();
    private final CodeAreaLineNumberLength lineNumberLength = new CodeAreaLineNumberLength();

    private int lineLength = 16;
    private int subFontSpace = 3;
    private boolean showHeader = true;
    private boolean showLineNumbers = true;
    private boolean mouseDown;
    private boolean editable = true;
    private boolean wrapMode = false;
    private boolean handleClipboard = true;
    private boolean showUnprintableCharacters = false;
    private boolean showShadowCursor = true;

    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollMode verticalScrollMode = VerticalScrollMode.PER_LINE;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollMode horizontalScrollMode = HorizontalScrollMode.PIXEL;
    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private ScrollPosition scrollPosition = new ScrollPosition();

    /**
     * Component colors.
     *
     * Parent foreground and background are used for header and line numbers
     * section.
     */
    private final ColorsGroup mainColors = new ColorsGroup();
    private final ColorsGroup alternateColors = new ColorsGroup();
    private final ColorsGroup selectionColors = new ColorsGroup();
    private final ColorsGroup mirrorSelectionColors = new ColorsGroup();
    private Color cursorColor;
    private Color decorationLineColor;

    /**
     * Listeners.
     */
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();
    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();

    private final PaintDataCache paintDataCache = new PaintDataCache();

    public CodeArea() {
        super();
        caret = new CodeAreaCaret(this);
        painter = new DefaultCodeAreaPainter(this);
        commandHandler = new DefaultCodeAreaCommandHandler(this);

        try {
            metaMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMask = java.awt.Event.CTRL_MASK;
        }
        init();
    }

    private void init() {
        Color textColor = UIManager.getColor("TextArea.foreground");
        Color backgroundColor = UIManager.getColor("TextArea.background");
        super.setForeground(textColor);
        super.setBackground(createOddColor(backgroundColor));
        Color unprintablesColor = new Color(textColor.getRed(), (textColor.getGreen() + 128) % 256, textColor.getBlue());
        mainColors.setTextColor(textColor);
        mainColors.setBothBackgroundColors(backgroundColor);
        mainColors.setUnprintablesColor(unprintablesColor);
        alternateColors.setTextColor(textColor);
        alternateColors.setBothBackgroundColors(createOddColor(backgroundColor));
        alternateColors.setUnprintablesColor(unprintablesColor);
        Color selectionTextColor = UIManager.getColor("TextArea.selectionForeground");
        Color selectionBackgroundColor = UIManager.getColor("TextArea.selectionBackground");
        selectionColors.setTextColor(selectionTextColor);
        selectionColors.setBothBackgroundColors(selectionBackgroundColor);
        selectionColors.setUnprintablesColor(unprintablesColor);
        mirrorSelectionColors.setTextColor(selectionTextColor);
        int grayLevel = (selectionBackgroundColor.getRed() + selectionBackgroundColor.getGreen() + selectionBackgroundColor.getBlue()) / 3;
        mirrorSelectionColors.setBothBackgroundColors(new Color(grayLevel, grayLevel, grayLevel));
        mirrorSelectionColors.setUnprintablesColor(unprintablesColor);

        cursorColor = UIManager.getColor("TextArea.caretForeground");
        decorationLineColor = Color.GRAY;

        verticalScrollBar = new JScrollBar(Scrollbar.VERTICAL);
        verticalScrollBar.setVisible(false);
        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
        add(verticalScrollBar);
        horizontalScrollBar = new JScrollBar(Scrollbar.HORIZONTAL);
        horizontalScrollBar.setIgnoreRepaint(true);
        horizontalScrollBar.setVisible(false);
        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
        add(horizontalScrollBar);

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addComponentListener(new CodeAreaComponentListener());

        CodeAreaMouseListener codeAreaMouseListener = new CodeAreaMouseListener();
        addMouseListener(codeAreaMouseListener);
        addMouseMotionListener(codeAreaMouseListener);
        addMouseWheelListener(codeAreaMouseListener);
        addKeyListener(new CodeAreaKeyListener());
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        Insets insets = getInsets();
        Dimension size = getSize();
        Rectangle compRect = new Rectangle();
        compRect.x = insets.left;
        compRect.y = insets.top;
        compRect.width = size.width - insets.left - insets.right;
        compRect.height = size.height - insets.top - insets.bottom;
        if (!paintDataCache.componentRectangle.equals(compRect)) {
            computePaintData();
        }

        Rectangle clipBounds = g.getClipBounds();
        if (charAntialiasingMode != CharAntialiasingMode.OFF && g instanceof Graphics2D) {
            Object antialiasingHint = getAntialiasingHint((Graphics2D) g);
            ((Graphics2D) g).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    antialiasingHint);
        }

        if (paintDataCache.fontMetrics == null) {
            computeFontMetrics();
        }

        painter.paintOverall(g);
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        if (showHeader) {
            g.setClip(clipBounds.createIntersection(new Rectangle(hexRect.x, 0, hexRect.width, hexRect.y)));
            painter.paintHeader(g);
        }

        g.setClip(clipBounds.createIntersection(new Rectangle(0, hexRect.y, hexRect.x + hexRect.width, hexRect.height)));
        painter.paintBackground(g);
        if (showLineNumbers) {
            painter.paintLineNumbers(g);
            g.setClip(clipBounds.createIntersection(new Rectangle(hexRect.x, hexRect.y, hexRect.width, hexRect.height)));
        }

        painter.paintMainArea(g);

        if (hasFocus()) {
            caret.paint(g);
        }
        g.setClip(clipBounds);
    }

    private Object getAntialiasingHint(Graphics2D g) {
        Object antialiasingHint;
        switch (charAntialiasingMode) {
            case AUTO: {
                // TODO detect if display is LCD?
                if (g.getDeviceConfiguration().getDevice().getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
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

        return antialiasingHint;
    }

    public CodeAreaCaret getCaret() {
        return caret;
    }

    private void moveCaret(MouseEvent me, int modifiers) {
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        int codeDigits = getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        Point scrollPoint = getScrollPoint();
        int bytesPerLine = paintDataCache.bytesPerLine;
        int cursorCharX = (me.getX() - hexRect.x + scrollPoint.x) / paintDataCache.charWidth;
        int cursorLineY = (me.getY() - hexRect.y + scrollPoint.y) / paintDataCache.lineHeight;
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnLine;
        if ((viewMode == ViewMode.DUAL && cursorCharX < bytesPerLine * charsPerByte) || viewMode == ViewMode.CODE_MATRIX) {
            caret.setSection(Section.CODE_MATRIX);
            int bytePosition = cursorCharX % (codeType.maxDigits + 1);
            codeOffset = bytePosition;
            if (codeOffset >= codeType.maxDigits) {
                codeOffset = codeType.maxDigits - 1;
            }

            byteOnLine = cursorCharX / (codeType.maxDigits + 1);
            if (byteOnLine >= bytesPerLine) {
                codeOffset = 0;
            }
        } else {
            caret.setSection(Section.TEXT_PREVIEW);
            byteOnLine = cursorCharX;
            if (viewMode == ViewMode.DUAL) {
                byteOnLine -= bytesPerLine * charsPerByte;
            }
        }

        if (byteOnLine >= bytesPerLine) {
            byteOnLine = bytesPerLine - 1;
        }

        dataPosition = byteOnLine + (cursorLineY * bytesPerLine);
        long dataSize = data.getDataSize();
        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, codeOffset);
        notifyCaretMoved();
        commandHandler.caretMoved();

        updateSelection(modifiers, caretPosition);
    }

    private void notifyCaretMoved() {
        for (CaretMovedListener caretMovedListener : caretMovedListeners) {
            caretMovedListener.caretMoved(caret.getCaretPosition(), caret.getSection());
        }
    }

    public void notifyScrolled() {
        for (ScrollingListener scrollingListener : scrollingListeners) {
            scrollingListener.scrolled();
        }
    }

    public void notifyDataChanged() {
        if (caret.getDataPosition() > data.getDataSize()) {
            caret.setCaretPosition(0);
            notifyCaretMoved();
        }

        for (DataChangedListener dataChangedListener : dataChangedListeners) {
            dataChangedListener.dataChanged();
        }
    }

    public Point getScrollPoint() {
        return new Point(scrollPosition.scrollCharPosition * paintDataCache.charWidth + scrollPosition.scrollCharOffset, (int) scrollPosition.scrollLinePosition * paintDataCache.lineHeight + scrollPosition.scrollLineOffset);
    }

    public ScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    public void revealCursor() {
        revealPosition(caret.getCaretPosition().getDataPosition(), caret.getSection());
    }

    public void revealPosition(long position, Section section) {
        if (paintDataCache.fontMetrics == null) {
            // Ignore if no font data is available
            return;
        }
        boolean scrolled = false;
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        long caretLine = position / paintDataCache.bytesPerLine;
        int codeDigits = getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;

        int positionByte;
        if (section == Section.CODE_MATRIX) {
            positionByte = (int) (position % paintDataCache.bytesPerLine) * charsPerByte + caret.getCodeOffset();
        } else {
            positionByte = (int) (position % paintDataCache.bytesPerLine);
            if (viewMode == ViewMode.DUAL) {
                positionByte += paintDataCache.bytesPerLine * charsPerByte;
            }
        }

        if (caretLine <= scrollPosition.scrollLinePosition) {
            scrollPosition.scrollLinePosition = caretLine;
            scrollPosition.scrollLineOffset = 0;
            scrolled = true;
        } else if (caretLine >= scrollPosition.scrollLinePosition + paintDataCache.linesPerRect) {
            scrollPosition.scrollLinePosition = caretLine - paintDataCache.linesPerRect;
            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                scrollPosition.scrollLineOffset = paintDataCache.lineHeight - (hexRect.height % paintDataCache.lineHeight);
            } else {
                scrollPosition.scrollLinePosition++;
            }
            scrolled = true;
        }
        if (positionByte <= scrollPosition.scrollCharPosition) {
            scrollPosition.scrollCharPosition = positionByte;
            scrollPosition.scrollCharOffset = 0;
            scrolled = true;
        } else if (positionByte >= scrollPosition.scrollCharPosition + paintDataCache.bytesPerRect) {
            scrollPosition.scrollCharPosition = positionByte - paintDataCache.bytesPerRect;
            if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                scrollPosition.scrollCharOffset = paintDataCache.charWidth - (hexRect.width % paintDataCache.charWidth);
            } else {
                scrollPosition.scrollCharPosition++;
            }
            scrolled = true;
        }

        if (scrolled) {
            updateScrollBars();
            notifyScrolled();
        }
    }

    public void updateScrollBars() {
        if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            verticalScrollBar.setValue((int) scrollPosition.scrollLinePosition);
        } else {
            verticalScrollBar.setValue((int) (scrollPosition.scrollLinePosition * paintDataCache.lineHeight + scrollPosition.scrollLineOffset));
        }

        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            horizontalScrollBar.setValue(scrollPosition.scrollCharPosition);
        } else {
            horizontalScrollBar.setValue(scrollPosition.scrollCharPosition * paintDataCache.charWidth + scrollPosition.scrollCharOffset);
        }
        repaint();
    }

    private void updateSelection(int modifiers, CaretPosition caretPosition) {
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0) {
            long currentPosition = caret.getDataPosition();
            long end = currentPosition;
            long start;
            if (selection != null) {
                start = selection.start;
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    selection.end = start < currentPosition ? end - 1 : end;
                }
            } else {
                start = caretPosition.getDataPosition();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    selection = new SelectionRange(start, start < currentPosition ? end - 1 : end);
                }
            }

            notifySelectionChanged();
        } else {
            clearSelection();
        }
        repaint();
    }

    public void moveRight(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() < data.getDataSize()) {
            if (caret.getSection() == Section.CODE_MATRIX) {
                int codeOffset = caret.getCodeOffset();
                if (caretPosition.getDataPosition() < data.getDataSize()) {
                    if (codeOffset < codeType.maxDigits - 1) {
                        caret.setCodeOffset(codeOffset + 1);
                    } else {
                        caret.setCaretPosition(caretPosition.getDataPosition() + 1, 0);
                    }
                    updateSelection(modifiers, caretPosition);
                    notifyCaretMoved();
                }
            } else {
                caret.setCaretPosition(caretPosition.getDataPosition() + 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            }
        }
    }

    public void moveLeft(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caret.getSection() == Section.CODE_MATRIX) {
            int codeOffset = caret.getCodeOffset();
            if (codeOffset > 0) {
                caret.setCodeOffset(codeOffset - 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            } else if (caretPosition.getDataPosition() > 0) {
                caret.setCaretPosition(caretPosition.getDataPosition() - 1, codeType.maxDigits - 1);
                updateSelection(modifiers, caretPosition);
                notifyCaretMoved();
            }
        } else if (caretPosition.getDataPosition() > 0) {
            caret.setCaretPosition(caretPosition.getDataPosition() - 1);
            updateSelection(modifiers, caretPosition);
            notifyCaretMoved();
        }
    }

    public SelectionRange getSelection() {
        return selection;
    }

    public void selectAll() {
        long dataSize = data.getDataSize();
        if (dataSize > 0) {
            selection = new SelectionRange(0, dataSize - 1);
            notifySelectionChanged();
            repaint();
        }
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
        return selection != null;
    }

    public void setSelection(SelectionRange selection) {
        this.selection = selection;
        notifySelectionChanged();
    }

    public void setCaretPosition(CaretPosition caretPosition) {
        caret.setCaretPosition(caretPosition);
        notifyCaretMoved();
    }

    public void setCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
        notifyCaretMoved();
    }

    public void setCaretPosition(long dataPosition, int codeOffset) {
        caret.setCaretPosition(dataPosition, codeOffset);
        notifyCaretMoved();
    }

    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    public void addCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    public void removeCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    public void addEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.add(editationModeChangedListener);
    }

    public void removeEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.remove(editationModeChangedListener);
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public void addScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    public void removeScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    /**
     * Returns component area rectangle.
     *
     * Computed as component size minus border insets.
     *
     * @return rectangle of component area
     */
    public Rectangle getComponentRectangle() {
        return paintDataCache.componentRectangle;
    }

    /**
     * Returns main code area rectangle.
     *
     * @return rectangle of main hexadecimal area
     */
    public Rectangle getCodeSectionRectangle() {
        return paintDataCache.codeSectionRectangle;
    }

    /**
     * Returns X start position of the ascii preview area.
     *
     * @return X position or -1 if area not present
     */
    public int getPreviewX() {
        return paintDataCache.previewX;
    }

    public int getLineHeight() {
        return paintDataCache.lineHeight;
    }

    public int getBytesPerLine() {
        return paintDataCache.bytesPerLine;
    }

    public int getCharWidth() {
        return paintDataCache.charWidth;
    }

    public FontMetrics getFontMetrics() {
        return paintDataCache.fontMetrics;
    }

    /**
     * Returns header space size.
     *
     * @return header space size
     */
    public int getHeaderSpace() {
        return paintDataCache.headerSpace;
    }

    /**
     * Returns line number space size.
     *
     * @return line number space size
     */
    public int getLineNumberSpace() {
        return paintDataCache.lineNumberSpace;
    }

    /**
     * Returns current line number length in characters.
     *
     * @return line number length
     */
    public int getLineNumberLength() {
        return paintDataCache.lineNumbersLength;
    }

    public BinaryData getData() {
        return data;
    }

    public void setData(BinaryData data) {
        this.data = data;
        notifyDataChanged();
        computePaintData();
        repaint();
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
        repaint();
    }

    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(CodeAreaPainter painter) {
        if (painter == null) {
            throw new NullPointerException("Painter cannot be null");
        }

        this.painter = painter;
        repaint();
    }

    public boolean isValidChar(char value) {
        return charset.canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = charset.encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        computeFontMetrics();
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
        computePaintData();
    }

    private void computeFontMetrics() {
        Graphics g = getGraphics();
        if (g != null) {
            Font font = getFont();
            paintDataCache.fontMetrics = g.getFontMetrics(font);
            /**
             * Use small 'w' character to guess normal font width.
             */
            paintDataCache.charWidth = paintDataCache.fontMetrics.charWidth('w');
            /**
             * Compare it to small 'i' to detect if font is monospaced.
             *
             * TODO: Is there better way?
             */
            paintDataCache.monospaceFont = paintDataCache.charWidth == paintDataCache.fontMetrics.charWidth(' ') && paintDataCache.charWidth == paintDataCache.fontMetrics.charWidth('i');
            int fontHeight = font.getSize();
            if (paintDataCache.charWidth == 0) {
                paintDataCache.charWidth = fontHeight;
            }
            paintDataCache.lineHeight = fontHeight + subFontSpace;
            computePaintData();
        }
    }

    public void computePaintData() {
        if (paintDataCache.fontMetrics == null) {
            return;
        }

        // TODO byte groups
        switch (viewMode) {
            case CODE_MATRIX: {
                paintDataCache.charsPerByte = codeType.maxDigits + 1;
                break;
            }
            case TEXT_PREVIEW: {
                paintDataCache.charsPerByte = 1;
                break;
            }
            case DUAL: {
                paintDataCache.charsPerByte = codeType.maxDigits + 2;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected view mode " + viewMode.name());
        }

        boolean verticalScrollBarVisible;
        boolean horizontalScrollBarVisible;

        Insets insets = getInsets();
        Dimension size = getSize();
        Rectangle compRect = paintDataCache.componentRectangle;
        compRect.x = insets.left;
        compRect.y = insets.top;
        compRect.width = size.width - insets.left - insets.right;
        compRect.height = size.height - insets.top - insets.bottom;

        switch (lineNumberLength.getLineNumberType()) {
            case AUTO: {
                double natLog = Math.log(getData().getDataSize());
                paintDataCache.lineNumbersLength = (int) Math.ceil(natLog / positionCodeType.baseLog);
                if (paintDataCache.lineNumbersLength == 0) {
                    paintDataCache.lineNumbersLength = 1;
                }
                break;
            }
            case SPECIFIED: {
                paintDataCache.lineNumbersLength = lineNumberLength.getLineNumberLength();
                break;
            }
        }

        int charsPerRect = computeCharsPerRect(compRect.width);
        int bytesPerLine;
        if (wrapMode) {
            bytesPerLine = charsPerRect / paintDataCache.charsPerByte;
            if (bytesPerLine == 0) {
                bytesPerLine = 1;
            }
        } else {
            bytesPerLine = lineLength;
        }
        int lines = (int) (data.getDataSize() / bytesPerLine) + 1;
        CodeAreaSpace.SpaceType headerSpaceType = headerSpace.getSpaceType();
        switch (headerSpaceType) {
            case NONE: {
                paintDataCache.headerSpace = 0;
                break;
            }
            case SPECIFIED: {
                paintDataCache.headerSpace = headerSpace.getSpaceSize();
                break;
            }
            case QUARTER_UNIT: {
                paintDataCache.headerSpace = paintDataCache.lineHeight / 4;
                break;
            }
            case HALF_UNIT: {
                paintDataCache.headerSpace = paintDataCache.lineHeight / 2;
                break;
            }
            case ONE_UNIT: {
                paintDataCache.headerSpace = paintDataCache.lineHeight;
                break;
            }
            case ONE_AND_HALF_UNIT: {
                paintDataCache.headerSpace = (int) (paintDataCache.lineHeight * 1.5f);
                break;
            }
            case DOUBLE_UNIT: {
                paintDataCache.headerSpace = paintDataCache.lineHeight * 2;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected header space type " + headerSpaceType.name());
        }

        CodeAreaSpace.SpaceType lineNumberSpaceType = lineNumberSpace.getSpaceType();
        switch (lineNumberSpaceType) {
            case NONE: {
                paintDataCache.lineNumberSpace = 0;
                break;
            }
            case SPECIFIED: {
                paintDataCache.lineNumberSpace = lineNumberSpace.getSpaceSize();
                break;
            }
            case QUARTER_UNIT: {
                paintDataCache.lineNumberSpace = paintDataCache.charWidth / 4;
                break;
            }
            case HALF_UNIT: {
                paintDataCache.lineNumberSpace = paintDataCache.charWidth / 2;
                break;
            }
            case ONE_UNIT: {
                paintDataCache.lineNumberSpace = paintDataCache.charWidth;
                break;
            }
            case ONE_AND_HALF_UNIT: {
                paintDataCache.lineNumberSpace = (int) (paintDataCache.charWidth * 1.5f);
                break;
            }
            case DOUBLE_UNIT: {
                paintDataCache.lineNumberSpace = paintDataCache.charWidth * 2;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected line number space type " + lineNumberSpaceType.name());
        }

        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        hexRect.y = insets.top + (showHeader ? paintDataCache.lineHeight + paintDataCache.headerSpace : 0);
        hexRect.x = insets.left + (showLineNumbers ? paintDataCache.charWidth * paintDataCache.lineNumbersLength + paintDataCache.lineNumberSpace : 0);

        if (verticalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
            verticalScrollBarVisible = lines > paintDataCache.linesPerRect;
        } else {
            verticalScrollBarVisible = verticalScrollBarVisibility == ScrollBarVisibility.ALWAYS;
        }
        if (verticalScrollBarVisible) {
            charsPerRect = computeCharsPerRect(compRect.x + compRect.width - paintDataCache.scrollBarThickness);
            if (wrapMode) {
                bytesPerLine = charsPerRect / paintDataCache.charsPerByte;
                if (bytesPerLine <= 0) {
                    bytesPerLine = 1;
                }
                lines = (int) (data.getDataSize() / bytesPerLine) + 1;
            }
        }

        paintDataCache.bytesPerLine = bytesPerLine;

        int maxWidth = compRect.x + compRect.width - hexRect.x;
        if (verticalScrollBarVisible) {
            maxWidth -= paintDataCache.scrollBarThickness;
        }

        if (horizontalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
            horizontalScrollBarVisible = paintDataCache.bytesPerLine * paintDataCache.charWidth * paintDataCache.charsPerByte > maxWidth;
        } else {
            horizontalScrollBarVisible = horizontalScrollBarVisibility == ScrollBarVisibility.ALWAYS;
        }
        if (horizontalScrollBarVisible) {
            paintDataCache.linesPerRect = (hexRect.height - paintDataCache.scrollBarThickness) / paintDataCache.lineHeight;
        }

        hexRect.width = compRect.x + compRect.width - hexRect.x;
        if (verticalScrollBarVisible) {
            hexRect.width -= paintDataCache.scrollBarThickness;
        }
        hexRect.height = compRect.y + compRect.height - hexRect.y;
        if (horizontalScrollBarVisible) {
            hexRect.height -= paintDataCache.scrollBarThickness;
        }

        paintDataCache.bytesPerRect = hexRect.width / paintDataCache.charWidth;
        paintDataCache.linesPerRect = hexRect.height / paintDataCache.lineHeight;

        int codeDigits = getCodeType().getMaxDigits();
        int charsPerByte = codeDigits + 1;
        // Compute sections positions
        if (viewMode == ViewMode.CODE_MATRIX) {
            paintDataCache.previewX = -1;
        } else {
            paintDataCache.previewX = hexRect.x;
            if (viewMode == ViewMode.DUAL) {
                paintDataCache.previewX += paintDataCache.bytesPerLine * paintDataCache.charWidth * charsPerByte;
            }
        }

        // Compute scrollbar positions
        boolean scrolled = false;
        verticalScrollBar.setVisible(verticalScrollBarVisible);
        if (verticalScrollBarVisible) {
            int verticalScrollBarHeight = compRect.y + compRect.height - hexRect.y;
            if (horizontalScrollBarVisible) {
                verticalScrollBarHeight -= paintDataCache.scrollBarThickness - 2;
            }
            verticalScrollBar.setBounds(compRect.x + compRect.width - paintDataCache.scrollBarThickness, hexRect.y, paintDataCache.scrollBarThickness, verticalScrollBarHeight);

            int verticalVisibleAmount;
            int verticalMaximum = lines;
            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                verticalVisibleAmount = hexRect.height;
                verticalMaximum *= paintDataCache.lineHeight;
            } else {
                verticalVisibleAmount = hexRect.height / paintDataCache.lineHeight;
            }
            verticalScrollBar.setMaximum(verticalMaximum);
            verticalScrollBar.setVisibleAmount(verticalVisibleAmount);

            // Cap vertical scrolling
            if (verticalVisibleAmount < verticalMaximum) {
                long maxLineScroll = verticalMaximum - verticalVisibleAmount;
                if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                    long lineScroll = scrollPosition.scrollLinePosition * paintDataCache.lineHeight + scrollPosition.scrollLineOffset;
                    if (lineScroll > maxLineScroll) {
                        scrollPosition.scrollLinePosition = maxLineScroll / paintDataCache.lineHeight;
                        scrollPosition.scrollLineOffset = (int) (maxLineScroll % paintDataCache.lineHeight);
                        scrolled = true;
                    }
                } else {
                    long lineScroll = scrollPosition.scrollLinePosition;
                    if (lineScroll > maxLineScroll) {
                        scrollPosition.scrollLinePosition = maxLineScroll;
                        scrolled = true;
                    }
                }
            }
        } else if (scrollPosition.scrollLinePosition > 0 || scrollPosition.scrollLineOffset > 0) {
            scrollPosition.scrollLinePosition = 0;
            scrollPosition.scrollLineOffset = 0;
            scrolled = true;
        }

        horizontalScrollBar.setVisible(horizontalScrollBarVisible);
        if (horizontalScrollBarVisible) {
            int horizontalScrollBarWidth = compRect.x + compRect.width - hexRect.x;
            if (verticalScrollBarVisible) {
                horizontalScrollBarWidth -= paintDataCache.scrollBarThickness - 2;
            }
            horizontalScrollBar.setBounds(hexRect.x, compRect.y + compRect.height - paintDataCache.scrollBarThickness, horizontalScrollBarWidth, paintDataCache.scrollBarThickness);

            int horizontalVisibleAmount;
            int horizontalMaximum = paintDataCache.bytesPerLine * paintDataCache.charsPerByte;
            if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                horizontalVisibleAmount = hexRect.width;
                horizontalMaximum *= paintDataCache.charWidth;
            } else {
                horizontalVisibleAmount = hexRect.width / paintDataCache.charWidth;
            }
            horizontalScrollBar.setMaximum(horizontalMaximum);
            horizontalScrollBar.setVisibleAmount(horizontalVisibleAmount);

            // Cap horizontal scrolling
            int maxByteScroll = horizontalMaximum - horizontalVisibleAmount;
            if (horizontalVisibleAmount < horizontalMaximum) {
                if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                    int byteScroll = scrollPosition.scrollCharPosition * paintDataCache.charWidth + scrollPosition.scrollCharOffset;
                    if (byteScroll > maxByteScroll) {
                        scrollPosition.scrollCharPosition = maxByteScroll / paintDataCache.charWidth;
                        scrollPosition.scrollCharOffset = maxByteScroll % paintDataCache.charWidth;
                        scrolled = true;
                    }
                } else {
                    int byteScroll = scrollPosition.scrollCharPosition;
                    if (byteScroll > maxByteScroll) {
                        scrollPosition.scrollCharPosition = maxByteScroll;
                        scrolled = true;
                    }
                }
            }
        } else if (scrollPosition.scrollCharPosition > 0 || scrollPosition.scrollCharOffset > 0) {
            scrollPosition.scrollCharPosition = 0;
            scrollPosition.scrollCharOffset = 0;
            scrolled = true;
        }

        if (scrolled) {
            updateScrollBars();
            notifyScrolled();
        }
    }

    private int computeCharsPerRect(int width) {
        if (showLineNumbers) {
            width -= paintDataCache.charWidth * paintDataCache.lineNumbersLength + getLineNumberSpace();
        }

        return width / paintDataCache.charWidth;
    }

    public ColorsGroup getMainColors() {
        return new ColorsGroup(mainColors);
    }

    public ColorsGroup getAlternateColors() {
        return new ColorsGroup(alternateColors);
    }

    public ColorsGroup getSelectionColors() {
        return new ColorsGroup(selectionColors);
    }

    public ColorsGroup getMirrorSelectionColors() {
        return new ColorsGroup(mirrorSelectionColors);
    }

    public void setMainColors(ColorsGroup colorsGroup) {
        mainColors.setColors(colorsGroup);
        repaint();
    }

    public void setAlternateColors(ColorsGroup colorsGroup) {
        alternateColors.setColors(colorsGroup);
        repaint();
    }

    public void setSelectionColors(ColorsGroup colorsGroup) {
        selectionColors.setColors(colorsGroup);
        repaint();
    }

    public void setMirrorSelectionColors(ColorsGroup colorsGroup) {
        mirrorSelectionColors.setColors(colorsGroup);
        repaint();
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
        repaint();
    }

    public Color getDecorationLineColor() {
        return decorationLineColor;
    }

    public void setDecorationLineColor(Color decorationLineColor) {
        this.decorationLineColor = decorationLineColor;
        repaint();
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == ViewMode.CODE_MATRIX) {
            caret.setSection(Section.CODE_MATRIX);
            notifyCaretMoved();
        } else if (viewMode == ViewMode.TEXT_PREVIEW) {
            caret.setSection(Section.TEXT_PREVIEW);
            notifyCaretMoved();
        }
        computePaintData();
        repaint();
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        computePaintData();
        repaint();
    }

    public PositionCodeType getPositionCodeType() {
        return positionCodeType;
    }

    public void setPositionCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = positionCodeType;
        computePaintData();
        repaint();
    }

    public BackgroundMode getBackgroundMode() {
        return backgroundMode;
    }

    public void setBackgroundMode(BackgroundMode backgroundMode) {
        this.backgroundMode = backgroundMode;
        repaint();
    }

    public int getDecorationMode() {
        return decorationMode;
    }

    public void setDecorationMode(int decorationMode) {
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
        return caret.getSection();
    }

    public void setActiveSection(Section activeSection) {
        caret.setSection(activeSection);
        revealCursor();
        repaint();
    }

    public EditationMode getEditationMode() {
        return editationMode;
    }

    public void setEditationMode(EditationMode editationMode) {
        boolean chaged = editationMode != this.editationMode;
        this.editationMode = editationMode;
        if (chaged) {
            for (EditationModeChangedListener listener : editationModeChangedListeners) {
                listener.editationModeChanged(editationMode);
            }
            repaint();
        }
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        computePaintData();
        repaint();
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
        computePaintData();
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
        computePaintData();
        repaint();
    }

    public boolean isHandleClipboard() {
        return handleClipboard;
    }

    public void setHandleClipboard(boolean handleClipboard) {
        this.handleClipboard = handleClipboard;
    }

    public boolean isShowUnprintableCharacters() {
        return showUnprintableCharacters;
    }

    public void setShowUnprintableCharacters(boolean showUnprintableCharacters) {
        this.showUnprintableCharacters = showUnprintableCharacters;
        repaint();
    }

    public boolean isShowShadowCursor() {
        return showShadowCursor;
    }

    public void setShowShadowCursor(boolean showShadowCursor) {
        this.showShadowCursor = showShadowCursor;
        repaint();
    }

    public int getLineLength() {
        return lineLength;
    }

    public void setLineLength(int lineLength) {
        this.lineLength = lineLength;
        if (!wrapMode) {
            computePaintData();
            repaint();
        }
    }

    public CharRenderingMode getCharRenderingMode() {
        return charRenderingMode;
    }

    public boolean isMonospaceFontDetected() {
        return paintDataCache.monospaceFont;
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

    public HexCharactersCase getHexCharactersCase() {
        return hexCharactersCase;
    }

    public void setHexCharactersCase(HexCharactersCase hexCharactersCase) {
        this.hexCharactersCase = hexCharactersCase;
        painter.setHexCharacters(hexCharactersCase == HexCharactersCase.LOWER ? CodeAreaUtils.LOWER_HEX_CODES : CodeAreaUtils.UPPER_HEX_CODES);
        repaint();
    }

    public CodeAreaSpace.SpaceType getHeaderSpaceType() {
        return headerSpace.getSpaceType();
    }

    public void setHeaderSpaceType(CodeAreaSpace.SpaceType spaceType) {
        if (spaceType == null) {
            throw new NullPointerException();
        }
        headerSpace.setSpaceType(spaceType);
        computePaintData();
        repaint();
    }

    public int getHeaderSpaceSize() {
        return headerSpace.getSpaceSize();
    }

    public void setHeaderSpaceSize(int spaceSize) {
        if (spaceSize < 0) {
            throw new IllegalArgumentException("Negative space size is not valid");
        }
        headerSpace.setSpaceSize(spaceSize);
        computePaintData();
        repaint();
    }

    public CodeAreaSpace.SpaceType getLineNumberSpaceType() {
        return lineNumberSpace.getSpaceType();
    }

    public void setLineNumberSpaceType(CodeAreaSpace.SpaceType spaceType) {
        if (spaceType == null) {
            throw new NullPointerException();
        }
        lineNumberSpace.setSpaceType(spaceType);
        computePaintData();
        repaint();
    }

    public int getLineNumberSpaceSize() {
        return lineNumberSpace.getSpaceSize();
    }

    public void setLineNumberSpaceSize(int spaceSize) {
        if (spaceSize < 0) {
            throw new IllegalArgumentException("Negative space size is not valid");
        }
        lineNumberSpace.setSpaceSize(spaceSize);
        computePaintData();
        repaint();
    }

    public CodeAreaLineNumberLength.LineNumberType getLineNumberType() {
        return lineNumberLength.getLineNumberType();
    }

    public void setLineNumberType(CodeAreaLineNumberLength.LineNumberType lineNumberType) {
        if (lineNumberType == null) {
            throw new NullPointerException("Line number type cannot be null");
        }
        lineNumberLength.setLineNumberType(lineNumberType);
        computePaintData();
        repaint();
    }

    public int getLineNumberSpecifiedLength() {
        return lineNumberLength.getLineNumberLength();
    }

    public void setLineNumberSpecifiedLength(int lineNumberSize) {
        if (lineNumberSize < 1) {
            throw new IllegalArgumentException("Line number type cannot be less then 1");
        }
        lineNumberLength.setLineNumberLength(lineNumberSize);
        computePaintData();
        repaint();
    }

    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        computePaintData();
        updateScrollBars();
    }

    public VerticalScrollMode getVerticalScrollMode() {
        return verticalScrollMode;
    }

    public void setVerticalScrollMode(VerticalScrollMode verticalScrollMode) {
        this.verticalScrollMode = verticalScrollMode;
        long linePosition = scrollPosition.scrollLinePosition;
        if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            scrollPosition.scrollLineOffset = 0;
        }
        computePaintData();
        scrollPosition.scrollLinePosition = linePosition;
        updateScrollBars();
        notifyScrolled();
    }

    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        computePaintData();
        updateScrollBars();
    }

    public HorizontalScrollMode getHorizontalScrollMode() {
        return horizontalScrollMode;
    }

    public void setHorizontalScrollMode(HorizontalScrollMode horizontalScrollMode) {
        this.horizontalScrollMode = horizontalScrollMode;
        int bytePosition = scrollPosition.scrollCharPosition;
        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            scrollPosition.scrollCharOffset = 0;
        }
        computePaintData();
        scrollPosition.scrollCharPosition = bytePosition;
        updateScrollBars();
        notifyScrolled();
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    public CaretPosition getCaretPosition() {
        return caret.getCaretPosition();
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public void copy() {
        commandHandler.copy();
    }

    public void copyAsCode() {
        commandHandler.copyAsCode();
    }

    public void cut() {
        commandHandler.cut();
    }

    public void paste() {
        commandHandler.paste();
    }

    public void pasteFromCode() {
        commandHandler.pasteFromCode();
    }

    public void delete() {
        commandHandler.delete();
    }

    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    private static Color createOddColor(Color color) {
        return new Color(
                computeOddColorComponent(color.getRed()),
                computeOddColorComponent(color.getGreen()),
                computeOddColorComponent(color.getBlue()));
    }

    private static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? - 16 : 16);
    }

    /**
     * Selection range is selection between two points where begin represents
     * originating point. End of the selection can be before or after begin.
     */
    public static class SelectionRange {

        private long start;
        private long end;

        public SelectionRange() {
        }

        public SelectionRange(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        /**
         * Returns first data position of the selection.
         *
         * @return data position
         */
        public long getFirst() {
            return end >= start ? start : end;
        }

        /**
         * Returns last data position of the selection.
         *
         * @return data position
         */
        public long getLast() {
            return end >= start ? end : start - 1;
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

    /**
     * Editation mode change listener.
     *
     * Event is fired each time editation mode is changed.
     */
    public interface EditationModeChangedListener {

        void editationModeChanged(EditationMode editationMode);
    }

    /**
     * Data changed listener.
     *
     * Event is fired each time data is modified.
     */
    public interface DataChangedListener {

        void dataChanged();
    }

    /**
     * Scrolling listener.
     *
     * Event is fired each time component is scrolled.
     */
    public interface ScrollingListener {

        void scrolled();
    }

    /**
     * Component supports showing numerical codes or textual preview, or both.
     */
    public static enum ViewMode {
        DUAL, CODE_MATRIX, TEXT_PREVIEW;
    }

    public static enum CodeType {
        BINARY(8), OCTAL(3), DECIMAL(3), HEXADECIMAL(2);

        private int maxDigits;

        CodeType(int maxDigits) {
            this.maxDigits = maxDigits;
        }

        /**
         * Maximum number of digits per single byte.
         *
         * @return number of digits
         */
        public int getMaxDigits() {
            return maxDigits;
        }
    }

    public static enum PositionCodeType {
        OCTAL(8), DECIMAL(10), HEXADECIMAL(16);

        int base;
        double baseLog;

        private PositionCodeType(int base) {
            this.base = base;
            baseLog = Math.log(base);
        }

        public int getBase() {
            return base;
        }

        public double getBaseLog() {
            return baseLog;
        }
    }

    public static enum Section {
        CODE_MATRIX, TEXT_PREVIEW
    }

    public static enum BackgroundMode {
        NONE, PLAIN, STRIPPED, GRIDDED
    }

    public static enum EditationMode {
        OVERWRITE, INSERT, READ_ONLY
    }

    public static enum VerticalScrollMode {
        PER_LINE, PIXEL
    }

    public static enum HorizontalScrollMode {
        PER_CHAR, PIXEL
    }

    public static enum ScrollBarVisibility {
        NEVER, IF_NEEDED, ALWAYS
    }

    /**
     * Character rendering mode.
     *
     * AUTO - Centers characters if width is not default and detects monospace
     * fonts to render characters as string if possible
     *
     * LINE_AT_ONCE - Render sequence of characters from top left corner of the
     * line ignoring character width. It's fastest, but render correctly only
     * for monospaced fonts and charsets where all characters have same width
     *
     * TOP_LEFT - Render each character from top left corner of it's position
     *
     * CENTER - Centers each character in it's area
     */
    public static enum CharRenderingMode {
        AUTO, LINE_AT_ONCE, TOP_LEFT, CENTER
    }

    public static enum CharAntialiasingMode {
        OFF, AUTO, DEFAULT, BASIC, GASP, LCD_HRGB, LCD_HBGR, LCD_VRGB, LCD_VBGR
    }

    public static enum HexCharactersCase {
        LOWER, UPPER
    }

    /**
     * Precomputed data for painting of the component.
     */
    private static class PaintDataCache {

        /**
         * Font related paint data.
         */
        FontMetrics fontMetrics = null;
        int charWidth;
        int lineHeight;
        boolean monospaceFont = false;

        int bytesPerLine;
        int charsPerByte;
        int lineNumbersLength;

        /**
         * Component area without border insets.
         */
        final Rectangle componentRectangle = new Rectangle();
        /**
         * Space between header and code area.
         */
        int headerSpace;
        /**
         * Space between line numbers and code area.
         */
        int lineNumberSpace;
        /**
         * Space between main code area and preview.
         */
        int previewSpace;

        /**
         * Main data area.
         *
         * Component area without header, line numbers and scrollbars.
         */
        final Rectangle codeSectionRectangle = new Rectangle();
        int previewX;
        int bytesPerRect;
        int linesPerRect;
        int scrollBarThickness = 17;
    }

    /**
     * Scrolling position.
     */
    public static class ScrollPosition {

        long scrollLinePosition = 0;
        int scrollLineOffset = 0;
        int scrollCharPosition = 0;
        int scrollCharOffset = 0;

        public long getScrollLinePosition() {
            return scrollLinePosition;
        }

        public int getScrollLineOffset() {
            return scrollLineOffset;
        }

        public int getScrollCharPosition() {
            return scrollCharPosition;
        }

        public int getScrollCharOffset() {
            return scrollCharOffset;
        }
    }

    /**
     * Set of colors for different sections of component rendering.
     */
    public static class ColorsGroup {

        private Color textColor;
        private Color backgroundColor;
        private Color unprintablesColor;
        private Color unprintablesBackgroundColor;

        public ColorsGroup() {
        }

        /**
         * Copy constructor.
         *
         * @param colorsGroup colors group
         */
        public ColorsGroup(ColorsGroup colorsGroup) {
            setColorsFromGroup(colorsGroup);
        }

        private void setColorsFromGroup(ColorsGroup colorsGroup) {
            textColor = colorsGroup.getTextColor();
            backgroundColor = colorsGroup.getBackgroundColor();
            unprintablesColor = colorsGroup.getUnprintablesColor();
            unprintablesBackgroundColor = colorsGroup.getUnprintablesBackgroundColor();
        }

        public Color getTextColor() {
            return textColor;
        }

        public void setTextColor(Color textColor) {
            this.textColor = textColor;
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public Color getUnprintablesColor() {
            return unprintablesColor;
        }

        public void setUnprintablesColor(Color unprintablesColor) {
            this.unprintablesColor = unprintablesColor;
        }

        public Color getUnprintablesBackgroundColor() {
            return unprintablesBackgroundColor;
        }

        public void setUnprintablesBackgroundColor(Color unprintablesBackgroundColor) {
            this.unprintablesBackgroundColor = unprintablesBackgroundColor;
        }

        public void setBothBackgroundColors(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            this.unprintablesBackgroundColor = backgroundColor;
        }

        public void setColors(ColorsGroup colorsGroup) {
            setColorsFromGroup(colorsGroup);
        }

        public Color getColor(ColorType colorType) {
            switch (colorType) {
                case TEXT:
                    return textColor;
                case BACKGROUND:
                    return backgroundColor;
                case UNPRINTABLES:
                    return unprintablesColor;
                case UNPRINTABLES_BACKGROUND:
                    return unprintablesBackgroundColor;
                default:
                    throw new IllegalStateException();
            }
        }

        public void setColor(ColorType colorType, Color color) {
            switch (colorType) {
                case TEXT: {
                    textColor = color;
                    break;
                }
                case BACKGROUND: {
                    backgroundColor = color;
                    break;
                }
                case UNPRINTABLES: {
                    unprintablesColor = color;
                    break;
                }
                case UNPRINTABLES_BACKGROUND: {
                    unprintablesBackgroundColor = color;
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * Enumeration of color types in ColorsGroup.
     */
    public static enum ColorType {
        TEXT,
        BACKGROUND,
        UNPRINTABLES,
        UNPRINTABLES_BACKGROUND
    }

    private class CodeAreaMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

        private Cursor currentCursor = getCursor();
        private final Cursor defaultCursor = Cursor.getDefaultCursor();
        private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

        @Override
        public void mousePressed(MouseEvent me) {
            requestFocus();
            if (isEnabled() && me.getButton() == MouseEvent.BUTTON1) {
                moveCaret(me, me.getModifiersEx());
                revealCursor();
                mouseDown = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            mouseDown = false;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            currentCursor = defaultCursor;
            setCursor(defaultCursor);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            updateMouseCursor(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateMouseCursor(e);
        }

        private void updateMouseCursor(MouseEvent e) {
            Cursor newCursor = defaultCursor;
            Rectangle hexRect = paintDataCache.codeSectionRectangle;
            if (e.getX() >= hexRect.x && e.getY() >= hexRect.y) {
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
            if (isEnabled() && mouseDown) {
                moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
                revealCursor();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!isEnabled()) {
                return;
            }

            if (e.isShiftDown() && horizontalScrollBar.isVisible()) {
                if (e.getWheelRotation() > 0) {
                    int visibleBytes = paintDataCache.codeSectionRectangle.width / (paintDataCache.charWidth * paintDataCache.charsPerByte);
                    int bytes = paintDataCache.bytesPerLine - visibleBytes;
                    if (scrollPosition.scrollCharPosition < bytes) {
                        if (scrollPosition.scrollCharPosition < bytes - MOUSE_SCROLL_LINES) {
                            scrollPosition.scrollCharPosition += MOUSE_SCROLL_LINES;
                        } else {
                            scrollPosition.scrollCharPosition = bytes;
                        }
                        updateScrollBars();
                        notifyScrolled();
                    }
                } else if (scrollPosition.scrollCharPosition > 0) {
                    if (scrollPosition.scrollCharPosition > MOUSE_SCROLL_LINES) {
                        scrollPosition.scrollCharPosition -= MOUSE_SCROLL_LINES;
                    } else {
                        scrollPosition.scrollCharPosition = 0;
                    }
                    updateScrollBars();
                    notifyScrolled();
                }
            } else if (e.getWheelRotation() > 0) {
                long lines = (int) (data.getDataSize() / paintDataCache.bytesPerLine);
                if (lines * paintDataCache.bytesPerLine < data.getDataSize()) {
                    lines++;
                }
                lines -= paintDataCache.linesPerRect;
                if (scrollPosition.scrollLinePosition < lines) {
                    if (scrollPosition.scrollLinePosition < lines - MOUSE_SCROLL_LINES) {
                        scrollPosition.scrollLinePosition += MOUSE_SCROLL_LINES;
                    } else {
                        scrollPosition.scrollLinePosition = lines;
                    }
                    updateScrollBars();
                    notifyScrolled();
                }
            } else if (scrollPosition.scrollLinePosition > 0) {
                if (scrollPosition.scrollLinePosition > MOUSE_SCROLL_LINES) {
                    scrollPosition.scrollLinePosition -= MOUSE_SCROLL_LINES;
                } else {
                    scrollPosition.scrollLinePosition = 0;
                }
                updateScrollBars();
                notifyScrolled();
            }
        }
    }

    private class CodeAreaKeyListener extends KeyAdapter {

        public CodeAreaKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() != 0xffff) {
                commandHandler.keyPressed(e.getKeyChar());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!isEnabled()) {
                return;
            }

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: {
                    moveLeft(e.getModifiersEx());
                    commandHandler.caretMoved();
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    moveRight(e.getModifiersEx());
                    commandHandler.caretMoved();
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_UP: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = paintDataCache.bytesPerLine;
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesPerLine) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesPerLine, caret.getCodeOffset());
                            notifyCaretMoved();
                        }
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    commandHandler.caretMoved();
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = paintDataCache.bytesPerLine;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesPerLine < dataSize
                                || (caretPosition.getDataPosition() + bytesPerLine == dataSize && caretPosition.getCodeOffset() == 0)) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesPerLine, caret.getCodeOffset());
                            notifyCaretMoved();
                        }
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    commandHandler.caretMoved();
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_HOME: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = paintDataCache.bytesPerLine;
                    if (caretPosition.getDataPosition() > 0 || caret.getCodeOffset() > 0) {
                        long targetPosition;
                        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                            targetPosition = 0;
                        } else {
                            targetPosition = (caretPosition.getDataPosition() / bytesPerLine) * bytesPerLine;
                        }
                        caret.setCaretPosition(targetPosition);
                        commandHandler.caretMoved();
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_END: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesPerLine = paintDataCache.bytesPerLine;
                    long dataSize = data.getDataSize();
                    if (caretPosition.getDataPosition() < dataSize) {
                        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                            caret.setCaretPosition(data.getDataSize());
                        } else if (caret.getSection() == Section.CODE_MATRIX) {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize, newPosition < dataSize ? codeType.maxDigits - 1 : 0);
                        } else {
                            long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                            caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
                        }
                        commandHandler.caretMoved();
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_PAGE_UP: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesStep = paintDataCache.bytesPerLine * paintDataCache.linesPerRect;
                    if (scrollPosition.scrollLinePosition > paintDataCache.linesPerRect) {
                        scrollPosition.scrollLinePosition -= paintDataCache.linesPerRect;
                        updateScrollBars();
                        notifyScrolled();
                    }
                    if (caretPosition.getDataPosition() > 0) {
                        if (caretPosition.getDataPosition() >= bytesStep) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - bytesStep, caret.getCodeOffset());
                        } else if (caretPosition.getDataPosition() >= paintDataCache.bytesPerLine) {
                            caret.setCaretPosition(caretPosition.getDataPosition() % paintDataCache.bytesPerLine, caret.getCodeOffset());
                        }
                        commandHandler.caretMoved();
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_PAGE_DOWN: {
                    CaretPosition caretPosition = caret.getCaretPosition();
                    int bytesStep = paintDataCache.bytesPerLine * paintDataCache.linesPerRect;
                    long dataSize = data.getDataSize();
                    if (scrollPosition.scrollLinePosition < dataSize / paintDataCache.bytesPerLine - paintDataCache.linesPerRect * 2) {
                        scrollPosition.scrollLinePosition += paintDataCache.linesPerRect;
                        updateScrollBars();
                        notifyScrolled();
                    }
                    if (caretPosition.getDataPosition() < dataSize) {
                        if (caretPosition.getDataPosition() + bytesStep < dataSize) {
                            caret.setCaretPosition(caretPosition.getDataPosition() + bytesStep, caret.getCodeOffset());
                        } else if (caretPosition.getDataPosition() + paintDataCache.bytesPerLine <= dataSize) {
                            long dataPosition = dataSize
                                    - dataSize % paintDataCache.bytesPerLine
                                    - ((caretPosition.getDataPosition() % paintDataCache.bytesPerLine <= dataSize % paintDataCache.bytesPerLine) ? 0 : paintDataCache.bytesPerLine)
                                    + (caretPosition.getDataPosition() % paintDataCache.bytesPerLine);
                            caret.setCaretPosition(dataPosition, dataPosition == dataSize ? 0 : caret.getCodeOffset());
                        }
                        commandHandler.caretMoved();
                        notifyCaretMoved();
                        updateSelection(e.getModifiersEx(), caretPosition);
                    }
                    revealCursor();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_INSERT: {
                    if (editationMode != EditationMode.READ_ONLY) {
                        setEditationMode(editationMode == EditationMode.INSERT ? EditationMode.OVERWRITE : EditationMode.INSERT);
                    }
                    e.consume();
                    break;
                }
                case KeyEvent.VK_TAB: {
                    if (viewMode == ViewMode.DUAL) {
                        Section activeSection = caret.getSection() == Section.CODE_MATRIX ? Section.TEXT_PREVIEW : Section.CODE_MATRIX;
                        if (activeSection == Section.TEXT_PREVIEW) {
                            caret.setCodeOffset(0);
                        }
                        caret.setSection(activeSection);
                        revealCursor();
                        repaint();
                    }
                    e.consume();
                    break;
                }
                case KeyEvent.VK_DELETE: {
                    commandHandler.deletePressed();
                    e.consume();
                    break;
                }
                case KeyEvent.VK_BACK_SPACE: {
                    commandHandler.backSpacePressed();
                    e.consume();
                    break;
                }
                default: {
                    if (handleClipboard) {
                        if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_C) {
                            commandHandler.copy();
                            e.consume();
                            break;
                        } else if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_X) {
                            commandHandler.cut();
                            e.consume();
                            break;
                        } else if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_V) {
                            commandHandler.paste();
                            e.consume();
                            break;
                        } else if ((e.getModifiers() & metaMask) > 0 && e.getKeyCode() == KeyEvent.VK_A) {
                            selectAll();
                            e.consume();
                            break;
                        }
                    }
                }
            }
        }
    }

    private class CodeAreaComponentListener implements ComponentListener {

        public CodeAreaComponentListener() {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            computePaintData();
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
                scrollPosition.scrollLinePosition = verticalScrollBar.getValue() / paintDataCache.lineHeight;
                scrollPosition.scrollLineOffset = verticalScrollBar.getValue() % paintDataCache.lineHeight;
            }
            repaint();
            notifyScrolled();
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
                scrollPosition.scrollCharPosition = horizontalScrollBar.getValue();
            } else {
                scrollPosition.scrollCharPosition = horizontalScrollBar.getValue() / paintDataCache.charWidth;
                scrollPosition.scrollCharOffset = horizontalScrollBar.getValue() % paintDataCache.charWidth;
            }
            repaint();
            notifyScrolled();
        }
    }
}
