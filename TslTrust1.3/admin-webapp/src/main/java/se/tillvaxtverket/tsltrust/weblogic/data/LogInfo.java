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

import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.TimeSource;

/**
 * Generic log record database record data class
 * This class is subclassed by the Admin, Major and Console database record classes
 */
public class LogInfo implements TTConstants {

    protected long time = 0;
    protected String timeStr, event = "", description = "", origin = "";

    /**
     * Constructor
     */
    public LogInfo() {
        timeNudge();
    }

    /**
     * Constructs new log event
     * @param event Event string value
     * @param description Description of event
     * @param origin Object generating log event (normally declared as "this")
     */
    public LogInfo(String event, String description, Object origin) {
        time = TimeSource.getCurrentTime();
        this.event = b(event);
        this.description = description;
        this.origin = origin.getClass().getName();
        timeNudge();
    }

    /**
     * Constructs new log event
     * @param event Event string
     * @param description Event description
     * @param origin description
     */
    public LogInfo(String event, String description, String origin) {
        time = TimeSource.getCurrentTime();
        this.event = b(event);
        this.description = description;
        this.origin = origin;
        timeNudge();
    }

    /**
     * Constructs log event
     * @param req Request model object
     * @param type 
     */
    public LogInfo(RequestModel req, EventType type) {
        this(req, null, type, "");
        timeNudge();
    }

    public LogInfo(RequestModel req, AdminUser admin, EventType type, String parameter) {
        time = TimeSource.getCurrentTime();
        boolean success = getRequestEvent(req, admin, type, parameter);
        if (!success) {
            event = "Unsupported event type";
        }
        timeNudge();
    }

    private void timeNudge() {
        //prevent multiple events at same time
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
        }
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getFormattedTime() {
        return TIME_FORMAT.format(new Date(time));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    protected String getTimeStr() {
        return BigInteger.valueOf(time).toString();
    }

    protected void setTimeStr(String timeStr) {
        time = (new BigInteger(timeStr)).longValue();
        this.timeStr = timeStr;
    }

    protected static String b(String str) {
        return "<b>" + str + "</b>";
    }

    protected String getRequestEvent(RequestModel req, EventType type, String parameter) {
        return null;
    }

    protected boolean getRequestEvent(RequestModel req, AdminUser admin, EventType type, String parameter) {
        switch (type) {
            case AdminReq:
                getAdminReqEvent(req, "Success");
                return true;
            case AdminReqBadPW:
                getAdminReqEvent(req, "Denied (Bad/wrong password)");
                return true;
            case UserLogin:
                getLoginEvent(req);
                return true;
            case AdminGrant:
                getAdminGrantEvent(req, admin, parameter);
                return true;
            case AdminDeny:
                getAdminDenyEvent(req, admin, parameter);
                return true;
        }
        return false;
    }

    protected void getAdminReqEvent(RequestModel req, String desc) {
        event = b("Admin privilege request");
        description = desc;
        origin = getUserIdString(req);
    }

    protected void getLoginEvent(RequestModel req) {
        event = b("User login");
        try {
            description = USER_TYPES[req.getAuthzLvl()];
        } catch (Exception ex) {
            description = "Unknown user type";
        }
        origin = getUserIdString(req);
    }

    protected void getAdminGrantEvent(RequestModel req, AdminUser user, String parameter) {
        event = b(parameter + " privileges granted");
        description = getUserIdString(user);
        origin = getAdminIdString(req);
    }

    protected void getAdminDenyEvent(RequestModel req, AdminUser user, String parameter) {
        event = b(parameter);
        description = getUserIdString(user);
        origin = getAdminIdString(req);
    }

    protected String getUserIdString(AdminUser user) {
        return b(user.getDisplayName()) + " (" + user.getAttributeId() + "="
                + b(user.getIdentifier()) + " Idp=" + b(user.getIdpDisplayName()) + ")";
    }

    protected static String getUserIdString(RequestModel req) {
        return b(req.getUserName()) + " (" + req.getUserIdAttribute() + "="
                + b(req.getUserId()) + " Idp=" + b(req.getIdpDisplayName()) + ")";
    }

    public static String getAdminIdString(RequestModel req) {
        return b(req.getUserName()) + " (id=" + b(req.getUserId()) + ")";
    }

    public static enum EventType {

        AdminReq, AdminReqBadPW, UserLogin, AdminGrant, AdminDeny
    }
}
