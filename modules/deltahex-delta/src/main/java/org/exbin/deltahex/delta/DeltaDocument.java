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

import org.exbin.deltahex.delta.list.DefaultDoublyLinkedList;

/**
 * Delta document defined as sequence of segments.
 *
 * @version 0.1.1 2016/09/20
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDocument {

    private final SegmentsRepository repository;
    private DataSegment firstSegment;

    private long dataLength = 0;
    private long pointerPosition;
    private DataSegment pointerSegment;

    private final DefaultDoublyLinkedList<DataSegment> segments = new DefaultDoublyLinkedList<>();

    public DeltaDocument(SegmentsRepository repository) {
        this.repository = repository;
//        dataLength = document.getDocumentSize();
//        DataSegment fullFileSegment = new FileSegment(0, dataLength);
//        segments.add(fullFileSegment);
//        pointerPosition = 0;
//        pointerSegment = fullFileSegment;
    }

    public DataSegment getFirstSegment() {
        return firstSegment;
    }

    public SegmentsRepository getRepository() {
        return repository;
    }
    
    public long getDocumentSize() {
        // TODO cache?
        long length = 0;
        DataSegment segment = firstSegment;
        while (segment != null) {
            length += segment.getLength();
            segment = segment.getNext();
        }
        return length;
    }
}
