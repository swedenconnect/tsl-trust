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
package se.tillvaxtverket.tsltrust.common.iaik;

import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.IA5String;
import iaik.asn1.ObjectID;
import iaik.asn1.PrintableString;
import iaik.asn1.SEQUENCE;
import iaik.x509.extensions.qualified.structures.QCStatementInfo;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Implements the PKI Discosure Statement QCStatement.
 * <p>
 * This class extends
 * the {@link iaik.x509.extensions.qualified.structures.QCStatementInfo QCStatementInfo}
 * class. For using this private statement it has to be {@link 
 * iaik.x509.extensions.qualified.structures.QCStatement#register(ObjectID, Class)
 * registered} within the QCStatement framework:
 * <pre>
 * QCStatement.register(MyPrivateQCStatement.statementID, MyPrivateQCStatement.class);
 * </pre>
 * <p>
 * The ASN.1 structure of the statement info belonging to this QCStatement
 * is defined in EN 310 862:
 * <pre>
 *  QcEuPDS ::= PdsLocations
 *
 *  PdsLocations ::= SEQUENCE SIZE (1..MAX) OF PdsLocation
 *
 *  PdsLocation::= SEQUENCE {
 *      url        IA5String,
 *      language   PrintableString (SIZE(2))} -- ISO 639-1 language code
 *
 * </pre>
 * @version File Revision <!-- $$Revision: --> 8 <!-- $ -->
 */
public class PdsQCStatement extends QCStatementInfo {

    /**
     * The statement id for this private QC statement.
     */
    public static final ObjectID statementID = new ObjectID("0.4.0.1862.1.5", "PdsQCStatement");
    HashMap<String, String> pdsURLs;

    /**
     * Default constructor
     */
    public PdsQCStatement() {
    }

    /**
     * Creates a PDS QC Statement
     * @param pdsMap a HashMap using a 2 character ISO 639-1 language code as
     * key to find the url to the PDS provided in the specified language.
     */
    public PdsQCStatement(HashMap<String, String> pdsMap) {
        this.pdsURLs = pdsMap;

    }

    /**
     * Returns a string representation of the statement info
     *
     * @return a string representation of the statement info
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        Iterator<String> iterator = pdsURLs.keySet().iterator();
        while (iterator.hasNext()) {
            String lang = iterator.next();
            String url = pdsURLs.get(lang);
            b.append("PDS ").append(lang).append(": ");
            b.append(url).append("\n");
        }
        return b.toString();
    }

    /**
     * Returns an ASN.1 representation of this statement info.
     *
     * @return this statement info as ASN1Object
     */
    @Override
    public ASN1Object toASN1Object() throws CodingException {

        SEQUENCE pdsQcSeq = new SEQUENCE();

        Iterator<String> iterator = pdsURLs.keySet().iterator();
        while (iterator.hasNext()) {
            String lang = iterator.next();
            String url = pdsURLs.get(lang);
            //Create pds url info sequence
            SEQUENCE pdsSeq = new SEQUENCE();
            pdsSeq.addComponent(new IA5String(url));
            pdsSeq.addComponent(new PrintableString(lang));
            //Add to PdsQCStatements sequence
            pdsQcSeq.addComponent(pdsSeq);
        }
        return pdsQcSeq;

    }

    /**
     * Returns the statement ID.
     *
     * @return the statement id
     */
    @Override
    public ObjectID getStatementID() {
        return statementID;
    }

    /**
     * Decodes the statement info.
     *
     * @param asno the statement info as ASN1Object
     */
    @Override
    public void decode(ASN1Object asno) throws CodingException {
        HashMap<String, String> pdsMap = new HashMap<String, String>();
        int count = asno.countComponents();
        for (int i = 0; i < count; i++) {
            ASN1Object pdsUrlSeq = asno.getComponentAt(i);
            String url = (String) pdsUrlSeq.getComponentAt(0).getValue();
            String lang = (String) pdsUrlSeq.getComponentAt(1).getValue();
            pdsMap.put(lang, url);
        }
        pdsURLs = pdsMap;
    }

    /**
     * Gets the statement data.
     * 
     * @return the statement message
     */
    public HashMap<String,String> getPdsURLs() {
        return pdsURLs;
    }

}
