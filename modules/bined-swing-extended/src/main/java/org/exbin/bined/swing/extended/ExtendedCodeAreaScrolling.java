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
package org.exbin.bined.swing.extended;

import java.awt.Dimension;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.DataProvider;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.basic.VerticalScrollUnit;
import org.exbin.bined.extended.ExtendedCodeAreaStructure;
import org.exbin.bined.extended.ExtendedHorizontalScrollUnit;
import org.exbin.bined.extended.capability.ExtendedScrollingCapable;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;

/**
 * Code area scrolling for extended core area.
 *
 * @version 0.2.0 2019/03/10
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaScrolling {

    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    private ScrollBarVerticalScale scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
    @Nonnull
    private final Dimension scrollViewDimension = new Dimension();
    private int horizontalExtendDifference;
    private int verticalExtendDifference;
    private int horizontalScrollbarHeight;
    private int verticalScrollbarWidth;

    @Nonnull
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    @Nonnull
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private ExtendedHorizontalScrollUnit horizontalScrollUnit = ExtendedHorizontalScrollUnit.PIXEL;
    @Nonnull
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private final CodeAreaScrollPosition maximumScrollPosition = new CodeAreaScrollPosition();

    public void updateCache(DataProvider codeArea, int horizontalScrollbarHeight, int verticalScrollbarWidth) {
        verticalScrollUnit = ((ExtendedScrollingCapable) codeArea).getVerticalScrollUnit();
        verticalScrollBarVisibility = ((ExtendedScrollingCapable) codeArea).getVerticalScrollBarVisibility();
        horizontalScrollUnit = ((ExtendedScrollingCapable) codeArea).getHorizontalScrollUnit();
        horizontalScrollBarVisibility = ((ExtendedScrollingCapable) codeArea).getHorizontalScrollBarVisibility();
        this.horizontalScrollbarHeight = horizontalScrollbarHeight;
        this.verticalScrollbarWidth = verticalScrollbarWidth;
    }

    @Nonnull
    public Dimension computeViewDimension(int dataViewWidth, int dataViewHeight, ExtendedCodeAreaLayoutProfile layoutProfile, ExtendedCodeAreaStructure structure, int characterWidth, int rowHeight) {
        int halfCharsPerRow = structure.getHalfCharsPerRow();
        int dataWidth = layoutProfile.computePositionX(halfCharsPerRow, characterWidth, characterWidth / 2);
        boolean fitsHorizontally = computeFitsHorizontally(dataViewWidth, dataWidth);

        long rowsPerData = structure.getRowsPerDocument();
        boolean fitsVertically = computeFitsVertically(dataViewHeight, rowsPerData, rowHeight);

        if (!fitsVertically) {
            fitsHorizontally = computeFitsHorizontally(dataViewWidth - verticalScrollbarWidth, dataWidth);
        }
        if (!fitsHorizontally) {
            fitsVertically = computeFitsVertically(dataViewHeight - horizontalScrollbarHeight, rowsPerData, rowHeight);
        }

        if (fitsHorizontally) {
            scrollViewDimension.width = dataWidth;
            verticalExtendDifference = 0;
        } else {
            scrollViewDimension.width = recomputeScrollViewWidth(dataViewWidth, characterWidth, dataWidth, halfCharsPerRow);
        }

        if (fitsVertically) {
            scrollViewDimension.height = (int) (rowsPerData * rowHeight);
            horizontalExtendDifference = 0;
        } else {
            scrollViewDimension.height = recomputeScrollViewHeight(dataViewHeight, rowHeight, rowsPerData);
        }

        return scrollViewDimension;
    }

    private boolean computeFitsHorizontally(int dataViewWidth, int dataWidth) {
        return dataWidth <= dataViewWidth;
    }

    private boolean computeFitsVertically(int dataViewHeight, long rowsPerData, int rowHeight) {
        int availableRows = (dataViewHeight + rowHeight - 1) / rowHeight;
        if (rowsPerData > availableRows) {
            return false;
        }

        return rowsPerData * rowHeight <= dataViewHeight;
    }

    private int recomputeScrollViewWidth(int dataViewWidth, int characterWidth, int dataWidth, int halfCharsPerRow) {
        int scrollViewWidth = 0;
        switch (horizontalScrollUnit) {
            case PIXEL: {
                scrollViewWidth = dataWidth;
                break;
            }
            case CHARACTER: {
                int charsPerDataView = dataViewWidth / characterWidth;
                scrollViewWidth = dataViewWidth + (((halfCharsPerRow + 1) / 2) - charsPerDataView);
                break;
            }
            case HALF_CHARACTER: {
                int halfCharsPerDataView = dataViewWidth / (characterWidth / 2);
                scrollViewWidth = dataViewWidth + (halfCharsPerRow - halfCharsPerDataView);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected scrolling unit " + horizontalScrollUnit);
        }

        return scrollViewWidth;
    }

    private int recomputeScrollViewHeight(int dataViewHeight, int rowHeight, long rowsPerData) {
        int scrollViewHeight = 0;
        switch (verticalScrollUnit) {
            case PIXEL: {
                if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                    scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                    scrollViewHeight = Integer.MAX_VALUE;
                    verticalExtendDifference = 0;
                } else {
                    scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                    scrollViewHeight = (int) (rowsPerData * rowHeight) - dataViewHeight;
                    verticalExtendDifference = 0;
                }
                break;
            }
            case ROW: {
                if (rowsPerData > (Integer.MAX_VALUE - dataViewHeight)) {
                    scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                    scrollViewHeight = Integer.MAX_VALUE;
                    verticalExtendDifference = 0;
                } else {
                    scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                    int rowsPerDataView = dataViewHeight / rowHeight;
                    scrollViewHeight = (int) (dataViewHeight + (rowsPerData - rowsPerDataView));
                    verticalExtendDifference = dataViewHeight - rowsPerDataView;
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected scrolling unit " + verticalScrollUnit);
        }

        return scrollViewHeight;
    }

    public void updateHorizontalScrollBarValue(int scrollBarValue, int characterWidth) {
        if (characterWidth == 0) {
            return;
        }

        horizontalExtendDifference = 0;
        switch (horizontalScrollUnit) {
            case PIXEL: {
                scrollPosition.setCharPosition(scrollBarValue / characterWidth);
                scrollPosition.setCharOffset(scrollBarValue % characterWidth);
                break;
            }
            case CHARACTER: {
                scrollPosition.setCharPosition(scrollBarValue);
                scrollPosition.setCharOffset(0);
                break;
            }
            case HALF_CHARACTER: {
                scrollPosition.setCharPosition(scrollBarValue);
                scrollPosition.setCharOffset(0);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected horizontal scroll unit: " + horizontalScrollUnit.name());
        }
    }

    public void updateVerticalScrollBarValue(int scrollBarValue, int rowHeight, int maxValue, long rowsPerDocumentToLastPage) {
        if (rowHeight == 0) {
            scrollPosition.setRowPosition(0);
            scrollPosition.setRowOffset(0);
            return;
        }

        switch (verticalScrollUnit) {
            case PIXEL: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    long targetRow;
                    if (scrollBarValue > 0 && rowsPerDocumentToLastPage > maxValue / scrollBarValue) {
                        targetRow = scrollBarValue * (rowsPerDocumentToLastPage / maxValue);
                        long rest = rowsPerDocumentToLastPage % maxValue;
                        targetRow += (rest * scrollBarValue) / maxValue;
                    } else {
                        targetRow = (scrollBarValue * rowsPerDocumentToLastPage) / Integer.MAX_VALUE;
                    }
                    scrollPosition.setRowPosition(targetRow);
                    if (verticalScrollUnit != VerticalScrollUnit.ROW) {
                        scrollPosition.setRowOffset(0);
                    }
                    return;
                }

                scrollPosition.setRowPosition(scrollBarValue / rowHeight);
                scrollPosition.setRowOffset(scrollBarValue % rowHeight);
                break;
            }
            case ROW: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    long targetRow;
                    if (scrollBarValue > 0 && rowsPerDocumentToLastPage > maxValue / scrollBarValue) {
                        targetRow = scrollBarValue * (rowsPerDocumentToLastPage / maxValue);
                        long rest = rowsPerDocumentToLastPage % maxValue;
                        targetRow += (rest * scrollBarValue) / maxValue;
                    } else {
                        targetRow = (scrollBarValue * rowsPerDocumentToLastPage) / Integer.MAX_VALUE;
                    }
                    scrollPosition.setRowPosition(targetRow);
                    if (verticalScrollUnit != VerticalScrollUnit.ROW) {
                        scrollPosition.setRowOffset(0);
                    }
                    return;
                }

                int rowPosition = scrollBarValue;
                scrollPosition.setRowPosition(rowPosition);
                scrollPosition.setRowOffset(0);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected vertical scroll unit: " + verticalScrollUnit.name());
        }
    }

    public int getVerticalScrollValue(int rowHeight, long rowsPerDocument) {
        switch (verticalScrollUnit) {
            case PIXEL: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    int scrollValue;
                    if (scrollPosition.getCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                        scrollValue = (int) ((scrollPosition.getRowPosition() * Integer.MAX_VALUE) / rowsPerDocument);
                    } else {
                        scrollValue = (int) (scrollPosition.getRowPosition() / (rowsPerDocument / Integer.MAX_VALUE));
                    }
                    return scrollValue;
                }
                return (int) (scrollPosition.getRowPosition() * rowHeight + scrollPosition.getRowOffset());
            }
            case ROW: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    int scrollValue;
                    if (scrollPosition.getCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                        scrollValue = (int) ((scrollPosition.getRowPosition() * Integer.MAX_VALUE) / rowsPerDocument);
                    } else {
                        scrollValue = (int) (scrollPosition.getRowPosition() / (rowsPerDocument / Integer.MAX_VALUE));
                    }
                    return scrollValue;
                }
                return (int) scrollPosition.getRowPosition();
            }
            default:
                throw new IllegalStateException("Unexpected vertical scroll unit: " + verticalScrollUnit.name());
        }
    }

    public int getHorizontalScrollValue(int characterWidth) {
        switch (horizontalScrollUnit) {
            case CHARACTER:
                return scrollPosition.getCharPosition() * characterWidth;
            case HALF_CHARACTER:
                return (scrollPosition.getCharPosition() >> 1) * characterWidth + (scrollPosition.getCharPosition() & 1) * (characterWidth / 2);
            case PIXEL:
                return scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
            default:
                throw new IllegalStateException("Unexpected horizontal scroll unit: " + horizontalScrollUnit.name());
        }
    }

    @Nonnull
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction, int rowsPerPage, long rowsPerDocument) {
        CodeAreaScrollPosition targetPosition = new CodeAreaScrollPosition();
        targetPosition.setScrollPosition(startPosition);

        switch (direction) {
            case UP: {
                if (startPosition.getRowPosition() == 0) {
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - 1);
                }
                break;
            }
            case DOWN: {
                if (maximumScrollPosition.isRowPositionGreaterThan(startPosition)) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + 1);
                }
                break;
            }
            case LEFT: {
                if (startPosition.getCharPosition() == 0) {
                    targetPosition.setCharOffset(0);
                } else {
                    targetPosition.setCharPosition(startPosition.getCharPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (maximumScrollPosition.isCharPositionGreaterThan(startPosition)) {
                    targetPosition.setCharPosition(startPosition.getCharPosition() + 1);
                }
                break;
            }
            case PAGE_UP: {
                if (startPosition.getRowPosition() < rowsPerPage) {
                    targetPosition.setRowPosition(0);
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - rowsPerPage);
                }
                break;
            }
            case PAGE_DOWN: {
                if (startPosition.getRowPosition() <= rowsPerDocument - rowsPerPage * 2) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + rowsPerPage);
                } else if (rowsPerDocument > rowsPerPage) {
                    targetPosition.setRowPosition(rowsPerDocument - rowsPerPage);
                } else {
                    targetPosition.setRowPosition(0);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected scrolling direction type: " + direction.name());
        }

        return targetPosition;
    }

    @Nonnull
    public PositionScrollVisibility computePositionScrollVisibility(long rowPosition, int charPosition, int bytesPerRow, int rowsPerPage, int halfCharsPerPage, int halfCharOffset, int rowOffset, int characterWidth, int rowHeight) {
        boolean partial = false;

        PositionScrollVisibility topVisibility = checkTopScrollVisibility(rowPosition);
        if (topVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= topVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility bottomVisibility = checkBottomScrollVisibility(rowPosition, rowsPerPage, rowOffset, rowHeight);
        if (bottomVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= bottomVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility leftVisibility = checkLeftScrollVisibility(charPosition, characterWidth);
        if (leftVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= leftVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility rightVisibility = checkRightScrollVisibility(charPosition, halfCharsPerPage, halfCharOffset, characterWidth);
        if (rightVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= rightVisibility == PositionScrollVisibility.PARTIAL;

        return partial ? PositionScrollVisibility.PARTIAL : PositionScrollVisibility.VISIBLE;
    }

    @Nullable
    public CodeAreaScrollPosition computeRevealScrollPosition(long rowPosition, int halfCharsPosition, int bytesPerRow, int rowsPerPage, int halfCharsPerPage, int halfCharOffset, int rowOffset, int characterWidth, int rowHeight) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);

        boolean scrolled = false;
        if (checkBottomScrollVisibility(rowPosition, rowsPerPage, rowOffset, rowHeight) != PositionScrollVisibility.VISIBLE) {
            int bottomRowOffset;
            if (verticalScrollUnit == VerticalScrollUnit.ROW) {
                bottomRowOffset = 0;
            } else {
                if (rowsPerPage == 0) {
                    bottomRowOffset = 0;
                } else {
                    bottomRowOffset = rowHeight - rowOffset;
                }
            }

            long targetRowPosition = rowPosition - rowsPerPage;
            if (verticalScrollUnit == VerticalScrollUnit.ROW && rowOffset > 0) {
                targetRowPosition++;
            }
            targetScrollPosition.setRowPosition(targetRowPosition);
            targetScrollPosition.setRowOffset(bottomRowOffset);
            scrolled = true;
        }

        if (checkTopScrollVisibility(rowPosition) != PositionScrollVisibility.VISIBLE) {
            targetScrollPosition.setRowPosition(rowPosition);
            targetScrollPosition.setRowOffset(0);
            scrolled = true;
        }

        if (checkRightScrollVisibility(halfCharsPosition, halfCharsPerPage, halfCharOffset, characterWidth) != PositionScrollVisibility.VISIBLE) {
            int rightCharOffset;
            if (horizontalScrollUnit != ExtendedHorizontalScrollUnit.PIXEL) {
                rightCharOffset = 0;
            } else {
                if (halfCharsPerPage < 1) {
                    rightCharOffset = 0;
                } else {
                    rightCharOffset = characterWidth - (halfCharOffset % characterWidth);
                }
            }

            // Scroll character right
            setHorizontalScrollPosition(targetScrollPosition, halfCharsPosition - halfCharsPerPage, rightCharOffset, characterWidth);
            scrolled = true;
        }

        if (checkLeftScrollVisibility(halfCharsPosition, characterWidth) != PositionScrollVisibility.VISIBLE) {
            setHorizontalScrollPosition(targetScrollPosition, halfCharsPosition, 0, characterWidth);
            scrolled = true;
        }

        return scrolled ? targetScrollPosition : null;
    }

    @Nonnull
    private PositionScrollVisibility checkTopScrollVisibility(long rowPosition) {
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            return rowPosition < scrollPosition.getRowPosition() ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (rowPosition > scrollPosition.getRowPosition() || (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    private PositionScrollVisibility checkBottomScrollVisibility(long rowPosition, int rowsPerPage, int rowOffset, int rowHeight) {
        int sumOffset = scrollPosition.getRowOffset() + rowOffset;

        long lastFullRow = scrollPosition.getRowPosition() + rowsPerPage;
        if (rowOffset > 0) {
            lastFullRow--;
        }
        if (sumOffset >= rowHeight) {
            lastFullRow++;
        }

        if (rowPosition <= lastFullRow) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != rowHeight && rowPosition == lastFullRow + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    private PositionScrollVisibility checkLeftScrollVisibility(int halfCharsPosition, int characterWidth) {
        int halfCharPos = getHorizontalScrollHalfChar(scrollPosition, characterWidth);
        if (horizontalScrollUnit != ExtendedHorizontalScrollUnit.PIXEL) {
            return halfCharsPosition < halfCharPos ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (halfCharsPosition > halfCharPos || (halfCharsPosition == halfCharPos && scrollPosition.getCharOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (halfCharsPosition == halfCharPos && scrollPosition.getCharOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    private PositionScrollVisibility checkRightScrollVisibility(int halfCharsPosition, int halfCharsPerPage, int halfCharOffset, int characterWidth) {
        int sumOffset = scrollPosition.getCharOffset() + halfCharOffset;

        int lastFullHalfChar = getHorizontalScrollHalfChar(scrollPosition, characterWidth) + halfCharsPerPage;
        if (halfCharOffset > 0) {
            lastFullHalfChar--;
        }
        if (sumOffset >= characterWidth / 2) {
            lastFullHalfChar++;
        }

        if (halfCharsPosition <= lastFullHalfChar) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != characterWidth / 2 && halfCharsPosition == lastFullHalfChar + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    public CodeAreaScrollPosition computeCenterOnScrollPosition(long rowPosition, int halfCharsPosition, int bytesPerRow, int rowsPerRect, int halfCharsPerRect, int dataViewWidth, int rowOffset, int characterWidth, int rowHeight) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);

        long centerRowPosition = rowPosition - rowsPerRect / 2;
// TODO        int rowCorrection = (rowsPerRect & 1) == 0 ? rowHeight : 0;
        int heightDiff = 0; // (rowsPerRect * rowHeight + rowCorrection - dataViewHeight) / 2;
        int targetRowOffset;
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            targetRowOffset = 0;
        } else {
            if (heightDiff > 0) {
                targetRowOffset = heightDiff;
            } else {
                targetRowOffset = 0;
            }
        }
        if (centerRowPosition < 0) {
            centerRowPosition = 0;
            targetRowOffset = 0;
        } else {
            CodeAreaScrollPosition centerPosition = new CodeAreaScrollPosition(centerRowPosition, targetRowOffset, 0, 0);
            if (centerPosition.isRowPositionGreaterThan(maximumScrollPosition)) {
                centerRowPosition = maximumScrollPosition.getRowPosition();
                targetRowOffset = maximumScrollPosition.getRowOffset();
            }
        }

        targetScrollPosition.setRowPosition(centerRowPosition);
        targetScrollPosition.setRowOffset(targetRowOffset);

        int centerHalfCharPosition = halfCharsPosition - halfCharsPerRect / 2;
        int charCorrection = (halfCharsPerRect & 1) == 0 ? rowHeight : 0;
        int widthDiff = (halfCharsPerRect * characterWidth + charCorrection - dataViewWidth) / 2;
        int charOffset;
        if (horizontalScrollUnit != ExtendedHorizontalScrollUnit.PIXEL) {
            charOffset = 0;
        } else {
            if (widthDiff > 0) {
                charOffset = widthDiff;
            } else {
                charOffset = 0;
            }
        }
        if (centerHalfCharPosition < 0) {
            centerHalfCharPosition = 0;
            charOffset = 0;
        } else {
            CodeAreaScrollPosition centerPosition = createScrollPosition(centerHalfCharPosition, charOffset, characterWidth);

            if (centerPosition.isCharPositionGreaterThan(maximumScrollPosition)) {
                centerHalfCharPosition = getHorizontalScrollHalfChar(maximumScrollPosition, characterWidth);
                charOffset = maximumScrollPosition.getCharOffset();
            }
        }
        setHorizontalScrollPosition(scrollPosition, centerHalfCharPosition, charOffset, characterWidth);
        return targetScrollPosition;
    }

    @Nonnull
    public void updateMaximumScrollPosition(long rowsPerDocument, int rowsPerPage, int halfCharsPerRow, int halfCharsPerPage, int halfCharOffset, int rowOffset, int characterWidth) {
        maximumScrollPosition.reset();
        if (rowsPerDocument > rowsPerPage) {
            maximumScrollPosition.setRowPosition(rowsPerDocument - rowsPerPage);
            if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
                maximumScrollPosition.setRowOffset(rowOffset);
            }
        }

        if (halfCharsPerRow > halfCharsPerPage) {
            int halfCharsDifference = halfCharsPerRow - halfCharsPerPage;
            switch (horizontalScrollUnit) {
                case CHARACTER: {
                    maximumScrollPosition.setCharPosition((halfCharsDifference >> 1) + ((halfCharsDifference & 1) == 1 || halfCharOffset > 0 ? 1 : 0));
                }
                case HALF_CHARACTER: {
                    maximumScrollPosition.setCharPosition(halfCharsDifference + ((halfCharOffset > 0) ? 1 : 0));
                }
                case PIXEL: {
                    maximumScrollPosition.setCharPosition(halfCharsDifference >> 1);
                    maximumScrollPosition.setCharOffset(halfCharOffset + (halfCharsDifference & 1) * (characterWidth / 2));
                }
            }
        }
    }

    public int getHorizontalScrollX(int characterWidth) {
        switch (horizontalScrollUnit) {
            case CHARACTER: {
                return scrollPosition.getCharPosition() * characterWidth;
            }
            case HALF_CHARACTER: {
                return (scrollPosition.getCharPosition() >> 1) * characterWidth
                        + (scrollPosition.getCharPosition() & 1) * (characterWidth / 2);
            }
            case PIXEL: {
                return scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
            }
            default:
                throw new IllegalStateException("Unexpected horizontal scrolling unit " + horizontalScrollUnit);
        }
    }

    public int getHorizontalScrollHalfChar(CodeAreaScrollPosition position, int characterWidth) {
        switch (horizontalScrollUnit) {
            case CHARACTER: {
                return position.getCharPosition() * 2;
            }
            case HALF_CHARACTER: {
                return position.getCharPosition();
            }
            case PIXEL: {
                return position.getCharPosition() * 2 + (position.getCharOffset() >= (characterWidth / 2) ? 1 : 0);
            }
            default:
                throw new IllegalStateException("Unexpected horizontal scrolling unit " + horizontalScrollUnit);
        }
    }

    private void setHorizontalScrollPosition(CodeAreaScrollPosition scrollPosition, int halfCharPos, int pixelOffset, int characterWidth) {
        switch (horizontalScrollUnit) {
            case CHARACTER: {
                scrollPosition.setCharPosition(halfCharPos >> 1);
                scrollPosition.setCharOffset(0);
                break;
            }
            case HALF_CHARACTER: {
                scrollPosition.setCharPosition(halfCharPos);
                scrollPosition.setCharOffset(0);
                break;
            }
            case PIXEL: {
                int charPos = halfCharPos >> 1;
                int halfSpaceWidth = characterWidth / 2;
                int offset = 0;
                if ((halfCharPos & 1) != 0 && (halfSpaceWidth + pixelOffset > characterWidth)) {
                    charPos++;
                    offset = halfSpaceWidth + pixelOffset - characterWidth;
                }
                scrollPosition.setCharPosition(charPos);
                scrollPosition.setCharOffset(offset);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected horizontal scrolling unit " + horizontalScrollUnit);
        }
    }

    @Nonnull
    private CodeAreaScrollPosition createScrollPosition(int halfCharPos, int pixelOffset, int characterWidth) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        setHorizontalScrollPosition(targetScrollPosition, halfCharPos, pixelOffset, characterWidth);
        return targetScrollPosition;
    }

    @Nonnull
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(CodeAreaScrollPosition scrollPosition) {
        this.scrollPosition.setScrollPosition(scrollPosition);
    }

    public int getHorizontalExtendDifference() {
        return horizontalExtendDifference;
    }

    public int getVerticalExtendDifference() {
        return verticalExtendDifference;
    }

    @Nonnull
    public ScrollBarVerticalScale getScrollBarVerticalScale() {
        return scrollBarVerticalScale;
    }

    public void setScrollBarVerticalScale(ScrollBarVerticalScale scrollBarVerticalScale) {
        this.scrollBarVerticalScale = scrollBarVerticalScale;
    }

    @Nonnull
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    @Nonnull
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Nonnull
    public ExtendedHorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Nonnull
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Nonnull
    public CodeAreaScrollPosition getMaximumScrollPosition() {
        return maximumScrollPosition;
    }
}
