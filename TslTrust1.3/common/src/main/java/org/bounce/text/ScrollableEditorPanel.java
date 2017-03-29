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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.text.View;
import javax.swing.text.WrappedPlainView;

/**
 * Wrapper panel to force the editor pane to resize when a 
 * Wrapped View has been installed. 
 * 
 * Takes Block Increment and Unit Increment info from editor.
 * 
 * @version $Revision: 1.2 $, $Date: 2008/01/28 21:02:18 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class ScrollableEditorPanel extends JPanel implements Scrollable {
        private static final long serialVersionUID = 3978147659863437620L;

        private JEditorPane editor = null;
        
        /**
         * Constructs the panel, with the editor in the Center 
         * of the BorderLayout.
         * 
         * @param editor the parent editor.
         */
        public ScrollableEditorPanel( JEditorPane editor) {
            super( new BorderLayout());
            
            this.editor = editor;
            
            add( editor, BorderLayout.CENTER);
        }
        
        /**
         * @see Scrollable#getPreferredScrollableViewportSize()
         */
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        /**
         * Returns the information directly from the editor component.
         * 
         * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
         */
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return editor.getScrollableUnitIncrement( visibleRect, orientation, direction);
        }

        /**
         * Returns the information directly from the editor component.
         * 
         * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
         */
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return editor.getScrollableBlockIncrement( visibleRect, orientation, direction);
        }  

        /**
         * Return true when a Wrapped View is used.
         * 
         * @see Scrollable#getScrollableTracksViewportWidth()
         */
        public boolean getScrollableTracksViewportWidth() {
        	View view = editor.getUI().getRootView( editor).getView(0);
        
            if ( view instanceof WrappedPlainView) {
                return true;
            } else if ( getParent() instanceof JViewport) {
                return (((JViewport)getParent()).getWidth() > getPreferredSize().width);
            }

            return false;
        }

        /**
         * @see Scrollable#getScrollableTracksViewportHeight()
         */
        public boolean getScrollableTracksViewportHeight() {

        	if ( getParent() instanceof JViewport) {
                return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
            }
            
            return false;
        }
    }