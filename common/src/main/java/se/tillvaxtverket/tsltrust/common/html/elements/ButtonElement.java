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
