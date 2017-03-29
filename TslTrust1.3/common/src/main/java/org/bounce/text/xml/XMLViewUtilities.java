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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.bounce.text.SyntaxHighlightingScanner;

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
 * @version $Revision: 1.5 $, $Date: 2009/01/22 22:14:59 $
 */
class XMLViewUtilities {
	// Update the scanner to point to the '<' begin token.
	public static void updateScanner(SyntaxHighlightingScanner scanner, Document doc, int start, int end) {
		try {
			if (!scanner.isValid()) {
				scanner.setRange(getTagEnd(doc, start), end);
				scanner.setValid(true);
			}

			while (scanner.getEndOffset() <= start && end > scanner.getEndOffset()) {
				scanner.scan();
			}
		} catch (IOException e) {
			// can't adjust scanner... calling logic
			// will simply render the remaining text.
			// e.printStackTrace();
		}
	}

	// Return the end position of the current tag.
	private static int getTagEnd(Document doc, int p) {
		int elementEnd = 0;

		if (p > 0) {
			try {
				int index;

				String s = doc.getText(0, p);
				int cdataStart = s.lastIndexOf("<![CDATA[");
				int cdataEnd = s.lastIndexOf("]]>");
				int commentStart = s.lastIndexOf("<!--");
				int commentEnd = s.lastIndexOf("-->");

				if (cdataStart > 0 && cdataStart > cdataEnd) {
					index = s.lastIndexOf(">", cdataStart);
				} else if (commentStart > 0 && commentStart > commentEnd) {
					index = s.lastIndexOf(">", commentStart);
				} else {
					index = s.lastIndexOf(">");
				}

				if (index != -1)
					elementEnd = index;
			} catch (BadLocationException bl) {
				// empty
			}
		}

		return elementEnd;
	}
}