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
