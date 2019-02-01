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
package org.exbin.bined.swing.extended;

import javax.annotation.concurrent.Immutable;

/**
 * Plain object for number of characters with support for half character.
 *
 * @version 0.2.0 2019/02/01
 * @author ExBin Project (https://exbin.org)
 */
@Immutable
public class CharactersNumAndHalf {

    private final int characters;
    private final boolean half;

    public CharactersNumAndHalf(int characters, boolean half) {
        this.characters = characters;
        this.half = half;
    }

    public int getCharacters() {
        return characters;
    }

    public boolean isHalf() {
        return half;
    }
}
