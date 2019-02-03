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

import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.extended.layout.CodeCharPositionIterator;
import org.exbin.bined.extended.layout.SpaceType;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ExtendedCodeAreaLayoutProfile.
 *
 * @version 0.2.0 2019/02/03
 * @author ExBin Project (https://exbin.org)
 */
public class ExtendedCodeAreaLayoutProfileTest {

    private static final int TEST_CHARACTER_WIDTH = 10;

    public ExtendedCodeAreaLayoutProfileTest() {
    }

    @Test
    public void testDefaultCharPosIterator() {
        ExtendedCodeAreaLayoutProfile layout = new ExtendedCodeAreaLayoutProfile();
        CodeCharPositionIterator charPositionIterator = layout.createCharPositionIterator(TEST_CHARACTER_WIDTH, CodeType.HEXADECIMAL);

        Assert.assertNotNull(charPositionIterator);
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
    }

    @Test
    public void testCombinedCharPosIterator() {
        ExtendedCodeAreaLayoutProfile layout = new ExtendedCodeAreaLayoutProfile();
        layout.setHalfSpaceGroupSize(2);
        layout.setSpaceGroupSize(3);
        layout.setDoubleSpaceGroupSize(4);
        CodeCharPositionIterator charPositionIterator = layout.createCharPositionIterator(TEST_CHARACTER_WIDTH, CodeType.HEXADECIMAL);

        Assert.assertNotNull(charPositionIterator);
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.HALF));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.DOUBLE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.DOUBLE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.HALF));
    }

    @Test
    public void testCombinedSpaceSizeBefore() {
        ExtendedCodeAreaLayoutProfile layout = new ExtendedCodeAreaLayoutProfile();
        layout.setHalfSpaceGroupSize(2);
        layout.setSpaceGroupSize(3);
        layout.setDoubleSpaceGroupSize(4);

        Assert.assertThat(layout.getSpaceSizeTypeBefore(0, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(1, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(2, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.HALF));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(3, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.SINGLE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(4, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.DOUBLE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(5, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(6, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.SINGLE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(7, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(8, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.DOUBLE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(9, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.SINGLE));
        Assert.assertThat(layout.getSpaceSizeTypeBefore(10, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.HALF));
    }

    @Test
    public void testCombinedPixelPosition() {
        ExtendedCodeAreaLayoutProfile layout = new ExtendedCodeAreaLayoutProfile();
        layout.setHalfSpaceGroupSize(2);
        layout.setSpaceGroupSize(3);
        layout.setDoubleSpaceGroupSize(4);

        int pixelPos = 0;
        Assert.assertThat(layout.computePixelPosition(0, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(1, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(2, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(3, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH / 2;
        Assert.assertThat(layout.computePixelPosition(4, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(5, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(6, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(7, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH * 2;
        Assert.assertThat(layout.computePixelPosition(8, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(9, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(10, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(11, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(12, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(13, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(14, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(15, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH * 2;
        Assert.assertThat(layout.computePixelPosition(16, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(17, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(18, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        Assert.assertThat(layout.computePixelPosition(19, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH / 2;
        Assert.assertThat(layout.computePixelPosition(20, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, 16), CoreMatchers.is(pixelPos));
    }
}
