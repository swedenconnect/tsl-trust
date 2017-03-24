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

import java.io.File;
import java.io.FilenameFilter;

/**
 * File name filter for filtering out file names with a certain start string
 * Usually used as new FilenameFileterImpl(".") to filter out directory references
 */
public class FilenameFilterImpl implements FilenameFilter{
        String bannedStart;

        public FilenameFilterImpl(String banned) {
            bannedStart = banned;
        }

    @Override
        public boolean accept(File file, String fname) {
            return !fname.startsWith(bannedStart);
        }

}
