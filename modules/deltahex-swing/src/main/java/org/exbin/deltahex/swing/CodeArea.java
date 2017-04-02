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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.exbin.deltahex.CaretMovedListener;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaControl;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.DataChangedListener;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.EditationModeChangedListener;
import org.exbin.deltahex.ScrollingListener;
import org.exbin.deltahex.SelectionChangedListener;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.ViewMode;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * @version 0.2.0 2017/04/02
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends JComponent implements CodeAreaControl {

    private BinaryData data;

    private CodeAreaCaret caret;
    private SelectionRange selection;
    private JScrollPane scrollPanel;

    private CodeAreaPainter painter;
    private CodeAreaCommandHandler commandHandler;

    private ViewMode viewMode = ViewMode.DUAL;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private EditationMode editationMode = EditationMode.OVERWRITE;

    /**
     * Listeners.
     */
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();
    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();

    public CodeArea() {
        super();
        init();
    }

    private void init() {
        caret = new CodeAreaCaret(this);
        scrollPanel = new JScrollPane();
        add(scrollPanel);
//        painter = new DefaultCodeAreaPainter(this);
//        commandHandler = new DefaultCodeAreaCommandHandler(this);

        // TODO buildColors();
//        verticalScrollBar = new JScrollBar(Scrollbar.VERTICAL);
//        verticalScrollBar.setVisible(false);
//        verticalScrollBar.setIgnoreRepaint(true);
//        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
//        add(verticalScrollBar);
//        horizontalScrollBar = new JScrollBar(Scrollbar.HORIZONTAL);
//        horizontalScrollBar.setIgnoreRepaint(true);
//        horizontalScrollBar.setVisible(false);
//        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
//        add(horizontalScrollBar);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addComponentListener(new CodeAreaComponentListener());

        CodeAreaMouseListener codeAreaMouseListener = new CodeAreaMouseListener(this);
        addMouseListener(codeAreaMouseListener);
        addMouseMotionListener(codeAreaMouseListener);
        addMouseWheelListener(codeAreaMouseListener);
        addKeyListener(new CodeAreaKeyListener());

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
                // TODO painter.rebuildColors();
            }
        });
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    public CodeAreaSection getActiveSection() {
        return caret.getSection();
    }

    public CaretPosition getCaretPosition() {
        return caret.getCaretPosition();
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
    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    @Override
    public boolean hasSelection() {
        return !selection.isEmpty();
    }

    private class CodeAreaKeyListener extends KeyAdapter {

        public CodeAreaKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent keyEvent) {
            commandHandler.keyTyped(keyEvent);
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            commandHandler.keyPressed(keyEvent);
        }
    }

    private class CodeAreaComponentListener implements ComponentListener {

        public CodeAreaComponentListener() {
        }

        @Override
        public void componentResized(ComponentEvent e) {
//            computePaintData();
//            validateLineOffset();
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
    }
}
