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
package org.exbin.bined.swing.section.diff;

import java.awt.Color;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.swing.CodeAreaColorAssessor;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPaintState;

/**
 * Highlighting color assessor for basic binary diff.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DiffHighlightCodeAreaColorAssessor implements CodeAreaColorAssessor {

    protected CodeAreaColorAssessor parentAssessor;
    protected BinaryData comparedData;
    protected Color diffColor;
    protected Color addedColor;

    protected CodeAreaCore codeArea;
    protected long dataSize;

    public DiffHighlightCodeAreaColorAssessor(CodeAreaCore codeArea, @Nullable CodeAreaColorAssessor parentColorAssessor, @Nullable BinaryData comparedData) {
        this.codeArea = codeArea;
        this.parentAssessor = parentColorAssessor;
        this.comparedData = comparedData;
        diffColor = new Color(255, 180, 180);
        addedColor = new Color(180, 255, 180);
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPainterState) {
        dataSize = codeAreaPainterState.getDataSize();

        if (parentAssessor != null) {
            parentAssessor.startPaint(codeAreaPainterState);
        }
    }

    @Nullable
    @Override
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        long position = rowDataPosition + byteOnRow;
        if (comparedData != null && position >= comparedData.getDataSize()) {
            return addedColor;
        }

        if (comparedData != null && position < dataSize && position < comparedData.getDataSize()) {
            byte sourceByte = codeArea.getContentData().getByte(position);
            byte comparedByte = comparedData.getByte(position);

            if (sourceByte != comparedByte) {
                return diffColor;
            }
        }

        if (parentAssessor != null) {
            return parentAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

        return null;
    }

    @Nullable
    @Override
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (parentAssessor != null) {
            return parentAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

        return null;
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorAssessor> getParentColorAssessor() {
        return Optional.ofNullable(parentAssessor);
    }

    public void setComparedData(BinaryData comparedData) {
        this.comparedData = comparedData;
        // Force repaint
        codeArea.notifyDataChanged();
    }

    @Nonnull
    public Color getDiffColor() {
        return diffColor;
    }

    public void setDiffColor(Color diffColor) {
        this.diffColor = diffColor;
    }
}
