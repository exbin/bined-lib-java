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
package org.exbin.bined.swt.basic;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.HorizontalScrollUnit;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.basic.VerticalScrollUnit;
import org.exbin.bined.swt.CodeAreaCommandHandler;
import org.exbin.bined.swt.CodeAreaCore;
import org.exbin.bined.swt.CodeAreaPainter;
import org.exbin.bined.swt.CodeAreaSwtControl;
import org.exbin.bined.swt.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.swt.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.EditModeChangedListener;
import org.exbin.bined.swt.CodeAreaSwtUtils;

/**
 * Code area component default code area.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeArea extends CodeAreaCore implements DefaultCodeArea, CodeAreaSwtControl {

    @Nonnull
    private CodeAreaPainter painter;

    @Nonnull
    private final DefaultCodeAreaCaret caret;
    @Nonnull
    private final CodeAreaSelection selection = new CodeAreaSelection();
    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();

    @Nonnull
    private Charset charset = Charset.forName(CodeAreaSwtUtils.DEFAULT_ENCODING);
    private ClipboardHandlingMode clipboardHandlingMode = ClipboardHandlingMode.PROCESS;

    @Nonnull
    private EditMode editMode = EditMode.EXPANDING;
    @Nonnull
    private EditOperation editOperation = EditOperation.OVERWRITE;
    @Nonnull
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    @Nullable
    private Font codeFont;
    @Nonnull
    private BasicBackgroundPaintMode borderPaintMode = BasicBackgroundPaintMode.STRIPED;
    @Nonnull
    private AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;
    @Nonnull
    private CodeType codeType = CodeType.HEXADECIMAL;
    @Nonnull
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    private boolean showMirrorCursor = true;
    @Nonnull
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int minRowPositionLength = 0;
    private int maxRowPositionLength = 0;
    private int wrappingBytesGroupSize = 0;
    private int maxBytesPerRow = 16;

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
    private final List<EditModeChangedListener> editModeChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     *
     * @param parent parent component
     * @param style style
     */
    public CodeArea(@Nullable Composite parent, int style) {
        this(parent, style, DefaultCodeAreaCommandHandler.createDefaultCodeAreaCommandHandlerFactory());
    }

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param parent parent component
     * @param style style
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeArea(@Nullable Composite parent, int style, @Nullable CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(parent, style, commandHandlerFactory);

        caret = new DefaultCodeAreaCaret(this::notifyCaretChanged);
        painter = new DefaultCodeAreaPainter(this);
        painter.attach();
        init();
    }

    private void init() {
        addPaintListener((PaintEvent paintEvent) -> {
            GC g = paintEvent.gc;
            if (g == null) {
                return;
            }

            painter.paintComponent(g);
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

    @Override
    public void paintComponent(GC g) {
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

    public boolean isInitialized() {
        return painter.isInitialized();
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
    public CodeAreaCaretPosition getCaretPosition() {
        return caret.getCaretPosition();
    }

    @Override
    public void setCaretPosition(CodeAreaCaretPosition caretPosition) {
        caret.setCaretPosition(caretPosition);
        notifyCaretMoved();
    }

    @Override
    public void setCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
        notifyCaretMoved();
    }

    @Override
    public void setCaretPosition(long dataPosition, int codeOffset) {
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
        if (viewMode != this.viewMode) {
            this.viewMode = viewMode;
            switch (viewMode) {
                case CODE_MATRIX:
                    caret.setSection(BasicCodeAreaSection.CODE_MATRIX);
                    reset();
                    notifyCaretMoved();
                    break;
                case TEXT_PREVIEW:
                    caret.setSection(BasicCodeAreaSection.TEXT_PREVIEW);
                    reset();
                    notifyCaretMoved();
                    break;
                default:
                    reset();
                    break;
            }
            updateLayout();
        }
    }

    @Nonnull
    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        updateLayout();
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
        if (revealScrollPosition.isPresent()) {
            setScrollPosition(revealScrollPosition.get());
        }
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
        if (centerOnScrollPosition.isPresent()) {
            setScrollPosition(centerOnScrollPosition.get());
        }
    }

    public void centerOnPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        centerOnPosition(new DefaultCodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        return painter.mousePositionToClosestCaretPosition(positionX, positionY, overflowMode);
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
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Override
    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int charPosition = scrollPosition.getCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setCharOffset(0);
        }
        resetPainter();
        scrollPosition.setCharPosition(charPosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void reset() {
        Display.getDefault().asyncExec(() -> {
            painter.reset();
        });
    }

    @Override
    public void updateLayout() {
        Display.getDefault().asyncExec(() -> {
//            layout(true, true);
            painter.resetLayout();
            painter.repaint();
        });
    }

    @Override
    public void repaint() {
        painter.repaint();
//      Display.getDefault().asyncExec(() -> {
//            layout(true, true);
//            resetPainter();
//            painter.repaint();
//            redraw();
//            update();
//        });
    }

    @Override
    public void resetPainter() {
        painter.reset();
    }

    protected void notifyCaretChanged() {
        if (painter != null) {
            painter.resetCaret();
        }
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

    @Nullable
    @Override
    public Font getCodeFont() {
        return codeFont;
    }

    @Override
    public void setCodeFont(@Nullable Font codeFont) {
        this.codeFont = codeFont;
        painter.resetFont();
        repaint();
    }

    @Nonnull
    @Override
    public BasicBackgroundPaintMode getBackgroundPaintMode() {
        return borderPaintMode;
    }

    @Override
    public void setBackgroundPaintMode(BasicBackgroundPaintMode borderPaintMode) {
        this.borderPaintMode = borderPaintMode;
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

    @Nonnull
    @Override
    public Optional<BasicCodeAreaColorsProfile> getBasicColors() {
        if (painter instanceof BasicColorsCapableCodeAreaPainter) {
            return Optional.of(((BasicColorsCapableCodeAreaPainter) painter).getBasicColors());
        }
        return Optional.empty();
    }

    @Override
    public void setBasicColors(BasicCodeAreaColorsProfile colors) {
        if (painter instanceof BasicColorsCapableCodeAreaPainter) {
            ((BasicColorsCapableCodeAreaPainter) painter).setBasicColors(colors);
        }
    }

    public void notifySelectionChanged() {
        selectionChangedListeners.forEach((selectionChangedListener) -> {
            selectionChangedListener.selectionChanged();
        });
    }

    protected void notifyCaretMoved() {
        caretMovedListeners.forEach((caretMovedListener) -> {
            caretMovedListener.caretMoved(caret.getCaretPosition());
        });
    }

    protected void notifyScrolled() {
        scrollingListeners.forEach((scrollingListener) -> {
            scrollingListener.scrolled();
        });
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
    public void addCaretMovedListener(CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    @Override
    public void removeCaretMovedListener(CaretMovedListener caretMovedListener) {
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

    @Override
    public void dispose() {
        painter.dispose();
    }
}
