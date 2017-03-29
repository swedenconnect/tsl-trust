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
 * HTML div element
 */
public class DivElement extends HtmlElement {

    /**
     * Html Div element
     */
    public DivElement() {
        this("", "");
    }

    /**
     * Html Div element
     * @param id id attribute
     */
    public DivElement(String id) {
        this(id, "");
    }

    /**
     * Html Div element
     * @param id id attribute
     * @param className class attribute
     */
    public DivElement(String id, String className) {
        this.tag = "div";
        if (id.length() > 0) {
            this.addAttribute("id", id);
        }
        if (className.length() > 0) {
            this.addAttribute("class", className);
        }
    }

}
