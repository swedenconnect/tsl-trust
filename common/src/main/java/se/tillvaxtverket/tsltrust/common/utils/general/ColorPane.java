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
package se.tillvaxtverket.tsltrust.common.utils.general;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

/**
 * JTextPane extension for display of styled text
 */
public class ColorPane {

    public static final String BLUE = "Blue";
    public static final String BLUE_BOLD = "BlueBold";
    public static final String RED_BOLD = "RedBold";
    public static final String RED = "Red";
    public static final String BLUE_UNDERLINE = "BlueUnderline";
    public static final String BLUE_BOLD_UNDERLINE = "BlueBoldUnderline";
    public static final String GRAY = "Gray";
    public static final String GREEN = "Green";
    public static final String GREEN_BOLD = "GreenBold";
    public static final String ORANGE = "Orange";
    public static final String MAGENTA = "Magenta";
    public static final String ATTRIBUTE = "Attribute";
    public static final String LIGHT_BLUE = "LightBlue";
    public static final String BOLD = "Bold";
    public static final String BOLD_UNDERLINE = "BoldUnderline";
    public static final String UNDERLINE = "Underline";
    public static final String NORMAL = "Normal";
    protected JTextPane pane;
    protected StyledDocument sd;
    protected String tempBuffer;
    protected List<StyleAttributes> paneStyles;

    public ColorPane(JTextPane jPane) {
        this.pane = jPane;
        this.sd = pane.getStyledDocument();
        this.tempBuffer = pane.getText();
        this.paneStyles = new LinkedList<StyleAttributes>();


        // Makes text blue
        Style style = pane.addStyle(BLUE, null);
        StyleConstants.setForeground(style, new Color(42,0,255));
        StyleConstants.setBold(style, false);

        // Makes text blue and bold
        style = pane.addStyle(BLUE_BOLD, null);
        StyleConstants.setForeground(style, new Color(42,0,255));
        StyleConstants.setBold(style, true);

        // Makes text red and bold
        style = pane.addStyle(RED_BOLD, null);
        StyleConstants.setForeground(style, Color.RED.darker());
        StyleConstants.setBold(style, true);

        // Makes text red
        style = pane.addStyle(RED, null);
        StyleConstants.setForeground(style, Color.RED.darker());
        StyleConstants.setBold(style, false);

        // Makes text underlined blue
        style = pane.addStyle(BLUE_UNDERLINE, null);
        StyleConstants.setForeground(style, new Color(42,0,255));
        StyleConstants.setBold(style, false);
        StyleConstants.setUnderline(style, true);

        // Makes text underlined blue and bold
        style = pane.addStyle(BLUE_BOLD_UNDERLINE, null);
        StyleConstants.setForeground(style, new Color(42,0,220));
        StyleConstants.setBold(style, true);
        StyleConstants.setUnderline(style, true);

        // Makes text gray
        style = pane.addStyle(GRAY, null);
        StyleConstants.setForeground(style, Color.gray);

        // Makes text Green
        style = pane.addStyle(GREEN, null);
        StyleConstants.setForeground(style, Color.getHSBColor(0.28f, 1.0f, 0.5f));

        // Makes text GreenBold
        style = pane.addStyle(GREEN_BOLD, null);
        StyleConstants.setForeground(style, Color.getHSBColor(0.28f, 1.0f, 0.5f));
        StyleConstants.setBold(style, true);

        // Makes text DarkOrange
        style = pane.addStyle(ORANGE, null);
        StyleConstants.setForeground(style, Color.ORANGE.darker());

        // Makes text DarkOrange
        style = pane.addStyle(LIGHT_BLUE, null);
        StyleConstants.setForeground(style, new Color(63, 95, 191));

        // Makes text DarkOrange
        style = pane.addStyle(ATTRIBUTE, null);
        StyleConstants.setForeground(style, new Color(63,127,127));

                // Makes text DarkOrange
        style = pane.addStyle(MAGENTA, null);
        StyleConstants.setForeground(style, new Color(127,0,127));

        // Makes text bold
        style = pane.addStyle(BOLD, null);
        StyleConstants.setBold(style, true);

        // Makes text bold and UNderlined
        style = pane.addStyle(BOLD_UNDERLINE, null);
        StyleConstants.setBold(style, true);
        StyleConstants.setUnderline(style, true);

        // Makes text Underlined
        style = pane.addStyle(UNDERLINE, null);
        StyleConstants.setForeground(style, Color.black);
        StyleConstants.setUnderline(style, true);

        // Makes text normal and black
        style = pane.addStyle(NORMAL, null);
        StyleConstants.setForeground(style, Color.black);
        StyleConstants.setBold(style, false);


    }

    public void addStyledText(String inpString, String style) {
        if (inpString == null) {
            return;
        }
        int offset = tempBuffer.length();
        int len = inpString.length();
        tempBuffer += inpString;
        paneStyles.add(new StyleAttributes(offset, len, style, true));
    }

    public void addStyledTextLine(String inpString, String style) {
        if (inpString == null) {
            return;
        }
        int offset = tempBuffer.length();
        int len = inpString.length();
        tempBuffer += inpString;
        paneStyles.add(new StyleAttributes(offset, len, style, true));
        addLF();
    }

    public void addPlainText(String inpString) {
        tempBuffer += inpString;
    }

    public void addPlainTextLine(String inpString) {
        tempBuffer += inpString;
        addLF();
    }

    public void addLF() {
        tempBuffer += (char) 10;
    }

    public void renderText() {
        pane.setText(tempBuffer);
        sd.setCharacterAttributes(0, tempBuffer.length(), pane.getStyle(NORMAL), true);
        for (StyleAttributes sa : paneStyles) {
            sd.setCharacterAttributes(sa.Offset, sa.Length, pane.getStyle(sa.style), sa.replace);
        }

    }

    protected class StyleAttributes {

        int Offset;
        int Length;
        String style;
        Boolean replace;

        StyleAttributes(int ofs, int len, String stl, Boolean r) {
            this.Offset = ofs;
            this.Length = len;
            this.style = stl;
            this.replace = r;
        }
    }
}
