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
package org.exbin.bined.swt.extended;

import org.exbin.bined.swt.*;
import org.exbin.bined.swt.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.swt.basic.DefaultCodeAreaPainter;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.RenderingHints;
import org.exbin.bined.CaretPosition;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.CodeAreaLineNumberLength;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.EditAllowed;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditModeChangedListener;
import org.exbin.bined.HexCharactersCase;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.Section;
import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.ViewMode;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.CodeAreaUtils;

/**
 * Binary viewer/editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaEx extends Composite implements CodeAreaControl {

    @Nullable
    private BinaryData data;

    @Nonnull
    private CodeAreaWorker worker;
    @Nonnull
    private CodeAreaCommandHandler commandHandler;

    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    private final CodeAreaCaret caret;
    private SelectionRange selection;

    private ViewMode viewMode = ViewMode.DUAL;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;
    private BackgroundPaintMode backgroundPaintMode = BackgroundPaintMode.STRIPPED;
    private boolean lineNumberBackground = true;
    private Charset charset = Charset.defaultCharset();
    private int decorationMode = DECORATION_DEFAULT;
    private EditAllowed editAllowed = EditAllowed.ALLOWED;
    private EditMode editMode = EditMode.OVERWRITE;
    private CharRenderingMode charRenderingMode = CharRenderingMode.AUTO;
    private CharAntialiasingMode charAntialiasingMode = CharAntialiasingMode.AUTO;
    private HexCharactersCase hexCharactersCase = HexCharactersCase.UPPER;
    private final CodeAreaSpace headerSpace = new CodeAreaSpace(CodeAreaSpace.SpaceType.HALF_UNIT);
    private final CodeAreaSpace lineNumberSpace = new CodeAreaSpace();
    private final CodeAreaLineNumberLength lineNumberLength = new CodeAreaLineNumberLength();

    private int lineLength = 16;
    private int byteGroupSize = 1;
    private int spaceGroupSize = 0;
    private int subFontSpace = 3;
    private boolean showHeader = true;
    private boolean showLineNumbers = true;
    private boolean mouseDown;
    private boolean wrapMode = false;
    private boolean handleClipboard = true;
    private boolean showUnprintableCharacters = false;
    private boolean showShadowCursor = true;

    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollMode verticalScrollMode = VerticalScrollMode.PER_LINE;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollMode horizontalScrollMode = HorizontalScrollMode.PIXEL;
    private ScrollBar horizontalScrollBar;
    private ScrollBar verticalScrollBar;
    private final ScrollPosition scrollPosition = new ScrollPosition();

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
    private Color negativeCursorColor;
    private Color decorationLineColor;

    /**
     * Listeners.
     */
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<EditModeChangedListener> editModeChangedListeners = new ArrayList<>();
    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();

    private final PaintDataCache paintDataCache = new PaintDataCache();

    public CodeAreaEx(Composite parent, int style) {
        super(parent, style);
        caret = new CodeAreaCaret(this);
        painter = new DefaultCodeAreaPainter(this);
        commandHandler = new DefaultCodeAreaCommandHandler(this);

        init();
    }

    private void init() {
        Display display = Display.getDefault();
        Color textColor = new Color(display, 0, 0, 0); // UIManager.getColor("TextArea.foreground");
        if (textColor == null) {
            textColor = new Color(display, 0, 0, 0);
        }
        Color backgroundColor = new Color(display, 255, 255, 255); // UIManager.getColor("TextArea.background");
        if (backgroundColor == null) {
            backgroundColor = new Color(display, 255, 255, 255);
        }
        super.setForeground(textColor);
        super.setBackground(createOddColor(backgroundColor));
        Color unprintablesColor = new Color(display, textColor.getRed(), (textColor.getGreen() + 128) % 256, textColor.getBlue());
        mainColors.setTextColor(textColor);
        mainColors.setBothBackgroundColors(backgroundColor);
        mainColors.setUnprintablesColor(unprintablesColor);
        alternateColors.setTextColor(textColor);
        alternateColors.setBothBackgroundColors(createOddColor(backgroundColor));
        alternateColors.setUnprintablesColor(unprintablesColor);
        Color selectionTextColor = new Color(display, 255, 255, 255); // UIManager.getColor("TextArea.selectionForeground");
        if (selectionTextColor == null) {
            selectionTextColor = new Color(display, 255, 255, 255);
        }
        Color selectionBackgroundColor = new Color(display, 96, 96, 255); // UIManager.getColor("TextArea.selectionBackground");
        if (selectionBackgroundColor == null) {
            selectionBackgroundColor = new Color(display, 96, 96, 255);
        }
        selectionColors.setTextColor(selectionTextColor);
        selectionColors.setBothBackgroundColors(selectionBackgroundColor);
        selectionColors.setUnprintablesColor(unprintablesColor);
        mirrorSelectionColors.setTextColor(selectionTextColor);
        int grayLevel = (selectionBackgroundColor.getRed() + selectionBackgroundColor.getGreen() + selectionBackgroundColor.getBlue()) / 3;
        mirrorSelectionColors.setBothBackgroundColors(new Color(display, grayLevel, grayLevel, grayLevel));
        mirrorSelectionColors.setUnprintablesColor(unprintablesColor);

        cursorColor = new Color(display, 0, 0, 0); // UIManager.getColor("TextArea.caretForeground");
        if (cursorColor == null) {
            cursorColor = new Color(display, 0, 0, 0);
        }
        negativeCursorColor = createNegativeColor(cursorColor);
        decorationLineColor = new Color(display, 127, 127, 127);

        verticalScrollBar = null; // new ScrollBar(Scrollbar.VERTICAL)
//        verticalScrollBar.setVisible(false);
//        verticalScrollBar.setIgnoreRepaint(true);
//        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
//        add(verticalScrollBar);
//        horizontalScrollBar = new JScrollBar(Scrollbar.HORIZONTAL);
//        horizontalScrollBar.setIgnoreRepaint(true);
//        horizontalScrollBar.setVisible(false);
//        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
//        add(horizontalScrollBar);

//        setFocusable(true);
//        setFocusTraversalKeysEnabled(false);
//        addComponentListener(new CodeAreaComponentListener());
//        CodeAreaMouseListener codeAreaMouseListener = new CodeAreaMouseListener();
//        addMouseListener(codeAreaMouseListener);
//        addMouseMotionListener(codeAreaMouseListener);
//        addMouseWheelListener(codeAreaMouseListener);
        addKeyListener(new CodeAreaKeyListener());
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                CodeAreaEx.this.widgetDisposed(e);

            }
        });
        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                CodeAreaEx.this.paintControl(e);
            }
        });
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                redraw();
            }

            @Override
            public void focusLost(FocusEvent fe) {
                redraw();
            }
        });
    }

    void paintControl(PaintEvent e) {
//        Insets insets = getInsets();
        Point size = getSize();
        Rectangle compRect = new Rectangle(0, 0, 0, 0);
        compRect.x = 0; // insets.left;
        compRect.y = 0; //insets.top;
        compRect.width = size.x; // - insets.left - insets.right;
        compRect.height = size.y; // - insets.top - insets.bottom;
        if (!paintDataCache.componentRectangle.equals(compRect)) {
            computePaintData();
        }

        GC g = e.gc;
        Rectangle clipBounds = g.getClipping();
//        if (charAntialiasingMode != CharAntialiasingMode.OFF && g instanceof Graphics2D) {
//            Object antialiasingHint = getAntialiasingHint((Graphics2D) g);
//            ((Graphics2D) g).setRenderingHint(
//                    RenderingHints.KEY_TEXT_ANTIALIASING,
//                    antialiasingHint);
//        }

        if (paintDataCache.fontMetrics == null) {
            computeFontMetrics(g);
        }

        painter.paintOverall(g);
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        if (showHeader) {
            g.setClipping(clipBounds.intersection(new Rectangle(hexRect.x, 0, hexRect.width, hexRect.y)));
            painter.paintHeader(g);
        }

        g.setClipping(clipBounds.intersection(new Rectangle(0, hexRect.y, hexRect.x + hexRect.width, hexRect.height)));
        painter.paintBackground(g);
        if (showLineNumbers) {
            painter.paintLineNumbers(g);
            g.setClipping(clipBounds.intersection(new Rectangle(hexRect.x, hexRect.y, hexRect.width, hexRect.height)));
        }

        painter.paintMainArea(g);
        painter.paintCursor(g);

        g.setClipping(clipBounds);
    }

    void widgetDisposed(DisposeEvent e) {
        // TODO
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
            default:
                throw CodeAreaUtils.getInvalidTypeException(charAntialiasingMode);
        }

        return antialiasingHint;
    }

    public CodeAreaCaret getCaret() {
        return caret;
    }

    private void moveCaret(MouseEvent me, int modifiers) {
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        int bytesPerLine = paintDataCache.bytesPerLine;
        int mouseX = me.x;
        if (mouseX < hexRect.x) {
            mouseX = hexRect.x;
        }
        int cursorCharX = (mouseX - hexRect.x + scrollPosition.scrollCharOffset) / paintDataCache.charWidth + scrollPosition.scrollCharPosition;
        long cursorLineY = (me.y - hexRect.y + scrollPosition.scrollLineOffset) / paintDataCache.lineHeight + scrollPosition.scrollLinePosition;
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnLine;
        if ((viewMode == ViewMode.DUAL && cursorCharX < paintDataCache.previewStartChar) || viewMode == ViewMode.CODE_MATRIX) {
            caret.setSection(Section.CODE_MATRIX);
            byteOnLine = computeByteOffsetPerCodeCharOffset(cursorCharX, false);
            if (byteOnLine >= bytesPerLine) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - computeByteCharPos(byteOnLine);
                if (codeOffset >= codeType.getMaxDigits()) {
                    codeOffset = codeType.getMaxDigits() - 1;
                }
            }
        } else {
            caret.setSection(Section.TEXT_PREVIEW);
            byteOnLine = cursorCharX;
            if (viewMode == ViewMode.DUAL) {
                byteOnLine -= paintDataCache.previewStartChar;
            }
        }

        if (byteOnLine >= bytesPerLine) {
            byteOnLine = bytesPerLine - 1;
        }

        dataPosition = byteOnLine + (cursorLineY * bytesPerLine) - scrollPosition.lineByteShift;
        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

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

    public void notifyCaretMoved() {
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
        computePaintData();

        for (DataChangedListener dataChangedListener : dataChangedListeners) {
            dataChangedListener.dataChanged();
        }
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

        int positionByte;
        if (section == Section.CODE_MATRIX) {
            positionByte = computeByteCharPos((int) (position % paintDataCache.bytesPerLine)) + caret.getCodeOffset();
        } else {
            positionByte = (int) (position % paintDataCache.bytesPerLine);
            if (viewMode == ViewMode.DUAL) {
                positionByte += paintDataCache.previewStartChar;
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
        if (scrollPosition.verticalMaxMode) {
            long lines = ((data.getDataSize() + scrollPosition.lineByteShift) / paintDataCache.bytesPerLine) + 1;
            int scrollValue;
            if (scrollPosition.scrollLinePosition < Long.MAX_VALUE / Integer.MAX_VALUE) {
                scrollValue = (int) ((scrollPosition.scrollLinePosition * Integer.MAX_VALUE) / lines);
            } else {
                scrollValue = (int) (scrollPosition.scrollLinePosition / (lines / Integer.MAX_VALUE));
            }
            verticalScrollBar.setSelection(scrollValue);
        } else if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
            verticalScrollBar.setSelection((int) scrollPosition.scrollLinePosition);
        } else {
            verticalScrollBar.setSelection((int) (scrollPosition.scrollLinePosition * paintDataCache.lineHeight + scrollPosition.scrollLineOffset));
        }

        if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
            horizontalScrollBar.setSelection(scrollPosition.scrollCharPosition);
        } else {
            horizontalScrollBar.setSelection(scrollPosition.scrollCharPosition * paintDataCache.charWidth + scrollPosition.scrollCharOffset);
        }
        redraw();
    }

    public void updateSelection(int modifiers, CaretPosition caretPosition) {
        if ((modifiers) > 0) { // if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0) {
            long currentPosition = caret.getDataPosition();
            long end = currentPosition;
            long start;
            if (selection != null) {
                start = selection.getStart();
                if (start == currentPosition) {
                    clearSelection();
                } else {
                    selection.setEnd(start < currentPosition ? end - 1 : end);
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
        redraw();
    }

    public void moveRight(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() < data.getDataSize()) {
            if (caret.getSection() == Section.CODE_MATRIX) {
                int codeOffset = caret.getCodeOffset();
                if (caretPosition.getDataPosition() < data.getDataSize()) {
                    if (codeOffset < codeType.getMaxDigits() - 1) {
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
                caret.setCaretPosition(caretPosition.getDataPosition() - 1, codeType.getMaxDigits() - 1);
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
            redraw();
        }
    }

    public void clearSelection() {
        selection = null;
        notifySelectionChanged();
        redraw();
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

    public void addEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.add(editModeChangedListener);
    }

    public void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.remove(editModeChangedListener);
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

    public int getLinesPerRect() {
        return paintDataCache.linesPerRect;
    }

    public int getCharsPerLine() {
        return paintDataCache.charsPerLine;
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

    public BinaryData getBinaryData() {
        return data;
    }

    public void setData(BinaryData data) {
        this.data = data;
        notifyDataChanged();
        computePaintData();
        redraw();
    }

    @Override
    public long getDataSize() {
        return data == null ? 0 : data.getDataSize();
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
        redraw();
    }

    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(CodeAreaPainter painter) {
        if (painter == null) {
            throw new NullPointerException("Painter cannot be null");
        }

        this.painter = painter;
        redraw();
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

//    @Override
//    public void setBorder(Border border) {
//        super.setBorder(border);
//        computePaintData();
//    }
    private void computeFontMetrics(GC g) {
        paintDataCache.fontMetrics = g.getFontMetrics();
    }

    private void computeFontMetrics() {
        Font font = getFont();

        /**
         * Use small 'w' character to guess normal font width.
         */
        paintDataCache.charWidth = paintDataCache.charWidth('w');
        /**
         * Compare it to small 'i' to detect if font is monospaced.
         *
         * TODO: Is there better way?
         */
        paintDataCache.monospaceFont = paintDataCache.charWidth == paintDataCache.charWidth(' ') && paintDataCache.charWidth == paintDataCache.charWidth('i');
        int fontHeight = font.getFontData()[0].getHeight();
        if (paintDataCache.charWidth == 0) {
            paintDataCache.charWidth = fontHeight;
        }
        paintDataCache.lineHeight = fontHeight + subFontSpace;
        computePaintData();
    }

    public void computePaintData() {
        if (paintDataCache.fontMetrics == null) {
            return;
        }

        boolean verticalScrollBarVisible;
        boolean horizontalScrollBarVisible;

//        Insets insets = getInsets();
        Point size = getSize();
        Rectangle compRect = paintDataCache.componentRectangle;
        compRect.x = 0; // insets.left;
        compRect.y = 0; // insets.top;
        compRect.width = size.x; // - insets.left - insets.right;
        compRect.height = size.y; // - insets.top - insets.bottom;

        switch (lineNumberLength.getLineNumberType()) {
            case AUTO: {
                long dataSize = getDataSize();
                if (dataSize > 0) {
                    double natLog = Math.log(dataSize);
                    paintDataCache.lineNumbersLength = (int) Math.ceil(natLog / positionCodeType.getBaseLog());
                    if (paintDataCache.lineNumbersLength == 0) {
                        paintDataCache.lineNumbersLength = 1;
                    }
                } else {
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
            bytesPerLine = computeFittingBytes(charsPerRect);
            if (bytesPerLine == 0) {
                bytesPerLine = 1;
            }
        } else {
            bytesPerLine = lineLength;
        }
        long lines = ((data.getDataSize() + scrollPosition.lineByteShift) / bytesPerLine) + 1;
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
                throw CodeAreaUtils.getInvalidTypeException(headerSpaceType);
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
                throw CodeAreaUtils.getInvalidTypeException(lineNumberSpaceType);
        }

        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        hexRect.y = 0 + (showHeader ? paintDataCache.lineHeight + paintDataCache.headerSpace : 0); // insets.top +
        hexRect.x = 0 + (showLineNumbers ? paintDataCache.charWidth * paintDataCache.lineNumbersLength + paintDataCache.lineNumberSpace : 0); // insets.left +

        if (verticalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
            verticalScrollBarVisible = lines > paintDataCache.linesPerRect;
        } else {
            verticalScrollBarVisible = verticalScrollBarVisibility == ScrollBarVisibility.ALWAYS;
        }
        if (verticalScrollBarVisible) {
            charsPerRect = computeCharsPerRect(compRect.x + compRect.width - paintDataCache.scrollBarThickness);
            if (wrapMode) {
                bytesPerLine = computeFittingBytes(charsPerRect);
                if (bytesPerLine <= 0) {
                    bytesPerLine = 1;
                }
                lines = ((data.getDataSize() + scrollPosition.lineByteShift) / bytesPerLine) + 1;
            }
        }

        paintDataCache.bytesPerLine = bytesPerLine;
        paintDataCache.charsPerLine = computeCharsPerLine(bytesPerLine);

        int maxWidth = compRect.x + compRect.width - hexRect.x;
        if (verticalScrollBarVisible) {
            maxWidth -= paintDataCache.scrollBarThickness;
        }

        if (horizontalScrollBarVisibility == ScrollBarVisibility.IF_NEEDED) {
            horizontalScrollBarVisible = paintDataCache.charsPerLine * paintDataCache.charWidth > maxWidth;
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

        // Compute sections positions
        paintDataCache.previewStartChar = 0;
        if (viewMode == ViewMode.CODE_MATRIX) {
            paintDataCache.previewX = -1;
        } else {
            paintDataCache.previewX = hexRect.x;
            if (viewMode == ViewMode.DUAL) {
                paintDataCache.previewStartChar = paintDataCache.charsPerLine - paintDataCache.bytesPerLine;
                paintDataCache.previewX += (paintDataCache.charsPerLine - paintDataCache.bytesPerLine) * paintDataCache.charWidth;
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
            // TODO
            // verticalScrollBar.setBounds(compRect.x + compRect.width - paintDataCache.scrollBarThickness, hexRect.y, paintDataCache.scrollBarThickness, verticalScrollBarHeight);

            int verticalVisibleAmount;
            scrollPosition.verticalMaxMode = false;
            int verticalMaximum;
            if (verticalScrollMode == VerticalScrollMode.PIXEL) {
                if (lines * paintDataCache.lineHeight > Integer.MAX_VALUE) {
                    scrollPosition.verticalMaxMode = true;
                    verticalMaximum = Integer.MAX_VALUE;
                    verticalVisibleAmount = (int) (hexRect.height * Integer.MAX_VALUE / lines);
                } else {
                    verticalMaximum = (int) (lines * paintDataCache.lineHeight);
                    verticalVisibleAmount = hexRect.height;
                }
            } else if (lines > Integer.MAX_VALUE) {
                scrollPosition.verticalMaxMode = true;
                verticalMaximum = Integer.MAX_VALUE;
                verticalVisibleAmount = (int) (hexRect.height * Integer.MAX_VALUE / paintDataCache.lineHeight / lines);
            } else {
                verticalMaximum = (int) lines;
                verticalVisibleAmount = hexRect.height / paintDataCache.lineHeight;
            }
            if (verticalVisibleAmount == 0) {
                verticalVisibleAmount = 1;
            }
            verticalScrollBar.setMaximum(verticalMaximum);
            verticalScrollBar.setSelection(verticalVisibleAmount);

            // Cap vertical scrolling
            if (!scrollPosition.verticalMaxMode && verticalVisibleAmount < verticalMaximum) {
                long maxLineScroll = verticalMaximum - verticalVisibleAmount;
                if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
                    long lineScroll = scrollPosition.scrollLinePosition;
                    if (lineScroll > maxLineScroll) {
                        scrollPosition.scrollLinePosition = maxLineScroll;
                        scrolled = true;
                    }
                } else {
                    long lineScroll = scrollPosition.scrollLinePosition * paintDataCache.lineHeight + scrollPosition.scrollLineOffset;
                    if (lineScroll > maxLineScroll) {
                        scrollPosition.scrollLinePosition = maxLineScroll / paintDataCache.lineHeight;
                        scrollPosition.scrollLineOffset = (int) (maxLineScroll % paintDataCache.lineHeight);
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
            // TODO
            // horizontalScrollBar.setBounds(hexRect.x, compRect.y + compRect.height - paintDataCache.scrollBarThickness, horizontalScrollBarWidth, paintDataCache.scrollBarThickness);

            int horizontalVisibleAmount;
            int horizontalMaximum = paintDataCache.charsPerLine;
            if (horizontalScrollMode == HorizontalScrollMode.PIXEL) {
                horizontalVisibleAmount = hexRect.width;
                horizontalMaximum *= paintDataCache.charWidth;
            } else {
                horizontalVisibleAmount = hexRect.width / paintDataCache.charWidth;
            }
            horizontalScrollBar.setMaximum(horizontalMaximum);
            horizontalScrollBar.setSelection(horizontalVisibleAmount);

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

    private void validateLineOffset() {
        if (paintDataCache.bytesPerLine > 0 && paintDataCache.bytesPerLine <= scrollPosition.lineByteShift) {
            scrollPosition.setLineByteShift(scrollPosition.lineByteShift % paintDataCache.bytesPerLine);
        }
    }

    private int computeCharsPerRect(int width) {
        if (showLineNumbers) {
            width -= paintDataCache.charWidth * paintDataCache.lineNumbersLength + getLineNumberSpace();
        }

        return width / paintDataCache.charWidth;
    }

    public int computeFittingBytes(int charsPerRect) {
        if (viewMode == ViewMode.TEXT_PREVIEW) {
            return charsPerRect;
        }

        int fittingBytes = computeByteOffsetPerCodeCharOffset(charsPerRect, viewMode == ViewMode.DUAL);
        if (byteGroupSize != 0 || spaceGroupSize != 0) {
            if (computeCharsPerLine(fittingBytes + 1) <= charsPerRect) {
                fittingBytes++;
            }
        }

        return fittingBytes;
    }

    /**
     * Computes byte offset index for given code line offset.
     *
     * @param charOffset char offset position
     * @param includePreview flag if preview should be included
     * @return byte offset index
     */
    public int computeByteOffsetPerCodeCharOffset(int charOffset, boolean includePreview) {
        int byteOffset;
        if (byteGroupSize == 0) {
            if (spaceGroupSize == 0) {
                byteOffset = (charOffset - (includePreview ? 1 : 0))
                        / (codeType.getMaxDigits() + (includePreview ? 1 : 0));
            } else {
                byteOffset = (int) (((long) (charOffset - (includePreview ? 1 : 0)) * spaceGroupSize)
                        / ((long) (codeType.getMaxDigits() + (includePreview ? 1 : 0)) * spaceGroupSize + 2));
            }
        } else if (spaceGroupSize == 0) {
            byteOffset = (int) (((long) (charOffset - (includePreview ? 1 : 0)) * byteGroupSize)
                    / ((long) (codeType.getMaxDigits() + (includePreview ? 1 : 0)) * byteGroupSize + 1));
        } else {
            byteOffset = 0;
            int charsPerLine = includePreview ? 1 : 0;
            while (charsPerLine < charOffset) {
                charsPerLine += codeType.getMaxDigits() + (includePreview ? 1 : 0);
                byteOffset++;
                if ((byteOffset % byteGroupSize) == 0) {
                    if ((byteOffset % spaceGroupSize) == 0) {
                        charsPerLine += 2;
                    } else {
                        charsPerLine++;
                    }
                } else if ((byteOffset % spaceGroupSize) == 0) {
                    charsPerLine += 2;
                }
                if (charsPerLine > charOffset) {
                    return byteOffset - 1;
                }
            }
        }

        return byteOffset;
    }

    /**
     * Computes number of characters for given number of bytes / offset.
     *
     * @param bytesPerLine number of bytes per line
     * @return characters count
     */
    public int computeCharsPerLine(int bytesPerLine) {
        if (viewMode == ViewMode.TEXT_PREVIEW) {
            return bytesPerLine;
        }

        int charsPerLine = computeByteCharPos(bytesPerLine, false);

        if (viewMode == ViewMode.DUAL) {
            charsPerLine += bytesPerLine + 1;
        }

        return charsPerLine;
    }

    /**
     * Computes character position for byte code of given offset position
     *
     * @param byteOffset byte start offset
     * @return characters position
     */
    public int computeByteCharPos(int byteOffset) {
        return computeByteCharPos(byteOffset, true);
    }

    public int computeByteCharPos(int byteOffset, boolean includeTail) {
        int charsPerLine = codeType.getMaxDigits() * byteOffset;
        if (!includeTail) {
            byteOffset--;
        }
        if (byteGroupSize == 0) {
            if (spaceGroupSize != 0) {
                charsPerLine += (byteOffset / spaceGroupSize) * 2;
            }
        } else if (spaceGroupSize == 0) {
            charsPerLine += (byteOffset / byteGroupSize);
        } else {
            for (int index = 1; index <= byteOffset; index++) {
                if ((index % byteGroupSize) == 0) {
                    if ((index % spaceGroupSize) == 0) {
                        charsPerLine += 2;
                    } else {
                        charsPerLine++;
                    }
                } else if ((index % spaceGroupSize) == 0) {
                    charsPerLine += 2;
                }
            }
        }

        return charsPerLine;
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
        redraw();
    }

    public void setAlternateColors(ColorsGroup colorsGroup) {
        alternateColors.setColors(colorsGroup);
        redraw();
    }

    public void setSelectionColors(ColorsGroup colorsGroup) {
        selectionColors.setColors(colorsGroup);
        redraw();
    }

    public void setMirrorSelectionColors(ColorsGroup colorsGroup) {
        mirrorSelectionColors.setColors(colorsGroup);
        redraw();
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
        negativeCursorColor = createNegativeColor(cursorColor);
        redraw();
    }

    public Color getNegativeCursorColor() {
        return negativeCursorColor;
    }

    public Color getDecorationLineColor() {
        return decorationLineColor;
    }

    public void setDecorationLineColor(Color decorationLineColor) {
        this.decorationLineColor = decorationLineColor;
        redraw();
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
        redraw();
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        computePaintData();
        redraw();
    }

    public PositionCodeType getPositionCodeType() {
        return positionCodeType;
    }

    public void setPositionCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = positionCodeType;
        computePaintData();
        redraw();
    }

    public BackgroundPaintMode getBackgroundPaintMode() {
        return backgroundPaintMode;
    }

    public void setBackgroundPaintMode(BackgroundPaintMode backgroundPaintMode) {
        this.backgroundPaintMode = backgroundPaintMode;
        redraw();
    }

    public boolean isLineNumberBackground() {
        return lineNumberBackground;
    }

    public void setLineNumberBackground(boolean lineNumberBackground) {
        this.lineNumberBackground = lineNumberBackground;
        redraw();
    }

    public int getDecorationMode() {
        return decorationMode;
    }

    public void setDecorationMode(int decorationMode) {
        this.decorationMode = decorationMode;
        redraw();
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
        redraw();
    }

    public EditAllowed getEditAllowed() {
        return editAllowed;
    }

    public void setEditAllowed(EditAllowed editAllowed) {
        this.editAllowed = editAllowed;
        switch (editAllowed) {
            case READ_ONLY: {
                editMode = EditMode.INSERT;
                break;
            }
            case OVERWRITE_ONLY: {
                editMode = EditMode.OVERWRITE;
                break;
            }
            default: // ignore
        }
        redraw();
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        switch (editAllowed) {
            case READ_ONLY: {
                editMode = EditMode.INSERT;
                break;
            }
            case OVERWRITE_ONLY: {
                editMode = EditMode.OVERWRITE;
                break;
            }
            default: // ignore
        }
        boolean chaged = editMode != this.editMode;
        this.editMode = editMode;
        if (chaged) {
            for (EditModeChangedListener listener : editModeChangedListeners) {
                listener.editModeChanged(editMode);
            }
            caret.resetBlink();
            redraw();
        }
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        computePaintData();
        redraw();
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
        computePaintData();
        redraw();
    }

    public boolean isEditable() {
        return editAllowed != EditAllowed.READ_ONLY;
    }

    public void setEditable(boolean editable) {
        setEditAllowed(EditAllowed.ALLOWED);
    }

    public boolean isWrapMode() {
        return wrapMode;
    }

    public void setWrapMode(boolean wrapMode) {
        this.wrapMode = wrapMode;
        computePaintData();
        validateLineOffset();
        redraw();
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
        redraw();
    }

    public boolean isShowShadowCursor() {
        return showShadowCursor;
    }

    public void setShowShadowCursor(boolean showShadowCursor) {
        this.showShadowCursor = showShadowCursor;
        redraw();
    }

    public int getLineLength() {
        return lineLength;
    }

    public void setLineLength(int lineLength) {
        if (lineLength < 1) {
            throw new IllegalStateException("Line length must be at least 1");
        }
        this.lineLength = lineLength;
        if (!wrapMode) {
            computePaintData();
            redraw();
        }
    }

    public int getByteGroupSize() {
        return byteGroupSize;
    }

    public void setByteGroupSize(int byteGroupSize) {
        if (byteGroupSize < 0) {
            throw new IllegalStateException("Negative group size is not valid");
        }
        this.byteGroupSize = byteGroupSize;
        computePaintData();
        redraw();
    }

    public int getSpaceGroupSize() {
        return spaceGroupSize;
    }

    public void setSpaceGroupSize(int spaceGroupSize) {
        if (spaceGroupSize < 0) {
            throw new IllegalStateException("Negative group size is not valid");
        }
        this.spaceGroupSize = spaceGroupSize;
        computePaintData();
        redraw();
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
        redraw();
    }

    public CharAntialiasingMode getCharAntialiasingMode() {
        return charAntialiasingMode;
    }

    public void setCharAntialiasingMode(CharAntialiasingMode charAntialiasingMode) {
        this.charAntialiasingMode = charAntialiasingMode;
        redraw();
    }

    public HexCharactersCase getHexCharactersCase() {
        return hexCharactersCase;
    }

    public void setHexCharactersCase(HexCharactersCase hexCharactersCase) {
        this.hexCharactersCase = hexCharactersCase;
        redraw();
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
        redraw();
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
        redraw();
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
        redraw();
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
        redraw();
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
        redraw();
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
        redraw();
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

    /**
     * Resets position of cursor and scrollbars.
     *
     * Useful for opening new file.
     */
    public void resetPosition() {
        getScrollPosition().reset();
        updateScrollBars();
        notifyScrolled();
        caret.setCaretPosition(0);
        notifyCaretMoved();
        commandHandler.caretMoved();
        computePaintData();
        clearSelection();
    }

    private static Color createOddColor(Color color) {
        Display display = Display.getDefault();
        return new Color(display,
                computeOddColorComponent(color.getRed()),
                computeOddColorComponent(color.getGreen()),
                computeOddColorComponent(color.getBlue()));
    }

    private static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? - 16 : 16);
    }

    private static Color createNegativeColor(Color color) {
        Display display = Display.getDefault();
        return new Color(display,
                255 - color.getRed(),
                255 - color.getGreen(),
                255 - color.getBlue());
    }

    public static enum BackgroundPaintMode {
        NONE, PLAIN, STRIPPED, GRIDDED
    }

    public static enum VerticalScrollMode {
        PER_LINE, PIXEL
    }

    public static enum HorizontalScrollMode {
        PER_CHAR, PIXEL
    }

    /**
     * Character rendering mode.
     */
    public static enum CharRenderingMode {
        /**
         * Centers characters if width is not default and detects monospace
         * fonts to render characters as string if possible
         */
        AUTO,
        /**
         * Render sequence of characters from top left corner of the line
         * ignoring character width. It's fastest, but render correctly only for
         * monospaced fonts and charsets where all characters have same width
         */
        LINE_AT_ONCE,
        /**
         * Render each character from top left corner of it's position
         */
        TOP_LEFT,
        /**
         * Centers each character in it's area
         */
        CENTER
    }

    public static enum CharAntialiasingMode {
        OFF, AUTO, DEFAULT, BASIC, GASP, LCD_HRGB, LCD_HBGR, LCD_VRGB, LCD_VBGR
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
        int charsPerLine;
        int lineNumbersLength;

        /**
         * Component area without border insets.
         */
        final Rectangle componentRectangle = new Rectangle(0, 0, 0, 0);
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
        final Rectangle codeSectionRectangle = new Rectangle(0, 0, 0, 0);
        int previewX;
        int previewStartChar;
        int bytesPerRect;
        int linesPerRect;
        int scrollBarThickness = 17;

        int charWidth(char character) {
            // TODO
            return charWidth;
        }
    }

    /**
     * Scrolling position.
     */
    public static class ScrollPosition {

        private long scrollLinePosition = 0;
        private int scrollLineOffset = 0;
        private int scrollCharPosition = 0;
        private int scrollCharOffset = 0;
        /**
         * How is start of the line scrolled compare it's normal position.
         */
        private int lineByteShift = 0;
        /**
         * Flag for scroll mode with huge data.
         */
        private boolean verticalMaxMode = false;

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

        public int getLineByteShift() {
            return lineByteShift;
        }

        public void setScrollLinePosition(long scrollLinePosition) {
            this.scrollLinePosition = scrollLinePosition;
        }

        public void setScrollLineOffset(int scrollLineOffset) {
            this.scrollLineOffset = scrollLineOffset;
        }

        public void setScrollCharPosition(int scrollCharPosition) {
            this.scrollCharPosition = scrollCharPosition;
        }

        public void setScrollCharOffset(int scrollCharOffset) {
            this.scrollCharOffset = scrollCharOffset;
        }

        public void setLineByteShift(int lineByteShift) {
            this.lineByteShift = lineByteShift;
        }

        public boolean isVerticalMaxMode() {
            return verticalMaxMode;
        }

        public void setVerticalMaxMode(boolean verticalMaxMode) {
            this.verticalMaxMode = verticalMaxMode;
        }

        private void reset() {
            scrollLinePosition = 0;
            scrollLineOffset = 0;
            scrollCharPosition = 0;
            scrollCharOffset = 0;
            lineByteShift = 0;
        }
    }

//    private class CodeAreaMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
//
//        private Cursor currentCursor = getCursor();
//        private final Cursor defaultCursor = Cursor.getDefaultCursor();
//        private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
//
//        @Override
//        public void mousePressed(MouseEvent me) {
//            requestFocus();
//            if (isEnabled() && me.getButton() == MouseEvent.BUTTON1) {
//                moveCaret(me, me.getModifiersEx());
//                revealCursor();
//                mouseDown = true;
//            }
//        }
//
//        @Override
//        public void mouseReleased(MouseEvent me) {
//            mouseDown = false;
//        }
//
//        @Override
//        public void mouseExited(MouseEvent e) {
//            currentCursor = defaultCursor;
//            setCursor(defaultCursor);
//        }
//
//        @Override
//        public void mouseEntered(MouseEvent e) {
//            updateMouseCursor(e);
//        }
//
//        @Override
//        public void mouseMoved(MouseEvent e) {
//            updateMouseCursor(e);
//        }
//
//        private void updateMouseCursor(MouseEvent e) {
//            Cursor newCursor = defaultCursor;
//            Rectangle hexRect = paintDataCache.codeSectionRectangle;
//            if (e.getX() >= hexRect.x && e.getY() >= hexRect.y) {
//                newCursor = textCursor;
//            }
//
//            if (newCursor != currentCursor) {
//                currentCursor = newCursor;
//                setCursor(newCursor);
//            }
//        }
//
//        @Override
//        public void mouseDragged(MouseEvent me) {
//            updateMouseCursor(me);
//            if (isEnabled() && mouseDown) {
//                moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
//                revealCursor();
//            }
//        }
//
//        @Override
//        public void mouseWheelMoved(MouseWheelEvent e) {
//            if (!isEnabled()) {
//                return;
//            }
//
//            if (e.isShiftDown() && horizontalScrollBar.isVisible()) {
//                if (e.getWheelRotation() > 0) {
//                    // TODO
//                    int visibleChars = paintDataCache.codeSectionRectangle.width / paintDataCache.charWidth;
//                    int bytes = paintDataCache.bytesPerLine - visibleChars;
//                    if (scrollPosition.scrollCharPosition < bytes) {
//                        if (scrollPosition.scrollCharPosition < bytes - MOUSE_SCROLL_LINES) {
//                            scrollPosition.scrollCharPosition += MOUSE_SCROLL_LINES;
//                        } else {
//                            scrollPosition.scrollCharPosition = bytes;
//                        }
//                        updateScrollBars();
//                        notifyScrolled();
//                    }
//                } else if (scrollPosition.scrollCharPosition > 0) {
//                    if (scrollPosition.scrollCharPosition > MOUSE_SCROLL_LINES) {
//                        scrollPosition.scrollCharPosition -= MOUSE_SCROLL_LINES;
//                    } else {
//                        scrollPosition.scrollCharPosition = 0;
//                    }
//                    updateScrollBars();
//                    notifyScrolled();
//                }
//            } else if (e.getWheelRotation() > 0) {
//                long lines = (data.getDataSize() + scrollPosition.lineByteShift) / paintDataCache.bytesPerLine;
//                if (lines * paintDataCache.bytesPerLine < data.getDataSize()) {
//                    lines++;
//                }
//                lines -= paintDataCache.linesPerRect;
//                if (scrollPosition.scrollLinePosition < lines) {
//                    if (scrollPosition.scrollLinePosition < lines - MOUSE_SCROLL_LINES) {
//                        scrollPosition.scrollLinePosition += MOUSE_SCROLL_LINES;
//                    } else {
//                        scrollPosition.scrollLinePosition = lines;
//                    }
//                    updateScrollBars();
//                    notifyScrolled();
//                }
//            } else if (scrollPosition.scrollLinePosition > 0) {
//                if (scrollPosition.scrollLinePosition > MOUSE_SCROLL_LINES) {
//                    scrollPosition.scrollLinePosition -= MOUSE_SCROLL_LINES;
//                } else {
//                    scrollPosition.scrollLinePosition = 0;
//                }
//                updateScrollBars();
//                notifyScrolled();
//            }
//        }
//    }
    private class CodeAreaKeyListener extends KeyAdapter {

        public CodeAreaKeyListener() {
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            commandHandler.keyPressed(keyEvent);
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            commandHandler.keyTyped(keyEvent);
        }
    }

//    private class CodeAreaComponentListener implements ComponentListener {
//
//        public CodeAreaComponentListener() {
//        }
//
//        @Override
//        public void componentResized(ComponentEvent e) {
//            computePaintData();
//            validateLineOffset();
//        }
//
//        @Override
//        public void componentMoved(ComponentEvent e) {
//        }
//
//        @Override
//        public void componentShown(ComponentEvent e) {
//        }
//
//        @Override
//        public void componentHidden(ComponentEvent e) {
//        }
//    }
//
//    private class VerticalAdjustmentListener implements AdjustmentListener {
//
//        public VerticalAdjustmentListener() {
//        }
//
//        @Override
//        public void adjustmentValueChanged(AdjustmentEvent e) {
//            int scrollBarValue = verticalScrollBar.getValue();
//            if (scrollPosition.verticalMaxMode) {
//                int maxValue = Integer.MAX_VALUE - verticalScrollBar.getVisibleAmount();
//                long lines = ((data.getDataSize() + scrollPosition.lineByteShift) / paintDataCache.bytesPerLine) - paintDataCache.linesPerRect + 1;
//                long targetLine;
//                if (scrollBarValue > 0 && lines > maxValue / scrollBarValue) {
//                    targetLine = scrollBarValue * (lines / maxValue);
//                    long rest = lines % maxValue;
//                    targetLine += (rest * scrollBarValue) / maxValue;
//                } else {
//                    targetLine = (scrollBarValue * lines) / Integer.MAX_VALUE;
//                }
//                scrollPosition.scrollLinePosition = targetLine;
//                if (verticalScrollMode != VerticalScrollMode.PER_LINE) {
//                    scrollPosition.scrollLineOffset = 0;
//                }
//            } else if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
//                scrollPosition.scrollLinePosition = scrollBarValue;
//            } else {
//                scrollPosition.scrollLinePosition = scrollBarValue / paintDataCache.lineHeight;
//                scrollPosition.scrollLineOffset = scrollBarValue % paintDataCache.lineHeight;
//            }
//
//            redraw();
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
//            if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
//                scrollPosition.scrollCharPosition = horizontalScrollBar.getValue();
//            } else {
//                scrollPosition.scrollCharPosition = horizontalScrollBar.getValue() / paintDataCache.charWidth;
//                scrollPosition.scrollCharOffset = horizontalScrollBar.getValue() % paintDataCache.charWidth;
//            }
//            redraw();
//            notifyScrolled();
//        }
//    }
}
