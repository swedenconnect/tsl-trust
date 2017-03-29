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
