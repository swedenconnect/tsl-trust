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
