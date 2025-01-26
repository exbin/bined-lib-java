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

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Binary viewer/editor component UI.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaUI extends TextUI implements ViewFactory {

    private static final Position.Bias[] discardBias = new Position.Bias[1];
    transient JTextComponent editor;
    transient RootView rootView = new RootView();
    private static final EditorKit defaultKit = new DefaultEditorKit();

    public static ComponentUI createUI(JComponent c) {
        return new CodeAreaUI();
    }

    @Override
    public void installUI(JComponent c) {
        if (c instanceof JTextComponent) {
            editor = (JTextComponent) c;
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
    }

    @Nullable
    @Override
    public View create(Element elem) {
        return null;
    }

    @Override
    @Deprecated
    public Rectangle modelToView(JTextComponent tc, int pos) throws BadLocationException {
        return modelToView(tc, pos, Position.Bias.Forward);
    }

    @Override
    @Deprecated
    public Rectangle modelToView(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
        return (Rectangle) modelToView(tc, pos, bias, false);
    }

    /* Don't override to support Java 8 */
    public Rectangle2D modelToView2D(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
        return modelToView(tc, pos, bias, true);
    }

    @Nullable
    private Rectangle2D modelToView(JTextComponent tc, int pos,
            Position.Bias bias, boolean useFPAPI)
            throws BadLocationException {
        Document doc = editor.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).readLock();
        }
        try {
            Rectangle alloc = getVisibleEditorRect();
            if (alloc != null) {
                rootView.setSize(alloc.width, alloc.height);
                Shape s = rootView.modelToView(pos, alloc, bias);
                if (s != null) {
                    return useFPAPI ? s.getBounds2D() : s.getBounds();
                }
            }
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
        return null;
    }

    @Override
    public int viewToModel(JTextComponent tc, Point pt) {
        return viewToModel(tc, pt, discardBias);
    }

    @Override
    @Deprecated
    public int viewToModel(JTextComponent tc, Point pt, Position.Bias[] biasReturn) {
        return viewToModel(tc, pt.x, pt.y, biasReturn);
    }

    private int viewToModel(JTextComponent tc, float x, float y,
            Position.Bias[] biasReturn) {
        int offs = -1;
        Document doc = editor.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).readLock();
        }
        try {
            Rectangle alloc = getVisibleEditorRect();
            if (alloc != null) {
                rootView.setSize(alloc.width, alloc.height);
                offs = rootView.viewToModel(x, y, alloc, biasReturn);
            }
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
        return offs;
    }

    protected Rectangle getVisibleEditorRect() {
        Rectangle alloc = editor.getBounds();
        if ((alloc.width > 0) && (alloc.height > 0)) {
            alloc.x = alloc.y = 0;
            Insets insets = editor.getInsets();
            alloc.x += insets.left;
            alloc.y += insets.top;
            alloc.width -= insets.left + insets.right; // + caretMargin;
            alloc.height -= insets.top + insets.bottom;
            return alloc;
        }
        return null;
    }

    @Override
    public int getNextVisualPositionFrom(JTextComponent tc, int pos, Position.Bias b, int direction, Position.Bias[] biasRet) throws BadLocationException {
        return -1;
    }

    @Override
    public void damageRange(JTextComponent tc, int p0, int p1) {
        damageRange(tc, p0, p1, Position.Bias.Forward, Position.Bias.Backward);
    }

    @Override
    public void damageRange(JTextComponent t, int p0, int p1, Position.Bias firstBias, Position.Bias secondBias) {
    }

    @Override
    public EditorKit getEditorKit(JTextComponent t) {
        return defaultKit;
    }

    @Override
    public View getRootView(JTextComponent t) {
        return rootView;
    }

    /**
     * Root view that acts as a gateway between the component and the View
     * hierarchy.
     * <p>
     * Derived from BasicTextUI.
     */
    class RootView extends View {

        private View view;

        RootView() {
            super(null);
        }

        void setView(View v) {
            View oldView = view;
            view = null;
            if (oldView != null) {
                // get rid of back reference so that the old
                // hierarchy can be garbage collected.
                oldView.setParent(null);
            }
            if (v != null) {
                v.setParent(this);
            }
            view = v;
        }

        @Override
        public AttributeSet getAttributes() {
            return null;
        }

        @Override
        public float getPreferredSpan(int axis) {
            if (view != null) {
                return view.getPreferredSpan(axis);
            }
            return 10;
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (view != null) {
                return view.getMinimumSpan(axis);
            }
            return 10;
        }

        @Override
        public float getMaximumSpan(int axis) {
            return Integer.MAX_VALUE;
        }

        @Override
        public void preferenceChanged(View child, boolean width, boolean height) {
            editor.revalidate();
        }

        @Override
        public float getAlignment(int axis) {
            if (view != null) {
                return view.getAlignment(axis);
            }
            return 0;
        }

        @Override
        public void paint(Graphics g, Shape allocation) {
            if (view != null) {
                Rectangle alloc = (allocation instanceof Rectangle)
                        ? (Rectangle) allocation : allocation.getBounds();
                setSize(alloc.width, alloc.height);
                view.paint(g, allocation);
            }
        }

        @Override
        public void setParent(View parent) {
            throw new Error("Can't set parent on root view");
        }

        @Override
        public int getViewCount() {
            return 1;
        }

        @Override
        public View getView(int n) {
            return view;
        }

        @Override
        public int getViewIndex(int pos, Position.Bias b) {
            return 0;
        }

        @Override
        public Shape getChildAllocation(int index, Shape a) {
            return a;
        }

        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            if (view != null) {
                return view.modelToView(pos, a, b);
            }
            return null;
        }

        @Override
        public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
            if (view != null) {
                return view.modelToView(p0, b0, p1, b1, a);
            }
            return null;
        }

        @Override
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            if (view != null) {
                int retValue = view.viewToModel(x, y, a, bias);
                return retValue;
            }
            return -1;
        }

        @Override
        public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
                int direction,
                Position.Bias[] biasRet)
                throws BadLocationException {
            if (pos < -1 || pos > getDocument().getLength()) {
                throw new BadLocationException("invalid position", pos);
            }
            if (view != null) {
                int nextPos = view.getNextVisualPositionFrom(pos, b, a,
                        direction, biasRet);
                if (nextPos != -1) {
                    pos = nextPos;
                } else {
                    biasRet[0] = b;
                }
            }
            return pos;
        }

        @Override
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.insertUpdate(e, a, f);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.removeUpdate(e, a, f);
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.changedUpdate(e, a, f);
            }
        }

        @Override
        public Document getDocument() {
            return editor.getDocument();
        }

        @Override
        public int getStartOffset() {
            if (view != null) {
                return view.getStartOffset();
            }
            return getElement().getStartOffset();
        }

        @Override
        public int getEndOffset() {
            if (view != null) {
                return view.getEndOffset();
            }
            return getElement().getEndOffset();
        }

        @Override
        public Element getElement() {
            if (view != null) {
                return view.getElement();
            }
            return editor.getDocument().getDefaultRootElement();
        }

        public View breakView(int axis, float len, Shape a) {
            throw new Error("Can't break root view");
        }

        @Override
        public int getResizeWeight(int axis) {
            if (view != null) {
                return view.getResizeWeight(axis);
            }
            return 0;
        }

        @Override
        public void setSize(float width, float height) {
            if (view != null) {
                view.setSize(width, height);
            }
        }

        @Override
        public Container getContainer() {
            return editor;
        }

        @Override
        public ViewFactory getViewFactory() {
            EditorKit kit = getEditorKit(editor);
            ViewFactory f = kit.getViewFactory();
            if (f != null) {
                return f;
            }
            return CodeAreaUI.this;
        }
    }
}
