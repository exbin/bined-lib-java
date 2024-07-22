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
package org.exbin.bined.swing.section.caret;

import java.awt.Graphics;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.section.caret.CodeAreaCaretShape;
import org.exbin.bined.section.caret.CodeAreaCaretType;
import org.exbin.bined.section.caret.DefaultCodeAreaCaretShape;

/**
 * Support for cursor carets shapes.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditableExtendedCodeAreaCaretsProfile implements SectionCodeAreaCaretsProfile {

    @Nonnull
    @Override
    public CodeAreaCaretShape identifyCaretShape(CodeAreaCaretType caretType) {
        switch (caretType) {
            case INSERT: {
                return DefaultCodeAreaCaretShape.LINE;
            }
            case OVERWRITE: {
                return DefaultCodeAreaCaretShape.FULL_BOX;
            }
            case SHADOW: {
                return DefaultCodeAreaCaretShape.DOTTED_BOX;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(caretType);
        }
    }

    @Override
    public void paintCaret(Graphics g, int cursorX, int cursorY, int width, int height, CodeAreaCaretShape codeAreaCaretShape) {
        g.fillRect(cursorX, cursorY, width, height);

        if (codeAreaCaretShape instanceof DefaultCodeAreaCaretShape) {
            switch ((DefaultCodeAreaCaretShape) codeAreaCaretShape) {
                case FULL_BOX: {
                    break;
                }
                case LINE: {
                    break;
                }
                case DOTTED_BOX: {
                    break;
                }
            }
        }
    }

//    @Override
//    public void paintCursor(Graphics g) {
//        if (!codeArea.hasFocus()) {
//            return;
//        }
//
//        CodeAreaCaret caret = codeArea.getCaret();
//        int bytesPerLine = codeArea.getBytesPerLine();
//        int lineHeight = codeArea.getLineHeight();
//        int charWidth = codeArea.getCharWidth();
//        int linesPerRect = codeArea.getLinesPerRect();
//        int codeDigits = codeArea.getCodeType().getMaxDigits();
//        Point cursorPoint = caret.getCursorPoint(bytesPerLine, lineHeight, charWidth, linesPerRect);
//        boolean cursorVisible = caret.isCursorVisible();
//        CodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
//
//        if (cursorVisible && cursorPoint != null) {
//            g.setColor(codeArea.getCursorColor());
//            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
//                g.setXORMode(Color.WHITE);
//            }
//
//            CodeAreaCaret.CursorShape cursorShape = codeArea.getEditMode() == EditMode.INSERT ? caret.getInsertCursorShape() : caret.getOverwriteCursorShape();
//            int cursorThickness = 0;
//            if (cursorShape.getWidth() != CodeAreaCaret.CursorShapeWidth.FULL) {
//                cursorThickness = caret.getCursorThickness(cursorShape, charWidth, lineHeight);
//            }
//            switch (cursorShape) {
//                case LINE_TOP:
//                case DOUBLE_TOP:
//                case QUARTER_TOP:
//                case HALF_TOP: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
//                            charWidth, cursorThickness, renderingMode);
//                    break;
//                }
//                case LINE_BOTTOM:
//                case DOUBLE_BOTTOM:
//                case QUARTER_BOTTOM:
//                case HALF_BOTTOM: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y + lineHeight - cursorThickness,
//                            charWidth, cursorThickness, renderingMode);
//                    break;
//                }
//                case LINE_LEFT:
//                case DOUBLE_LEFT:
//                case QUARTER_LEFT:
//                case HALF_LEFT: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
//                    break;
//                }
//                case LINE_RIGHT:
//                case DOUBLE_RIGHT:
//                case QUARTER_RIGHT:
//                case HALF_RIGHT: {
//                    paintCursorRect(g, cursorPoint.x + charWidth - cursorThickness, cursorPoint.y, cursorThickness, lineHeight, renderingMode);
//                    break;
//                }
//                case BOX: {
//                    paintCursorRect(g, cursorPoint.x, cursorPoint.y,
//                            charWidth, lineHeight, renderingMode);
//                    break;
//                }
//                case FRAME: {
//                    g.drawRect(cursorPoint.x, cursorPoint.y, charWidth, lineHeight - 1);
//                    break;
//                }
//                case BOTTOM_CORNERS:
//                case CORNERS: {
//                    int quarterWidth = charWidth / 4;
//                    int quarterLine = lineHeight / 4;
//                    if (cursorShape == CodeAreaCaret.CursorShape.CORNERS) {
//                        g.drawLine(cursorPoint.x, cursorPoint.y,
//                                cursorPoint.x + quarterWidth, cursorPoint.y);
//                        g.drawLine(cursorPoint.x + charWidth - quarterWidth, cursorPoint.y,
//                                cursorPoint.x + charWidth, cursorPoint.y);
//
//                        g.drawLine(cursorPoint.x, cursorPoint.y + 1,
//                                cursorPoint.x, cursorPoint.y + quarterLine);
//                        g.drawLine(cursorPoint.x + charWidth, cursorPoint.y + 1,
//                                cursorPoint.x + charWidth, cursorPoint.y + quarterLine);
//                    }
//
//                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - quarterLine - 1,
//                            cursorPoint.x, cursorPoint.y + lineHeight - 2);
//                    g.drawLine(cursorPoint.x + charWidth, cursorPoint.y + lineHeight - quarterLine - 1,
//                            cursorPoint.x + charWidth, cursorPoint.y + lineHeight - 2);
//
//                    g.drawLine(cursorPoint.x, cursorPoint.y + lineHeight - 1,
//                            cursorPoint.x + quarterWidth, cursorPoint.y + lineHeight - 1);
//                    g.drawLine(cursorPoint.x + charWidth - quarterWidth, cursorPoint.y + lineHeight - 1,
//                            cursorPoint.x + charWidth, cursorPoint.y + lineHeight - 1);
//                    break;
//                }
//                default: {
//                    throw CodeAreaUtils.getInvalidTypeException(cursorShape);
//                }
//            }
//
//            if (renderingMode == CodeAreaCaret.CursorRenderingMode.XOR) {
//                g.setPaintMode();
//            }
//        }
//
//        // Paint shadow cursor
//        if (codeArea.getViewMode() == ViewMode.DUAL && codeArea.isShowShadowCursor()) {
//            g.setColor(codeArea.getCursorColor());
//            Point shadowCursorPoint = caret.getShadowCursorPoint(bytesPerLine, lineHeight, charWidth, linesPerRect);
//            if (shadowCursorPoint != null) {
//                Graphics2D g2d = (Graphics2D) g.create();
//                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
//                g2d.setStroke(dashed);
//                g2d.drawRect(shadowCursorPoint.x, shadowCursorPoint.y,
//                        charWidth * (codeArea.getActiveSection() == Section.TEXT_PREVIEW ? codeDigits : 1), lineHeight - 1);
//                g2d.dispose();
//            }
//        }
//    }
//
//    private void paintCursorRect(Graphics g, int x, int y, int width, int height, CodeAreaCaret.CursorRenderingMode renderingMode) {
//        switch (renderingMode) {
//            case PAINT: {
//                g.fillRect(x, y, width, height);
//                break;
//            }
//            case XOR: {
//                Rectangle rect = new Rectangle(x, y, width, height);
//                Rectangle intersection = rect.intersection(g.getClipBounds());
//                if (!intersection.isEmpty()) {
//                    g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
//                }
//                break;
//            }
//            case NEGATIVE: {
//                Rectangle rect = new Rectangle(x, y, width, height);
//                Rectangle intersection = rect.intersection(g.getClipBounds());
//                if (intersection.isEmpty()) {
//                    break;
//                }
//                Shape clip = g.getClip();
//                g.setClip(intersection.x, intersection.y, intersection.width, intersection.height);
//                CodeArea.ScrollPosition scrollPosition = codeArea.getScrollPosition();
//                g.fillRect(x, y, width, height);
//                g.setColor(codeArea.getNegativeCursorColor());
//                Rectangle codeRect = codeArea.getCodeSectionRectangle();
//                int previewX = codeArea.getPreviewX();
//                int charWidth = codeArea.getCharWidth();
//                int lineHeight = codeArea.getLineHeight();
//                int line = (y + scrollPosition.getScrollLineOffset() - codeRect.y) / lineHeight;
//                int scrolledX = x + scrollPosition.getScrollCharPosition() * charWidth + scrollPosition.getScrollCharOffset();
//                int posY = codeRect.y + (line + 1) * lineHeight - codeArea.getSubFontSpace() - scrollPosition.getScrollLineOffset();
//                if (codeArea.getViewMode() != ViewMode.CODE_MATRIX && scrolledX >= previewX) {
//                    int charPos = (scrolledX - previewX) / charWidth;
//                    long dataSize = codeArea.getDataSize();
//                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + charPos - scrollPosition.getLineByteShift();
//                    if (dataPosition >= dataSize) {
//                        g.setClip(clip);
//                        break;
//                    }
//
//                    char[] previewChars = new char[1];
//                    Charset charset = codeArea.getCharset();
//                    CharsetEncoder encoder = charset.newEncoder();
//                    int maxCharLength = (int) encoder.maxBytesPerChar();
//                    byte[] data = new byte[maxCharLength];
//
//                    if (maxCharLength > 1) {
//                        int charDataLength = maxCharLength;
//                        if (dataPosition + maxCharLength > dataSize) {
//                            charDataLength = (int) (dataSize - dataPosition);
//                        }
//
//                        codeArea.getData().copyToArray(dataPosition, data, 0, charDataLength);
//                        String displayString = new String(data, 0, charDataLength, charset);
//                        if (!displayString.isEmpty()) {
//                            previewChars[0] = displayString.charAt(0);
//                        }
//                    } else {
//                        if (charMappingCharset == null || charMappingCharset != charset) {
//                            buildCharMapping(charset);
//                        }
//
//                        previewChars[0] = charMapping[codeArea.getData().getByte(dataPosition) & 0xFF];
//                    }
//
//                    if (codeArea.isShowUnprintableCharacters()) {
//                        if (unprintableCharactersMapping == null) {
//                            buildUnprintableCharactersMapping();
//                        }
//                        Character replacement = unprintableCharactersMapping.get(previewChars[0]);
//                        if (replacement != null) {
//                            previewChars[0] = replacement;
//                        }
//                    }
//                    int posX = previewX + charPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
//                    if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                        g.drawChars(previewChars, 0, 1, posX, posY);
//                    } else {
//                        drawCenteredChar(g, previewChars, 0, charWidth, posX, posY);
//                    }
//                } else {
//                    int charPos = (scrolledX - codeRect.x) / charWidth;
//                    int byteOffset = codeArea.computeByteOffsetPerCodeCharOffset(charPos);
//                    int codeCharPos = codeArea.computeByteCharPos(byteOffset);
//                    char[] lineChars = new char[codeArea.getCodeType().getMaxDigits()];
//                    long dataSize = codeArea.getDataSize();
//                    long dataPosition = (line + scrollPosition.getScrollLinePosition()) * codeArea.getBytesPerLine() + byteOffset - scrollPosition.getLineByteShift();
//                    if (dataPosition >= dataSize) {
//                        g.setClip(clip);
//                        break;
//                    }
//
//                    byte dataByte = codeArea.getData().getByte(dataPosition);
//                    CodeAreaUtils.byteToCharsCode(dataByte, codeArea.getCodeType(), lineChars, 0, codeArea.getHexCharactersCase());
//                    int posX = codeRect.x + codeCharPos * charWidth - scrollPosition.getScrollCharPosition() * charWidth - scrollPosition.getScrollCharOffset();
//                    int charsOffset = charPos - codeCharPos;
//                    if (codeArea.getCharRenderingMode() == CodeArea.CharRenderingMode.LINE_AT_ONCE) {
//                        g.drawChars(lineChars, charsOffset, 1, posX + (charsOffset * charWidth), posY);
//                    } else {
//                        drawCenteredChar(g, lineChars, charsOffset, charWidth, posX + (charsOffset * charWidth), posY);
//                    }
//                }
//                g.setClip(clip);
//                break;
//            }
//        }
//    }
}
