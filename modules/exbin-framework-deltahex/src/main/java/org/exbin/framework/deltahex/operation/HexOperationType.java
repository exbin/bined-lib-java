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
package org.exbin.framework.deltahex.operation;

/**
 * Operation type enumeration.
 *
 * @version 0.1.0 2016/04/22
 * @author ExBin Project (http://exbin.org)
 */
public enum HexOperationType {

    /**
     * Block added.
     */
    ADD_BLOCK("Add block"),
    /**
     * Block deleted.
     */
    DELETE_BLOCK("Delete block"),
    /**
     * Block modified.
     */
    MODIFY_BLOCK("Modify block"),
    /**
     * Block moved.
     */
    MOVE_BLOCK("Move block");

    private final String caption;

    private HexOperationType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }
}
