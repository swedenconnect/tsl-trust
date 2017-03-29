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
package se.tillvaxtverket.tsltrust.admin.maint;

import java.text.SimpleDateFormat;
import se.tillvaxtverket.tsltrust.common.utils.general.ColorPane;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.JTextPane;

/**
 * This class provides basic functions for adding text to the console window in the 
 * admin daemon frame. This class is a subclass of the ColorPane class.
 */
public class Console extends ColorPane {

    /**
     * Simple Date format "yyyy-MM-dd HH:mm:ss"
     */
    static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String blanks = "";

    /**
     * Console constructor
     * @param jTextPane The JTextPane component where the console text is to be displayed
     * @param space the number of space characters that will separate display strings
     */
    public Console(JTextPane jTextPane, int space) {
        super(jTextPane);
        for (int i = 0; i < space; i++) {
            blanks += " ";
        }
    }

    /**
     * Clears the console window
     */
    public void clear() {
        pane.setText("");
        sd = pane.getStyledDocument();
        tempBuffer = "";
        paneStyles = new LinkedList<StyleAttributes>();
    }

    /**
     * Adding a text string to the console
     * @param t1 string to be displayed
     * @param styles Color Pane styles 
     */
    public void add(String t1, String[] styles) {
        String time = TIME_FORMAT.format(new Date());
        add(new String[]{time, t1}, styles);
    }

    /**
     * Adding two text strings to the console
     * @param t1 first string
     * @param t2 second string
     * @param styles ColorPane styles
     */
    public void add(String t1, String t2, String[] styles) {
        String time = TIME_FORMAT.format(new Date());
        add(new String[]{time, t1, t2}, styles);
    }

    /**
     * Adding an arbitrary array of strings to the console using provided styles
     * @param inpStrings Array of strings
     * @param styles Array of ColorPane styles
     */
    public void add(String[] inpStrings, String[] styles) {
        int is = inpStrings.length;
        int st = styles.length;
        int max = (is < st) ? is : st;
        boolean added = false;

        for (int i = 0; i < max; i++) {
            addStyledText(inpStrings[i] + blanks, styles[i]);
            added = true;
        }
        if (is > max) {
            for (int i = max; i < is; i++) {
                addPlainText(inpStrings[i] + blanks);
                added = true;
            }
        }
        if (added) {
            addLF();
        }
        renderText();
    }
}