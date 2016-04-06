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
package org.exbin.dhex.framework.deltahex;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.exbin.framework.api.XBApplication;
import org.exbin.dhex.framework.deltahex.panel.HexPanel;
import org.exbin.framework.gui.editor.api.XBEditorProvider;
import org.exbin.framework.gui.utils.ActionUtils;

/**
 * Word wrapping handler.
 *
 * @version 0.1.0 2016/04/03
 * @author ExBin Project (http://exbin.org)
 */
public class WordWrappingHandler {

    private final XBEditorProvider editorProvider;
    private final XBApplication application;
    private final ResourceBundle resourceBundle;

    private Action viewWordWrapAction;

    public WordWrappingHandler(XBApplication application, XBEditorProvider editorProvider) {
        this.application = application;
        this.editorProvider = editorProvider;
        resourceBundle = ActionUtils.getResourceBundleByClass(DeltaHexModule.class);
    }

    public void init() {
        viewWordWrapAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editorProvider instanceof HexPanel) {
                    boolean lineWraping = ((HexPanel) editorProvider).changeLineWrap();
                    viewWordWrapAction.putValue(Action.SELECTED_KEY, lineWraping);
                }
            }
        };
        ActionUtils.setupAction(viewWordWrapAction, resourceBundle, "viewWordWrapAction");
        viewWordWrapAction.putValue(ActionUtils.ACTION_TYPE, ActionUtils.ActionType.CHECK);
    }

    public Action getViewWordWrapAction() {
        return viewWordWrapAction;
    }
}
