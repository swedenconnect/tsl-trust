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

import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Filtering class providing input filter options for text data supplied by an untrusted source
 */
public class InputValidator {

    private static final Logger LOG = Logger.getLogger(InputValidator.class.getName());
    private static final HTMLInputFilter htmlFilter = new HTMLInputFilter();
    private static final char SINGLE_QUOTE = (char) 39; // '   
    private static final char DOUBLE_QUOTE = (char) 34; // "
    private static final char START_BRACKET = (char) 60;// <
    private static final char END_BRACKET = (char) 62;  // >
    private static final char SPACE = (char) 32;
    static {
        ArrayList<String> no_atts = new ArrayList<String>();
        htmlFilter.vAllowed.put("br", no_atts);
    }

    public static String filter(String inpText, Rule rule) {
        String filtered = "";

        switch (rule) {
            case ASCII:
                filtered = applyAsciiRule(inpText, false);
                break;
            case PRINTABLE_ASCII:
                filtered = applyAsciiRule(inpText, true);
                break;
            case INTEGER:
                filtered = applyIntegerRule(inpText);
                break;
            case BASE64:
                filtered = applyBase64Rule(inpText);
                break;
            case TEXTAREA:
                filtered = applyTextAreaRule(inpText);
                break;
            case HTML_TAGS:
                filtered = applyHtmlTagRule(inpText);
                break;
            case TEXT_LABEL:
                filtered = applyTextLabelRule(inpText);
                break;
            case NO_SCRIPT:
                filtered = applyNoScriptRule(inpText);
                break;
            case NO_FRAMES:
                filtered = applyNoFramesRule(inpText);
                break;
            case HTML_SCRUB:
                filtered = applyNoFramesRule(applyNoScriptRule(inpText));
                break;
        }

        return filtered;
    }

    public static String filter(String inpText, Rule[] rules) {
        String filtered = (inpText == null) ? "" : inpText;
        for (Rule rule : rules) {
            filtered = filter(filtered, rule);
        }
        return filtered;
    }

