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
package org.exbin.framework.deltahex.operation;

import java.io.IOException;
import org.exbin.deltahex.data.EditableHexadecimalData;
import org.exbin.deltahex.data.HexadecimalData;
import org.exbin.deltahex.Hexadecimal;
import org.exbin.xbup.core.parser.XBProcessingException;
import org.exbin.xbup.core.serial.param.XBPSequenceSerialHandler;
import org.exbin.xbup.core.serial.param.XBPSequenceSerializable;
import org.exbin.xbup.core.serial.param.XBSerializationMode;
import org.exbin.xbup.operation.Operation;

/**
 * Operation for deleting child block.
 *
 * @version 0.1.0 2016/05/14
 * @author ExBin Project (http://exbin.org)
 */
public class RemoveDataOperation extends HexOperation {

    private long position;
    private boolean lowerHalf;
    private long size;

    public RemoveDataOperation(Hexadecimal hexadecimal, long position, boolean lowerHalf, long size) {
        super(hexadecimal);
        this.position = position;
        this.lowerHalf = lowerHalf;
        this.size = size;
    }

    @Override
    public HexOperationType getType() {
        return HexOperationType.REMOVE_DATA;
    }

    @Override
    public void execute() throws Exception {
        execute(false);
    }

    @Override
    public Operation executeWithUndo() throws Exception {
        return execute(true);
    }

    private Operation execute(boolean withUndo) {
        Operation undoOperation = null;
        if (withUndo) {
            HexadecimalData undoData = hexadecimal.getData().copy(position, size);
            undoOperation = new InsertDataOperation(hexadecimal, position, lowerHalf, undoData);
        }
        ((EditableHexadecimalData) hexadecimal.getData()).remove(position, size);
        hexadecimal.getCaret().setCaretPosition(position, lowerHalf);
        return undoOperation;
    }

    private class Serializator implements XBPSequenceSerializable {

        private long position;

        private Serializator() {
        }

        public Serializator(long position) {
            this.position = position;
        }

        @Override
        public void serializeXB(XBPSequenceSerialHandler serializationHandler) throws XBProcessingException, IOException {
            serializationHandler.begin();
            serializationHandler.matchType();
            if (serializationHandler.getSerializationMode() == XBSerializationMode.PULL) {
                position = serializationHandler.pullLongAttribute();
            } else {
                serializationHandler.putAttribute(position);
            }
            serializationHandler.end();
        }
    }
}
