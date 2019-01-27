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
package org.exbin.bined.swing.extended;

import org.exbin.bined.CodeType;
import org.exbin.bined.swing.extended.layout.CodeCharPositionIterator;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ExtendedCodeAreaLayoutProfile.
 *
 * @version 0.2.0 2019/01/27
 * @author ExBin Project (https://exbin.org)
 */
public class ExtendedCodeAreaLayoutProfileTest {

    private static final int TEST_CHARACTER_WIDTH = 10;

    public ExtendedCodeAreaLayoutProfileTest() {
    }

    @Test
    public void testDefaultCharPosIterator() {
        ExtendedCodeAreaLayoutProfile layout = new ExtendedCodeAreaLayoutProfile();
        CodeCharPositionIterator charPositionIterator = layout.getCharPositionIterator(TEST_CHARACTER_WIDTH, CodeType.HEXADECIMAL);

        Assert.assertNotNull(charPositionIterator);
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH));
    }

    @Test
    public void testCombinedCharPosIterator() {
        ExtendedCodeAreaLayoutProfile layout = new ExtendedCodeAreaLayoutProfile();
        layout.setHalfSpaceGroupSize(2);
        layout.setSpaceGroupSize(3);
        layout.setDoubleSpaceGroupSize(4);
        CodeCharPositionIterator charPositionIterator = layout.getCharPositionIterator(TEST_CHARACTER_WIDTH, CodeType.HEXADECIMAL);

        Assert.assertNotNull(charPositionIterator);
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH / 2));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH * 2));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH * 2));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(0));
        Assert.assertThat(charPositionIterator.nextSpaceSize(), CoreMatchers.is(TEST_CHARACTER_WIDTH / 2));
    }
}
