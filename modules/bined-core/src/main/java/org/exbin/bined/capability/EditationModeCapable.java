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
package org.exbin.bined.capability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationModeChangedListener;
import org.exbin.bined.EditationOperation;

/**
 * Support for editation mode capability.
 *
 * @version 0.2.0 2019/07/07
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface EditationModeCapable {

    boolean isEditable();

    @Nonnull
    EditationMode getEditationMode();

    void setEditationMode(EditationMode editationMode);

    /**
     * Returns currently active operation as set or enforced by current
     * editation mode.
     *
     * @return active editation operation
     */
    @Nonnull
    EditationOperation getActiveOperation();

    /**
     * Returns currently enforced editation operation.
     *
     * @return editation operation
     */
    @Nonnull
    EditationOperation getEditationOperation();

    void setEditationOperation(EditationOperation editationOperation);

    void addEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener);

    void removeEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener);
}
