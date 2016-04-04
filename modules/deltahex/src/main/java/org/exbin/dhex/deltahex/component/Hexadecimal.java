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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.xbup.core.type.XBData;

/**
 * Hex editor component.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class Hexadecimal extends JComponent {

    private XBData data;

    private HexadecimalCaret caret;
    private SelectionRange selection;
    private SelectionChangedListener selectionChangedListener = null;
    private ViewMode viewMode = ViewMode.DUAL;
    private Section activeSection = Section.HEXADECIMAL;
    private EditationMode editationMode = EditationMode.OVERWRITE;
    private boolean showHeader = true;
    private boolean showLineNumbers = false;
    private boolean mouseDown;

    private HexadecimalLinePainter linePainter;

    private Color textColor;
    private Color oddBackgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color dualBackgroundColor;
    private Color cursorColor;
    private int subFontSpace = 3;

    private FontMetricsCache metricsCache = null;

    public Hexadecimal() {
        super();
        caret = new HexadecimalCaret(this);
        linePainter = new DefaultHexadecimalLinePainter(this);

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
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
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
                        long caretPosition = caret.getCaretPosition();
                        if (caretPosition > 0) {
                            caret.setCaretPosition(caretPosition - 1);
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_RIGHT: {
                        long caretPosition = caret.getCaretPosition();
                        if (caretPosition < data.getDataSize() * 2) {
                            caret.setCaretPosition(caretPosition + 1);
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_UP: {
                        long caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        if (caretPosition > 0) {
                            if (caretPosition > bytesPerLine * 2) {
                                caret.setCaretPosition(caretPosition - bytesPerLine * 2);
                            }
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_DOWN: {
                        long caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        long dataSize = data.getDataSize();
                        if (caretPosition < dataSize * 2) {
                            if (caretPosition + bytesPerLine * 2 < dataSize * 2) {
                                caret.setCaretPosition(caretPosition + bytesPerLine * 2);
                            }
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_HOME: {
                        long caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        if (caretPosition > 0) {
                            caret.setCaretPosition((caretPosition / (bytesPerLine * 2)) * bytesPerLine * 2);
                            updateSelection(e.getModifiersEx(), caretPosition);
                        }
                        break;
                    }
                    case KeyEvent.VK_END: {
                        long caretPosition = caret.getCaretPosition();
                        int bytesPerLine = getBytesPerLine();
                        long dataSize = data.getDataSize();
                        if (caretPosition < dataSize * 2) {
                            long newPosition = ((caretPosition / (bytesPerLine * 2)) + 1) * bytesPerLine * 2 - 1;
                            caret.setCaretPosition(newPosition < dataSize * 2 ? newPosition : dataSize * 2);
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
                        repaint();
                        break;
                    }
                }
            }
        });
    }

    private void moveCaret(MouseEvent me, int modifiers) {
        int bytesPerLine = getBytesPerLine();
        int cursorCharX = me.getX() / metricsCache.charWidth;
        int cursorY = me.getY() / metricsCache.lineHeight;
        if (cursorY > 1) {
            cursorY -= 2;
        } else if (cursorY > 0) {
            cursorY--;
        }
        int bytePosition = cursorCharX % 3;
        if (bytePosition > 2) {
            bytePosition = 1;
        }
        int cursorX = cursorCharX / 3;
        long dataPosition = (cursorX * 2 + bytePosition) + (cursorY * bytesPerLine * 2);
        if (dataPosition > data.getDataSize() * 2) {
            dataPosition = data.getDataSize() * 2;
        }
//        Rectangle oldCursorRect = caret.getCursorRect(bytesPerLine, lineHeight, charWidth);
        long caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition);
//        Rectangle newCursorRect = caret.getCursorRect(bytesPerLine, lineHeight, charWidth);
        updateSelection(modifiers, caretPosition);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        JDialog dialog = WindowUtils.createBasicDialog();
        Hexadecimal hexPanel = new Hexadecimal();
        XBData data = new XBData();
        data.loadFromStream(hexPanel.getClass().getResourceAsStream("/org/exbin/dhex/deltahex/resources/DeltaHexModule.properties"));
        hexPanel.setData(data);
        dialog.add(hexPanel);
        WindowUtils.invokeWindow(dialog);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (metricsCache == null) {
            computeFontMatrices();
        }

        int bytesPerLine = getBytesPerLine();
        Rectangle clipBounds = g.getClipBounds();

        int positionY = metricsCache.lineHeight;
        if (showHeader) {
            g.setColor(textColor);
            g.setFont(getFont());
            for (int i = 0; i < bytesPerLine; i++) {
                char[] chars = HexadecimalUtils.byteToHexChars((byte) i);
                g.drawChars(chars, 0, 2, i * metricsCache.charWidth * 3, positionY);
            }

            // g.drawString("TEST", 0, textFont.getSize());
        }

        // Render hexadecimal part
        positionY += metricsCache.lineHeight * 2;
        long line = 0;
        int byteOnLine = 0;
        long dataPosition = 0;
        long dataSize = data.getDataSize();
        do {
            if (byteOnLine == 0) {
                linePainter.paintBackground(g, line, positionY, dataPosition, bytesPerLine, metricsCache.lineHeight, metricsCache.charWidth);
            }

            if (dataPosition < dataSize) {
                linePainter.paintLine(g, line, positionY, dataPosition, bytesPerLine, metricsCache.lineHeight, metricsCache.charWidth, byteOnLine);
            } else {
                break;
            }

            byteOnLine++;
            dataPosition++;

            if (byteOnLine == bytesPerLine) {
                byteOnLine = 0;
                positionY += metricsCache.lineHeight;
                line++;
            }
        } while (positionY - metricsCache.lineHeight < clipBounds.height);

        caret.paint(g, bytesPerLine, metricsCache.lineHeight, metricsCache.charWidth);
    }

    private void updateSelection(int modifiers, long caretPosition) {
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
        Rectangle panelBounds = getBounds();
        int charsPerPanel = panelBounds.width / metricsCache.charWidth;
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
        return bytesPerLine > 0 ? bytesPerLine : 1;
    }

    public SelectionRange getSelection() {
        return selection;
    }

    public void selectAll() {
        selection = new SelectionRange(0, data.getDataSize() * 2);
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
        if (viewMode == ViewMode.PREVIEW) {
            return -1;
        }
        
        int hexadecimalX = 0;
        if (showLineNumbers) {
            hexadecimalX += metricsCache.charWidth * 8;
        }
        
        return hexadecimalX;
    }

    /**
     * Returns X start position of the ascii preview area.
     *
     * @return X position or -1 if area not present.
     */
    public int getPreviewX() {
        if (viewMode == ViewMode.HEXADECIMAL) {
            return -1;
        }

        int previewX = getBytesPerLine() * metricsCache.charWidth * 3;
        if (showLineNumbers) {
            previewX += metricsCache.charWidth * 8;
        }

        return previewX;
    }

    public XBData getData() {
        return data;
    }

    public void setData(XBData data) {
        this.data = data;
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
            metricsCache = new FontMetricsCache();
            metricsCache.metrics = g.getFontMetrics(font);
            metricsCache.charWidth = metricsCache.metrics.charWidth('0');
            int fontHeight = font.getSize();
            metricsCache.lineHeight = fontHeight + subFontSpace;
        }
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

    /**
     * Selection range is selection between two points where begin represents
     * originating point. End of the selection can be before or after begin.
     */
    public static class SelectionRange {

        private long begin;
        private long end;

        public SelectionRange() {
            begin = 0;
            end = 0;
        }

        public SelectionRange(long begin, long end) {
            this.begin = begin;
            this.end = end;
        }

        public long getBegin() {
            return begin;
        }

        public void setBegin(long begin) {
            this.begin = begin;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public long getSelectionFirst() {
            return end > begin ? begin : end;
        }

        public long getSelectionLast() {
            return end > begin ? end : begin;
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
     * Font metrics cache depending on current font.
     */
    private class FontMetricsCache {

        FontMetrics metrics;
        int charWidth;
        int lineHeight;
    }
}
