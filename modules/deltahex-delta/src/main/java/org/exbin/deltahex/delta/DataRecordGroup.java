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

import java.util.HashMap;
import java.util.Map;

/**
 * Document group record.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class DataRecordGroup implements DataRecord {

    public static final int GROUP_SIZE = 1024;

    private final long position;
    private final int depth;
    // Cached value for group size depending directly on depth
    private final long groupSize;
    private final Map<Integer, DataRecord> children = new HashMap<>();

    private DocumentSection section;

    public DataRecordGroup(DocumentSection section, long position, int depth, long groupSize) {
        this.section = section;
        this.position = position;
        this.depth = depth;
        this.groupSize = groupSize;
    }

    @Override
    public DocumentSection getSection() {
        return section;
    }

    @Override
    public void setSection(DocumentSection position) {
        this.section = section;
    }

    public long getPosition() {
        return position;
    }

    public int getDepth() {
        return depth;
    }

    public DataRecord getRecord(long recordPosition) {
        int childIndex = (int) ((recordPosition - position) / groupSize);
        DataRecord childRecord = children.get(childIndex);
        if (childRecord == null) {
            return this;
        } else if (childRecord instanceof DataRecordGroup) {
            return ((DataRecordGroup) childRecord).getRecord(recordPosition);
        } else {
            return childRecord;
        }
    }
}
