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

import org.exbin.xbup.core.block.XBTEditableDocument;
import org.exbin.xbup.core.type.XBData;
import org.exbin.xbup.operation.Operation;
import org.exbin.xbup.operation.basic.XBBasicOperationType;

/**
 * Abstract class for operation on hexadecimal document.
 *
 * @version 0.1.0 2016/04/22
 * @author ExBin Project (http://exbin.org)
 */
public abstract class HexOperation implements Operation {

    protected final XBTEditableDocument document;
    protected XBData data;

    public HexOperation(XBTEditableDocument document) {
        this.document = document;
        data = new XBData();
    }

    /**
     * Returns type of the operation.
     *
     * @return basic type
     */
    public abstract XBBasicOperationType getBasicType();

    public XBTEditableDocument getDocument() {
        return document;
    }

    public XBData getData() {
        return data;
    }

    public void setData(XBData data) {
        this.data = data;
    }

    /**
     * Performs dispose of the operation.
     *
     * Default dispose is empty.
     *
     * @throws Exception if not successful
     */
    @Override
    public void dispose() throws Exception {
    }

    @Override
    public String getCaption() {
        return getBasicType().getCaption();
    }
}
