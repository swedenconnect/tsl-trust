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
