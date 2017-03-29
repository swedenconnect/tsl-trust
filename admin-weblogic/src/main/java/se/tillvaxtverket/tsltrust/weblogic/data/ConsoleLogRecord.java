/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.tillvaxtverket.tsltrust.weblogic.data;

import java.io.Serializable;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;

/**
 * Console log database record data class
 */
public class ConsoleLogRecord extends LogInfo implements Serializable {

    public ConsoleLogRecord() {
    }

    public ConsoleLogRecord(String event, String description, Object origin) {
        super(event, description, origin);
    }

    public ConsoleLogRecord(String event, String description, String origin) {
        super(event, description, origin);
    }

    public ConsoleLogRecord(RequestModel req, EventType type) {
        super(req, type);
    }

    public ConsoleLogRecord(RequestModel req, AdminUser admin, EventType type, String parameter) {
        super(req, admin, type, parameter);
    }
}
