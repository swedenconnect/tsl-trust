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

import java.util.ArrayList;
import java.util.List;

/**
 * User database record data class
 */
public class AdminUser {
    private String displayName="", identifier="", idpEntityId="", targets="", attributeId="", idpDisplayName="";
    private int id, authLevel;
    private List<String> targetList = new ArrayList<String>();

    public AdminUser(String displayName, String identifier, String idpEntityId, String targets, String attributeId, int id, int authLevel) {
        this.displayName = displayName;
        this.identifier = identifier;
        this.idpEntityId = idpEntityId;
        this.targets = targets;
        this.id = id;
        this.authLevel = authLevel;
        this.attributeId = attributeId;
    }

    public AdminUser() {
    }

    
    
    public int getAuthLevel() {
        return authLevel;
    }

    public void setAuthLevel(int authLevel) {
        this.authLevel = authLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    public List<String> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<String> targetList) {
        this.targetList = targetList;
        if (targetList==null || targetList.isEmpty()){
            targets="";
            targetList=new ArrayList<String>();
            return;
        }
        StringBuilder b = new StringBuilder();
        for (String target:targetList){
            b.append(target).append(";");
        }
        b.deleteCharAt(b.lastIndexOf(";"));
        targets = b.toString();
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
        if (targets==null){
            targets="";
            targetList=new ArrayList<String>();
            return;
        }
        String[] split = targets.split(";");
        targetList = new ArrayList<String>(split.length);
        for (String target:split){
            targetList.add(target);
        }                
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getIdpDisplayName() {
        return idpDisplayName;
    }

    public void setIdpDisplayName(String idpDisplayName) {
        this.idpDisplayName = idpDisplayName;
    }
    
    
}


