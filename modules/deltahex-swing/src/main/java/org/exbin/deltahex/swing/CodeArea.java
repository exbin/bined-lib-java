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
package org.exbin.deltahex.swing;

import com.sun.istack.internal.NotNull;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.exbin.deltahex.CaretMovedListener;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaControl;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.DataChangedListener;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.EditationModeChangedListener;
import org.exbin.deltahex.ScrollBarVisibility;
import org.exbin.deltahex.ScrollingListener;
import org.exbin.deltahex.SelectionChangedListener;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.ViewMode;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * @version 0.2.0 2017/04/10
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends JComponent implements CodeAreaControl {

    private BinaryData data;

    private CodeAreaCaret caret;
    private SelectionRange selection;
    private JScrollPane scrollPanel;
    private CodeAreaDataView dataView;
    private Charset charset = Charset.defaultCharset();

    private CodeAreaPainter painter;
    private CodeAreaCommandHandler commandHandler;

    private ViewMode viewMode = ViewMode.DUAL;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private EditationMode editationMode = EditationMode.OVERWRITE;

    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollUnit verticalScrollMode = VerticalScrollUnit.LINE;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollUnit horizontalScrollMode = HorizontalScrollUnit.PIXEL;
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    private VerticalOverflowMode verticalOverflowMode = VerticalOverflowMode.NORMAL;

    /*
     * Listeners.
     */
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();
    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     */
    public CodeArea() {
        this(new DefaultCodeAreaCommandHandler(this), new DefaultCodeAreaPainter(this));
    }

    /**
     * Creates new instance with command handler and painter.
     *
     * @param commandHandler command handler
     * @param painter painter
     */
    public CodeArea(@NotNull CodeAreaCommandHandler commandHandler, @NotNull CodeAreaPainter painter) {
        super();
        this.commandHandler = commandHandler;
        this.painter = painter;
        init();
    }

    private void init() {
        caret = new CodeAreaCaret(this);
        scrollPanel = new JScrollPane();
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        verticalScrollBar.setVisible(false);
        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        horizontalScrollBar.setIgnoreRepaint(true);
        horizontalScrollBar.setVisible(false);
        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
        add(scrollPanel);
        dataView = new CodeAreaDataView(this);
        scrollPanel.setViewportView(dataView);

        // TODO buildColors();
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                computePaintData();
                validateLineOffset();
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
        });

        CodeAreaMouseListener codeAreaMouseListener = new CodeAreaMouseListener(this);
        addMouseListener(codeAreaMouseListener);
        addMouseMotionListener(codeAreaMouseListener);
        addMouseWheelListener(codeAreaMouseListener);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                commandHandler.keyTyped(keyEvent);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                commandHandler.keyPressed(keyEvent);
            }
        });

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
        UIManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // TODO painter.rebuildColors();
            }
        });
    }

    public CodeAreaCaret getCaret() {
        return caret;
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    public CodeAreaSection getActiveSection() {
        return caret.getSection();
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

    @Override
    public void copy() {
        commandHandler.copy();
    }

    @Override
    public void copyAsCode() {
        commandHandler.copyAsCode();
    }

    @Override
    public void cut() {
        commandHandler.cut();
    }

    @Override
    public void paste() {
        commandHandler.paste();
    }

    @Override
    public void pasteFromCode() {
        commandHandler.pasteFromCode();
    }

    @Override
    public void delete() {
        commandHandler.delete();
    }

    @Override
    public void selectAll() {
        commandHandler.selectAll();
    }

    @Override
    public void clearSelection() {
        commandHandler.clearSelection();
    }

    @Override
    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    @Override
    public boolean hasSelection() {
        return !selection.isEmpty();
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

    public long getDataSize() {
        return data == null ? 0 : data.getDataSize();
    }

    /**
     * Returns currently used charset.
     *
     * @return charset
     */
    @NotNull
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets charset to use for characters decoding.
     *
     * @param charset charset
     */
    public void setCharset(@NotNull Charset charset) {
        if (charset == null) {
            throw new NullPointerException("Charset cannot be null");
        }

        this.charset = charset;
        repaint();
    }

    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(@NotNull CodeAreaPainter painter) {
        if (painter == null) {
            throw new NullPointerException("Painter cannot be null");
        }

        this.painter = painter;
        repaint();
    }

    @NotNull
    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(@NotNull ViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == ViewMode.CODE_MATRIX) {
            caret.setSection(CodeAreaSection.CODE_MATRIX);
            notifyCaretMoved();
        } else if (viewMode == ViewMode.TEXT_PREVIEW) {
            caret.setSection(CodeAreaSection.TEXT_PREVIEW);
            notifyCaretMoved();
        }
        computePaintData();
        repaint();
    }

    @NotNull
    public EditationMode getEditationMode() {
        return editationMode;
    }

    public void setEditationMode(@NotNull EditationMode editationMode) {
        boolean changed = editationMode != this.editationMode;
        this.editationMode = editationMode;
        if (changed) {
            for (EditationModeChangedListener listener : editationModeChangedListeners) {
                listener.editationModeChanged(editationMode);
            }
            caret.resetBlink();
            repaint();
        }
    }

    public void computePaintData() {
        if (paintDataCache.fontMetrics == null) {
            return;
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
            verticalScrollBar.setBounds(compRect.x + compRect.width - paintDataCache.scrollBarThickness, hexRect.y, paintDataCache.scrollBarThickness, verticalScrollBarHeight);

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
            verticalScrollBar.setVisibleAmount(verticalVisibleAmount);

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
            horizontalScrollBar.setBounds(hexRect.x, compRect.y + compRect.height - paintDataCache.scrollBarThickness, horizontalScrollBarWidth, paintDataCache.scrollBarThickness);

            int horizontalVisibleAmount;
            int horizontalMaximum = paintDataCache.charsPerLine;
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

    private void moveCaret(MouseEvent me, int modifiers) {
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        int bytesPerLine = paintDataCache.bytesPerLine;
        int mouseX = me.getX();
        if (mouseX < hexRect.x) {
            mouseX = hexRect.x;
        }
        int cursorCharX = (mouseX - hexRect.x + scrollPosition.scrollCharOffset) / paintDataCache.charWidth + scrollPosition.scrollCharPosition;
        long cursorLineY = (me.getY() - hexRect.y + scrollPosition.scrollLineOffset) / paintDataCache.lineHeight + scrollPosition.scrollLinePosition;
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
            byteOnLine = computeByteOffsetPerCodeCharOffset(cursorCharX);
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
        commandHandler.sequenceBreak();

        updateSelection(modifiers, caretPosition);
    }

    public void notifyCaretMoved() {
        for (CaretMovedListener caretMovedListener : caretMovedListeners) {
            caretMovedListener.caretMoved(caret.getCaretPosition());
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

    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    public void revealCursor() {
        revealPosition(caret.getCaretPosition().getDataPosition(), caret.getSection());
    }

    public void revealPosition(long position, CodeAreaSection section) {
        if (paintDataCache.fontMetrics == null) {
            // Ignore if no font data is available
            return;
        }
        boolean scrolled = false;
        Rectangle hexRect = paintDataCache.codeSectionRectangle;
        long caretLine = position / paintDataCache.bytesPerLine;

        int positionByte;
        if (section == CodeAreaSection.CODE_MATRIX) {
            positionByte = computeByteCharPos((int) (position % paintDataCache.bytesPerLine)) + caret.getCodeOffset();
        } else {
            positionByte = (int) (position % paintDataCache.bytesPerLine);
            if (viewMode == ViewMode.DUAL) {
                positionByte += paintDataCache.previewStartChar;
            }
        }

        if (caretLine <= scrollPosition.getScrollLinePosition()) {
            scrollPosition.setScrollLinePosition(caretLine);
            scrollPosition.setScrollLineOffset(0);
            scrolled = true;
        } else if (caretLine >= scrollPosition.getScrollLinePosition() + paintDataCache.linesPerRect) {
            scrollPosition.setScrollLinePosition(caretLine - paintDataCache.linesPerRect);
            if (verticalScrollMode == VerticalScrollUnit.PIXEL) {
                scrollPosition.setScrollLineOffset(paintDataCache.lineHeight - (hexRect.height % paintDataCache.lineHeight));
            } else {
                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
            }
            scrolled = true;
        }
        if (positionByte <= scrollPosition.getScrollCharPosition()) {
            scrollPosition.setScrollCharPosition(positionByte);
            scrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else if (positionByte >= scrollPosition.getScrollCharPosition() + paintDataCache.bytesPerRect) {
            scrollPosition.setScrollCharPosition(positionByte - paintDataCache.bytesPerRect);
            if (horizontalScrollMode == HorizontalScrollUnit.PIXEL) {
                scrollPosition.setScrollCharOffset(paintDataCache.charWidth - (hexRect.width % paintDataCache.charWidth));
            } else {
                scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() + 1);
            }
            scrolled = true;
        }

        if (scrolled) {
            updateScrollBars();
            notifyScrolled();
        }
    }

    public void updateScrollBars() {
        if (verticalOverflowMode == VerticalOverflowMode.OVERFLOW) {
            long lines = ((data.getDataSize() + scrollPosition.lineByteShift) / paintDataCache.bytesPerLine) + 1;
            int scrollValue;
            if (scrollPosition.getScrollCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                scrollValue = (int) ((scrollPosition.getScrollLinePosition() * Integer.MAX_VALUE) / lines);
            } else {
                scrollValue = (int) (scrollPosition.getScrollLinePosition() / (lines / Integer.MAX_VALUE));
            }
            scrollPanel.getVerticalScrollBar().setValue(scrollValue);
        } else if (verticalScrollMode == VerticalScrollUnit.LINE) {
            scrollPanel.getVerticalScrollBar().setValue((int) scrollPosition.getScrollLinePosition());
        } else {
            scrollPanel.getVerticalScrollBar().setValue((int) (scrollPosition.getScrollLinePosition() * paintDataCache.lineHeight + scrollPosition.getScrollLineOffset()));
        }

        if (horizontalScrollMode == HorizontalScrollUnit.CHARACTER) {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition());
        } else {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition() * paintDataCache.charWidth + scrollPosition.getScrollCharOffset());
        }
        repaint();
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int scrollBarValue = verticalScrollBar.getValue();
            if (verticalOverflowMode) {
                int maxValue = Integer.MAX_VALUE - verticalScrollBar.getVisibleAmount();
                long lines = ((data.getDataSize() + scrollPosition.lineByteShift) / paintDataCache.bytesPerLine) - paintDataCache.linesPerRect + 1;
                long targetLine;
                if (scrollBarValue > 0 && lines > maxValue / scrollBarValue) {
                    targetLine = scrollBarValue * (lines / maxValue);
                    long rest = lines % maxValue;
                    targetLine += (rest * scrollBarValue) / maxValue;
                } else {
                    targetLine = (scrollBarValue * lines) / Integer.MAX_VALUE;
                }
                scrollPosition.setScrollLinePosition(targetLine);
                if (verticalScrollMode != VerticalScrollUnit.LINE) {
                    scrollPosition.setScrollLineOffset(0);
                }
            } else if (verticalScrollMode == VerticalScrollUnit.LINE) {
                scrollPosition.setScrollLinePosition(scrollBarValue);
            } else {
                scrollPosition.setScrollLinePosition(scrollBarValue / paintDataCache.lineHeight);
                scrollPosition.setScrollLineOffset(scrollBarValue % paintDataCache.lineHeight);
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
            codeArea.repaint();
            codeArea.notifyScrolled();
        }
    }

    /**
     * Enumeration of vertical scrolling unit sizes.
     */
    public enum VerticalScrollUnit {
        /**
         * Sroll per whole line.
         */
        LINE,
        /**
         * Sroll per pixel.
         */
        PIXEL
    }

    /**
     * Enumeration of horizontal scrolling unit sizes.
     */
    public enum HorizontalScrollUnit {
        /**
         * Sroll per whole character.
         */
        CHARACTER,
        /**
         * Sroll per pixel.
         */
        PIXEL
    }

    /**
     * Enumeration of vertical overflow modes.
     */
    public enum VerticalOverflowMode {
        /**
         * Normal ratio 1 on 1.
         */
        NORMAL,
        /**
         * Height is more than available precision and scaled.
         */
        OVERFLOW
    }
}
