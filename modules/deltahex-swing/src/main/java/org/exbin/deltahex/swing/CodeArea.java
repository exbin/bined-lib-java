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

import org.exbin.deltahex.swing.basic.DefaultCodeAreaPainter;
import org.exbin.deltahex.swing.basic.DefaultCodeAreaCommandHandler;
import java.awt.Color;
import java.awt.Graphics;
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
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.exbin.deltahex.CaretMovedListener;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaControl;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.DataChangedListener;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.EditationModeChangedListener;
import org.exbin.deltahex.ScrollingListener;
import org.exbin.deltahex.SelectionChangedListener;
import org.exbin.deltahex.SelectionRange;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * @version 0.2.0 2017/10/31
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends JComponent implements CodeAreaControl {

    private BinaryData data;

    private CodeAreaCaret caret;
    private SelectionRange selection;
    private Charset charset = Charset.defaultCharset();
    private boolean handleClipboard = true;

    private CodeAreaPainter painter;
    private CodeAreaCommandHandler commandHandler;

    private EditationMode editationMode = EditationMode.OVERWRITE;

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
    public CodeArea(@Nonnull CodeAreaCommandHandler commandHandler, @Nonnull CodeAreaPainter painter) {
        super();
        this.commandHandler = commandHandler;
        this.painter = painter;
        init();
    }

    private void init() {
        caret = new CodeAreaCaret(this);

        // TODO: Use swing color instead
        setBackground(Color.WHITE);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                resetPainter();
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

    @Nonnull
    public CodeAreaCaret getCaret() {
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

    public SelectionRange getSelection() {
        return selection;
    }

    public void setSelection(SelectionRange selection) {
        this.selection = selection;
        notifySelectionChanged();
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

    public void setPainter(@Nonnull CodeAreaPainter painter) {
        if (painter == null) {
            throw new NullPointerException("Painter cannot be null");
        }

        this.painter = painter;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        painter.paintComponent(g);
    }

    public int computeCodeAreaCharacter(int pixelX) {
        return painter.computeCodeAreaCharacter(pixelX);
    }

    public int computeCodeAreaLine(int pixelY) {
        return painter.computeCodeAreaLine(pixelY);
    }

    /**
     * Returns currently used charset.
     *
     * @return charset
     */
    @Nonnull
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets charset to use for characters decoding.
     *
     * @param charset charset
     */
    public void setCharset(@Nonnull Charset charset) {
        if (charset == null) {
            throw new NullPointerException("Charset cannot be null");
        }

        this.charset = charset;
        repaint();
    }

    @Nonnull
    public EditationMode getEditationMode() {
        return editationMode;
    }

    public boolean isEditable() {
        return editationMode != EditationMode.READ_ONLY;
    }

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

    public boolean isHandleClipboard() {
        return handleClipboard;
    }

    public void setHandleClipboard(boolean handleClipboard) {
        this.handleClipboard = handleClipboard;
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
        resetPainter();

        for (DataChangedListener dataChangedListener : dataChangedListeners) {
            dataChangedListener.dataChanged();
        }
    }

    public void notifySelectionChanged() {
        for (SelectionChangedListener selectionChangedListener : selectionChangedListeners) {
            selectionChangedListener.selectionChanged(selection);
        }
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

    public void resetPainter() {
        painter.reset();
    }

    public void revealCursor() {
        revealPosition(caret.getCaretPosition().getDataPosition(), caret.getSection());
    }
    
    public void revealPosition(long position, CodeAreaSection section) {
        painter.revealPosition(position, section);
    }

    public void repaintCursor() {
        repaint();
        // TODO
//        Graphics graphics = getGraphics();
//        if (graphics != null) {
//            painter.paintCursor(graphics);
//        }
    }
}
