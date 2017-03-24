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
package se.tillvaxtverket.ttsigvalws.daemon;

import java.util.logging.Level;
import java.util.logging.Logger;
import se.tillvaxtverket.ttsigvalws.daemon.ContextParameters;

/**
 * This class implements the servlet daemon that is invoked when initiating the servlet context.
 */
public abstract class ServletDaemon {

    private static final long MAX_TASK_SHUTDOWN_TIME = 20000;    
    private Thread daemonThread;
    private DaemonTaskThread task = new DaemonTaskThread();
    private String timeString;
    protected ContextParameters contextParams;
    protected boolean alive = true, taskComplete = true;
    protected static final Logger LOG = Logger.getLogger(ServletDaemon.class.getName());

    /**
     * Constructor for the web application daemon
     * @param contextParams Context parameters for the daemon
     */
    public ServletDaemon(ContextParameters contextParams) {
        this.contextParams = contextParams;
    }

    /**
     * Placeholder for the task to be performed by the daemon
     * This task (if performed by a remote java class, should be 
     * listen to the alive boolean and shut down gracefully if alive is set to false.
     * When finished or gracefully stopped, this task should set the taskComplete
     * boolean to true.
     */
    abstract void doDaemonTask();

    /**
     * Start the daemon
     */
    public void invokeDaemon() {
        alive = true;
        if (alive) {
            if (running(daemonThread)) {
                return;
            }
            timeString = String.valueOf(System.currentTimeMillis());
            LOG.info("Servlet Daemon (" + timeString + ") started......");
            daemonThread = new Thread(task);
            daemonThread.setDaemon(true);
            daemonThread.start();
        }
    }

    /**
     * Stop the daemon
     */
    public void stopDaemon() {
        LOG.info("Sigval servlet daemon (" + timeString + ") stopping...");
        alive = false;
        // Wait for current task to end gracefully
//        long stopTime = System.currentTimeMillis();
//        while (!taskComplete) {
//            if (System.currentTimeMillis() > (stopTime + MAX_TASK_SHUTDOWN_TIME)) {
//                taskComplete = true;
//                LOG.info("Forced daemon task shutdown");
//            } else {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ex) {
//                }
//            }
//        }
        //Kill task thread
        task.stop();
        //Wait for thread to die
        try {
            daemonThread.join();
            LOG.info("Servlet daemon (" + timeString + ") stopped");
        } catch (InterruptedException ex) {
            LOG.info("join error");
        }
    }

    /**
     * Tests if a thread is running
     * @param thread The thread to be tested
     * @return true if the thread is currently active
     */
    private boolean running(Thread thread) {
        return (thread != null && thread.isAlive());
    }

    /**
     * This class defines the task to be carried out in the daemon thread.
     */
    class DaemonTaskThread implements Runnable {
        Thread actionThread;

        long startTime;

        public DaemonTaskThread() {
        }

        @Override
        public void run() {
            while (alive) {
                taskComplete = false;
                startTime = System.currentTimeMillis();
                invokeDaemonAction();
                taskComplete = true;
                if (alive) {
                    try {
                        Thread.sleep(getSleepTime());
                    } catch (InterruptedException ex) {
                        LOG.info("Sigval daemon idle timer interrupted");
                    }
                }
            }
        }

        public void stop() {
            daemonThread.interrupt();
        }

        private long getSleepTime() {
            long threadSleep = contextParams.getDaemonIdleTime();
            long ct = System.currentTimeMillis();
            long elapsed = ct - startTime;
            if (elapsed > threadSleep) {
                return 0;
            }
            return threadSleep - elapsed;
        }

        private void invokeDaemonAction() {
            if (running(actionThread)) {
                return;
            }
            actionThread = new Thread(new DaemonAction());
            actionThread.setDaemon(true);
            actionThread.start();
            try {
                actionThread.join();
            } catch (InterruptedException ex) {
                LOG.info("Sigval daemon action interrupted");
            }
        }
    }

    class DaemonAction implements Runnable {

        public DaemonAction() {
        }

        @Override
        public void run() {
            doDaemonTask();
        }
    }
}
