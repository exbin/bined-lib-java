/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.swing.section;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.junit.Assert;
import org.junit.Test;
import org.exbin.bined.capability.EditModeCapable;

/**
 * Tests for codeArea component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class SectionCodeAreaTest {

    public static final String SAMPLE_FILES_PATH = "/org/exbin/bined/resources/test/";
    public static final String SAMPLE_5BYTES = SAMPLE_FILES_PATH + "5bytes.dat";
    public static final String SAMPLE_10BYTES = SAMPLE_FILES_PATH + "10bytes.dat";
    public static final String SAMPLE_ALLBYTES = SAMPLE_FILES_PATH + "allbytes.dat";

    public SectionCodeAreaTest() {
    }

    @Test
    public void testDeleteAll() {
        SectCodeArea codeArea = new SectCodeArea();
        codeArea.setContentData(getSampleData(SAMPLE_ALLBYTES));
        codeArea.selectAll();
        codeArea.delete();
        Assert.assertTrue(codeArea.getDataSize() == 0);
    }

    @Test
    public void testCopyPasteInOverwriteMode() {
        SectCodeArea codeArea = new SectCodeArea();
        EditableBinaryData sampleData = getSampleData(SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        codeArea.paste();
        Assert.assertTrue(codeArea.getDataSize() == dataSize);
    }

    @Test
    public void testCopyPasteInInsertMode() {
        SectCodeArea codeArea = new SectCodeArea();
        ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);
        EditableBinaryData sampleData = getSampleData(SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        codeArea.paste();
        Assert.assertTrue(codeArea.getDataSize() == dataSize * 2);
    }

    @Test
    public void testCopyPasteAtTheEnd() {
        SectCodeArea codeArea = new SectCodeArea();
        EditableBinaryData sampleData = getSampleData(SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCodeAreaCaret().setCaretPosition(dataSize / 2);
        codeArea.paste();
        Assert.assertTrue(codeArea.getDataSize() == (dataSize / 2 + dataSize));
    }

    private EditableBinaryData getSampleData(String dataPath) {
        ByteArrayEditableData data = new ByteArrayEditableData();
        try (InputStream stream = SectionCodeAreaTest.class.getResourceAsStream(dataPath)) {
            data.loadFromStream(stream);
        } catch (IOException ex) {
            Logger.getLogger(SectionCodeAreaTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }
}
