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
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.swing.basic.BasicCodeAreaMetrics;

/**
 * Basic code area component characters visibility in scroll window.
 *
 * @version 0.2.0 2019/02/03
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaVisibility {

    private int previewRelativeX;
    private int visibleHalfCharStart;
    private int visibleHalfCharEnd;
    private int visibleMatrixHalfCharEnd;
    private int visiblePreviewHalfCharStart;
    private int visiblePreviewHalfCharEnd;
    private int visibleCodeHalfCharStart;
    private int visibleCodeHalfCharEnd;
    private int visibleMatrixCodeHalfCharEnd;

    public void recomputeCharPositions(BasicCodeAreaMetrics metrics, ExtendedCodeAreaStructure structure, ExtendedCodeAreaDimensions dimensions, CodeAreaScrollPosition scrollPosition) {
        int dataViewWidth = dimensions.getDataViewWidth();
        int previewCharPos = structure.getPreviewHalfCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int halfCharWidth = characterWidth / 2;
        previewRelativeX = previewCharPos * characterWidth;

        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerCodeSection = structure.getHalfCharsPerCodeSection();
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
            visibleMatrixHalfCharEnd = (dataViewWidth + (scrollPosition.getCharPosition() + charactersPerCodeSection) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleMatrixHalfCharEnd > charactersPerCodeSection) {
                visibleMatrixHalfCharEnd = charactersPerCodeSection;
            }
            visibleCodeHalfCharStart = structure.computePositionByte(visibleHalfCharStart);
            visibleCodeHalfCharEnd = structure.computePositionByte(visibleHalfCharEnd - 1) + 1;
            if (visibleCodeHalfCharEnd > bytesPerRow) {
                visibleCodeHalfCharEnd = bytesPerRow;
            }

            visibleMatrixCodeHalfCharEnd = structure.computePositionByte(visibleMatrixHalfCharEnd - 1) + 1;
            if (visibleMatrixCodeHalfCharEnd > bytesPerRow) {
                visibleMatrixCodeHalfCharEnd = bytesPerRow;
            }
        } else {
            visibleHalfCharStart = 0;
            visibleHalfCharEnd = -1;
            visibleCodeHalfCharStart = 0;
            visibleCodeHalfCharEnd = -1;
            visibleMatrixCodeHalfCharEnd = -1;
        }

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            visiblePreviewHalfCharStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewHalfCharStart < 0) {
                visiblePreviewHalfCharStart = 0;
            }
            if (visibleCodeHalfCharEnd < 0) {
                visibleHalfCharStart = visiblePreviewHalfCharStart + previewCharPos;
            }
            visiblePreviewHalfCharEnd = (dataViewWidth + (scrollPosition.getCharPosition() + 1) * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewHalfCharEnd > bytesPerRow) {
                visiblePreviewHalfCharEnd = bytesPerRow;
            }
            if (visiblePreviewHalfCharEnd >= 0) {
                visibleHalfCharEnd = visiblePreviewHalfCharEnd + previewCharPos;
            }
        } else {
            visiblePreviewHalfCharStart = 0;
            visiblePreviewHalfCharEnd = -1;
        }
    }

    public int getPreviewRelativeX() {
        return previewRelativeX;
    }

    public int getVisibleCharStart() {
        return visibleHalfCharStart;
    }

    public int getVisibleCharEnd() {
        return visibleHalfCharEnd;
    }

    public int getVisibleMatrixCharEnd() {
        return visibleMatrixHalfCharEnd;
    }

    public int getVisiblePreviewStart() {
        return visiblePreviewHalfCharStart;
    }

    public int getVisiblePreviewEnd() {
        return visiblePreviewHalfCharEnd;
    }

    public int getVisibleCodeStart() {
        return visibleCodeHalfCharStart;
    }

    public int getVisibleCodeEnd() {
        return visibleCodeHalfCharEnd;
    }

    public int getVisibleMatrixCodeEnd() {
        return visibleMatrixCodeHalfCharEnd;
    }
}
