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
package org.exbin.bined.swing.section;

import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.basic.VerticalScrollUnit;
import org.exbin.bined.section.SectionHorizontalScrollUnit;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingControl;
import org.exbin.bined.swing.basic.AntialiasingMode;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret;
import org.exbin.bined.swing.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.swing.section.color.ColorsProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.section.layout.LayoutProfileCapableCodeAreaPainter;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.bined.swing.section.theme.ThemeProfileCapableCodeAreaPainter;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.swing.section.caret.CaretsProfileCapableCodeAreaPainter;
import org.exbin.bined.EditModeChangedListener;
import org.exbin.bined.swing.section.caret.SectionCodeAreaCaretsProfile;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.CodeAreaCaretListener;

/**
 * TODO: Binary viewer/editor component with configurable sections.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectCodeArea extends CodeAreaCore implements SectionCodeArea, CodeAreaSwingControl {

    @Nonnull
    protected CodeAreaPainter painter;

    @Nonnull
    protected final DefaultCodeAreaCaret caret;
    @Nonnull
    protected final CodeAreaSelection selection = new CodeAreaSelection();
    @Nonnull
    protected final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();

    @Nonnull
    protected Charset charset = Charset.defaultCharset();
    @Nonnull
    protected ClipboardHandlingMode clipboardHandlingMode = ClipboardHandlingMode.PROCESS;

    @Nonnull
    protected EditMode editMode = EditMode.EXPANDING;
    @Nonnull
    protected EditOperation editOperation = EditOperation.OVERWRITE;
    @Nonnull
    protected CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    @Nullable
    protected Font codeFont;
    @Nonnull
    protected AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;
    @Nonnull
    protected CodeType codeType = CodeType.HEXADECIMAL;
    protected int minRowPositionLength = 0;
    protected int maxRowPositionLength = 0;
    @Nonnull
    protected CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    protected boolean showMirrorCursor = true;
    @Nonnull
    protected PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;
    @Nonnull
    protected RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    protected int wrappingBytesGroupSize = 0;
    protected int maxBytesPerRow = 16;

    @Nonnull
    protected ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    protected VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    @Nonnull
    protected ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    protected SectionHorizontalScrollUnit horizontalScrollUnit = SectionHorizontalScrollUnit.PIXEL;

    private final List<CodeAreaCaretListener> caretMovedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<EditModeChangedListener> editModeChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     */
    public SectCodeArea() {
        this(DefaultCodeAreaCommandHandler.createDefaultCodeAreaCommandHandlerFactory());
    }

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public SectCodeArea(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(commandHandlerFactory);

        caret = new DefaultCodeAreaCaret(this::notifyCaretChanged);
        painter = new SectionCodeAreaPainter(this);
        painter.attach();
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

        this.painter.detach();
        this.painter = painter;
        painter.attach();
        reset();
        repaint();
    }

    public boolean isInitialized() {
        return painter.isInitialized();
    }

    @Override
    public void paintComponent(Graphics g) {
        painter.paintComponent(g);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (getBorder() == null) {
            super.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextAreaUI.border"));
        }
        if (painter != null) {
            painter.rebuildColors();
            painter.resetFont();
            painter.resetColors();
        }
    }

    @Override
    public void setBorder(@Nullable Border border) {
        super.setBorder(border);
        updateLayout();
    }

    @Nonnull
    @Override
    public DefaultCodeAreaCaret getCodeAreaCaret() {
        return caret;
    }

    @Override
    public boolean isShowMirrorCursor() {
        return showMirrorCursor;
    }

    @Override
    public void setShowMirrorCursor(boolean showMirrorCursor) {
        this.showMirrorCursor = showMirrorCursor;
        updateLayout();
    }

    @Override
    public int getMinRowPositionLength() {
        return minRowPositionLength;
    }

    @Override
    public void setMinRowPositionLength(int minRowPositionLength) {
        this.minRowPositionLength = minRowPositionLength;
        updateLayout();
    }

    @Override
    public int getMaxRowPositionLength() {
        return maxRowPositionLength;
    }

    @Override
    public void setMaxRowPositionLength(int maxRowPositionLength) {
        this.maxRowPositionLength = maxRowPositionLength;
        updateLayout();
    }

    @Override
    public long getDataPosition() {
        return caret.getDataPosition();
    }

    @Override
    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    @Nonnull
    @Override
    public CodeAreaSection getActiveSection() {
        return caret.getSection();
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition getActiveCaretPosition() {
        return caret.getCaretPosition();
    }

    @Override
    public void setActiveCaretPosition(CodeAreaCaretPosition caretPosition) {
        caret.setCaretPosition(caretPosition);
        notifyCaretMoved();
    }

    @Override
    public void setActiveCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
        notifyCaretMoved();
    }

    @Override
    public void setActiveCaretPosition(long dataPosition, int codeOffset) {
        caret.setCaretPosition(dataPosition, codeOffset);
        notifyCaretMoved();
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        return painter.getMouseCursorShape(positionX, positionY);
    }

    @Nonnull
    @Override
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    @Override
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        updateLayout();
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
            caret.setSection(BasicCodeAreaSection.CODE_MATRIX);
            notifyCaretMoved();
        } else if (viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            caret.setSection(BasicCodeAreaSection.TEXT_PREVIEW);
            notifyCaretMoved();
        }
        updateLayout();
    }

    @Nonnull
    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        validateCaret();
        updateLayout();
    }

    public void validateCaret() {
        boolean moved = false;
        if (caret.getDataPosition() > getDataSize()) {
            caret.setDataPosition(getDataSize());
            moved = true;
        }
        if (caret.getSection() == BasicCodeAreaSection.CODE_MATRIX && caret.getCodeOffset() >= codeType.getMaxDigitsForByte()) {
            caret.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
            moved = true;
        }

        if (moved) {
            notifyCaretMoved();
        }
    }

    @Override
    public void revealCursor() {
        revealPosition(caret.getCaretPosition());
    }

    @Override
    public void revealPosition(CodeAreaCaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        Optional<CodeAreaScrollPosition> revealScrollPosition = painter.computeRevealScrollPosition(caretPosition);
        revealScrollPosition.ifPresent(this::setScrollPosition);
    }

    public void revealPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        revealPosition(new DefaultCodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Override
    public void centerOnCursor() {
        centerOnPosition(caret.getCaretPosition());
    }

    @Override
    public void centerOnPosition(CodeAreaCaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        Optional<CodeAreaScrollPosition> centerOnScrollPosition = painter.computeCenterOnScrollPosition(caretPosition);
        centerOnScrollPosition.ifPresent(this::setScrollPosition);
    }

    public void centerOnPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        centerOnPosition(new DefaultCodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overlapMode) {
        return painter.mousePositionToClosestCaretPosition(positionX, positionY, overlapMode);
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return painter.computeMovePosition(position, direction);
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection scrollingShift) {
        return painter.computeScrolling(startPosition, scrollingShift);
    }

    protected void updateScrollBars() {
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
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
            painter.scrollPositionModified();
            updateScrollBars();
            notifyScrolled();
        }
    }

    @Override
    public void updateScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
            repaint();
            notifyScrolled();
        }
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
        long rowPosition = scrollPosition.getRowPosition();
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            scrollPosition.setRowOffset(0);
        }
        resetPainter();
        scrollPosition.setRowPosition(rowPosition);
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
    public SectionHorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Override
    public void setHorizontalScrollUnit(SectionHorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int charPosition = scrollPosition.getCharPosition();
        if (horizontalScrollUnit == SectionHorizontalScrollUnit.CHARACTER) {
            scrollPosition.setCharOffset(0);
        }
        resetPainter();
        scrollPosition.setCharPosition(charPosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void reset() {
        painter.reset();
    }

    @Override
    public void updateLayout() {
        painter.resetLayout();
        repaint();
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    @Override
    public void resetPainter() {
        painter.reset();
    }

    protected void notifyCaretChanged() {
        painter.resetCaret();
        repaint();
    }

    @Override
    public void notifyDataChanged() {
        super.notifyDataChanged();
        updateLayout();
    }

    @Nonnull
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
        return selection.getRange();
    }

    @Override
    public void setSelection(SelectionRange selectionRange) {
        this.selection.setRange(CodeAreaUtils.requireNonNull(selectionRange));
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
    public CodeAreaSelection getSelectionHandler() {
        return selection;
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
        reset();
        repaint();
    }

    @Override
    public boolean isEditable() {
        return editMode != EditMode.READ_ONLY;
    }

    @Nonnull
    @Override
    public EditMode getEditMode() {
        return editMode;
    }

    @Override
    public void setEditMode(EditMode editMode) {
        boolean changed = editMode != this.editMode;
        this.editMode = editMode;
        if (changed) {
            editModeChangedListeners.forEach((listener) -> {
                listener.editModeChanged(editMode, getActiveOperation());
            });
            caret.resetBlink();
            notifyCaretChanged();
            repaint();
        }
    }

    @Nonnull
    @Override
    public EditOperation getActiveOperation() {
        switch (editMode) {
            case READ_ONLY:
                return EditOperation.INSERT;
            case INPLACE:
                return EditOperation.OVERWRITE;
            case CAPPED:
            case EXPANDING:
                return editOperation;
            default:
                throw CodeAreaUtils.getInvalidTypeException(editMode);
        }
    }

    @Nonnull
    @Override
    public EditOperation getEditOperation() {
        return editOperation;
    }

    @Override
    public void setEditOperation(EditOperation editOperation) {
        EditOperation previousOperation = getActiveOperation();
        this.editOperation = editOperation;
        EditOperation currentOperation = getActiveOperation();
        boolean changed = previousOperation != currentOperation;
        if (changed) {
            editModeChangedListeners.forEach((listener) -> {
                listener.editModeChanged(editMode, currentOperation);
            });
            caret.resetBlink();
            notifyCaretChanged();
            repaint();
        }
    }

    @Nonnull
    @Override
    public ClipboardHandlingMode getClipboardHandlingMode() {
        return clipboardHandlingMode;
    }

    @Override
    public void setClipboardHandlingMode(ClipboardHandlingMode clipboardHandlingMode) {
        this.clipboardHandlingMode = clipboardHandlingMode;
    }

    @Nonnull
    @Override
    public Font getCodeFont() {
        return codeFont == null ? super.getFont() : codeFont;
    }

    @Override
    public void setCodeFont(@Nullable Font codeFont) {
        this.codeFont = codeFont;
        painter.resetFont();
        repaint();
    }

    @Nonnull
    @Override
    public PositionCodeType getPositionCodeType() {
        return positionCodeType;
    }

    @Override
    public void setPositionCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = positionCodeType;
        updateLayout();
    }

    @Nonnull
    @Override
    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    @Override
    public void setRowWrapping(RowWrappingMode rowWrapping) {
        this.rowWrapping = rowWrapping;
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
        return maxBytesPerRow;
    }

    @Override
    public void setMaxBytesPerRow(int maxBytesPerRow) {
        this.maxBytesPerRow = maxBytesPerRow;
        updateLayout();
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
    public SectionCodeAreaLayoutProfile getLayoutProfile() {
        if (painter instanceof LayoutProfileCapableCodeAreaPainter) {
            return ((LayoutProfileCapableCodeAreaPainter) painter).getLayoutProfile();
        }

        return null;
    }

    @Override
    public void setLayoutProfile(SectionCodeAreaLayoutProfile layoutProfile) {
        if (painter instanceof LayoutProfileCapableCodeAreaPainter) {
            ((LayoutProfileCapableCodeAreaPainter) painter).setLayoutProfile(layoutProfile);
        }
    }

    @Nullable
    @Override
    public SectionCodeAreaThemeProfile getThemeProfile() {
        if (painter instanceof ThemeProfileCapableCodeAreaPainter) {
            return ((ThemeProfileCapableCodeAreaPainter) painter).getThemeProfile();
        }

        return null;
    }

    @Override
    public void setThemeProfile(SectionCodeAreaThemeProfile themeProfile) {
        if (painter instanceof ThemeProfileCapableCodeAreaPainter) {
            ((ThemeProfileCapableCodeAreaPainter) painter).setThemeProfile(themeProfile);
        }
    }

    @Nullable
    @Override
    public SectionCodeAreaCaretsProfile getCaretsProfile() {
        if (painter instanceof CaretsProfileCapableCodeAreaPainter) {
            return ((CaretsProfileCapableCodeAreaPainter) painter).getCaretsProfile();
        }

        return null;
    }

    @Override
    public void setCaretsProfile(SectionCodeAreaCaretsProfile caretsProfile) {
        if (painter instanceof CaretsProfileCapableCodeAreaPainter) {
            ((CaretsProfileCapableCodeAreaPainter) painter).setCaretsProfile(caretsProfile);
        }
    }

    public void notifySelectionChanged() {
        selectionChangedListeners.forEach(SelectionChangedListener::selectionChanged);
    }

    protected void notifyCaretMoved() {
        caretMovedListeners.forEach((caretMovedListener) -> caretMovedListener.caretMoved(caret.getCaretPosition()));
    }

    protected void notifyScrolled() {
        painter.resetLayout();
        scrollingListeners.forEach(ScrollingListener::scrolled);
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    @Override
    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    @Override
    public void addCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    @Override
    public void removeCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    @Override
    public void addScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    @Override
    public void removeScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    @Override
    public void addEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.add(editModeChangedListener);
    }

    @Override
    public void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.remove(editModeChangedListener);
    }
}
