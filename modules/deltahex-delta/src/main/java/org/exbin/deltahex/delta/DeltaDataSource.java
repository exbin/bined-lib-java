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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Data source for access to resource with keeping list of modifications to it.
 *
 * Data source is opened in read only mode and there structure keeping all the
 * changes.
 *
 * @version 0.1.0 2016/06/07
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDataSource {

    private final RandomAccessFile file;
    private final DeltaDataPageWindow window;

    public DeltaDataSource(File sourceFile) throws FileNotFoundException, IOException {
        file = new RandomAccessFile(sourceFile, "rw");
        window = new DeltaDataPageWindow(this);
    }

    public long getFileLength() throws IOException {
        return file.length();
    }

    RandomAccessFile getFile() {
        return file;
    }

    public DeltaDataPageWindow getWindow() {
        return window;
    }
}