    private static String applyAsciiRule(String inpText, boolean printable) {
        StringBuilder b = new StringBuilder();
        try {
            byte[] bytes = inpText.getBytes("UTF-8");
            for (byte cb : bytes) {
                if (printable) {
                    if ((int) cb > 31 && (int) cb < 127) {
                        b.append((char) cb);
                    }
                    if ((int) cb == 10) {
                        b.append("\n");
                    }
                } else {
                    if ((int) cb < 128) {
                        b.append((char) cb);
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return b.toString();
    }

    private static String applyIntegerRule(String inpText) {
        try {
            int num = Integer.valueOf(inpText.trim());
            return String.valueOf(num);
        } catch (Exception ex) {
        }
        return "0";
    }

    private static String applyBase64Rule(String inpText) {
        String encodeLines = "";
        try {
        } catch (Exception ex) {
            byte[] decodeLines = Base64Coder.decodeLines(inpText);
            encodeLines = Base64Coder.encodeLines(decodeLines);
        }
        return encodeLines;
    }

    private static String applyTextAreaRule(String inpText) {
        String[] bannedStrings = new String[]{"textarea"};
        String[] replaceStrings = new String[]{"xxtextareaxx"};
        return banMarkup(inpText, bannedStrings, replaceStrings);
    }

    private static String applyHtmlTagRule(String inpText) {
        String[] bannedStrings = new String[]{"<", ">"};
        String[] replaceStrings = new String[]{"[", "]"};
        return replaceText(inpText, bannedStrings, replaceStrings);
    }

    private static String applyTextLabelRule(String inpText) {
        StringBuilder b = new StringBuilder();
        try {
            byte[] bytes = inpText.getBytes("UTF-8");
            for (byte cb : bytes) {
                if ((int) cb == 32) {
                    b.append(" ");
                }
                if ((int) cb == 45) {
                    b.append("-");
                }
                if ((int) cb == 95) {
                    b.append("_");
                }
                if ((int) cb >= 48 && (int) cb <= 57) { // 0-9
                    b.append((char) cb);
                }
                if ((int) cb >= 65 && (int) cb <= 90) { // A-Z
                    b.append((char) cb);
                }
                if ((int) cb >= 97 && (int) cb <= 122) { // a-z
                    b.append((char) cb);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return b.toString();
    }

    private static String replaceText(String inpText, String[] bannedStrings, String[] replaceStrings) {
        for (int i = 0; i < bannedStrings.length; i++) {
            String banned = bannedStrings[i];
            String replace = (replaceStrings.length > i) ? replaceStrings[i] : "";
            inpText = Pattern.compile(banned, Pattern.CASE_INSENSITIVE).matcher(inpText).replaceAll(replace);
        }
        return inpText;
    }

    private static String banMarkup(String inpText, String[] bannedStrings, String[] replaceStrings) {
        if (inpText == null || inpText.length() == 0) {
            return "";
        }
        // ban text with comment info <%--
        if (inpText.indexOf("<%--") > -1) {
            return "";
        }
        List<Integer> sbIdx = new ArrayList<Integer>();
        List<Integer> ebIdx = new ArrayList<Integer>();

        boolean tag = false;

        for (int i = 0; i < inpText.length(); i++) {
            char charAt = inpText.charAt(i);
            if (isQuoteChar(charAt) && tag) {
                i = skipQuoted(charAt, i, inpText);
                continue;
            }
            if (charAt == START_BRACKET) {
                try {
                    char nextChar = inpText.charAt(i + 1);
                    if (isLetter(nextChar)) {
                        sbIdx.add(i);
                        tag = true;
                    }
                } catch (Exception ex) {
                }
            }
            if (charAt == END_BRACKET && tag) {
                ebIdx.add(i);
                tag = false;
            }
        }
        String markupFilter = markupFilter(inpText, sbIdx, ebIdx, bannedStrings, replaceStrings);

        return markupFilter;
    }

    private static String applyNoScriptRule(String inpText) {
        String filter = htmlFilter.filter(inpText);
        return filter;
//        String[] bannedStrings = new String[]{"eval", "javascript", "script", "body", "onload","\\"};
//        String[] replaceStrings = new String[]{"xxeva1xx", "xxjavascr1ptxx", "xxscr1ptxx","xxbodyxx", "xxonloadxx","x"};
//        return banMarkup(filter, bannedStrings, replaceStrings);
    }

    private static String applyNoFramesRule(String inpText) {
        String[] bannedStrings = new String[]{"frame"};
        String[] replaceStrings = new String[]{"xxframexx"};
        return banMarkup(inpText, bannedStrings, replaceStrings);
    }

    private static void addIndex(String string, int d, int s, List<List<String>> quoteParts, List<Integer> bracketIdx) {
        int partIdx = 0;
        String part = quoteParts.get(d).get(s);
        // Gert index to the first character of the target dquote string
        for (int i = 0; i < d; i++) {
            List<String> dQparts = quoteParts.get(i);
            for (int j = 0; j < dQparts.size(); j++) {
                partIdx += dQparts.get(j).length();
                partIdx++;
            }
        }
        // Add index to the first character of the target squote string
        List<String> dqPart = quoteParts.get(d);
        for (int i = 0; i < s; i++) {
            String sqPart = dqPart.get(i);
            partIdx += sqPart.length();
            partIdx++;
        }
        //Look for presence of the target charcter
        int tIdx = 0;
        int len = part.length();
        while (tIdx < len) {
            int idx = part.indexOf(string, tIdx);
            if (idx > -1) {
                // Add found targets
                bracketIdx.add(partIdx + idx);
                tIdx = idx + 1;
            } else {
                break;
            }
        }
    }

    private static int skipQuoted(char quoteChar, int startIndex, String inpStr) {
        int endIndex = startIndex;
        // first char is quoted mark it as quoted and skip
        startIndex++;
        for (int i = startIndex; i < inpStr.length(); i++) {
            char charAt = inpStr.charAt(i);
            // If we found the matching end quote
            if (charAt == quoteChar) {
                return i;
            }
            endIndex = i;
        }
        return endIndex;
    }

    private static boolean isQuoteChar(char charAt) {
        return charAt == SINGLE_QUOTE || charAt == DOUBLE_QUOTE;
    }

    private static boolean isLetter(char charAt) {
        boolean letter = true;
        int charCode = (int) charAt;
        if (charCode < 65 || charCode > 122) {
            letter = false;
        }
        if (charCode > 90 && charCode < 97) {
            letter = false;
        }
        return letter;
    }

//    private static boolean isDifferentQuoteChar(char quoteChar, char charAt) {
//        return (quoteChar != charAt && isQuoteChar(charAt));
//    }
//
//    private static char otherQuoteChar(char quoteChar) {
//        return quoteChar == SINGLE_QUOTE ? DOUBLE_QUOTE : SINGLE_QUOTE;
//    }
    private static String markupFilter(String inpStr, List<Integer> sbList, List<Integer> ebList, String[] banStrings, String[] replaceStrings) {
        // require the number of start and closing brackets to match
        if (sbList.size() != ebList.size()) {
            return "Bad markup";
        }
        // require ban and replaceStrings to have the same number of strings
        if (banStrings.length != replaceStrings.length) {
            return "Internal error";
        }

        int prevE = -1, s = 0, e = 0;
        for (int i = 0; i < sbList.size(); i++) {
            s = sbList.get(i);
            e = ebList.get(i);
            String sb = String.valueOf(inpStr.charAt(s));
            String eb = String.valueOf(inpStr.charAt(e));
            if (!sb.equals("<") || !eb.equals(">")) {
                return "Bad markup";
            }
            if (e < s || s < prevE) {
                return "Bad markup";
            }
            prevE = e;
        }

        //Structure is OK, now filter
        //The filtering is done end -  start to preseve the index of not yet processed replacements
        String filter = inpStr;
        for (int i = sbList.size() - 1; i > -1; i--) {
            s = sbList.get(i);
            e = ebList.get(i);
            String start = filter.substring(0, s + 1);
            //Get the string inside the tag brackets
            String target = filter.substring(s + 1, e);
            String end = filter.substring(e);
            String replaceText = replaceText(target, banStrings, replaceStrings);
            filter = start + replaceText + end;
        }
        return filter;
    }

    public static enum Rule {

        ASCII, PRINTABLE_ASCII, INTEGER, BASE64, TEXTAREA, HTML_TAGS, TEXT_LABEL, NO_SCRIPT, NO_FRAMES, HTML_SCRUB
    }
}
