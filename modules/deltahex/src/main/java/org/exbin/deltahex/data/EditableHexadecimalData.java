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
package org.exbin.deltahex.data;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for editable hexadecimal data.
 *
 * @version 0.1.0 2016/05/02
 * @author ExBin Project (http://exbin.org)
 */
public interface EditableHexadecimalData extends HexadecimalData {

    /**
     * Sets data size.
     *
     * If size is bigger than current size, it will fill it with zeros,
     * otherwise it will shrink current data.
     *
     * @param size target size
     */
    public void setDataSize(long size);

    /**
     * Sets byte to given position.
     *
     * @param position position
     * @param value byte value to be set
     */
    public void setByte(long position, byte value);

    /**
     * Inserts empty data of given length to given position.
     *
     * @param startFrom position to insert to
     * @param length length of data
     */
    public void insert(long startFrom, long length);

    /**
     * Inserts given data to given position.
     *
     * @param startFrom position to insert to
     * @param insertedData data to insert
     */
    public void insert(long startFrom, byte[] insertedData);

    /**
     * Inserts given data to given position.
     *
     * @param startFrom position to insert to
     * @param insertedData data to insert
     */
    public void insert(long startFrom, HexadecimalData insertedData);

    /**
     * Removes area of data.
     *
     * @param startFrom position to start removal from
     * @param length length of area
     */
    public void remove(long startFrom, long length);

    /**
     * Loads data from given stream.
     *
     * @param inputStream input stream
     * @throws java.io.IOException if input/output error
     */
    public void loadFromStream(InputStream inputStream) throws IOException;
}
