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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.deltahex.delta.list.DefaultDoublyLinkedList;
import org.exbin.deltahex.delta.list.DoublyLinkedItem;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.OutOfBoundsException;

/**
 * Repository of delta segments.
 *
 * @version 0.1.1 2016/10/03
 * @author ExBin Project (http://exbin.org)
 */
public class SegmentsRepository {

    private final Map<FileDataSource, DataSegmentsMap> fileSources = new HashMap<>();
    private final Map<MemoryDataSource, DataSegmentsMap> memorySources = new HashMap<>();

    private final List<DeltaDocument> documents = new ArrayList<>();

    public SegmentsRepository() {
    }

    public FileDataSource openFileSource(File sourceFile) throws IOException {
        FileDataSource fileSource = new FileDataSource(sourceFile);
        fileSources.put(fileSource, new DataSegmentsMap());
        return fileSource;
    }

    public void closeFileSource(FileDataSource fileSource) {
        // TODO
        fileSource.close();
    }

    public MemoryDataSource openMemorySource() {
        MemoryDataSource memorySource = new MemoryDataSource();
        memorySources.put(memorySource, new DataSegmentsMap());
        return memorySource;
    }

    public void closeMemorySource(MemoryDataSource memorySource) {
        // TODO
        memorySource.clear();
    }

    /**
     * Creates empty delta document.
     *
     * @return delta document
     */
    public DeltaDocument createDocument() {
        DeltaDocument document = new DeltaDocument(this);
        documents.add(document);
        return document;
    }

    /**
     * Creates delta document for given file source.
     *
     * @param fileSource file source
     * @return delta document
     * @throws IOException if input/output error
     */
    public DeltaDocument createDocument(FileDataSource fileSource) throws IOException {
        DeltaDocument document = new DeltaDocument(this, fileSource);
        documents.add(document);
        return document;
    }

    /**
     * Save document to it's source file and update all documents.
     *
     * @param savedDocument document to save
     * @throws java.io.IOException if input/output error
     */
    public void saveDocument(DeltaDocument savedDocument) throws IOException {
        FileDataSource fileSource = savedDocument.getFileSource();

        // Create transformation document
        DeltaDocument transformationDocument = new DeltaDocument(this);

        // Apply inversion to other document
        for (DeltaDocument document : documents) {
            if (document != savedDocument) {
//                applyTransformationDocument(document, transformationDocument);
            }
        }

        // Perform document save
        // Update document segments
        savedDocument.clear();
        DataSegment fullFileSegment = new FileSegment(fileSource, 0, fileSource.getFileLength());
        savedDocument.getSegments().add(fullFileSegment);
    }

    /**
     * Creates new file segment on given file source.
     *
     * @param fileSource file source
     * @param startPosition start position
     * @param length length
     * @return file segment
     */
    public FileSegment createFileSegment(FileDataSource fileSource, long startPosition, long length) {
        try {
            if (startPosition + length > fileSource.getFileLength()) {
                throw new OutOfBoundsException("");
            }
        } catch (IOException ex) {
            Logger.getLogger(SegmentsRepository.class.getName()).log(Level.SEVERE, null, ex);
        }

        FileSegment fileSegment = new FileSegment(fileSource, startPosition, length);
        DataSegmentsMap segmentsMap = fileSources.get(fileSource);
        segmentsMap.add(fileSegment);
        return fileSegment;
    }

    public void dropFileSegment(FileSegment fileSegment) {
        DataSegmentsMap segmentsMap = fileSources.get(fileSegment.getSource());
        segmentsMap.remove(fileSegment);
    }

    public MemorySegment createMemorySegment() {
        return createMemorySegment(openMemorySource(), 0, 0);
    }

    /**
     * Creates new memory segment on given memory source.
     *
     * @param memorySource memory source
     * @param startPosition start position
     * @param length length
     * @return memory segment
     */
    public MemorySegment createMemorySegment(MemoryDataSource memorySource, long startPosition, long length) {
        if (startPosition + length > memorySource.getDataSize()) {
            memorySource.setDataSize(startPosition + length);
        }

        MemorySegment memorySegment = new MemorySegment(memorySource, startPosition, length);
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        segmentsMap.add(memorySegment);
        return memorySegment;
    }

