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
package org.exbin.bined.highlight.swing;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.swing.CodeAreaCharAssessor;
import org.exbin.bined.swing.CodeAreaPaintState;
import org.exbin.bined.swing.CodeAreaColorAssessor;

/**
 * Code area non-printable characters highlighting.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class NonprintableCodeAreaAssessor implements CodeAreaColorAssessor, CodeAreaCharAssessor {

    private final CodeAreaColorAssessor parentColorAssessor;
    private final CodeAreaCharAssessor parentCharAssessor;

    @Nullable
    protected Map<Character, Character> unprintableCharactersMapping = null;
    protected boolean showNonprintables;

    private Color nonprintableColor;
    private int charactersPerRow = 1;

    public NonprintableCodeAreaAssessor(@Nullable CodeAreaColorAssessor parentColorAssessor, @Nullable CodeAreaCharAssessor parentCharAssessor) {
        this.parentColorAssessor = parentColorAssessor;
        this.parentCharAssessor = parentCharAssessor;

        nonprintableColor = new Color(180, 255, 180);
    }

    public boolean isShowNonprintables() {
        return showNonprintables;
    }

    public void setShowNonprintables(boolean showNonprintables) {
        this.showNonprintables = showNonprintables;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPaintState) {
        charactersPerRow = codeAreaPaintState.getCharactersPerRow();

        if (unprintableCharactersMapping != null) {
            buildUnprintableCharactersMapping();
        }

        if (parentColorAssessor != null) {
            parentColorAssessor.startPaint(codeAreaPaintState);
        }
    }

    @Nullable
    @Override
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {

        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
        }

        return null;
    }

    @Nullable
    @Override
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
        }

        return null;
    }

    @Nonnull
    @Override
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        if (parentCharAssessor != null) {
            return parentCharAssessor.getPreviewCharacter(rowDataPosition, byteOnRow, charOnRow, section);
        }

        return ' ';
    }

    @Nonnull
    @Override
    public char getPreviewCursorCharacter(long rowDataPosition, int byteOnRow, int charOnRow, byte[] cursorData, int cursorDataLength, CodeAreaSection section) {
        if (parentCharAssessor != null) {
            return parentCharAssessor.getPreviewCursorCharacter(rowDataPosition, byteOnRow, charOnRow, cursorData, cursorDataLength, section);
        }

        return ' ';
    }

    @Nonnull
    @Override
    public Optional<CodeAreaCharAssessor> getParentCharAssessor() {
        return Optional.ofNullable(parentCharAssessor);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorAssessor> getParentColorAssessor() {
        return Optional.ofNullable(parentColorAssessor);
    }

    private void buildUnprintableCharactersMapping() {
        unprintableCharactersMapping = new HashMap<>();
        // Unicode control characters, might not be supported by font
        for (int i = 0; i < 32; i++) {
            unprintableCharactersMapping.put((char) i, Character.toChars(9216 + i)[0]);
        }
        // Space -> Middle Dot
        unprintableCharactersMapping.put(' ', Character.toChars(183)[0]);
        // Tab -> Right-Pointing Double Angle Quotation Mark
        unprintableCharactersMapping.put('\t', Character.toChars(187)[0]);
        // Line Feed -> Currency Sign
        unprintableCharactersMapping.put('\r', Character.toChars(164)[0]);
        // Carriage Return -> Pilcrow Sign
        unprintableCharactersMapping.put('\n', Character.toChars(182)[0]);
        // Ideographic Space -> Degree Sign
        unprintableCharactersMapping.put(Character.toChars(127)[0], Character.toChars(176)[0]);
    }
}
