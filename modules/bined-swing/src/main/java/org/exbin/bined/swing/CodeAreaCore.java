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
import javax.swing.text.SimpleAttributeSet;
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

    private static final String UI_CLASS_ID = "CodeAreaUI";

    @Nonnull
    protected BinaryData contentData;

    @Nonnull
    protected CodeAreaCommandHandler commandHandler;

    protected final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeAreaCore(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        this(commandHandlerFactory, EmptyBinaryData.getInstance());
    }

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     * @param contentData content data
     */
    public CodeAreaCore(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory, BinaryData contentData) {
        super();
        this.contentData = contentData;
        this.commandHandler = createCommandHandler(CodeAreaUtils.requireNonNull(commandHandlerFactory));
        init();
    }

    private void init() {
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

    protected void registerControlListeners() {
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

    @Override
    public void cut() {
        commandHandler.cut();
    }

    @Override
    public void paste() {
        commandHandler.paste();
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
    public String getUIClassID() {
        return UI_CLASS_ID;
    }

    @Override
    public void updateUI() {
        setUI(CodeAreaUI.createUI(this));
        invalidate();
    }

    @Nonnull
    @Override
    public BinaryData getContentData() {
        return contentData;
    }

    public void setContentData(@Nullable BinaryData contentData) {
        this.contentData = contentData == null ? EmptyBinaryData.getInstance() : contentData;
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
    protected static class SimulatedDocument implements Document {

        private final List<UndoableEditListener> undoableEditListeners = new ArrayList<>();
        private final List<DocumentListener> documentListeners = new ArrayList<>();
        private SimulatedElement rootElement = new SimulatedElement(this);

        public SimulatedDocument() {
        }

        @Override
        public int getLength() {
            // Return non-empty document - some components blocks backspace for empty documents
            return 2;
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
                    return new DocumentEvent.ElementChange() {
                        @Override
                        public Element getElement() {
                            return elem;
                        }

                        @Override
                        public int getIndex() {
                            return 0;
                        }

                        @Override
                        public Element[] getChildrenRemoved() {
                            return new Element[0];
                        }

                        @Override
                        public Element[] getChildrenAdded() {
                            return new Element[0];
                        }
                    };
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
            // ignore
        }

        @Override
        public void insertString(int i, String string, AttributeSet as) throws BadLocationException {
            // ignore
        }

        @Override
        public String getText(int offset, int length) throws BadLocationException {
            if (offset < 2) {
                if (length == 1) {
                    return " ";
                }
                if (offset == 0 && length == 2) {
                    return "  ";
                }
            }
            return "";
        }

        @Override
        public void getText(int offset, int length, Segment sgmnt) throws BadLocationException {
            if (offset < 2) {
                if (length == 1) {
                    sgmnt.array = new char[1];
                    sgmnt.array[0] = ' ';
                    sgmnt.offset = 0;
                    sgmnt.count = 1;
                    return;
                }
                if (offset == 0 && length == 2) {
                    sgmnt.array = new char[2];
                    sgmnt.array[0] = ' ';
                    sgmnt.array[1] = ' ';
                    sgmnt.offset = 0;
                    sgmnt.count = 2;
                    return;
                }
            }

            sgmnt.array = new char[0];
            sgmnt.offset = 0;
            sgmnt.count = 0;
        }

        @Nonnull
        @Override
        public Position getStartPosition() {
            return () -> 0;
        }

        @Nonnull
        @Override
        public Position getEndPosition() {
            return () -> 0;
        }

        @Nonnull
        @Override
        public Position createPosition(int i) throws BadLocationException {
            return () -> i;
        }

        @Override
        public Element[] getRootElements() {
            Element[] result = new Element[1];
            result[0] = rootElement;
            return result;
        }

        @Override
        public Element getDefaultRootElement() {
            return rootElement;
        }

        @Override
        public void render(Runnable r) {
        }
    }

    public static class SimulatedElement implements Element {

        private final Document document;
        private final AttributeSet attributeSet = new SimpleAttributeSet();

        public SimulatedElement(Document document) {
            this.document = document;
        }

        @Override
        public Document getDocument() {
            return document;
        }

        @Override
        public Element getParentElement() {
            return null;
        }

        @Override
        public String getName() {
            return "codeArea";
        }

        @Override
        public AttributeSet getAttributes() {
            return attributeSet;
        }

        @Override
        public int getStartOffset() {
            return 0;
        }

        @Override
        public int getEndOffset() {
            return 2;
        }

        @Override
        public int getElementIndex(int offset) {
            return 0;
        }

        @Override
        public int getElementCount() {
            return 0;
        }

        @Override
        public Element getElement(int index) {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return true;
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

        @Nonnull
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
    protected static class SimulatedCaret implements Caret {

        @Nullable
        private JTextComponent component;
        private final EventListenerList listenerList = new EventListenerList();
        private boolean visible = true;
        private boolean selectionVisible = false;
        private int rate;
        private int dot = 1;
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

        @Nonnull
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
