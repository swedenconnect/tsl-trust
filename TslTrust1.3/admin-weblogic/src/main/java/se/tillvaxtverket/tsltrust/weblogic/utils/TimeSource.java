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
