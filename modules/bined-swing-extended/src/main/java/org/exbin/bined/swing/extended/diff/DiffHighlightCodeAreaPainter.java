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
package org.exbin.bined.swing.extended.diff;

import java.awt.Color;
import java.awt.Graphics;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.extended.ExtendedCodeAreaPainter;
import org.exbin.auxiliary.binary_data.BinaryData;

/**
 * Highlighting painter for basic binary diff.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DiffHighlightCodeAreaPainter extends ExtendedCodeAreaPainter {

    private BinaryData comparedData;
    private Color diffColor;
    private Color addedColor;

    public DiffHighlightCodeAreaPainter(CodeAreaCore codeArea) {
        this(codeArea, null);
    }

    public DiffHighlightCodeAreaPainter(CodeAreaCore codeArea, @Nullable BinaryData comparedData) {
        super(codeArea);

        this.comparedData = comparedData;
        diffColor = new Color(255, 180, 180);
        addedColor = new Color(180, 255, 180);
    }

    @Override
    public void paintMainArea(@Nonnull Graphics g) {
        super.paintMainArea(g);
    }

    @Nullable
    @Override
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables) {
        long position = rowDataPosition + byteOnRow;
        if (comparedData != null && position >= comparedData.getDataSize()) {
            return addedColor;
        }

        if (comparedData != null && position < codeArea.getDataSize() && position < comparedData.getDataSize()) {
            byte sourceByte = codeArea.getContentData().getByte(position);
            byte comparedByte = comparedData.getByte(position);

            if (sourceByte != comparedByte) {
                return diffColor;
            }
        }

        return super.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
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
