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

import org.exbin.deltahex.swing.basic.DefaultCodeAreaCommandHandler;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.exbin.deltahex.CodeAreaControl;
import org.exbin.deltahex.DataChangedListener;
import org.exbin.deltahex.capability.SelectionCapable;
import org.exbin.deltahex.swing.basic.DefaultCodeAreaWorker;
import org.exbin.deltahex.swing.capability.FontCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * @version 0.2.0 2017/12/23
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends JComponent implements CodeAreaControl {

    @Nullable
    private BinaryData data;

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
     * Creates new instance with provided command handler and worker.
     *
     * @param worker code area worker or null for default worker
     * @param commandHandler command handler or null for default handler
     */
    public CodeArea(@Nullable CodeAreaWorker worker, @Nullable CodeAreaCommandHandler commandHandler) {
        super();
        this.worker = worker == null ? new DefaultCodeAreaWorker(this) : worker;
        this.commandHandler = commandHandler == null ? new DefaultCodeAreaCommandHandler(this) : commandHandler;
        init();
    }

    private void init() {
        // TODO: Use swing color instead
        setBackground(Color.WHITE);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(@Nonnull ComponentEvent event) {
                resetPainter();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(@Nonnull KeyEvent keyEvent) {
                commandHandler.keyTyped(keyEvent);
            }

            @Override
            public void keyPressed(@Nonnull KeyEvent keyEvent) {
                commandHandler.keyPressed(keyEvent);
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(@Nonnull FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(@Nonnull FocusEvent e) {
                repaint();
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
        if (worker == null) {
            throw new NullPointerException("Worker cannot be null");
        }

        this.worker = worker;
    }

    @Override
    protected void paintComponent(@Nonnull Graphics g) {
        super.paintComponent(g);

        if (!worker.isInitialized()) {
            ((FontCapable) worker).setFont(getFont());
        }
        worker.paintComponent(g);
    }

    @Nonnull
    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(@Nonnull CodeAreaCommandHandler commandHandler) {
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
        return ((SelectionCapable) worker).hasSelection();
    }

    @Nullable
    public BinaryData getData() {
        return data;
    }

    public void setData(@Nullable BinaryData data) {
        this.data = data;
        notifyDataChanged();
        repaint();
    }

    public long getDataSize() {
        return data == null ? 0 : data.getDataSize();
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

    public void addDataChangedListener(@Nullable DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(@Nullable DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public void resetPainter() {
        worker.reset();
    }
}
