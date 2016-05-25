/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.deltahex.operation.command;

import org.exbin.deltahex.Hexadecimal;
import org.exbin.deltahex.operation.ModifyDataOperation;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Command for modifying data.
 *
 * @version 0.1.0 2016/05/03
 * @author ExBin Project (http://exbin.org)
 */
public class ModifyDataCommand extends OpHexCommand {

    public ModifyDataCommand(Hexadecimal hexadecimal, long position, BinaryData data) {
        super(hexadecimal);
        super.setOperation(new ModifyDataOperation(hexadecimal, position, data));
    }

    @Override
    public HexCommandType getType() {
        return HexCommandType.DATA_MODIFIED;
    }
}
