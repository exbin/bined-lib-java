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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.CodeAreaUtils;

/**
 * Binary viewer/editor component.
 *
 * @version 0.2.0 2018/12/25
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class CodeAreaCore extends Pane implements CodeAreaControl {

    @Nullable
    private BinaryData contentData;

    @Nonnull
    private CodeAreaCommandHandler commandHandler;

    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeAreaCore(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super();
        this.commandHandler = createCommandHandler(CodeAreaUtils.requireNonNull(commandHandlerFactory));
        init();
    }

    private void init() {
        // TODO: Use swing color instead
        setBackground(Background.EMPTY);
        setFocusTraversable(true);
        registerControlListeners();
    }

    @Nonnull
    private CodeAreaCommandHandler createCommandHandler(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        return commandHandlerFactory.createCommandHandler(this);
    }

    private void registerControlListeners() {
        addEventHandler(EventType.ROOT, (Event t) -> {
            updateLayout();
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
    }

    @Nonnull
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
        if (this instanceof SelectionCapable) {
            return ((SelectionCapable) this).hasSelection();
        }

        return false;
    }

    @Nullable
    @Override
    public BinaryData getContentData() {
        return contentData;
    }

    public void setContentData(@Nullable BinaryData contentData) {
        this.contentData = contentData;
        notifyDataChanged();
        repaint();
    }

    @Override
    public long getDataSize() {
        return contentData == null ? 0 : contentData.getDataSize();
    }

    /**
     * Notifies component, that internal data was changed.
     */
    public void notifyDataChanged() {
        dataChangedListeners.forEach(DataChangedListener::dataChanged);
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public abstract void resetPainter();

    public abstract void updateLayout();

    public abstract void repaint();
}
