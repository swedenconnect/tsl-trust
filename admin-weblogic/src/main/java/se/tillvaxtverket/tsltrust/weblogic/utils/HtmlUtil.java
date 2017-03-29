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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.ImageElement;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for html manipulations
 */
public class HtmlUtil implements TTConstants, HtmlConstants {

    private static final String[] sigStatusKey = new String[]{
        SIGNSTATUS_VERIFIED,
        SIGNSTATUS_INVALID,
        SIGNSTATUS_SYNTAX,
        SIGNSTATUS_ABSENT,
        SIGNSTATUS_UNVERIFIABLE,
        SIGNSTATUS_INVALID_LOTL
    };
    private static final String[] sigStatusText;
    private static final String[] sigStatusIcon = new String[]{
        "img/Ok-icon.png",
        "img/Nok-icon.png",
        "img/Nok-icon.png",
        "img/Nok-icon.png",
        "img/inconclusive.png",
        "img/inconclusive.png"
    };
    private static Map<String, String> sigStatusMap, sigIconMap;

    static {
        sigStatusText = new String[]{
            b("Valid")+"- This TSL is signed and the signature is verified",
            b("Invalid")+" - The signature on this TSL failed signature validation",
            b("Syntax")+" - The signature on this TSL could not be parsed",
            b("Absent")+" - This TSL is not signed. Accuracy of provided information can't be verified",
            b("Unverifiable")+" - The Signer's certificate is not provided in the signed EU list of TSLs (LotL)",
            b("Invalid LotL Signature")+" - The TSL signature can't be verified since the EU list of TSLs failed signature validation"
        };
        sigStatusMap = new HashMap<String, String>();
        sigIconMap = new HashMap<String, String>();
        for (int i = 0; i < sigStatusKey.length; i++) {
            sigStatusMap.put(sigStatusKey[i], sigStatusText[i]);
            sigIconMap.put(sigStatusKey[i], sigStatusIcon[i]);
        }
    }

    public static String link(String url, String refText) {
        HtmlElement a = new GenericHtmlElement("a");
        a.addAttribute("href", url);
        a.setText(refText);
        return a.toString();
    }

    public static String link(String url) {
        return link(url, url);
    }

    public static String tagString(String text, String tag) {
        HtmlElement str = new GenericHtmlElement(tag);
        str.setText(text);
        return str.toString();
    }
    
    public static String countServices(int cnt){
        HtmlElement str = new GenericHtmlElement("span");
        str.addAttribute("class", "counterString");
        String label = (cnt==1)? " service)":" services)";
        str.setText(SPACE+"("+String.valueOf(cnt)+label);
        return str.toString();
    }

    public static String b(String bold) {
        return tagString(bold, "b");
    }

    public static String strong(String strong) {
        return tagString(strong, "strong");
    }

    public static String h1(String heading) {
        return tagString(heading, "h1");
    }

    public static String h2(String heading) {
        return tagString(heading, "h2");
    }

    public static String h3(String heading) {
        return tagString(heading, "h3");
    }

    public static String getSignStatusMessage(String sigStatus, int heightPx) {
        String statusText;
        String statusIcon;
        String height = String.valueOf(heightPx);

        if (!sigStatusMap.containsKey(sigStatus)) {
            return "Unrecognized Signature Status";
        }
        statusIcon = sigIconMap.get(sigStatus);

        HtmlElement icn = new ImageElement(statusIcon);
        icn.addAttribute("height", height);

        statusText = icn.toString() + " " + sigStatusMap.get(sigStatus);

        return statusText;
    }
}