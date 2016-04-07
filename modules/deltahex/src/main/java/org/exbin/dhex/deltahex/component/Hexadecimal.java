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
package org.exbin.dhex.deltahex.component;

import org.exbin.dhex.deltahex.HexadecimalUtils;
import org.exbin.dhex.deltahex.CaretPosition;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import org.exbin.dhex.deltahex.EditableHexadecimalData;
import org.exbin.dhex.deltahex.HexadecimalData;

/**
 * Hex editor component.
 *
 * @version 0.1.0 2016/04/07
 * @author ExBin Project (http://exbin.org)
 */
public class Hexadecimal extends JComponent {

    private HexadecimalData data;

    private HexadecimalCaret caret;
    private SelectionRange selection;
    private SelectionChangedListener selectionChangedListener = null;
    private ViewMode viewMode = ViewMode.DUAL;
    private Section activeSection = Section.HEXADECIMAL;
    private EditationMode editationMode = EditationMode.OVERWRITE;
    private boolean showHeader = true;
    private boolean showLineNumbers = true;
    private boolean mouseDown;

    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private long scrollLinePostion = 1;

    private HexadecimalTextPainter linePainter;

    private Color textColor;
    private Color oddBackgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color dualBackgroundColor;
    private Color cursorColor;
    private int subFontSpace = 3;

    private final DimensionsCache dimensionsCache = new DimensionsCache();

    public Hexadecimal() {
        super();
        caret = new HexadecimalCaret(this);
        linePainter = new DefaultHexadecimalTextPainter(this);

        textColor = UIManager.getColor("TextArea.foreground");
        super.setBackground(UIManager.getColor("TextArea.background"));
        oddBackgroundColor = new Color(240, 240, 240);
        selectionColor = UIManager.getColor("TextArea.selectionForeground");
        selectionBackgroundColor = UIManager.getColor("TextArea.selectionBackground");
        dualBackgroundColor = Color.LIGHT_GRAY;
        cursorColor = UIManager.getColor("TextArea.caretForeground");

        init();
        computeFontMatrices();
    }

