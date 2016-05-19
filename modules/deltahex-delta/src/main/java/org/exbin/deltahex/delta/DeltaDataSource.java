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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Data source for access to resource with keeping list of modifications to it.
 *
 * Data source is opened in read only mode and there structure keeping all the
 * changes.
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDataSource {

    private RandomAccessFile file;

    private long fileLength = 0;
    private final DataRecordGroup rootGroup;

    private final LinkedList<DocumentSegment> segments = new LinkedList<>();
    private final Map<DocumentSegment, DocumentSection> sections = new HashMap<>();

    public DeltaDataSource(File sourceFile) throws FileNotFoundException, IOException {
        file = new RandomAccessFile(sourceFile, "rw");
        fileLength = file.length();
        DocumentSegment rootSegment = new DocumentSegment(0, fileLength);
        segments.add(rootSegment);
        DocumentSection rootSection = new DocumentSection(rootSegment, 0);
        DepthRecord depthRecord = computeDepth();
        rootGroup = new DataRecordGroup(rootSection, 0, depthRecord.depth, depthRecord.groupSize);
    }

    public long getFileLength() {
        return fileLength;
    }

    public byte getByte(long position) throws IOException {
        DataRecord record = rootGroup.getRecord(position);
        byte value;
        if (record instanceof DataRecordPage) {
            DataRecordPage page = (DataRecordPage) record;
            value = page.getByte(file, (int) (position % DataRecordPage.PAGE_SIZE), segments, sections);
        } else {
            DataRecordGroup group = (DataRecordGroup) record;
            DocumentSection section = group.getSection();
            long groupPosition = group.getPosition();
            long bytePosition = section.getSectionPosition() + (position - groupPosition);
            file.seek(bytePosition);
            value = file.readByte();
        }
        return value;
    }

    public void remove(long startFrom, long length) {
        // TODO throw new UnsupportedOperationException("Not supported yet.");
    }

    private DepthRecord computeDepth() {
        if (fileLength < DataRecordPage.PAGE_SIZE) {
            return new DepthRecord(0, fileLength);
        }

        int depth = 1;
        long groupSize = DataRecordPage.PAGE_SIZE;
        if (fileLength > groupSize * DataRecordGroup.GROUP_SIZE) {
            depth++;
            groupSize *= DataRecordGroup.GROUP_SIZE;
        }

        return new DepthRecord(depth, groupSize);
    }

    private static class DepthRecord {

        public DepthRecord(int depth, long groupSize) {
            this.depth = depth;
            this.groupSize = groupSize;
        }

        int depth;
        long groupSize;
    }
}
