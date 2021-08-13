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
package org.exbin.bined.swt.basic;

import org.exbin.bined.capability.BackgroundPaintCapable;
import org.exbin.bined.capability.BasicScrollingCapable;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.swt.CodeAreaSwtControl;
import org.exbin.bined.swt.capability.AntialiasingCapable;
import org.exbin.bined.swt.capability.BasicColorsCapable;
import org.exbin.bined.swt.capability.FontCapable;
import org.exbin.bined.capability.EditModeCapable;

/**
 * Code area component default interface.
 *
 * @version 0.2.0 2018/12/25
 * @author ExBin Project (https://exbin.org)
 */
public interface DefaultCodeArea extends CodeAreaSwtControl, SelectionCapable, CaretCapable, ScrollingCapable, BasicScrollingCapable, ViewModeCapable,
        CodeTypeCapable, EditModeCapable, CharsetCapable, CodeCharactersCaseCapable, FontCapable,
        BackgroundPaintCapable, RowWrappingCapable, ClipboardCapable, BasicColorsCapable, AntialiasingCapable {
}
