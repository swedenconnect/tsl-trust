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
 * HTML text input element
 */
public class TextInputElement extends HtmlElement {

    public TextInputElement(String id) {
        this(id, "", "");
    }

    public TextInputElement(String id, String event, String function) {
        this(id, event, function, new String[]{});
    }

    public TextInputElement(String id, String event, String function, String[] args) {
        this.tag = "input";
        this.addAttribute("type", "text");
        this.addAttribute("id", id);
        if (event.length()>0){
            this.addAction(event, function, args);
        }
    }
}
