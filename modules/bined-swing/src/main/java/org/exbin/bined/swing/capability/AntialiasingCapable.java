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
package org.exbin.bined.swing.capability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.swing.basic.AntialiasingMode;

/**
 * Support for anti-aliasing capability.
 *
 * @version 0.2.0 2017/11/25
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface AntialiasingCapable {

    /**
     * Returns antialiasing mode for text painting.
     *
     * @return antialiasing mode
     */
    @Nonnull
    AntialiasingMode getAntialiasingMode();

    /**
     * Sets antialiasing mode for text painting.
     *
     * @param antialiasingMode antialiasing mode
     */
    void setAntialiasingMode(AntialiasingMode antialiasingMode);
}
