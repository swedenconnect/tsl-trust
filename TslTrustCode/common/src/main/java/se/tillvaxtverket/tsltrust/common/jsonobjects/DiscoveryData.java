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
package se.tillvaxtverket.tsltrust.common.jsonobjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Discovery data data class for JSON serialization
 */
public class DiscoveryData {
    public String EntityID="";
    public List<DisplayNameData> DisplayNames = new ArrayList<DisplayNameData>();
    public List<DesciptionData> Description = new ArrayList<DesciptionData>();
    public List<LogotypeData> Logotypes = new ArrayList<LogotypeData>();
    public List<String> EntityCategories = new ArrayList<String>();
    public List<String> LoA = new ArrayList<String>();

    public DiscoveryData() {
    }

    public DisplayNameData addDisplayName(String lang, String value){
        DisplayNameData dispName = new DisplayNameData(lang, value);
        DisplayNames.add(dispName);
        return dispName;
    }
    public LogotypeData addLogotype(String uri, int width, int height){
        LogotypeData logo = new LogotypeData(uri, width, height);
        Logotypes.add(logo);
        return logo;
    }
    
    public DesciptionData addDescription(String lang, String value){
        DesciptionData desc = new DesciptionData(lang, value);
        Description.add(desc);
        return desc;
    }
        
    public class DisplayNameData {

        public String lang;
        public String value;

        public DisplayNameData(String lang, String value) {
            this.lang = lang;
            this.value = value;
        }
        
    }
    public class LogotypeData {

        public String uri;
        public int width;
        public int height;

        public LogotypeData(String uri, int width, int height) {
            this.uri = uri;
            this.width = width;
            this.height = height;
        }
    }
    public class DesciptionData {

        public String lang;
        public String value;

        public DesciptionData(String lang, String value) {
            this.lang = lang;
            this.value = value;
        }
    }
    
}
