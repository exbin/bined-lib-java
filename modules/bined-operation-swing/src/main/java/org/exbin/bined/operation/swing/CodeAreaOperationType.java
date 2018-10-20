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
package org.exbin.bined.operation.swing;

import javax.annotation.Nullable;

/**
 * Operation type enumeration.
 *
 * @version 0.2.0 2018/10/20
 * @author ExBin Project (https://exbin.org)
 */
public enum CodeAreaOperationType {

    /**
     * Insert data operation.
     */
    INSERT_DATA("Insert data"),
    /**
     * Remove data operation.
     */
    REMOVE_DATA("Remove data"),
    /**
     * Modify data operation.
     */
    MODIFY_DATA("Modify data"),
    /**
     * Move data operation.
     */
    MOVE_DATA("Move data"),
    /**
     * Edit data operation.
     */
    EDIT_DATA("Edit data");

    @Nullable
    private final String caption;

    private CodeAreaOperationType(@Nullable String caption) {
        this.caption = caption;
    }

    @Nullable
    public String getCaption() {
        return caption;
    }
}
