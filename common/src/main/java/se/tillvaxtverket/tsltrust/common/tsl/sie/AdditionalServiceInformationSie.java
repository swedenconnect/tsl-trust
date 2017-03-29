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
