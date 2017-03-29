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

import java.util.logging.Logger;

/**
 * Utility functions for TSL data
 */
public class ExtractorUtil {

    private static final Logger LOG = Logger.getLogger(ExtractorUtil.class.getName());


    public static String stripRefUrl(String inpString) {
        final String[] prefix = {
            "http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/",
            "http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/TSLType/",
            "http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/StatusDetn/",
            "http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/schemerules/",
            "http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/Svcstatus/",
            "http://uri.etsi.org/TrstSvc/",
            "http://uri.etsi.org/TrstSvc/Svctype/",
            "http://uri.etsi.org/02231/Svctype/",
            "http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/SvcInfoExt/"
        };
        inpString = inpString.trim();
        String outString = inpString;
        int len = inpString.length();

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < prefix.length; j++) {
                if (inpString.substring(0, i).equalsIgnoreCase(prefix[j])) {
                    outString = inpString.substring(i, len);
                }
            }
        }
        return outString;
    }
}
