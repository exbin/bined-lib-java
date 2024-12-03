/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.javafx.basic;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.javafx.CodeAreaCharAssessor;
import org.exbin.bined.javafx.CodeAreaPaintState;

/**
 * Default code area character assessor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCharAssessor implements CodeAreaCharAssessor {

    protected final CodeAreaCharAssessor parentAssessor;

    protected char[] charMapping = null;

    protected long dataSize;
    protected int maxBytesPerChar;
    protected byte[] rowData;
    protected Charset charset;
    private CharsetDecoder decoder;
    private ByteBuffer byteBuffer;

    public DefaultCodeAreaCharAssessor() {
        parentAssessor = null;
    }

    public DefaultCodeAreaCharAssessor(@Nullable CodeAreaCharAssessor parentAssessor) {
        this.parentAssessor = parentAssessor;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPainterState) {
        dataSize = codeAreaPainterState.getDataSize();
        Charset painterCharset = codeAreaPainterState.getCharset();
        maxBytesPerChar = codeAreaPainterState.getMaxBytesPerChar();
        if (charset != painterCharset) {
            charMapping = null;
            decoder = painterCharset.newDecoder();
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            byteBuffer = ByteBuffer.allocate(maxBytesPerChar);
            this.charset = painterCharset;
        }
        rowData = codeAreaPainterState.getRowData();
    }

    @Override
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        if (byteOnRow >= rowData.length) {
            return ' ';
        }

        if (maxBytesPerChar > 1) {
            decoder.reset();

            if (rowDataPosition + maxBytesPerChar > dataSize) {
                byteBuffer.clear();
                byteBuffer.put(rowData, byteOnRow, (int) (dataSize - rowDataPosition));
            } else {
                byteBuffer.rewind();
                byteBuffer.put(rowData, byteOnRow, maxBytesPerChar);
            }
            byteBuffer.rewind();
            try {
                CharBuffer decodeResult = decoder.decode(byteBuffer);
                return decodeResult.get();
            } catch (CharacterCodingException | BufferUnderflowException ex) {
            }
        } else {
            if (charMapping == null) {
                buildCharMapping();
            }

            return charMapping[rowData[byteOnRow] & 0xFF];
        }

        return ' ';
    }

    @Override
    public char getPreviewCursorCharacter(long rowDataPosition, int byteOnRow, int charOnRow, byte[] cursorData, int cursorDataLength, CodeAreaSection section) {
        if (cursorDataLength == 0) {
            return ' ';
        }

        if (maxBytesPerChar > 1) {
            decoder.reset();
            byteBuffer.rewind();
            byteBuffer.put(cursorData, 0, cursorDataLength);
            byteBuffer.rewind();
            try {
                CharBuffer decodeResult = decoder.decode(byteBuffer);
                return decodeResult.get();
            } catch (CharacterCodingException | BufferUnderflowException ex) {
            }
        } else {
            if (charMapping == null) {
                buildCharMapping();
            }

            return charMapping[cursorData[0] & 0xFF];
        }

        return ' ';
    }

    @Nonnull
    @Override
    public Optional<CodeAreaCharAssessor> getParentCharAssessor() {
        return Optional.ofNullable(parentAssessor);
    }

    /**
     * Precomputes widths for basic ascii characters.
     */
    private void buildCharMapping() {
        charMapping = new char[256];
        ByteBuffer buffer = ByteBuffer.allocate(1);
        for (int i = 0; i < 256; i++) {
            buffer.rewind();
            buffer.put((byte) i);
            decoder.reset();
            buffer.rewind();
            CharBuffer decodeResult;
            try {
                decodeResult = decoder.decode(buffer);
                charMapping[i] = decodeResult.get();
            } catch (CharacterCodingException | BufferUnderflowException ex) {
                charMapping[i] = ' ';
            }
        }
    }
}
