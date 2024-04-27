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
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.operation.BinaryDataCommandSequenceListener;
import org.exbin.bined.operation.undo.BinaryDataUndoableCommandSequence;

/**
 * Undo handler for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaUndoHandler implements BinaryDataUndoableCommandSequence {

    private long undoMaximumCount;
    private long undoMaximumSize;
    private long usedSize;
    private long commandPosition;
    private long syncPointPosition = -1;
    private final List<BinaryDataCommand> commands = new ArrayList<>();
    private final CodeAreaCore codeArea;
    private final List<BinaryDataCommandSequenceListener> listeners = new ArrayList<>();

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
        CodeAreaUndoHandler.this.setSyncPosition(0);
    }

    /**
     * Adds new step into revert list.
     *
     * @param command command
     */
    @Override
    public void execute(BinaryDataCommand command) {
        command.execute();
        commandAdded(command);
    }

    @Override
    public void schedule(BinaryDataCommand command) {
        commandAdded(command);
    }

    @Override
    public void executeScheduled(int count) {
        performRedo(count);
    }

    private void commandAdded(BinaryDataCommand addedCommand) {
        // TODO: Check for undoOperationsMaximumCount & size
        while (commands.size() > commandPosition) {
            BinaryDataCommand command = commands.get((int) commandPosition);
            command.dispose();
            commands.remove(command);
        }
        commands.add(addedCommand);
        commandPosition++;

        undoUpdated();
    }

    @Override
    public void performUndo() {
        performUndoInt();
        undoUpdated();
    }

    private void performUndoInt() {
        commandPosition--;
        BinaryDataCommand command = commands.get((int) commandPosition);
        command.undo();
    }

    @Override
    public void performRedo() {
        performRedoInt();
        undoUpdated();
    }

    private void performRedoInt() {
        BinaryDataCommand command = commands.get((int) commandPosition);
        command.redo();
        commandPosition++;
    }

    @Override
    public void performUndo(int count) {
        if (commandPosition < count) {
            throw new IllegalArgumentException("Unable to perform " + count + " undo steps");
        }
        while (count > 0) {
            performUndoInt();
            count--;
        }
        undoUpdated();
    }

    @Override
    public void performRedo(int count) {
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
            command.dispose();
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

    public long getMaximumUndo() {
        return undoMaximumCount;
    }

    @Override
    public long getCommandPosition() {
        return commandPosition;
    }

    /**
     * Performs revert to sync point.
     */
    @Override
    public void performSync() {
        setCommandPosition(syncPointPosition);
    }

    public void setUndoMaxCount(long maxUndo) {
        this.undoMaximumCount = maxUndo;
    }

    public long getUndoMaximumSize() {
        return undoMaximumSize;
    }

    public void setUndoMaximumSize(long maxSize) {
        this.undoMaximumSize = maxSize;
    }

    public long getUsedSize() {
        return usedSize;
    }

    @Override
    public long getSyncPosition() {
        return syncPointPosition;
    }

    @Override
    public void setSyncPosition(long syncPoint) {
        this.syncPointPosition = syncPoint;
    }

    @Override
    public void setSyncPosition() {
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
     */
    public void setCommandPosition(long targetPosition) {
        if (targetPosition < commandPosition) {
            performUndo((int) (commandPosition - targetPosition));
        } else if (targetPosition > commandPosition) {
            performRedo((int) (targetPosition - commandPosition));
        }
    }

    private void undoUpdated() {
        codeArea.notifyDataChanged();
        listeners.forEach(BinaryDataCommandSequenceListener::sequenceChanged);
    }

    @Override
    public void addCommandSequenceListener(BinaryDataCommandSequenceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCommandSequenceListener(BinaryDataCommandSequenceListener listener) {
        listeners.remove(listener);
    }
}
