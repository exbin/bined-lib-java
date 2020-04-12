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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.bined.CodeAreaTest;
import org.exbin.bined.EditationMode;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests clipboard actions for CodeArea command component.
 *
 * @version 0.2.0 2020/04/12
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaCommandClipboardTest {

    public CodeAreaCommandClipboardTest() {
    }

    @Test
    public void testDeleteAll() {
        CodeArea codeArea = new CodeArea();
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));
        codeArea.selectAll();
        codeArea.delete();

        Assert.assertTrue(codeArea.getDataSize() == 0);
    }

    @Test
    public void testCopySelection() {
        CodeArea codeArea = new CodeArea();
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        byte[] expectedData = new byte[2];
        sampleData.copyToArray(5, expectedData, 0, 2);
        
        codeArea.setContentData(sampleData);
        codeArea.setSelection(5, 7);
        codeArea.copy();

        try {
            String text = (String) CodeAreaSwingUtils.getClipboard().getData(DataFlavor.stringFlavor);

            Assert.assertArrayEquals(expectedData, text.getBytes("UTF-8"));
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(CodeAreaCommandClipboardTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail();
        }
    }

    @Test
    public void testCopyPasteAllInOverwriteMode() {
        CodeArea codeArea = new CodeArea();
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == dataSize);
    }

    @Test
    public void testCopyPasteAllInInsertMode() {
        CodeArea codeArea = new CodeArea();
        ((EditationModeCapable) codeArea).setEditationOperation(EditationOperation.INSERT);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == dataSize * 2);
    }

    @Test
    public void testCopyPasteAllInInplaceMode() {
        CodeArea codeArea = new CodeArea();
        ((EditationModeCapable) codeArea).setEditationMode(EditationMode.INPLACE);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == dataSize);
    }

    @Test
    public void testCopyPasteInOverwriteAtTheEnd() {
        CodeArea codeArea = new CodeArea();
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCaret().setCaretPosition(dataSize);
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == (dataSize * 2));
    }

    @Test
    public void testCopyPasteInInsertAtTheEnd() {
        CodeArea codeArea = new CodeArea();
        ((EditationModeCapable) codeArea).setEditationOperation(EditationOperation.INSERT);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCaret().setCaretPosition(dataSize);
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == (dataSize * 2));
    }

    @Test
    public void testCopyPasteInInplaceAtTheEnd() {
        CodeArea codeArea = new CodeArea();
        ((EditationModeCapable) codeArea).setEditationMode(EditationMode.INPLACE);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        long dataSize = sampleData.getDataSize();
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCaret().setCaretPosition(dataSize);
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == dataSize);
    }

    @Test
    public void testCopyPasteInOverwriteWithOverflow() {
        CodeArea codeArea = new CodeArea();
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = (int) (dataSize / 2 + dataSize);

        byte[] expectedData = new byte[expectedSize];
        sampleData.copyToArray(0, expectedData, 0, dataSize / 2);
        sampleData.copyToArray(0, expectedData, dataSize / 2, dataSize);

        codeArea.setContentData(sampleData);
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCaret().setCaretPosition(dataSize / 2);
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == expectedSize);
        byte[] resultData = new byte[expectedSize];
        Objects.requireNonNull(codeArea.getContentData()).copyToArray(0, resultData, 0, expectedSize);
        Assert.assertArrayEquals(expectedData, resultData);
    }

    @Test
    public void testCopyPasteInInsertWithOverflow() {
        CodeArea codeArea = new CodeArea();
        ((EditationModeCapable) codeArea).setEditationOperation(EditationOperation.INSERT);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize * 2;

        byte[] expectedData = new byte[expectedSize];
        sampleData.copyToArray(0, expectedData, 0, dataSize / 2);
        sampleData.copyToArray(0, expectedData, dataSize / 2, dataSize);
        sampleData.copyToArray(dataSize / 2, expectedData, dataSize + dataSize / 2, dataSize / 2);

        codeArea.setContentData(sampleData);
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCaret().setCaretPosition(dataSize / 2);
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == expectedSize);
        byte[] resultData = new byte[expectedSize];
        Objects.requireNonNull(codeArea.getContentData()).copyToArray(0, resultData, 0, expectedSize);
        Assert.assertArrayEquals(expectedData, resultData);
    }

    @Test
    public void testCopyPasteInInplaceWithOverflow() {
        CodeArea codeArea = new CodeArea();
        ((EditationModeCapable) codeArea).setEditationMode(EditationMode.INPLACE);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize;
        byte[] expectedData = new byte[expectedSize];
        sampleData.copyToArray(0, expectedData, 0, dataSize / 2);
        sampleData.copyToArray(0, expectedData, dataSize / 2, dataSize / 2);

        codeArea.setContentData(sampleData);
        codeArea.selectAll();
        codeArea.copy();
        codeArea.clearSelection();
        ((CaretCapable) codeArea).getCaret().setCaretPosition(dataSize / 2);
        codeArea.paste();

        Assert.assertTrue(codeArea.getDataSize() == expectedSize);
        byte[] resultData = new byte[expectedSize];
        Objects.requireNonNull(codeArea.getContentData()).copyToArray(0, resultData, 0, expectedSize);
        Assert.assertArrayEquals(expectedData, resultData);
    }
}
