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
package org.exbin.bined.operation.undo;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataCommandSequence;

/**
 * Undoable command sequence.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinaryDataUndoableCommandSequence extends BinaryDataCommandSequence, BinaryDataUndoHandler {

    /**
     * Performs specific number of redo commands.
     *
     * @param count count
     */
    void performRedo(int count);

    /**
     * Performs specific number of undo commands.
     *
     * @param count count
     */
    void performUndo(int count);

    /**
     * Performs executions or reverts to reqch synchronization position.
     *
     * @throws java.lang.Exception exception
     */
    void performSync() throws Exception;

    /**
     * Returns synchronization mark position.
     *
     * @return command position
     */
    long getSyncPosition();

    /**
     * Sets synchronization mark position.
     *
     * @param commandPosition command position
     */
    void setSyncPosition(long commandPosition);

    /**
     * Sets synchronization mark position to current command position.
     */
    void setSyncPosition();
}
