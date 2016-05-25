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

import java.io.IOException;
import org.exbin.deltahex.Hexadecimal;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;
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
import org.exbin.xbup.parser_tree.XBTBlockToXBBlock;
import org.exbin.xbup.parser_tree.XBTTreeNode;
import org.exbin.xbup.parser_tree.XBTreeReader;
import org.exbin.xbup.parser_tree.XBTreeWriter;

/**
 * Operation for inserting data.
 *
 * @version 0.1.0 2015/05/25
 * @author ExBin Project (http://exbin.org)
 */
public class InsertDataOperation extends HexOperation {

    private long position;
    private boolean lowerHalf;
    private BinaryData data;

    public InsertDataOperation(Hexadecimal hexadecimal, long position, boolean lowerHalf, BinaryData data) {
        super(hexadecimal);
        this.position = position;
        this.lowerHalf = lowerHalf;
        this.data = data;
    }

    @Override
    public HexOperationType getType() {
        return HexOperationType.INSERT_DATA;
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
        HexOperation undoOperation = null;
        ((EditableBinaryData) hexadecimal.getData()).insert(position, data);
        if (withUndo) {
            undoOperation = new RemoveDataOperation(hexadecimal, position, lowerHalf, data.getDataSize());
        }
        hexadecimal.getCaret().setCaretPosition(position + data.getDataSize(), lowerHalf);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        ((EditableBinaryData) data).insert(data.getDataSize(), appendData);
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
