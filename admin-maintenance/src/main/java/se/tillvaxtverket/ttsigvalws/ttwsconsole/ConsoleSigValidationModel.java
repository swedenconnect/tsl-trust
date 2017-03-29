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
package se.tillvaxtverket.ttsigvalws.ttwsconsole;

import javax.swing.JTextPane;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;

/**
 * Sub class of the Signature validation model used by the signature validation web application
 * This subclass redirects the log data to UI components of the local application.
 */
public class ConsoleSigValidationModel extends SigValidationModel {

    public ConsoleSigValidationModel() {
        super();
    }

    public ConsoleSigValidationModel(JTextPane ocspPane, JTextPane crlPane) {
        super();
        init(ocspPane, crlPane, null);
    }

    public ConsoleSigValidationModel(JTextPane ocspPane, JTextPane crlPane, JTextPane exceptionPane) {
        super();
        init(ocspPane, crlPane, exceptionPane);
    }

    private void init(JTextPane ocspPane, JTextPane crlPane, JTextPane exceptionPane) {
        OLOG.setTarget(ocspPane);
        CLOG.setTarget(crlPane);

        if (exceptionPane != null) {
            ELOG.setTarget(exceptionPane);
        }

    }
}
