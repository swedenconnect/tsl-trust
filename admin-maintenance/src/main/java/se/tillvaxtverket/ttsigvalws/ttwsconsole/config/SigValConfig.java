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
