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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.xml.stream.events.XMLEvent;

import org.bounce.text.FoldingMargin;

/**
 * Line folding margin for a JTextComponent.
 * 
 * <pre>
 * JEditorPane editor = new JEditorPane();
 * JScrollPane scroller = new JScrollPane(editor);
 *
 * // Add the number margin as a Row Header View
 * XMLFoldingMargin margin = new XMLFoldingMargin(editor);
 * scroller.setRowHeaderView(margin);
 * </pre>
 * 
 * @author Edwin Dankert <edankert@gmail.com>
 */

public class XMLFoldingMargin extends FoldingMargin {
	private static final long serialVersionUID = 8489615051963807472L;

	private XMLScanner scanner = null;

	/**
	 * Convenience constructor for Text Components
	 */
	public XMLFoldingMargin(JTextComponent editor) throws IOException {
		super(editor);

		initScanner(editor.getDocument());
		
		editor.addPropertyChangeListener("document", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object prop = event.getNewValue();
				
				if (prop instanceof Document) {
					try {
						initScanner((Document)prop);
					} catch (IOException e) {
						// This will just not set the scanner object 
					}
				}
			}
		});
	}
	
	private void initScanner(Document document) throws IOException {
		scanner = new XMLScanner(document);
	}

	protected int getFoldClosingLine(int start, int end) {
		Element element = editor.getDocument().getDefaultRootElement().getElement(start);
		int tagStart = getStartTagLocation(element.getStartOffset(), element.getEndOffset());
		
		if (tagStart != -1) {
			Element endElement = editor.getDocument().getDefaultRootElement().getElement(end);
			int tagEnd = getEndTagLocation(tagStart, endElement.getEndOffset());

			if (tagStart >= element.getStartOffset() && tagStart < element.getEndOffset() && tagEnd > element.getEndOffset()) {
				return editor.getDocument().getDefaultRootElement().getElementIndex(tagEnd);
			}
		}
		
		return start;
	}

	private int getStartTagLocation(int offset, int end){
		scanner.setValid(false);
		XMLViewUtilities.updateScanner(scanner, editor.getDocument(), offset, end);

		try {
			scanner.getNextTag();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (scanner.getEventType() == XMLEvent.START_ELEMENT) {
			return scanner.getStartOffset();
		}
		
		return -1;
	}

	private int getEndTagLocation(int startTagLocation, int endOffset) {
		scanner.setValid(false);
		XMLViewUtilities.updateScanner(scanner, editor.getDocument(), startTagLocation, endOffset);

		int startTags = 1;

		do {
			int event = -1;

			try {
				event = scanner.getNextTag();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (event == XMLEvent.START_ELEMENT) {
				startTags++;
			} else if (event == XMLEvent.END_ELEMENT) {
				startTags--;
			}
		} while (startTags > 0 && scanner.getEndOffset() < endOffset && scanner.token != null);
		
		if (startTags == 0) {
			return scanner.getStartOffset();
		}
		
		return endOffset;
	}
}
