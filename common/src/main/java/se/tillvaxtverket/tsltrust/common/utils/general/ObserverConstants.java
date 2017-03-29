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
