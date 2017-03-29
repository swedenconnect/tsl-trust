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
