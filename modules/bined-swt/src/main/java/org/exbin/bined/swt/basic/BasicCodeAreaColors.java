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
package org.exbin.bined.swt.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.exbin.bined.CodeAreaUtils;

/**
 * Basic code area set of colors.
 *
 * @version 0.2.0 2018/09/01
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaColors {

    @Nullable
    private Color textForeground;
    @Nullable
    private Color textBackground;
    @Nullable
    private Color selectionForeground;
    @Nullable
    private Color selectionBackground;
    @Nullable
    private Color selectionMirrorForeground;
    @Nullable
    private Color selectionMirrorBackground;
    @Nullable
    private Color cursor;
    @Nullable
    private Color negativeCursor;
    @Nullable
    private Color cursorMirror;
    @Nullable
    private Color negativeCursorMirror;
    @Nullable
    private Color decorationLine;
    @Nullable
    private Color stripes;

    public BasicCodeAreaColors() {
    }

    @Nonnull
    public Color getTextForeground() {
        return CodeAreaUtils.requireNonNull(textForeground);
    }

    public void setTextForeground(@Nonnull Color textForeground) {
        this.textForeground = CodeAreaUtils.requireNonNull(textForeground);
    }

    @Nonnull
    public Color getTextBackground() {
        return CodeAreaUtils.requireNonNull(textBackground);
    }

    public void setTextBackground(@Nonnull Color textBackground) {
        this.textBackground = CodeAreaUtils.requireNonNull(textBackground);
    }

    @Nonnull
    public Color getSelectionForeground() {
        return CodeAreaUtils.requireNonNull(selectionForeground);
    }

    public void setSelectionForeground(@Nonnull Color selectionForeground) {
        this.selectionForeground = CodeAreaUtils.requireNonNull(selectionForeground);
    }

    @Nonnull
    public Color getSelectionBackground() {
        return CodeAreaUtils.requireNonNull(selectionBackground);
    }

    public void setSelectionBackground(@Nonnull Color selectionBackground) {
        this.selectionBackground = CodeAreaUtils.requireNonNull(selectionBackground);
    }

    @Nonnull
    public Color getSelectionMirrorForeground() {
        return CodeAreaUtils.requireNonNull(selectionMirrorForeground);
    }

    public void setSelectionMirrorForeground(@Nonnull Color selectionMirrorForeground) {
        this.selectionMirrorForeground = CodeAreaUtils.requireNonNull(selectionMirrorForeground);
    }

    @Nonnull
    public Color getSelectionMirrorBackground() {
        return CodeAreaUtils.requireNonNull(selectionMirrorBackground);
    }

    public void setSelectionMirrorBackground(@Nonnull Color selectionMirrorBackground) {
        this.selectionMirrorBackground = CodeAreaUtils.requireNonNull(selectionMirrorBackground);
    }

    @Nonnull
    public Color getCursor() {
        return CodeAreaUtils.requireNonNull(cursor);
    }

    public void setCursor(@Nonnull Color cursor) {
        this.cursor = CodeAreaUtils.requireNonNull(cursor);
    }

    @Nonnull
    public Color getNegativeCursor() {
        return CodeAreaUtils.requireNonNull(negativeCursor);
    }

    public void setNegativeCursor(@Nonnull Color negativeCursor) {
        this.negativeCursor = CodeAreaUtils.requireNonNull(negativeCursor);
    }

    @Nonnull
    public Color getCursorMirror() {
        return CodeAreaUtils.requireNonNull(cursorMirror);
    }

    public void setCursorMirror(@Nonnull Color cursorMirror) {
        this.cursorMirror = CodeAreaUtils.requireNonNull(cursorMirror);
    }

    @Nonnull
    public Color getNegativeCursorMirror() {
        return CodeAreaUtils.requireNonNull(negativeCursorMirror);
    }

    public void setNegativeCursorMirror(@Nonnull Color negativeCursorMirror) {
        this.negativeCursorMirror = CodeAreaUtils.requireNonNull(negativeCursorMirror);
    }

    @Nonnull
    public Color getDecorationLine() {
        return CodeAreaUtils.requireNonNull(decorationLine);
    }

    public void setDecorationLine(@Nonnull Color decorationLine) {
        this.decorationLine = CodeAreaUtils.requireNonNull(decorationLine);
    }

    @Nonnull
    public Color getStripes() {
        return CodeAreaUtils.requireNonNull(stripes);
    }

    public void setStripes(@Nonnull Color stripes) {
        this.stripes = CodeAreaUtils.requireNonNull(stripes);
    }

    public void dispose() {
        // TODO fix colors resource handling
        textForeground.dispose();
        textBackground.dispose();
        selectionForeground.dispose();
        selectionBackground.dispose();
        selectionMirrorForeground.dispose();
        selectionMirrorBackground.dispose();
        cursor.dispose();
        negativeCursor.dispose();
        cursorMirror.dispose();
        negativeCursorMirror.dispose();
        decorationLine.dispose();
        stripes.dispose();
    }
}
