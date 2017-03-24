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

import java.util.Date;
import org.etsi.uri.x02231.v2.ExpiredCertsRevocationInfoDocument;
import org.w3c.dom.Node;

/**
 * Class holding data about a Taken Over By Extension
 */
public class ExpiredCertsRevocationInfoSie implements ServiceInfoExtension {

    private Node node;
    private ExpiredCertsRevocationInfoDocument expCertInfoDoc;
    boolean critical;

    /**
     * Constructor
     * @param expCertInfoDoc The extension element
     * @param sie The extension XML Node
     * @param critical  true if the extension is critical, otherwise false.
     */
    public ExpiredCertsRevocationInfoSie(ExpiredCertsRevocationInfoDocument expCertInfoDoc, Node sie, boolean critical) {
        this.node = sie;
        this.critical = critical;
        this.expCertInfoDoc = expCertInfoDoc;
    }

    @Override
    public SieType getType() {
        return SieType.ExpiredCertsRevocationInfo;
    }

    @Override
    public String getInfo() {
        return "Expired Certificates Revocation Extension";
    }

    @Override
    public String getName() {
        return "Expired Certs";
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public boolean isCritical() {
        return critical;
    }

    /**
     * Getter for the extension element information
     * @return extension element
     */
    public Date getExpiredCertsRevDate() {
        return expCertInfoDoc.getExpiredCertsRevocationInfo().getTime();
    }
}
