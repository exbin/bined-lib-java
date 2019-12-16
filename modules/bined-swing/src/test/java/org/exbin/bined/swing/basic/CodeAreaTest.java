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
package org.exbin.bined.swing.basic;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for codeArea component.
 *
 * @version 0.2.0 2017/11/14
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaTest {

    public static final String SAMPLE_FILES_PATH = "/org/exbin/bined/resources/test/";
    public static final String SAMPLE_5BYTES = SAMPLE_FILES_PATH + "5bytes.dat";
    public static final String SAMPLE_10BYTES = SAMPLE_FILES_PATH + "10bytes.dat";
    public static final String SAMPLE_ALLBYTES = SAMPLE_FILES_PATH + "allbytes.dat";

    public CodeAreaTest() {
    }

    @Test
    public void testDeleteAll() {
        CodeArea codeArea = new CodeArea();
        codeArea.setContentData(getSampleData(SAMPLE_ALLBYTES));
        codeArea.selectAll();
        codeArea.delete();

        Assert.assertTrue(codeArea.getDataSize() == 0);
    }

    @Test
    public void testCopyPasteInOverwriteMode() {
        CodeArea codeArea = new CodeArea();
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
        CodeArea codeArea = new CodeArea();
        ((EditationModeCapable) codeArea).setEditationOperation(EditationOperation.INSERT);
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
        CodeArea codeArea = new CodeArea();
        EditableBinaryData sampleData = getSampleData(SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCaret().setCaretPosition(dataSize / 2);
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == (dataSize / 2 + dataSize));
    }

    @Nonnull
    private EditableBinaryData getSampleData(@Nonnull String dataPath) {
        ByteArrayEditableData data = new ByteArrayEditableData();
        try (InputStream stream = CodeAreaTest.class.getResourceAsStream(dataPath)) {
            data.loadFromStream(stream);
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger(CodeAreaTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }
}
