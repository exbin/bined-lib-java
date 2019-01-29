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
 * @version 0.2.0 2019/01/29
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

    public int computeCodeCharacterPosition(long positionX, int characterWidth, int bytesPerRow, CodeType codeType) {
        CodeCharPositionIterator charPositionIterator = createCharPositionIterator(characterWidth, codeType);
        int charPositionX = 0;
        do {
            if (positionX >= charPositionX && positionX < charPositionX + characterWidth) {
                return charPositionIterator.getPosition();
            }

            int spaceSizeX = charPositionIterator.nextSpaceSize();
            charPositionX += characterWidth + spaceSizeX;
        } while (charPositionX < bytesPerRow * codeType.getMaxDigitsForByte());

        return -1;
    }

    public int computeClosestCharacterPosition(long positionX, int characterWidth, int bytesPerRow, CodeType codeType) {
        CodeCharPositionIterator charPositionIterator = createCharPositionIterator(characterWidth, codeType);
        int charPositionX = 0;
        do {
            if (positionX >= charPositionX && positionX < charPositionX + characterWidth) {
                return charPositionIterator.getPosition();
            }

            charPositionX += characterWidth;
            int spaceSize = charPositionIterator.nextSpaceSize();
            int halfSpaceSize = spaceSize / 2;
            if (positionX >= charPositionX && positionX < charPositionX + halfSpaceSize) {
                return charPositionIterator.getPosition() - 1;
            } else if (positionX >= charPositionX + halfSpaceSize && positionX < charPositionX + spaceSize) {
                return charPositionIterator.getPosition();
            }

            charPositionX += spaceSize;
        } while (charPositionX < bytesPerRow * codeType.getMaxDigitsForByte());

        return -1;
    }

    @Nonnull
    public CodeCharPositionIterator createCharPositionIterator(int characterWidth, CodeType codeType) {
        return new CodeCharPosIterator(characterWidth, codeType);
    }

    public int computePixelPosition(int codeCharPosition, int characterWidth, CodeAreaViewMode viewMode, CodeType codeType, int bytesPerRow) {
        if (codeCharPosition == 0) {
            return 0;
        }

        int digitsPerByte = codeType.getMaxDigitsForByte();
        int firstPreviewCodeChar = viewMode == CodeAreaViewMode.TEXT_PREVIEW ? 0 : bytesPerRow * digitsPerByte;

        int positionX = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            CodeCharPosIterator codeCharPosIterator = new CodeCharPosIterator(characterWidth, codeType);
            while (codeCharPosIterator.getPosition() < codeCharPosition) {
                positionX += characterWidth + codeCharPosIterator.nextSpaceSize();
            }

            if (codeCharPosition < firstPreviewCodeChar) {
                return positionX;
            }
        }

        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            int previewCharPos = codeCharPosition - (digitsPerByte * bytesPerRow);
            if (previewCharPos > bytesPerRow) {
                return -1;
            }
            return positionX + (viewMode == CodeAreaViewMode.DUAL ? characterWidth : 0) + (previewCharPos * characterWidth);
        }
        return -1;
    }

    public SpaceType getSpaceSizeTypeBefore(int byteOffset, int characterWidth) {
        if (byteOffset == 0) {
            return SpaceType.NONE;
        }
        if (doubleSpaceGroupSize > 0 && (byteOffset % doubleSpaceGroupSize) == 0) {
            return SpaceType.DOUBLE;
        }
        if (spaceGroupSize > 0 && (byteOffset % spaceGroupSize) == 0) {
            return SpaceType.SINGLE;
        }
        if (halfSpaceGroupSize > 0 && (byteOffset % halfSpaceGroupSize) == 0) {
            return SpaceType.HALF;
        }

        return SpaceType.NONE;
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

    private final class CodeCharPosIterator implements CodeCharPositionIterator {

        private int position = 0;
        private int codeOffset = 0;
        private boolean oddHalf = false;

        private final int characterWidth;
        private final int codeLength;
        private int halfSpacePos = 0;
        private int spacePos = 0;
        private int doubleSpacePos = 0;

        CodeCharPosIterator(int characterWidth, CodeType codeType) {
            if (characterWidth < 2) {
                throw new IllegalArgumentException("Characters must be at least 2 pixels wide");
            }
            this.characterWidth = characterWidth;
            codeLength = codeType.getMaxDigitsForByte();
            reset();
        }

        @Override
        public void reset() {
            position = 0;
            codeOffset = codeLength;
            halfSpacePos = halfSpaceGroupSize;
            spacePos = spaceGroupSize;
            doubleSpacePos = doubleSpaceGroupSize;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public int nextSpaceSize() {
            position++;

            if (codeOffset > 1) {
                codeOffset--;
                return 0;
            }

            int spaceSize = 0;
            if (doubleSpacePos > 0) {
                if (doubleSpacePos == 1) {
                    spaceSize = characterWidth * 2;
                    doubleSpacePos = doubleSpaceGroupSize;
                } else {
                    doubleSpacePos--;
                }
            }
            if (spacePos > 0) {
                if (spacePos == 1) {
                    if (spaceSize == 0) {
                        spaceSize = characterWidth;
                    }
                    spacePos = spaceGroupSize;
                } else {
                    spacePos--;
                }
            }
            if (halfSpacePos > 0) {
                if (halfSpacePos == 1) {
                    if (spaceSize == 0) {
                        spaceSize = oddHalf ? characterWidth - characterWidth / 2 : characterWidth / 2;
                        oddHalf = !oddHalf;
                    }
                    halfSpacePos = halfSpaceGroupSize;
                } else {
                    halfSpacePos--;
                }
            }

            codeOffset = codeLength;
            return spaceSize;
        }

        @Override
        public void skip(int count) {
            for (int step = 0; step < count; step++) {
                nextSpaceSize();
            }
        }
    }

    public enum SpaceType {
        NONE, HALF, SINGLE, DOUBLE
    };
}
