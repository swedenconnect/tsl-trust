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

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Table section data class for the Info table html table model
 */
public class InfoTableSection {

    private String heading;
    private String[] tableRowClasses = new String[]{};
    private String[] sectionHeadingClasses = new String[]{};
    private InfoTableElements elements = new InfoTableElements();
    private String ajaxContentId="";
    private boolean ajaxContent=false;
    private boolean foldable = false, displayHeading = false, keepFoldableElement = false, keepFirstFoldableCell = false;
    private InfoTableElement foldedElement = null;
    private boolean siblingFolding=false, siblingHeading=false;
    private int siblingHeadCols=0, siblingDataCols=0;
    
    public InfoTableSection(String Heading, InfoTableModel tModel) {
        this.heading = Heading;
        displayHeading = true;
        tModel.setSectionDefaults(this);
    }

    public void addNewElement(Object o) {
        add(new InfoTableElement(str(o)));
    }

    public void addNewElement(Object o1, Object o2) {
        add(new InfoTableElement(str(o1), str(o2)));
    }

    public void addNewElement(Object o1, Object o2, Object o3) {
        add(new InfoTableElement(str(o1), str(o2), str(o3)));
    }

    public void addNewElement(Object[] oa) {
        addNewElement(oa, null);
    }

    public void addNewElement(Object[] oa, String[] classNames) {
        String[] strArray;
        if (oa == null) {
            strArray = new String[]{""};
        } else {
            strArray = new String[oa.length];
            for (int i = 0; i < oa.length; i++) {
                strArray[i] = str(oa[i]);
            }
        }
        if (classNames == null) {
            add(new InfoTableElement(strArray));
        } else {
            add(new InfoTableElement(strArray, classNames));
        }
    }

    private String str(Object o) {
        SimpleDateFormat tformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (o == null) {
            return "";
        }
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Integer) {
            return String.valueOf((Integer) o);
        }
        if (o instanceof BigInteger) {
            return ((BigInteger) o).toString();
        }
        if (o instanceof Date) {
            return tformat.format((Date) o);
        }
        return "";
    }

    public InfoTableSection(InfoTableModel tModel) {
        tModel.setSectionDefaults(this);
    }

    public void add(InfoTableElement element) {
        elements.add(element);
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String Heading) {
        this.heading = Heading;
        displayHeading = true;
    }

    public InfoTableElements getElements() {
        return elements;
    }

    public void setElements(InfoTableElements element) {
        this.elements = element;
    }

    public InfoTableElement getfoldedElement() {
        return foldedElement;
    }

    public void setFoldedElement(InfoTableElement foldElement) {
        this.foldedElement = foldElement;
        foldable = true;
    }

    public void setFoldedElement(String foldString) {
        setFoldedElement(new InfoTableElement(foldString));
    }
    public void setFoldedElement(String foldString, String className) {
        setFoldedElement(new InfoTableElement(new String[]{foldString}, new String[]{className}));
    }
    public boolean isDisplayHeading() {
        return displayHeading;
    }

    public boolean isFoldable() {
        return foldable;
    }

    public String[] getSectionHeadingClass() {
        return sectionHeadingClasses;
    }

    public void setSectionHeadingClasses(String[] sectionHeadingClasses) {
        this.sectionHeadingClasses = sectionHeadingClasses;
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

    public boolean isKeepFoldableElement() {
        return keepFoldableElement;
    }

    public void setKeepFoldableElement(boolean keepFoldableElement) {
        this.keepFoldableElement = keepFoldableElement;
    }

    public int getSiblingDataCols() {
        return siblingDataCols;
    }

    public void setSiblingDataCols(int siblingDataCols) {
        this.siblingDataCols = siblingDataCols;
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

    public boolean isKeepFirstFoldableCell() {
        return keepFirstFoldableCell;
    }

    public void setKeepFirstFoldableCell(boolean keepFirstFoldableCell) {
        this.keepFirstFoldableCell = keepFirstFoldableCell;
    }

    public boolean isAjaxContent() {
        return ajaxContent;
    }

    public String getAjaxContentId() {
        return ajaxContentId;
    }

    public void setAjaxContentId(String ajaxContentId) {
        ajaxContent=true;
        this.ajaxContentId = ajaxContentId;
    }
        
}
