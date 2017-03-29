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
