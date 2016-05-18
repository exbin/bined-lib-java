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
package org.exbin.deltahex.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.framework.deltahex.XBHexadecimalData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Hex editor painter.
 *
 * @version 0.1.0 2016/05/18
 * @author ExBin Project (http://exbin.org)
 */
public class HexadecimalTest {

    public final static String SAMPLE_FILES_PATH = "/org/exbin/deltahex/resources/test/";
    public final static String SAMPLE_5BYTES = SAMPLE_FILES_PATH + "5bytes.dat";
    public final static String SAMPLE_10BYTES = SAMPLE_FILES_PATH + "10bytes.dat";

    public HexadecimalTest() {
    }

    @Test
    public void testDataLoadFromStream() {
        XBHexadecimalData instance = new XBHexadecimalData();
        try (InputStream stream = HexadecimalTest.class.getResourceAsStream(SAMPLE_5BYTES)) {
            instance.loadFromStream(stream);
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger(HexadecimalTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue(instance.getDataSize() == 5);
    }
}
