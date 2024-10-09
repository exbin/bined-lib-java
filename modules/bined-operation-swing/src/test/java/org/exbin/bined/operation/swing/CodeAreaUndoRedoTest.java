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
package org.exbin.bined.operation.swing;

import javax.annotation.Nonnull;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaTest;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.swing.command.DeleteSelectionCommand;
import org.exbin.bined.operation.swing.command.EditCharDataCommand;
import org.exbin.bined.operation.swing.command.EditCodeDataCommand;
import org.exbin.bined.operation.swing.command.EditDataCommand;
import org.exbin.bined.operation.swing.command.InsertDataCommand;
import org.exbin.bined.operation.swing.command.ModifyDataCommand;
import org.exbin.bined.operation.swing.command.RemoveDataCommand;
import org.exbin.bined.operation.undo.BinaryDataUndoRedo;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.bined.swing.basic.CodeAreaComponentTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for CodeAreaUndoRedo component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaUndoRedoTest extends CodeAreaComponentTest {

    protected static final char BACKSPACE_CHAR = '\b';
    protected static final char DELETE_CHAR = (char) 0x7f;

    public CodeAreaUndoRedoTest() {
    }

    @Nonnull
    @Override
    public CodeAreaCore createCodeArea() {
        CodeArea codeArea = new CodeArea();
        codeArea.setCommandHandler(new CodeAreaOperationCommandHandler(codeArea, new CodeAreaUndoRedo(codeArea)));
        return codeArea;
    }

    @Test
    public void insertBinaryDataIntoEmpty() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        codeArea.setContentData(new ByteArrayEditableData());
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        InsertDataCommand insertDataCommand = new InsertDataCommand(codeArea, 0, 0, sampleData);
        undoRedo.execute(insertDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(sampleData));

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertEquals(0, codeArea.getDataSize());

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(sampleData));
    }

    @Test
    public void insertBinaryDataMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        byte[] insertedDataArray = new byte[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        BinaryData insertedData = new ByteArrayEditableData(insertedDataArray);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);

        byte[] expectedData = new byte[266];
        sampleData.copyToArray(0, expectedData, 0, 125);
        insertedData.copyToArray(0, expectedData, 125, 10);
        sampleData.copyToArray(125, expectedData, 135, 131);

        codeArea.setContentData(sampleData);
        InsertDataCommand insertDataCommand = new InsertDataCommand(codeArea, 125, 0, insertedData);
        undoRedo.execute(insertDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertEquals(256, codeArea.getDataSize());

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void deleteFullData() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData = sampleData.copy();
        codeArea.setContentData(sampleData);
        RemoveDataCommand removeDataCommand = new RemoveDataCommand(codeArea, 0, 0, 256);
        undoRedo.execute(removeDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(0, codeArea.getDataSize());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(0, codeArea.getDataSize());
    }

    @Test
    public void deleteMiddleData() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData = sampleData.copy();
        codeArea.setContentData(sampleData);
        RemoveDataCommand removeDataCommand = new RemoveDataCommand(codeArea, 125, 0, 10);
        undoRedo.execute(removeDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(246, codeArea.getDataSize());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(246, codeArea.getDataSize());
    }

    @Test
    public void deleteFullSelection() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData = sampleData.copy();
        codeArea.setContentData(sampleData);
        ((SelectionCapable) codeArea).setSelection(new SelectionRange(0, 256));
        DeleteSelectionCommand removeDataCommand = new DeleteSelectionCommand(codeArea);
        undoRedo.execute(removeDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(0, codeArea.getDataSize());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(0, codeArea.getDataSize());
    }

    @Test
    public void deleteMiddleSelection() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData = sampleData.copy();
        codeArea.setContentData(sampleData);
        ((SelectionCapable) codeArea).setSelection(new SelectionRange(125, 135));
        DeleteSelectionCommand removeDataCommand = new DeleteSelectionCommand(codeArea);
        undoRedo.execute(removeDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(246, codeArea.getDataSize());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertEquals(246, codeArea.getDataSize());
    }

    @Test
    public void modifyBinaryDataMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        byte[] replacedDataArray = new byte[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        BinaryData replacedData = new ByteArrayEditableData(replacedDataArray);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData2 = sampleData.copy();

        byte[] expectedData = new byte[256];
        sampleData.copyToArray(0, expectedData, 0, 125);
        replacedData.copyToArray(0, expectedData, 125, 10);
        sampleData.copyToArray(135, expectedData, 135, 121);

        codeArea.setContentData(sampleData);
        ModifyDataCommand modifyDataCommand = new ModifyDataCommand(codeArea, 125, replacedData);
        undoRedo.execute(modifyDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void insertCodeCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);

        byte[] expectedData = new byte[257];
        sampleData.copyToArray(0, expectedData, 0, 125);
        expectedData[125] = (byte) 240;
        sampleData.copyToArray(125, expectedData, 126, 131);

        BinaryData expectedData2 = sampleData.copy();

        codeArea.setContentData(sampleData);
        EditCodeDataCommand editDataCommand = new EditCodeDataCommand(codeArea, EditDataCommand.EditOperationType.INSERT, 125, 0, (byte) 15);
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void overwriteCodeCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        EditableBinaryData expectedData = (EditableBinaryData) sampleData.copy();
        expectedData.setByte(125, (byte) 173);
        BinaryData expectedData2 = sampleData.copy();

        codeArea.setContentData(sampleData);
        EditCodeDataCommand editDataCommand = new EditCodeDataCommand(codeArea, EditDataCommand.EditOperationType.OVERWRITE, 125, 0, (byte) 10);
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));
    }

    @Test
    public void deteleCodeCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData2 = sampleData.copy();
        byte[] expectedData = new byte[255];
        sampleData.copyToArray(0, expectedData, 0, 125);
        sampleData.copyToArray(126, expectedData, 125, 130);

        codeArea.setContentData(sampleData);
        EditCodeDataCommand editDataCommand = new EditCodeDataCommand(codeArea, EditDataCommand.EditOperationType.DELETE, 125, 0, (byte) DELETE_CHAR);
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void backspaceCodeCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData2 = sampleData.copy();
        byte[] expectedData = new byte[255];
        sampleData.copyToArray(0, expectedData, 0, 124);
        sampleData.copyToArray(125, expectedData, 124, 131);

        codeArea.setContentData(sampleData);
        EditCodeDataCommand editDataCommand = new EditCodeDataCommand(codeArea, EditDataCommand.EditOperationType.DELETE, 125, 0, (byte) BACKSPACE_CHAR);
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void insertPreviewCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);

        byte[] expectedData = new byte[257];
        sampleData.copyToArray(0, expectedData, 0, 125);
        expectedData[125] = (byte) 88;
        sampleData.copyToArray(125, expectedData, 126, 131);

        BinaryData expectedData2 = sampleData.copy();

        codeArea.setContentData(sampleData);
        EditCharDataCommand editDataCommand = new EditCharDataCommand(codeArea, EditDataCommand.EditOperationType.INSERT, 125, 'X');
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void overviewPreviewCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        EditableBinaryData expectedData = (EditableBinaryData) sampleData.copy();
        expectedData.setByte(125, (byte) 88);
        BinaryData expectedData2 = sampleData.copy();

        codeArea.setContentData(sampleData);
        EditCharDataCommand editDataCommand = new EditCharDataCommand(codeArea, EditDataCommand.EditOperationType.OVERWRITE, 125, 'X');
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData));
    }

    @Test
    public void detelePreviewCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData2 = sampleData.copy();
        byte[] expectedData = new byte[255];
        sampleData.copyToArray(0, expectedData, 0, 125);
        sampleData.copyToArray(126, expectedData, 125, 130);

        codeArea.setContentData(sampleData);
        EditCharDataCommand editDataCommand = new EditCharDataCommand(codeArea, EditDataCommand.EditOperationType.DELETE, 125, DELETE_CHAR);
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void backspacePreviewCharMiddle() {
        CodeAreaCore codeArea = createCodeArea();
        BinaryDataUndoRedo undoRedo = ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoRedo();

        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        BinaryData expectedData2 = sampleData.copy();
        byte[] expectedData = new byte[255];
        sampleData.copyToArray(0, expectedData, 0, 124);
        sampleData.copyToArray(125, expectedData, 124, 131);

        codeArea.setContentData(sampleData);
        EditCharDataCommand editDataCommand = new EditCharDataCommand(codeArea, EditDataCommand.EditOperationType.DELETE, 125, BACKSPACE_CHAR);
        undoRedo.execute(editDataCommand);
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());

        undoRedo.performUndo();
        Assert.assertEquals(0, undoRedo.getCommandPosition());
        Assert.assertTrue("Data should match", ((ByteArrayEditableData) codeArea.getContentData()).compareTo(expectedData2));

        undoRedo.performRedo();
        Assert.assertEquals(1, undoRedo.getCommandPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    public static void checkResultData(byte[] expectedData, BinaryData data) {
        Assert.assertEquals(expectedData.length, data.getDataSize());
        byte[] resultData = new byte[expectedData.length];
        data.copyToArray(0, resultData, 0, expectedData.length);
        Assert.assertArrayEquals(expectedData, resultData);
    }
}
