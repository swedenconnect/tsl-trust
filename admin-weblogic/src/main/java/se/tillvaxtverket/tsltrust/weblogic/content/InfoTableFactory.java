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
package se.tillvaxtverket.tsltrust.weblogic.content;

import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElement;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for html tables based on the InfoTable data model
 */
public class InfoTableFactory implements HtmlConstants {

    InfoTableModel model;
    SessionModel session;
    int foldCount;

    public InfoTableFactory(InfoTableModel model, SessionModel session) {
        this.model = model;
        this.session = session;

        //Look for foldable sections
    }

    public TableElement getTable() {
        foldCount = 0;
        TableElement table = new TableElement(model.getTableClass());
        if (model.getTableHeading().length > 0) {
            if (model.getTableHeading().length == 1) {
                InfoTableElements elements = model.getElements();
                elements.calculateColumnValues();
                int totalColumns = elements.getHeadAndFoldColumns() + elements.getDataColumns();
//                int cols = getElementTotalColumns(model.getElements().getAll());
                table.addRow(model.getTableHeading(), model.getTableHeadingClass(), totalColumns, new boolean[]{true});
            } else {
                table.addRow(model.getTableHeading(), model.getTableHeadingClass());
            }
        }
        int classIndex = model.isCascadingRowClasses() ? -1 : 0;
        addElementData(model.getElements(), table, model.getTableRowClasses(), classIndex);
        return table;
    }

    private void addElementData(InfoTableElements elements, TableElement table,
            String[] rowClasses, int classIndex) {
        int row = classIndex + 1;
        elements.calculateColumnValues();

        for (InfoTableElement element : elements.getAll()) {
            List<TableCellElement> cellList = new ArrayList<TableCellElement>();
            if (element.isSubStructure()) {
                InfoTableSection section = element.getTableSection();
                if (element.getTableSection().isFoldable()) {

                    //Get fold or unfold state (model holds default setting and session holds stored state if node has been toggled
                    String foldID = model.getFoldNamePrefix() + String.valueOf(getFoldIndex());
                    boolean elementUnfolded = (element.isOverrideDefaultFolding())
                            ? element.isUnfolded()
                            : model.isDefaultUnfolded();
                    boolean unfolded = (model.isRememberFoldState())
                            ? session.isTableNodeUnfolded(model.getFoldNamePrefix(), foldID, elementUnfolded)
                            : elementUnfolded;

                    //Prepare element for ajax section
                    if (section.isAjaxContent()) {
                        HtmlElement ajaxDiv = new DivElement("itAjax" + foldID);
                        ajaxDiv.setText("Loading...");
                        section.addNewElement(ajaxDiv.toString());
                    }

                    //Get table row data.
                    List<TableCellElement> foldList = getFoldedRow(element.getTableSection(), foldID, section.getSiblingDataCols());
                    List<TableCellElement> unfoldList = getUnfoldedRow(element.getTableSection(), foldID, section.getSiblingDataCols(), row);

                    //Add rows to table
                    addTableRow(table, foldList, rowClasses, row, !unfolded, foldID + "fold");
                    addTableRow(table, unfoldList, rowClasses, row++, unfolded, foldID);
                } else {
                    // If not foldable section
                    if (section.isDisplayHeading()) {
                        cellList.add(getTableCell(section.getHeading(), section.getSectionHeadingClass()[0]));
                    } else {
                        //If no heading but sibbling has heading
                        if (section.isSiblingHeading()) {
                            cellList.add(getTableCell(SPACE, ""));
                        }
                    }
                    // If sibbling has folding. Then add extra emptly table cell to align
                    if (section.isSiblingFolding()) {
                        cellList.add(getTableCell(SPACE, ""));
                    }
                    //Add substructure table in next cell
                    cellList = addCells(cellList, getTableCells(element.getTableSection(), section.getSiblingDataCols(), row));
                    addTableRow(table, cellList, section.getTableRowClasses(), row++, true, "");
                }
            } else {
                // if not a section, then pad empty cells to align with sibling headings and fold icons
                for (int i = 0; i < element.getSiblingHeadCols(); i++) {
                    cellList.add(getTableCell(SPACE, ""));
                }
                // Add element cells
                cellList = addCells(cellList, getTableCells(element, 0));
                addTableRow(table, cellList, rowClasses, row, true, "");
                row++;
            }
        }
    }

    private void addTableRow(TableElement table, List<TableCellElement> cellList, String[] rowClasses, int row, boolean show, String iD) {
        String rowClass = (row % 2 == 0) ? rowClasses[0] : rowClasses[1];
        TableRowElement tr = getTableRow(cellList, rowClass, show);
        if (iD.length() > 0) {
            tr.addAttribute("id", iD);
        }
        table.addHtmlElement(tr);
    }

    private List<TableCellElement> getFoldedRow(InfoTableSection section, String foldID, int colspan) {
        List<TableCellElement> foldedList = new ArrayList<TableCellElement>();
        if (section.isDisplayHeading()) {
            foldedList.add(getTableCell(section.getHeading(), section.getSectionHeadingClass()[0]));
        }
        foldedList.add(getFoldCell(model.getFoldedIconUrl(), model.getIconSizeStr(),
                foldID + "fold", foldID, section.isAjaxContent(), section.getAjaxContentId()));
        foldedList = addCells(foldedList, getTableCells(section.getfoldedElement(), colspan));
        return foldedList;
    }

