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
