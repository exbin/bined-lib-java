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
package org.exbin.bined.swing.extended.layout;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;

/**
 * Layout profile for extended code area.
 *
 * @version 0.2.0 2019/01/21
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaLayoutProfile {

    private boolean showHeader = true;
    private int topHeaderSpace = 0;
    private int bottomHeaderSpace = 0;

    private boolean showRowPosition = true;
    private int leftRowPositionSpace = 0;
    private int rightRowPositionSpace = 0;

    private int halfSpaceGroupSize = 0;
    private int spaceGroupSize = 1;
    private int doubleSpaceGroupSize = 0;

    public ExtendedCodeAreaLayoutProfile() {
    }

    /**
     * Copy constructor.
     *
     * @param profile source profile
     * @return copy of profile
     */
    @Nonnull
    public static ExtendedCodeAreaLayoutProfile createCopy(ExtendedCodeAreaLayoutProfile profile) {
        ExtendedCodeAreaLayoutProfile copy = new ExtendedCodeAreaLayoutProfile();
        copy.showHeader = profile.showHeader;
        copy.topHeaderSpace = profile.topHeaderSpace;
        copy.bottomHeaderSpace = profile.bottomHeaderSpace;
        copy.showRowPosition = profile.showRowPosition;
        copy.leftRowPositionSpace = profile.leftRowPositionSpace;
        copy.rightRowPositionSpace = profile.rightRowPositionSpace;
        copy.halfSpaceGroupSize = profile.halfSpaceGroupSize;
        copy.spaceGroupSize = profile.spaceGroupSize;
        copy.doubleSpaceGroupSize = profile.doubleSpaceGroupSize;

        return copy;
    }

    public int computeCharacterPosition(long positionX, int characterWidth, CodeAreaViewMode viewMode, CodeType codeType, int bytesPerRow) {
        // TODO
        return 0;
    }

    public int computeClosestCharacterPosition(long positionX, int characterWidth, CodeAreaViewMode viewMode, CodeType codeType, int bytesPerRow) {
        // TODO
        return 0;
    }

    public long computePixelPosition(int codeCharPosition, int characterWidth, CodeAreaViewMode viewMode, CodeType codeType, int bytesPerRow) {
        if (codeCharPosition == 0) {
            return 0;
        }

        int digitsPerByte = codeType.getMaxDigitsForByte();
        int firstPreviewCodeChar = viewMode == CodeAreaViewMode.TEXT_PREVIEW ? 0 : bytesPerRow * digitsPerByte;

        long positionX;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW && codeCharPosition < firstPreviewCodeChar) {
            int bytePosition = codeCharPosition / digitsPerByte;
            int byteCharacterOffset = codeCharPosition % digitsPerByte;

            positionX = (bytePosition * digitsPerByte * characterWidth) + (byteCharacterOffset * characterWidth);
            for (int bytePos = 1; bytePos < bytePosition; bytePos++) {
                positionX += getSpaceSizeBefore(bytePos, characterWidth);
            }

            if (spaceGroupSize > 0) {
                positionX += (bytePosition / spaceGroupSize) * characterWidth;
            }
            return positionX;
        }

        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
// TODO            return positionX + 
        }
        return -1;
    }

    public int getSpaceSizeBefore(int byteOffset, int characterWidth) {
        if (byteOffset == 0) {
            return 0;
        }
        if (doubleSpaceGroupSize > 0 && (byteOffset % doubleSpaceGroupSize) == 0) {
            return 2 * characterWidth;
        }
        if (spaceGroupSize > 0 && (byteOffset % spaceGroupSize) == 0) {
            return characterWidth;
        }
        if (halfSpaceGroupSize > 0 && (byteOffset % halfSpaceGroupSize) == 0) {
            return characterWidth / 2;
        }

        return 0;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

    public int getBottomHeaderSpace() {
        return bottomHeaderSpace;
    }

    public void setBottomHeaderSpace(int bottomHeaderSpace) {
        this.bottomHeaderSpace = bottomHeaderSpace;
    }

    public int getLeftRowPositionSpace() {
        return leftRowPositionSpace;
    }

    public void setLeftRowPositionSpace(int leftRowPositionSpace) {
        this.leftRowPositionSpace = leftRowPositionSpace;
    }

    public boolean isShowRowPosition() {
        return showRowPosition;
    }

    public void setShowRowPosition(boolean showRowPosition) {
        this.showRowPosition = showRowPosition;
    }

    public int getTopHeaderSpace() {
        return topHeaderSpace;
    }

    public void setTopHeaderSpace(int topHeaderSpace) {
        this.topHeaderSpace = topHeaderSpace;
    }

    public int getRightRowPositionSpace() {
        return rightRowPositionSpace;
    }

    public void setRightRowPositionSpace(int rightRowPositionSpace) {
        this.rightRowPositionSpace = rightRowPositionSpace;
    }

    public int getHalfSpaceGroupSize() {
        return halfSpaceGroupSize;
    }

    public void setHalfSpaceGroupSize(int halfSpaceGroupSize) {
        this.halfSpaceGroupSize = halfSpaceGroupSize;
    }

    public int getSpaceGroupSize() {
        return spaceGroupSize;
    }

    public void setSpaceGroupSize(int spaceGroupSize) {
        this.spaceGroupSize = spaceGroupSize;
    }

    public int getDoubleSpaceGroupSize() {
        return doubleSpaceGroupSize;
    }

    public void setDoubleSpaceGroupSize(int doubleSpaceGroupSize) {
        this.doubleSpaceGroupSize = doubleSpaceGroupSize;
    }
}
