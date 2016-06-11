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
package org.exbin.deltahex.highlight;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.exbin.deltahex.DefaultHexadecimalPainter;
import org.exbin.deltahex.Hexadecimal;

/**
 * Hexadecimal component painter supporting search matches highlighting.
 *
 * @version 0.1.0 2016/06/11
 * @author ExBin Project (http://exbin.org)
 */
public class HighlightHexadecimalPainter extends DefaultHexadecimalPainter {

    /**
     * Matches must be ordered by position.
     */
    private final List<SearchMatch> matches = new ArrayList<>();
    private int currentMatchIndex = -1;
    private int matchIndex = 0;

    private Color foundMatchesBackgroundColor;
    private Color currentMatchBackgroundColor;

    public HighlightHexadecimalPainter(Hexadecimal hexadecimal) {
        super(hexadecimal);

        foundMatchesBackgroundColor = new Color(180, 255, 180);
        currentMatchBackgroundColor = new Color(255, 210, 180);
    }

    @Override
    public void paintMainArea(Graphics g) {
        matchIndex = 0;
        super.paintMainArea(g);
    }

    @Override
    public void paintLineBackground(Graphics g, long line, int positionY, long dataPosition, int bytesPerBounds, int lineHeight, int charWidth) {
        g.setColor(foundMatchesBackgroundColor);
        Point scrollPoint = hexadecimal.getScrollPoint();
        long lineMatchIndex = matchIndex;
        while (lineMatchIndex < matches.size()) {
            SearchMatch match = matches.get(matchIndex);
            if (match.position > dataPosition + bytesPerBounds) {
                break;
            }
            if (match.position + match.length >= dataPosition) {
                long startPosition;
                if (match.position <= dataPosition) {
                    startPosition = 0;
                } else {
                    startPosition = match.position - dataPosition;
                }
                long endPosition = match.position + match.length - dataPosition;

                int blockX = (int) (startPosition * charWidth);
                int blockWidth = (int) ((endPosition - startPosition) * charWidth);
                if (lineMatchIndex == currentMatchIndex) {
                    g.setColor(currentMatchBackgroundColor);
                }
                if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.PREVIEW) {
                    g.fillRect(hexadecimal.getHexadecimalRectangle().x - scrollPoint.x + blockX * 3, positionY - lineHeight, blockWidth * 3 - charWidth, lineHeight);
                }
                if (hexadecimal.getViewMode() != Hexadecimal.ViewMode.HEXADECIMAL) {
                    g.fillRect(hexadecimal.getPreviewX() - scrollPoint.x + blockX, positionY - lineHeight, blockWidth, lineHeight);
                }
                if (lineMatchIndex == currentMatchIndex) {
                    g.setColor(foundMatchesBackgroundColor);
                }
                lineMatchIndex++;
            } else {
                matchIndex++;
                lineMatchIndex++;
            }
        }

        super.paintSelectionBackground(g, line, positionY, dataPosition, bytesPerBounds, lineHeight, charWidth);
    }

    public List<SearchMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<SearchMatch> matches) {
        this.matches.clear();
        this.matches.addAll(matches);
    }

    public void clearMatches() {
        this.matches.clear();
    }

    public SearchMatch getCurrentMatch() {
        if (currentMatchIndex >= 0) {
            return matches.get(currentMatchIndex);
        }

        return null;
    }

    public int getCurrentMatchIndex() {
        return currentMatchIndex;
    }

    public void setCurrentMatchIndex(int currentMatchIndex) {
        this.currentMatchIndex = currentMatchIndex;
    }

    public Color getFoundMatchesBackgroundColor() {
        return foundMatchesBackgroundColor;
    }

    public void setFoundMatchesBackgroundColor(Color foundMatchesBackgroundColor) {
        this.foundMatchesBackgroundColor = foundMatchesBackgroundColor;
    }

    public Color getCurrentMatchBackgroundColor() {
        return currentMatchBackgroundColor;
    }

    public void setCurrentMatchBackgroundColor(Color currentMatchBackgroundColor) {
        this.currentMatchBackgroundColor = currentMatchBackgroundColor;
    }

    /**
     * Simple POJO class for search match.
     */
    public static class SearchMatch {

        private long position;
        private long length;

        public SearchMatch() {
        }

        public SearchMatch(long position, long length) {
            this.position = position;
            this.length = length;
        }

        public long getPosition() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }
    }
}
