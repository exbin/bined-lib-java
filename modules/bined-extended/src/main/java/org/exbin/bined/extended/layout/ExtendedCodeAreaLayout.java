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
package org.exbin.bined.extended.layout;

import org.exbin.bined.CaretPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.extended.ExtendedCodeAreaStructure;

/**
 * Layout interface for extended code area.
 *
 * @version 0.2.0 2019/02/03
 * @author ExBin Project (https://exbin.org)
 */
public interface ExtendedCodeAreaLayout {

    int computeBytesPerRow(int halfCharsPerPage, ExtendedCodeAreaStructure structure);

    int computeHalfCharsPerRow(ExtendedCodeAreaStructure structure);

    long computeRowsPerDocument(ExtendedCodeAreaStructure structure);

    int computePositionByte(int rowHalfCharPosition, ExtendedCodeAreaStructure structure);

    int computeFirstCodeHalfCharPos(int byteOffset, ExtendedCodeAreaStructure structure);

    int computeLastCodeHalfCharPos(int byteOffset, ExtendedCodeAreaStructure structure);

    CaretPosition computeMovePosition(CaretPosition position, MovementDirection direction, ExtendedCodeAreaStructure structure, int rowsPerPage);
}
