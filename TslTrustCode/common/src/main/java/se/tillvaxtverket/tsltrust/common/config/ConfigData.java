/*
 * Copyright 2013 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *   Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 *   European Commission - subsequent versions of the EUPL (the "Licence");
 *   You may not use this work except in compliance with the Licence. 
 *   You may obtain a copy of the Licence at:
 * 
 *   http://joinup.ec.europa.eu/software/page/eupl 
 * 
 *   Unless required by applicable law or agreed to in writing, software distributed 
 *   under the Licence is distributed on an "AS IS" basis,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 *   implied.
 *   See the Licence for the specific language governing permissions and limitations 
 *   under the Licence.
 */
package se.tillvaxtverket.tsltrust.common.config;

/**
 * Interface for Configuration data classes. Each implementation is supposed to define a set of vectors
 * that can be serialized into Json by the factory class.
 */
public interface ConfigData {
    
    /**
     * Set default values for each configuration value
     */
    public void setDefaults();
    
    /**
     * Get the name of this parameter file. The parameter file will be named according to 
     * this name with the addition of a .json extension.
     * @return 
     */
    public String getName();
   
}
