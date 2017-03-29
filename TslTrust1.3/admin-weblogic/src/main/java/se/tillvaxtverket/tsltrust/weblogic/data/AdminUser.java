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


