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
package org.exbin.bined.basic;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.capability.RowWrappingCapable;

/**
 * Code area data representation structure for basic variant.
 *
 * @version 0.2.0 2019/02/03
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaLayout {

    public int computeBytesPerRow(BasicCodeAreaStructure structure, int charactersPerPage) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeType codeType = structure.getCodeType();
        int maxBytesPerLine = structure.getMaxBytesPerLine();
        int wrappingBytesGroupSize = structure.getWrappingBytesGroupSize();
        RowWrappingCapable.RowWrappingMode rowWrapping = structure.getRowWrapping();
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

    public int computeCharactersPerRow(BasicCodeAreaStructure structure) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        int charsPerRow = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeCharPos(structure, bytesPerRow - 1) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return charsPerRow;
    }

    public long computeRowsPerDocument(BasicCodeAreaStructure structure) {
        long dataSize = structure.getDataSize();
        int bytesPerRow = structure.getBytesPerRow();
        return dataSize / bytesPerRow + (dataSize % bytesPerRow > 0 ? 1 : 0);
    }

    public int computePositionByte(BasicCodeAreaStructure structure, int rowCharPosition) {
        CodeType codeType = structure.getCodeType();
        return rowCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeFirstCodeCharacterPos(BasicCodeAreaStructure structure, int byteOffset) {
        CodeType codeType = structure.getCodeType();
        return byteOffset * (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeLastCodeCharPos(BasicCodeAreaStructure structure, int byteOffset) {
        CodeType codeType = structure.getCodeType();
        return byteOffset * (codeType.getMaxDigitsForByte() + 1) + codeType.getMaxDigitsForByte() - 1;
    }

    @Nonnull
    public CaretPosition computeMovePosition(BasicCodeAreaStructure structure, CaretPosition position, MovementDirection direction, int rowsPerPage) {
        CodeType codeType = structure.getCodeType();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
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
}
