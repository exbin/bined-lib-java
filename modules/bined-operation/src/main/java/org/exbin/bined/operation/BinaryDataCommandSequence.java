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
package org.exbin.bined.operation;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Operation interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinaryDataCommandSequence {

    /**
     * Executes given command and adds it at the end of the sequence replacing
     * scheduled commands if present.
     *
     * @param command command to execute
     */
    void execute(BinaryDataCommand command);

    /**
     * Adds command at the end of the sequence without executing it.
     *
     * @param command command to schedule
     */
    void schedule(BinaryDataCommand command);

    /**
     * Executes specific number of scheduled commands.
     *
     * @param count count of commands to execute
     */
    void executeScheduled(int count);

    /**
     * Returns list of commands.
     *
     * @return list of commands
     */
    @Nonnull
    List<BinaryDataCommand> getCommandList();

    /**
     * Returns position in sequence between already executed and scheduled
     * commands.
     *
     * @return position in sequence.
     */
    long getCommandPosition();

    /**
     * Resets / clears all commands in sequence.
     */
    void clear();

    /**
     * Registers command sequence listener.
     *
     * @param listener listener
     */
    void addCommandSequenceListener(BinaryDataCommandSequenceListener listener);

    /**
     * Unregisters command sequence listener.
     *
     * @param listener listener
     */
    void removeCommandSequenceListener(BinaryDataCommandSequenceListener listener);
}
