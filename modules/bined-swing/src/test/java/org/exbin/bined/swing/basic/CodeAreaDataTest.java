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

import org.exbin.bined.CodeAreaTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for codeArea component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaDataTest {

    public CodeAreaDataTest() {
    }

    @Test
    public void testContentData() {
        CodeArea codeArea = new CodeArea();
        codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

        Assert.assertTrue(codeArea.getDataSize() == 256);
    }
}
