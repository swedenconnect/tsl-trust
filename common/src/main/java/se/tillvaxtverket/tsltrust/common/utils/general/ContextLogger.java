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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.*;

/**
 * Class holding a customized logger based on the Logger class.
 * This class provides it's own formatters and handlers and handles publication
 * of these handlers.
 * 
 * One handler publish log messages to the console and the other handler allows
 * assignment of a JTextPane component to publication.
 * 
 * Three formatters are provided in the class for clean, time stamped and verbose
 * logging.
 * 
 */
public final class ContextLogger extends Logger implements Observer, ObserverConstants {
    
    static final String LF = System.getProperty("line.separator");
    private Formatters formatters = new Formatters();
    private Publisher publisher = new Publisher();

    /**
     * Creating a logger that provides log messages in accordance with a provided
     * ResourceBundler
     * @param name Name of the logger
     * @param resourceBundleName Name of the ResourceBundle
     */
    public ContextLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
        setLevel(Level.ALL);
        addObservers();
    }

    /**
     * Creating logger with optional console logging turned on
     * @param name Name of the Logger
     * @param consoleLogger true for turning console logging on or false for
     * creating a clean logger without any output handler.
     */
    public ContextLogger(String name, boolean consoleLogger) {
        super(name, null);
        setLevel(Level.ALL);
        addObservers();
        if (consoleLogger) {
            addConsoleLogging();
        }
    }

    /**
     * Creating logger with no set log handler
     * @param name Name of the Logger
     */
    public ContextLogger(String name) {
        super(name, null);
        setLevel(Level.ALL);
        addObservers();
    }


    private void addObservers() {
        formatters.addObserver(this);
    }
    
    private void publish() {
        Thread pt = new Thread(publisher);
        pt.start();
    }
    
    public void addConsoleLogging(Formatter formatter) {
        addHandler(new StreamHandler(System.out, formatter));
    }
    
    public void addConsoleLogging() {
        addHandler(new StreamHandler(System.out, formatters.fullFormatter()));
    }
    
    public Formatter simpleFormatter() {
        return new SimpleFormatter();
    }
    
    public Formatter coreFormatter() {
        return formatters.coreFormatter();
    }
    
    public Formatter timeFormatter() {
        return formatters.timeFormatter();
    }
    
    public void removeAllHandlers() {
        for (Handler h : getHandlers()) {
            removeHandler(h);
        }
    }

    public void update(Observable o, Object arg) {
        if (o instanceof Formatters) {
            if (arg.equals(COMPLETE)) {
                publish();
            }
        }
    }

    /**
     * Publisher Class to handle publication of log messages.
     * This is handled in its own thread since the publisher needs to
     * wait 100 ms in order to make sure that the log record has been properly
     * recorded. By handling this in a separate thread, it does not hold back the
     * main thread.
     */
    class Publisher implements Runnable {
        
        public void run() {
            try {
                Thread.sleep(100);
                Handler[] handlers = getHandlers();
                for (Handler h : handlers) {
                    h.flush();
                }
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * An Observable class which defines formating methods of log messages
     * This class is observable in order to allow the formatters to notify
     * the publisher when a new log record is formatted and ready for publication.
     */
    class Formatters extends Observable implements ObserverConstants {
        
        public Formatters() {
        }

        /**
         * Formatter which just publish the log message.
         * @return The formatted log message.
         */
        public Formatter coreFormatter() {
            Formatter formatter = new Formatter() {
                
                @Override
                public String format(LogRecord record) {
                    StringBuilder b = new StringBuilder();
                    b.append(record.getMessage()).append(LF);
                    setChanged();
                    notifyObservers(COMPLETE);
                    return b.toString();
                }
            };
            return formatter;
        }

        /**
         * Formatter, publishing the log message preceeded with time information
         * @return The formatted log message
         */
        public Formatter timeFormatter() {
            final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final Formatter formatter = new Formatter() {
                
                @Override
                public String format(LogRecord record) {
                    StringBuilder b = new StringBuilder();
                    b.append(timeFormat.format(new Date(record.getMillis())));
                    b.append(" - ");
                    b.append(record.getMessage()).append(LF);
                    setChanged();
                    notifyObservers(COMPLETE);
                    return b.toString();
                }
            };
            return formatter;
        }

        /**
         * Formatter, publishing a verbose log record on two lines.
         * @return First line holds time, class and method information. Second 
         * line holds information about log level and the log message.
         */
        public Formatter fullFormatter() {
            final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final Formatter formatter = new Formatter() {
                
                @Override
                public String format(LogRecord record) {
                    StringBuilder b = new StringBuilder();
                    b.append(timeFormat.format(new Date(record.getMillis()))).append(" ");
                    b.append(record.getSourceClassName()).append(" ");
                    b.append(record.getSourceMethodName()).append(" ").append(LF);
                    b.append(record.getLevel().toString()).append(": ");
                    b.append(record.getMessage()).append(LF);
                    setChanged();
                    notifyObservers(COMPLETE);
                    return b.toString();
                }
            };
            return formatter;
        }
    }
}
