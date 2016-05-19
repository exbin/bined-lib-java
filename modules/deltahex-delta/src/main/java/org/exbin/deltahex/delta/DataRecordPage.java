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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Single document page record.
 * 
 * Page contains list of modification in natural order which can be only on of the type:
 * - deletion of given bytes
 * - insertion of given bytes
 * - modification of given bytes
 *
 * @version 0.1.0 2016/05/19
 * @author ExBin Project (http://exbin.org)
 */
public class DataRecordPage implements DataRecord {

    public static final int PAGE_SIZE = 1024;

    private DocumentSection section;
    private List<DataPageModification> modifications = new LinkedList<>();

    public DataRecordPage(DocumentSection section) {
        this.section = section;
    }

    @Override
    public DocumentSection getSection() {
        return section;
    }

    @Override
    public void setSection(DocumentSection position) {
        this.section = position;
    }

    public List<DataPageModification> getModifications() {
        return modifications;
    }

    public byte getByte(RandomAccessFile file, int pagePosition, LinkedList<DocumentSegment> segments, Map<DocumentSegment, DocumentSection> sections) throws IOException {
        for (DataPageModification modification : modifications) {
            if (modification.getOffset() > pagePosition) {
                break;
            }
        }
                
        // TODO Go thru all modifications and find proper byte
        long bytePosition = section.getSegment().getStartPosition() + section.getOffset() + pagePosition;
        file.seek(bytePosition);
        return file.readByte();
    }
}
