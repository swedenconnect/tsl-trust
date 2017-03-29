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
 * A button Html Element
 */
public class ButtonElement extends HtmlElement {

    /**
     * Button html element constructor
     * @param value Text label of the button;
     */
    public ButtonElement(String value) {
        this(value, "", "");
    }

    /**
     * Button html element constructor     * 
     * @param value button label
     * @param event event associated with this button (e.g. onClick event)
     * @param function the function to execute on the specified event.
     */
    public ButtonElement(String value, String event, String function) {
        this(value, event, function, new String[]{});
    }

    /**
     * Button html element constructor     * 
     * @param value button label
     * @param event event associated with this button (e.g. onClick event)
     * @param function the function to execute on the specified event.
     * @param args arguments for the function
     */
    public ButtonElement(String value, String event, String function, String[] args) {
        this.tag = "input";
        this.addAttribute("type", "button");
        this.addAttribute("value", value);
        if (event.length()>0){
            this.addAction(event, function, args);
        }
    }

}
