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
import org.exbin.dhex.deltahex.dialog.FindHexDialog;
import org.exbin.dhex.deltahex.panel.HexPanel;
import org.exbin.framework.gui.editor.api.XBEditorProvider;
import org.exbin.framework.gui.frame.api.GuiFrameModuleApi;
import org.exbin.framework.gui.utils.ActionUtils;

/**
 * Find/replace handler.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class FindReplaceHandler {

    private final XBEditorProvider editorProvider;
    private final XBApplication application;
    private ResourceBundle resourceBundle;

    private int metaMask;

    private FindHexDialog findDialog = null;

    private Action editFindAction;
    private Action editFindAgainAction;
    private Action editReplaceAction;

    public FindReplaceHandler(XBApplication application, XBEditorProvider editorProvider) {
        this.application = application;
        this.editorProvider = editorProvider;
        resourceBundle = ActionUtils.getResourceBundleByClass(DeltaHexModule.class);
    }

    public void init() {
        metaMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        editFindAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initFindDialog();
                findDialog.setShallReplace(false);
                findDialog.setSelected();
                findDialog.setLocationRelativeTo(findDialog.getParent());
                findDialog.setVisible(true);
                if (findDialog.getDialogOption() == JOptionPane.OK_OPTION) {
                    if (editorProvider instanceof HexPanel) {
                        ((HexPanel) editorProvider).findText(findDialog);
                    }
                }
            }
        };
        ActionUtils.setupAction(editFindAction, resourceBundle, "editFindAction");
        editFindAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, metaMask));
        editFindAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);

        editFindAgainAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initFindDialog();
                findDialog.setShallReplace(false);
                if (editorProvider instanceof HexPanel) {
                    ((HexPanel) editorProvider).findText(findDialog);
                }
            }
        };
        ActionUtils.setupAction(editFindAgainAction, resourceBundle, "editFindAgainAction");
        editFindAgainAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));

        editReplaceAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initFindDialog();
                findDialog.setShallReplace(true);
                findDialog.setSelected();
                findDialog.setLocationRelativeTo(findDialog.getParent());
                findDialog.setVisible(true);
                if (findDialog.getDialogOption() == JOptionPane.OK_OPTION) {
                    if (editorProvider instanceof HexPanel) {
                        ((HexPanel) editorProvider).findText(findDialog);
                    }
                }
            }
        };
        ActionUtils.setupAction(editReplaceAction, resourceBundle, "editReplaceAction");
        editReplaceAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, metaMask));
        editReplaceAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);
    }

    public Action getEditFindAction() {
        return editFindAction;
    }

    public Action getEditFindAgainAction() {
        return editFindAgainAction;
    }

    public Action getEditReplaceAction() {
        return editReplaceAction;
    }

    public void initFindDialog() {
        if (findDialog == null) {
            GuiFrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(GuiFrameModuleApi.class);
            findDialog = new FindHexDialog(frameModule.getFrame(), true);
            findDialog.setIconImage(application.getApplicationIcon());
        }
    }
}
