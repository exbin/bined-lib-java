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
package org.exbin.bined.section.layout;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.section.SectionCodeAreaStructure;
import org.exbin.bined.CodeAreaCaretPosition;

/**
 * Layout interface for section code area.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface SectionCodeAreaLayoutProfile {

    int computeBytesPerRow(int halfCharsPerPage, SectionCodeAreaStructure structure);

    int computeHalfCharsPerRow(SectionCodeAreaStructure structure);

    long computeRowsPerDocument(SectionCodeAreaStructure structure);

    int computePositionByte(int rowHalfCharPosition, SectionCodeAreaStructure structure);

    int computeFirstByteHalfCharPos(int byteOffset, CodeAreaSection section, SectionCodeAreaStructure structure);

    int computeLastByteHalfCharPos(int byteOffset, CodeAreaSection section, SectionCodeAreaStructure structure);

    boolean isShowHeader();

    void setShowHeader(boolean showHeader);

    boolean isShowRowPosition();

    void setShowRowPosition(boolean showRowPosition);

    boolean isHalfShiftedUsed();

    int computeHeaderOffsetPositionY();

    int computeRowPositionOffsetPositionX();

    @Nonnull
    CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction, SectionCodeAreaStructure structure, int rowsPerPage);

    int computePositionX(int halfCharPosition, int characterWidth, int halfSpaceWidth);

    @Nonnull
    SectionCodeAreaLayoutProfile createCopy();

    @Nonnull
    PositionIterator createPositionIterator(CodeType codeType, CodeAreaViewMode viewMode, int bytesPerRow);

    int computeRowPositionAreaWidth(int characterWidth, int rowPositionLength);

    int computeHeaderAreaHeight(int fontHeight);
}
