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
