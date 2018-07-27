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
import javax.annotation.Nullable;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataProvider;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.RowWrappingCapable.RowWrappingMode;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;

/**
 * Code area data representation structure for basic variant.
 *
 * @version 0.2.0 2018/07/27
 * @author ExBin Project (http://exbin.org)
 */
public class BasicCodeAreaStructure {

    @Nullable
    private CodeAreaViewMode viewMode;
    private final CodeAreaCaretPosition caretPosition = new CodeAreaCaretPosition();
    @Nullable
    private SelectionRange selectionRange = null;

    @Nullable
    private CodeType codeType;

    private long dataSize;
    @Nonnull
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int maxBytesPerLine;
    private int wrappingBytesGroupSize;

    private long rowsPerDocument;
    private int bytesPerRow;
    private int charactersPerRow;

    private int codeLastCharPos;
    private int previewCharPos;

    public void resetCharPositions() {
        // Compute first and last visible character of the code area
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            codeLastCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1) - 1;
        } else {
            codeLastCharPos = 0;
        }

        if (viewMode == CodeAreaViewMode.DUAL) {
            previewCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1);
        } else {
            previewCharPos = 0;
        }

    }

    public void updateCache(@Nonnull DataProvider worker, int charactersPerPage) {
        viewMode = ((ViewModeCapable) worker).getViewMode();
        codeType = ((CodeTypeCapable) worker).getCodeType();
        caretPosition.setPosition(((CaretCapable) worker).getCaret().getCaretPosition());
        selectionRange = ((SelectionCapable) worker).getSelection();
        dataSize = worker.getDataSize();
        rowWrapping = ((RowWrappingCapable) worker).isRowWrapping();
        maxBytesPerLine = ((RowWrappingCapable) worker).getMaxBytesPerRow();
        wrappingBytesGroupSize = ((RowWrappingCapable) worker).getWrappingBytesGroupSize();
        bytesPerRow = computeBytesPerRow(charactersPerPage);
        charactersPerRow = computeCharactersPerRow();
        rowsPerDocument = computeRowsPerDocument();
    }

    private int computeCharactersPerRow() {
        int charsPerRow = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeCharPos(bytesPerRow - 1) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return charsPerRow;
    }

    private long computeRowsPerDocument() {
        return dataSize / bytesPerRow + (dataSize % bytesPerRow > 0 ? 1 : 0);
    }

    public int computePositionByte(int rowCharPosition) {
        return rowCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeLastCodeCharPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1) + codeType.getMaxDigitsForByte() - 1;
    }

    private int computeBytesPerRow(int charactersPerPage) {
        int computedBytesPerRow;
        if (rowWrapping == RowWrappingMode.WRAPPING) {
            int charactersPerByte = 0;
            if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
                charactersPerByte += codeType.getMaxDigitsForByte();
            }
            if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
                charactersPerByte++;
            }
            computedBytesPerRow = (charactersPerPage - (viewMode == CodeAreaViewMode.DUAL ? 1 : 0)) / charactersPerByte;

            if (computedBytesPerRow > maxBytesPerLine) {
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

    @Nullable
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Nullable
    public CodeAreaCaretPosition getCaretPosition() {
        return caretPosition;
    }

    @Nullable
    public SelectionRange getSelectionRange() {
        return selectionRange;
    }

    @Nullable
    public CodeType getCodeType() {
        return codeType;
    }

    public long getDataSize() {
        return dataSize;
    }

    @Nullable
    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    public int getMaxBytesPerLine() {
        return maxBytesPerLine;
    }

    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    public long getRowsPerDocument() {
        return rowsPerDocument;
    }

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public int getCharactersPerRow() {
        return charactersPerRow;
    }

    public int getCodeLastCharPos() {
        return codeLastCharPos;
    }

    public int getPreviewCharPos() {
        return previewCharPos;
    }
}
