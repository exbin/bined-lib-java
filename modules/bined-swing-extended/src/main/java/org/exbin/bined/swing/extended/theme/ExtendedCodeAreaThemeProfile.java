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
package org.exbin.bined.swing.extended.theme;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.extended.theme.CodeAreaDecorationType;
import org.exbin.bined.extended.theme.ExtendedBackgroundPaintMode;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaDecorations;

/**
 * Layout profile for extended code area.
 *
 * @version 0.2.0 2018/12/18
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExtendedCodeAreaThemeProfile {

    @Nonnull
    private ExtendedBackgroundPaintMode borderPaintMode = ExtendedBackgroundPaintMode.STRIPED;
    private boolean paintRowPosBackground = true;
    private int verticalLineByteGroupSize = 0;

    private final Set<CodeAreaDecorationType> decorations = new HashSet<>();

    public ExtendedCodeAreaThemeProfile() {
        decorations.add(ExtendedCodeAreaDecorations.HEADER_LINE);
        decorations.add(ExtendedCodeAreaDecorations.ROW_POSITION_LINE);
        decorations.add(ExtendedCodeAreaDecorations.SPLIT_LINE);
    }

    /**
     * Copy constructor.
     *
     * @param profile source profile
     * @return copy of the profile
     */
    @Nonnull
    public static ExtendedCodeAreaThemeProfile createCopy(ExtendedCodeAreaThemeProfile profile) {
        ExtendedCodeAreaThemeProfile copy = new ExtendedCodeAreaThemeProfile();
        copy.borderPaintMode = profile.borderPaintMode;
        copy.paintRowPosBackground = profile.paintRowPosBackground;
        copy.verticalLineByteGroupSize = profile.verticalLineByteGroupSize;
        copy.decorations.clear();
        copy.decorations.addAll(profile.decorations);

        return copy;
    }

    @Nonnull
    public ExtendedBackgroundPaintMode getBackgroundPaintMode() {
        return borderPaintMode;
    }

    public void setBackgroundPaintMode(ExtendedBackgroundPaintMode borderPaintMode) {
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
        return decorations.contains(ExtendedCodeAreaDecorations.ROW_POSITION_LINE);
    }

    public boolean showHeaderLine() {
        return decorations.contains(ExtendedCodeAreaDecorations.HEADER_LINE);
    }

    public boolean showSplitLine() {
        return decorations.contains(ExtendedCodeAreaDecorations.SPLIT_LINE);
    }

    public boolean showBoxLine() {
        return decorations.contains(ExtendedCodeAreaDecorations.BOX_LINES);
    }

    public boolean showGroupLines() {
        return decorations.contains(ExtendedCodeAreaDecorations.GROUP_LINES);
    }
}
