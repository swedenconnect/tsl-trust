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
package se.tillvaxtverket.tsltrust.common.tsl.sie;

import org.w3c.dom.Node;

/**
 * Class storing information about unknown Service Information Extensions
 */
public class UnknownSie implements ServiceInfoExtension {

    public Node node;
    boolean critical;

    public UnknownSie(Node node) {
        this.node = node;
    }

    @Override
    public SieType getType() {
        return SieType.UNKNOWN;
    }

    @Override
    public String getInfo() {
        return "Unknown Service Information Extension";
    }

    @Override
    public String getName() {
        return "Unknown";
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public boolean isCritical() {
        return critical;
    }
}
