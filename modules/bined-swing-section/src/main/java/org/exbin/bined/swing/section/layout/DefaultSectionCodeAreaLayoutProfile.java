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
package org.exbin.bined.swing.section.layout;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.section.layout.SpaceType;
import org.exbin.bined.section.SectionCodeAreaStructure;
import org.exbin.bined.section.layout.PositionIterator;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;

/**
 * Layout profile for extended code area.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultSectionCodeAreaLayoutProfile implements SectionCodeAreaLayoutProfile {

    protected boolean showHeader = true;
    protected int topHeaderSpace = 0;
    protected int bottomHeaderSpace = 0;

    protected boolean showRowPosition = true;
    protected int leftRowPositionSpace = 0;
    protected int rightRowPositionSpace = 0;

    protected int halfSpaceGroupSize = 0;
    protected int spaceGroupSize = 1;
    protected int doubleSpaceGroupSize = 0;

    public DefaultSectionCodeAreaLayoutProfile() {
    }

    /**
     * Copy constructor.
     *
     * @return copy of profile
     */
    @Nonnull
    @Override
    public DefaultSectionCodeAreaLayoutProfile createCopy() {
        DefaultSectionCodeAreaLayoutProfile copy = new DefaultSectionCodeAreaLayoutProfile();
        copy.showHeader = this.showHeader;
        copy.topHeaderSpace = this.topHeaderSpace;
        copy.bottomHeaderSpace = this.bottomHeaderSpace;
        copy.showRowPosition = this.showRowPosition;
        copy.leftRowPositionSpace = this.leftRowPositionSpace;
        copy.rightRowPositionSpace = this.rightRowPositionSpace;
        copy.halfSpaceGroupSize = this.halfSpaceGroupSize;
        copy.spaceGroupSize = this.spaceGroupSize;
        copy.doubleSpaceGroupSize = this.doubleSpaceGroupSize;

        return copy;
    }

    @Override
    public int computeHalfCharsPerRow(SectionCodeAreaStructure structure) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeType codeType = structure.getCodeType();
        int bytesPerRow = structure.getBytesPerRow();
        PosIterator posIterator = new PosIterator(codeType, viewMode, bytesPerRow);
        while (!posIterator.isEndReached()) {
            posIterator.nextSpaceType();
        }

        return posIterator.getHalfCharPosition();
    }

    @Override
    public int computeBytesPerRow(int halfCharsPerPage, SectionCodeAreaStructure structure) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeType codeType = structure.getCodeType();
        int maxBytesPerLine = structure.getMaxBytesPerLine();
        RowWrappingMode rowWrapping = structure.getRowWrapping();
        int wrappingBytesGroupSize = structure.getWrappingBytesGroupSize();
        int computedBytesPerRow = 0;
        if (rowWrapping == RowWrappingMode.WRAPPING) {
            if (viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
                computedBytesPerRow = halfCharsPerPage >> 1;
            } else {
                PosIterator posIterator = new PosIterator(codeType, CodeAreaViewMode.CODE_MATRIX, 0);
                do {
                    computedBytesPerRow = posIterator.getBytePosition();
                    int halfCharPos = posIterator.getHalfCharPosition() + 2;
                    if (viewMode == CodeAreaViewMode.DUAL) {
                        halfCharPos += 4 + posIterator.getBytePosition() * 2;
                    }

                    if (halfCharPos > halfCharsPerPage) {
                        break;
                    }

                    posIterator.nextSpaceType();
                } while (!posIterator.endReached);
            }

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
    public long computeRowsPerDocument(SectionCodeAreaStructure structure) {
        long dataSize = structure.getDataSize();
        int bytesPerRow = structure.getBytesPerRow();
        return dataSize / bytesPerRow + 1;
    }

    @Override
    public int computePositionByte(int rowHalfCharPosition, SectionCodeAreaStructure structure) {
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        PosIterator posIterator = new PosIterator(codeType, viewMode, bytesPerRow);
        int bytePosition = 0;
        while (posIterator.getHalfCharPosition() < rowHalfCharPosition && !posIterator.isEndReached()) {
            bytePosition = posIterator.getBytePosition();
            posIterator.nextSpaceType();
        }

        return bytePosition;
    }

    @Override
    public synchronized int computeFirstByteHalfCharPos(int byteOffset, CodeAreaSection section, SectionCodeAreaStructure structure) {
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();

        PosIterator posIterator = new PosIterator(codeType, viewMode, bytesPerRow);
        while ((posIterator.getBytePosition() < byteOffset || posIterator.getSection() != section) && !posIterator.isEndReached()) {
            posIterator.nextSpaceType();
        }
        return posIterator.getHalfCharPosition();
    }

    @Override
    public int computeLastByteHalfCharPos(int byteOffset, CodeAreaSection section, SectionCodeAreaStructure structure) {
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        PosIterator posIterator = new PosIterator(codeType, viewMode, bytesPerRow);
        int halfCharPos = 0;
        while ((posIterator.getBytePosition() <= byteOffset || posIterator.getSection() != section) && !posIterator.isEndReached()) {
            halfCharPos = posIterator.getHalfCharPosition();
            posIterator.nextSpaceType();
        }

        return halfCharPos;
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction, SectionCodeAreaStructure structure, int rowsPerPage) {
        CodeType codeType = structure.getCodeType();
        long dataSize = structure.getDataSize();
        int bytesPerRow = structure.getBytesPerRow();
        CodeAreaSection section = position.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        DefaultCodeAreaCaretPosition target = new DefaultCodeAreaCaretPosition(position.getDataPosition(), position.getCodeOffset(), section);
        switch (direction) {
            case LEFT: {
                if (section != BasicCodeAreaSection.TEXT_PREVIEW) {
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
                if (section != BasicCodeAreaSection.TEXT_PREVIEW) {
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
                if (position.getDataPosition() < dataSize - bytesPerRow || (position.getDataPosition() == dataSize - bytesPerRow && position.getCodeOffset() == 0)) {
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
                if (section != BasicCodeAreaSection.TEXT_PREVIEW) {
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
                long increment = (long) bytesPerRow * rowsPerPage;
                if (dataPosition < increment) {
                    target.setDataPosition(dataPosition % bytesPerRow);
                } else {
                    target.setDataPosition(dataPosition - increment);
                }
                break;
            }
            case PAGE_DOWN: {
                long dataPosition = position.getDataPosition();
                long increment = (long) bytesPerRow * rowsPerPage;
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
                CodeAreaSection activeSection = section == BasicCodeAreaSection.TEXT_PREVIEW ? BasicCodeAreaSection.CODE_MATRIX : BasicCodeAreaSection.TEXT_PREVIEW;
                if (activeSection == BasicCodeAreaSection.TEXT_PREVIEW) {
                    target.setCodeOffset(0);
                }
                target.setSection(activeSection);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(direction);
        }

        return target;
    }

    public int computeCodeCharacterPosition(long positionX, int characterWidth, int bytesPerRow, CodeType codeType) {
        PositionIterator charPositionIterator = createPositionIterator(codeType, CodeAreaViewMode.CODE_MATRIX, bytesPerRow);
        int charPositionX = 0;
        do {
            if (positionX >= charPositionX && positionX < charPositionX + characterWidth) {
                return charPositionIterator.getPosition();
            }

            SpaceType spaceType = charPositionIterator.nextSpaceType();
            charPositionX += characterWidth + characterWidth * spaceType.getHalfCharSize();
        } while (charPositionX < bytesPerRow * codeType.getMaxDigitsForByte());

        return -1;
    }

    public int computeClosestCharacterPosition(long positionX, int characterWidth, int bytesPerRow, CodeType codeType) {
        PositionIterator charPositionIterator = createPositionIterator(codeType, CodeAreaViewMode.CODE_MATRIX, bytesPerRow);
        int charPositionX = 0;
        do {
            if (positionX >= charPositionX && positionX < charPositionX + characterWidth) {
                return charPositionIterator.getPosition();
            }

            charPositionX += characterWidth;
            charPositionIterator.nextSpaceType();
            int halfSpaceSize = characterWidth / 2;
            if (positionX >= charPositionX && positionX < charPositionX + halfSpaceSize) {
                return charPositionIterator.getPosition() - 1;
            } else if (positionX >= charPositionX + halfSpaceSize && positionX < charPositionX + characterWidth) {
                return charPositionIterator.getPosition();
            }

            charPositionX += characterWidth;
        } while (charPositionX < bytesPerRow * codeType.getMaxDigitsForByte());

        return -1;
    }

    @Nonnull
    @Override
    public PositionIterator createPositionIterator(CodeType codeType, CodeAreaViewMode viewMode, int bytesPerRow) {
        return new PosIterator(codeType, viewMode, bytesPerRow);
    }

    public int computePixelPosition(int codeCharPosition, int characterWidth, CodeAreaViewMode viewMode, CodeType codeType, int bytesPerRow) {
        if (codeCharPosition == 0) {
            return 0;
        }

        int digitsPerByte = codeType.getMaxDigitsForByte();
        int firstPreviewCodeChar = viewMode == CodeAreaViewMode.TEXT_PREVIEW ? 0 : bytesPerRow * digitsPerByte;
        int halfSpaceWidth = characterWidth / 2;

        int positionX = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            PosIterator posIterator = new PosIterator(codeType, viewMode, bytesPerRow);
            while (posIterator.getPosition() < codeCharPosition) {
                posIterator.nextSpaceType();
                positionX = computePositionX(posIterator.getHalfCharPosition(), characterWidth, halfSpaceWidth);
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

    @Override
    public int computePositionX(int halfCharPosition, int characterWidth, int halfSpaceWidth) {
        return characterWidth * (halfCharPosition >> 1) + halfSpaceWidth * (halfCharPosition & 1);
    }

    @Override
    public boolean isShowHeader() {
        return showHeader;
    }

    @Override
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

    @Override
    public boolean isShowRowPosition() {
        return showRowPosition;
    }

    @Override
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

    @Override
    public boolean isHalfShiftedUsed() {
        return halfSpaceGroupSize > 0;
    }

    @Override
    public int computeRowPositionAreaWidth(int characterWidth, int rowPositionLength) {
        return isShowRowPosition() ? characterWidth * (rowPositionLength + 1) + getLeftRowPositionSpace() + getRightRowPositionSpace() : 0;
    }

    @Override
    public int computeHeaderAreaHeight(int fontHeight) {
        return isShowHeader() ? fontHeight + fontHeight / 4 + getTopHeaderSpace() + getBottomHeaderSpace() : 0;
    }

    @Override
    public int computeHeaderOffsetPositionY() {
        return topHeaderSpace;
    }

    @Override
    public int computeRowPositionOffsetPositionX() {
        return leftRowPositionSpace;
    }

    @ParametersAreNonnullByDefault
    private final class PosIterator implements PositionIterator {

        private int position;
        private int bytePosition;
        private int halfCharPosition;
        private int codeOffset;
        private boolean oddHalf;
        private boolean endReached;
        @Nonnull
        private BasicCodeAreaSection section;

        private final int codeLength;
        @Nonnull
        private final CodeAreaViewMode viewMode;
        private final int bytesPerRow;

        private int halfSpacePos = 0;
        private int spacePos = 0;
        private int doubleSpacePos = 0;

        PosIterator(CodeType codeType, CodeAreaViewMode viewMode, int bytesPerRow) {
            codeLength = codeType.getMaxDigitsForByte();
            this.viewMode = viewMode;
            this.bytesPerRow = bytesPerRow;
            reset();
        }

        @Override
        public void reset() {
            endReached = false;
            position = 0;
            bytePosition = 0;
            halfCharPosition = 0;
            oddHalf = false;
            section = viewMode == CodeAreaViewMode.TEXT_PREVIEW ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX;
            codeOffset = 0;
            halfSpacePos = halfSpaceGroupSize;
            spacePos = spaceGroupSize;
            doubleSpacePos = doubleSpaceGroupSize;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public int getBytePosition() {
            return bytePosition;
        }

        @Override
        public int getHalfCharPosition() {
            return halfCharPosition;
        }

        @Override
        public int getCodeOffset() {
            return codeOffset;
        }

        @Override
        public boolean isEndReached() {
            return endReached;
        }

        @Nonnull
        @Override
        public BasicCodeAreaSection getSection() {
            return section;
        }

        @Nonnull
        @Override
        public SpaceType nextSpaceType() {
            if (endReached) {
                return SpaceType.NONE;
            }

            position++;

            SpaceType spaceType = SpaceType.NONE;
            if (section != BasicCodeAreaSection.TEXT_PREVIEW) {
                if (codeOffset < codeLength - 1) {
                    codeOffset++;
                    halfCharPosition += 2;
                    return spaceType;
                }

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

                codeOffset = 0;
            }

            if (bytePosition + 1 == bytesPerRow) {
                if (viewMode == CodeAreaViewMode.DUAL && section == BasicCodeAreaSection.CODE_MATRIX) {
                    section = BasicCodeAreaSection.TEXT_PREVIEW;
                    bytePosition = 0;
                    spaceType = SpaceType.SINGLE;
                } else {
                    endReached = true;
                    spaceType = SpaceType.NONE;
                }
            } else {
                bytePosition++;
            }

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
