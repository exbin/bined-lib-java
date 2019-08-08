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
package org.exbin.bined.swing.extended.caret;

import java.awt.Graphics;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.extended.caret.CodeAreaCaretShape;
import org.exbin.bined.extended.caret.CodeAreaCaretType;
import org.exbin.bined.extended.caret.DefaultCodeAreaCaretShape;

/**
 * Support for cursor carets shapes.
 *
 * @version 0.2.0 2019/08/08
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultExtendedCodeAreaCaretsProfile implements ExtendedCodeAreaCaretsProfile {

    @Nonnull
    @Override
    public CodeAreaCaretShape identifyCaretShape(CodeAreaCaretType caretType) {
        switch (caretType) {
            case INSERT: {
                return DefaultCodeAreaCaretShape.LINE;
            }
            case OVERWRITE: {
                return DefaultCodeAreaCaretShape.FULL_BOX;
            }
            case SHADOW: {
                return DefaultCodeAreaCaretShape.DOTTED_BOX;
            }
            default:
                throw new IllegalStateException("Unexpected caret type: " + caretType.name());
        }
    }

    @Override
    public void paintCaret(Graphics g, int cursorX, int cursorY, int width, int height, CodeAreaCaretShape codeAreaCaretShape) {
        // TODO
        g.fillRect(cursorX, cursorY, width, height);

        if (codeAreaCaretShape instanceof DefaultCodeAreaCaretShape) {
            switch ((DefaultCodeAreaCaretShape) codeAreaCaretShape) {
                case FULL_BOX: {
                    break;
                }
                case LINE: {
                    break;
                }
                case DOTTED_BOX: {
                    break;
                }
            }
        }
    }
}
