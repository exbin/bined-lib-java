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
package org.exbin.bined.operation;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for compound command.
 *
 * @version 0.2.0 2018/02/13
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinaryDataCompoundCommand extends BinaryDataCommand {

    /**
     * Appends command to the list of commands.
     *
     * @param command appended command
     */
    void appendCommand(BinaryDataCommand command);

    /**
     * Appends list of commands to the list of commands.
     *
     * @param commands appended commands
     */
    void appendCommands(Collection<BinaryDataCommand> commands);

    /**
     * Returns list of commands.
     *
     * @return list of commands
     */
    @Nonnull
    List<BinaryDataCommand> getCommands();

    /**
     * Returns true if compound command is empty.
     *
     * @return true if command is empty
     */
    boolean isEmpty();
}
