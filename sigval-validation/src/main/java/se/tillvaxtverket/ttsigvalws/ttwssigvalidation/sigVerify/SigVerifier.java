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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify;

import java.util.Observable;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.utils.general.ObserverConstants;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;

/**
 * Abstract superclass for signature verification of signed documents
 */
public abstract class SigVerifier extends Observable implements ObserverConstants, Runnable {

    protected static final Logger LOG = Logger.getLogger(PdfSigVerifier.class.getName());

    public SigVerifier() {
    }

    protected abstract void getSignatureContext();
    protected SigValidationModel model;
    protected boolean running = true;
    protected ResourceBundle textBundle = ResourceBundle.getBundle("reportText");

    @Override
    public void run() {
        getSignatureContext();
    }

    protected void errClose() {
        model.setSignVerificationComplete(false);
        setChanged();
        if (running) {
            notifyObservers(COMPLETE);
        } else {
            notifyObservers(RETURN_FROM_ABORT);
        }
    }
}
