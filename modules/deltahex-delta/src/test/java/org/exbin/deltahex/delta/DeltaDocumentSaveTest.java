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
package org.exbin.deltahex.delta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for delta document.
 *
 * @version 0.1.1 2016/11/19
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDocumentSaveTest {

    public static final String SAMPLE_FILES_PATH = "/org/exbin/deltahex/delta/resources/test/";
    public static final String SAMPLE_ALLBYTES = SAMPLE_FILES_PATH + "allbytes.dat";
    public static final String SAMPLE_INSERTED_BEGIN = SAMPLE_FILES_PATH + "inserted_begin.dat";
    public static final String SAMPLE_INSERTED_MIDDLE = SAMPLE_FILES_PATH + "inserted_middle.dat";
    public static final String SAMPLE_INSERTED_END = SAMPLE_FILES_PATH + "inserted_end.dat";
    public static final String SAMPLE_OVERWRITTEN_BEGIN = SAMPLE_FILES_PATH + "overwritten_begin.dat";
    public static final String SAMPLE_OVERWRITTEN_MIDDLE = SAMPLE_FILES_PATH + "overwritten_middle.dat";
    public static final String SAMPLE_OVERWRITTEN_END = SAMPLE_FILES_PATH + "overwritten_end.dat";
    public static final int SAMPLE_ALLBYTES_SIZE = 256;

    public DeltaDocumentSaveTest() {
    }

    @Test
    public void testSaveDocument() {
        DeltaDocument document = openTempDeltaDocument();
        assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());

        try {
            document.save();
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception: " + ex.getMessage());
        }

        document.clear();
        assertEquals(0, document.getSegments().size());
        document.destroy();
    }

    @Test
    public void testInsertBeginSaveDocument() {
        DeltaDocument document = openTempDeltaDocument();
        assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());
        document.insert(0, new byte[]{0x40, 0x41});

        try {
            document.save();

            InputStream comparisionFile;
            try (InputStream dataInputStream = document.getDataInputStream()) {
                comparisionFile = new FileInputStream(DeltaDocumentSaveTest.class.getResource(SAMPLE_INSERTED_BEGIN).getFile());
                TestUtils.assertEqualsInputStream(comparisionFile, dataInputStream);
            }
            comparisionFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception: " + ex.getMessage());
        }

        document.clear();
        assertEquals(0, document.getSegments().size());
        document.destroy();
    }

    @Test
    public void testInsertMiddleSaveDocument() {
        DeltaDocument document = openTempDeltaDocument();
        assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());
        document.insert(120, new byte[]{0x40, 0x41});

        try {
            document.save();

            InputStream comparisionFile;
            try (InputStream dataInputStream = document.getDataInputStream()) {
                comparisionFile = new FileInputStream(DeltaDocumentSaveTest.class.getResource(SAMPLE_INSERTED_MIDDLE).getFile());
                TestUtils.assertEqualsInputStream(comparisionFile, dataInputStream);
            }
            comparisionFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception: " + ex.getMessage());
        }

        document.clear();
        assertEquals(0, document.getSegments().size());
        document.destroy();
    }

    @Test
    public void testInsertEndSaveDocument() {
        DeltaDocument document = openTempDeltaDocument();
        assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());
        document.insert(256, new byte[]{0x40, 0x41});

        try {
            document.save();

            InputStream comparisionFile;
            try (InputStream dataInputStream = document.getDataInputStream()) {
                comparisionFile = new FileInputStream(DeltaDocumentSaveTest.class.getResource(SAMPLE_INSERTED_END).getFile());
                TestUtils.assertEqualsInputStream(comparisionFile, dataInputStream);
            }
            comparisionFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception: " + ex.getMessage());
        }

        document.clear();
        assertEquals(0, document.getSegments().size());
        document.destroy();
    }

    @Test
    public void testOverwriteBeginSaveDocument() {
        DeltaDocument document = openTempDeltaDocument();
        assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());
        document.replace(0, new byte[]{0x40, 0x41});

        try {
            document.save();

            InputStream comparisionFile;
            try (InputStream dataInputStream = document.getDataInputStream()) {
                comparisionFile = new FileInputStream(DeltaDocumentSaveTest.class.getResource(SAMPLE_OVERWRITTEN_BEGIN).getFile());
                TestUtils.assertEqualsInputStream(comparisionFile, dataInputStream);
            }
            comparisionFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception: " + ex.getMessage());
        }

        document.clear();
        assertEquals(0, document.getSegments().size());
        document.destroy();
    }

    @Test
    public void testOverwriteMiddleSaveDocument() {
        DeltaDocument document = openTempDeltaDocument();
        assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());
        document.replace(120, new byte[]{0x40, 0x41});

        try {
            document.save();

            InputStream comparisionFile;
            try (InputStream dataInputStream = document.getDataInputStream()) {
                comparisionFile = new FileInputStream(DeltaDocumentSaveTest.class.getResource(SAMPLE_OVERWRITTEN_MIDDLE).getFile());
                TestUtils.assertEqualsInputStream(comparisionFile, dataInputStream);
            }
            comparisionFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception: " + ex.getMessage());
        }

        document.clear();
        assertEquals(0, document.getSegments().size());
        document.destroy();
    }

    @Test
    public void testOverwriteEndSaveDocument() {
        DeltaDocument document = openTempDeltaDocument();
        assertEquals(SAMPLE_ALLBYTES_SIZE, document.getDataSize());
        document.replace(254, new byte[]{0x40, 0x41});

        try {
            document.save();

            InputStream comparisionFile;
            try (InputStream dataInputStream = document.getDataInputStream()) {
                comparisionFile = new FileInputStream(DeltaDocumentSaveTest.class.getResource(SAMPLE_OVERWRITTEN_END).getFile());
                TestUtils.assertEqualsInputStream(comparisionFile, dataInputStream);
            }
            comparisionFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception: " + ex.getMessage());
        }

        document.clear();
        assertEquals(0, document.getSegments().size());
        document.destroy();
    }

    public static DeltaDocument openTempDeltaDocument() {
        SegmentsRepository segmentsRepository = new SegmentsRepository();

        File sampleFile = new File(DeltaDocumentSaveTest.class.getResource(SAMPLE_ALLBYTES).getFile());
        try {
            File tempFile = File.createTempFile("deltahex-example", ".tmp");

            try (FileInputStream fileInput = new FileInputStream(sampleFile); FileOutputStream fileOutput = new FileOutputStream(tempFile)) {
                // Copy file
                byte[] buffer = new byte[4096];
                while (fileInput.available() > 0) {
                    int red = fileInput.read(buffer);
                    if (red < 0) {
                        break;
                    }
                    fileOutput.write(buffer, 0, red);
                }
            }

            FileDataSource fileSource = segmentsRepository.openFileSource(tempFile);
            return segmentsRepository.createDocument(fileSource);
        } catch (IOException ex) {
            Logger.getLogger(DeltaDocumentSaveTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}