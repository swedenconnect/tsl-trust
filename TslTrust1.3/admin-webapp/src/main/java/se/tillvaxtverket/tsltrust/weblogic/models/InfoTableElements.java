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

import java.util.ArrayList;
import java.util.List;

/**
 * data class for element lists of the infor table model for html tables
 */
public class InfoTableElements {

    boolean foldElements;
    boolean headingElements;
    int headAndFoldColumns;
    int dataColumns;
    List<InfoTableElement> elements = new ArrayList<InfoTableElement>();

    public InfoTableElements() {
    }

    public InfoTableSection addNewSection(InfoTableModel tm, String heading) {
        InfoTableElement element = new InfoTableElement();
        InfoTableSection section = new InfoTableSection(heading, tm);
        element.setTableSection(section);
        add(element);
        return section;
    }

    public InfoTableSection addNewSection(InfoTableModel tm, String heading, boolean defaultUnfold) {
        InfoTableElement element = new InfoTableElement();
        InfoTableSection section = new InfoTableSection(heading, tm);
        element.setUnfolded(defaultUnfold);
        element.setTableSection(section);
        add(element);
        return section;
    }

    public InfoTableSection addNewSection(InfoTableModel tm, boolean defaultUnfold) {
        InfoTableElement element = new InfoTableElement();
        InfoTableSection section = new InfoTableSection(tm);
        element.setUnfolded(defaultUnfold);
        element.setTableSection(section);
        add(element);
        return section;
    }

    public InfoTableSection addNewSection(InfoTableModel tm) {
        InfoTableElement element = new InfoTableElement();
        InfoTableSection section = new InfoTableSection(tm);
        element.setTableSection(section);
        add(element);
        return section;
    }

    public InfoTableElement get(int i) {
        return elements.get(i);
    }

    public boolean add(InfoTableElement element) {
        return elements.add(element);
    }

    /**
     * This function is used by the factory class to calculate and store columns counts,
     * heading and folding options in substructures.
     */
    public void calculateColumnValues() {
        int tempDataCols = 0, tempHeadFoldColumns = 0;
        boolean heading = false, folding = false;
        InfoTableSection ts;
        for (InfoTableElement ie : elements) {
            int datacols = 0;
            if (ie.hasSubStructure()) {
                ts = ie.getTableSection();
                heading = (ts.isDisplayHeading()) ? true : heading;
                if (ts.isFoldable()) {
                    // If foldable, then cols is given by the fold element
                    datacols = ts.getfoldedElement().getValues().length;
                    folding=true;
                } else {
                    // If not, this is just a 1 column element holding a table
                    datacols=1;
                }
            } else {
                //If no substructurem then data cols is given by the element in the list
                datacols = ie.getValues().length;
            }
            //grab the largest value
            tempDataCols = (datacols>tempDataCols)? datacols:tempDataCols;
        }
        // Wrapup
        tempHeadFoldColumns = (heading)? 1:0;
        tempHeadFoldColumns = (folding)? tempHeadFoldColumns+1:tempHeadFoldColumns;
        dataColumns=tempDataCols;
        headAndFoldColumns=tempHeadFoldColumns;
        foldElements=folding;
        headingElements=heading;
        
        //Now store values in section childs
        for (InfoTableElement ie:elements){
            if (ie.hasSubStructure()){
                InfoTableSection sect = ie.getTableSection();
                sect.setSiblingFolding(folding);
                sect.setSiblingHeading(heading);
                sect.setSiblingDataCols(tempDataCols);
                sect.setSiblingHeadCols(tempHeadFoldColumns);
            }
            ie.setSiblingFolding(folding);
            ie.setSiblingHeading(heading);
            ie.setSiblingHeadCols(tempHeadFoldColumns);
        }        
    }

    public List<InfoTableElement> getAll() {
        return elements;
    }

    public int getDataColumns() {
        calculateColumnValues();
        return dataColumns;
    }

    public boolean isFoldElements() {
        calculateColumnValues();
        return foldElements;
    }

    public int getHeadAndFoldColumns() {
        calculateColumnValues();
        return headAndFoldColumns;
    }

    public boolean isHeadingElements() {
        calculateColumnValues();
        return headingElements;
    }
}
