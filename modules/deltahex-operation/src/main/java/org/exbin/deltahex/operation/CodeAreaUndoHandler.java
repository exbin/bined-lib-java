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
package org.exbin.deltahex.operation;

import java.util.ArrayList;
import java.util.List;
import org.exbin.deltahex.CodeArea;
import org.exbin.xbup.operation.Command;
import org.exbin.xbup.operation.undo.XBUndoHandler;
import org.exbin.xbup.operation.undo.XBUndoUpdateListener;

/**
 * Undo handler for hexadecimal editor.
 *
 * @version 0.1.0 2016/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaUndoHandler implements XBUndoHandler {

    private long undoMaximumCount;
    private long undoMaximumSize;
    private long usedSize;
    private long commandPosition;
    private long syncPointPosition = -1;
    private final List<Command> commandList;
    private final CodeArea codeArea;
    private final List<XBUndoUpdateListener> listeners = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param codeArea hexadecimal component
     */
    public CodeAreaUndoHandler(CodeArea codeArea) {
        this.codeArea = codeArea;
        undoMaximumCount = 1024;
        undoMaximumSize = 65535;
        commandList = new ArrayList<>();
        init();
    }

    private void init() {
        usedSize = 0;
        commandPosition = 0;
        setSyncPoint(0);
        commandList.clear();
    }

    /**
     * Adds new step into revert list.
     *
     * @param command command
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void execute(Command command) throws Exception {
        command.execute();

        commandAdded(command);
    }

    @Override
    public void addCommand(Command command) {
        command.use();
        commandAdded(command);
    }

    private void commandAdded(Command command) {
        // TODO: Check for undoOperationsMaximumCount & size
        while (commandList.size() > commandPosition) {
            commandList.remove((int) commandPosition);
        }
        commandList.add(command);
        commandPosition++;

        undoUpdated();
        for (XBUndoUpdateListener listener : listeners) {
            listener.undoCommandAdded(command);
        }
    }

    /**
     * Performs single undo step.
     *
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void performUndo() throws Exception {
        performUndoInt();
        undoUpdated();
    }

    private void performUndoInt() throws Exception {
        commandPosition--;
        Command command = commandList.get((int) commandPosition);
        command.undo();
    }

    /**
     * Performs single redo step.
     *
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void performRedo() throws Exception {
        performRedoInt();
        undoUpdated();
    }

    private void performRedoInt() throws Exception {
        Command command = commandList.get((int) commandPosition);
        command.redo();
        commandPosition++;
    }

    /**
     * Performs multiple undo step.
     *
     * @param count count of steps
     * @throws Exception if commands throws it
     */
    @Override
    public void performUndo(int count) throws Exception {
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
     * Performs multiple redo step.
     *
     * @param count count of steps
     * @throws Exception if commands throws it
     */
    @Override
    public void performRedo(int count) throws Exception {
        if (commandList.size() - commandPosition < count) {
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
        init();
    }

    @Override
    public boolean canUndo() {
        return commandPosition > 0;
    }

    @Override
    public boolean canRedo() {
        return commandList.size() > commandPosition;
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
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void doSync() throws Exception {
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

    @Override
    public List<Command> getCommandList() {
        return commandList;
    }

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void setCommandPosition(long targetPosition) throws Exception {
        if (targetPosition < commandPosition) {
            performUndo((int) (commandPosition - targetPosition));
        } else if (targetPosition > commandPosition) {
            performRedo((int) (targetPosition - commandPosition));
        }
    }

    private void undoUpdated() {
        for (XBUndoUpdateListener listener : listeners) {
            listener.undoCommandPositionChanged();
        }
    }

    @Override
    public void addUndoUpdateListener(XBUndoUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeUndoUpdateListener(XBUndoUpdateListener listener) {
        listeners.remove(listener);
    }
}
