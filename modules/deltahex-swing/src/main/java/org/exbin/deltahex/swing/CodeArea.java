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
 * @version 0.2.0 2017/04/21
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

    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.LINE;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;
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
        super();
        this.commandHandler = new DefaultCodeAreaCommandHandler(this);
        this.painter = new DefaultCodeAreaPainter(this);
        init();
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

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                painter.notifyModified();
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
                painter.rebuildColors();
            }
        });
    }

    public @NotNull
    CodeAreaCaret getCaret() {
        return caret;
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    public @NotNull
    CodeAreaSection getActiveSection() {
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
        repaint();
    }

    public long getDataSize() {
        return data == null ? 0 : data.getDataSize();
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
        painter.notifyModified();
        repaint();
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        painter.notifyModified();
        repaint();
    }

    @NotNull
    public EditationMode getEditationMode() {
        return editationMode;
    }

    public boolean isEditable() {
        return editationMode != EditationMode.READ_ONLY;
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

    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        painter.notifyModified();
        updateScrollBars();
    }

    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit) {
        this.verticalScrollUnit = verticalScrollUnit;
        long linePosition = scrollPosition.getScrollLinePosition();
        if (verticalScrollUnit == VerticalScrollUnit.LINE) {
            scrollPosition.setScrollLineOffset(0);
        }
        painter.notifyModified();
        scrollPosition.setScrollLinePosition(linePosition);
        updateScrollBars();
        notifyScrolled();
    }

    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        painter.notifyModified();
        updateScrollBars();
    }

    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int bytePosition = scrollPosition.getScrollCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setScrollCharOffset(0);
        }
        painter.notifyModified();
        scrollPosition.setScrollCharPosition(bytePosition);
        updateScrollBars();
        notifyScrolled();
    }

    /**
     * Returns rectangle of the data view area.
     *
     * @return rectangle
     */
    public Rectangle getDataViewRectangle() {
        return dataView.getBounds();
    }

    /**
     * Returns rectangle of the data view area.
     *
     * @return rectangle
     */
    public int getPreviewX() {
        return painter.getPreviewX();
    }
    
    public int getBytesPerRectangle() {
        return painter.getBytesPerRectangle();
    }
    
    public int getLinesPerRectangle() {
        return painter.getLinesPerRectangle();
    }

    public int getBytesPerLine() {
        return painter.getBytesPerLine();
    }

    public int getCharactersPerLine() {
        return painter.getCharactersPerLine();
    }
    
    boolean isHorizontalScrollBarVisible() {
        // TODO
        return scrollPanel.getHorizontalScrollBar().isVisible();
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

    /**
     * Notifies component, that internal data was changed.
     */
    public void notifyDataChanged() {
        if (caret.getDataPosition() > data.getDataSize()) {
            caret.setCaretPosition(0);
            notifyCaretMoved();
        }
        painter.notifyModified();

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
            if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
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
            if (horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
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
        } else if (verticalScrollUnit == VerticalScrollUnit.LINE) {
            scrollPanel.getVerticalScrollBar().setValue((int) scrollPosition.getScrollLinePosition());
        } else {
            scrollPanel.getVerticalScrollBar().setValue((int) (scrollPosition.getScrollLinePosition() * paintDataCache.lineHeight + scrollPosition.getScrollLineOffset()));
        }

        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
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
            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
            if (verticalOverflowMode == VerticalOverflowMode.OVERFLOW) {
                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
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
                if (verticalScrollUnit != VerticalScrollUnit.LINE) {
                    scrollPosition.setScrollLineOffset(0);
                }
            } else if (verticalScrollUnit == VerticalScrollUnit.LINE) {
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
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                scrollPosition.setScrollCharPosition(scrollPanel.getHorizontalScrollBar().getValue());
            } else {
                scrollPosition.setScrollCharPosition(scrollPanel.getHorizontalScrollBar().getValue() / paintDataCache.charWidth);
                scrollPosition.setScrollCharOffset(scrollPanel.getHorizontalScrollBar().getValue() % paintDataCache.charWidth);
            }
            repaint();
            notifyScrolled();
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
