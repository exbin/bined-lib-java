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
package org.exbin.deltahex.swing.basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.exbin.deltahex.CaretMovedListener;
import org.exbin.deltahex.CaretPosition;
import org.exbin.deltahex.CodeAreaSection;
import org.exbin.deltahex.CodeAreaUtils;
import org.exbin.deltahex.HexCharactersCase;
import org.exbin.deltahex.CodeAreaViewMode;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.DataChangedListener;
import org.exbin.deltahex.EditationMode;
import org.exbin.deltahex.EditationModeChangedListener;
import org.exbin.deltahex.ScrollBarVisibility;
import org.exbin.deltahex.ScrollingListener;
import org.exbin.deltahex.SelectionChangedListener;
import org.exbin.deltahex.SelectionRange;
import org.exbin.deltahex.swing.CharacterRenderingMode;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.swing.CodeAreaPainter;
import org.exbin.deltahex.swing.CodeAreaSwingUtils;
import org.exbin.deltahex.swing.CodeAreaWorker;
import org.exbin.utils.binary_data.OutOfBoundsException;
import org.exbin.deltahex.capability.CaretCapable;
import org.exbin.deltahex.capability.CharsetCapable;
import org.exbin.deltahex.capability.CodeTypeCapable;
import org.exbin.deltahex.capability.EditationModeCapable;
import org.exbin.deltahex.capability.ViewModeCapable;
import org.exbin.deltahex.capability.ScrollingCapable;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2017/11/08
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaWorker implements CodeAreaWorker, CaretCapable, ScrollingCapable, ViewModeCapable, CodeTypeCapable, EditationModeCapable, CharsetCapable {

    @Nonnull
    protected final CodeArea codeArea;
    private int subFontSpace = 3;

    @Nonnull
    private CodeAreaPainter painter;

    @Nonnull
    private SelectionRange selection;
    @Nonnull
    private Charset charset = Charset.defaultCharset();
    private boolean handleClipboard = true;

    @Nonnull
    private EditationMode editationMode = EditationMode.OVERWRITE;

    @Nonnull
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    @Nonnull
    private BasicBorderPaintMode borderPaintMode = BasicBorderPaintMode.STRIPED;
    @Nonnull
    private CharacterRenderingMode characterRenderingMode = CharacterRenderingMode.AUTO;
    @Nonnull
    private CodeType codeType = CodeType.HEXADECIMAL;
    @Nonnull
    private HexCharactersCase hexCharactersCase = HexCharactersCase.UPPER;
    private boolean showShadowCursor = true;

    @Nonnull
    private DefaultCodeAreaCaret caret;

    @Nonnull
    private final JScrollPane scrollPanel;
    @Nonnull
    private final CodeAreaDataView dataView;
    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.LINE;
    @Nonnull
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();

    public DefaultCodeAreaWorker(@Nonnull CodeArea codeArea) {
        this.codeArea = codeArea;

        scrollPanel = new JScrollPane();
        scrollPanel.setBorder(null);
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        verticalScrollBar.setVisible(false);
        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new DefaultCodeAreaWorker.VerticalAdjustmentListener());
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        horizontalScrollBar.setIgnoreRepaint(false);
        horizontalScrollBar.setVisible(true);
        horizontalScrollBar.addAdjustmentListener(new DefaultCodeAreaWorker.HorizontalAdjustmentListener());
        codeArea.add(scrollPanel);
        dataView = new CodeAreaDataView(codeArea);
        dataView.setOpaque(false);
        scrollPanel.setOpaque(false);
        scrollPanel.setBackground(Color.RED);
        scrollPanel.setViewportView(dataView);
        scrollPanel.getViewport().setOpaque(false);
        caret = new DefaultCodeAreaCaret(codeArea);

        this.painter = new DefaultCodeAreaPainter(codeArea);

        DefaultCodeAreaMouseListener codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea);
        codeArea.addMouseListener(codeAreaMouseListener);
        codeArea.addMouseMotionListener(codeAreaMouseListener);
        codeArea.addMouseWheelListener(codeAreaMouseListener);
        dataView.addMouseListener(codeAreaMouseListener);
        dataView.addMouseMotionListener(codeAreaMouseListener);
        dataView.addMouseWheelListener(codeAreaMouseListener);
    }

    @Override
    public void dataViewScrolled(@Nonnull Graphics g) {
        if (!isInitialized()) {
            return;
        }

        resetScrollState();
        if (state.characterWidth > 0) {
            resetCharPositions();
            paintComponent(g);
        }
    }

    @Nonnull
    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(@Nonnull CodeAreaPainter painter) {
        if (painter == null) {
            throw new NullPointerException("Painter cannot be null");
        }

        this.painter = painter;
        codeArea.repaint();
    }

    @Override
    public boolean isInitialized() {
        return painter.isInitialized();
    }

    @Override
    public void paintComponent(@Nonnull Graphics g) {
        painter.paintComponent(g);
    }

    @Nonnull
    public DefaultCodeAreaCaret getCaret() {
        return caret;
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    @Nonnull
    public CodeAreaSection getActiveSection() {
        return caret.getSection();
    }

    @Nonnull
    public CaretPosition getCaretPosition() {
        return caret.getCaretPosition();
    }

    public void setCaretPosition(@Nonnull CaretPosition caretPosition) {
        caret.setCaretPosition(caretPosition);
        notifyCaretMoved();
    }

    public void setCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
        notifyCaretMoved();
    }

    public void setCaretPosition(long dataPosition, int codeOffset) {
        caret.setCaretPosition(dataPosition, codeOffset);
        notifyCaretMoved();
    }

    @Override
    public int getCursorShape(int x, int y) {
        Rectangle dataViewRectangle = getDataViewRectangle();
        if (x >= dataViewRectangle.x && y >= dataViewRectangle.y) {
            return Cursor.TEXT_CURSOR;
        }

        return Cursor.DEFAULT_CURSOR;
    }

    @Override
    public void paintCursor(@Nonnull Graphics g) {
        if (!codeArea.hasFocus()) {
            return;
        }

        DefaultCodeAreaCaret caret = getCaret();
        int codeDigits = getCodeType().getMaxDigitsForByte();
        int bytesPerLine = getBytesPerLine();
        int lineHeight = getLineHeight();
        int characterWidth = getCharacterWidth();
        int linesPerRect = getLinesPerRectangle();
        Point cursorPoint = getCursorPoint(bytesPerLine, lineHeight, characterWidth, linesPerRect);
        boolean cursorVisible = caret.isCursorVisible();
        DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();

        if (cursorVisible && cursorPoint != null) {
            cursorPoint.setLocation(cursorPoint.x + state.lineNumbersAreaWidth, cursorPoint.y + state.headerAreaHeight);
            g.setColor(state.colors.cursor);
            if (renderingMode == DefaultCodeAreaCaret.CursorRenderingMode.XOR) {
                g.setXORMode(Color.WHITE);
            }

            DefaultCodeAreaCaret.CursorShape cursorShape = getEditationMode() == EditationMode.INSERT ? caret.getInsertCursorShape() : caret.getOverwriteCursorShape();
            int cursorThickness = 0;
            if (cursorShape.getWidth() != DefaultCodeAreaCaret.CursorShapeWidth.FULL) {
                cursorThickness = caret.getCursorThickness(cursorShape, characterWidth, lineHeight);
            }
            switch (cursorShape) {
                case LINE_TOP:
                case DOUBLE_TOP:
                case QUARTER_TOP:
                case HALF_TOP: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
                            characterWidth, cursorThickness, renderingMode);
                    break;
                }
                case LINE_BOTTOM:
                case DOUBLE_BOTTOM:
                case QUARTER_BOTTOM:
                case HALF_BOTTOM: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y + lineHeight - cursorThickness,
                            characterWidth, cursorThickness, renderingMode);
                    break;
                }
                case LINE_LEFT:
                case DOUBLE_LEFT:
                case QUARTER_LEFT:
                case HALF_LEFT: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
                    break;
                }
                case LINE_RIGHT:
                case DOUBLE_RIGHT:
                case QUARTER_RIGHT:
                case HALF_RIGHT: {
                    paintCursorRect(g, cursorPoint.x + characterWidth - cursorThickness, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
                    break;
                }
                case BOX: {
                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
                            characterWidth, lineHeight, renderingMode);
                    break;
                }
                case FRAME: {
                    g.drawRect(cursorPoint.x, cursorPoint.y, characterWidth, lineHeight - 1);
                    break;
                }
                case BOTTOM_CORNERS:
                case CORNERS: {
                    int quarterWidth = characterWidth / 4;
                    int quarterLine = lineHeight / 4;
                    if (cursorShape == DefaultCodeAreaCaret.CursorShape.CORNERS) {
                        g.drawLine(cursorPoint.x, cursorPoint.y,
                                cursorPoint.x + quarterWidth, cursorPoint.y);
                        g.drawLine(cursorPoint.x + characterWidth - quarterWidth, cursorPoint.y,
                                cursorPoint.x + characterWidth, cursorPoint.y);

                        g.drawLine(cursorPoint.x, cursorPoint.y + 1,
                                cursorPoint.x, cursorPoint.y + quarterLine);
                        g.drawLine(cursorPoint.x + characterWidth, cursorPoint.y + 1,
                                cursorPoint.x + characterWidth, cursorPoint.y + quarterLine);
                    }

                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x, cursorPoint.y + lineHeight - 2);
                    g.drawLine(cursorPoint.x + characterWidth, cursorPoint.y + lineHeight - quarterLine - 1,
                            cursorPoint.x + characterWidth, cursorPoint.y + lineHeight - 2);

                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - 1,
                            cursorPoint.x + quarterWidth, cursorPoint.y + lineHeight - 1);
                    g.drawLine(cursorPoint.x + characterWidth - quarterWidth, cursorPoint.y + lineHeight - 1,
                            cursorPoint.x + characterWidth, cursorPoint.y + lineHeight - 1);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected cursor shape type " + cursorShape.name());
                }
            }

            if (renderingMode == DefaultCodeAreaCaret.CursorRenderingMode.XOR) {
                g.setPaintMode();
            }
        }

        // Paint shadow cursor
        if (getViewMode() == CodeAreaViewMode.DUAL && showShadowCursor) {
            g.setColor(state.colors.cursor);
            Point shadowCursorPoint = getShadowCursorPoint(bytesPerLine, lineHeight, characterWidth, linesPerRect);
            shadowCursorPoint.setLocation(shadowCursorPoint.x + state.lineNumbersAreaWidth, shadowCursorPoint.y + state.headerAreaHeight);
            Graphics2D g2d = (Graphics2D) g.create();
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
            g2d.setStroke(dashed);
            g2d.drawRect(shadowCursorPoint.x, shadowCursorPoint.y,
                    characterWidth * (getActiveSection() == CodeAreaSection.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
        }
    }

    private void paintCursorRect(@Nonnull Graphics g, int x, int y, int width, int height, @Nonnull DefaultCodeAreaCaret.CursorRenderingMode renderingMode) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRect(x, y, width, height);
                break;
            }
            case XOR: {
                Rectangle rect = new Rectangle(x, y, width, height);
                Rectangle intersection = rect.intersection(g.getClipBounds());
                if (!intersection.isEmpty()) {
                    g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
                }
                break;
            }
            case NEGATIVE: {
                Rectangle rect = new Rectangle(x, y, width, height);
                Rectangle clipBounds = g.getClipBounds();
                Rectangle intersection;
                if (clipBounds != null) {
                    intersection = rect.intersection(clipBounds);
                } else {
                    intersection = rect;
                }
                if (intersection.isEmpty()) {
                    break;
                }
                Shape clip = g.getClip();
                g.setClip(intersection.x, intersection.y, intersection.width, intersection.height);
                g.fillRect(x, y, width, height);
                g.setColor(state.colors.negativeCursor);
                Rectangle codeRect = getDataViewRect();
                int previewX = getPreviewX();
                int charWidth = getCharacterWidth();
                int lineHeight = getLineHeight();
                int line = (y + scrollPosition.getScrollLineOffset() - codeRect.y) / lineHeight;
                int scrolledX = x + scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();
                int posY = codeRect.y + (line + 1) * lineHeight - subFontSpace - scrollPosition.getScrollLineOffset();
                if (getViewMode() != CodeAreaViewMode.CODE_MATRIX && scrolledX >= previewX) {
                    int charPos = (scrolledX - previewX) / charWidth;
                    long dataSize = codeArea.getDataSize();
                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + charPos - scrollPosition.getLineDataOffset();
                    if (dataPosition >= dataSize) {
                        g.setClip(clip);
                        break;
                    }

                    char[] previewChars = new char[1];
                    Charset charset = getCharset();
                    CharsetEncoder encoder = charset.newEncoder();
                    int maxCharLength = (int) encoder.maxBytesPerChar();
                    byte[] data = new byte[maxCharLength];

                    if (maxCharLength > 1) {
                        int charDataLength = maxCharLength;
                        if (dataPosition + maxCharLength > dataSize) {
                            charDataLength = (int) (dataSize - dataPosition);
                        }

                        codeArea.getData().copyToArray(dataPosition, data, 0, charDataLength);
                        String displayString = new String(data, 0, charDataLength, charset);
                        if (!displayString.isEmpty()) {
                            previewChars[0] = displayString.charAt(0);
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != charset) {
                            buildCharMapping(charset);
                        }

                        previewChars[0] = charMapping[codeArea.getData().getByte(dataPosition) & 0xFF];
                    }
                    int posX = previewX + charPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
                    if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                        g.drawChars(previewChars, 0, 1, posX, posY);
                    } else {
                        drawCenteredChar(g, previewChars, 0, charWidth, posX, posY);
                    }
                } else {
                    int charPos = (scrolledX - codeRect.x) / charWidth;
                    int byteOffset = computePositionByte(charPos);
                    int codeCharPos = computeFirstCodeCharPos(byteOffset);
                    char[] lineChars = new char[getCodeType().getMaxDigitsForByte()];
                    long dataSize = codeArea.getDataSize();
                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + byteOffset - scrollPosition.getLineDataOffset();
                    if (dataPosition >= dataSize) {
                        g.setClip(clip);
                        break;
                    }

                    byte dataByte = codeArea.getData().getByte(dataPosition);
                    CodeAreaUtils.byteToCharsCode(dataByte, getCodeType(), lineChars, 0, hexCharactersCase);
                    int posX = codeRect.x + codeCharPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
                        g.drawChars(lineChars, charsOffset, 1, posX + (charsOffset * charWidth), posY);
                    } else {
                        drawCenteredChar(g, lineChars, charsOffset, charWidth, posX + (charsOffset * charWidth), posY);
                    }
                }
                g.setClip(clip);
                break;
            }
        }
    }

    /**
     * Draws char in array centering it in precomputed space.
     *
     * @param g graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param charWidthSpace default character width
     * @param startX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChar(@Nonnull Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY) {
        int charWidth = state.fontMetrics.charWidth(drawnChars[charOffset]);
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, startX, positionY, (charWidthSpace + 1 - charWidth) >> 1);
    }

    protected void drawShiftedChar(@Nonnull Graphics g, char[] drawnChars, int charOffset, int charWidthSpace, int startX, int positionY, int shift) {
        g.drawChars(drawnChars, charOffset, 1, startX + shift, positionY);
    }

    private void buildCharMapping(@Nonnull Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    @Override
    public int getPreviewX() {
        return computeFirstCodeCharPos(getBytesPerLine()) * getCharacterWidth();
    }

    @Override
    public int getPreviewFirstChar() {
        return computeLastCodeCharPos(getBytesPerLine());
    }

    @Override
    public void rebuildColors() {
    }

    @Nonnull
    @Override
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Override
    public void setViewMode(@Nonnull CodeAreaViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == CodeAreaViewMode.CODE_MATRIX) {
            getCaret().setSection(CodeAreaSection.CODE_MATRIX);
            notifyCaretMoved();
        } else if (viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            getCaret().setSection(CodeAreaSection.TEXT_PREVIEW);
            notifyCaretMoved();
        }
        codeArea.resetPainter();
        codeArea.repaint();
    }

    @Override
    @Nonnull
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void setCodeType(@Nonnull CodeType codeType) {
        this.codeType = codeType;
        codeArea.resetPainter();
        codeArea.repaint();
    }

    @Override
    public int getBytesPerRectangle() {
        return 100;
    }

    @Override
    public int getLinesPerRectangle() {
        if (state.lineHeight == 0) {
            return 0;
        }

        return state.areaHeight / state.lineHeight + 1;
    }

    @Override
    public int getBytesPerLine() {
        return 16;
    }

    @Override
    public int getCharactersPerLine() {
        return state.charactersPerLine;
    }

    @Override
    public int getLineHeight() {
        return state.lineHeight;
    }

    @Override
    public int getCharacterWidth() {
        return state.characterWidth;
    }

    @Override
    public int computePositionByte(int lineCharPosition) {
        return lineCharPosition / (state.maxDigits + 1);
    }

    @Override
    public int computeCodeAreaCharacter(int pixelX) {
        return pixelX / getCharacterWidth();
    }

    @Override
    public int computeCodeAreaLine(int pixelY) {
        return pixelY / getLineHeight();
    }

    @Override
    public int computeFirstCodeCharPos(int byteOffset) {
        return byteOffset * (state.maxDigits + 1);
    }

    @Override
    public int computeLastCodeCharPos(int byteOffset) {
        return computeFirstCodeCharPos(byteOffset + 1) - 2;
    }

    @Override
    public long cursorPositionToDataPosition(long line, int byteOffset) throws OutOfBoundsException {
        return 16;
    }

    @Override
    @Nonnull
    public CaretPosition mousePositionToCaretPosition(int mouseX, int mouseY) {
        CodeAreaPainter painter = codeArea.getPainter();
        Rectangle hexRect = getDataViewRectangle();
        CodeAreaViewMode viewMode = getViewMode();
        CodeType codeType = getCodeType();
        DefaultCodeAreaCaret caret = getCaret();
        int bytesPerLine = codeArea.getBytesPerLine();
        if (mouseX < hexRect.x) {
            mouseX = hexRect.x;
        }
        int cursorCharX = computeCodeAreaCharacter(mouseX - hexRect.x + scrollPosition.getScrollCharOffset()) + scrollPosition.getScrollCharPosition();
        long cursorLineY = computeCodeAreaLine(mouseY - hexRect.y + scrollPosition.getScrollLineOffset()) + scrollPosition.getScrollLinePosition();
        if (cursorLineY < 0) {
            cursorLineY = 0;
        }
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnLine;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < painter.getPreviewFirstChar()) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(CodeAreaSection.CODE_MATRIX);
            byteOnLine = painter.computePositionByte(cursorCharX);
            if (byteOnLine >= bytesPerLine) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - painter.computeFirstCodeCharPos(byteOnLine);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(CodeAreaSection.TEXT_PREVIEW);
            byteOnLine = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnLine -= painter.getPreviewFirstChar();
            }
        }

        if (byteOnLine >= bytesPerLine) {
            byteOnLine = bytesPerLine - 1;
        }

        dataPosition = byteOnLine + (cursorLineY * bytesPerLine) - scrollPosition.getLineDataOffset();
        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        long dataSize = codeArea.getDataSize();
        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        CaretPosition caretPosition = caret.getCaretPosition();
        caret.setCaretPosition(dataPosition, codeOffset);

        return caretPosition;
    }

    @Nonnull
    public Rectangle getDataViewRect() {
        // TODO cache
        return new Rectangle(state.lineNumbersAreaWidth, state.headerAreaHeight, state.areaWidth - state.lineNumbersAreaWidth, state.areaHeight - state.headerAreaHeight);
    }

    @Override
    public void revealCursor() {
        revealPosition(caret.getCaretPosition().getDataPosition(), caret.getSection());
    }

    public void revealPosition(long position, @Nonnull CodeAreaSection section) {
        if (!isInitialized()) {
            // Ignore if painter not initialized
            return;
        }

        boolean scrolled = false;
        Rectangle hexRect = getDataViewRectangle();
        int bytesPerRect = getBytesPerRectangle();
        int linesPerRect = getLinesPerRectangle();
        int bytesPerLine = getBytesPerLine();
        long caretLine = position / bytesPerLine;

        int positionByte = computePositionByte((int) (position % bytesPerLine));

        if (caretLine <= scrollPosition.getScrollLinePosition()) {
            scrollPosition.setScrollLinePosition(caretLine);
            scrollPosition.setScrollLineOffset(0);
            scrolled = true;
        } else if (caretLine >= scrollPosition.getScrollLinePosition() + linesPerRect) {
            scrollPosition.setScrollLinePosition(caretLine - linesPerRect);
            if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
                scrollPosition.setScrollLineOffset(getLineHeight() - (hexRect.height % getLineHeight()));
            } else {
                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + 1);
            }
            scrolled = true;
        }
        if (positionByte <= scrollPosition.getScrollCharPosition()) {
            scrollPosition.setScrollCharPosition(positionByte);
            scrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else if (positionByte >= scrollPosition.getScrollCharPosition() + bytesPerRect) {
            scrollPosition.setScrollCharPosition(positionByte - bytesPerRect);
            if (horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
                scrollPosition.setScrollCharOffset(getCharacterWidth() - (hexRect.width % getCharacterWidth()));
            } else {
                scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() + 1);
            }
            scrolled = true;
        }

        if (scrolled) {
            updateScrollBars();
            notifyScrolled();
        }
    }

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param bytesPerLine bytes per line
     * @param lineHeight line height
     * @param charWidth character width
     * @param linesPerRect lines per visible rectangle
     * @return cursor position or null
     */
    @Override
    @Nullable
    public Point getCursorPoint(int bytesPerLine, int lineHeight, int charWidth, int linesPerRect) {
        CaretPosition caretPosition = getCaretPosition();
        long shiftedPosition = caretPosition.getDataPosition() + scrollPosition.getLineDataOffset();
        long line = shiftedPosition / bytesPerLine - scrollPosition.getScrollLinePosition();
        if (line < -1 || line > linesPerRect) {
            return null;
        }

        int byteOffset = (int) (shiftedPosition % bytesPerLine);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + line * lineHeight) - scrollPosition.getScrollLineOffset();
        int caretX;
        if (caretPosition.getSection() == CodeAreaSection.TEXT_PREVIEW) {
            caretX = codeArea.getPreviewX() + charWidth * byteOffset;
        } else {
            caretX = dataViewRect.x + charWidth * (codeArea.getPainter().computeFirstCodeCharPos(byteOffset) + caretPosition.getCodeOffset());
        }
        caretX -= scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    /**
     * Returns relative shadow cursor position in code area or null if cursor is
     * not visible.
     *
     * @param bytesPerLine bytes per line
     * @param lineHeight line height
     * @param charWidth character width
     * @param linesPerRect lines per visible rectangle
     * @return cursor position or null
     */
    @Override
    @Nullable
    public Point getShadowCursorPoint(int bytesPerLine, int lineHeight, int charWidth, int linesPerRect) {
        CaretPosition caretPosition = getCaretPosition();
        long shiftedPosition = caretPosition.getDataPosition() + scrollPosition.getLineDataOffset();
        long line = shiftedPosition / bytesPerLine - scrollPosition.getScrollLinePosition();
        if (line < -1 || line + 1 > linesPerRect) {
            return null;
        }

        int byteOffset = (int) (shiftedPosition % bytesPerLine);

        Rectangle dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + line * lineHeight) - scrollPosition.getScrollLineOffset();
        int caretX;
        if (caretPosition.getSection() == CodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.x + charWidth * codeArea.getPainter().computeFirstCodeCharPos(byteOffset);
        } else {
            caretX = codeArea.getPreviewX() + charWidth * byteOffset;
        }
        caretX -= scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    @Override
    public void updateScrollBars() {
        if (scrollPosition.getVerticalOverflowMode() == CodeAreaScrollPosition.VerticalOverflowMode.OVERFLOW) {
            long lines = ((codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / getBytesPerLine()) + 1;
            int scrollValue;
            if (scrollPosition.getScrollCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                scrollValue = (int) ((scrollPosition.getScrollLinePosition() * Integer.MAX_VALUE) / lines);
            } else {
                scrollValue = (int) (scrollPosition.getScrollLinePosition() / (lines / Integer.MAX_VALUE));
            }
            scrollPanel.getVerticalScrollBar().setValue(scrollValue);
        } else if (verticalScrollUnit == VerticalScrollUnit.LINE) {
            scrollPanel.getVerticalScrollBar().setValue((int) scrollPosition.getScrollLinePosition());
        } else {
            scrollPanel.getVerticalScrollBar().setValue((int) (scrollPosition.getScrollLinePosition() * getLineHeight() + scrollPosition.getScrollLineOffset()));
        }

        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition());
        } else {
            scrollPanel.getHorizontalScrollBar().setValue(scrollPosition.getScrollCharPosition() * getCharacterWidth() + scrollPosition.getScrollCharOffset());
        }
        codeArea.repaint();
    }

    boolean isHorizontalScrollBarVisible() {
        // TODO
        return scrollPanel.getHorizontalScrollBar().isVisible();
    }

    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        codeArea.resetPainter();
        updateScrollBars();
    }

    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit) {
        this.verticalScrollUnit = verticalScrollUnit;
        long linePosition = scrollPosition.getScrollLinePosition();
        if (verticalScrollUnit == VerticalScrollUnit.LINE) {
            scrollPosition.setScrollLineOffset(0);
        }
        codeArea.resetPainter();
        scrollPosition.setScrollLinePosition(linePosition);
        updateScrollBars();
        notifyScrolled();
    }

    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        codeArea.resetPainter();
        updateScrollBars();
    }

    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int bytePosition = scrollPosition.getScrollCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setScrollCharOffset(0);
        }
        codeArea.resetPainter();
        scrollPosition.setScrollCharPosition(bytePosition);
        updateScrollBars();
        notifyScrolled();
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
            if (scrollPosition.getVerticalOverflowMode() == CodeAreaScrollPosition.VerticalOverflowMode.OVERFLOW) {
                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
                long lines = ((codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / getBytesPerLine()) - getLinesPerRectangle() + 1;
                long targetLine;
                if (scrollBarValue > 0 && lines > maxValue / scrollBarValue) {
                    targetLine = scrollBarValue * (lines / maxValue);
                    long rest = lines % maxValue;
                    targetLine += (rest * scrollBarValue) / maxValue;
                } else {
                    targetLine = (scrollBarValue * lines) / Integer.MAX_VALUE;
                }
                scrollPosition.setScrollLinePosition(targetLine);
                if (verticalScrollUnit != VerticalScrollUnit.LINE) {
                    scrollPosition.setScrollLineOffset(0);
                }
            } else if (verticalScrollUnit == VerticalScrollUnit.LINE) {
                scrollPosition.setScrollLinePosition(scrollBarValue);
            } else {
                scrollPosition.setScrollLinePosition(scrollBarValue / getLineHeight());
                scrollPosition.setScrollLineOffset(scrollBarValue % getLineHeight());
            }

            // TODO
            codeArea.repaint();
//            dataViewScrolled(codeArea.getGraphics());
            notifyScrolled();
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                scrollPosition.setScrollCharPosition(scrollPanel.getHorizontalScrollBar().getValue());
            } else {
                int characterWidth = getCharacterWidth();
                if (characterWidth > 0) {
                    int horizontalScroll = scrollPanel.getHorizontalScrollBar().getValue();
                    scrollPosition.setScrollCharPosition(horizontalScroll / characterWidth);
                    scrollPosition.setScrollCharOffset(horizontalScroll % characterWidth);
                }
            }

            codeArea.repaint();
            dataViewScrolled(codeArea.getGraphics());
            notifyScrolled();
        }
    }

    /**
     * Returns rectangle of the data view area.
     *
     * @return rectangle
     */
    public Rectangle getDataViewRectangle() {
        return dataView.getBounds();
    }

    private int getLineNumberLength() {
        return 8;
    }

    public SelectionRange getSelection() {
        return selection;
    }

    public void setSelection(SelectionRange selection) {
        this.selection = selection;
        notifySelectionChanged();
    }

    /**
     * Returns currently used charset.
     *
     * @return charset
     */
    @Nonnull
    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(@Nonnull Charset charset) {
        if (charset == null) {
            throw new NullPointerException("Charset cannot be null");
        }

        this.charset = charset;
        repaint();
    }

    @Nonnull
    @Override
    public EditationMode getEditationMode() {
        return editationMode;
    }

    @Override
    public boolean isEditable() {
        return editationMode != EditationMode.READ_ONLY;
    }

    @Override
    public void setEditationMode(@Nonnull EditationMode editationMode) {
        boolean changed = editationMode != this.editationMode;
        this.editationMode = editationMode;
        if (changed) {
            for (EditationModeChangedListener listener : editationModeChangedListeners) {
                listener.editationModeChanged(editationMode);
            }
            caret.resetBlink();
            repaint();
        }
    }

    public boolean isHandleClipboard() {
        return handleClipboard;
    }

    public void setHandleClipboard(boolean handleClipboard) {
        this.handleClipboard = handleClipboard;
    }

    public void notifySelectionChanged() {
        for (SelectionChangedListener selectionChangedListener : selectionChangedListeners) {
            selectionChangedListener.selectionChanged(selection);
        }
    }

    public void addSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    public void removeSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    @Override
    public void notifyCaretMoved() {
        for (CaretMovedListener caretMovedListener : caretMovedListeners) {
            caretMovedListener.caretMoved(caret.getCaretPosition());
        }
    }

    public void notifyScrolled() {
        for (ScrollingListener scrollingListener : scrollingListeners) {
            scrollingListener.scrolled();
        }
    }

    public void addCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    public void removeCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    public void addScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    public void removeScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    public void addEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.add(editationModeChangedListener);
    }

    public void removeEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.remove(editationModeChangedListener);
    }

    private static class Colors {

        Color foreground;
        Color background;
        Color selectionForeground;
        Color selectionBackground;
        Color selectionMirrorForeground;
        Color selectionMirrorBackground;
        Color cursor;
        Color negativeCursor;
        Color cursorMirror;
        Color negativeCursorMirror;
        Color decorationLine;
        Color stripes;
    }
}
