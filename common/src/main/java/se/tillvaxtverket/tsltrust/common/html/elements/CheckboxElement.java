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
 * HTML checkbox element
 */
public class CheckboxElement extends HtmlElement {

    /**
     * Html checkbox element
     * @param checked
     */
    public CheckboxElement(boolean checked) {
        this("", checked);
    }

    /**
     * Check box element constructor
     * @param id value of id attribute
     * @param checked true if this checkbox is preselected, else false,
     */
    public CheckboxElement(String id, boolean checked) {
        this(id, "", "", "", checked);
    }

    /**
     * 
     * Check box element constructor
     * @param id value of id attribute
     * @param className class attribute value
     * @param event event for executing function (e.g. onchange)
     * @param function JavaScript function to call on the event function to 
     * @param checked true if this checkbox is preselected, else false,
     */
    public CheckboxElement(String id, String className, String event, String function, boolean checked) {
        this(id, className, event, function, new String[]{}, checked);
    }

    /**
     * Check box element constructor
     * @param id value of id attribute
     * @param className class attribute value
     * @param event event for executing function (e.g. onchange)
     * @param function Java Script function to call on the event function to      * 
     * @param args arguments to the java Script function.
     * @param checked true if this checkbox is preselected, else false,
     */
    public CheckboxElement(String id, String className, String event, String function, String[] args, boolean checked) {
        this.tag = "input";
        this.addAttribute("type", "checkbox");

        if (id.length() > 0) {
            this.addAttribute("id", id);
        }
        if (className.length() > 0) {
            this.addAttribute("class", className);
        }
        if (event.length() > 0) {
            this.addAction(event, function, args);
        }
        if (checked){
            this.addAttribute("checked", "true");
        }
    }

}
