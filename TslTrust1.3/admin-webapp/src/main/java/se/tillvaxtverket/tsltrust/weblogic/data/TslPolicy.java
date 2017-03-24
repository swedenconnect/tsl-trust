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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * TSL policy database record data class
 */
public class TslPolicy implements Serializable {

    private Gson gson = new Gson();
    private static final Type listStringType = new TypeToken<List<String>>() {
    }.getType();
    private String tslPolicyName;
    private List<String> states, serviceTypes, statusTypes, signStatus;
    private String statesStr, serviceTypesStr, statusTypesStr, signStatusStr;
    int expiredTslGrace = -1;

    public TslPolicy() {
        tslPolicyName = "";
        states = new ArrayList<String>();
        serviceTypes = new ArrayList<String>();
        statusTypes = new ArrayList<String>();
        signStatus = new ArrayList<String>();
    }

    public void setTslPolicyName(String val) {
        this.tslPolicyName = val == null ? "" : val;
    }

    public void setExpiredTslGrace(int expiredTslGrace) {
        this.expiredTslGrace = expiredTslGrace;
    }

    public void setServiceTypes(List<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setSignStatus(List<String> signStatus) {
        this.signStatus = signStatus;
    }

    public void setStates(List<String> states) {
        this.states = states;
    }

    public void setStatusTypes(List<String> statusTypes) {
        this.statusTypes = statusTypes;
    }

    public void setServiceTypesStr(String val) {
        this.serviceTypesStr = val == null ? "[]" : val;
        this.serviceTypes = gson.fromJson(serviceTypesStr, listStringType);
        if (serviceTypes == null) {
            serviceTypes = new ArrayList<String>();
        }
    }

    public void setStatesStr(String val) {
        this.statesStr = val == null ? "[]" : val;
        this.states = gson.fromJson(statesStr, listStringType);
        if (states == null) {
            states = new ArrayList<String>();
        }
    }

    public void setSignStatusStr(String val) {
        this.signStatusStr = val == null ? "[]" : val;
        this.signStatus = gson.fromJson(signStatusStr, listStringType);
        if (signStatus == null) {
            signStatus = new ArrayList<String>();
        }
    }

    public void setStatusTypesStr(String val) {
        this.statusTypesStr = val == null ? "[]" : val;
        this.statusTypes = gson.fromJson(statusTypesStr, listStringType);
        if (statusTypes == null) {
            statusTypes = new ArrayList<String>();
        }
    }

    public String getTslPolicyName() {
        return tslPolicyName;
    }

    public int getExpiredTslGrace() {
        return expiredTslGrace;
    }

    public List<String> getServiceTypes() {
        return serviceTypes;
    }

    public List<String> getSignStatus() {
        return signStatus;
    }

    public List<String> getStates() {
        return states;
    }

    public List<String> getStatusTypes() {
        return statusTypes;
    }

    public String getServiceTypesStr() {
        this.serviceTypesStr = gson.toJson(serviceTypes);
        return serviceTypesStr;
    }

    public String getSignStatusStr() {
        this.signStatusStr = gson.toJson(signStatus);
        return signStatusStr;
    }

    public String getStatesStr() {
        this.statesStr = gson.toJson(states);
        return statesStr;
    }

    public String getStatusTypesStr() {
        this.statusTypesStr = gson.toJson(statusTypes);
        return statusTypesStr;
    }
}
