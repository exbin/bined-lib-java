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

import java.awt.Rectangle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.swing.basic.BasicCodeAreaMetrics;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2019/08/05
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaDimensions {

    private int dataViewX;
    private int dataViewY;
    private int verticalScrollBarSize;
    private int horizontalScrollBarSize;
    private int scrollPanelWidth;
    private int scrollPanelHeight;
    private int dataViewWidth;
    private int dataViewHeight;
    private int halfCharOffset;
    private int rowOffset;

    private int headerAreaHeight;
    private int rowPositionAreaWidth;
    private int rowsPerRect;
    private int rowsPerPage;
    private int halfCharsPerPage;
    private int halfCharsPerRect;

    @Nullable
    private ExtendedCodeAreaLayoutProfile layoutProfile;
    @Nonnull
    private final Rectangle componentRect = new Rectangle();
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

    public void recomputeSizes(BasicCodeAreaMetrics metrics, int componentX, int componentY, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize, ExtendedCodeAreaLayoutProfile layoutProfile) {
        componentRect.setBounds(componentX, componentY, componentWidth, componentHeight);
        this.layoutProfile = layoutProfile;
        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        headerAreaHeight = layoutProfile.computeHeaderAreaHeight(metrics.getFontHeight());
        rowPositionAreaWidth = layoutProfile.computeRowPositionAreaWidth(metrics.getCharacterWidth(), rowPositionLength);

        dataViewX = componentX + rowPositionAreaWidth;
        dataViewY = componentY + headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - verticalScrollBarSize;
        dataViewHeight = scrollPanelHeight - horizontalScrollBarSize;
        halfCharsPerRect = computeHalfCharsPerRectangle(metrics);
        halfCharsPerPage = computeHalfCharsPerPage(metrics);
        rowsPerRect = computeRowsPerRectangle(metrics);
        rowsPerPage = computeRowsPerPage(metrics);
        halfCharOffset = metrics.isInitialized() ? dataViewWidth % halfSpaceWidth : 0;
        rowOffset = metrics.isInitialized() ? dataViewHeight % metrics.getRowHeight() : 0;

        boolean availableWidth = rowPositionAreaWidth + verticalScrollBarSize <= componentWidth;
        boolean availableHeight = dataViewY + horizontalScrollBarSize <= componentHeight;

        if (availableWidth && availableHeight) {
            mainAreaRect.setBounds(componentX + rowPositionAreaWidth, dataViewY, componentWidth - rowPositionAreaWidth - verticalScrollBarSize, componentHeight - dataViewY - horizontalScrollBarSize);
        } else {
            mainAreaRect.setBounds(0, 0, 0, 0);
        }
        if (availableWidth) {
            headerAreaRectangle.setBounds(componentX + rowPositionAreaWidth, componentY, componentWidth - rowPositionAreaWidth - verticalScrollBarSize, headerAreaHeight);
        } else {
            headerAreaRectangle.setBounds(0, 0, 0, 0);
        }
        if (availableHeight) {
            rowPositionAreaRectangle.setBounds(componentX, dataViewY, rowPositionAreaWidth, componentHeight - dataViewY - horizontalScrollBarSize);
        } else {
            rowPositionAreaRectangle.setBounds(0, 0, 0, 0);
        }

        scrollPanelRectangle.setBounds(dataViewX, dataViewY, scrollPanelWidth, scrollPanelHeight);
        dataViewRectangle.setBounds(dataViewX, dataViewY, dataViewWidth >= 0 ? dataViewWidth : 0, dataViewHeight >= 0 ? dataViewHeight : 0);
    }

    @Nonnull
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

    private int computeHalfCharsPerRectangle(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        if (characterWidth == 0) {
            return 0;
        }
        int width = (dataViewWidth + halfSpaceWidth - 1);
        int halfChars = (width / characterWidth) * 2;
        if (width % characterWidth >= halfSpaceWidth) {
            halfChars++;
        }

        return halfChars;
    }

    private int computeHalfCharsPerPage(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        if (characterWidth == 0) {
            return 0;
        }
        int halfChars = (dataViewWidth / characterWidth) * 2;
        if (dataViewWidth % characterWidth >= halfSpaceWidth) {
            halfChars++;
        }

        return halfChars;
    }

    private int computeRowsPerRectangle(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
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

    public int getHalfCharsPerRect() {
        return halfCharsPerRect;
    }

    public int getHalfCharsPerPage() {
        return halfCharsPerPage;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getHalfCharOffset() {
        return halfCharOffset;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    @Nonnull
    public ExtendedCodeAreaLayoutProfile getLayoutProfile() {
        CodeAreaUtils.requireNonNull(layoutProfile);
        return layoutProfile;
    }

    @Nonnull
    public Rectangle getComponentRect() {
        return componentRect;
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

    @Nonnull
    public Rectangle getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    @Nonnull
    public Rectangle getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }
}
