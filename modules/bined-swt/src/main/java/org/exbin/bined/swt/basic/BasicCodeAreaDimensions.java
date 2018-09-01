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
package org.exbin.bined.swt.basic;

import javax.annotation.Nonnull;
import org.eclipse.swt.graphics.Rectangle;
import org.exbin.bined.BasicCodeAreaZone;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2017/08/31
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaDimensions {

    private int componentWidth;
    private int componentHeight;
    private int dataViewX;
    private int dataViewY;
    private int verticalScrollBarSize;
    private int horizontalScrollBarSize;
    private int scrollPanelWidth;
    private int scrollPanelHeight;
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
    private final Rectangle mainAreaRect = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle headerAreaRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle rowPositionAreaRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle scrollPanelRectangle = new Rectangle(0, 0, 0, 0);
    @Nonnull
    private final Rectangle dataViewRectangle = new Rectangle(0, 0, 0, 0);

    public void recomputeSizes(@Nonnull BasicCodeAreaMetrics metrics, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        this.componentWidth = componentWidth;
        this.componentHeight = componentHeight;
        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;
        rowPositionAreaWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);
        headerAreaHeight = metrics.getFontHeight() + metrics.getFontHeight() / 4;

        dataViewX = rowPositionAreaWidth;
        dataViewY = headerAreaHeight;
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
        boolean availableHeight = dataViewY + horizontalScrollBarSize <= componentHeight;

        if (availableWidth && availableHeight) {
            modifyRect(mainAreaRect, rowPositionAreaWidth, dataViewY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - dataViewY - getHorizontalScrollBarSize());
        } else {
            modifyRect(mainAreaRect, 0, 0, 0, 0);
        }
        if (availableWidth) {
            modifyRect(headerAreaRectangle, rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        } else {
            modifyRect(headerAreaRectangle, 0, 0, 0, 0);
        }

        if (availableHeight) {
            modifyRect(rowPositionAreaRectangle, 0, dataViewY, rowPositionAreaWidth, componentHeight - dataViewY - getHorizontalScrollBarSize());
        } else {
            modifyRect(rowPositionAreaRectangle, 0, 0, 0, 0);
        }

        modifyRect(scrollPanelRectangle, dataViewX, dataViewY, scrollPanelWidth, scrollPanelHeight);
        modifyRect(dataViewRectangle, dataViewX, dataViewY, dataViewWidth >= 0 ? dataViewWidth : 0, dataViewHeight >= 0 ? dataViewHeight : 0);
    }

    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= dataViewY) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            return BasicCodeAreaZone.ROW_POSITIONS;
        }

        if (positionX >= dataViewX + scrollPanelWidth && positionY < dataViewY + scrollPanelHeight) {
            return BasicCodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= dataViewY + scrollPanelHeight) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.BOTTOM_LEFT_CORNER;
            } else if (positionX >= dataViewX + scrollPanelWidth) {
                return BasicCodeAreaZone.SCROLLBAR_CORNER;
            }

            return BasicCodeAreaZone.HORIZONTAL_SCROLLBAR;
        }

        return BasicCodeAreaZone.CODE_AREA;
    }

    public int getComponentWidth() {
        return componentWidth;
    }

    public int getComponentHeight() {
        return componentHeight;
    }

    public int getDataViewX() {
        return dataViewX;
    }

    public int getDataViewY() {
        return dataViewY;
    }

    public int getVerticalScrollBarSize() {
        return verticalScrollBarSize;
    }

    public int getHorizontalScrollBarSize() {
        return horizontalScrollBarSize;
    }

    public int getScrollPanelWidth() {
        return scrollPanelWidth;
    }

    public int getScrollPanelHeight() {
        return scrollPanelHeight;
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
    public Rectangle getMainAreaRect() {
        return mainAreaRect;
    }

    @Nonnull
    public Rectangle getScrollPanelRectangle() {
        return scrollPanelRectangle;
    }

    @Nonnull
    public Rectangle getDataViewRectangle() {
        return dataViewRectangle;
    }

    public Rectangle getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    public Rectangle getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }

    private int computeCharactersPerRectangle(@Nonnull BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage(@Nonnull BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    private int computeRowsPerRectangle(@Nonnull BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage(@Nonnull BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }

    private static void modifyRect(@Nonnull Rectangle rect, int x, int y, int width, int height) {
        rect.x = x;
        rect.y = y;
        rect.width = width;
        rect.height = height;
    }
}
