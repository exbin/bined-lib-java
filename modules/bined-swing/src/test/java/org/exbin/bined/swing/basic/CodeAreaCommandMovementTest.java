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
package org.exbin.bined.swing.basic;

import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for codeArea component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaCommandMovementTest {

    public CodeAreaCommandMovementTest() {
    }

    @Test
    public void testMoveBeginLeft() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        emulateKeyPressed(codeArea, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveMiddleLeft() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(128);
        emulateKeyPressed(codeArea, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(127, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveEndLeft() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(257);
        emulateKeyPressed(codeArea, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(256, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveBeginRight() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        emulateKeyPressed(codeArea, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveBeginRightTwice() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        emulateKeyPressed(codeArea, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);
        emulateKeyPressed(codeArea, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(1, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveMiddleRight() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(128);
        emulateKeyPressed(codeArea, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(128, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveEndRight() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(257);
        emulateKeyPressed(codeArea, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(257, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveBeginUp() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        emulateKeyPressed(codeArea, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveMiddleUp() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(128);
        emulateKeyPressed(codeArea, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(112, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveEndUp() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(257);
        emulateKeyPressed(codeArea, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(241, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveBeginDown() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        emulateKeyPressed(codeArea, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(16, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveMiddleDown() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(128);
        emulateKeyPressed(codeArea, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(144, caretPosition.getDataPosition());
    }

    @Test
    public void testMoveEndDown() {
        CodeArea codeArea = new CodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        codeArea.setCaretPosition(257);
        emulateKeyPressed(codeArea, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);

        CodeAreaCaretPosition caretPosition = codeArea.getCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(257, caretPosition.getDataPosition());
    }

    private void emulateKeyPressed(Component component, int keyEvent, char keyChar) {
        component.dispatchEvent(new KeyEvent(component, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyEvent, keyChar));
        component.dispatchEvent(new KeyEvent(component, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyEvent, keyChar));
    }
}
