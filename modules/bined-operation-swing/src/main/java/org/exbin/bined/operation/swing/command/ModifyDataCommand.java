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
package org.exbin.bined.operation.swing.command;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.swing.ModifyDataOperation;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.auxiliary.binary_data.BinaryData;

/**
 * Command for modifying data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ModifyDataCommand extends OpCodeAreaCommand {

    public ModifyDataCommand(CodeAreaCore codeArea, long position, BinaryData data) {
        super(codeArea);
        super.setOperation(new ModifyDataOperation(codeArea, position, data));
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_MODIFIED;
    }
}