    public void dropMemorySegment(MemorySegment memorySegment) {
        DataSegmentsMap segmentsMap = memorySources.get(memorySegment.getSource());
        segmentsMap.remove(memorySegment);
    }

    public void dropSegment(DataSegment segment) {
        if (segment instanceof FileSegment) {
            dropFileSegment((FileSegment) segment);
        } else {
            dropMemorySegment((MemorySegment) segment);
        }
    }

    public void dropDocument(DeltaDocument document) {
        document.clear();
        documents.remove(document);
    }

    /**
     * Sets byte to given segment.
     *
     * Handles shared memory between multiple segments.
     *
     * @param memorySegment memory segment
     * @param segmentPosition relative position to segment start
     * @param value value to set
     */
    public void setMemoryByte(MemorySegment memorySegment, long segmentPosition, byte value) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        if (segmentsMap.hasMoreSegments()) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        if (segmentPosition >= memorySegment.getLength()) {
            memorySegment.setLength(segmentPosition + 1);
            if (memorySegment.getStartPosition() + segmentPosition >= memorySource.getDataSize()) {
                memorySource.setDataSize(memorySegment.getStartPosition() + segmentPosition + 1);
            }
        }
        memorySource.setByte(memorySegment.getStartPosition() + segmentPosition, value);
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, BinaryData insertedData) {
        detachMemoryArea(memorySegment, position, insertedData.getDataSize());
        memorySegment.getSource().insert(position, insertedData);
        memorySegment.setLength(memorySegment.getLength() + insertedData.getDataSize());
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, byte[] insertedData) {
        detachMemoryArea(memorySegment, position, insertedData.length);
        memorySegment.getSource().insert(position, insertedData);
        memorySegment.setLength(memorySegment.getLength() + insertedData.length);
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, long length) {
        detachMemoryArea(memorySegment, position, length);
        memorySegment.getSource().insert(position, length);
        memorySegment.setLength(memorySegment.getLength() + length);
    }

    public void insertUninitializedMemoryData(MemorySegment memorySegment, long position, long length) {
        detachMemoryArea(memorySegment, position, length);
        memorySegment.getSource().insertUninitialized(position, length);
        memorySegment.setLength(memorySegment.getLength() + length);
    }

    /**
     * Detaches all other memory segments crossing given area of provided memory
     * segment.
     *
     * @param memorySegment provided memory segment
     * @param position position
     * @param length length
     */
    public void detachMemoryArea(MemorySegment memorySegment, long position, long length) {
        DataSegmentsMap segmentsMap = memorySources.get(memorySegment.getSource());
        // TODO
//        for (MemorySegment segment : segmentsMap.getAllSegments()) {
//            if (segment != memorySegment) {
//                if (position >= segment.getStartPosition() && position < segment.getStartPosition() + segment.getLength()) {
//                    // TODO: If segments collide, copy on write
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
//            }
//        }
    }

    /**
     * Creates copy of segment.
     *
     * @param segment original segment
     * @return copy of segment
     */
    public DataSegment copySegment(DataSegment segment) {
        if (segment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) segment;
            return createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition(), memorySegment.getLength());
        } else {
            FileSegment fileSegment = (FileSegment) segment;
            return createFileSegment(fileSegment.getSource(), fileSegment.getStartPosition(), fileSegment.getLength());
        }
    }

    /**
     * Mapping of segments to data source.
     *
     * Segments are suppose to be kept ordered by start position and length with
     * max position computed.
     */
    private class DataSegmentsMap {

        private final DefaultDoublyLinkedList<SegmentRecord> records = new DefaultDoublyLinkedList<>();
        private SegmentRecord pointerRecord = null;

        public DataSegmentsMap() {
        }

        private void add(DataSegment segment) {
            focusSegment(segment.getStartPosition(), segment.getLength());
            SegmentRecord record = new SegmentRecord();
            record.dataSegment = segment;
            long maxPosition = segment.getStartPosition() + segment.getLength();
            if (pointerRecord == null) {
                record.maxPosition = maxPosition;
                records.add(0, record);
                SegmentRecord nextRecord = record.next;
                while (nextRecord != null && nextRecord.maxPosition < maxPosition) {
                    nextRecord.maxPosition = maxPosition;
                    nextRecord = records.nextTo(nextRecord);
                }
            } else {
                if (pointerRecord.maxPosition > segment.getStartPosition() + segment.getLength()) {
                    maxPosition = pointerRecord.maxPosition;
                } else {
                    SegmentRecord nextRecord = pointerRecord.next;
                    while (nextRecord != null && nextRecord.maxPosition < maxPosition) {
                        nextRecord.maxPosition = maxPosition;
                        nextRecord = records.nextTo(nextRecord);
                    }
                }
                record.maxPosition = maxPosition;
                records.addAfter(pointerRecord, record);
            }
        }

        private void remove(DataSegment segment) {
            focusSegment(segment.getStartPosition(), segment.getLength());
            SegmentRecord record = pointerRecord;
            while (record.dataSegment != segment
                    && record.dataSegment.getStartPosition() == segment.getStartPosition()
                    && record.dataSegment.getLength() == segment.getLength()) {
                record = records.prevTo(record);
            }

            if (record.dataSegment == segment) {
                SegmentRecord prevRecord = records.prevTo(record);
                SegmentRecord nextRecord = records.nextTo(record);
                records.remove(record);
                pointerRecord = prevRecord;

                // Update maxPosition cached values
                if (nextRecord != null) {
                    long maxPosition = nextRecord.dataSegment.getStartPosition() + nextRecord.dataSegment.getLength();
                    if (prevRecord != null) {
                        long prevMaxPosition = prevRecord.dataSegment.getStartPosition() + prevRecord.dataSegment.getLength();
                        if (prevMaxPosition > maxPosition) {
                            maxPosition = prevMaxPosition;
                        }
                    }
                    while (nextRecord != null && maxPosition < nextRecord.maxPosition) {
                        long nextMaxPosition = nextRecord.dataSegment.getStartPosition() + nextRecord.dataSegment.getLength();
                        if (nextMaxPosition == nextRecord.maxPosition) {
                            break;
                        }
                        if (nextMaxPosition > maxPosition) {
                            maxPosition = nextMaxPosition;
                        }
                        nextRecord.maxPosition = maxPosition;
                        nextRecord = records.nextTo(nextRecord);
                    }
                }
            } else {
                throw new IllegalStateException("Segment requested for removal was not found");
            }
        }

        private boolean hasMoreSegments() {
            return records.first() != null && records.first() != records.last();
        }

        private void updateSegment(DataSegment segment) {
            // TODO optimalization
            remove(segment);
            add(segment);
        }

        /**
         * Aligns focus segment on last segment at given start position and
         * length or last segment before given position.
         *
         * @param startPosition
         * @param length
         */
        private void focusSegment(long startPosition, long length) {
            if (pointerRecord == null) {
                pointerRecord = records.first();
            }

            if (pointerRecord == null) {
                return;
            }

            if (pointerRecord.dataSegment.getStartPosition() < startPosition
                    || (pointerRecord.dataSegment.getStartPosition() == startPosition && pointerRecord.dataSegment.getLength() <= length)) {
                // Forward direction traversal
                SegmentRecord record = pointerRecord;
                while (record.dataSegment.getStartPosition() < startPosition
                        || (record.dataSegment.getStartPosition() == startPosition && record.dataSegment.getLength() <= length)) {
                    pointerRecord = record;
                    record = records.nextTo(pointerRecord);
                    if (record == null) {
                        break;
                    }
                }
            } else {
                // Backward direction traversal
                SegmentRecord record = pointerRecord;
                while (record.dataSegment.getStartPosition() > startPosition
                        || (record.dataSegment.getStartPosition() == startPosition && record.dataSegment.getLength() > length)) {
                    pointerRecord = record;
                    record = records.prevTo(record);
                    if (record == null) {
                        pointerRecord = null;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Internal structure for segment and cached maximum position.
     */
    private static class SegmentRecord implements DoublyLinkedItem {

        SegmentRecord prev = null;
        SegmentRecord next = null;

        DataSegment dataSegment;
        long maxPosition;

        @Override
        public DoublyLinkedItem getNext() {
            return next;
        }

        @Override
        public void setNext(DoublyLinkedItem next) {
            this.next = (SegmentRecord) next;
        }

        @Override
        public DoublyLinkedItem getPrev() {
            return prev;
        }

        @Override
        public void setPrev(DoublyLinkedItem prev) {
            this.prev = (SegmentRecord) prev;
        }
    }
}
