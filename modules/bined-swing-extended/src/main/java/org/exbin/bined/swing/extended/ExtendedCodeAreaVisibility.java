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

import org.exbin.bined.extended.ExtendedCodeAreaStructure;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.swing.basic.BasicCodeAreaMetrics;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.extended.layout.PositionIterator;

/**
 * Basic code area component characters visibility in scroll window.
 *
 * @version 0.2.0 2019/02/17
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaVisibility {

    private int splitLinePos;
    private int visibleHalfCharStart;
    private int visibleHalfCharEnd;
    private int visibleMatrixHalfCharEnd;
    private int visiblePreviewStart;
    private int visiblePreviewEnd;
    private int visibleCodeStart;
    private int visibleCodeEnd;
    private int visibleMatrixCodeEnd;

    public void recomputeCharPositions(BasicCodeAreaMetrics metrics, ExtendedCodeAreaStructure structure, ExtendedCodeAreaDimensions dimensions, ExtendedCodeAreaLayoutProfile layout, CodeAreaScrollPosition scrollPosition) {
        int dataViewWidth = dimensions.getDataViewWidth();
        int previewCharPos = structure.getPreviewHalfCharPos();
        int characterWidth = metrics.getCharacterWidth();
        CodeAreaViewMode viewMode = structure.getViewMode();
        splitLinePos = computeSplitLinePos(viewMode, characterWidth, structure, layout);
        int halfCharsPerCodeSection = structure.getHalfCharsPerCodeSection();
        int bytesPerRow = structure.getBytesPerRow();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            visibleHalfCharStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleHalfCharStart < 0) {
                visibleHalfCharStart = 0;
            }
            visibleHalfCharEnd = ((scrollPosition.getCharPosition() + dimensions.getCharactersPerRect()) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleHalfCharEnd > structure.getHalfCharsPerRow()) {
                visibleHalfCharEnd = structure.getHalfCharsPerRow();
            }
            visibleMatrixHalfCharEnd = (dataViewWidth + (scrollPosition.getCharPosition() + halfCharsPerCodeSection) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleMatrixHalfCharEnd > halfCharsPerCodeSection) {
                visibleMatrixHalfCharEnd = halfCharsPerCodeSection;
            }
            visibleCodeStart = structure.computePositionByte(visibleHalfCharStart);
            visibleCodeEnd = structure.computePositionByte(visibleHalfCharEnd - 1) + 1;
            if (visibleCodeEnd > bytesPerRow) {
                visibleCodeEnd = bytesPerRow;
            }

            visibleMatrixCodeEnd = structure.computePositionByte(visibleMatrixHalfCharEnd - 1) + 1;
            if (visibleMatrixCodeEnd > bytesPerRow) {
                visibleMatrixCodeEnd = bytesPerRow;
            }
        } else {
            visibleHalfCharStart = 0;
            visibleHalfCharEnd = -1;
            visibleCodeStart = 0;
            visibleCodeEnd = -1;
            visibleMatrixCodeEnd = -1;
        }

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            visiblePreviewStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewStart < 0) {
                visiblePreviewStart = 0;
            }
            if (visibleCodeEnd < 0) {
                visibleHalfCharStart = visiblePreviewStart + previewCharPos;
            }
            visiblePreviewEnd = (dataViewWidth + (scrollPosition.getCharPosition() + 1) * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewEnd > bytesPerRow) {
                visiblePreviewEnd = bytesPerRow;
            }
            if (visiblePreviewEnd >= 0) {
                visibleHalfCharEnd = visiblePreviewEnd + previewCharPos;
            }
        } else {
            visiblePreviewStart = 0;
            visiblePreviewEnd = -1;
        }
    }

    private int computeSplitLinePos(CodeAreaViewMode viewMode, int characterWidth, ExtendedCodeAreaStructure structure, ExtendedCodeAreaLayoutProfile layout) {
        int linePos = 0;
        if (viewMode == CodeAreaViewMode.DUAL) {
            PositionIterator positionIterator = layout.createPositionIterator(structure.getCodeType(), structure.getViewMode(), structure.getBytesPerRow());
            int halfCharPos = 0;
            while (!positionIterator.isEndReached()) {
                int nextSpaceSize = positionIterator.nextSpaceType().getHalfCharSize();
                if (positionIterator.getBytePosition() == 0 && positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    linePos = layout.computePositionX(halfCharPos + 2, characterWidth, characterWidth / 2)
                            + layout.computePositionX(nextSpaceSize, characterWidth, characterWidth / 2) / 2;
                    break;
                }
                halfCharPos += 2 + nextSpaceSize;
            }
        }

        return linePos;
    }

    /**
     * Returns pixel position of slit line relative to data view or 0 if not in
     * use.
     *
     * @return x-position or 0
     */
    public int getSplitLinePos() {
        return splitLinePos;
    }

    public int getVisibleHalfCharStart() {
        return visibleHalfCharStart;
    }

    public int getVisibleHalfCharEnd() {
        return visibleHalfCharEnd;
    }

    public int getVisibleMatrixHalfCharEnd() {
        return visibleMatrixHalfCharEnd;
    }

    public int getVisiblePreviewStart() {
        return visiblePreviewStart;
    }

    public int getVisiblePreviewEnd() {
        return visiblePreviewEnd;
    }

    public int getVisibleCodeStart() {
        return visibleCodeStart;
    }

    public int getVisibleCodeEnd() {
        return visibleCodeEnd;
    }

    public int getVisibleMatrixCodeEnd() {
        return visibleMatrixCodeEnd;
    }
}
