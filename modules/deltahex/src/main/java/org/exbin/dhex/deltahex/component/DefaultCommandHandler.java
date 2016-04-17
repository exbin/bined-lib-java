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
package org.exbin.dhex.deltahex.component;

import org.exbin.dhex.deltahex.HexadecimalCommandHandler;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.1.0 2016/04/17
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCommandHandler implements HexadecimalCommandHandler {

    private final Hexadecimal hexadecimal;

    public DefaultCommandHandler(Hexadecimal hexadecimal) {
        this.hexadecimal = hexadecimal;
    }

    @Override
    public void caretMoved() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyPressed(int keyValue) {
        if (hexadecimal.hasSelection()) {
            // TODO delete selection
        }
    }

}
