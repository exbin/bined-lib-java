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

import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.UIManager;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationModeChangedListener;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.HorizontalScrollUnit;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.basic.VerticalScrollUnit;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingControl;
import org.exbin.bined.swing.basic.AntialiasingMode;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret;
import org.exbin.bined.swing.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.extended.color.ColorsProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.swing.extended.layout.LayoutProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.bined.swing.extended.theme.ThemeProfileCapableCodeAreaPainter;

/**
 * Code area component extended code area.
 *
 * @version 0.2.0 2018/12/11
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtCodeArea extends CodeAreaCore implements ExtendedCodeArea, CodeAreaSwingControl {

    @Nonnull
    private CodeAreaPainter painter;

    @Nonnull
    private final DefaultCodeAreaCaret caret;
    @Nonnull
    private final SelectionRange selection = new SelectionRange();
    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();

    @Nonnull
    private Charset charset = Charset.defaultCharset();
    private boolean handleClipboard = true;

    @Nonnull
    private EditationMode editationMode = EditationMode.EXPANDING;
    @Nonnull
    private EditationOperation editationOperation = EditationOperation.OVERWRITE;
    @Nonnull
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    @Nullable
    private Font font;
    @Nonnull
    private AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;
    @Nonnull
    private CodeType codeType = CodeType.HEXADECIMAL;
    private boolean showUnprintables;
    private int minRowPositionLength = 0;
    private int maxRowPositionLength = 0;
    @Nonnull
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    private boolean showMirrorCursor = true;
    @Nonnull
    private PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;
    @Nonnull
    private RowWrappingMode lineWrapping = RowWrappingMode.NO_WRAPPING;
    private int wrappingBytesGroupSize = 0;
    private int maxBytesPerLine = 16;

    @Nonnull
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    @Nonnull
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     */
    public ExtCodeArea() {
        super(DefaultCodeAreaCommandHandler.createDefaultCodeAreaCommandHandlerFactory());

        caret = new DefaultCodeAreaCaret(this);
        painter = new ExtendedCodeAreaPainter(this);
        init();
    }

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public ExtCodeArea(@Nullable CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(commandHandlerFactory);

        caret = new DefaultCodeAreaCaret(this);
        painter = new ExtendedCodeAreaPainter(this);
        init();
    }

    private void init() {
        UIManager.addPropertyChangeListener((@Nonnull PropertyChangeEvent evt) -> {
            resetColors();
        });
        caret.setSection(BasicCodeAreaSection.CODE_MATRIX);
    }

    @Nonnull
    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(CodeAreaPainter painter) {
        CodeAreaUtils.requireNonNull(painter);

        this.painter = painter;
        repaint();
    }

    public boolean isInitialized() {
        return painter.isInitialized();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (!isInitialized()) {
            ((FontCapable) this).setCodeFont(getCodeFont());
        }

        painter.paintComponent(g);
    }

    @Nonnull
    @Override
    public DefaultCodeAreaCaret getCaret() {
        return caret;
    }

    @Override
    public boolean isShowMirrorCursor() {
        return showMirrorCursor;
    }

    @Override
    public void setShowMirrorCursor(boolean showMirrorCursor) {
        this.showMirrorCursor = showMirrorCursor;
        repaint();
    }

    @Override
    public int getMinRowPositionLength() {
        return minRowPositionLength;
    }

    @Override
    public void setMinRowPositionLength(int minRowPositionLength) {
        this.minRowPositionLength = minRowPositionLength;
        reset();
        repaint();
    }

    @Override
    public int getMaxRowPositionLength() {
        return maxRowPositionLength;
    }

    @Override
    public void setMaxRowPositionLength(int maxRowPositionLength) {
        this.maxRowPositionLength = maxRowPositionLength;
        reset();
        repaint();
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

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        return painter.getMouseCursorShape(positionX, positionY);
    }

    @Override
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        return painter.getPositionZone(positionX, positionY);
    }

    @Nonnull
    @Override
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    @Override
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        repaint();
    }

    @Override
    public void resetColors() {
        painter.resetColors();
        repaint();
    }

    @Nonnull
    @Override
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Override
    public void setViewMode(CodeAreaViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == CodeAreaViewMode.CODE_MATRIX) {
            getCaret().setSection(BasicCodeAreaSection.CODE_MATRIX);
            notifyCaretMoved();
        } else if (viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            getCaret().setSection(BasicCodeAreaSection.TEXT_PREVIEW);
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
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        painter.reset();
        repaint();
    }

    @Override
    public void revealCursor() {
        revealPosition(caret.getCaretPosition());
    }

    @Override
    public void revealPosition(CaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        CodeAreaScrollPosition revealScrollPosition = painter.computeRevealScrollPosition(caretPosition);
        if (revealScrollPosition != null) {
            setScrollPosition(revealScrollPosition);
            resetPainter();
            updateScrollBars();
            notifyScrolled();
        }
    }

    public void revealPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        revealPosition(new CodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Override
    public void centerOnCursor() {
        centerOnPosition(caret.getCaretPosition());
    }

    @Override
    public void centerOnPosition(CaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        CodeAreaScrollPosition centerOnScrollPosition = painter.computeCenterOnScrollPosition(caretPosition);
        if (centerOnScrollPosition != null) {
            setScrollPosition(centerOnScrollPosition);
            resetPainter();
            updateScrollBars();
            notifyScrolled();
        }
    }

    public void centerOnPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        centerOnPosition(new CodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Nullable
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, PositionOverflowMode overflowMode) {
        return painter.mousePositionToClosestCaretPosition(positionX, positionY, overflowMode);
    }

    @Nonnull
    @Override
    public CaretPosition computeMovePosition(CaretPosition position, MovementDirection direction) {
        return painter.computeMovePosition(position, direction);
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection scrollingShift) {
        return painter.computeScrolling(startPosition, scrollingShift);
    }

    @Override
    public void updateScrollBars() {
        painter.updateScrollBars();
        repaint();
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    @Override
    public void setScrollPosition(CodeAreaScrollPosition scrollPosition) {
        this.scrollPosition.setScrollPosition(scrollPosition);
    }

    @Nonnull
    @Override
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Override
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

    @Nonnull
    @Override
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    @Override
    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit) {
        this.verticalScrollUnit = verticalScrollUnit;
        long linePosition = scrollPosition.getRowPosition();
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            scrollPosition.setRowOffset(0);
        }
        resetPainter();
        scrollPosition.setRowPosition(linePosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Nonnull
    @Override
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Override
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

    @Nonnull
    @Override
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Override
    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int bytePosition = scrollPosition.getCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setCharOffset(0);
        }
        resetPainter();
        scrollPosition.setCharPosition(bytePosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void reset() {
        painter.reset();
    }

    @Override
    public void updateLayout() {
        painter.updateLayout();
    }

    @Override
    public void repaint() {
        resetPainter();
        super.repaint();
    }

    @Override
    public void resetPainter() {
        painter.reset();
    }

    @Override
    public void notifyCaretChanged() {
        repaint();
    }

    @Override
    public AntialiasingMode getAntialiasingMode() {
        return antialiasingMode;
    }

    @Override
    public void setAntialiasingMode(AntialiasingMode antialiasingMode) {
        this.antialiasingMode = antialiasingMode;
        reset();
        repaint();
    }

    @Nonnull
    @Override
    public SelectionRange getSelection() {
        return selection;
    }

    @Override
    public void setSelection(SelectionRange selection) {
        CodeAreaUtils.requireNonNull(selection);

        this.selection.setSelection(selection);
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void setSelection(long start, long end) {
        this.selection.setSelection(start, end);
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void clearSelection() {
        this.selection.clearSelection();
        notifySelectionChanged();
        repaint();
    }

    @Override
    public boolean hasSelection() {
        return !selection.isEmpty();
    }

    @Nonnull
    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(Charset charset) {
        CodeAreaUtils.requireNonNull(charset);

        this.charset = charset;
        painter.reset();
        repaint();
    }

    @Override
    public boolean isEditable() {
        return editationMode != EditationMode.READ_ONLY;
    }

    @Nonnull
    @Override
    public EditationMode getEditationMode() {
        return editationMode;
    }

    @Override
    public void setEditationMode(EditationMode editationMode) {
        boolean changed = editationMode != this.editationMode;
        this.editationMode = editationMode;
        if (changed) {
            editationModeChangedListeners.forEach((listener) -> {
                listener.editationModeChanged(editationMode, getActiveOperation());
            });
            caret.resetBlink();
            repaint();
        }
    }

    @Override
    public EditationOperation getActiveOperation() {
        switch (editationMode) {
            case READ_ONLY:
                return EditationOperation.INSERT;
            case INPLACE:
                return EditationOperation.OVERWRITE;
            case CAPPED:
            case EXPANDING:
                return editationOperation;
            default:
                throw new IllegalStateException("Unexpected code type: " + editationMode.name());
        }
    }

    @Nonnull
    @Override
    public EditationOperation getEditationOperation() {
        return editationOperation;
    }

    @Override
    public void setEditationOperation(EditationOperation editationOperation) {
        EditationOperation previousOperation = getActiveOperation();
        this.editationOperation = editationOperation;
        EditationOperation currentOperation = getActiveOperation();
        boolean changed = previousOperation != currentOperation;
        if (changed) {
            editationModeChangedListeners.forEach((listener) -> {
                listener.editationModeChanged(editationMode, currentOperation);
            });
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
    public Font getCodeFont() {
        return font;
    }

    @Override
    public void setCodeFont(Font font) {
        this.font = font;
        painter.reset();
        repaint();
    }

    @Override
    public PositionCodeType getPositionCodeType() {
        return positionCodeType;
    }

    @Override
    public void setPositionCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = positionCodeType;
        painter.reset();
        repaint();
    }

    @Nonnull
    @Override
    public RowWrappingMode getRowWrapping() {
        return lineWrapping;
    }

    @Override
    public void setRowWrapping(RowWrappingMode lineWrapping) {
        this.lineWrapping = lineWrapping;
        updateLayout();
    }

    @Override
    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    @Override
    public void setWrappingBytesGroupSize(int groupSize) {
        wrappingBytesGroupSize = groupSize;
        updateLayout();
    }

    @Override
    public int getMaxBytesPerRow() {
        return maxBytesPerLine;
    }

    @Override
    public void setMaxBytesPerLine(int maxBytesPerLine) {
        this.maxBytesPerLine = maxBytesPerLine;
    }

    @Nullable
    @Override
    public CodeAreaColorsProfile getColorsProfile() {
        if (painter instanceof ColorsProfileCapableCodeAreaPainter) {
            return ((ColorsProfileCapableCodeAreaPainter) painter).getColorsProfile();
        }

        return null;
    }

    @Override
    public void setColorsProfile(CodeAreaColorsProfile colorsProfile) {
        if (painter instanceof ColorsProfileCapableCodeAreaPainter) {
            ((ColorsProfileCapableCodeAreaPainter) painter).setColorsProfile(colorsProfile);
        }
    }

    @Nullable
    @Override
    public ExtendedCodeAreaLayoutProfile getLayoutProfile() {
        if (painter instanceof LayoutProfileCapableCodeAreaPainter) {
            return ((LayoutProfileCapableCodeAreaPainter) painter).getLayoutProfile();
        }

        return null;
    }

    @Override
    public void setLayoutProfile(ExtendedCodeAreaLayoutProfile layoutProfile) {
        if (painter instanceof LayoutProfileCapableCodeAreaPainter) {
            ((LayoutProfileCapableCodeAreaPainter) painter).setLayoutProfile(layoutProfile);
        }
    }

    @Override
    public ExtendedCodeAreaThemeProfile getThemeProfile() {
        if (painter instanceof ThemeProfileCapableCodeAreaPainter) {
            return ((ThemeProfileCapableCodeAreaPainter) painter).getThemeProfile();
        }

        return null;
    }

    @Override
    public void setThemeProfile(ExtendedCodeAreaThemeProfile themeProfile) {
        if (painter instanceof ThemeProfileCapableCodeAreaPainter) {
            ((ThemeProfileCapableCodeAreaPainter) painter).setThemeProfile(themeProfile);
        }
    }

    @Override
    public boolean isShowUnprintables() {
        return showUnprintables;
    }

    @Override
    public void setShowUnprintables(boolean show) {
        this.showUnprintables = show;
        updateLayout();
    }

    public void notifySelectionChanged() {
        selectionChangedListeners.forEach((selectionChangedListener) -> {
            selectionChangedListener.selectionChanged(selection);
        });
    }

    @Override
    public void notifyCaretMoved() {
        caretMovedListeners.forEach((caretMovedListener) -> {
            caretMovedListener.caretMoved(caret.getCaretPosition());
        });
    }

    @Override
    public void notifyScrolled() {
        scrollingListeners.forEach((scrollingListener) -> {
            scrollingListener.scrolled();
        });
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
