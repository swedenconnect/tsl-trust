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
package se.tillvaxtverket.ttsigvalws.ttwsconsole.config;

import se.tillvaxtverket.tsltrust.common.utils.general.JsonConfigData;

/**
 * Configuration data class for signature validation functions
 */
public class SigValConfig implements JsonConfigData {

    private String languageCode;
    private String validationTimeoutSeconds;

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public void setDefaults() {
        languageCode = "en";
        validationTimeoutSeconds = "20";
    }

    public SigValConfig() {
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getValidationTimeoutSeconds() {
        return validationTimeoutSeconds;
    }

    public void setValidationTimeoutSeconds(String validationTimeoutSeconds) {
        this.validationTimeoutSeconds = validationTimeoutSeconds;
    }
}
