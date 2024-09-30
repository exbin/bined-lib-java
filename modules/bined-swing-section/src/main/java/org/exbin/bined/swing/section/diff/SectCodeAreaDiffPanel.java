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
package org.exbin.bined.swing.section.diff;

import java.awt.BorderLayout;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.EditMode;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.swing.section.SectCodeArea;

/**
 * Panel for difference comparison of two code areas.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectCodeAreaDiffPanel extends javax.swing.JPanel {

    private final SectCodeArea leftCodeArea;
    private final SectCodeArea rightCodeArea;
    private final DiffHighlightCodeAreaPainter leftPainter;
    private final DiffHighlightCodeAreaPainter rightPainter;
    private volatile boolean updatingScrolling = false;

    public SectCodeAreaDiffPanel() {
        initComponents();

        leftCodeArea = new SectCodeArea();
        rightCodeArea = new SectCodeArea();
        leftPainter = new DiffHighlightCodeAreaPainter(leftCodeArea);
        rightPainter = new DiffHighlightCodeAreaPainter(rightCodeArea);
        init();
    }

    private void init() {
        leftCodeArea.setEditMode(EditMode.READ_ONLY);
        rightCodeArea.setEditMode(EditMode.READ_ONLY);
        leftCodeArea.setPainter(leftPainter);
        rightCodeArea.setPainter(rightPainter);
        leftPanel.add(leftCodeArea, BorderLayout.CENTER);
        rightPanel.add(rightCodeArea, BorderLayout.CENTER);

        leftCodeArea.addScrollingListener(() -> {
            if (!updatingScrolling) {
                updatingScrolling = true;
                CodeAreaScrollPosition currentScrollPosition = rightCodeArea.getScrollPosition();
                CodeAreaScrollPosition scrollPosition = leftCodeArea.getScrollPosition();
                long maxRowPosition = rightCodeArea.getDataSize() / rightCodeArea.getMaxBytesPerRow();
                if (scrollPosition.getRowPosition() > maxRowPosition) {
                    if (currentScrollPosition.getRowPosition() >= maxRowPosition) {
                        updatingScrolling = false;
                        return;
                    }
                    scrollPosition.setRowPosition(maxRowPosition);
                }
                rightCodeArea.setScrollPosition(scrollPosition);
                updatingScrolling = false;
            }
        });

        rightCodeArea.addScrollingListener(() -> {
            if (!updatingScrolling) {
                updatingScrolling = true;
                CodeAreaScrollPosition currentScrollPosition = leftCodeArea.getScrollPosition();
                CodeAreaScrollPosition scrollPosition = rightCodeArea.getScrollPosition();
                long maxRowPosition = leftCodeArea.getDataSize() / leftCodeArea.getMaxBytesPerRow();
                if (scrollPosition.getRowPosition() > maxRowPosition) {
                    if (currentScrollPosition.getRowPosition() >= maxRowPosition) {
                        updatingScrolling = false;
                        return;
                    }
                    scrollPosition.setRowPosition(maxRowPosition);
                }
                leftCodeArea.setScrollPosition(scrollPosition);
                updatingScrolling = false;
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        leftPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);

        leftPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setLeftComponent(leftPanel);

        rightPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setRightComponent(rightPanel);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables

    @Nonnull
    public SectCodeArea getLeftCodeArea() {
        return leftCodeArea;
    }

    @Nonnull
    public SectCodeArea getRightCodeArea() {
        return rightCodeArea;
    }

    public void setLeftContentData(BinaryData contentData) {
        leftCodeArea.setContentData(contentData);
        rightPainter.setComparedData(contentData);
    }

    public void setRightContentData(BinaryData contentData) {
        rightCodeArea.setContentData(contentData);
        leftPainter.setComparedData(contentData);
    }
}