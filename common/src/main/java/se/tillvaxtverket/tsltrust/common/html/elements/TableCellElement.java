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
package se.tillvaxtverket.tsltrust.common.html.elements;

/**
 * HTML table cell element
 */
public class TableCellElement extends HtmlElement {

    public TableCellElement(String text) {
        this(text, 1, "");
    }

    public TableCellElement(String text, int colspan) {
        this(text, colspan, "");
    }

    public TableCellElement(String text, String className) {
        this(text, 1, className);
    }

    public TableCellElement(String text, int colsapn, String className) {
        this.tag = "td";
        this.text = text;
        if (colsapn != 1) {
            this.addAttribute("colspan", String.valueOf(colsapn));
        }
        if (className.length() > 0) {
            this.addAttribute("class", className);
        }
    }
}
