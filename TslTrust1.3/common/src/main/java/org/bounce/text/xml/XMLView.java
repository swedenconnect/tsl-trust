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

import java.io.IOException;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;

import org.bounce.text.SyntaxHighlightingScanner;
import org.bounce.text.SyntaxHighlightingView;

/**
 * The XML View uses the XML scanner to determine the style (font, color) of the
 * text that it renders.
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @author Edwin Dankert <edankert@gmail.com>
 * @version $Revision: 1.4 $, $Date: 2008/04/16 19:36:18 $
 */
public class XMLView extends SyntaxHighlightingView {
	/**
	 * Construct a colorized view of xml text for the element. Gets the current
	 * document and creates a new Scanner object.
	 * 
	 * @param context
	 *            the styles used to colorize the view.
	 * @param elem
	 *            the element to create the view for.
	 * @throws IOException
	 *             input/output exception while reading document
	 */
	public XMLView(XMLScanner scanner, StyleContext context, Element elem) throws IOException {
		super(scanner, context, elem);
	}

	// Update the scanner to point to the '<' begin token.
	protected void updateScanner(SyntaxHighlightingScanner scanner, Document doc, int start, int end) {
		XMLViewUtilities.updateScanner(scanner, doc, start, end);
	}

	@Override
	protected boolean isErrorHighlighting() {
		Object errorHighlighting = getDocument().getProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE);

		if (errorHighlighting != null) {
			return (Boolean) errorHighlighting;
		}

		return false;
	}
}