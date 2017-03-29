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

import java.util.Observable;
import java.util.Observer;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * Providing an observable close action of another frame
 * This class is instantiated and called from the closing frame.
 */
public final class ObservableFrameCloser extends Observable implements ObserverConstants {

    private Object closingFrame;
    private Observer observer;
    private boolean closed = false;

    /**
     * Constructor for the observable frame closing action
     * @param closingFrame The frame to be closed. Either a JFrame or a JInternalFrame
     * @param observer  The observer receiving the notification. The Observing class must implement the OBserver interface.
     */
    public ObservableFrameCloser(Object closingFrame, Observer observer) {
        this.closingFrame = closingFrame;
        this.observer = observer;
        registerObserver();
    }

    private void registerObserver() {
        addObserver(observer);
    }

    public void close(Object notification) {
        if (!closed) {
            if (closeFrame()) {
                setChanged();
                notifyObservers(notification);
                closed = true;
            }
        }
    }

    private boolean closeFrame() {
        if (closingFrame instanceof JInternalFrame) {
            JInternalFrame frame = (JInternalFrame) closingFrame;
            frame.doDefaultCloseAction();
            return true;
        }
        if (closingFrame instanceof JFrame) {
            JFrame frame = (JFrame) closingFrame;
            frame.dispose();
            return true;
        }
        return false;
    }
}
