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
package se.tillvaxtverket.tsltrust.common.utils.general;

/**
 * Generic constants
 */
public interface Constants {

    /**
     * The system line separator string
     */
    public static final String LF = System.getProperty("line.separator");
    /**
     * Quote ASCII character
     */
    public static final char QUOTE = (char) 34;
    /**
     * Back slash ASCII character
     */
    public static final char BACKSLASH = (char) 92;
    /**
     * Forward slash ASCII character
     */
    public static final char FORWARDSLASH = (char) 47;
    /**
     * "0" character ASCII code 48
     */
    public static final int ZERO = (char) 48;
    /**
     * "9" character ASCII code 57
     */
    public static final int NINE = (char) 57;
    /**
     * Capital "A" ASCII code 65
     */
    public static final int CAPITAL_A = (char) 65;
    /**
     * Capital "Z" ASCII code 90
     */
    public static final int CAPITAL_Z = (char) 90;
    /**
     * Small letter "a" ASCII code 97
     */
    public static final int SMALL_A = (char) 97;
    /**
     * Small letter "z" ASCII code 122
     */
    public static final int SMALL_Z = (char) 122;
    /**
     * Hyphen "-" ASCII code 45
     */
    public static final int HYPHEN_DASH = (char) 45;
    /**
     * Underscore "_" ASCII code 95
     */
    public static final int UNDERSCORE_DASH = (char) 95;
    /**
     * Capital "C" ASCII code 67
     */
    public static final int CAPITAL_C = (char) 67;
    /**
     * Capital "N" ASCII code 78
     */
    public static final int CAPITAL_N = (char) 78;
    /**
     * Equal sign "=" ASCII code 61
     */
    public static final int EQUAL = (char) 61;
    /**
     * Comma sign "," ASCII code 44
     */
    public static final int COMMA = (char) 44;
    /**
     * Plus sign "," ASCII code 43
     */
    public static final int PLUS = (char) 43;
    /**
     * Comma sign ":" ASCII code 58
     */
    public static final int COLON = (char) 58;
    /*********
     * 
     * TSL Trust Constants
     * 
     * 
     */
    /**
     * Supported ID Attributes
     */
    static final String[] SUPPORTED_ID_ATTRIBUTES = new String[]{"persistent-id","norEduPersonNIN", "personalIdentityNumber"};
    /**
     * Shibboleth Idp Attribute
     */
    static final String IDP_ATTRIBUTE = "Shib-Identity-Provider";

}
