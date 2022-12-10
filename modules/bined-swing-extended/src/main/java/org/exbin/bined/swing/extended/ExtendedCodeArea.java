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
package org.exbin.bined.swing.extended;

import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.extended.capability.ExtendedScrollingCapable;
import org.exbin.bined.extended.capability.PositionCodeTypeCapable;
import org.exbin.bined.extended.capability.ShowUnprintablesCapable;
import org.exbin.bined.swing.capability.AntialiasingCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.extended.capability.ColorsProfileCapable;
import org.exbin.bined.swing.extended.capability.LayoutProfileCapable;
import org.exbin.bined.swing.extended.capability.ThemeProfileCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.swing.extended.capability.CaretsProfileCapable;
import org.exbin.bined.capability.EditModeCapable;

/**
 * Code area component extended interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface ExtendedCodeArea extends SelectionCapable, CaretCapable, ScrollingCapable, ExtendedScrollingCapable, ViewModeCapable,
        CodeTypeCapable, EditModeCapable, CharsetCapable, CodeCharactersCaseCapable, FontCapable,
        RowWrappingCapable, ClipboardCapable, AntialiasingCapable, ShowUnprintablesCapable, PositionCodeTypeCapable,
        ColorsProfileCapable, LayoutProfileCapable, ThemeProfileCapable, CaretsProfileCapable {
}
