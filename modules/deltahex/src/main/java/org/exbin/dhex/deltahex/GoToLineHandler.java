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
package org.exbin.dhex.deltahex;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.exbin.framework.api.XBApplication;
import org.exbin.dhex.deltahex.dialog.GotoHexDialog;
import org.exbin.dhex.deltahex.panel.HexPanel;
import org.exbin.framework.gui.editor.api.XBEditorProvider;
import org.exbin.framework.gui.frame.api.GuiFrameModuleApi;
import org.exbin.framework.gui.utils.ActionUtils;

/**
 * Go to line handler.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class GoToLineHandler {

    private final XBEditorProvider editorProvider;
    private final XBApplication application;
    private final ResourceBundle resourceBundle;

    private int metaMask;

    private GotoHexDialog gotoDialog = null;

    private Action goToLineAction;

    public GoToLineHandler(XBApplication application, XBEditorProvider editorProvider) {
        this.application = application;
        this.editorProvider = editorProvider;
        resourceBundle = ActionUtils.getResourceBundleByClass(DeltaHexModule.class);
    }

    public void init() {
        metaMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        goToLineAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editorProvider instanceof HexPanel) {
                    HexPanel activePanel = (HexPanel) editorProvider;
                    initGotoDialog();
                    gotoDialog.setMaxLine(activePanel.getLineCount());
                    gotoDialog.setCharPos(1);
                    gotoDialog.setLocationRelativeTo(gotoDialog.getParent());
                    gotoDialog.setVisible(true);
                    if (gotoDialog.getDialogOption() == JOptionPane.OK_OPTION) {
                        activePanel.gotoLine(gotoDialog.getLine());
                        activePanel.gotoRelative(gotoDialog.getCharPos());
                    }
                }
            }
        };
        ActionUtils.setupAction(goToLineAction, resourceBundle, "goToLineAction");
        goToLineAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, metaMask));
        goToLineAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);
    }

    public Action getGoToLineAction() {
        return goToLineAction;
    }

    private void initGotoDialog() {
        if (gotoDialog == null) {
            GuiFrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(GuiFrameModuleApi.class);
            gotoDialog = new GotoHexDialog(frameModule.getFrame(), true);
            gotoDialog.setIconImage(application.getApplicationIcon());
        }
    }
}
