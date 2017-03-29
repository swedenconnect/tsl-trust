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
