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