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

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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
    private JTextPane pane;
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

    /**
     * Creating a logger with a default publishing target
     * @param name The name of the logger
     * @param target Target JTextPane for publishing log messages.
     */
    public ContextLogger(String name, JTextPane target) {
        super(name, null);
        setLevel(Level.ALL);
        addObservers();
        setTarget(target);
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
    
    public void setTarget(JTextPane textComponent) {
        setTarget(textComponent, formatters.coreFormatter());
    }
    
    public void setTarget(JTextPane textComponent, Formatter formatter) {
        pane = textComponent;
        OutputStream out = new OutputStream() {
            
            @Override
            public void write(final int b) throws IOException {
                updateTextPane(String.valueOf((char) b));
            }
            
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextPane(new String(b, off, len));
            }
            
            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
        Handler logHandler = new StreamHandler(out, formatter);
        addHandler(logHandler);
    }
    
    private void updateTextPane(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                Document doc = pane.getDocument();
                try {
                    doc.insertString(doc.getLength(), text, null);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
                pane.setCaretPosition(doc.getLength() - 1);
            }
        });
    }
    
    public void clearTargetPane() {
        if (pane == null) {
            return;
        }
        pane.setText("");
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
