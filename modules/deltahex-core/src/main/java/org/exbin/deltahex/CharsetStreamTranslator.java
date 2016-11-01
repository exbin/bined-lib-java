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
package org.exbin.deltahex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Input stream translation class which converts from input charset to target
 * charset.
 *
 * @version 0.1.1 2016/11/01
 * @author ExBin Project (http://exbin.org)
 */
public class CharsetStreamTranslator extends InputStream {

    public static final int BYTE_BUFFER_SIZE = 1024;

    private final Charset inputCharset;
    private final Charset outputCharset;
    private final CharsetEncoder encoder;
    private final CharsetDecoder decoder;
    private final InputStream source;

    private final ByteBuffer inputBuffer;
    private final ByteBuffer outputBuffer;
    private final CharBuffer charBuffer;
    private boolean endOfInput = false;

    private int bufferOffset = 0;
    private int maxInputCharSize;
    private int maxOutputCharSize;

    public CharsetStreamTranslator(Charset inputCharset, Charset outputCharset, InputStream source, int bufferSize) {
        this.inputCharset = inputCharset;
        this.outputCharset = outputCharset;
        this.source = source;
        encoder = inputCharset.newEncoder();
        decoder = outputCharset.newDecoder();
        maxInputCharSize = (int) encoder.maxBytesPerChar();
        maxOutputCharSize = (int) decoder.maxCharsPerByte();
        inputBuffer = ByteBuffer.allocate(bufferSize);
        outputBuffer = ByteBuffer.allocate(bufferSize);
        charBuffer = CharBuffer.allocate(bufferSize);
    }

    public CharsetStreamTranslator(Charset inputCharset, Charset outputCharset, InputStream source) {
        this(inputCharset, outputCharset, source, BYTE_BUFFER_SIZE);
    }

    @Override
    public int read() throws IOException {
        boolean dataReady = outputBuffer.remaining() > 0;
        if (!dataReady) {
            if (endOfInput) {
                return -1;
            } else {
                processNext();
            }
        }

        return outputBuffer.get();
    }

    public void processNext() {
        byte[] buffer = inputBuffer.array();
        if (inputBuffer.remaining() > 0) {
            // Copy remaining data from previous processing
            int bufferOffset = inputBuffer.position();
            int length = inputBuffer.remaining();
            System.arraycopy(buffer, bufferOffset, buffer, 0, length);
            inputBuffer.rewind();
            inputBuffer.limit(length);
            inputBuffer.position(length);
        } else {
            inputBuffer.rewind();
            inputBuffer.limit(0);
        }

        int position = inputBuffer.position();
        int toRead = inputBuffer.capacity() - inputBuffer.position();
        inputBuffer.limit(position + toRead);
        int offset = position;
        while (toRead > 0) {
            try {
                int red = source.read(buffer, offset, toRead);
                if (red < 0) {
                    inputBuffer.limit(position + offset);
                    endOfInput = true;
                    break;
                }

                offset += red;
                toRead -= red;
            } catch (IOException ex) {
                Logger.getLogger(CharsetStreamTranslator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        inputBuffer.position(0);
        

        decoder.reset();
        CoderResult decodeResult = decoder.decode(inputBuffer, charBuffer, endOfInput);
        

        encoder.reset();
        outputBuffer.reset();
        CoderResult encodeResult = encoder.encode(charBuffer, outputBuffer, endOfInput);
    }
}
