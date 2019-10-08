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
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.delta.swap.SwapFileRepository;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for swap file repository.
 *
 * @version 0.2.0 2019/10/08
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SwapFileRepositoryTest {

    public static final String SAMPLE_FILES_PATH = "/org/exbin/bined/delta/resources/test/";
    public static final String SAMPLE_ALLBYTES = SAMPLE_FILES_PATH + "allbytes.dat";
    public static final int SAMPLE_ALLBYTES_SIZE = 256;

    public SwapFileRepositoryTest() {
    }

    @Test
    public void testSwapFileRepository() {
        SwapFileRepository swapFileRepository = new SwapFileRepository();
        byte[] emptyData = new byte[swapFileRepository.getPageSize()];

        long page1 = swapFileRepository.allocatePage();
        byte[] page1Data = swapFileRepository.getPage(page1);
        Assert.assertArrayEquals(emptyData, page1Data);

        File sampleFile = new File(SwapFileRepositoryTest.class.getResource(SAMPLE_ALLBYTES).getFile());
        byte[] sampleData;
        try {
            sampleData = Files.readAllBytes(sampleFile.toPath());
            System.arraycopy(sampleData, 0, page1Data, 0, SAMPLE_ALLBYTES_SIZE);
        } catch (IOException ex) {
            Logger.getLogger(SwapFileRepositoryTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Unable to read sample file");
        }

        swapFileRepository.setPage(page1, page1Data);
        
        byte[] page1Modified = swapFileRepository.getPage(page1);
        Assert.assertArrayEquals(page1Data, page1Modified);
        swapFileRepository.close();
    }
}
