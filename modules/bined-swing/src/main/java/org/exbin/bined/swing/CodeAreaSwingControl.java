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
package org.exbin.bined.swing;

import java.awt.Graphics;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.CodeAreaScrollPosition;

/**
 * Code area swing control.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CodeAreaSwingControl {

    /**
     * Paints the main component.
     *
     * @param g graphics
     */
    void paintComponent(Graphics g);

    /**
     * Rebuilds colors after UIManager change.
     */
    void resetColors();

    /**
     * Resets painter state for new painting.
     */
    void reset();

    /**
     * Requests update of the component layout.
     *
     * Notifies code area, that change of parameters will affect layout and it
     * should be recomputed and updated if necessary.
     */
    void updateLayout();

    /**
     * Updates scroll position.
     *
     * @param scrollPosition scroll position
     */
    void updateScrollPosition(CodeAreaScrollPosition scrollPosition);
}
