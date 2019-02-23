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
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaSection;
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
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;

/**
 * Code area data representation structure for extended variant.
 *
 * @version 0.2.0 2019/02/23
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

    private ExtendedCodeAreaLayoutProfile layout;
    private long rowsPerDocument;
    private int bytesPerRow;
    private int halfCharsPerRow;
    private int halfCharsPerCodeSection;

    private int codeLastHalfCharPos;

    public void updateCache(DataProvider codeArea, int halfCharsPerPage, ExtendedCodeAreaLayoutProfile layout) {
        this.layout = layout;
        viewMode = ((ViewModeCapable) codeArea).getViewMode();
        codeType = ((CodeTypeCapable) codeArea).getCodeType();
        positionCodeType = ((PositionCodeTypeCapable) codeArea).getPositionCodeType();
        selectionRange = ((SelectionCapable) codeArea).getSelection();
        dataSize = codeArea.getDataSize();
        rowWrapping = ((RowWrappingCapable) codeArea).getRowWrapping();
        maxBytesPerLine = ((RowWrappingCapable) codeArea).getMaxBytesPerRow();
        wrappingBytesGroupSize = ((RowWrappingCapable) codeArea).getWrappingBytesGroupSize();
        bytesPerRow = layout.computeBytesPerRow(halfCharsPerPage, this);
        halfCharsPerRow = layout.computeHalfCharsPerRow(this);
        codeLastHalfCharPos = layout.computeLastByteHalfCharPos(bytesPerRow - 1, viewMode == CodeAreaViewMode.CODE_MATRIX ? BasicCodeAreaSection.CODE_MATRIX : BasicCodeAreaSection.TEXT_PREVIEW, this);
        rowsPerDocument = layout.computeRowsPerDocument(this);

        // Compute last visible character of the code area
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            halfCharsPerCodeSection = layout.computeLastByteHalfCharPos(bytesPerRow - 1, BasicCodeAreaSection.CODE_MATRIX, this);
        } else {
            halfCharsPerCodeSection = -1;
            codeLastHalfCharPos = 0;
        }
    }

    public int computePositionByte(int rowHalfCharPosition) {
        return layout.computePositionByte(rowHalfCharPosition, this);
    }

    public int computeFirstCodeHalfCharPos(int byteOffset, CodeAreaSection section) {
        return layout.computeFirstByteHalfCharPos(byteOffset, section, this);
    }

    @Nonnull
    public CaretPosition computeMovePosition(CaretPosition position, MovementDirection direction, int rowsPerPage) {
        return layout.computeMovePosition(position, direction, this, rowsPerPage);
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
}
