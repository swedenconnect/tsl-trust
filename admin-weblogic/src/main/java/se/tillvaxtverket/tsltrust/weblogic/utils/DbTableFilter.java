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
