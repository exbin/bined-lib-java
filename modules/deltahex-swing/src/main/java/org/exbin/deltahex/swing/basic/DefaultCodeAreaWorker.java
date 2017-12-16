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

import java.awt.Font;
import java.awt.Graphics;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import org.exbin.deltahex.capability.CaretCapable;
import org.exbin.deltahex.capability.CharsetCapable;
import org.exbin.deltahex.capability.ClipboardCapable;
import org.exbin.deltahex.capability.CodeTypeCapable;
import org.exbin.deltahex.capability.EditationModeCapable;
import org.exbin.deltahex.capability.ViewModeCapable;
import org.exbin.deltahex.swing.capability.ScrollingCapable;
import org.exbin.deltahex.capability.SelectionCapable;
import org.exbin.deltahex.capability.CodeCharactersCaseCapable;
import org.exbin.deltahex.capability.LineWrappingCapable;
import org.exbin.deltahex.swing.capability.AntialiasingCapable;
import org.exbin.deltahex.swing.capability.BorderPaintCapable;
import org.exbin.deltahex.swing.capability.FontCapable;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2017/12/15
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaWorker implements CodeAreaWorker, SelectionCapable, CaretCapable, ScrollingCapable, ViewModeCapable,
        CodeTypeCapable, EditationModeCapable, CharsetCapable, CodeCharactersCaseCapable, AntialiasingCapable, FontCapable,
        BorderPaintCapable, LineWrappingCapable, ClipboardCapable {

    @Nonnull
    protected final CodeArea codeArea;

    @Nonnull
    private CodeAreaPainter painter;

    @Nonnull
    private final DefaultCodeAreaCaret caret;
    @Nonnull
    private SelectionRange selection = null;
    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();

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
    private boolean lineWrapping = false;
    private int maxBytesPerLine = 16;

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

        caret = new DefaultCodeAreaCaret(codeArea);

        this.painter = new DefaultCodeAreaPainter(this);
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
        return painter.getCursorShape(x, y);
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
    public void revealCursor() {
        revealPosition(caret.getCaretPosition().getDataPosition(), caret.getSection());
    }

    @Override
    public void revealPosition(@Nonnull CaretPosition caretPosition) {
        revealPosition(caretPosition.getDataPosition(), caretPosition.getSection());
    }

    public void revealPosition(long position, @Nonnull CodeAreaSection section) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        boolean scrolled = painter.revealPosition(position, section);

        if (scrolled) {
            updateScrollBars();
            notifyScrolled();
        }
    }

    @Nullable
    @Override
    public CaretPosition mousePositionToCaretPosition(int positionX, int positionY) {
        // TODO
        return null;
    }

    private void updateScrollBars() {
        painter.updateScrollBars();
        repaint();
    }

    @Override
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    @Override
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Override
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        codeArea.resetPainter();
        updateScrollBars();
    }

    @Override
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    @Override
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

    @Override
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Override
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        codeArea.resetPainter();
        updateScrollBars();
    }

    @Override
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Override
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
    public void notifyCaretChanged() {
        // TODO
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

    @Override
    public boolean hasSelection() {
        return selection != null && !selection.isEmpty();
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

    @Override
    public boolean isHandleClipboard() {
        return handleClipboard;
    }

    @Override
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

    @Override
    public boolean isLineWrapping() {
        return lineWrapping;
    }

    @Override
    public void setLineWrapping(boolean lineWrapping) {
        this.lineWrapping = lineWrapping;
    }

    @Override
    public int getMaxBytesPerLine() {
        return maxBytesPerLine;
    }

    @Override
    public void setMaxBytesPerLine(int maxBytesPerLine) {
        this.maxBytesPerLine = maxBytesPerLine;
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

    @Override
    public void notifyScrolled() {
        for (ScrollingListener scrollingListener : scrollingListeners) {
            scrollingListener.scrolled();
        }
    }

    @Override
    public void addSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    @Override
    public void removeSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    @Override
    public void addCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    @Override
    public void removeCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    @Override
    public void addScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    @Override
    public void removeScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    @Override
    public void addEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.add(editationModeChangedListener);
    }

    @Override
    public void removeEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.remove(editationModeChangedListener);
    }
}
