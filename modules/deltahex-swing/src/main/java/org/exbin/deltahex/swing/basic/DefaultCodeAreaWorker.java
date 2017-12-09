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
package org.exbin.deltahex.swing.basic;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.exbin.deltahex.CaretMovedListener;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeCharactersCase;
import org.exbin.deltahex.CodeAreaViewMode;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.EditationModeChangedListener;
import org.exbin.deltahex.ScrollBarVisibility;
import org.exbin.deltahex.ScrollingListener;
import org.exbin.deltahex.SelectionChangedListener;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.swing.CharacterRenderingMode;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.swing.CodeAreaPainter;
import org.exbin.deltahex.swing.CodeAreaWorker;
import org.exbin.utils.binary_data.OutOfBoundsException;
import org.exbin.deltahex.capability.CaretCapable;
import org.exbin.deltahex.capability.CharsetCapable;
import org.exbin.deltahex.capability.CodeTypeCapable;
import org.exbin.deltahex.capability.EditationModeCapable;
import org.exbin.deltahex.capability.ViewModeCapable;
import org.exbin.deltahex.capability.ScrollingCapable;
import org.exbin.deltahex.capability.SelectionCapable;
import org.exbin.deltahex.capability.CodeCharactersCaseCapable;
import org.exbin.deltahex.swing.capability.AntialiasingCapable;
import org.exbin.deltahex.swing.capability.BorderPaintCapable;
import org.exbin.deltahex.swing.capability.FontCapable;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2017/12/09
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaWorker implements CodeAreaWorker, SelectionCapable, CaretCapable, ScrollingCapable, ViewModeCapable, CodeTypeCapable, EditationModeCapable, CharsetCapable, CodeCharactersCaseCapable, AntialiasingCapable, FontCapable, BorderPaintCapable {

    @Nonnull
    protected final CodeArea codeArea;

    @Nonnull
    private CodeAreaPainter painter;

    @Nonnull
    private SelectionRange selection;
    @Nonnull
    private Charset charset = Charset.defaultCharset();
    private boolean handleClipboard = true;

    @Nonnull
    private EditationMode editationMode = EditationMode.OVERWRITE;
    @Nonnull
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    @Nullable
    private Font font;
    @Nonnull
    private BasicBorderPaintMode borderPaintMode = BasicBorderPaintMode.STRIPED;
    @Nonnull
    private CharacterRenderingMode characterRenderingMode = CharacterRenderingMode.AUTO;
    @Nonnull
    private CodeType codeType = CodeType.HEXADECIMAL;
    @Nonnull
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    private boolean showShadowCursor = true;

    @Nonnull
    private DefaultCodeAreaCaret caret;

    @Nonnull
    private final JScrollPane scrollPanel;
    @Nonnull
    private final CodeAreaDataView dataView;
    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.LINE;
    @Nonnull
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();

    public DefaultCodeAreaWorker(@Nonnull CodeArea codeArea) {
        this.codeArea = codeArea;

        scrollPanel = new JScrollPane();
        scrollPanel.setBorder(null);
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        verticalScrollBar.setVisible(false);
        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new DefaultCodeAreaWorker.VerticalAdjustmentListener());
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        horizontalScrollBar.setIgnoreRepaint(false);
        horizontalScrollBar.setVisible(true);
        horizontalScrollBar.addAdjustmentListener(new DefaultCodeAreaWorker.HorizontalAdjustmentListener());
        codeArea.add(scrollPanel);
        dataView = new CodeAreaDataView(codeArea);
        dataView.setOpaque(false);
        scrollPanel.setOpaque(false);
        scrollPanel.setBackground(Color.RED);
        scrollPanel.setViewportView(dataView);
        scrollPanel.getViewport().setOpaque(false);
        caret = new DefaultCodeAreaCaret(codeArea);

        this.painter = new DefaultCodeAreaPainter(this);

        DefaultCodeAreaMouseListener codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea);
        codeArea.addMouseListener(codeAreaMouseListener);
        codeArea.addMouseMotionListener(codeAreaMouseListener);
        codeArea.addMouseWheelListener(codeAreaMouseListener);
        dataView.addMouseListener(codeAreaMouseListener);
        dataView.addMouseMotionListener(codeAreaMouseListener);
        dataView.addMouseWheelListener(codeAreaMouseListener);
    }

    @Nonnull
    @Override
    public CodeArea getCodeArea() {
        return codeArea;
    }

    @Nonnull
    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(@Nonnull CodeAreaPainter painter) {
        if (painter == null) {
            throw new NullPointerException("Painter cannot be null");
        }

        this.painter = painter;
        repaint();
    }

    @Override
    public boolean isInitialized() {
        return painter.isInitialized();
    }

    @Override
    public void paintComponent(@Nonnull Graphics g) {
        painter.paintComponent(g);
    }

    @Nonnull
    @Override
    public DefaultCodeAreaCaret getCaret() {
        return caret;
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    @Nonnull
    public CodeAreaSection getActiveSection() {
        return caret.getSection();
    }

    @Nonnull
    public CaretPosition getCaretPosition() {
        return caret.getCaretPosition();
    }

    public void setCaretPosition(@Nonnull CaretPosition caretPosition) {
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

    @Override
    public int getCursorShape(int x, int y) {
        Rectangle dataViewRectangle = getDataViewRectangle();
        if (x >= dataViewRectangle.x && y >= dataViewRectangle.y) {
            return Cursor.TEXT_CURSOR;
        }

        return Cursor.DEFAULT_CURSOR;
    }

    @Nonnull
    @Override
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    @Override
    public void setCodeCharactersCase(@Nonnull CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        repaint();
    }

//    @Override
//    public int getPreviewX() {
//        return computeFirstCodeCharPos(getBytesPerLine()) * getCharacterWidth();
//    }
//
//    @Override
//    public int getPreviewFirstChar() {
//        return computeLastCodeCharPos(getBytesPerLine());
//    }
    @Override
    public void rebuildColors() {
    }

    @Nonnull
    @Override
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Override
    public void setViewMode(@Nonnull CodeAreaViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == CodeAreaViewMode.CODE_MATRIX) {
            getCaret().setSection(CodeAreaSection.CODE_MATRIX);
            notifyCaretMoved();
        } else if (viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            getCaret().setSection(CodeAreaSection.TEXT_PREVIEW);
            notifyCaretMoved();
        }
        repaint();
    }

    @Override
    @Nonnull
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void setCodeType(@Nonnull CodeType codeType) {
        this.codeType = codeType;
        repaint();
    }

    @Override
    public int computePositionByte(int lineCharPosition) {
        return lineCharPosition / (state.maxDigits + 1);
    }

    @Override
    public int computeCodeAreaCharacter(int pixelX) {
        return pixelX / getCharacterWidth();
    }

    @Override
    public int computeCodeAreaLine(int pixelY) {
        return pixelY / getLineHeight();
    }

    @Override
    public int computeFirstCodeCharPos(int byteOffset) {
        return byteOffset * (state.maxDigits + 1);
    }

    @Override
    public int computeLastCodeCharPos(int byteOffset) {
        return computeFirstCodeCharPos(byteOffset + 1) - 2;
    }

    @Override
    public long cursorPositionToDataPosition(long line, int byteOffset) throws OutOfBoundsException {
        return 16;
    }

    @Nonnull
    public Rectangle getDataViewRect() {
        // TODO cache
        return new Rectangle(state.lineNumbersAreaWidth, state.headerAreaHeight, state.areaWidth - state.lineNumbersAreaWidth, state.areaHeight - state.headerAreaHeight);
    }

    @Override
    public void revealCursor() {
        revealPosition(caret.getCaretPosition().getDataPosition(), caret.getSection());
    }

    public void revealPosition(long position, @Nonnull CodeAreaSection section) {
        if (!isInitialized()) {
            // Ignore if painter not initialized
            return;
        }

        boolean scrolled = false;
        Rectangle hexRect = getDataViewRectangle();
        int bytesPerRect = getBytesPerRectangle();
        int linesPerRect = getLinesPerRectangle();
        int bytesPerLine = getBytesPerLine();
        long caretLine = position / bytesPerLine;

        int positionByte = computePositionByte((int) (position % bytesPerLine));

        if (caretLine <= scrollPosition.getScrollLinePosition()) {
            scrollPosition.setScrollLinePosition(caretLine);
            scrollPosition.setScrollLineOffset(0);
            scrolled = true;
        } else if (caretLine >= scrollPosition.getScrollLinePosition() + linesPerRect) {
            scrollPosition.setScrollLinePosition(caretLine - linesPerRect);
            if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
                scrollPosition.setScrollLineOffset(getLineHeight() - (hexRect.height % getLineHeight()));
            } else {
                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
            }
            scrolled = true;
        }
        if (positionByte <= scrollPosition.getScrollCharPosition()) {
            scrollPosition.setScrollCharPosition(positionByte);
            scrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else if (positionByte >= scrollPosition.getScrollCharPosition() + bytesPerRect) {
            scrollPosition.setScrollCharPosition(positionByte - bytesPerRect);
            if (horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
                scrollPosition.setScrollCharOffset(getCharacterWidth() - (hexRect.width % getCharacterWidth()));
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

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param bytesPerLine bytes per line
     * @param lineHeight line height
     * @param charWidth character width
     * @param linesPerRect lines per visible rectangle
     * @return cursor position or null
     */
    @Override
    @Nullable
    public Point getCursorPoint(int bytesPerLine, int lineHeight, int charWidth, int linesPerRect) {
        CaretPosition caretPosition = getCaretPosition();
        long shiftedPosition = caretPosition.getDataPosition() + scrollPosition.getLineDataOffset();
        long line = shiftedPosition / bytesPerLine - scrollPosition.getScrollLinePosition();
        if (line < -1 || line > linesPerRect) {
            return null;
        }

        int byteOffset = (int) (shiftedPosition % bytesPerLine);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + line * lineHeight) - scrollPosition.getScrollLineOffset();
        int caretX;
        if (caretPosition.getSection() == CodeAreaSection.TEXT_PREVIEW) {
            caretX = codeArea.getPreviewX() + charWidth * byteOffset;
        } else {
            caretX = dataViewRect.x + charWidth * (computeFirstCodeCharPos(byteOffset) + caretPosition.getCodeOffset());
        }
        caretX -= scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    /**
     * Returns relative shadow cursor position in code area or null if cursor is
     * not visible.
     *
     * @param bytesPerLine bytes per line
     * @param lineHeight line height
     * @param charWidth character width
     * @param linesPerRect lines per visible rectangle
     * @return cursor position or null
     */
    @Override
    @Nullable
    public Point getShadowCursorPoint(int bytesPerLine, int lineHeight, int charWidth, int linesPerRect) {
        CaretPosition caretPosition = getCaretPosition();
        long shiftedPosition = caretPosition.getDataPosition() + scrollPosition.getLineDataOffset();
        long line = shiftedPosition / bytesPerLine - scrollPosition.getScrollLinePosition();
        if (line < -1 || line + 1 > linesPerRect) {
            return null;
        }

        int byteOffset = (int) (shiftedPosition % bytesPerLine);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + line * lineHeight) - scrollPosition.getScrollLineOffset();
        int caretX;
        if (caretPosition.getSection() == CodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.x + charWidth * computeFirstCodeCharPos(byteOffset);
        } else {
            caretX = codeArea.getPreviewX() + charWidth * byteOffset;
        }
        caretX -= scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    @Override
    public void updateScrollBars() {
        if (scrollPosition.getVerticalOverflowMode() == CodeAreaScrollPosition.VerticalOverflowMode.OVERFLOW) {
            long lines = ((codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / getBytesPerLine()) + 1;
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
            scrollPanel.getVerticalScrollBar().setValue((int) (scrollPosition.getScrollLinePosition() * getLineHeight() + scrollPosition.getScrollLineOffset()));
        }

        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition());
        } else {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition() * getCharacterWidth() + scrollPosition.getScrollCharOffset());
        }
        codeArea.repaint();
    }

    boolean isHorizontalScrollBarVisible() {
        // TODO
        return scrollPanel.getHorizontalScrollBar().isVisible();
    }

    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        codeArea.resetPainter();
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
        codeArea.resetPainter();
        scrollPosition.setScrollLinePosition(linePosition);
        updateScrollBars();
        notifyScrolled();
    }

    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        codeArea.resetPainter();
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
        codeArea.resetPainter();
        scrollPosition.setScrollCharPosition(bytePosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void reset() {
        painter.reset();
    }

    private void repaint() {
        codeArea.resetPainter();
        codeArea.repaint();
    }

    @Override
    public CaretPosition mousePositionToCaretPosition(int mouseX, int mouseY) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void notifyCaretChanged() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Nonnull
    @Override
    public CharacterRenderingMode getCharacterRenderingMode() {
        return characterRenderingMode;
    }

    @Override
    public void setCharacterRenderingMode(@Nonnull CharacterRenderingMode characterRenderingMode) {
        this.characterRenderingMode = characterRenderingMode;
        repaint();
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
            if (scrollPosition.getVerticalOverflowMode() == CodeAreaScrollPosition.VerticalOverflowMode.OVERFLOW) {
                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
                long lines = ((codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / getBytesPerLine()) - getLinesPerRectangle() + 1;
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
                scrollPosition.setScrollLinePosition(scrollBarValue / getLineHeight());
                scrollPosition.setScrollLineOffset(scrollBarValue % getLineHeight());
            }

            // TODO
            notifyScrolled();
            repaint();
//            dataViewScrolled(codeArea.getGraphics());
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
                int characterWidth = getCharacterWidth();
                if (characterWidth > 0) {
                    int horizontalScroll = scrollPanel.getHorizontalScrollBar().getValue();
                    scrollPosition.setScrollCharPosition(horizontalScroll / characterWidth);
                    scrollPosition.setScrollCharOffset(horizontalScroll % characterWidth);
                }
            }

            repaint();
            dataViewScrolled(codeArea.getGraphics());
            notifyScrolled();
        }
    }

    @Nonnull
    public Rectangle getDataViewRectangle() {
        return dataView.getBounds();
    }

    @Nonnull
    @Override
    public SelectionRange getSelection() {
        return selection;
    }

    @Override
    public void setSelection(@Nonnull SelectionRange selection) {
        this.selection = selection;
        notifySelectionChanged();
    }

    @Nonnull
    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(@Nonnull Charset charset) {
        if (charset == null) {
            throw new NullPointerException("Charset cannot be null");
        }

        this.charset = charset;
        repaint();
    }

    @Nonnull
    @Override
    public EditationMode getEditationMode() {
        return editationMode;
    }

    @Override
    public boolean isEditable() {
        return editationMode != EditationMode.READ_ONLY;
    }

    @Override
    public void setEditationMode(@Nonnull EditationMode editationMode) {
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

    public boolean isHandleClipboard() {
        return handleClipboard;
    }

    public void setHandleClipboard(boolean handleClipboard) {
        this.handleClipboard = handleClipboard;
    }

    @Nonnull
    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(@Nonnull Font font) {
        this.font = font;
        repaint();
    }

    @Nonnull 
    @Override
    public BasicBorderPaintMode getBorderPaintMode() {
        return borderPaintMode;
    }

    @Override
    public void setBorderPaintMode(@Nonnull BasicBorderPaintMode borderPaintMode) {
        this.borderPaintMode = borderPaintMode;
        repaint();
    }

    public void notifySelectionChanged() {
        for (SelectionChangedListener selectionChangedListener : selectionChangedListeners) {
            selectionChangedListener.selectionChanged(selection);
        }
    }

    @Override
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

    public void addSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    public void removeSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    public void addCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    public void removeCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    public void addScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    public void removeScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    public void addEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.add(editationModeChangedListener);
    }

    public void removeEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.remove(editationModeChangedListener);
    }
}
