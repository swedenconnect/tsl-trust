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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.common.html.elements.SelectElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Class providing filters for display of columns in the TSL trust service table
 */
public class DbTableFilter extends SelectElement implements HtmlConstants{

    SelectElement filter;
    List<String> filterOptions = new ArrayList<String>();
    int selectIdx;

    public DbTableFilter(String filterId, String allText, String selected) {
        super(filterId);
        selectIdx = getInt(selected);
        this.addStyle("margin-left", "10px");
        this.addAction(ONCHANGE, EXECUTE_OPTION_FUNCTION, new String[]{
                    MAIN_DATA_AREA, //Load element for ajax response
                    filterId, //Ajax request id
                    filterId //Element holding the selected filter
                });
        addFilterOption(allText);
    }

    public void addFilterOption(String option) {
        if (filterOptions.contains(option)) {
            return;
        }
        filterOptions.add(option);
        addOption(option, filterOptions.size()==selectIdx+1);
    }

    public void addUrlFilterOption(String option) {
        addFilterOption(ExtractorUtil.stripRefUrl(option));
    }
    
    public String getFilterString(){
        if (selectIdx==0){
            return "";
        }
        return filterOptions.get(selectIdx);
    }
    
    public boolean isFilterMatch(String compareString){
        if (selectIdx==0){
            return true;
        }
        return compareString.equals(filterOptions.get(selectIdx));
    }
    
    public boolean isUrlFilterMatch(String compareString){
        return isFilterMatch(ExtractorUtil.stripRefUrl(compareString));
    }

    private int getInt(String intString) {
        int val = 0;
        try {
            val = Integer.parseInt(intString);
        } catch (Exception ex) {
        }
        return val;
    }
}
