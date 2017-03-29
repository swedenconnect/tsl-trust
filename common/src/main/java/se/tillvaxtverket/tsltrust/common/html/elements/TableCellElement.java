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
