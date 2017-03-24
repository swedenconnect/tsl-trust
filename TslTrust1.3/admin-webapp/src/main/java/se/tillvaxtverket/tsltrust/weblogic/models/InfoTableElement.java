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
package se.tillvaxtverket.tsltrust.weblogic.models;

/**
 * Data class in the foldable InfoTable model for html tables
 */
public class InfoTableElement {

    private String[] values = new String[]{};
    private String[] cellClasses = new String[]{};
    private InfoTableSection tableSection = null;
    private boolean subStructure = false, defaultRowClass = true,
            overrideDefaultFolding = false, unfolded = false;
    private String rowClass;
    private boolean siblingFolding = false, siblingHeading = false;
    private int siblingHeadCols = 0;

    public InfoTableElement() {
    }

    public InfoTableElement(String[] values, String[] cellClasses) {
        this.values = values;
        this.cellClasses = cellClasses;
    }

    public InfoTableElement(String[] values) {
        this.values = values;
    }

    public InfoTableElement(String value) {
        this.values = new String[]{value};
    }

    public InfoTableElement(String value1, String value2) {
        this.values = new String[]{value1, value2};
    }

    public InfoTableElement(String value1, String value2, String value3) {
        this.values = new String[]{value1, value2, value3};
    }

    public InfoTableElement(InfoTableSection tableSection) {
        this.tableSection = tableSection;
        subStructure = true;
    }

    public boolean hasSubStructure() {
        return subStructure;
    }

    public InfoTableSection getTableSection() {
        return tableSection;
    }

    public void setTableSection(InfoTableSection tableSection) {
        this.tableSection = tableSection;
        subStructure = true;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
        subStructure = false;
    }

    public String[] getCellClasses() {
        return cellClasses;
    }

    public void setCellClasses(String[] cellClasses) {
        this.cellClasses = cellClasses;
    }

    public boolean isDefaultRowClass() {
        return defaultRowClass;
    }

    public String getRowClass() {
        return rowClass;
    }

    public void setRowClass(String rowClass) {
        this.rowClass = rowClass;
        defaultRowClass = false;
    }

    public boolean isSubStructure() {
        return subStructure;
    }

    public boolean isUnfolded() {
        return unfolded;
    }

    public void setUnfolded(boolean unfolded) {
        this.unfolded = unfolded;
        overrideDefaultFolding = true;
    }

    public boolean isOverrideDefaultFolding() {
        return overrideDefaultFolding;
    }

    public boolean isSiblingFolding() {
        return siblingFolding;
    }

    public void setSiblingFolding(boolean siblingFolding) {
        this.siblingFolding = siblingFolding;
    }

    public int getSiblingHeadCols() {
        return siblingHeadCols;
    }

    public void setSiblingHeadCols(int siblingHeadCols) {
        this.siblingHeadCols = siblingHeadCols;
    }

    public boolean isSiblingHeading() {
        return siblingHeading;
    }

    public void setSiblingHeading(boolean siblingHeading) {
        this.siblingHeading = siblingHeading;
    }
    
}
