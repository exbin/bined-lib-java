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
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.OutOfBoundsException;

/**
 * Repository of delta segments.
 *
 * @version 0.1.1 2016/09/24
 * @author ExBin Project (http://exbin.org)
 */
public class SegmentsRepository {

    private final Map<FileDataSource, List<FileSegment>> fileSources = new HashMap<>();
    private final Map<MemoryDataSource, List<MemorySegment>> memorySources = new HashMap<>();

    private final List<DeltaDocument> documents = new ArrayList<>();

    public SegmentsRepository() {
    }

    public FileDataSource openFileSource(File sourceFile) throws IOException {
        FileDataSource fileSource = new FileDataSource(sourceFile);
        List<FileSegment> fileSegments = new ArrayList<>();
        fileSources.put(fileSource, fileSegments);
        return fileSource;
    }

    public void closeFileSource(FileDataSource fileSource) {
        fileSource.close();
    }

    public void saveFileSource(FileDataSource fileSource) {
        // TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MemoryDataSource openMemorySource() {
        MemoryDataSource memorySource = new MemoryDataSource();
        List<MemorySegment> memorySegments = new ArrayList<>();
        memorySources.put(memorySource, memorySegments);
        return memorySource;
    }

    public void closeMemorySource(MemoryDataSource memorySource) {
        // TODO
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
        List<FileSegment> segments = fileSources.get(fileSource);
        segments.add(fileSegment);
        return fileSegment;
    }

    public void dropFileSegment(FileSegment fileSegment) {
        List<FileSegment> segments = fileSources.get(fileSegment.getSource());
        segments.remove(fileSegment);
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
        List<MemorySegment> segments = memorySources.get(memorySource);
        segments.add(memorySegment);
        return memorySegment;
    }

    public void dropMemorySegment(MemorySegment memorySegment) {
        List<MemorySegment> segments = memorySources.get(memorySegment.getSource());
        segments.remove(memorySegment);
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
        List<MemorySegment> segments = memorySources.get(memorySource);
        if (segments.size() > 1) {
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
        List<MemorySegment> memorySegments = memorySources.get(memorySegment.getSource());
        for (MemorySegment segment : memorySegments) {
            if (segment != memorySegment) {
                if (position >= segment.getStartPosition() && position < segment.getStartPosition() + segment.getLength()) {
                    // TODO: If segments collide, copy on write
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }
        }
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
}
