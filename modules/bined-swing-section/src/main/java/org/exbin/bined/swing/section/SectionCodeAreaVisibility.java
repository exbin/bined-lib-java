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
package org.exbin.bined.swing.section;

import org.exbin.bined.section.SectionCodeAreaStructure;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.swing.basic.BasicCodeAreaMetrics;
import org.exbin.bined.section.layout.PositionIterator;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;

/**
 * Section code area component characters visibility in scroll window.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectionCodeAreaVisibility {

    private int splitLinePos;

    private int skipTo;
    private int skipToChar;
    private int skipRestFrom;
    private int skipRestFromChar;

    private boolean codeSectionVisible;
    private boolean previewSectionVisible;

    public void recomputeCharPositions(BasicCodeAreaMetrics metrics, SectionCodeAreaStructure structure, SectionCodeAreaDimensions dimensions, SectionCodeAreaLayoutProfile layout, SectionCodeAreaScrolling scrolling) {
        int characterWidth = metrics.getCharacterWidth();
        int halfSpaceWidth = characterWidth / 2;
        CodeAreaViewMode viewMode = structure.getViewMode();

        int invisibleFromLeftX = scrolling.getHorizontalScrollX(characterWidth);
        int invisibleFromRightX = invisibleFromLeftX + dimensions.getDataViewWidth();

        skipTo = 0;
        skipToChar = 0;
        skipRestFrom = -1;
        skipRestFromChar = -1;
        codeSectionVisible = viewMode != CodeAreaViewMode.TEXT_PREVIEW;
        previewSectionVisible = viewMode != CodeAreaViewMode.CODE_MATRIX;

        int linePos = 0;
        PositionIterator positionIterator = layout.createPositionIterator(structure.getCodeType(), structure.getViewMode(), structure.getBytesPerRow());
        int halfCharPos = 0;
        while (!positionIterator.isEndReached()) {
            int nextSpaceSize = positionIterator.nextSpaceType().getHalfCharSize();
            if (viewMode == CodeAreaViewMode.DUAL && positionIterator.getBytePosition() == 0 && positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                linePos = layout.computePositionX(halfCharPos + 2, characterWidth, halfSpaceWidth)
                        + layout.computePositionX(nextSpaceSize, characterWidth, halfSpaceWidth) / 2;
            }

            halfCharPos += 2 + nextSpaceSize;
            int positionX = layout.computePositionX(halfCharPos, characterWidth, halfSpaceWidth);
            if (positionX < invisibleFromLeftX) {
                skipTo++;
                skipToChar = positionIterator.getHalfCharPosition() / 2;
                if (positionIterator.getSection() == BasicCodeAreaSection.TEXT_PREVIEW || positionIterator.isEndReached()) {
                    codeSectionVisible = false;
                }
            } else if (skipRestFrom == -1 && positionX > invisibleFromRightX) {
                skipRestFrom = positionIterator.getPosition();
                skipRestFromChar = (positionIterator.getHalfCharPosition() + 1) / 2;
                if (viewMode == CodeAreaViewMode.DUAL || positionIterator.getSection() == BasicCodeAreaSection.CODE_MATRIX) {
                    previewSectionVisible = false;
                }
            }
        }
        if (skipRestFromChar == -1) {
            skipRestFromChar = (positionIterator.getHalfCharPosition() + 1) / 2;
        }

        splitLinePos = linePos;
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

    public int getSkipTo() {
        return skipTo;
    }

    public int getSkipToChar() {
        return skipToChar;
    }

    public int getSkipRestFrom() {
        return skipRestFrom;
    }

    public int getSkipRestFromChar() {
        return skipRestFromChar;
    }

    public boolean isCodeSectionVisible() {
        return codeSectionVisible;
    }

    public boolean isPreviewSectionVisible() {
        return previewSectionVisible;
    }

    public int getMaxRowDataChars() {
        return skipRestFromChar - skipToChar;
    }
}
