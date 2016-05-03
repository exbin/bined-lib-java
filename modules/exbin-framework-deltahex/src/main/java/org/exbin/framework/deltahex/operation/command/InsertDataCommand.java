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
package org.exbin.framework.deltahex.operation.command;

import org.exbin.deltahex.HexadecimalData;
import org.exbin.deltahex.component.Hexadecimal;
import org.exbin.framework.deltahex.command.command.HexCommandType;
import org.exbin.framework.deltahex.operation.InsertDataOperation;

/**
 * Command for inserting data.
 *
 * @version 0.1.0 2016/05/03
 * @author ExBin Project (http://exbin.org)
 */
public class InsertDataCommand extends OpHexCommand {

    public InsertDataCommand(Hexadecimal hexadecimal, long position, HexadecimalData data) {
        super(hexadecimal);
        super.setOperation(new InsertDataOperation(hexadecimal, position, data));
    }

    @Override
    public HexCommandType getType() {
        return HexCommandType.DATA_INSERTED;
    }
}
