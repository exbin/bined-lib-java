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
package org.exbin.bined.operation.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Undo handler for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaUndoHandler implements BinaryDataUndoHandler {

    private long undoMaximumCount;
    private long undoMaximumSize;
    private long usedSize;
    private long commandPosition;
    private long syncPointPosition = -1;
    private final List<BinaryDataCommand> commands = new ArrayList<>();
    private final CodeAreaCore codeArea;
    private final List<BinaryDataUndoUpdateListener> listeners = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param codeArea code area component
     */
    public CodeAreaUndoHandler(CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        undoMaximumCount = 1024;
        undoMaximumSize = 65535;
        init();
    }

    private void init() {
        usedSize = 0;
        commandPosition = 0;
        setSyncPoint(0);
    }

    /**
     * Adds new step into revert list.
     *
     * @param command command
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void execute(BinaryDataCommand command) throws BinaryDataOperationException {
        command.execute();
        commandAdded(command);
    }

    @Override
    public void addCommand(BinaryDataCommand command) {
        command.use();
        commandAdded(command);
    }

    private void commandAdded(BinaryDataCommand addedCommand) {
        // TODO: Check for undoOperationsMaximumCount & size
        while (commands.size() > commandPosition) {
            BinaryDataCommand command = commands.get((int) commandPosition);
            try {
                command.dispose();
            } catch (BinaryDataOperationException ex) {
                Logger.getLogger(CodeAreaUndoHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            commands.remove(command);
        }
        commands.add(addedCommand);
        commandPosition++;

        undoUpdated();
        listeners.forEach((listener) -> {
            listener.undoCommandAdded(addedCommand);
        });
    }

    /**
     * Performs single undo step.
     *
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performUndo() throws BinaryDataOperationException {
        performUndoInt();
        undoUpdated();
    }

    private void performUndoInt() throws BinaryDataOperationException {
        commandPosition--;
        BinaryDataCommand command = commands.get((int) commandPosition);
        command.undo();
    }

    /**
     * Performs single redo step.
     *
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performRedo() throws BinaryDataOperationException {
        performRedoInt();
        undoUpdated();
    }

    private void performRedoInt() throws BinaryDataOperationException {
        BinaryDataCommand command = commands.get((int) commandPosition);
        command.redo();
        commandPosition++;
    }

    /**
     * Performs multiple undo steps.
     *
     * @param count count of steps
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performUndo(int count) throws BinaryDataOperationException {
        if (commandPosition < count) {
            throw new IllegalArgumentException("Unable to perform " + count + " undo steps");
        }
        while (count > 0) {
            performUndoInt();
            count--;
        }
        undoUpdated();
    }

    /**
     * Performs multiple redo steps.
     *
     * @param count count of steps
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performRedo(int count) throws BinaryDataOperationException {
        if (commands.size() - commandPosition < count) {
            throw new IllegalArgumentException("Unable to perform " + count + " redo steps");
        }
        while (count > 0) {
            performRedoInt();
            count--;
        }
        undoUpdated();
    }

    @Override
    public void clear() {
        commands.forEach((command) -> {
            try {
                command.dispose();
            } catch (BinaryDataOperationException ex) {
                Logger.getLogger(CodeAreaUndoHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        commands.clear();
        init();
        undoUpdated();
    }

    @Override
    public boolean canUndo() {
        return commandPosition > 0;
    }

    @Override
    public boolean canRedo() {
        return commands.size() > commandPosition;
    }

    @Override
    public long getMaximumUndo() {
        return undoMaximumCount;
    }

    @Override
    public long getCommandPosition() {
        return commandPosition;
    }

    /**
     * Performs revert to sync point.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void doSync() throws BinaryDataOperationException {
        setCommandPosition(syncPointPosition);
    }

    public void setUndoMaxCount(long maxUndo) {
        this.undoMaximumCount = maxUndo;
    }

    @Override
    public long getUndoMaximumSize() {
        return undoMaximumSize;
    }

    public void setUndoMaximumSize(long maxSize) {
        this.undoMaximumSize = maxSize;
    }

    @Override
    public long getUsedSize() {
        return usedSize;
    }

    @Override
    public long getSyncPoint() {
        return syncPointPosition;
    }

    @Override
    public void setSyncPoint(long syncPoint) {
        this.syncPointPosition = syncPoint;
    }

    @Override
    public void setSyncPoint() {
        this.syncPointPosition = commandPosition;
    }

    @Nonnull
    @Override
    public List<BinaryDataCommand> getCommandList() {
        return commands;
    }

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void setCommandPosition(long targetPosition) throws BinaryDataOperationException {
        if (targetPosition < commandPosition) {
            performUndo((int) (commandPosition - targetPosition));
        } else if (targetPosition > commandPosition) {
            performRedo((int) (targetPosition - commandPosition));
        }
    }

    private void undoUpdated() {
        codeArea.notifyDataChanged();
        listeners.forEach((listener) -> {
            listener.undoCommandPositionChanged();
        });
    }

    @Override
    public void addUndoUpdateListener(BinaryDataUndoUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeUndoUpdateListener(BinaryDataUndoUpdateListener listener) {
        listeners.remove(listener);
    }
}
