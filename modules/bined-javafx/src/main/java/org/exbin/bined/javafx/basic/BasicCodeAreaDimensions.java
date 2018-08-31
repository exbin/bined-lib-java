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

import java.awt.Rectangle;
import javax.annotation.Nonnull;
import org.exbin.bined.BasicCodeAreaZone;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2018/08/31
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaDimensions {

    private double componentWidth;
    private double componentHeight;
    private double dataViewX;
    private double dataViewY;
    private double verticalScrollBarSize;
    private double horizontalScrollBarSize;
    private double scrollPanelWidth;
    private double scrollPanelHeight;
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
    private final Rectangle mainAreaRect = new Rectangle();
    @Nonnull
    private final Rectangle headerAreaRectangle = new Rectangle();
    @Nonnull
    private final Rectangle rowPositionAreaRectangle = new Rectangle();
    @Nonnull
    private final Rectangle scrollPanelRectangle = new Rectangle();
    @Nonnull
    private final Rectangle dataViewRectangle = new Rectangle();

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
        lastCharOffset = metrics.isInitialized() ? (int) dataViewWidth % metrics.getCharacterWidth() : 0;
        lastRowOffset = metrics.isInitialized() ? (int) dataViewHeight % metrics.getRowHeight() : 0;

        mainAreaRect.setBounds(rowPositionAreaWidth, dataViewY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - dataViewY - getHorizontalScrollBarSize());
        scrollPanelRectangle.setBounds(dataViewX, dataViewY, scrollPanelWidth, scrollPanelHeight);
        dataViewRectangle.setBounds(dataViewX, dataViewY, dataViewWidth, dataViewHeight);
        headerAreaRectangle.setBounds(rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        rowPositionAreaRectangle.setBounds(0, dataViewY, rowPositionAreaWidth, componentHeight - dataViewY - getHorizontalScrollBarSize());
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

    public double getComponentWidth() {
        return componentWidth;
    }

    public double getComponentHeight() {
        return componentHeight;
    }

    public double getDataViewX() {
        return dataViewX;
    }

    public double getDataViewY() {
        return dataViewY;
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
        return (int) (characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth);
    }

    private int computeCharactersPerPage(@Nonnull BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return (int) (characterWidth == 0 ? 0 : dataViewWidth / characterWidth);
    }

    private int computeRowsPerRectangle(@Nonnull BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return (int) (rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight);
    }

    private int computeRowsPerPage(@Nonnull BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return (int) (rowHeight == 0 ? 0 : dataViewHeight / rowHeight);
    }
}
