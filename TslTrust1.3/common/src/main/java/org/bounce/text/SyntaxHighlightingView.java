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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;

/**
 * The View uses the syntax highlighting scanner to determine the style (font, color) of the
 * text that it renders.
 * <p>
 * <b>Note: </b> The Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @author Edwin Dankert <edankert@gmail.com>
 * @version $Revision: 1.4 $, $Date: 2008/04/16 19:36:18 $
 */
public abstract class SyntaxHighlightingView extends FoldingPlainView {
	private static Style DEFAULT_STYLE = (new StyleContext()).new NamedStyle();

	private Color selected = null;

	private SyntaxHighlightingScanner scanner = null;
	private StyleContext context = null;

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
	public SyntaxHighlightingView(SyntaxHighlightingScanner scanner, StyleContext context, Element elem) throws IOException {
		super(elem);

		this.context = context;
		this.scanner = scanner;
	}

	/**
	 * Invalidates the scanner, to make sure a new range is set later.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param a
	 *            the shape.
	 * @see View#paint(Graphics g, Shape a)
	 */
	public void paint(Graphics g, Shape a) {
		JTextComponent component = (JTextComponent) getContainer();
		Highlighter highlighter = component.getHighlighter();
		Color unselected = component.isEnabled() ? component.getForeground() : component.getDisabledTextColor();
		Caret caret = component.getCaret();
		selected = !caret.isSelectionVisible() || highlighter == null ? unselected : component.getSelectedTextColor();

		super.paint(g, a);

		scanner.setValid(false);
	}

	/**
	 * Renders the given range in the model as normal unselected text. This will
	 * paint the text according to the styles..
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the starting X coordinate
	 * @param y
	 *            the starting Y coordinate
	 * @param start
	 *            the beginning position in the model
	 * @param end
	 *            the ending position in the model
	 * @return the location of the end of the range
	 * @throws BadLocationException
	 *             if the range is invalid
	 */
	protected int drawUnselectedText(Graphics g, int x, int y, int start, int end) throws BadLocationException {
		if (this.context != null) {
			Document doc = getDocument();
	
			while (start < end) {
				updateScanner(scanner, doc, start, doc.getLength());
	
				int position = Math.min(scanner.getEndOffset(), end);
				position = (position <= start) ? end : position;
	
				Style style = context.getStyle(scanner.token);
				
				if (style == null) {
					style = DEFAULT_STYLE;
				}
	
				// color change, flush what we have
				g.setColor(context.getForeground(style));
				g.setFont(g.getFont().deriveFont(getFontStyle(style)));
	
				Segment text = getLineBuffer();
				doc.getText(start, position - start, text);
	
				int x1 = x;
	
				x = Utilities.drawTabbedText(text, x, y, g, (TabExpander) this, start);
	
				if (scanner.isError()) {
					drawError(scanner, g, x1, x - x1, y, false);
				}
	
				start = position;
			}
		}	
		
		return x;
	}

	private void drawError(SyntaxHighlightingScanner scanner, Graphics g, int x, int length, int y, boolean selected) throws BadLocationException {
		if (isErrorHighlighting()) {
			if (!selected) {
				g.setColor(new Color(255, 0, 0));
			}

			Rectangle rec = ((JTextComponent) getContainer()).modelToView(scanner.getStartOffset());
			
			if (rec != null) {
				drawZigZag(g, x, rec.x, length, y);
			}
		}
	}

	protected abstract boolean isErrorHighlighting();
	
	static private void drawZigZag(Graphics g, int x, int x1, int width, int y) {
		int pos = 0;
		int before = x - x1;
		int npoints = width;

		if (npoints > 0) {
			int[] xpoints = new int[npoints];
			int[] ypoints = new int[npoints];

			for (int i = 0; i < npoints; i++) {
				int height = ((i + before) % 4);

				if (height > 2) {
					height = height - 2;
				}

				xpoints[i] = x + pos;
				ypoints[i] = y + 2 - height;

				pos += 1;
			}

			g.drawPolyline(xpoints, ypoints, npoints);
		}
	}

	/**
	 * Renders the given range in the model as selected text. This will paint
	 * the text according to the font as found in the styles..
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the starting X coordinate
	 * @param y
	 *            the starting Y coordinate
	 * @param start
	 *            the beginning position in the model
	 * @param end
	 *            the ending position in the model
	 * @return the location of the end of the range
	 * @throws BadLocationException
	 *             if the range is invalid
	 */
	protected int drawSelectedText(Graphics g, int x, int y, int start, int end) throws BadLocationException {
		g.setColor(selected);

		Document doc = getDocument();

		while (start < end) {
			updateScanner(scanner, doc, start, doc.getLength());

			int position = (int) Math.min(scanner.getEndOffset(), end);
			position = (position <= start) ? end : position;

			Style style = context.getStyle(scanner.token);
			
			if (style == null) {
				style = DEFAULT_STYLE;
			}

			// color change, flush what we have
			g.setFont(g.getFont().deriveFont(getFontStyle(style)));

			Segment text = getLineBuffer();
			doc.getText(start, position - start, text);

			int x1 = x;
			x = Utilities.drawTabbedText(text, x, y, g, (TabExpander) this, start);

			if (scanner.isError()) {
				drawError(scanner, g, x1, x - x1, y, true);
			}

			start = position;
		}

		return x;
	}
	
	protected abstract void updateScanner(SyntaxHighlightingScanner scanner, Document doc, int start, int end);

	/**
	 * Fetch the font to use for a lexical token with the given scan value.
	 * 
	 * @param style
	 *            the style.
	 * 
	 * @return the font style
	 */
	public static int getFontStyle(Style style) {
		int fontStyle = Font.PLAIN;

		if (style != null) {
			if (StyleConstants.isItalic(style)) {
				fontStyle += Font.ITALIC;
			}

			if (StyleConstants.isBold(style)) {
				fontStyle += Font.BOLD;
			}
		}

		return fontStyle;
	}
}