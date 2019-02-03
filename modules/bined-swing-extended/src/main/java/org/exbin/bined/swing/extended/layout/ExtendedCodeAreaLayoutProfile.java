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

import org.exbin.bined.extended.layout.CodeCharPositionIterator;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.extended.layout.SpaceType;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayout;
import org.exbin.bined.extended.ExtendedCodeAreaStructure;

/**
 * Layout profile for extended code area.
 *
 * @version 0.2.0 2019/02/03
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaLayoutProfile implements ExtendedCodeAreaLayout {

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

    @Override
    public int computeHalfCharsPerRow(ExtendedCodeAreaStructure structure) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        int charsPerRow = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeHalfCharPos(bytesPerRow - 1, structure) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return new CharactersNumAndHalf(charsPerRow, false);
    }

    @Override
    public int computeBytesPerRow(int charactersPerPage, ExtendedCodeAreaStructure structure) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeType codeType = structure.getCodeType();
        int maxBytesPerLine = structure.getMaxBytesPerLine();
        RowWrappingCapable.RowWrappingMode rowWrapping = structure.getRowWrapping();
        int wrappingBytesGroupSize = structure.getWrappingBytesGroupSize();
        int computedBytesPerRow;
        if (rowWrapping == RowWrappingCapable.RowWrappingMode.WRAPPING) {
            int charactersPerByte = 0;
            if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
                charactersPerByte += codeType.getMaxDigitsForByte() + 1;
            }
            if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
                charactersPerByte++;
            }
            computedBytesPerRow = charactersPerPage / charactersPerByte;

            if (maxBytesPerLine > 0 && computedBytesPerRow > maxBytesPerLine) {
                computedBytesPerRow = maxBytesPerLine;
            }

            if (wrappingBytesGroupSize > 1) {
                int wrappingBytesGroupOffset = computedBytesPerRow % wrappingBytesGroupSize;
                if (wrappingBytesGroupOffset > 0) {
                    computedBytesPerRow -= wrappingBytesGroupOffset;
                }
            }
        } else {
            computedBytesPerRow = maxBytesPerLine;
        }

        if (computedBytesPerRow < 1) {
            computedBytesPerRow = 1;
        }

        return computedBytesPerRow;
    }

    @Override
    public long computeRowsPerDocument(ExtendedCodeAreaStructure structure) {
        long dataSize = structure.getDataSize();
        int bytesPerRow = structure.getBytesPerRow();
        return dataSize / bytesPerRow + (dataSize % bytesPerRow > 0 ? 1 : 0);
    }

    @Override
    public int computePositionByte(int rowHalfCharPosition, ExtendedCodeAreaStructure structure) {
        CodeType codeType = structure.getCodeType();
        CodeCharPosIterator codeCharPosIterator = new CodeCharPosIterator(codeType);

        return rowHalfCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    @Override
    public synchronized int computeFirstCodeHalfCharPos(int byteOffset, ExtendedCodeAreaStructure structure) {
        CodeType codeType = structure.getCodeType();
        CodeCharPosIterator iterator = new CodeCharPosIterator(byteOffset, codeType);
        return new CharactersNumAndHalf(byteOffset * (codeType.getMaxDigitsForByte() + 1), false);
    }

    @Override
    public int computeLastCodeHalfCharPos(int byteOffset, ExtendedCodeAreaStructure structure) {
        CodeType codeType = structure.getCodeType();
        return byteOffset * (codeType.getMaxDigitsForByte() + 1) + codeType.getMaxDigitsForByte() - 1;
    }

    @Override
    public CaretPosition computeMovePosition(CaretPosition position, MovementDirection direction, ExtendedCodeAreaStructure structure, int rowsPerPage) {
        CodeType codeType = structure.getCodeType();
        long dataSize = structure.getDataSize();
        int bytesPerRow = structure.getBytesPerRow();
        CodeAreaCaretPosition target = new CodeAreaCaretPosition(position.getDataPosition(), position.getCodeOffset(), position.getSection());
        switch (direction) {
            case LEFT: {
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX) {
                    int codeOffset = position.getCodeOffset();
                    if (codeOffset > 0) {
                        target.setCodeOffset(codeOffset - 1);
                    } else if (position.getDataPosition() > 0) {
                        target.setDataPosition(position.getDataPosition() - 1);
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                } else if (position.getDataPosition() > 0) {
                    target.setDataPosition(position.getDataPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX) {
                    int codeOffset = position.getCodeOffset();
                    if (position.getDataPosition() < dataSize && codeOffset < codeType.getMaxDigitsForByte() - 1) {
                        target.setCodeOffset(codeOffset + 1);
                    } else if (position.getDataPosition() < dataSize) {
                        target.setDataPosition(position.getDataPosition() + 1);
                        target.setCodeOffset(0);
                    }
                } else if (position.getDataPosition() < dataSize) {
                    target.setDataPosition(position.getDataPosition() + 1);
                }
                break;
            }
            case UP: {
                if (position.getDataPosition() >= bytesPerRow) {
                    target.setDataPosition(position.getDataPosition() - bytesPerRow);
                }
                break;
            }
            case DOWN: {
                if (position.getDataPosition() + bytesPerRow < dataSize || (position.getDataPosition() + bytesPerRow == dataSize && position.getCodeOffset() == 0)) {
                    target.setDataPosition(position.getDataPosition() + bytesPerRow);
                }
                break;
            }
            case ROW_START: {
                long dataPosition = position.getDataPosition();
                dataPosition -= (dataPosition % bytesPerRow);
                target.setDataPosition(dataPosition);
                target.setCodeOffset(0);
                break;
            }
            case ROW_END: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow - 1 - (dataPosition % bytesPerRow);
                if (dataPosition > Long.MAX_VALUE - increment || dataPosition + increment > dataSize) {
                    target.setDataPosition(dataSize);
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX) {
                    if (target.getDataPosition() == dataSize) {
                        target.setCodeOffset(0);
                    } else {
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                }
                break;
            }
            case PAGE_UP: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow * rowsPerPage;
                if (dataPosition < increment) {
                    target.setDataPosition(dataPosition % bytesPerRow);
                } else {
                    target.setDataPosition(dataPosition - increment);
                }
                break;
            }
            case PAGE_DOWN: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow * rowsPerPage;
                if (dataPosition > dataSize - increment) {
                    long positionOnRow = dataPosition % bytesPerRow;
                    long lastRowDataStart = dataSize - (dataSize % bytesPerRow);
                    if (lastRowDataStart == dataSize - positionOnRow) {
                        target.setDataPosition(dataSize);
                        target.setCodeOffset(0);
                    } else if (lastRowDataStart > dataSize - positionOnRow) {
                        if (lastRowDataStart > bytesPerRow) {
                            lastRowDataStart -= bytesPerRow;
                            target.setDataPosition(lastRowDataStart + positionOnRow);
                        }
                    } else {
                        target.setDataPosition(lastRowDataStart + positionOnRow);
                    }
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                break;
            }
            case DOC_START: {
                target.setDataPosition(0);
                target.setCodeOffset(0);
                break;
            }
            case DOC_END: {
                target.setDataPosition(dataSize);
                target.setCodeOffset(0);
                break;
            }
            case SWITCH_SECTION: {
                CodeAreaSection activeSection = position.getSection() == BasicCodeAreaSection.CODE_MATRIX ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX;
                if (activeSection == BasicCodeAreaSection.TEXT_PREVIEW) {
                    target.setCodeOffset(0);
                }
                target.setSection(activeSection);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected movement direction " + direction.name());
            }
        }

        return target;
    }

    public int computeCodeCharacterPosition(long positionX, int characterWidth, int bytesPerRow, CodeType codeType) {
        CodeCharPositionIterator charPositionIterator = createCharPositionIterator(characterWidth, codeType);
        int charPositionX = 0;
        do {
            if (positionX >= charPositionX && positionX < charPositionX + characterWidth) {
                return charPositionIterator.getCharPosition();
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
                return charPositionIterator.getCharPosition();
            }

            charPositionX += characterWidth;
            int spaceSize = charPositionIterator.nextSpaceSize();
            int halfSpaceSize = spaceSize / 2;
            if (positionX >= charPositionX && positionX < charPositionX + halfSpaceSize) {
                return charPositionIterator.getCharPosition() - 1;
            } else if (positionX >= charPositionX + halfSpaceSize && positionX < charPositionX + spaceSize) {
                return charPositionIterator.getCharPosition();
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
            while (codeCharPosIterator.getCharPosition() < codeCharPosition) {
                positionX += characterWidth + codeCharPosIterator.nextSpaceType();
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

    @Nonnull
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

        private int codeCharPosition;
        private int bytePosition;
        private int halfCharPosition;
        private int codeOffset;
        private boolean oddHalf;

        private final int codeLength;
        private int halfSpacePos = 0;
        private int spacePos = 0;
        private int doubleSpacePos = 0;

        CodeCharPosIterator(CodeType codeType) {
            codeLength = codeType.getMaxDigitsForByte();
            reset();
        }

        @Override
        public void reset() {
            codeCharPosition = 0;
            bytePosition = 0;
            halfCharPosition = 0;
            oddHalf = false;
            codeOffset = codeLength;
            halfSpacePos = halfSpaceGroupSize;
            spacePos = spaceGroupSize;
            doubleSpacePos = doubleSpaceGroupSize;
        }

        @Override
        public int getCharPosition() {
            return codeCharPosition;
        }

        @Override
        public int getBytePosition() {
            return bytePosition;
        }

        @Override
        public SpaceType nextSpaceType() {
            codeCharPosition++;

            if (codeOffset > 1) {
                codeOffset--;
                halfCharPosition += 2;
                return SpaceType.NONE;
            }

            SpaceType spaceType = SpaceType.NONE;
            if (doubleSpacePos > 0) {
                if (doubleSpacePos == 1) {
                    spaceType = SpaceType.DOUBLE;
                    doubleSpacePos = doubleSpaceGroupSize;
                } else {
                    doubleSpacePos--;
                }
            }
            if (spacePos > 0) {
                if (spacePos == 1) {
                    if (spaceType == SpaceType.NONE) {
                        spaceType = SpaceType.SINGLE;
                    }
                    spacePos = spaceGroupSize;
                } else {
                    spacePos--;
                }
            }
            if (halfSpacePos > 0) {
                if (halfSpacePos == 1) {
                    if (spaceType == SpaceType.NONE) {
                        spaceType = SpaceType.HALF;
                        oddHalf = !oddHalf;
                    }
                    halfSpacePos = halfSpaceGroupSize;
                } else {
                    halfSpacePos--;
                }
            }

            codeOffset = codeLength;
            bytePosition++;
            halfCharPosition += 2 + spaceType.getHalfCharSize();
            return spaceType;
        }

        @Override
        public void skip(int count) {
            for (int step = 0; step < count; step++) {
                nextSpaceType();
            }
        }
    }
}
