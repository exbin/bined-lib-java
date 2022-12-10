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

import java.util.Date;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Interface for code area command.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface BinaryDataCommand {

    /**
     * Returns caption as text.
     *
     * @return text caption
     */
    @Nonnull
    String getCaption();

    /**
     * Performs operation on given document.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    void execute() throws BinaryDataOperationException;

    /**
     * Performs update of command use information.
     */
    void use();

    /**
     * Performs redo on given document.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    void redo() throws BinaryDataOperationException;

    /**
     * Performs undo operation on given document.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    void undo() throws BinaryDataOperationException;

    /**
     * Returns true if command support undo operation.
     *
     * @return true if undo supported
     */
    boolean canUndo();

    /**
     * Disposes command.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    void dispose() throws BinaryDataOperationException;

    /**
     * Returns time of command execution.
     *
     * @return time
     */
    @Nonnull
    Optional<Date> getExecutionTime();
}
