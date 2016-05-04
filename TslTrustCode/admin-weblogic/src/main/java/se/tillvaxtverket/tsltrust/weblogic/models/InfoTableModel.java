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

import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;

/**
 * Main data model class for data content in info table html tables
 */
public class InfoTableModel implements HtmlConstants {

    private String tableClass = "", tableHeadingClass="",
            tableFoldIconClass = "", foldNamePrefix = "cellPrefix";
    private String[] tableCellClasses = new String[]{};    
    private String[] tableRowClasses = new String[]{};
    private String[] sectionHeadingClasses = new String[]{};
    private String[] tableHeading = new String[]{};
    private InfoTableElements elements = new InfoTableElements();
    private boolean defaultUnfolded = false, foldableSections = false,
            cascadingRowClasses = false, rememberFoldState = true;
    private String[] foldedIcon, unfoldedIcon;
    private int iconSize=13;

    public InfoTableModel() {
        setDefaults();
    }

    public InfoTableModel(String foldPrefix) {
        this.foldNamePrefix = foldPrefix;
        setDefaults();
    }

    public void setDefaults() {
        tableClass = INFO_TABLE_CLASS;
        sectionHeadingClasses = new String[]{TABLE_SECTION_HEAD, TABLE_UNFOLD_HEAD};
        tableRowClasses = new String[]{TABLE_SECTION_ROW_EVEN, TABLE_SECTION_ROW_ODD};
        tableHeadingClass = TABLE_HEAD_CLASS;
        tableFoldIconClass = TABLE_FOLD_ICON_CELL;
        foldedIcon = new String[]{"img/folded.png", "img/folded_hover.png"};
        unfoldedIcon = new String[]{"img/unfolded.png", "img/unfolded_hover.png"};
    }

    public void setSectionDefaults(InfoTableSection section) {
        section.setSectionHeadingClasses(sectionHeadingClasses);
        section.setTableRowClasses(tableRowClasses);
    }

    public InfoTableElement addNewElement() {
        InfoTableElement element = new InfoTableElement();
        element.setCellClasses(tableCellClasses);
        add(element);
        return element;
    }

    public void add(InfoTableElement element) {
        elements.add(element);
    }

    public String[] getFoldedIconUrl() {
        return foldedIcon;
    }

    public void setFoldedIconUrl(String[] foldIconUrl) {
        this.foldedIcon = foldIconUrl;
    }

    public String[] getSectionHeadingClass() {
        return sectionHeadingClasses;
    }

    public void setSectionHeadingClasses(String sectionHeadingClass) {
        this.sectionHeadingClasses = new String[]{sectionHeadingClass,sectionHeadingClass};
    }

        public void setSectionHeadingClasses(String[] sectionHeadingClasses) {
        if (sectionHeadingClasses.length == 2) {
            this.sectionHeadingClasses = sectionHeadingClasses;
        }
    }

    public String[] getTableRowClasses() {
        return tableRowClasses;
    }

    public void setTableRowClasses(String[] tableRowClasses) {
        if (tableRowClasses.length == 2) {
            this.tableRowClasses = tableRowClasses;
        }
    }

    public void setTableRowClasses(String tableRowClass) {
        this.tableRowClasses = new String[]{tableRowClass, tableRowClass};
    }

    public InfoTableElements getElements() {
        return elements;
    }

    public void setElements(InfoTableElements elements) {
        this.elements = elements;
    }

    public String[] getTableCellClasses() {
        return tableCellClasses;
    }

    public void setTableCellClasses(String[] tableCellClasses) {
        this.tableCellClasses = tableCellClasses;
    }

    public String getTableClass() {
        return tableClass;
    }

    public void setTableClass(String tableClass) {
        this.tableClass = tableClass;
    }

    public String[] getUnfoldedIconUrl() {
        return unfoldedIcon;
    }

    public void setUnfoldedIconUrl(String[] unfoldIconUrl) {
        this.unfoldedIcon = unfoldIconUrl;
    }

    public String[] getTableHeading() {
        return tableHeading;
    }

    public void setTableHeading(String[] tableHeading) {
        this.tableHeading = tableHeading;
    }

    public void setTableHeading(String tableHeading) {
        this.tableHeading = new String[]{tableHeading};
    }

    public String getTableHeadingClass() {
        return tableHeadingClass;
    }

    public void setTableHeadingClass(String tableHeadingClass) {
        this.tableHeadingClass = tableHeadingClass;
    }

    public boolean isDefaultUnfolded() {
        return defaultUnfolded;
    }

    public void setDefaultUnfolded(boolean defaultUnfolded) {
        this.defaultUnfolded = defaultUnfolded;
    }

    public String getFoldNamePrefix() {
        return foldNamePrefix;
    }

    public void setFoldNamePrefix(String foldNamePrefix) {
        this.foldNamePrefix = foldNamePrefix;
    }

    public boolean isFoldableSections() {
        return foldableSections;
    }

    public void setFoldableSections(boolean foldableSections) {
        this.foldableSections = foldableSections;
    }

    public String getTableFoldIconClass() {
        return tableFoldIconClass;
    }

    public void setTableFoldIconClass(String tableFoldIconClass) {
        this.tableFoldIconClass = tableFoldIconClass;
    }

    public boolean isCascadingRowClasses() {
        return cascadingRowClasses;
    }

    public void setCascadingRowClasses(boolean cascadingRowClasses) {
        this.cascadingRowClasses = cascadingRowClasses;
    }

    public boolean isRememberFoldState() {
        return rememberFoldState;
    }

    public void setRememberFoldState(boolean rememberFoldState) {
        this.rememberFoldState = rememberFoldState;
    }

    public int getIconSize() {
        return iconSize;
    }

    public String getIconSizeStr() {
        return String.valueOf(iconSize);
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }
    
}
