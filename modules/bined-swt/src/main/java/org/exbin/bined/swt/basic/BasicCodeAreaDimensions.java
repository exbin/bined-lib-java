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
package org.exbin.bined.swt.basic;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.swt.graphics.Rectangle;
import org.exbin.bined.basic.BasicCodeAreaZone;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaDimensions {

    private int scrollPanelX;
    private int scrollPanelY;
    private int scrollPanelWidth;
    private int scrollPanelHeight;
    private int verticalScrollBarSize;
    private int horizontalScrollBarSize;
    private int dataViewWidth;
    private int dataViewHeight;
    private int lastCharOffset;
    private int lastRowOffset;

    private int headerAreaHeight;
    private int rowPositionAreaWidth;
    private int rowsPerRect;
    private int rowsPerPage;
    private int charactersPerPage;
    private int charactersPerRect;

    @Nonnull
    private final Rectangle componentRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle mainAreaRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle headerAreaRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle rowPositionAreaRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle scrollPanelRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle dataViewRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle dataViewInnerRectangle = new Rectangle(0, 0, 0, 0);

    public void recomputeSizes(BasicCodeAreaMetrics metrics, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        modifyRect(componentRectangle, 0, 0, componentWidth, componentHeight);
        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;
        rowPositionAreaWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);
        headerAreaHeight = metrics.getFontHeight() + metrics.getFontHeight() / 4;

        scrollPanelX = rowPositionAreaWidth;
        scrollPanelY = headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - verticalScrollBarSize;
        dataViewHeight = scrollPanelHeight - horizontalScrollBarSize;
        charactersPerRect = computeCharactersPerRectangle(metrics);
        charactersPerPage = computeCharactersPerPage(metrics);
        rowsPerRect = computeRowsPerRectangle(metrics);
        rowsPerPage = computeRowsPerPage(metrics);
        lastCharOffset = metrics.isInitialized() ? dataViewWidth % metrics.getCharacterWidth() : 0;
        lastRowOffset = metrics.isInitialized() ? dataViewHeight % metrics.getRowHeight() : 0;

        boolean availableWidth = rowPositionAreaWidth + verticalScrollBarSize <= componentWidth;
        boolean availableHeight = scrollPanelY + horizontalScrollBarSize <= componentHeight;

        if (availableWidth && availableHeight) {
            modifyRect(mainAreaRectangle, rowPositionAreaWidth, scrollPanelY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - scrollPanelY - getHorizontalScrollBarSize());
        } else {
            modifyRect(mainAreaRectangle, 0, 0, 0, 0);
        }
        if (availableWidth) {
            modifyRect(headerAreaRectangle, rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        } else {
            modifyRect(headerAreaRectangle, 0, 0, 0, 0);
        }

        if (availableHeight) {
            modifyRect(rowPositionAreaRectangle, 0, scrollPanelY, rowPositionAreaWidth, componentHeight - scrollPanelY - getHorizontalScrollBarSize());
        } else {
            modifyRect(rowPositionAreaRectangle, 0, 0, 0, 0);
        }

        modifyRect(scrollPanelRectangle, scrollPanelX, scrollPanelY, scrollPanelWidth, scrollPanelHeight);
        modifyRect(dataViewRectangle, scrollPanelX, scrollPanelY, Math.max(dataViewWidth, 0), Math.max(dataViewHeight, 0));
        modifyRect(dataViewInnerRectangle, 0, 0, Math.max(dataViewWidth, 0), Math.max(dataViewHeight, 0));
    }

    @Nonnull
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= scrollPanelY) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            if (positionY >= scrollPanelY + scrollPanelHeight) {
                return BasicCodeAreaZone.BOTTOM_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.ROW_POSITIONS;
            }
        }

        if (positionX >= scrollPanelX + scrollPanelWidth && positionY < scrollPanelY + scrollPanelHeight) {
            return BasicCodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= scrollPanelY + scrollPanelHeight) {
            if (positionX >= scrollPanelX + scrollPanelWidth) {
                return BasicCodeAreaZone.SCROLLBAR_CORNER;
            } else {
                return BasicCodeAreaZone.HORIZONTAL_SCROLLBAR;
            }
        }

        return BasicCodeAreaZone.CODE_AREA;
    }

    private int computeCharactersPerRectangle(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    private int computeRowsPerRectangle(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }

    public int getScrollPanelX() {
        return scrollPanelX;
    }

    public int getScrollPanelY() {
        return scrollPanelY;
    }

    public int getScrollPanelWidth() {
        return scrollPanelWidth;
    }

    public int getScrollPanelHeight() {
        return scrollPanelHeight;
    }

    public int getVerticalScrollBarSize() {
        return verticalScrollBarSize;
    }

    public int getHorizontalScrollBarSize() {
        return horizontalScrollBarSize;
    }

    public int getDataViewWidth() {
        return dataViewWidth;
    }

    public int getDataViewHeight() {
        return dataViewHeight;
    }

    public int getHeaderAreaHeight() {
        return headerAreaHeight;
    }

    public int getRowPositionAreaWidth() {
        return rowPositionAreaWidth;
    }

    public int getRowsPerRect() {
        return rowsPerRect;
    }

    public int getCharactersPerRect() {
        return charactersPerRect;
    }

    public int getCharactersPerPage() {
        return charactersPerPage;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getLastCharOffset() {
        return lastCharOffset;
    }

    public int getLastRowOffset() {
        return lastRowOffset;
    }

    @Nonnull
    public Rectangle getComponentRectangle() {
        return componentRectangle;
    }

    @Nonnull
    public Rectangle getMainAreaRectangle() {
        return mainAreaRectangle;
    }

    @Nonnull
    public Rectangle getScrollPanelRectangle() {
        return scrollPanelRectangle;
    }

    @Nonnull
    public Rectangle getDataViewRectangle() {
        return dataViewRectangle;
    }

    @Nonnull
    public Rectangle getDataViewInnerRectangle() {
        return dataViewInnerRectangle;
    }

    @Nonnull
    public Rectangle getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    @Nonnull
    public Rectangle getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }

    private static void modifyRect(Rectangle rect, int x, int y, int width, int height) {
        rect.x = x;
        rect.y = y;
        rect.width = width;
        rect.height = height;
    }
}
