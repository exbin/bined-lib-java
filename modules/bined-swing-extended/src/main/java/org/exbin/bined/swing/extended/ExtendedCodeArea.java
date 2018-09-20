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
package org.exbin.bined.swing.extended;

import org.exbin.bined.swing.basic.DefaultCodeArea;
import org.exbin.bined.swing.capability.BasicColorsCapable;
import org.exbin.bined.swing.extended.capability.AntialiasingCapable;
import org.exbin.bined.swing.extended.capability.ShowingUnprintableCapable;

/**
 * Code area component extended interface.
 *
 * @version 0.2.0 2018/09/18
 * @author ExBin Project (https://exbin.org)
 */
public interface ExtendedCodeArea extends DefaultCodeArea, AntialiasingCapable, BasicColorsCapable, ShowingUnprintableCapable {
}
