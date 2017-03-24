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
 * HTML select element
 */
public class SelectElement extends HtmlElement {

    public SelectElement() {
        this("");
    }

    public SelectElement(String id) {
        this.tag = "select";
        if (id.length() > 0) {
            this.addAttribute("id", id);
        }
    }

    public void addOption(String option) {
        addOption(option, "", "", "",false);
    }
    public void addOption(String option, boolean selected) {
        addOption(option, "", "", "",selected);
    }

    public void addOption(String option, String event, String function, String arg, boolean selected) {
        addOption(option, event, function, new String[]{arg}, selected);
    }

    public void addOption(String option, String event, String function, String[] args, boolean selected) {
        HtmlElement optElement = new GenericHtmlElement("option");
        optElement.text = option;
        if (event.length()>0){
            optElement.addAction(event, function, args);
        }
        if (selected){
            optElement.addAttribute("selected", "true");
        }
        addHtmlElement(optElement);
        
    }
}
