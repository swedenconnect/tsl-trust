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
package org.bounce.text;

import java.io.IOException;

import javax.swing.text.Document;

/**
 * Associates input stream characters with specific styles.
 * <p>
 * <b>Note:</b> The Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.5 $, $Date: 2009/01/22 22:14:59 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public abstract class SyntaxHighlightingScanner {
	protected int start = 0;
	protected long pos = 0;
	protected boolean error = false;

	protected DocumentInputReader in = null;
	protected boolean valid = false;

	/** The last token scanned */
	public String token = null;

	/**
	 * Constructs a scanner for the Document.
	 * 
	 * @param document
	 *            the document containing the XML content.
	 * 
	 * @throws IOException
	 */
	public SyntaxHighlightingScanner(Document document) throws IOException {
		try {
			in = new DocumentInputReader(document);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		in.read();
	}

	public boolean isError() {
		return error;
	}
	
	/**
	 * Returns true when no paint has invalidated the scanner.
	 * 
	 * @return true when no paint has invalidated the output.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Set valid when correct range is set.
	 * 
	 * @param valid
	 *            when correct range set.
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Sets the scanning range.
	 * 
	 * @param start
	 *            the start of the range.
	 * @param end
	 *            the end of the range.
	 * 
	 * @throws IOException
	 */
	public void setRange(int start, int end) throws IOException {
		in.setRange(start, end);

		this.start = start;

		token = null;
		pos = 0;

		in.read();
		scan();
	}

	/**
	 * Gets the starting location of the current token in the document.
	 * 
	 * @return the starting location.
	 */
	public final int getStartOffset() {
		return start + (int) pos;
	}

	/**
	 * Gets the end location of the current token in the document.
	 * 
	 * @return the end location.
	 */
	public final int getEndOffset() {
		return start + (int) in.pos;
	}

	/**
	 * Scans the Xml Stream for XML specific tokens.
	 * 
	 * @return the last location.
	 * 
	 * @throws IOException
	 */
	public abstract long scan() throws IOException;
}
