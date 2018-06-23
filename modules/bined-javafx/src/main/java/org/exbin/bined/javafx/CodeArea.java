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
package org.exbin.bined.javafx;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Control;
import javafx.scene.layout.Background;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.UIManager;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.javafx.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.javafx.basic.DefaultCodeAreaWorker;
import org.exbin.bined.javafx.capability.FontCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * @version 0.2.0 2018/06/23
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends Control implements CodeAreaControl {

    @Nullable
    private BinaryData contentData;

    @Nonnull
    private CodeAreaWorker worker;
    @Nonnull
    private CodeAreaCommandHandler commandHandler;

    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     */
    public CodeArea() {
        this(null, null);
    }

    /**
     * Creates new instance with provided command handler and worker factory
     * methods.
     *
     * @param workerFactory code area worker or null for default worker
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeArea(@Nullable CodeAreaWorker.CodeAreaWorkerFactory workerFactory, @Nullable CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super();
        this.worker = workerFactory == null ? new DefaultCodeAreaWorker(this) : workerFactory.createWorker(this);
        this.commandHandler = commandHandlerFactory == null ? new DefaultCodeAreaCommandHandler(this) : commandHandlerFactory.createCommandHandler(this);
        init();
    }

    private void init() {
        // TODO: Use swing color instead
        setBackground(Background.EMPTY);
        setFocusTraversable(true);
        registerControlListeners();
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(@Nonnull ComponentEvent event) {
                updateLayout();
            }
        });

        setOnKeyPressed((javafx.scene.input.KeyEvent keyEvent) -> {
            commandHandler.keyPressed(keyEvent);
        });
        setOnKeyTyped((javafx.scene.input.KeyEvent keyEvent) -> {
            commandHandler.keyTyped(keyEvent);
        });

        focusedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            repaint();
        });

        UIManager.addPropertyChangeListener((@Nonnull PropertyChangeEvent evt) -> {
            worker.resetColors();
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

    @Override
    protected void paintComponent(@Nullable GraphicsContext g) {
        super.paintComponent(g);
        if (g == null) {
            return;
        }

        if (!worker.isInitialized()) {
            ((FontCapable) worker).setFont(getFont());
        }
        worker.paintComponent(g);
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
        repaint();
    }

    public long getDataSize() {
        return contentData == null ? 0 : contentData.getDataSize();
    }

    /**
     * Notifies component, that internal data was changed.
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
    
    public void updateLayout() {
        worker.updateLayout();
    }
}