    private void init() {
        verticalScrollBar = new JScrollBar(Scrollbar.VERTICAL);
        verticalScrollBar.setVisible(false);
        verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                scrollLinePostion = verticalScrollBar.getValue();
                repaint();
            }
        });
        add(verticalScrollBar);
        horizontalScrollBar = new JScrollBar(Scrollbar.HORIZONTAL);
        horizontalScrollBar.setVisible(false);
        add(horizontalScrollBar);

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                computeDimensions();
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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON1) {
                    moveCaret(me, 0);

//                    if ((me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) > 0) {
//                        setCursorPosition((int) (me.getX() / scaleRatio) + windowPosition);
//                        if (getCursorPosition() > mousePressPosition) {
//                            selection = new SelectionRange(mousePressPosition, getCursorPosition());
//                        } else {
//                            selection = new SelectionRange(getCursorPosition(), mousePressPosition);
//                        }
//                    } else {
//                        clearSelection();
//                        int oldPosition = getCursorPosition();
//                        setCursorPosition((int) (me.getX() / scaleRatio) + windowPosition);
//                        mousePressPosition = getCursorPosition();
//                    }
                }
                mouseDown = true;
            }

            @Override
            public void mouseReleased(MouseEvent me) {
//                mouseClickEnd = (int) (me.getX() / scaleRatio);
//                if (mouseClickEnd < 0) mouseClickEnd = 0;
//                if (mouseClickEnd > getWidth()) mouseClickEnd = getWidth();
//                repaint();
                mouseDown = false;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                if (mouseDown) {
                    moveCaret(me, KeyEvent.SHIFT_DOWN_MASK);
//                    setCursorPosition((int) (me.getX() / scaleRatio) + windowPosition);
//                    if ((selection != null) || (getCursorPosition() > mousePressPosition + 3) || (getCursorPosition() < mousePressPosition - 3)) {
//                        if (getCursorPosition() > mousePressPosition) {
//                            selection = new SelectionRange(mousePressPosition, getCursorPosition());
//                        } else {
//                            selection = new SelectionRange(getCursorPosition(), mousePressPosition);
//                        }
//                        if (selectionChangedListener != null) {
//                            selectionChangedListener.selectionChanged();
//                        }
//
//                        repaint();
//                    }
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: {
                        CaretPosition caretPosition = caret.getCaretPosition();
                        if (activeSection == Section.HEXADECIMAL) {
                            boolean lowerHalf = caret.isLowerHalf();
                            if (lowerHalf) {
                                caret.setLowerHalf(false);
                                updateSelection(e.getModifiersEx(), caretPosition);
                            } else if (caretPosition.getDataPosition() > 0) {
                                caret.setCaretPosition(caretPosition.getDataPosition() - 1, true);
                                updateSelection(e.getModifiersEx(), caretPosition);
                            }
                        } else if (caretPosition.getDataPosition() > 0) {
                            caret.setCaretPosition(caretPosition.getDataPosition() - 1);
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_RIGHT: {
                        moveRight(e.getModifiersEx());
                        break;
                    }
                    case KeyEvent.VK_UP: {
                        CaretPosition caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        if (caretPosition.getDataPosition() > 0) {
                            if (caretPosition.getDataPosition() >= bytesPerLine) {
                                caret.setCaretPosition(caretPosition.getDataPosition() - bytesPerLine, caret.isLowerHalf());
                            }
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_DOWN: {
                        CaretPosition caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        long dataSize = data.getDataSize();
                        if (caretPosition.getDataPosition() < dataSize) {
                            if (caretPosition.getDataPosition() + bytesPerLine < dataSize) {
                                caret.setCaretPosition(caretPosition.getDataPosition() + bytesPerLine, caret.isLowerHalf());
                            }
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_HOME: {
                        CaretPosition caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        if (caretPosition.getDataPosition() > 0 || caret.isLowerHalf()) {
                            caret.setCaretPosition((caretPosition.getDataPosition() / bytesPerLine) * bytesPerLine);
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_END: {
                        CaretPosition caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        long dataSize = data.getDataSize();
                        if (caretPosition.getDataPosition() < dataSize) {
                            if (activeSection == Section.HEXADECIMAL) {
                                long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                                caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize, true);
                            } else {
                                long newPosition = ((caretPosition.getDataPosition() / bytesPerLine) + 1) * bytesPerLine - 1;
                                caret.setCaretPosition(newPosition < dataSize ? newPosition : dataSize);
                            }
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_INSERT: {
                        if (editationMode != EditationMode.READ_ONLY) {
                            setEditationMode(editationMode == EditationMode.INSERT ? EditationMode.OVERWRITE : EditationMode.INSERT);
                            repaint();
                        }
                        break;
                    }
                    case KeyEvent.VK_TAB: {
                        activeSection = activeSection == Section.HEXADECIMAL ? Section.PREVIEW : Section.HEXADECIMAL;
                        if (activeSection == Section.PREVIEW) {
                            caret.setLowerHalf(false);
                        }
                        repaint();
                        break;
                    }
                    default: {
                        if (activeSection == Section.HEXADECIMAL) {
                            if ((e.getKeyChar() >= '0' && e.getKeyChar() <= '9')
                                    || (e.getKeyChar() >= 'a' && e.getKeyChar() <= 'f')) {
                                CaretPosition caretPosition = caret.getCaretPosition();
                                int value;
                                if (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') {
                                    value = e.getKeyChar() - '0';
                                } else {
                                    value = e.getKeyChar() - 'a' + 10;
                                }
                                setHalfByte(value);
                                moveRight(0);
                            }
                        } else {
                            char keyChar = e.getKeyChar();
                            if (keyChar > 31 && keyChar < 255) {
                                CaretPosition caretPosition = caret.getCaretPosition();
                                long dataPosition = caretPosition.getDataPosition();
                                ((EditableHexadecimalData) data).setByte(dataPosition, (byte) keyChar);
                                moveRight(0);
                            }
                        }
                    }
                }
            }
        });
    }

    private void setHalfByte(int value) {
        CaretPosition caretPosition = caret.getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();

        byte byteValue = data.getByte(dataPosition);

        if (caretPosition.isLowerHalf()) {
            byteValue = (byte) ((byteValue & 0xf0) | value);
        } else {
            byteValue = (byte) ((byteValue & 0xf) | (value << 4));
        }

        ((EditableHexadecimalData) data).setByte(dataPosition, byteValue);
    }

    private void moveRight(int modifiers) {
        CaretPosition caretPosition = caret.getCaretPosition();
        if (activeSection == Section.HEXADECIMAL) {
            boolean lowerHalf = caret.isLowerHalf();
            if (!lowerHalf) {
                caret.setLowerHalf(true);
                updateSelection(modifiers, caretPosition);
            } else if (caretPosition.getDataPosition() < data.getDataSize()) {
                caret.setCaretPosition(caretPosition.getDataPosition() + 1, false);
                updateSelection(modifiers, caretPosition);
            }
        } else if (caretPosition.getDataPosition() < data.getDataSize()) {
            caret.setCaretPosition(caretPosition.getDataPosition() + 1);
            updateSelection(modifiers, caretPosition);
        }
    }

    private void moveCaret(MouseEvent me, int modifiers) {
        int bytesPerLine = getBytesPerLine();
        int cursorCharX = me.getX() / dimensionsCache.charWidth;
        int cursorY = me.getY() / dimensionsCache.lineHeight;
        if (cursorY > 1) {
            cursorY -= 2;
        } else if (cursorY > 0) {
            cursorY--;
        }
        if (showLineNumbers) {
            if (cursorCharX < 9) {
                cursorCharX = 0;
            } else {
                cursorCharX -= 9;
            }
        }

        long dataPosition;
        boolean lowerHalf = false;
        if (cursorCharX < bytesPerLine * 3 || viewMode == ViewMode.HEXADECIMAL) {
            setActiveSection(Section.HEXADECIMAL);
            int bytePosition = cursorCharX % 3;
            lowerHalf = bytePosition > 0;

            int cursorX = cursorCharX / 3;
            int byteOnLine = cursorX;
            if (byteOnLine >= bytesPerLine) {
                byteOnLine = bytesPerLine - 1;
            }
            dataPosition = byteOnLine + (cursorY * bytesPerLine);
        } else {
            setActiveSection(Section.PREVIEW);
            int byteOnLine = (cursorCharX - (bytesPerLine * 3));
            if (byteOnLine >= bytesPerLine) {
                byteOnLine = bytesPerLine - 1;
            }
            dataPosition = byteOnLine + (cursorY * bytesPerLine);
            if (dataPosition > data.getDataSize()) {
                dataPosition = data.getDataSize();
            }
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, lowerHalf);

        updateSelection(modifiers, caretPosition);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (dimensionsCache.fontMetrics == null) {
            computeFontMatrices();
        }

        int bytesPerLine = getBytesPerLine();
        Rectangle clipBounds = g.getClipBounds();

        int positionY = dimensionsCache.lineHeight;
        if (showHeader) {
            int hexadecimalX = getHexadecimalX();
            g.setColor(textColor);
            g.setFont(getFont());
            for (int i = 0; i < bytesPerLine; i++) {
                char[] chars = HexadecimalUtils.byteToHexChars((byte) i);
                g.drawChars(chars, 0, 2, hexadecimalX + i * dimensionsCache.charWidth * 3, positionY);
            }
        }

        // Render hexadecimal part
        positionY += dimensionsCache.hexadecimalY;
        long line = scrollLinePostion;
        int byteOnLine = 0;
        long dataPosition = line * bytesPerLine;
        long dataSize = data.getDataSize();
        do {
            if (byteOnLine == 0) {
                linePainter.paintBackground(g, line, positionY, dataPosition, bytesPerLine, dimensionsCache.lineHeight, dimensionsCache.charWidth);
            }

            if (dataPosition < dataSize || (dataPosition == dataSize && byteOnLine == 0)) {
                linePainter.paintText(g, line, positionY, dataPosition, bytesPerLine, dimensionsCache.lineHeight, dimensionsCache.charWidth, byteOnLine);
            } else {
                break;
            }

            byteOnLine++;
            dataPosition++;

            if (byteOnLine == bytesPerLine) {
                byteOnLine = 0;
                positionY += dimensionsCache.lineHeight;
                line++;
            }
        } while (positionY - dimensionsCache.lineHeight < clipBounds.height);

        caret.paint(g, bytesPerLine, dimensionsCache.lineHeight, dimensionsCache.charWidth);
    }

    private void updateSelection(int modifiers, CaretPosition caretPosition) {
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0) {
            if (selection != null) {
                selection.end = caret.getCaretPosition();
            } else {
                selection = new SelectionRange(caretPosition, caret.getCaretPosition());
            }
        } else {
            clearSelection();
        }
        repaint();
    }

    private int getBytesPerLine() {
        return dimensionsCache.bytesPerLine;
    }

    public SelectionRange getSelection() {
        return selection;
    }

    public void selectAll() {
        CaretPosition endPosition = new CaretPosition(data.getDataSize(), false);
        selection = new SelectionRange(new CaretPosition(), endPosition);
        if (selectionChangedListener != null) {
            selectionChangedListener.selectionChanged();
        }

        repaint();
    }

    public void clearSelection() {
        selection = null;
        if (selectionChangedListener != null) {
            selectionChangedListener.selectionChanged();
        }

        repaint();
    }

    public boolean hasSelection() {
        return selection != null && (selection.begin != selection.end);
    }

    public void setSelection(SelectionRange selection) {
        this.selection = selection;

        if (selectionChangedListener != null) {
            selectionChangedListener.selectionChanged();
        }
    }

    public void setSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        this.selectionChangedListener = selectionChangedListener;
    }

    /**
     * Returns X start position of the hexadecimal area.
     *
     * @return X position or -1 if area not present.
     */
    public int getHexadecimalX() {
        return dimensionsCache.hexadecimalX;
    }

    /**
     * Returns X start position of the ascii preview area.
     *
     * @return X position or -1 if area not present.
     */
    public int getPreviewX() {
        return dimensionsCache.previewX;
    }

    public HexadecimalData getData() {
        return data;
    }

    public void setData(HexadecimalData data) {
        this.data = data;
        computeDimensions();
        repaint();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        computeFontMatrices();
    }

    private void computeFontMatrices() {
        Graphics g = getGraphics();
        if (g != null) {
            Font font = getFont();
            dimensionsCache.fontMetrics = g.getFontMetrics(font);
            dimensionsCache.charWidth = dimensionsCache.fontMetrics.charWidth('0');
            int fontHeight = font.getSize();
            dimensionsCache.lineHeight = fontHeight + subFontSpace;
            computeDimensions();
        }
    }

    private void computeDimensions() {
        if (dimensionsCache.fontMetrics == null) {
            return;
        }

        if (viewMode == ViewMode.PREVIEW) {
            dimensionsCache.hexadecimalX = -1;
        } else {
            dimensionsCache.hexadecimalX = 0;
            if (showLineNumbers) {
                dimensionsCache.hexadecimalX += dimensionsCache.charWidth * 9;
            }
        }

        dimensionsCache.hexadecimalY = dimensionsCache.lineHeight * 2;

        if (viewMode == ViewMode.HEXADECIMAL) {
            dimensionsCache.previewX = -1;
        } else {
            dimensionsCache.previewX = getBytesPerLine() * dimensionsCache.charWidth * 3;
            if (showLineNumbers) {
                dimensionsCache.previewX += dimensionsCache.charWidth * 9;
            }
        }

        Rectangle panelBounds = getBounds();
        int width = panelBounds.width;
        if (verticalScrollBar.isVisible()) {
            width -= dimensionsCache.scrollBarThickness;
        }
        if (showLineNumbers) {
            width -= dimensionsCache.charWidth * 9;
        }
        int charsPerPanel = width / dimensionsCache.charWidth;
        int charsPerByte;
        switch (viewMode) {
            case HEXADECIMAL: {
                charsPerByte = 3;
                break;
            }
            case PREVIEW: {
                charsPerByte = 1;
                break;
            }
            case DUAL: {
                charsPerByte = 4;
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected viewMode " + viewMode.name());
            }
        }
        int bytesPerLine = charsPerPanel / charsPerByte;
        dimensionsCache.bytesPerLine = bytesPerLine > 0 ? bytesPerLine : 1;

        dimensionsCache.linesPerScreen = (panelBounds.height - dimensionsCache.hexadecimalY) / dimensionsCache.lineHeight;

        int lines = (int) (data.getDataSize() / bytesPerLine);
        boolean verticalScrollBarVisible = lines > dimensionsCache.linesPerScreen;
        boolean horizontalScrollBarVisible = false;
        Rectangle bounds = getBounds();
        int verticalScrollBarHeight = bounds.height - dimensionsCache.hexadecimalY;
        if (horizontalScrollBarVisible) {
            verticalScrollBarHeight -= dimensionsCache.scrollBarThickness + 1;
        }
        verticalScrollBar.setBounds(bounds.width - dimensionsCache.scrollBarThickness, dimensionsCache.hexadecimalY, dimensionsCache.scrollBarThickness, verticalScrollBarHeight);
        verticalScrollBar.setVisible(verticalScrollBarVisible);
        verticalScrollBar.setMaximum(lines);

        horizontalScrollBar.setBounds(dimensionsCache.hexadecimalX, bounds.height - dimensionsCache.scrollBarThickness, bounds.width - dimensionsCache.hexadecimalX - dimensionsCache.scrollBarThickness + 1, dimensionsCache.scrollBarThickness);
        horizontalScrollBar.setVisible(horizontalScrollBarVisible);
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getOddBackgroundColor() {
        return oddBackgroundColor;
    }

    public void setBackgroundColor(Color oddBackgroundColor) {
        this.oddBackgroundColor = oddBackgroundColor;
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        this.selectionBackgroundColor = selectionBackgroundColor;
    }

    public Color getDualBackgroundColor() {
        return dualBackgroundColor;
    }

    public void setDualBackgroundColor(Color dualBackgroundColor) {
        this.dualBackgroundColor = dualBackgroundColor;
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }

    public void setSubFontSpace(int subFontSpace) {
        this.subFontSpace = subFontSpace;
    }

    public Section getActiveSection() {
        return activeSection;
    }

    public void setActiveSection(Section activeSection) {
        this.activeSection = activeSection;
    }

    public EditationMode getEditationMode() {
        return editationMode;
    }

    public void setEditationMode(EditationMode editationMode) {
        this.editationMode = editationMode;
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }

    /**
     * Selection range is selection between two points where begin represents
     * originating point. End of the selection can be before or after begin.
     */
    public static class SelectionRange {

        private CaretPosition begin = new CaretPosition();
        private CaretPosition end = new CaretPosition();

        public SelectionRange() {
        }

        public SelectionRange(CaretPosition begin, CaretPosition end) {
            this.begin.setPosition(begin);
            this.end.setPosition(end);
        }

        public CaretPosition getBegin() {
            return begin;
        }

        public void setBegin(CaretPosition begin) {
            this.begin = begin;
        }

        public CaretPosition getEnd() {
            return end;
        }

        public void setEnd(CaretPosition end) {
            this.end = end;
        }

        public CaretPosition getSelectionFirst() {
            return (end.getDataPosition() > begin.getDataPosition())
                    || (end.getDataPosition() == begin.getDataPosition() && !begin.isLowerHalf()) ? begin : end;
        }

        public CaretPosition getSelectionLast() {
            return (end.getDataPosition() > begin.getDataPosition())
                    || (end.getDataPosition() == begin.getDataPosition() && !begin.isLowerHalf()) ? end : begin;
        }
    }

    public interface SelectionChangedListener {

        void selectionChanged();
    }

    public enum ViewMode {
        HEXADECIMAL, PREVIEW, DUAL
    }

    public enum Section {
        HEXADECIMAL, PREVIEW
    }

    public enum EditationMode {
        OVERWRITE, INSERT, READ_ONLY
    }

    /**
     * Precomputed dimensions for the hexadecimal editor.
     */
    private class DimensionsCache {

        int hexadecimalX;
        int hexadecimalY;
        int previewX;
        int bytesPerLine;
        int linesPerScreen;
        int scrollBarThickness = 17;

        FontMetrics fontMetrics = null;
        int charWidth;
        int lineHeight;
    }
}
