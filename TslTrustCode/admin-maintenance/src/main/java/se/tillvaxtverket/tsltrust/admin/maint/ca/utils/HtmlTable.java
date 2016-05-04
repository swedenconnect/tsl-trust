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
package se.tillvaxtverket.tsltrust.admin.maint.ca.utils;

import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;

/**
 * HTML table for display of CA information
 */
public class HtmlTable extends TableElement {

    public HtmlTable() {
        addStyle("font-family", "Verdana, Arial, sans-serif");
        addStyle("font-size", "small");
        addStyle("width","100%");
    }

    public void addRow(String[] cellData, Type type, RowStyle rowStyle) {
        TableRowElement tr = new TableRowElement();
        if (rowStyle.bgColor.length() > 0) {
            tr.addStyle("background-color", rowStyle.bgColor);
        }
        if (rowStyle.color.length() > 0) {
            tr.addStyle("color", rowStyle.color);
        }
        if (rowStyle.fontWeight.length() > 0) {
            tr.addStyle("font-weight", rowStyle.fontWeight);
        }

        CellStyle[] styles = type.styles;

        for (int i = 0; i < cellData.length; i++) {
            String cellText = cellData[i];
            TableCellElement td = new TableCellElement(cellText);

            CellStyle style = styles[i];

            if (style.width.length() > 0) {
                td.addAttribute("width", style.width);
            }

            if (style.color.length() > 0) {
                td.addStyle("color", style.color);
            }
            if (style.fontWeight.length() > 0) {
                td.addStyle("font-weight", style.fontWeight);
            }

            tr.addHtmlElement(td);
        }
        addHtmlElement(tr);
    }

    public enum Type {

        CERT(new CellStyle[]{CellStyle.SERIAL, CellStyle.NAME, CellStyle.EVENT, CellStyle.TIME}),
        LOG(new CellStyle[]{CellStyle.TIME, CellStyle.EVENT, CellStyle.SERIAL, CellStyle.REASON}),
        EXPORT(new CellStyle[]{CellStyle.INDEX, CellStyle.NAME, CellStyle.SERIAL}),
        CERTHEADING(new CellStyle[]{CellStyle.NORLMAL, CellStyle.NORLMAL, CellStyle.NORLMAL, CellStyle.NORLMAL}),
        LOGHEADING(new CellStyle[]{CellStyle.NORLMAL, CellStyle.NORLMAL, CellStyle.NORLMAL, CellStyle.NORLMAL}),
        EXPORTHEADING(new CellStyle[]{CellStyle.NORLMAL, CellStyle.NORLMAL, CellStyle.NORLMAL});
        private final CellStyle[] styles;

        private Type(CellStyle[] styles) {
            this.styles = styles;
        }
    }

    private enum CellStyle {

        NORLMAL("", "", ""), TIME("#333333", "", "160"), SERIAL("#132124", "bold", ""), NAME("#100651", "", ""), EVENT("#595a00", "", ""), REASON("#333333", "", ""), INDEX("#333333", "", "");
        private final String color, fontWeight, width;

        private CellStyle(String color, String fontWeight, String width) {
            this.color = color;
            this.fontWeight = fontWeight;
            this.width = width;
        }
    }

    public enum RowStyle {

        HEADING("#000033", "#ffffcc", "bold"), ODD("#e5e5e5", "", ""), EVEN("", "", "");
        private final String bgColor, color, fontWeight;

        private RowStyle(String bgColor, String color, String fontWeight) {
            this.bgColor = bgColor;
            this.color = color;
            this.fontWeight = fontWeight;
        }
    }
}
