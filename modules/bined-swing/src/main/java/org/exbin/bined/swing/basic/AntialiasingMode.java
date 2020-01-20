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
package org.exbin.bined.swing.basic;

import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Enumeration of supported anti-aliasing modes.
 *
 * @version 0.2.0 2020/01/16
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum AntialiasingMode {

    OFF,
    AUTO,
    DEFAULT,
    BASIC,
    GASP,
    LCD_HRGB,
    LCD_HBGR,
    LCD_VRGB,
    LCD_VBGR;

    @Nonnull
    public Object getAntialiasingHint(Graphics2D g) {
        Object antialiasingHint;
        switch (this) {
            case AUTO: {
                Toolkit tk = Toolkit.getDefaultToolkit();
                Map map = (Map) (tk.getDesktopProperty("awt.font.desktophints"));
                if (map != null) {
                    // Use system one only if it's not default
                    antialiasingHint = map.get(RenderingHints.KEY_TEXT_ANTIALIASING);
                    if (antialiasingHint != null && antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {
                        return antialiasingHint;
                    }
                }

                // Basic fallback detection
                if (g.getDeviceConfiguration().getDevice().getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                } else {
                    antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
                }
                break;
            }
            case BASIC: {
                antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
                break;
            }
            case GASP: {
                antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
                break;
            }
            case DEFAULT: {
                antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
                break;
            }
            case LCD_HRGB: {
                antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                break;
            }
            case LCD_HBGR: {
                antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
                break;
            }
            case LCD_VRGB: {
                antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
                break;
            }
            case LCD_VBGR: {
                antialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected antialiasing type " + name());
            }
        }

        return antialiasingHint;
    }
}
