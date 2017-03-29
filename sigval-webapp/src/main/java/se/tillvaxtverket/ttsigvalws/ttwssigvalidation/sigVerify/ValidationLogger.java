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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify;

import java.util.ArrayList;
import java.util.List;

/**
 * Log data class used to capture events in the certificate status processes
 */
public class ValidationLogger {

    public List<String> log;
    public List<String> verboseLog;
    public List<String> exceptionLog = new ArrayList<String>();

    public ValidationLogger() {
        log = new ArrayList<String>();
    }

    public ValidationLogger(int initialCapacity) {
        log = new ArrayList<String>(initialCapacity);
        verboseLog = new ArrayList<String>(initialCapacity);
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }

    public List<String> getExceptionLog() {
        return exceptionLog;
    }

    public void setExceptionLog(List<String> exceptionLog) {
        this.exceptionLog = exceptionLog;
    }

    public void logString(String logData) {
        log.add(logData);
        verboseLog.add(logData);
    }

    public void verboseLogString(String logData) {
        verboseLog.add(logData);
    }

    public void logStringList(List<String> logData) {
        for (String str : logData) {
            log.add(str);
        }
    }

    public void loggInt(int intValue) {
        log.add(String.valueOf(intValue));
    }

    public String getLogtoString() {
        StringBuilder b = new StringBuilder();
        for (String str : log) {
            b.append(str).append((char) 10);
        }
        return b.toString();
    }

    public void logException(String exception) {
        exceptionLog.add(exception);
    }

    public void logException(Exception ex) {
        exceptionLog.add(ex.toString());
        exceptionLog.add(generateUnderline(ex.toString().length()));
        StackTraceElement[] stackElements = ex.getStackTrace();
        for (StackTraceElement se : stackElements) {
            exceptionLog.add(se.toString());
        }
        exceptionLog.add("");
    }

    public String getExceptionLogtoString() {
        StringBuilder b = new StringBuilder();
        for (String str : exceptionLog) {
            b.append(str).append((char) 10);
        }
        return b.toString();
    }

    public void clearLog() {
        log = new ArrayList<String>();
        exceptionLog = new ArrayList<String>();
    }
    
    private String generateUnderline(int len){
        String underline="";
        for (int i = 0;i<len;i++){
            underline +="-";
        }
        return underline;
    }
    
}
