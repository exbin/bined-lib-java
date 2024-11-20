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
package org.exbin.bined.swing;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EmptyBinaryData;

/**
 * Binary viewer/editor component.
 * <p>
 * Class extends JTextComponent to be able to invoke on-screen keyboard.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
// Java 9+ @SwingContainer(false)
public abstract class CodeAreaCore extends JTextComponent implements CodeAreaControl, Accessible {

    @Nonnull
    protected BinaryData contentData = EmptyBinaryData.INSTANCE;

    @Nonnull
    protected CodeAreaCommandHandler commandHandler;

    protected final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

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
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setName("CodeArea");
        setDocument(new SimulatedDocument());
        setCaret(new SimulatedCaret());
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
    }

    @Nonnull
    private CodeAreaCommandHandler createCommandHandler(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        return commandHandlerFactory.createCommandHandler(this);
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(@Nonnull ComponentEvent event) {
                updateLayout();
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

    // TODO - collision with component
    @Override
    public boolean hasSelection() {
        if (this instanceof SelectionCapable) {
            return ((SelectionCapable) this).hasSelection();
        }

        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Nonnull
    @Override
    public BinaryData getContentData() {
        return contentData;
    }

    public void setContentData(@Nullable BinaryData contentData) {
        this.contentData = contentData == null ? EmptyBinaryData.INSTANCE : contentData;
        notifyDataChanged();
        repaint();
    }

    @Override
    public long getDataSize() {
        return contentData.getDataSize();
    }

    @Nonnull
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleComponent();
        }
        return accessibleContext;
    }

    /**
     * Notifies component, that the internal data was changed.
     */
    public void notifyDataChanged() {
        dataChangedListeners.forEach(DataChangedListener::dataChanged);
        Document document = getDocument();
        if (document instanceof SimulatedDocument) {
            ((SimulatedDocument) document).notifyDataChanged();
        }
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public abstract void resetPainter();

    public abstract void updateLayout();

    @ParametersAreNonnullByDefault
    protected class SimulatedDocument implements Document {

        private final List<UndoableEditListener> undoableEditListeners = new ArrayList<>();
        private final List<DocumentListener> documentListeners = new ArrayList<>();

        public SimulatedDocument() {
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public void addDocumentListener(DocumentListener listener) {
            documentListeners.add(listener);
        }

        @Override
        public void removeDocumentListener(DocumentListener listener) {
            documentListeners.remove(listener);
        }

        public void notifyDataChanged() {
            if (documentListeners.isEmpty()) {
                return;
            }

            DocumentEvent documentEvent = new DocumentEvent() {
                @Override
                public int getOffset() {
                    return 0;
                }

                @Override
                public int getLength() {
                    return 0;
                }

                @Override
                public Document getDocument() {
                    return SimulatedDocument.this;
                }

                @Nonnull
                @Override
                public DocumentEvent.EventType getType() {
                    return DocumentEvent.EventType.CHANGE;
                }

                @Override
                public DocumentEvent.ElementChange getChange(Element elem) {
                    throw new UnsupportedOperationException();
                }
            };
            documentListeners.forEach((t) -> {
                t.changedUpdate(documentEvent);
            });
        }

        @Override
        public void addUndoableEditListener(UndoableEditListener listener) {
            undoableEditListeners.add(listener);
        }

        @Override
        public void removeUndoableEditListener(UndoableEditListener listener) {
            undoableEditListeners.remove(listener);
        }

        @Nullable
        @Override
        public Object getProperty(Object key) {
            return null;
        }

        @Override
        public void putProperty(Object key, @Nullable Object value) {
        }

        @Override
        public void remove(int offs, int len) throws BadLocationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertString(int i, String string, AttributeSet as) throws BadLocationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText(int offset, int length) throws BadLocationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void getText(int i, int i1, Segment sgmnt) throws BadLocationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getStartPosition() {
            return () -> 0;
        }

        @Override
        public Position getEndPosition() {
            return () -> 0;
        }

        @Override
        public Position createPosition(int i) throws BadLocationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Element[] getRootElements() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Element getDefaultRootElement() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void render(Runnable r) {
        }
    }

    @ParametersAreNonnullByDefault
    public class AccessibleComponent extends AccessibleJComponent {

        /**
         * Gets the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the
         * object (AccessibleRole.TEXT)
         * @see AccessibleRole
         */
        @Nonnull
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            if (CodeAreaCore.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }
    }

    @ParametersAreNonnullByDefault
    protected class SimulatedCaret implements Caret {

        @Nullable
        private JTextComponent component;
        private final EventListenerList listenerList = new EventListenerList();
        private boolean visible = true;
        private boolean selectionVisible = false;
        private int rate;
        private int dot;
        private Point magicCaretPosition = new Point();

        @Override
        public void install(JTextComponent component) {
            this.component = component;
        }

        @Override
        public void deinstall(JTextComponent jtc) {
            this.component = null;
        }

        @Override
        public void paint(Graphics g) {
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            listenerList.add(ChangeListener.class, listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            listenerList.remove(ChangeListener.class, listener);
        }

        @Override
        public boolean isVisible() {
            return visible;
        }

        @Override
        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Override
        public boolean isSelectionVisible() {
            return selectionVisible;
        }

        @Override
        public void setSelectionVisible(boolean selectionVisible) {
            this.selectionVisible = selectionVisible;
        }

        @Override
        public void setMagicCaretPosition(Point magicCaretPosition) {
            this.magicCaretPosition = magicCaretPosition;
        }

        @Override
        public Point getMagicCaretPosition() {
            return magicCaretPosition;
        }

        @Override
        public void setBlinkRate(int rate) {
            this.rate = rate;
        }

        @Override
        public int getBlinkRate() {
            return rate;
        }

        @Override
        public int getDot() {
            return dot;
        }

        @Override
        public int getMark() {
            return 0;
        }

        @Override
        public void setDot(int dot) {
            this.dot = dot;
        }

        @Override
        public void moveDot(int dot) {
            this.dot = dot;
        }
    }
}
