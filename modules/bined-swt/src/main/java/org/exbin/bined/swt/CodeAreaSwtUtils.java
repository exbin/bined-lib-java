/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.bined.swt;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CharsetStreamTranslator;
import org.exbin.bined.swt.capability.CharAssessorPainterCapable;
import org.exbin.bined.swt.capability.ColorAssessorPainterCapable;

/**
 * Hexadecimal editor component SWT utilities.
 */
@NullMarked
public class CodeAreaSwtUtils {

    public static final int MIN_MONOSPACE_CODE_POINT = 0x1F;
    public static final int MAX_MONOSPACE_CODE_POINT = 0x1C3;
    public static final int INV_SPACE_CODE_POINT = 0x7f;
    public static final int EXCEPTION1_CODE_POINT = 0x8e;
    public static final int EXCEPTION2_CODE_POINT = 0x9e;

    public static int MAX_COLOR_COMPONENT_VALUE = 255;
    public static final String DEFAULT_ENCODING = CharsetStreamTranslator.DEFAULT_ENCODING;

    public static final String FALLBACK_CLIPBOARD = "clipboard";
    private static Clipboard clipboard = null;

    private CodeAreaSwtUtils() {
    }

    public static boolean areSameColors(@Nullable Color color, @Nullable Color comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    public static Color createOddColor(Color color) {
        return new Color(color.getDevice(),
                computeOddColorComponent(color.getRed()),
                computeOddColorComponent(color.getGreen()),
                computeOddColorComponent(color.getBlue()));
    }

    public static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? - 16 : 16);
    }

    public static Color createNegativeColor(Color color) {
        return new Color(color.getDevice(),
                MAX_COLOR_COMPONENT_VALUE - color.getRed(),
                MAX_COLOR_COMPONENT_VALUE - color.getGreen(),
                MAX_COLOR_COMPONENT_VALUE - color.getBlue());
    }

    public static Color computeGrayColor(Color color) {
        int grayLevel = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return new Color(color.getDevice(), grayLevel, grayLevel, grayLevel);
    }

    public static boolean canPaste(Clipboard clipboard, DataFlavor binaryDataFlavor) {
        try {
            return clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    /**
     * Finds specific color assessor if present.
     *
     * @param <T> color assessor
     * @param painter painter
     * @param assessorClass color assessor class
     * @return color assessor if present
     */
    @Nullable
    public static <T extends CodeAreaColorAssessor> T findColorAssessor(ColorAssessorPainterCapable painter, Class<T> assessorClass) {
        CodeAreaColorAssessor colorAssessor = painter.getColorAssessor();
        do {
            if (assessorClass.isInstance(colorAssessor)) {
                return assessorClass.cast(colorAssessor);
            }
            colorAssessor = colorAssessor.getParentColorAssessor().orElse(null);
        } while (colorAssessor != null);

        return null;
    }

    /**
     * Finds specific character assessor if present.
     *
     * @param <T> character assessor
     * @param painter painter
     * @param assessorClass character assessor class
     * @return character assessor if present
     */
    @Nullable
    public static <T extends CodeAreaCharAssessor> T findCharAssessor(CharAssessorPainterCapable painter, Class<T> assessorClass) {
        CodeAreaCharAssessor charAssessor = painter.getCharAssessor();
        do {
            if (assessorClass.isInstance(charAssessor)) {
                return assessorClass.cast(charAssessor);
            }
            charAssessor = charAssessor.getParentCharAssessor().orElse(null);
        } while (charAssessor != null);

        return null;
    }

    public static int getMetaMask() {
        return SWT.CTRL | SWT.COMMAND;
    }

    /**
     * A shared {@code Clipboard}.
     *
     * @return clipboard clipboard instance
     */
    public static Clipboard getClipboard() {
        // TODO convert to import org.eclipse.swt.dnd.Clipboard
        if (clipboard == null) {
            try {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            } catch (SecurityException e) {
                clipboard = new Clipboard(FALLBACK_CLIPBOARD);
            }
        }

        return clipboard;
    }

    @NullMarked
    public static class BinaryDataClipboardData implements ClipboardData {

        private final BinaryData data;
        private final DataFlavor binaryDataFlavor;
        @Nullable
        private final Charset charset;

        public BinaryDataClipboardData(BinaryData data, DataFlavor binaryDataFlavor, @Nullable Charset charset) {
            this.data = data;
            this.binaryDataFlavor = binaryDataFlavor;
            this.charset = charset;
        }

        public BinaryDataClipboardData(BinaryData data, DataFlavor binaryDataFlavor) {
            this(data, binaryDataFlavor, null);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{binaryDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binaryDataFlavor)) {
                return data;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                Object result;
                try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()) {
                    data.saveToStream(byteArrayStream);
                    result = charset == null ? byteArrayStream.toString(DEFAULT_ENCODING) : byteArrayStream.toString(charset.name());
                }
                return result;
            }

            throw new UnsupportedFlavorException(flavor);
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }

        @Override
        public void dispose() {
            data.dispose();
        }
    }

    public interface ClipboardData extends Transferable, ClipboardOwner {

        void dispose();
    }
}
