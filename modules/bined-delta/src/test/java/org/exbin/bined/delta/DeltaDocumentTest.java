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
package org.exbin.bined.delta;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.exbin.bined.delta.list.DefaultDoublyLinkedList;
import org.exbin.utils.binary_data.BinaryData;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for delta document.
 *
 * @version 0.2.0 2018/04/27
 * @author ExBin Project (https://exbin.org)
 */
public class DeltaDocumentTest {

    public static final String SAMPLE_FILES_PATH = "/org/exbin/bined/delta/resources/test/";
    public static final String SAMPLE_ALLBYTES = SAMPLE_FILES_PATH + "allbytes.dat";
    public static final int SAMPLE_ALLBYTES_SIZE = 256;

    public DeltaDocumentTest() {
    }

    @Test
    public void testOpenDocument() {
        DeltaDocument document = openDeltaDocument();
        Assert.assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        Assert.assertEquals(1, segments.size());

        DataSegment segment0 = segments.first();
        Assert.assertTrue(segment0 instanceof FileSegment);
        Assert.assertEquals(0, ((FileSegment) segment0).getStartPosition());
        Assert.assertEquals(SAMPLE_ALLBYTES_SIZE, segment0.getLength());

        document.validate();
        document.clear();
        Assert.assertEquals(0, document.getSegments().size());
        document.dispose();
    }

    @Test
    public void testSetBytes() {
        DeltaDocument document = openDeltaDocument();
        document.setByte(10, (byte) 0);
        Assert.assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        Assert.assertEquals(3, segments.size());

        DataSegment segment0 = segments.first();
        Assert.assertTrue(segment0 instanceof FileSegment);
        Assert.assertEquals(0, ((FileSegment) segment0).getStartPosition());
        Assert.assertEquals(10, segment0.getLength());

        DataSegment segment1 = segments.get(1);
        Assert.assertTrue(segment1 instanceof MemorySegment);
        Assert.assertEquals(0, ((MemorySegment) segment1).getStartPosition());
        Assert.assertEquals(1, segment1.getLength());

        DataSegment segment2 = segments.get(2);
        Assert.assertTrue(segment2 instanceof FileSegment);
        Assert.assertEquals(11, ((FileSegment) segment2).getStartPosition());
        Assert.assertEquals(SAMPLE_ALLBYTES_SIZE - 11, segment2.getLength());

        document.validatePointerPosition();
        document.clear();
        Assert.assertEquals(0, document.getSegments().size());
        document.dispose();
    }

    @Test
    public void testInsertBinaryData() {
        DeltaDocument document = openDeltaDocument();
        BinaryData data = new MemoryDataSource(new byte[]{0});
        document.insert(10, data);
        Assert.assertEquals(SAMPLE_ALLBYTES_SIZE + 1, document.getDataSize());

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        Assert.assertEquals(3, segments.size());

        DataSegment segment0 = segments.first();
        Assert.assertTrue(segment0 instanceof FileSegment);
        Assert.assertEquals(0, ((FileSegment) segment0).getStartPosition());
        Assert.assertEquals(10, segment0.getLength());

        DataSegment segment1 = segments.get(1);
        Assert.assertTrue(segment1 instanceof MemorySegment);
        Assert.assertEquals(0, ((MemorySegment) segment1).getStartPosition());
        Assert.assertEquals(1, segment1.getLength());

        DataSegment segment2 = segments.get(2);
        Assert.assertTrue(segment2 instanceof FileSegment);
        Assert.assertEquals(10, ((FileSegment) segment2).getStartPosition());
        Assert.assertEquals(SAMPLE_ALLBYTES_SIZE - 10, segment2.getLength());

        document.validatePointerPosition();
        document.clear();
        Assert.assertEquals(0, document.getSegments().size());
    }

    @Nullable
    public static DeltaDocument openDeltaDocument() {
        SegmentsRepository segmentsRepository = new SegmentsRepository();
        try {
            FileDataSource fileSource = segmentsRepository.openFileSource(new File(DeltaDocumentTest.class.getResource(SAMPLE_ALLBYTES).getFile()));
            return segmentsRepository.createDocument(fileSource);
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
