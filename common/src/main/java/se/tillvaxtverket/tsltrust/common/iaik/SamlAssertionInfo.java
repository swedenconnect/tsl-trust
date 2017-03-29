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
package se.tillvaxtverket.tsltrust.common.iaik;

import java.util.ArrayList;

/**
 *
 * @author stefan
 */
public class SamlAssertionInfo {
    public ArrayList<Attribute> authContextInfo = new ArrayList<Attribute>();
    public ArrayList<Attribute> idAttributes = new ArrayList<Attribute>();

    public SamlAssertionInfo() {
    }
    
    public static Attribute getNewAttribute(){
        return new Attribute();
    }
    
    public static Attribute getNewAttribute(String name, String id, String value){
        return new Attribute(name, id, value);
    }
    
    public static class Attribute{
        public String name;
        public String id;
        public String value;
        public String certOid;

        public Attribute(String name, String id, String value) {
            this.name = name;
            this.id = id;
            this.value = value;
        }
        public Attribute(String name, String id, String value, String certOid) {
            this.name = name;
            this.id = id;
            this.value = value;
            this.certOid = certOid;
        }

        public Attribute() {
        }
        
    }
}