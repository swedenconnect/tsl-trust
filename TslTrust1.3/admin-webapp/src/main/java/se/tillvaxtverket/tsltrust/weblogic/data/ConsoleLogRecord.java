/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
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
