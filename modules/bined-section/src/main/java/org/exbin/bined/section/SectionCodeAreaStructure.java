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
package org.exbin.bined.section;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataProvider;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.section.capability.PositionCodeTypeCapable;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;

/**
 * Code area data representation structure for section variant.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectionCodeAreaStructure {

    @Nonnull
    protected CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;

    @Nonnull
    protected CodeType codeType = CodeType.HEXADECIMAL;
    @Nonnull
    protected PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;

    protected long dataSize;
    @Nonnull
    protected RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    protected int maxBytesPerLine;
    protected int wrappingBytesGroupSize;

    protected SectionCodeAreaLayoutProfile layout;
    protected long rowsPerDocument;
    protected int bytesPerRow;
    protected int halfCharsPerRow;

    protected int codeLastHalfCharPos;

    public void updateCache(DataProvider codeArea, int halfCharsPerPage, SectionCodeAreaLayoutProfile layout) {
        this.layout = layout;
        viewMode = ((ViewModeCapable) codeArea).getViewMode();
        codeType = ((CodeTypeCapable) codeArea).getCodeType();
        positionCodeType = ((PositionCodeTypeCapable) codeArea).getPositionCodeType();
        dataSize = codeArea.getDataSize();
        rowWrapping = ((RowWrappingCapable) codeArea).getRowWrapping();
        maxBytesPerLine = ((RowWrappingCapable) codeArea).getMaxBytesPerRow();
        wrappingBytesGroupSize = ((RowWrappingCapable) codeArea).getWrappingBytesGroupSize();
        bytesPerRow = layout.computeBytesPerRow(halfCharsPerPage, this);
        halfCharsPerRow = layout.computeHalfCharsPerRow(this);
        codeLastHalfCharPos = viewMode == CodeAreaViewMode.TEXT_PREVIEW ? 0
                : layout.computeLastByteHalfCharPos(bytesPerRow - 1, viewMode == CodeAreaViewMode.CODE_MATRIX ? BasicCodeAreaSection.CODE_MATRIX : BasicCodeAreaSection.TEXT_PREVIEW, this);
        rowsPerDocument = layout.computeRowsPerDocument(this);
    }

    public int computePositionByte(int rowHalfCharPosition) {
        return layout.computePositionByte(rowHalfCharPosition, this);
    }

    public int computeFirstCodeHalfCharPos(int byteOffset, CodeAreaSection section) {
        return layout.computeFirstByteHalfCharPos(byteOffset, section, this);
    }

    @Nonnull
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction, int rowsPerPage) {
        return layout.computeMovePosition(position, direction, this, rowsPerPage);
    }

    @Nonnull
    public CodeAreaViewMode getViewMode() {
        return viewMode;
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

    public int getCodeLastHalfCharPos() {
        return codeLastHalfCharPos;
    }
}
