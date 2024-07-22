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
package org.exbin.bined.swing.section;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.section.SectionCodeAreaStructure;
import org.exbin.bined.swing.CodeAreaSwingControl;
import org.exbin.bined.swing.basic.BasicCodeAreaMetrics;

/**
 * Section scroll pane for binary component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectionCodeAreaScrollPane extends JScrollPane {

    private volatile boolean scrollingByUser = false;
    private volatile boolean scrollingUpdate = false;

    @Nonnull
    private final VerticalScrollBarModel verticalScrollBarModel = new VerticalScrollBarModel();
    @Nonnull
    private final HorizontalScrollBarModel horizontalScrollBarModel = new HorizontalScrollBarModel();

    @Nonnull
    private final CodeAreaSwingControl control;
    @Nonnull
    private final BasicCodeAreaMetrics metrics;
    @Nonnull
    private final SectionCodeAreaStructure structure;
    @Nonnull
    private final SectionCodeAreaScrolling scrolling;
    @Nonnull
    private final SectionCodeAreaDimensions dimensions;

    public SectionCodeAreaScrollPane(CodeAreaSwingControl control, BasicCodeAreaMetrics metrics, SectionCodeAreaStructure structure, SectionCodeAreaScrolling scrolling, SectionCodeAreaDimensions dimensions) {
        this.control = control;
        this.metrics = metrics;
        this.structure = structure;
        this.dimensions = dimensions;
        this.scrolling = scrolling;
        init();
    }

    private void init() {
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setIgnoreRepaint(true);
        setOpaque(false);
        setInheritsPopupMenu(true);
        setViewportBorder(null);
        // TODO: Try to use setColumnHeader and setRowHeader

        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
        verticalScrollBar.setModel(verticalScrollBarModel);
        verticalScrollBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    scrollingByUser = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    scrollingByUser = false;
                }
            }
        });
        horizontalScrollBar.setIgnoreRepaint(true);
        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
        horizontalScrollBar.setModel(horizontalScrollBarModel);
        horizontalScrollBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    scrollingByUser = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    scrollingByUser = false;
                }
            }
        });
    }

    @Nonnull
    @Override
    public JScrollBar createVerticalScrollBar() {
        return new JScrollPane.ScrollBar(JScrollBar.VERTICAL) {
            @Override
            public void setValue(int value) {
                if (!scrollingUpdate && !scrollingByUser) {
                    scrollingByUser = true;
                    super.setValue(value);
                    scrollingByUser = false;
                } else {
                    super.setValue(value);
                }
            }
        };
    }

    @Nonnull
    @Override
    public JScrollBar createHorizontalScrollBar() {
        return new JScrollPane.ScrollBar(JScrollBar.HORIZONTAL) {
            @Override
            public void setValue(int value) {
                if (!scrollingUpdate && !scrollingByUser) {
                    scrollingByUser = true;
                    super.setValue(value);
                    scrollingByUser = false;
                } else {
                    super.setValue(value);
                }
            }
        };
    }

    public void horizontalExtentChanged() {
        horizontalScrollBarModel.notifyChanged();
    }

    public void verticalExtentChanged() {
        verticalScrollBarModel.notifyChanged();
    }

    public void updateScrollBars(int verticalScrollValue, int horizontalScrollValue) {
        scrollingUpdate = true;
        verticalScrollBar.setValue(verticalScrollValue);
        horizontalScrollBar.setValue(horizontalScrollValue);
        scrollingUpdate = false;
    }

    private class VerticalScrollBarModel extends DefaultBoundedRangeModel {

        private volatile int depth = 0;

        public VerticalScrollBarModel() {
            super();
        }

        @Override
        public int getExtent() {
            return super.getExtent() - scrolling.getVerticalExtentDifference();
        }

        @Override
        public int getMaximum() {
            return super.getMaximum() - scrolling.getVerticalExtentDifference();
        }

        @Override
        public void setRangeProperties(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {
            super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);
            if (!scrollingUpdate && newValue == scrolling.getLastVerticalScrollingValue() && (newValue <= newMin || newValue >= newMax - newExtent)) {
                // We still want to report change when scrolling up on corners for big files
                depth++;
                try {
                    if (depth < 5) {
                        fireStateChanged();
                    }
                } finally {
                    depth--;
                }
            }
        }

        @Override
        public void setValue(int n) {
            // Keeps previous value - depends on that scrolling by button calls this method
            scrolling.setLastVerticalScrollingValue(getValue());
            super.setValue(n);
        }

        public void notifyChanged() {
            fireStateChanged();
        }
    }

    private class HorizontalScrollBarModel extends DefaultBoundedRangeModel {

        public HorizontalScrollBarModel() {
            super();
        }

        @Override
        public int getExtent() {
            return super.getExtent() - scrolling.getHorizontalExtentDifference();
        }

        @Override
        public int getMaximum() {
            return super.getMaximum() - scrolling.getHorizontalExtentDifference();
        }

        public void notifyChanged() {
            fireStateChanged();
        }
    }

    private class VerticalAdjustmentListener implements AdjustmentListener {

        private boolean wasAdjusting = false;

        public VerticalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(@Nullable AdjustmentEvent e) {
            if (e == null || scrollingUpdate) {
                return;
            }

            if (!e.getValueIsAdjusting()) {
                if (wasAdjusting) {
                    wasAdjusting = false;
                } else {
                    // Override scrolling up/down by scrollbar buttons with direct operation
                    int lastValue = scrolling.getLastVerticalScrollingValue();
                    if (scrollingByUser && scrolling.getScrollBarVerticalScale() == ScrollBarVerticalScale.SCALED) {
                        if (lastValue != -1) {
                            if (e.getValue() == lastValue - 1 || (lastValue == 0 && e.getValue() == 0)) {
                                SwingUtilities.invokeLater(() -> {
                                    scrolling.performScrolling(ScrollingDirection.UP, dimensions.getRowsPerPage(), structure.getRowsPerDocument());
                                    control.updateScrollPosition(scrolling.getScrollPosition());
                                });
                                return;
                            }

                            int maxScroll = verticalScrollBarModel.getMaximum() - verticalScrollBarModel.getExtent();
                            if (e.getValue() == lastValue + 1 || (lastValue == maxScroll && e.getValue() == maxScroll)) {
                                SwingUtilities.invokeLater(() -> {
                                    scrolling.performScrolling(ScrollingDirection.DOWN, dimensions.getRowsPerPage(), structure.getRowsPerDocument());
                                    control.updateScrollPosition(scrolling.getScrollPosition());
                                });
                                return;
                            }
                        }
                    }
                }
            } else {
                wasAdjusting = true;
            }

            if (scrollingByUser) {
                int scrollBarValue = verticalScrollBar.getValue();
                int maxValue = Integer.MAX_VALUE - verticalScrollBar.getVisibleAmount();
                long rowsPerDocumentToLastPage = structure.getRowsPerDocument() - dimensions.getRowsPerRect();
                scrolling.updateVerticalScrollBarValue(scrollBarValue, metrics.getRowHeight(), maxValue, rowsPerDocumentToLastPage);
                control.updateScrollPosition(scrolling.getScrollPosition());
            }
        }
    }

    private class HorizontalAdjustmentListener implements AdjustmentListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void adjustmentValueChanged(@Nullable AdjustmentEvent e) {
            if (e == null || !scrollingByUser || scrollingUpdate) {
                return;
            }

            int scrollBarValue = horizontalScrollBar.getValue();
            scrolling.updateHorizontalScrollBarValue(scrollBarValue, metrics.getCharacterWidth());
            control.updateScrollPosition(scrolling.getScrollPosition());
        }
    }
}
