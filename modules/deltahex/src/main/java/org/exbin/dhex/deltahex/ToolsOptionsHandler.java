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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.exbin.framework.api.XBApplication;
import org.exbin.dhex.deltahex.dialog.HexColorDialog;
import org.exbin.dhex.deltahex.panel.HexPanel;
import org.exbin.framework.editor.text.dialog.TextFontDialog;
import org.exbin.framework.gui.editor.api.XBEditorProvider;
import org.exbin.framework.gui.frame.api.GuiFrameModuleApi;
import org.exbin.framework.gui.utils.ActionUtils;
import org.exbin.dhex.deltahex.panel.HexColorPanelApi;

/**
 * Tools options action handler.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class ToolsOptionsHandler {

    private int metaMask;
    private ResourceBundle resourceBundle;

    private Action toolsSetFontAction;
    private Action toolsSetColorAction;

    private final XBEditorProvider editorProvider;
    private final XBApplication application;

    public ToolsOptionsHandler(XBApplication application, XBEditorProvider editorProvider) {
        this.application = application;
        this.editorProvider = editorProvider;
        resourceBundle = ActionUtils.getResourceBundleByClass(DeltaHexModule.class);
    }

    public void init() {
        toolsSetFontAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiFrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(GuiFrameModuleApi.class);
                TextFontDialog dialog = new TextFontDialog(frameModule.getFrame(), true);
                dialog.setIconImage(application.getApplicationIcon());
                dialog.setLocationRelativeTo(dialog.getParent());
                if (editorProvider instanceof HexPanel) {
                    ((HexPanel) editorProvider).showFontDialog(dialog);
                }
            }
        };
        ActionUtils.setupAction(toolsSetFontAction, resourceBundle, "toolsSetFontAction");
        toolsSetFontAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);

        toolsSetColorAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiFrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(GuiFrameModuleApi.class);
                HexColorPanelApi textColorPanelFrame = new HexColorPanelApi() {
                    @Override
                    public Color[] getCurrentTextColors() {
                        return ((HexPanel) editorProvider).getCurrentColors();
                    }

                    @Override
                    public Color[] getDefaultTextColors() {
                        return ((HexPanel) editorProvider).getDefaultColors();
                    }

                    @Override
                    public void setCurrentTextColors(Color[] colors) {
                        ((HexPanel) editorProvider).setCurrentColors(colors);
                    }
                };
                HexColorDialog dialog = new HexColorDialog(frameModule.getFrame(), textColorPanelFrame, true);
                dialog.setIconImage(application.getApplicationIcon());
                dialog.setLocationRelativeTo(dialog.getParent());
                dialog.showDialog();
            }
        };
        ActionUtils.setupAction(toolsSetColorAction, resourceBundle, "toolsSetColorAction");
        toolsSetColorAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);
    }

    public Action getToolsSetFontAction() {
        return toolsSetFontAction;
    }

    public Action getToolsSetColorAction() {
        return toolsSetColorAction;
    }
}
