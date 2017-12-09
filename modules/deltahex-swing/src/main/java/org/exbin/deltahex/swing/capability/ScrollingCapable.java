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
package org.exbin.deltahex.swing.capability;

import org.exbin.deltahex.ScrollBarVisibility;
import org.exbin.deltahex.capability.WorkerCapability;
import org.exbin.deltahex.swing.basic.CodeAreaScrollPosition;
import org.exbin.deltahex.swing.basic.HorizontalScrollUnit;
import org.exbin.deltahex.swing.basic.VerticalScrollUnit;

/**
 * Support for code type capability.
 *
 * @version 0.2.0 2017/12/09
 * @author ExBin Project (http://exbin.org)
 */
public interface ScrollingCapable {

    CodeAreaScrollPosition getScrollPosition();

    ScrollBarVisibility getVerticalScrollBarVisibility();

    void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility);

    VerticalScrollUnit getVerticalScrollUnit();

    void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit);

    ScrollBarVisibility getHorizontalScrollBarVisibility();

    void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility);

    HorizontalScrollUnit getHorizontalScrollUnit();

    void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit);

    public static class ScrollingCapability implements WorkerCapability {

    }
}
