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
