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

/**
 * Observer constants
 */
public interface ObserverConstants {
    // The observed object acknowledges an abort request
    public static final Object ABORTED = "aborted";
    // The observed object has terminated after an abort request
    public static final Object RETURN_FROM_ABORT = "returnFromAbort";
    // The observed object has completed it's task
    public static final Object COMPLETE = "complete";
    // The observed class has completed it's task through a cancel action
    public static final Object CANCEL = "cancel";
    // The observed object has completed it's task through a save action
    public static final Object SAVE = "save";
    // Project specific
    public static final Object CACHEDEAMON_CLOSE = "cdClose";
    public static final Object PREFERENCES_SAVE = "prefSave";
    public static final Object PREFERENCES_CANCEL = "prefCancel";
    
}
