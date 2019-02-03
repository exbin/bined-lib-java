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
package org.exbin.bined.extended;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataProvider;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.RowWrappingCapable.RowWrappingMode;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.extended.capability.PositionCodeTypeCapable;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayout;

/**
 * Code area data representation structure for extended variant.
 *
 * @version 0.2.0 2019/02/03
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaStructure {

    @Nonnull
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    @Nullable
    private SelectionRange selectionRange = null;

    @Nonnull
    private CodeType codeType = CodeType.HEXADECIMAL;
    @Nonnull
    private PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;

    private long dataSize;
    @Nonnull
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int maxBytesPerLine;
    private int wrappingBytesGroupSize;

    private ExtendedCodeAreaLayout layout;
    private long rowsPerDocument;
    private int bytesPerRow;
    private int halfCharsPerRow;
    private int halfCharsPerCodeSection;

    private int codeLastHalfCharPos;
    private int previewHalfCharPos;

    public void updateCache(DataProvider codeArea, int charactersPerPage, ExtendedCodeAreaLayout layout) {
        this.layout = layout;
        viewMode = ((ViewModeCapable) codeArea).getViewMode();
        codeType = ((CodeTypeCapable) codeArea).getCodeType();
        positionCodeType = ((PositionCodeTypeCapable) codeArea).getPositionCodeType();
        selectionRange = ((SelectionCapable) codeArea).getSelection();
        dataSize = codeArea.getDataSize();
        rowWrapping = ((RowWrappingCapable) codeArea).getRowWrapping();
        maxBytesPerLine = ((RowWrappingCapable) codeArea).getMaxBytesPerRow();
        wrappingBytesGroupSize = ((RowWrappingCapable) codeArea).getWrappingBytesGroupSize();
        bytesPerRow = layout.computeBytesPerRow(this, charactersPerPage);
        halfCharsPerRow = layout.computeHalfCharsPerRow(this);
        halfCharsPerCodeSection = layout.computeFirstCodeHalfCharPos(this, bytesPerRow);
        rowsPerDocument = layout.computeRowsPerDocument(this);

        // Compute first and last visible character of the code area
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            codeLastHalfCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1) - 1;
        } else {
            codeLastHalfCharPos = 0;
        }

        if (viewMode == CodeAreaViewMode.DUAL) {
            previewHalfCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1);
        } else {
            previewHalfCharPos = 0;
        }
    }

    public int computePositionByte(int rowCharPosition) {
        return layout.computePositionByte(this, rowCharPosition);
    }

    public int computeFirstCodeHalfCharPos(int byteOffset) {
        return layout.computeFirstCodeHalfCharPos(this, byteOffset);
    }

    @Nonnull
    public CaretPosition computeMovePosition(CaretPosition position, MovementDirection direction, int rowsPerPage) {
        return layout.computeMovePosition(this, position, direction, rowsPerPage);
    }

    @Nonnull
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Nullable
    public SelectionRange getSelectionRange() {
        return selectionRange;
    }

    @Nonnull
    public CodeType getCodeType() {
        return codeType;
    }

    @Nonnull
    public PositionCodeType getPositionCodeType() {
        return positionCodeType;
    }

    public long getDataSize() {
        return dataSize;
    }

    @Nonnull
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

    public int getHalfCharsPerRow() {
        return halfCharsPerRow;
    }

    public int getHalfCharsPerCodeSection() {
        return halfCharsPerCodeSection;
    }

    public int getCodeLastHalfCharPos() {
        return codeLastHalfCharPos;
    }

    public int getPreviewHalfCharPos() {
        return previewHalfCharPos;
    }
}
