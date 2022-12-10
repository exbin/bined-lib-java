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
package org.exbin.bined;

/**
 * Enumeration of scrollbar visibility modes.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum ScrollBarVisibility {

    /**
     * Never show scrollbars even when area is bigger then available space.
     */
    NEVER,
    /**
     * Show scrollbars only if area is bigger then available space.
     */
    IF_NEEDED,
    /**
     * Always show scrollbar regardless of the available space.
     */
    ALWAYS
}
