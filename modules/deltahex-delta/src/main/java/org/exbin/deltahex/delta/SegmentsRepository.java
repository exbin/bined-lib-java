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
 * @version 0.1.1 2016/11/09
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

        // Create save transformation
        Map<DataSegment, Long> saveMap = createSaveTransformation(savedDocument);

        // Apply transformation to other documents
        for (DeltaDocument document : documents) {
            if (document != savedDocument) {
                applySaveMap(document, saveMap, savedDocument.getFileSource());
            }
        }

        // Perform document save
        // Update document segments
        savedDocument.clear();
        DataSegment fullFileSegment = createFileSegment(fileSource, 0, fileSource.getFileLength());
        savedDocument.getSegments().add(fullFileSegment);
    }

    private Map<DataSegment, Long> createSaveTransformation(DeltaDocument savedDocument) {
        Map<DataSegment, Long> transformation = new HashMap<>();
        DefaultDoublyLinkedList<DataSegment> segments = savedDocument.getSegments();
        long position = 0;
        for (DataSegment segment : segments) {
            transformation.put(segment, position);
            position += segment.getLength();
        }

        return transformation;
    }

    private void applySaveMap(DeltaDocument document, Map<DataSegment, Long> saveMap, FileDataSource fileSource) {
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        long processed = 0;
        for (DataSegment segment : segments) {
            long segmentPosition = segment.getStartPosition();
            long segmentLength = segment.getLength();
            DataSegmentsMap segmentMap;
            if (segment instanceof FileSegment) {
                FileSegment fileSegment = (FileSegment) segment;
                FileDataSource segmentSource = fileSegment.getSource();
                segmentMap = fileSources.get(segmentSource);
            } else {
                MemorySegment memorySegment = (MemorySegment) segment;
                MemoryDataSource segmentSource = memorySegment.getSource();
                segmentMap = memorySources.get(segmentSource);
            }

            // Split segment by saved file segments
            segmentMap.focusFirstOverlay(segmentPosition, segmentLength);
            SegmentRecord record = segmentMap.pointerRecord;
            if (record != null) {
                while (processed < segmentLength && record.dataSegment.getStartPosition() <= segmentPosition + segmentLength) {
                    Long savePosition = saveMap.get(record.dataSegment);
                    if (savePosition != null && record.dataSegment.getStartPosition() + record.dataSegment.getLength() >= segmentPosition + processed) {
                        // Replace segment for file segment pointing to after-save position
                        long replacedLength = record.dataSegment.getLength();
                        long startPosition = record.dataSegment.getStartPosition();
                        if (segmentPosition > startPosition) {
                            replacedLength -= segmentPosition - startPosition;
                            startPosition = segmentPosition;
                        }
                        if (replacedLength > segmentPosition + segmentLength - record.dataSegment.getStartPosition()) {
                            replacedLength = segmentPosition + segmentLength - record.dataSegment.getStartPosition();
                        }

                        if (processed < startPosition) {
                            loadFileSegmentsAsData(document, fileSource, processed, startPosition - processed);
                        }

                        FileSegment newSegment = createFileSegment(fileSource, savePosition + startPosition, replacedLength);
                        document.remove(startPosition, replacedLength);
                        document.insert(startPosition, newSegment);
                    }

                    record = segmentMap.records.nextTo(record);
                    if (record == null) {
                        break;
                    }
                }
            }
        }
        if (processed < document.getDataSize()) {
            loadFileSegmentsAsData(document, fileSource, processed, document.getDataSize() - processed);
        }
    }

    /**
     * Loads data for segments which will not be available after save.
     */
    private void loadFileSegmentsAsData(DeltaDocument document, FileDataSource fileSource, long startPosition, long length) {
        document.getSegment(startPosition);
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

    public void updateSegment(DataSegment segment, long position, long length) {
        if (segment instanceof MemorySegment) {
            DataSegmentsMap segmentsMap = memorySources.get(((MemorySegment) segment).getSource());
            segmentsMap.updateSegment(segment, position, length);
        } else {
            DataSegmentsMap segmentsMap = fileSources.get(((FileSegment) segment).getSource());
            segmentsMap.updateSegment(segment, position, length);
        }
    }

    public void updateSegmentLength(DataSegment segment, long length) {
        if (segment instanceof MemorySegment) {
            DataSegmentsMap segmentsMap = memorySources.get(((MemorySegment) segment).getSource());
            segmentsMap.updateSegmentLength(segment, length);
        } else {
            DataSegmentsMap segmentsMap = fileSources.get(((FileSegment) segment).getSource());
            segmentsMap.updateSegmentLength(segment, length);
        }
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
            segmentsMap.updateSegmentLength(memorySegment, segmentPosition + 1);
            if (memorySegment.getStartPosition() + segmentPosition >= memorySource.getDataSize()) {
                memorySource.setDataSize(memorySegment.getStartPosition() + segmentPosition + 1);
            }
        }
        memorySource.setByte(memorySegment.getStartPosition() + segmentPosition, value);
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, BinaryData insertedData) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, position, insertedData.getDataSize());
        memorySegment.getSource().insert(position, insertedData);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedData.getDataSize());
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, position, insertedData.getDataSize());
        memorySegment.getSource().insert(position, insertedData, insertedDataOffset, insertedDataLength);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedDataLength);
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, byte[] insertedData) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, position, insertedData.length);
        memorySegment.getSource().insert(position, insertedData);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedData.length);
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, position, insertedData.length);
        memorySegment.getSource().insert(position, insertedData, insertedDataOffset, insertedDataLength);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedDataLength);
    }

    public void insertMemoryData(MemorySegment memorySegment, long position, long length) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, position, length);
        memorySegment.getSource().insert(position, length);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + length);
    }

    public void insertUninitializedMemoryData(MemorySegment memorySegment, long position, long length) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, position, length);
        memorySegment.getSource().insertUninitialized(position, length);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + length);
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
     * Creates copy of segment.
     *
     * @param segment original segment
     * @param offset segment area offset
     * @param length segment area length
     * @return copy of segment
     */
    public DataSegment copySegment(DataSegment segment, long offset, long length) {
        if (segment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) segment;
            return createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + offset, length);
        } else {
            FileSegment fileSegment = (FileSegment) segment;
            return createFileSegment(fileSegment.getSource(), fileSegment.getStartPosition() + offset, length);
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
            addRecord(record);
        }

        private void addRecord(SegmentRecord record) {
            long startPosition = record.dataSegment.getStartPosition();
            long length = record.dataSegment.getLength();
            long maxPosition = startPosition + length;
            if (pointerRecord == null) {
                record.maxPosition = maxPosition;
                records.add(0, record);
                SegmentRecord nextRecord = record.next;
                while (nextRecord != null && nextRecord.maxPosition < maxPosition) {
                    nextRecord.maxPosition = maxPosition;
                    nextRecord = records.nextTo(nextRecord);
                }
            } else {
                if (pointerRecord.maxPosition > maxPosition) {
                    maxPosition = pointerRecord.maxPosition;
                } else {
                    SegmentRecord nextRecord = pointerRecord.next;
                    while (nextRecord != null && maxPosition > nextRecord.maxPosition) {
                        nextRecord.maxPosition = maxPosition;
                        nextRecord = records.nextTo(nextRecord);
                    }
                }
                record.maxPosition = maxPosition;
                records.addAfter(pointerRecord, record);
            }
        }

        private void remove(DataSegment segment) {
            SegmentRecord record = findRecord(segment);

            if (record.dataSegment == segment) {
                removeRecord(record);
            } else {
                throw new IllegalStateException("Segment requested for removal was not found");
            }
        }

        private void removeRecord(SegmentRecord record) {
            SegmentRecord prevRecord = records.prevTo(record);
            SegmentRecord nextRecord = records.nextTo(record);
            long recordEndPosition = record.dataSegment.getStartPosition() + record.dataSegment.getLength();
            records.remove(record);
            pointerRecord = prevRecord;
            long prevMaxPosition = 0;
            if (prevRecord != null) {
                prevMaxPosition = prevRecord.maxPosition;
            }

            // Update maxPosition cached values
            if (nextRecord != null && prevMaxPosition < recordEndPosition) {
                long maxPosition = nextRecord.dataSegment.getStartPosition() + nextRecord.dataSegment.getLength();
                if (prevMaxPosition > maxPosition) {
                    maxPosition = prevMaxPosition;
                }

                while (nextRecord != null && maxPosition < nextRecord.maxPosition) {
                    nextRecord.maxPosition = maxPosition;
                    nextRecord = records.nextTo(nextRecord);

                    if (nextRecord != null) {
                        long nextMaxPosition = nextRecord.dataSegment.getStartPosition() + nextRecord.dataSegment.getLength();
                        if (nextMaxPosition > maxPosition) {
                            maxPosition = nextMaxPosition;
                        }
                    }
                }
            }
        }

        private boolean hasMoreSegments() {
            return records.first() != null && records.first() != records.last();
        }

        private void updateSegment(DataSegment segment, long position, long length) {
            // TODO optimalization - update only affected records without removing current record
            SegmentRecord record = findRecord(segment);
            if (record.dataSegment == segment) {
                removeRecord(record);
                if (segment instanceof MemorySegment) {
                    ((MemorySegment) segment).setStartPosition(position);
                    ((MemorySegment) segment).setLength(length);
                } else {
                    ((FileSegment) segment).setStartPosition(position);
                    ((FileSegment) segment).setLength(length);
                }
                focusSegment(segment.getStartPosition(), segment.getLength());
                addRecord(record);
            } else {
                throw new IllegalStateException("Segment requested for update was not found");
            }
        }

        private void updateSegmentLength(DataSegment segment, long length) {
            // TODO optimalization - update only affected records without removing current record
            SegmentRecord record = findRecord(segment);
            if (record.dataSegment == segment) {
                removeRecord(record);
                if (segment instanceof MemorySegment) {
                    ((MemorySegment) segment).setLength(length);
                } else {
                    ((FileSegment) segment).setLength(length);
                }
                addRecord(record);
            } else {
                throw new IllegalStateException("Segment requested for update was not found");
            }
        }

        private SegmentRecord findRecord(DataSegment segment) {
            focusSegment(segment.getStartPosition(), segment.getLength());
            SegmentRecord record = pointerRecord;
            while (record.dataSegment != segment
                    && record.dataSegment.getStartPosition() == segment.getStartPosition()
                    && record.dataSegment.getLength() == segment.getLength()) {
                record = records.prevTo(record);
            }

            return record;
        }

        /**
         * Aligns focus segment on last segment at given start position and
         * length or last segment before given position or null if there is no
         * such segment.
         *
         * @param startPosition start position
         * @param length length
         */
        private void focusSegment(long startPosition, long length) {
            if (pointerRecord == null) {
                pointerRecord = records.first();
            }

            if (pointerRecord == null) {
                return;
            }

            if (startPosition > pointerRecord.dataSegment.getStartPosition()
                    || (pointerRecord.dataSegment.getStartPosition() == startPosition && length >= pointerRecord.dataSegment.getLength())) {
                // Forward direction traversal
                SegmentRecord record = pointerRecord;
                while (startPosition > record.dataSegment.getStartPosition()
                        || (record.dataSegment.getStartPosition() == startPosition && length >= record.dataSegment.getLength())) {
                    pointerRecord = record;
                    record = records.nextTo(pointerRecord);
                    if (record == null) {
                        break;
                    }
                }
            } else {
                // Backward direction traversal
                while (startPosition < pointerRecord.dataSegment.getStartPosition()
                        || (pointerRecord.dataSegment.getStartPosition() == startPosition && length < pointerRecord.dataSegment.getLength())) {
                    pointerRecord = records.prevTo(pointerRecord);
                    if (pointerRecord == null) {
                        break;
                    }
                }
            }
        }

        /**
         * Aligns focus segment on first segment which overlays given area.
         *
         * @param startPosition start position
         * @param length length
         */
        private void focusFirstOverlay(long startPosition, long length) {
            if (pointerRecord == null) {
                pointerRecord = records.first();
            }

            if (pointerRecord == null) {
                return;
            }

            if (pointerRecord.maxPosition < startPosition) {
                // Forward direction traversal
                SegmentRecord record = pointerRecord;
                while (pointerRecord.maxPosition < startPosition) {
                    pointerRecord = record;
                    record = records.nextTo(pointerRecord);
                    if (record == null) {
                        break;
                    }
                }
            } else {
                // Backward direction traversal
                SegmentRecord record = pointerRecord;
                while (pointerRecord.maxPosition < startPosition) {
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
