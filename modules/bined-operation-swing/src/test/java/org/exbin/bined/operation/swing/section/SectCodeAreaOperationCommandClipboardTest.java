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
package org.exbin.bined.operation.swing.section;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.swing.CodeAreaUndoRedo;
import org.exbin.bined.swing.basic.CodeAreaCommandClipboardTest;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Tests clipboard actions for SectCodeArea command component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SectCodeAreaOperationCommandClipboardTest extends CodeAreaCommandClipboardTest {

    public SectCodeAreaOperationCommandClipboardTest() {
    }

    @Nonnull
    @Override
    public CodeAreaCore createCodeArea() {
        SectCodeArea codeArea = new SectCodeArea();
        codeArea.setCommandHandler(new CodeAreaOperationCommandHandler(codeArea, new CodeAreaUndoRedo(codeArea)));
        return codeArea;
    }
}
