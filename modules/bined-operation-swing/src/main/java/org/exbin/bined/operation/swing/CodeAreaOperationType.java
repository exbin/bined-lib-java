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
package org.exbin.bined.operation.swing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.operation.BinaryDataOperationType;

/**
 * Operation type enumeration.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum CodeAreaOperationType implements BinaryDataOperationType {

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
    EDIT_DATA("Edit data"),
    /**
     * Compound operation.
     */
    COMPOUND("Compound operation");

    @Nullable
    private final String name;

    CodeAreaOperationType(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
