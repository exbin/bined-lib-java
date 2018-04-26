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
package org.exbin.bined.swt;

import org.exbin.bined.swt.basic.DefaultCodeAreaWorker;
import org.exbin.bined.swt.basic.DefaultCodeAreaCommandHandler;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.UIManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.exbin.bined.DataChangedListener;
import org.exbin.utils.binary_data.BinaryData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.GC;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.capability.SelectionCapable;

/**
 * Hexadecimal viewer/editor component.
 *
 * @version 0.2.0 2018/04/24
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends Composite implements CodeAreaControl {

    @Nullable
    private BinaryData contentData;

    @Nonnull
    private CodeAreaWorker worker;
    @Nonnull
    private CodeAreaCommandHandler commandHandler;

    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     *
     * @param parent parent component
     * @param style style
     */
    public CodeArea(@Nullable Composite parent, int style) {
        this(parent, style, null, null);
    }

    /**
     * Creates new instance with provided command handler and worker factory
     * methods.
     *
     * @param parent parent component
     * @param style style
     * @param workerFactory code area worker or null for default worker
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeArea(@Nullable Composite parent, int style, @Nullable CodeAreaWorker.CodeAreaWorkerFactory workerFactory, @Nullable CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(parent, style);
        this.worker = workerFactory == null ? new DefaultCodeAreaWorker(this) : workerFactory.createWorker(this);
        this.commandHandler = commandHandlerFactory == null ? new DefaultCodeAreaCommandHandler(this) : commandHandlerFactory.createCommandHandler(this);
        init();
    }

    private void init() {
        // TODO: Use swing color instead
//        setBackground(java.awt.Color.WHITE);
//        setFocusable(true);
//        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    private void registerControlListeners() {
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent disposeEvent) {
                CodeArea.this.widgetDisposed(disposeEvent);
            }
        });
        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent paintEvent) {
                CodeArea.this.paintControl(paintEvent);
            }
        });

        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                commandHandler.keyTyped(keyEvent);
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                commandHandler.keyPressed(keyEvent);
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
        UIManager.addPropertyChangeListener((@Nonnull PropertyChangeEvent evt) -> {
            worker.rebuildColors();
        });
    }

    @Nonnull
    public CodeAreaWorker getWorker() {
        return worker;
    }

    public void setWorker(@Nonnull CodeAreaWorker worker) {
        Objects.requireNonNull(worker, "Worker cannot be null");

        this.worker = worker;
    }

    @Nonnull
    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(@Nonnull CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    void paintControl(PaintEvent e) {
        GC g = e.gc;
        if (g == null) {
            return;
        }

        if (!worker.isInitialized()) {
// TODO            ((FontCapable) worker).setFont(getFont());
        }
        worker.paintComponent(g);
    }

    void widgetDisposed(DisposeEvent e) {
        // TODO worker.dispose();
        // TODO commandHandler.dispose();
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
        return ((SelectionCapable) worker).hasSelection();
    }

    @Nullable
    public BinaryData getContentData() {
        return contentData;
    }

    public void setContentData(@Nullable BinaryData contentData) {
        this.contentData = contentData;
        notifyDataChanged();
        redraw();
    }

    public long getDataSize() {
        return contentData == null ? 0 : contentData.getDataSize();
    }

    /**
     * Notifies component, that internal contentData was changed.
     */
    public void notifyDataChanged() {
        resetPainter();

        dataChangedListeners.forEach((listener) -> {
            listener.dataChanged();
        });
    }

    public void addDataChangedListener(@Nonnull DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(@Nonnull DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public void resetPainter() {
        worker.reset();
    }
}
