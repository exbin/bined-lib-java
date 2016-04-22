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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.xbup.core.block.XBTBlock;
import org.exbin.xbup.core.block.XBTDefaultBlock;
import org.exbin.xbup.core.block.XBTEditableBlock;
import org.exbin.xbup.core.block.XBTEditableDocument;
import org.exbin.xbup.core.parser.XBProcessingException;
import org.exbin.xbup.core.serial.XBPSerialReader;
import org.exbin.xbup.core.serial.XBPSerialWriter;
import org.exbin.xbup.core.serial.param.XBPSequenceSerialHandler;
import org.exbin.xbup.core.serial.param.XBPSequenceSerializable;
import org.exbin.xbup.core.serial.param.XBSerializationMode;
import org.exbin.xbup.operation.Operation;
import org.exbin.xbup.operation.basic.XBBasicOperationType;

/**
 * Operation for deleting child block.
 *
 * @version 0.1.0 2016/04/22
 * @author ExBin Project (http://exbin.org)
 */
public class XBTDeleteBlockOperation extends HexOperation {

    public XBTDeleteBlockOperation(XBTEditableDocument document, XBTBlock block) {
        super(document);
        long position = XBTDefaultBlock.getBlockIndex(block);
        OutputStream dataOutputStream = data.getDataOutputStream();
        XBPSerialWriter writer = new XBPSerialWriter(dataOutputStream);
        Serializator serializator = new Serializator(position);
        writer.write(serializator);
    }

    @Override
    public XBBasicOperationType getBasicType() {
        return XBBasicOperationType.DELETE_BLOCK;
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
        InputStream dataInputStream = getData().getDataInputStream();
        XBPSerialReader reader = new XBPSerialReader(dataInputStream);
        Serializator serial = new Serializator();
        try {
            reader.read(serial);
        } catch (XBProcessingException | IOException ex) {
            Logger.getLogger(XBTDeleteBlockOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Unable to process data");
        }

        XBTAddBlockOperation undoOperation = null;

        if (withUndo) {
            int childIndex = 0;
            Long parentPosition = null;
            XBTEditableBlock deletedNode;
            if (serial.position < 1) {
                deletedNode = (XBTEditableBlock) document.getRootBlock();
            } else {
                deletedNode = (XBTEditableBlock) document.findBlockByIndex(serial.position);
                XBTEditableBlock parentNode = (XBTEditableBlock) deletedNode.getParent();
                parentPosition = (long) XBTDefaultBlock.getBlockIndex(parentNode);
                childIndex = Arrays.asList(parentNode.getChildren()).indexOf(deletedNode);
            }
            undoOperation = new XBTAddBlockOperation(document, parentPosition, childIndex, deletedNode);
        }

        if (serial.position < 1) {
            document.clear();
        } else {
            XBTEditableBlock node = (XBTEditableBlock) document.findBlockByIndex(serial.position);
            XBTEditableBlock parentNode = (XBTEditableBlock) node.getParent();
            int childIndex = Arrays.asList(parentNode.getChildren()).indexOf(node);
            parentNode.removeChild(childIndex);
        }

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
