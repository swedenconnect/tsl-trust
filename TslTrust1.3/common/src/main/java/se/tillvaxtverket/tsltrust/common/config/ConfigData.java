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
