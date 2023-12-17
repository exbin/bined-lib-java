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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataOperationException;

/**
 * Empty code area undo.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EmptyBinaryDataUndoHandler implements BinaryDataUndoHandler {

    @Override
    public boolean canRedo() {
        return false;
    }

    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public void doSync() throws BinaryDataOperationException {
    }

    @Override
    public void execute(BinaryDataCommand command) throws BinaryDataOperationException {
        try {
            command.execute();
        } catch (BinaryDataOperationException ex) {
            Logger.getLogger(EmptyBinaryDataUndoHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void addCommand(BinaryDataCommand command) {
        try {
            command.execute();
        } catch (BinaryDataOperationException ex) {
            Logger.getLogger(EmptyBinaryDataUndoHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Nonnull
    @Override
    public List<BinaryDataCommand> getCommandList() {
        return Collections.emptyList();
    }

    @Override
    public long getCommandPosition() {
        return 0;
    }

    @Override
    public long getMaximumUndo() {
        return 0;
    }

    @Override
    public long getSyncPoint() {
        return 0;
    }

    @Override
    public long getUndoMaximumSize() {
        return 0;
    }

    @Override
    public long getUsedSize() {
        throw new IllegalStateException();
    }

    @Override
    public void performUndo() throws BinaryDataOperationException {
        throw new IllegalStateException();
    }

    @Override
    public void performUndo(int count) throws BinaryDataOperationException {
        throw new IllegalStateException();
    }

    @Override
    public void performRedo() throws BinaryDataOperationException {
        throw new IllegalStateException();
    }

    @Override
    public void performRedo(int count) throws BinaryDataOperationException {
        throw new IllegalStateException();
    }

    @Override
    public void setCommandPosition(long targetPosition) throws BinaryDataOperationException {
        throw new IllegalStateException();
    }

    @Override
    public void setSyncPoint(long syncPoint) {
        throw new IllegalStateException();
    }

    @Override
    public void setSyncPoint() {
    }

    @Override
    public void addUndoUpdateListener(BinaryDataUndoUpdateListener listener) {
    }

    @Override
    public void removeUndoUpdateListener(BinaryDataUndoUpdateListener listener) {
    }
}
