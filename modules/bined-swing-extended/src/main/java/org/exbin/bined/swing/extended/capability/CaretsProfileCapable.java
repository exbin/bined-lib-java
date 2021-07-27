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
package org.exbin.bined.swing.extended.capability;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.swing.extended.caret.ExtendedCodeAreaCaretsProfile;

/**
 * Support for cursor carets profiling.
 *
 * @version 0.2.0 2019/08/08
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CaretsProfileCapable {

    /**
     * Returns carets profile.
     *
     * @return carets profile
     */
    @Nullable
    ExtendedCodeAreaCaretsProfile getCaretsProfile();

    /**
     * Sets carets profile.
     *
     * @param caretsProfile carets profile
     */
    void setCaretsProfile(ExtendedCodeAreaCaretsProfile caretsProfile);
}
