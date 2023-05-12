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
package org.exbin.bined.swing.example;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.exbin.bined.swing.example.panel.CursorPanelEx;
import org.exbin.bined.swing.example.panel.LayoutPanelEx;
import org.exbin.bined.swing.example.panel.ModePanelEx;
import org.exbin.bined.swing.example.panel.ScrollingPanelEx;
import org.exbin.bined.swing.example.panel.StatePanelEx;
import org.exbin.bined.swing.example.panel.ThemePanelEx;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.diff.ExtCodeAreaDiffPanel;

/**
 * Binary difference component example panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdExampleDiffPanel extends javax.swing.JPanel {

    private ExtCodeAreaDiffPanel diffPanel;
    private final Map<JScrollPane, JPanel> tabMap = new HashMap<>();
    private JScrollPane activeTab;

    public BinEdExampleDiffPanel() {
        initComponents();
    }

    public void setDiffPanel(final ExtCodeAreaDiffPanel diffPanel) {
        this.diffPanel = diffPanel;
        splitPane.setRightComponent(diffPanel);

        ExtCodeArea leftCodeArea = diffPanel.getLeftCodeArea();

        ModePanelEx modePanel = new ModePanelEx();
        modePanel.setCodeArea(leftCodeArea);
        StatePanelEx statePanel = new StatePanelEx();
        statePanel.setCodeArea(leftCodeArea);
        LayoutPanelEx layoutPanel = new LayoutPanelEx();
        layoutPanel.setCodeArea(leftCodeArea);
        ThemePanelEx themePanel = new ThemePanelEx();
        themePanel.setCodeArea(leftCodeArea);
        ScrollingPanelEx scrollingPanel = new ScrollingPanelEx();
        scrollingPanel.setCodeArea(leftCodeArea);
        CursorPanelEx cursorPanel = new CursorPanelEx();
        cursorPanel.setCodeArea(leftCodeArea);

        tabMap.put(modeScrollPane, modePanel);
        tabMap.put(stateScrollPane, statePanel);
        tabMap.put(layoutScrollPane, layoutPanel);
        tabMap.put(themeScrollPane, themePanel);
        tabMap.put(scrollingScrollPane, scrollingPanel);
        tabMap.put(cursorScrollPane, cursorPanel);

        activeTab = modeScrollPane;
        modeScrollPane.setViewportView(modePanel);
        splitPane.setDividerLocation(330);
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
        tabbedPane = new javax.swing.JTabbedPane();
        modeScrollPane = new javax.swing.JScrollPane();
        stateScrollPane = new javax.swing.JScrollPane();
        layoutScrollPane = new javax.swing.JScrollPane();
        themeScrollPane = new javax.swing.JScrollPane();
        scrollingScrollPane = new javax.swing.JScrollPane();
        cursorScrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        tabbedPane.addTab("Mode", modeScrollPane);
        tabbedPane.addTab("State", stateScrollPane);
        tabbedPane.addTab("Layout", layoutScrollPane);
        tabbedPane.addTab("Theme", themeScrollPane);
        tabbedPane.addTab("Scrolling", scrollingScrollPane);
        tabbedPane.addTab("Cursor", cursorScrollPane);

        splitPane.setLeftComponent(tabbedPane);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        Component tab = tabbedPane.getSelectedComponent();
        if (tab != null && tab != activeTab && !tabMap.isEmpty()) {
            if (activeTab != null) {
                ((JScrollPane) activeTab).remove(tabMap.get(activeTab));
            }

            JPanel tabPanel = tabMap.get((JScrollPane) tab);
            ((JScrollPane) tab).setViewportView(tabPanel);
            activeTab = (JScrollPane) tab;
        }
    }//GEN-LAST:event_tabbedPaneStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane cursorScrollPane;
    private javax.swing.JScrollPane layoutScrollPane;
    private javax.swing.JScrollPane modeScrollPane;
    private javax.swing.JScrollPane scrollingScrollPane;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JScrollPane stateScrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JScrollPane themeScrollPane;
    // End of variables declaration//GEN-END:variables

}