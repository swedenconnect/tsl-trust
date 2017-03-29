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
