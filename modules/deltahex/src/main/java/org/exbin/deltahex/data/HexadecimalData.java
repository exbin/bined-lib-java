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
import java.io.OutputStream;

/**
 * Interface for hexadecimal data.
 *
 * @version 0.1.0 2016/04/20
 * @author ExBin Project (http://exbin.org)
 */
public interface HexadecimalData {

    /**
     * Returns true if data are empty.
     *
     * @return true if data empty
     */
    public boolean isEmpty();

    /**
     * Returns size of data or -1 if size is not available.
     *
     * @return size of data in bytes
     */
    public long getDataSize();

    /**
     * Returns particular byte from data.
     *
     * @param position position
     * @return byte on requested position
     */
    public byte getByte(long position);

    /**
     * Saves data to given stream.
     *
     * @param outputStream output stream
     * @throws java.io.IOException if input/output error
     */
    public void saveToStream(OutputStream outputStream) throws IOException;

    /**
     * Creates copy of all data.
     *
     * @return copy of data
     */
    public HexadecimalData copy();

    /**
     * Creates copy of given area.
     *
     * @param startFrom position to start copy from
     * @param length length of area
     * @return copy of data
     */
    public HexadecimalData copy(long startFrom, long length);
}