    private List<TableCellElement> getUnfoldedRow(InfoTableSection section, String foldID, int colspan, int row) {
        List<TableCellElement> unfoldedList = new ArrayList<TableCellElement>();
        if (section.isDisplayHeading()) {
            unfoldedList.add(getTableCell(section.getHeading(), section.getSectionHeadingClass()[0]));
        }
        unfoldedList.add(getFoldCell(model.getUnfoldedIconUrl(), model.getIconSizeStr(), foldID, foldID + "fold", false, ""));
        unfoldedList = addCells(unfoldedList, getTableCells(section, colspan, row % 2));
        return unfoldedList;
    }

    private TableCellElement getTableCell(String text, String className) {
        TableCellElement td = new TableCellElement(text);
        if (className.length() > 0) {
            td.addAttribute("class", className);
        }
        return td;
    }

    private List<TableCellElement> getTableCells(InfoTableSection section, int colspan, int classIndex) {
        // reset table row class if model is not set to cascading row classes
        classIndex = model.isCascadingRowClasses() ? classIndex : 0;
        List<TableCellElement> cellList = new ArrayList<TableCellElement>();
        TableElement table = new TableElement(model.getTableClass());

        //If KeepFoldElements, then add a first line to the table.
        if (section.isKeepFoldableElement()) {
            InfoTableElements elements = section.getElements();
            elements.calculateColumnValues();
            int totalColumns = elements.getHeadAndFoldColumns() + elements.getDataColumns();

            //Add the kept fold elements
            TableRowElement tr = new TableRowElement(section.getTableRowClasses()[classIndex]);
            String[] values = section.getfoldedElement().getValues();
            if (section.isKeepFirstFoldableCell() && values.length > 1) {
                values = new String[]{values[0]};
            }
            for (String val : values) {
                TableCellElement td = new TableCellElement(val);
                td.addAttribute("class", section.getSectionHeadingClass()[1]);
                if (values.length == 1 && totalColumns > 1) {
                    td.addAttribute("colspan", String.valueOf(totalColumns));
                }
                tr.addHtmlElement(td);
            }
            table.addHtmlElement(tr);
        }
        addElementData(section.getElements(), table, section.getTableRowClasses(), classIndex);
        TableCellElement td = new TableCellElement("");
        if (colspan > 1) {
            td.addAttribute("colspan", String.valueOf(colspan));
        }
        td.addHtmlElement(table);
        cellList.add(td);
        return cellList;
    }

    private List<TableCellElement> getTableCells(InfoTableElement element, int colspan) {
        List<TableCellElement> cellList = new ArrayList<TableCellElement>();
        String[] data = element.getValues();
        String[] className = element.getCellClasses();
        for (int i = 0; i < data.length; i++) {
            TableCellElement td = new TableCellElement(data[i]);
            if (i < className.length) {
                td.addAttribute("class", className[i]);
            }
            if (i == 0 && data.length == 1 && colspan > 1) {
                td.addAttribute("colspan", String.valueOf(colspan));
            }
            cellList.add(td);
        }
        return cellList;
    }

    private TableCellElement getFoldCell(String[] imgUrl, String width, String thisid, String otherid, boolean ajax, String ajaxId) {
        TableCellElement foldCell = new TableCellElement("");
        foldCell.addAttribute("class", model.getTableFoldIconClass());
        HtmlElement image = getImage(imgUrl[0], width);
        String iconID = thisid + "Icn";
        image.addAttribute("id", iconID);
        if (ajax) {
            image.addAction(ONCLICK, AJAX_UNFOLD_FUNCTION, new String[]{
                        ajaxId, otherid, model.getFoldNamePrefix()});
        } else {
            image.addAction(ONCLICK, FOLD_UNFOLD_FUNCTION, new String[]{
                        thisid, otherid, model.getFoldNamePrefix()});
        }

        image.addAction(ONMOUSEOVER, CHANGE_ICON_FUNCTION, new String[]{
                    iconID, imgUrl[1]});
        image.addAction(ONMOUSEOUT, CHANGE_ICON_FUNCTION, new String[]{
                    iconID, imgUrl[0]});


        foldCell.addHtmlElement(image);
        return foldCell;
    }

    private HtmlElement getImage(String imgUrl, String height) {
        HtmlElement image = new GenericHtmlElement("img");
        image.addAttribute("src", imgUrl);
        image.addAttribute("height", height);
        return image;
    }

    private int getFoldIndex() {
        return foldCount++;
    }

    private List<TableCellElement> addCells(List<TableCellElement> startList, List<TableCellElement> endList) {
        for (TableCellElement td : endList) {
            startList.add(td);
        }
        return startList;
    }

    private TableRowElement getTableRow(List<TableCellElement> foldedList, String rowClass, boolean show) {
        TableRowElement tr = new TableRowElement(rowClass);
        for (TableCellElement td : foldedList) {
            tr.addHtmlElement(td);
        }
        if (!show) {
            tr.addStyle("display", "none");
        }
        return tr;
    }

    private boolean hasFoldColumn(List<InfoTableElement> elements) {
        for (InfoTableElement element : elements) {
            if (element.isSubStructure()) {
                if (element.getTableSection().isFoldable() && !element.getTableSection().isDisplayHeading()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<TableCellElement>[] getUnfoldArray(List<TableCellElement> foldList, List<TableCellElement> unfoldList) {
        int cols = unfoldList.size() - 1;
        List<TableCellElement> ufRow = new ArrayList<TableCellElement>();
        List<TableCellElement> tableRow = new ArrayList<TableCellElement>();
        TableCellElement empty = new TableCellElement(SPACE);
        for (int i = 0; i < cols; i++) {
            ufRow.add(unfoldList.get(i));
            tableRow.add(empty);
        }
        for (int i = cols; i < foldList.size(); i++) {
            ufRow.add(foldList.get(i));
        }
        tableRow.add(unfoldList.get(cols));

        return new List[]{ufRow, tableRow};
    }
}
