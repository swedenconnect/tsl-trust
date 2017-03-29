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
