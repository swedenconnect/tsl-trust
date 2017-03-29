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
package org.bounce.text.xml;

/**
 * The contants used for the XML editor.
 * 
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.3 $, $Date: 2009/01/22 22:14:59 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public interface XMLStyleConstants {
    /** The style constant for element name */
    public static final String ELEMENT_NAME     = "element-name";
    /** The style constant for element prefix */
    public static final String ELEMENT_PREFIX   = "element-prefix";
    /** The style constant for element value */
    public static final String ELEMENT_VALUE    = "element-value";

    /** The style constant for attribute name */
    public static final String ATTRIBUTE_NAME   = "attribute-name";
    /** The style constant for attribute prefix */
    public static final String ATTRIBUTE_PREFIX = "attribute-prefix";
    /** The style constant for attribute value */
    public static final String ATTRIBUTE_VALUE  = "attribute-value";

    /** The style constant for namespace name*/
    public static final String NAMESPACE_NAME   = "namespace-name";
    /** The style constant for namespace prefix */
    public static final String NAMESPACE_PREFIX = "namespace-prefix";
    /** The style constant for namespace value */
    public static final String NAMESPACE_VALUE  = "namespace-value";

    /** The style constant for entity */
    public static final String ENTITY           = "Entity";
    /** The style constant for comment */
    public static final String COMMENT          = "Comment";
    /** The style constant for cdata */
    public static final String CDATA          	= "CDATA";
    /** The style constant for declaration */
    public static final String DECLARATION      = "Declaration";

    /** The style constant for special */
    public static final String SPECIAL          = "Special";
    /** The style constant for string */
    public static final String STRING           = "String";

    public static final String WHITESPACE       = "Whitespace";

    /** The style constant for entity */
    public static final String ENTITY_REFERENCE = "EntityReference";
}
