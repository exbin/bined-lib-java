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
 * Operation for modifying data.
 *
 * @version 0.1.0 2016/05/12
 * @author ExBin Project (http://exbin.org)
 */
public class ModifyDataOperation extends HexOperation {

    private long position;
    private HexadecimalData data;

    public ModifyDataOperation(Hexadecimal hexadecimal, long position, HexadecimalData data) {
        super(hexadecimal);
        this.position = position;
        this.data = data;
    }

    @Override
    public HexOperationType getType() {
        return HexOperationType.MODIFY_DATA;
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
            HexadecimalData undoData = hexadecimal.getData().copy(position, data.getDataSize());
            undoOperation = new ModifyDataOperation(hexadecimal, position, undoData);
        }
        ((EditableHexadecimalData) hexadecimal.getData()).remove(position, data.getDataSize());
        ((EditableHexadecimalData) hexadecimal.getData()).insert(position, data);
        return undoOperation;
    }

    public void appendData(HexadecimalData appendData) {
        ((EditableHexadecimalData) data).insert(data.getDataSize(), appendData);
    }

    private class Serializator implements XBPSequenceSerializable {

        private long position;
        private XBTEditableBlock newNode;

        private Serializator() {
        }

        public Serializator(long position, XBTEditableBlock newNode) {
            this.position = position;
            this.newNode = newNode;
        }

        @Override
        public void serializeXB(XBPSequenceSerialHandler serializationHandler) throws XBProcessingException, IOException {
            serializationHandler.begin();
            serializationHandler.matchType();
            if (serializationHandler.getSerializationMode() == XBSerializationMode.PULL) {
                position = serializationHandler.pullLongAttribute();
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
                serializationHandler.putAttribute(position);
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
