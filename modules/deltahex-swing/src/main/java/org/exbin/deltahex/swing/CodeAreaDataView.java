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
package org.exbin.deltahex.swing;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JComponent;

/**
 * Hexadecimal viewer/editor inner data view component.
 *
 * @version 0.2.0 2017/04/02
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaDataView extends JComponent {

    private final CodeArea codeArea;

    public CodeAreaDataView(CodeArea codeArea) {
        this.codeArea = codeArea;
        init();
    }

    private void init() {
//        verticalScrollBar = new JScrollBar(Scrollbar.VERTICAL);
//        verticalScrollBar.setVisible(false);
//        verticalScrollBar.setIgnoreRepaint(true);
//        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
//        add(verticalScrollBar);
//        horizontalScrollBar = new JScrollBar(Scrollbar.HORIZONTAL);
//        horizontalScrollBar.setIgnoreRepaint(true);
//        horizontalScrollBar.setVisible(false);
//        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
//        add(horizontalScrollBar);
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int scrollBarValue = verticalScrollBar.getValue();
            if (scrollPosition.verticalMaxMode) {
                int maxValue = Integer.MAX_VALUE - verticalScrollBar.getVisibleAmount();
                long lines = ((data.getDataSize() + scrollPosition.lineByteShift) / paintDataCache.bytesPerLine) - paintDataCache.linesPerRect + 1;
                long targetLine;
                if (scrollBarValue > 0 && lines > maxValue / scrollBarValue) {
                    targetLine = scrollBarValue * (lines / maxValue);
                    long rest = lines % maxValue;
                    targetLine += (rest * scrollBarValue) / maxValue;
                } else {
                    targetLine = (scrollBarValue * lines) / Integer.MAX_VALUE;
                }
                scrollPosition.scrollLinePosition = targetLine;
                if (verticalScrollMode != VerticalScrollMode.PER_LINE) {
                    scrollPosition.scrollLineOffset = 0;
                }
            } else if (verticalScrollMode == VerticalScrollMode.PER_LINE) {
                scrollPosition.scrollLinePosition = scrollBarValue;
            } else {
                scrollPosition.scrollLinePosition = scrollBarValue / paintDataCache.lineHeight;
                scrollPosition.scrollLineOffset = scrollBarValue % paintDataCache.lineHeight;
            }

            repaint();
            notifyScrolled();
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (horizontalScrollMode == HorizontalScrollMode.PER_CHAR) {
                scrollPosition.scrollCharPosition = horizontalScrollBar.getValue();
            } else {
                scrollPosition.scrollCharPosition = horizontalScrollBar.getValue() / paintDataCache.charWidth;
                scrollPosition.scrollCharOffset = horizontalScrollBar.getValue() % paintDataCache.charWidth;
            }
            repaint();
            notifyScrolled();
        }
    }
}
