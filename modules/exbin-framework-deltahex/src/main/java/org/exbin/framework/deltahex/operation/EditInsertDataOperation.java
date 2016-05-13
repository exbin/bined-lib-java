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
import org.exbin.deltahex.EditableHexadecimalData;
import org.exbin.deltahex.component.Hexadecimal;
import org.exbin.framework.deltahex.XBHexadecimalData;
import org.exbin.xbup.core.block.XBTEditableBlock;
import org.exbin.xbup.core.parser.XBParserMode;
import org.exbin.xbup.core.parser.XBProcessingException;
import org.exbin.xbup.core.parser.token.event.XBEventReader;
import org.exbin.xbup.core.parser.token.event.XBEventWriter;
import org.exbin.xbup.core.parser.token.event.convert.XBEventListenerToListener;
import org.exbin.xbup.core.parser.token.event.convert.XBListenerToEventListener;
import org.exbin.xbup.core.serial.param.XBPSequenceSerialHandler;
import org.exbin.xbup.core.serial.param.XBPSequenceSerializable;
import org.exbin.xbup.core.serial.param.XBSerializationMode;
import org.exbin.xbup.core.type.XBData;
import org.exbin.xbup.operation.Operation;
import org.exbin.xbup.parser_tree.XBTBlockToXBBlock;
import org.exbin.xbup.parser_tree.XBTTreeNode;
import org.exbin.xbup.parser_tree.XBTreeReader;
import org.exbin.xbup.parser_tree.XBTreeWriter;

/**
 * Operation for editing data.
 *
 * @version 0.1.0 2015/05/13
 * @author ExBin Project (http://exbin.org)
 */
public class EditInsertDataOperation extends HexOperation {

    private long position;
    private XBHexadecimalData editedData;
    private boolean positionLowerHalf = false;
    private boolean lowerHalf = false;
    private boolean trailing = false;

    public EditInsertDataOperation(Hexadecimal hexadecimal, long position, boolean positionLowerHalf) {
        super(hexadecimal);
        this.position = position;
        this.positionLowerHalf = positionLowerHalf;
        this.lowerHalf = positionLowerHalf;
        editedData = new XBHexadecimalData();
        if (lowerHalf) {
            editedData.insert(0, new byte[]{hexadecimal.getData().getByte(position)});
        }
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
    public Operation executeWithUndo() throws Exception {
        return execute(true);
    }

    private Operation execute(boolean withUndo) {
        Operation undoOperation = null;
        ((EditableHexadecimalData) hexadecimal.getData()).insert(position, editedData);
        if (withUndo) {
            undoOperation = new RemoveDataOperation(hexadecimal, position, editedData.getDataSize());
        }
        return undoOperation;
    }

    public void appendEdit(char value) {
        EditableHexadecimalData data = (EditableHexadecimalData) hexadecimal.getData();
        long editedDataPosition = position + editedData.getDataSize();
        if (trailing) {
            editedDataPosition--;
        }

        if (lowerHalf) {
            byte lowerPart = (byte) (data.getByte(editedDataPosition - 1) & 0xf);
            if (lowerPart > 0) {
                data.insert(editedDataPosition, 1);
                data.setByte(editedDataPosition, lowerPart);
                editedData.insert(editedData.getDataSize(), 1);
                editedData.setByte(editedData.getDataSize() - 1, lowerPart);
                trailing = true;
            }

            byte byteValue = (byte) (data.getByte(editedDataPosition - 1) & 0xf0);
            data.setByte(editedDataPosition - 1, (byte) (byteValue | value));
            editedData.setByte(editedData.getDataSize() - 1, byteValue);
        } else {
            byte byteValue = (byte) (value << 4);
            data.insert(editedDataPosition, 1);
            data.setByte(editedDataPosition, byteValue);
            editedData.insert(editedData.getDataSize(), 1);
            editedData.setByte(editedData.getDataSize() - 1, byteValue);
        }
        lowerHalf = !lowerHalf;
    }

    private class Serializator implements XBPSequenceSerializable {

        // Position is shifted: 0 mean no parent (for root blocks), others are shifted by 1
        // Should be null supporting natural later?
        private long parentPosition;
        private int childIndex;
        private XBTEditableBlock newNode;

        private Serializator() {
        }

        public Serializator(long parentPosition, int childIndex, XBTEditableBlock newNode) {
            this.parentPosition = parentPosition;
            this.childIndex = childIndex;
            this.newNode = newNode;
        }

        @Override
        public void serializeXB(XBPSequenceSerialHandler serializationHandler) throws XBProcessingException, IOException {
            serializationHandler.begin();
            serializationHandler.matchType();
            if (serializationHandler.getSerializationMode() == XBSerializationMode.PULL) {
                parentPosition = serializationHandler.pullLongAttribute();
                childIndex = serializationHandler.pullIntAttribute();
                newNode = new XBTTreeNode();
                serializationHandler.consist(new XBPSequenceSerializable() {
                    @Override
                    public void serializeXB(XBPSequenceSerialHandler serializationHandler) throws XBProcessingException, IOException {
                        serializationHandler.begin();
                        XBData data = new XBData();
                        data.loadFromStream(serializationHandler.pullData());
                        serializationHandler.end();

                        XBTreeReader treeReader = new XBTreeReader(new XBTBlockToXBBlock(newNode));
                        XBEventReader reader = new XBEventReader(data.getDataInputStream(), XBParserMode.SKIP_EXTENDED);
                        reader.attachXBEventListener(new XBListenerToEventListener(treeReader));
                        reader.read();
                        reader.close();
                    }
                });
            } else {
                serializationHandler.putAttribute(parentPosition);
                serializationHandler.putAttribute(childIndex);
                serializationHandler.consist(new XBPSequenceSerializable() {
                    @Override
                    public void serializeXB(XBPSequenceSerialHandler serializationHandler) throws XBProcessingException, IOException {
                        XBData data = new XBData();
                        XBTreeWriter treeWriter = new XBTreeWriter(new XBTBlockToXBBlock(newNode));
                        XBEventWriter writer = new XBEventWriter(data.getDataOutputStream());
                        treeWriter.attachXBListener(new XBEventListenerToListener(writer));

                        serializationHandler.begin();
                        serializationHandler.putData(data.getDataInputStream());
                        serializationHandler.end();
                    }
                });
            }
            serializationHandler.end();
        }
    }
}
