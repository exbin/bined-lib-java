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
package org.exbin.bined.swt.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.DefaultBoundedRangeModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.swt.CodeAreaSwtControl;

/**
 * Default scroll pane for binary component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaScrollPane extends ScrolledComposite {

    private volatile boolean scrollingByUser = false;
    private volatile boolean scrollingUpdate = false;

    @Nonnull
    private final VerticalScrollBarModel verticalScrollBarModel = new VerticalScrollBarModel();
    @Nonnull
    private final HorizontalScrollBarModel horizontalScrollBarModel = new HorizontalScrollBarModel();
    @Nonnull
    private final BasicCodeAreaMetrics metrics;
    @Nonnull
    private final BasicCodeAreaStructure structure;
    @Nonnull
    private final BasicCodeAreaScrolling scrolling;
    @Nonnull
    private final BasicCodeAreaDimensions dimensions;
    @Nonnull
    private final CodeAreaSwtControl control;

    public DefaultCodeAreaScrollPane(Composite parent, CodeAreaSwtControl control, BasicCodeAreaMetrics metrics, BasicCodeAreaStructure structure, BasicCodeAreaDimensions dimensions, BasicCodeAreaScrolling scrolling) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        this.control = control;
        this.metrics = metrics;
        this.structure = structure;
        this.dimensions = dimensions;
        this.scrolling = scrolling;
        init();
    }

    private void init() {
//        setBorder(new EmptyBorder(0, 0, 0, 0));
//        setIgnoreRepaint(true);
//        setOpaque(false);
//        setInheritsPopupMenu(true);
//        setViewportBorder(null);
        // TODO: Try to use setColumnHeader and setRowHeader
        ScrollBar verticalScrollBar = getVerticalBar();
//        verticalScrollBar.setIgnoreRepaint(true);
        verticalScrollBar.addSelectionListener(new VerticalAdjustmentListener());
//        verticalScrollBar.setModel(verticalScrollBarModel);
//        verticalScrollBar.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON1) {
//                    scrollingByUser = true;
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON1) {
//                    scrollingByUser = false;
//                }
//            }
//        });

        ScrollBar horizontalScrollBar = getHorizontalBar();
//        horizontalScrollBar.setIgnoreRepaint(true);
        horizontalScrollBar.addSelectionListener(new HorizontalAdjustmentListener());
//        horizontalScrollBar.setModel(horizontalScrollBarModel);
//        horizontalScrollBar.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON1) {
//                    scrollingByUser = true;
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON1) {
//                    scrollingByUser = false;
//                }
//            }
//        });
    }

//    @Nonnull
//    @Override
//    public JScrollBar createVerticalScrollBar() {
//        return new JScrollPane.ScrollBar(JScrollBar.VERTICAL) {
//            @Override
//            public void setValue(int value) {
//                if (!scrollingUpdate && !scrollingByUser) {
//                    scrollingByUser = true;
//                    super.setValue(value);
//                    scrollingByUser = false;
//                } else {
//                    super.setValue(value);
//                }
//            }
//        };
//    }
//
//    @Nonnull
//    @Override
//    public JScrollBar createHorizontalScrollBar() {
//        return new JScrollPane.ScrollBar(JScrollBar.HORIZONTAL) {
//            @Override
//            public void setValue(int value) {
//                if (!scrollingUpdate && !scrollingByUser) {
//                    scrollingByUser = true;
//                    super.setValue(value);
//                    scrollingByUser = false;
//                } else {
//                    super.setValue(value);
//                }
//            }
//        };
//    }

    public void horizontalExtentChanged() {
        horizontalScrollBarModel.notifyChanged();
    }

    public void verticalExtentChanged() {
        verticalScrollBarModel.notifyChanged();
    }

    public void updateScrollBars(int verticalScrollValue, int horizontalScrollValue) {
        scrollingUpdate = true;
        getVerticalBar().setSelection(verticalScrollValue);
        getHorizontalBar().setSelection(horizontalScrollValue);
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

    private class VerticalAdjustmentListener implements SelectionListener {

        private boolean wasAdjusting = false;

        public VerticalAdjustmentListener() {
        }

        @Override
        public void widgetSelected(@Nullable SelectionEvent se) {
            if (se == null || scrollingUpdate) {
                return;
            }

//            if (!se.getValueIsAdjusting()) {
//                if (wasAdjusting) {
//                    wasAdjusting = false;
//                } else {
//                    // Override scrolling up/down by scrollbar buttons with direct operation
//                    int lastValue = scrolling.getLastVerticalScrollingValue();
//                    if (scrollingByUser && scrolling.getScrollBarVerticalScale() == ScrollBarVerticalScale.SCALED) {
//                        if (lastValue != -1) {
//                            if (se.getValue() == lastValue - 1 || (lastValue == 0 && e.getValue() == 0)) {
//                                SwingUtilities.invokeLater(() -> {
//                                    scrolling.performScrolling(ScrollingDirection.UP, dimensions.getRowsPerPage(), structure.getRowsPerDocument());
//                                    control.updateScrollPosition(scrolling.getScrollPosition());
//                                });
//                                return;
//                            }
//
//                            int maxScroll = verticalScrollBarModel.getMaximum() - verticalScrollBarModel.getExtent();
//                            if (se.getValue() == lastValue + 1 || (lastValue == maxScroll && e.getValue() == maxScroll)) {
//                                SwingUtilities.invokeLater(() -> {
//                                    scrolling.performScrolling(ScrollingDirection.DOWN, dimensions.getRowsPerPage(), structure.getRowsPerDocument());
//                                    control.updateScrollPosition(scrolling.getScrollPosition());
//                                });
//                                return;
//                            }
//                        }
//                    }
//                }
//            } else {
//                wasAdjusting = true;
//            }

            if (scrollingByUser) {
                int scrollBarValue = getVerticalBar().getSelection();
                int maxValue = Integer.MAX_VALUE - getVerticalBar().getSelection();
                long rowsPerDocumentToLastPage = structure.getRowsPerDocument() - dimensions.getRowsPerRect();
                scrolling.updateVerticalScrollBarValue(scrollBarValue, metrics.getRowHeight(), maxValue, rowsPerDocumentToLastPage);
                control.updateScrollPosition(scrolling.getScrollPosition());
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent se) {
        }
    }

    private class HorizontalAdjustmentListener implements SelectionListener {

        public HorizontalAdjustmentListener() {
        }

        @Override
        public void widgetSelected(@Nullable SelectionEvent se) {
            if (se == null || !scrollingByUser || scrollingUpdate) {
                return;
            }

            int scrollBarValue = getHorizontalBar().getSelection();
            scrolling.updateHorizontalScrollBarValue(scrollBarValue, metrics.getCharacterWidth());
            control.updateScrollPosition(scrolling.getScrollPosition());
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent se) {
        }
    }
}
