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
package org.exbin.bined.swing.basic;

import java.awt.Component;
import java.awt.event.KeyEvent;

/**
 * Tests for codeArea component.
 *
 * @version 0.2.0 2020/04/12
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaCommandEditTest {

    public CodeAreaCommandEditTest() {
    }

    private void emulateKeyTyped(Component component, int keyEvent, char keyChar) {
        component.dispatchEvent(new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, keyEvent, keyChar));
    }
}
