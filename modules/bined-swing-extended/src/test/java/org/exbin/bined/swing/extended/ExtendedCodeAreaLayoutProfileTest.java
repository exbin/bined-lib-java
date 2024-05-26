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
package org.exbin.bined.swing.extended;

import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeType;
import org.exbin.bined.extended.layout.SpaceType;
import org.exbin.bined.swing.extended.layout.DefaultExtendedCodeAreaLayoutProfile;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.exbin.bined.extended.layout.PositionIterator;
import org.hamcrest.MatcherAssert;

/**
 * Tests for DefaultExtendedCodeAreaLayoutProfile.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class ExtendedCodeAreaLayoutProfileTest {

    private static final int TEST_CHARACTER_WIDTH = 10;
    private static final int TEST_BYTES_PER_ROW_DEFAULT = 3;
    private static final int TEST_BYTES_PER_ROW_COMBINED = 11;

    public ExtendedCodeAreaLayoutProfileTest() {
    }

    @Test
    public void testDefaultCharPosIterator() {
        DefaultExtendedCodeAreaLayoutProfile layout = new DefaultExtendedCodeAreaLayoutProfile();
        PositionIterator charPositionIterator = layout.createPositionIterator(CodeType.HEXADECIMAL, CodeAreaViewMode.DUAL, TEST_BYTES_PER_ROW_DEFAULT);

        Assert.assertNotNull(charPositionIterator);
        MatcherAssert.assertThat(charPositionIterator.isEndReached(), CoreMatchers.is(false));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.isEndReached(), CoreMatchers.is(false));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.isEndReached(), CoreMatchers.is(true));
    }

    @Test
    public void testCombinedCharPosIterator() {
        DefaultExtendedCodeAreaLayoutProfile layout = new DefaultExtendedCodeAreaLayoutProfile();
        layout.setHalfSpaceGroupSize(2);
        layout.setSpaceGroupSize(3);
        layout.setDoubleSpaceGroupSize(4);
        PositionIterator charPositionIterator = layout.createPositionIterator(CodeType.HEXADECIMAL, CodeAreaViewMode.DUAL, TEST_BYTES_PER_ROW_COMBINED);

        Assert.assertNotNull(charPositionIterator);
        MatcherAssert.assertThat(charPositionIterator.isEndReached(), CoreMatchers.is(false));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.HALF));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.DOUBLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.DOUBLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.HALF));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.isEndReached(), CoreMatchers.is(false));
        MatcherAssert.assertThat(charPositionIterator.nextSpaceType(), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(charPositionIterator.isEndReached(), CoreMatchers.is(true));
    }

    @Test
    public void testCombinedSpaceSizeBefore() {
        DefaultExtendedCodeAreaLayoutProfile layout = new DefaultExtendedCodeAreaLayoutProfile();
        layout.setHalfSpaceGroupSize(2);
        layout.setSpaceGroupSize(3);
        layout.setDoubleSpaceGroupSize(4);

        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(0, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(1, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(2, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.HALF));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(3, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(4, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.DOUBLE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(5, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(6, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(7, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.NONE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(8, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.DOUBLE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(9, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.SINGLE));
        MatcherAssert.assertThat(layout.getSpaceSizeTypeBefore(10, TEST_CHARACTER_WIDTH), CoreMatchers.is(SpaceType.HALF));
    }

    @Test
    public void testCombinedPixelPosition() {
        DefaultExtendedCodeAreaLayoutProfile layout = new DefaultExtendedCodeAreaLayoutProfile();
        layout.setHalfSpaceGroupSize(2);
        layout.setSpaceGroupSize(3);
        layout.setDoubleSpaceGroupSize(4);

        int pixelPos = 0;
        MatcherAssert.assertThat(layout.computePixelPosition(0, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(1, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(2, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(3, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH / 2;
        MatcherAssert.assertThat(layout.computePixelPosition(4, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(5, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(6, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(7, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH * 2;
        MatcherAssert.assertThat(layout.computePixelPosition(8, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(9, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(10, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(11, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(12, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(13, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(14, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(15, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH * 2;
        MatcherAssert.assertThat(layout.computePixelPosition(16, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(17, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(18, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH;
        MatcherAssert.assertThat(layout.computePixelPosition(19, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
        pixelPos += TEST_CHARACTER_WIDTH + TEST_CHARACTER_WIDTH / 2;
        MatcherAssert.assertThat(layout.computePixelPosition(20, TEST_CHARACTER_WIDTH, CodeAreaViewMode.CODE_MATRIX, CodeType.HEXADECIMAL, TEST_BYTES_PER_ROW_COMBINED), CoreMatchers.is(pixelPos));
    }
}
