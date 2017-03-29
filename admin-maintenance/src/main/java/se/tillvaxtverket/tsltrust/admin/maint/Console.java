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