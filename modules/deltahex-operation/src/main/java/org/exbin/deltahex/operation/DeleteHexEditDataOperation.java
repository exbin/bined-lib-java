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

import org.exbin.deltahex.Hexadecimal;
import org.exbin.deltahex.delta.MemoryHexadecimalData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data in delete mode.
 *
 * @version 0.1.0 2015/05/17
 * @author ExBin Project (http://exbin.org)
 */
public class DeleteHexEditDataOperation extends HexEditDataOperation {

    private static final char BACKSPACE_CHAR = '\b';
    private static final char DELETE_CHAR = (char) 0x7f;

    private long position;
    private final MemoryHexadecimalData undoData = new MemoryHexadecimalData();

    public DeleteHexEditDataOperation(Hexadecimal hexadecimal, long startPosition) {
        super(hexadecimal);
        this.position = startPosition;
    }

    @Override
    public HexOperationType getType() {
        return HexOperationType.EDIT_DATA;
    }

    @Override
    public void execute() throws Exception {
        execute(false);
    }

    @Override
    public HexOperation executeWithUndo() throws Exception {
        return execute(true);
    }

    private HexOperation execute(boolean withUndo) {
        throw new IllegalStateException("Cannot be executed");
    }

    @Override
    public void appendEdit(byte value) {
        EditableBinaryData data = (EditableBinaryData) hexadecimal.getData();
        switch (value) {
            case BACKSPACE_CHAR: {
                if (position > 0) {
                    position--;
                    undoData.insert(0, new byte[]{data.getByte(position)});
                    data.remove(position, 1);
                }
                break;
            }
            case DELETE_CHAR: {
                if (position < data.getDataSize()) {
                    undoData.insert(0, new byte[]{data.getByte(position)});
                    data.remove(position, 1);
                }
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected character " + value);
            }
        }
        hexadecimal.getCaret().setCaretPosition(position);
        hexadecimal.repaint();
    }

    @Override
    public HexOperation[] generateUndo() {
        InsertDataOperation insertOperation = new InsertDataOperation(hexadecimal, position, false, undoData);
        return new HexOperation[]{insertOperation};
    }

    public long getPosition() {
        return position;
    }
}
