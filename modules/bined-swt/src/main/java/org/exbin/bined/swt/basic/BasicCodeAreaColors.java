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

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.swt.graphics.Color;

/**
 * Basic code area set of colors.
 *
 * @version 0.2.0 2018/09/01
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaColors {

    @Nullable
    private Color foreground;
    @Nullable
    private Color background;
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
    public Color getForeground() {
        return Objects.requireNonNull(foreground);
    }

    public void setForeground(@Nonnull Color foreground) {
        this.foreground = Objects.requireNonNull(foreground);
    }

    @Nonnull
    public Color getBackground() {
        return Objects.requireNonNull(background);
    }

    public void setBackground(@Nonnull Color background) {
        this.background = Objects.requireNonNull(background);
    }

    @Nonnull
    public Color getSelectionForeground() {
        return Objects.requireNonNull(selectionForeground);
    }

    public void setSelectionForeground(@Nonnull Color selectionForeground) {
        this.selectionForeground = Objects.requireNonNull(selectionForeground);
    }

    @Nonnull
    public Color getSelectionBackground() {
        return Objects.requireNonNull(selectionBackground);
    }

    public void setSelectionBackground(@Nonnull Color selectionBackground) {
        this.selectionBackground = Objects.requireNonNull(selectionBackground);
    }

    @Nonnull
    public Color getSelectionMirrorForeground() {
        return Objects.requireNonNull(selectionMirrorForeground);
    }

    public void setSelectionMirrorForeground(@Nonnull Color selectionMirrorForeground) {
        this.selectionMirrorForeground = Objects.requireNonNull(selectionMirrorForeground);
    }

    @Nonnull
    public Color getSelectionMirrorBackground() {
        return Objects.requireNonNull(selectionMirrorBackground);
    }

    public void setSelectionMirrorBackground(@Nonnull Color selectionMirrorBackground) {
        this.selectionMirrorBackground = Objects.requireNonNull(selectionMirrorBackground);
    }

    @Nonnull
    public Color getCursor() {
        return Objects.requireNonNull(cursor);
    }

    public void setCursor(@Nonnull Color cursor) {
        this.cursor = Objects.requireNonNull(cursor);
    }

    @Nonnull
    public Color getNegativeCursor() {
        return Objects.requireNonNull(negativeCursor);
    }

    public void setNegativeCursor(@Nonnull Color negativeCursor) {
        this.negativeCursor = Objects.requireNonNull(negativeCursor);
    }

    @Nonnull
    public Color getCursorMirror() {
        return Objects.requireNonNull(cursorMirror);
    }

    public void setCursorMirror(@Nonnull Color cursorMirror) {
        this.cursorMirror = Objects.requireNonNull(cursorMirror);
    }

    @Nonnull
    public Color getNegativeCursorMirror() {
        return Objects.requireNonNull(negativeCursorMirror);
    }

    public void setNegativeCursorMirror(@Nonnull Color negativeCursorMirror) {
        this.negativeCursorMirror = Objects.requireNonNull(negativeCursorMirror);
    }

    @Nonnull
    public Color getDecorationLine() {
        return Objects.requireNonNull(decorationLine);
    }

    public void setDecorationLine(@Nonnull Color decorationLine) {
        this.decorationLine = Objects.requireNonNull(decorationLine);
    }

    @Nonnull
    public Color getStripes() {
        return Objects.requireNonNull(stripes);
    }

    public void setStripes(@Nonnull Color stripes) {
        this.stripes = Objects.requireNonNull(stripes);
    }

    public void dispose() {
        // TODO fix colors resource handling
        foreground.dispose();
        background.dispose();
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
