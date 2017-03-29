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
