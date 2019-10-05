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
package org.exbin.bined.javafx.basic;

import javafx.geometry.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.BasicCodeAreaZone;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2019/08/25
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaDimensions {

    private double scrollPanelX;
    private double scrollPanelY;
    private double scrollPanelWidth;
    private double scrollPanelHeight;
    private double verticalScrollBarSize;
    private double horizontalScrollBarSize;
    private double dataViewWidth;
    private double dataViewHeight;
    private int lastCharOffset;
    private int lastRowOffset;

    private double headerAreaHeight;
    private double rowPositionAreaWidth;
    private int rowsPerRect;
    private int rowsPerPage;
    private int charactersPerPage;
    private int charactersPerRect;

    @Nonnull
    private Rectangle2D componentRectangle = new Rectangle2D(0, 0, 0, 0);
    @Nonnull
    private Rectangle2D mainAreaRectangle = new Rectangle2D(0, 0, 0, 0);
    @Nonnull
    private Rectangle2D headerAreaRectangle = new Rectangle2D(0, 0, 0, 0);
    @Nonnull
    private Rectangle2D rowPositionAreaRectangle = new Rectangle2D(0, 0, 0, 0);
    @Nonnull
    private Rectangle2D scrollPanelRectangle = new Rectangle2D(0, 0, 0, 0);
    @Nonnull
    private Rectangle2D dataViewRectangle = new Rectangle2D(0, 0, 0, 0);
    @Nonnull
    private Rectangle2D dataViewInnerRectangle = new Rectangle2D(0, 0, 0, 0);

    public void recomputeSizes(BasicCodeAreaMetrics metrics, double componentX, double componentY, double componentWidth, double componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        componentRectangle = new Rectangle2D(componentX, componentY, componentWidth, componentHeight);
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
        lastCharOffset = metrics.isInitialized() ? (int) dataViewWidth % metrics.getCharacterWidth() : 0;
        lastRowOffset = metrics.isInitialized() ? (int) dataViewHeight % metrics.getRowHeight() : 0;

        boolean availableWidth = rowPositionAreaWidth + verticalScrollBarSize <= componentWidth;
        boolean availableHeight = scrollPanelY + horizontalScrollBarSize <= componentHeight;

        mainAreaRectangle = availableWidth && availableHeight
                ? new Rectangle2D(rowPositionAreaWidth, scrollPanelY, componentWidth - rowPositionAreaWidth - verticalScrollBarSize, componentHeight - scrollPanelY - horizontalScrollBarSize)
                : new Rectangle2D(0, 0, 0, 0);
        headerAreaRectangle = availableWidth
                ? new Rectangle2D(rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - verticalScrollBarSize, headerAreaHeight)
                : new Rectangle2D(0, 0, 0, 0);
        rowPositionAreaRectangle = availableHeight
                ? new Rectangle2D(0, scrollPanelY, rowPositionAreaWidth, componentHeight - scrollPanelY - horizontalScrollBarSize)
                : new Rectangle2D(0, 0, 0, 0);

        scrollPanelRectangle = new Rectangle2D(scrollPanelX, scrollPanelY, scrollPanelWidth, scrollPanelHeight);
        dataViewRectangle = new Rectangle2D(scrollPanelX, scrollPanelY, dataViewWidth >= 0 ? dataViewWidth : 0, dataViewHeight >= 0 ? dataViewHeight : 0);
        dataViewInnerRectangle = new Rectangle2D(0, 0, dataViewWidth >= 0 ? dataViewWidth : 0, dataViewHeight >= 0 ? dataViewHeight : 0);
    }

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

    public double getScrollPanelX() {
        return scrollPanelX;
    }

    public double getScrollPanelY() {
        return scrollPanelY;
    }

    public double getVerticalScrollBarSize() {
        return verticalScrollBarSize;
    }

    public double getHorizontalScrollBarSize() {
        return horizontalScrollBarSize;
    }

    public double getScrollPanelWidth() {
        return scrollPanelWidth;
    }

    public double getScrollPanelHeight() {
        return scrollPanelHeight;
    }

    public double getDataViewWidth() {
        return dataViewWidth;
    }

    public double getDataViewHeight() {
        return dataViewHeight;
    }

    public double getHeaderAreaHeight() {
        return headerAreaHeight;
    }

    public double getRowPositionAreaWidth() {
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
    public Rectangle2D getComponentRectangle() {
        return componentRectangle;
    }

    @Nonnull
    public Rectangle2D getMainAreaRectangle() {
        return mainAreaRectangle;
    }

    @Nonnull
    public Rectangle2D getScrollPanelRectangle() {
        return scrollPanelRectangle;
    }

    @Nonnull
    public Rectangle2D getDataViewRectangle() {
        return dataViewRectangle;
    }

    @Nonnull
    public Rectangle2D getDataViewInnerRectangle() {
        return dataViewInnerRectangle;
    }

    @Nonnull
    public Rectangle2D getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    @Nonnull
    public Rectangle2D getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }

    private int computeCharactersPerRectangle(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return (int) (characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth);
    }

    private int computeCharactersPerPage(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return (int) (characterWidth == 0 ? 0 : dataViewWidth / characterWidth);
    }

    private int computeRowsPerRectangle(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return (int) (rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight);
    }

    private int computeRowsPerPage(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return (int) (rowHeight == 0 ? 0 : dataViewHeight / rowHeight);
    }
}
