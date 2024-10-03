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
package org.exbin.bined.swing.section.theme;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.section.theme.CodeAreaDecorationType;
import org.exbin.bined.section.theme.SectionBackgroundPaintMode;
import org.exbin.bined.swing.section.layout.SectionCodeAreaDecorations;

/**
 * Layout profile for section code area.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectionCodeAreaThemeProfile {

    @Nonnull
    protected SectionBackgroundPaintMode borderPaintMode = SectionBackgroundPaintMode.STRIPED;
    protected boolean paintRowPosBackground = true;
    protected int verticalLineByteGroupSize = 0;

    protected final Set<CodeAreaDecorationType> decorations = new HashSet<>();

    public SectionCodeAreaThemeProfile() {
        decorations.add(SectionCodeAreaDecorations.HEADER_LINE);
        decorations.add(SectionCodeAreaDecorations.ROW_POSITION_LINE);
        decorations.add(SectionCodeAreaDecorations.SPLIT_LINE);
    }

    /**
     * Copy constructor.
     *
     * @return copy of the profile
     */
    @Nonnull
    public SectionCodeAreaThemeProfile createCopy() {
        SectionCodeAreaThemeProfile copy = new SectionCodeAreaThemeProfile();
        copy.borderPaintMode = borderPaintMode;
        copy.paintRowPosBackground = paintRowPosBackground;
        copy.verticalLineByteGroupSize = verticalLineByteGroupSize;
        copy.decorations.clear();
        copy.decorations.addAll(decorations);

        return copy;
    }

    @Nonnull
    public SectionBackgroundPaintMode getBackgroundPaintMode() {
        return borderPaintMode;
    }

    public void setBackgroundPaintMode(SectionBackgroundPaintMode borderPaintMode) {
        this.borderPaintMode = borderPaintMode;
    }

    public boolean isPaintRowPosBackground() {
        return paintRowPosBackground;
    }

    public void setPaintRowPosBackground(boolean paintRowPosBackground) {
        this.paintRowPosBackground = paintRowPosBackground;
    }

    public int getVerticalLineByteGroupSize() {
        return verticalLineByteGroupSize;
    }

    public void setVerticalLineByteGroupSize(int verticalLineByteGroupSize) {
        this.verticalLineByteGroupSize = verticalLineByteGroupSize;
    }

    public boolean hasDecoration(CodeAreaDecorationType decoration) {
        return decorations.contains(decoration);
    }

    public void setDecoration(CodeAreaDecorationType decoration, boolean value) {
        if (!value && hasDecoration(decoration)) {
            decorations.remove(decoration);
        } else if (value && !hasDecoration(decoration)) {
            decorations.add(decoration);
        }
    }

    public boolean showRowPositionLine() {
        return decorations.contains(SectionCodeAreaDecorations.ROW_POSITION_LINE);
    }

    public boolean showHeaderLine() {
        return decorations.contains(SectionCodeAreaDecorations.HEADER_LINE);
    }

    public boolean showSplitLine() {
        return decorations.contains(SectionCodeAreaDecorations.SPLIT_LINE);
    }

    public boolean showBoxLine() {
        return decorations.contains(SectionCodeAreaDecorations.BOX_LINES);
    }

    public boolean showGroupLines() {
        return decorations.contains(SectionCodeAreaDecorations.GROUP_LINES);
    }
}
