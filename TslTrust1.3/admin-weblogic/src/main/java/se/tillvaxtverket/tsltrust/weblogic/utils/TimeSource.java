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

/**
 * Simple functions for time source conversions 
 */
public class TimeSource {
    private static long lastTime = System.currentTimeMillis();
           
    /**
     * Provides unique current time. This is useful in batch jobs when current time is used as 
     * the database key. If two concurrent time requests are received within the same
     * millisecond, then one millisecond is added to current time in order to ensure
     * that each given time is unique.
     * 
     * @return Unique current time in milliseconds since midnight, January 1, 1970 UTC
     */
    public static long getCurrentTime(){
        long currentTime = System.currentTimeMillis();
        if (currentTime <= lastTime){
            currentTime=lastTime+1;
        }
        lastTime=currentTime;
        return currentTime;
    }    
}
