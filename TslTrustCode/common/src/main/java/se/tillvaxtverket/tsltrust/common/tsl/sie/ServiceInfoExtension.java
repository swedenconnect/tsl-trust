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
package se.tillvaxtverket.tsltrust.common.tsl.sie;

import org.w3c.dom.Node;

/**
 * Interface for Service Information Extensions
 */
public interface ServiceInfoExtension {
   
    /**
     * Getter for extension display name
     * @return display name
     */
    String getName();

    /**
     * Getter for an informational label describing the nature of the information
     * found in this extension
     * @return Textual information about the extension
     */
    String getInfo();

    /**
     * Getter for the type of extension
     * @return An enumeration of the type of extension
     */
    SieType getType();
    
    /**
     * Getter for the XML node holding the extension data.
     * @return XML node
     */
    Node getNode();
    
    /**
     * The criticality of the extension.
     * @return true if the extension is critical, otherwise false.
     */
    boolean isCritical();
}
