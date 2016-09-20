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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository of delta segments.
 *
 * @version 0.1.1 2016/09/20
 * @author ExBin Project (http://exbin.org)
 */
public class SegmentsRepository {

    private final List<FileDataSource> fileSources = new ArrayList<>();
    private final List<MemoryDataSource> memorySources = new ArrayList<>();

    private final List<DeltaDocument> documents = new ArrayList<>();
    private final Map<FileSegment, FileDataSource> fileSegments = new HashMap<>();
    private final Map<MemorySegment, MemoryDataSource> memorySegments = new HashMap<>();

    public SegmentsRepository() {
    }

    public void addFileSource(FileDataSource fileSource) {
        fileSources.add(fileSource);
    }

    public void addMemoryDataSource(MemoryDataSource memorySource) {
        memorySources.add(memorySource);
    }

    public void closeFileSource(FileDataSource fileSource) {
        // TODO
    }
}
