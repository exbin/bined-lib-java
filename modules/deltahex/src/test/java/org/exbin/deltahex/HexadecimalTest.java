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
package org.exbin.deltahex;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.deltahex.data.ByteEditableHexadecimalData;
import org.exbin.deltahex.data.EditableHexadecimalData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for hexadecimal component.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class HexadecimalTest {

    public final static String SAMPLE_FILES_PATH = "/org/exbin/deltahex/resources/test/";
    public final static String SAMPLE_5BYTES = SAMPLE_FILES_PATH + "5bytes.dat";
    public final static String SAMPLE_10BYTES = SAMPLE_FILES_PATH + "10bytes.dat";
    public final static String SAMPLE_ALLBYTES = SAMPLE_FILES_PATH + "allbytes.dat";

    public HexadecimalTest() {
    }

    @Test
    public void testDeleteAll() {
        Hexadecimal hexadecimal = new Hexadecimal();
        hexadecimal.setData(getSampleData(SAMPLE_ALLBYTES));
        hexadecimal.selectAll();
        hexadecimal.delete();
        assertTrue(hexadecimal.getData().getDataSize() == 0);
    }

    @Test
    public void testCopyPasteInOverwriteMode() {
        Hexadecimal hexadecimal = new Hexadecimal();
        EditableHexadecimalData sampleData = getSampleData(SAMPLE_ALLBYTES);
        hexadecimal.setData(sampleData);
        long dataSize = sampleData.getDataSize();
        hexadecimal.selectAll();
        hexadecimal.copy();
        hexadecimal.clearSelection();
        hexadecimal.paste();
        assertTrue(hexadecimal.getData().getDataSize() == dataSize);
    }

    @Test
    public void testCopyPasteInInsertMode() {
        Hexadecimal hexadecimal = new Hexadecimal();
        hexadecimal.setEditationMode(Hexadecimal.EditationMode.INSERT);
        EditableHexadecimalData sampleData = getSampleData(SAMPLE_ALLBYTES);
        hexadecimal.setData(sampleData);
        long dataSize = sampleData.getDataSize();
        hexadecimal.selectAll();
        hexadecimal.copy();
        hexadecimal.clearSelection();
        hexadecimal.paste();
        assertTrue(hexadecimal.getData().getDataSize() == dataSize * 2);
    }

    @Test
    public void testCopyPasteAtTheEnd() {
        Hexadecimal hexadecimal = new Hexadecimal();
        EditableHexadecimalData sampleData = getSampleData(SAMPLE_ALLBYTES);
        hexadecimal.setData(sampleData);
        long dataSize = sampleData.getDataSize();
        hexadecimal.selectAll();
        hexadecimal.copy();
        hexadecimal.clearSelection();
        hexadecimal.getCaret().setCaretPosition(dataSize / 2);
        hexadecimal.paste();
        assertTrue(hexadecimal.getData().getDataSize() == (dataSize / 2 + dataSize));
    }

    private EditableHexadecimalData getSampleData(String dataPath) {
        ByteEditableHexadecimalData data = new ByteEditableHexadecimalData();
        try (InputStream stream = HexadecimalTest.class.getResourceAsStream(dataPath)) {
            data.loadFromStream(stream);
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger(HexadecimalTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }
}
