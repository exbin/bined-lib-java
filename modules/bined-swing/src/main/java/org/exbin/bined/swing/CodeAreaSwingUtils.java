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
package org.exbin.bined.swing;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.ScrollPaneConstants;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.auxiliary.paged_data.BinaryData;

/**
 * Hexadecimal editor component swing utilities.
 *
 * @version 0.2.0 2018/12/24
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaSwingUtils {

    public static final int MIN_MONOSPACE_CODE_POINT = 0x1F;
    public static final int MAX_MONOSPACE_CODE_POINT = 0x1C3;
    public static final int INV_SPACE_CODE_POINT = 0x7f;
    public static final int EXCEPTION1_CODE_POINT = 0x8e;
    public static final int EXCEPTION2_CODE_POINT = 0x9e;

    public static int MAX_COMPONENT_VALUE = 255;
    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String FALLBACK_CLIPBOARD = "clipboard";
    private static Clipboard clipboard = null;

    private CodeAreaSwingUtils() {
    }

    public static boolean areSameColors(@Nullable Color color, @Nullable Color comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    @Nonnull
    public static Color createOddColor(Color color) {
        return new Color(
                computeOddColorComponent(color.getRed()),
                computeOddColorComponent(color.getGreen()),
                computeOddColorComponent(color.getBlue()));
    }

    public static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? - 16 : 16);
    }

    @Nonnull
    public static Color createNegativeColor(Color color) {
        return new Color(
                MAX_COMPONENT_VALUE - color.getRed(),
                MAX_COMPONENT_VALUE - color.getGreen(),
                MAX_COMPONENT_VALUE - color.getBlue());
    }

    @Nonnull
    public static Color computeGrayColor(Color color) {
        int grayLevel = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return new Color(grayLevel, grayLevel, grayLevel);
    }

    public static int getVerticalScrollBarPolicy(ScrollBarVisibility scrollBarVisibility) {
        switch (scrollBarVisibility) {
            case NEVER:
                return ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
            case ALWAYS:
                return ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
            case IF_NEEDED:
                return ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
            default:
                throw new IllegalStateException("Unexpected scrollBarVisibility type " + scrollBarVisibility.name());
        }
    }

    public static int getHorizontalScrollBarPolicy(ScrollBarVisibility scrollBarVisibility) {
        switch (scrollBarVisibility) {
            case NEVER:
                return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
            case ALWAYS:
                return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
            case IF_NEEDED:
                return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
            default:
                throw new IllegalStateException("Unexpected scrollBarVisibility type " + scrollBarVisibility.name());
        }
    }

    public static boolean canPaste(Clipboard clipboard, DataFlavor binaryDataFlavor) {
        try {
            return clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public static int getMetaMaskDown() {
        // TODO: Replace with getMenuShortcutKeyMaskEx when switching to java 10 or later
        try {
            switch (java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
                case java.awt.Event.CTRL_MASK:
                    return KeyEvent.CTRL_DOWN_MASK;
                case java.awt.Event.META_MASK:
                    return KeyEvent.META_DOWN_MASK;
                case java.awt.Event.SHIFT_MASK:
                    return KeyEvent.SHIFT_DOWN_MASK;
                case java.awt.Event.ALT_MASK:
                    return KeyEvent.ALT_DOWN_MASK;
                default:
                    return KeyEvent.CTRL_DOWN_MASK;
            }
        } catch (java.awt.HeadlessException ex) {
            return KeyEvent.CTRL_DOWN_MASK;
        }
    }

    /**
     * A shared {@code Clipboard}.
     *
     * @return clipboard clipboard instance
     */
    @Nonnull
    public static Clipboard getClipboard() {
        if (clipboard == null) {
            try {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            } catch (SecurityException e) {
                clipboard = new Clipboard(FALLBACK_CLIPBOARD);
            }
        }

        return clipboard;
    }

    @ParametersAreNonnullByDefault
    public static class BinaryDataClipboardData implements ClipboardData {

        private final BinaryData data;
        private final DataFlavor binaryDataFlavor;

        public BinaryDataClipboardData(BinaryData data, DataFlavor binaryDataFlavor) {
            this.data = data;
            this.binaryDataFlavor = binaryDataFlavor;
        }

        @Nonnull
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{binaryDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Nonnull
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binaryDataFlavor)) {
                return data;
            } else {
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                data.saveToStream(byteArrayStream);
                return byteArrayStream.toString(DEFAULT_ENCODING);
            }
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

    @ParametersAreNonnullByDefault
    public static class CodeDataClipboardData implements ClipboardData {

        private final BinaryData data;
        private final DataFlavor binaryDataFlavor;
        private final CodeType codeType;
        private final CodeCharactersCase charactersCase;

        public CodeDataClipboardData(BinaryData data, DataFlavor binaryDataFlavor, CodeType codeType, CodeCharactersCase charactersCase) {
            this.data = data;
            this.binaryDataFlavor = binaryDataFlavor;
            this.codeType = codeType;
            this.charactersCase = charactersCase;
        }

        @Nonnull
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{binaryDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Nonnull
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binaryDataFlavor)) {
                return data;
            } else {
                int charsPerByte = codeType.getMaxDigitsForByte() + 1;
                int textLength = (int) (data.getDataSize() * charsPerByte);
                if (textLength > 0) {
                    textLength--;
                }

                char[] targetData = new char[textLength];
                Arrays.fill(targetData, ' ');
                for (int i = 0; i < data.getDataSize(); i++) {
                    CodeAreaUtils.byteToCharsCode(data.getByte(i), codeType, targetData, i * charsPerByte, charactersCase);
                }
                return new String(targetData);
//                return new ByteArrayInputStream(new String(dataTarget).getBytes(textPlainUnicodeFlavor.getParameter(MIME_CHARSET)));
            }
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

    public static interface ClipboardData extends Transferable, ClipboardOwner {

        void dispose();
    }
}
