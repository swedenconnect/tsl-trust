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

import org.etsi.uri.x02231.v2.AdditionalServiceInformationType;
import org.etsi.uri.x02231.v2.AnyType;
import org.w3c.dom.Node;

/**
 * This class carries information about Additional Service Information - Service
 * Information Extensions.
 */
public class AdditionalServiceInformationSie implements ServiceInfoExtension {

    private Node node;
    private AdditionalServiceInformationType asi;
    boolean critical;

    public AdditionalServiceInformationSie(AdditionalServiceInformationType additionalSi, Node sie, boolean critical) {
        this.node = sie;
        this.critical = critical;
        this.asi = additionalSi;
    }

    @Override
    public SieType getType() {
        return SieType.AdditionalServiceInformation;
    }

    @Override
    public String getInfo() {
        return "Additional Service Information Extension";
    }

    @Override
    public String getName() {
        return "Service Info";
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public boolean isCritical() {
        return critical;
    }

    public AdditionalServiceInformationType getExtension() {
        return asi;
    }

    public String getURI() {
        String data = null;
        try {
            data = asi.getURI().getStringValue();
        } catch (Exception ex) {
        }
        return data;
    }

    public String getInformationValue() {
        return asi.getInformationValue();
    }

    public AnyType getOtherInformation() {
        return asi.getOtherInformation();
    }
}
