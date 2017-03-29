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
package se.tillvaxtverket.tsltrust.common.xmldsig;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * Key info selector for extracting the Certificates from an XML signature
 */
public class KeyInfoKeySelector extends KeySelector implements
        KeySelectorResult {

    private static final Logger LOG = Logger.getLogger(KeyInfoKeySelector.class.getName());
    private X509Certificate certificate = null;
    private List<X509Certificate> certList = new ArrayList<X509Certificate>();

    @Override
    public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
            AlgorithmMethod method, XMLCryptoContext context)
            throws KeySelectorException {
        List<XMLStructure> keyInfoContent = keyInfo.getContent();
        for (XMLStructure keyInfoStructure : keyInfoContent) {
            if (false == (keyInfoStructure instanceof X509Data)) {
                continue;
            }
            X509Data x509Data = (X509Data) keyInfoStructure;
            List<Object> x509DataList = x509Data.getContent();
            for (Object x509DataObject : x509DataList) {
                if (false == (x509DataObject instanceof X509Certificate)) {
                    continue;
                }
                if (certificate == null) {
                    certificate = (X509Certificate) x509DataObject;
                }
                certList.add((X509Certificate) x509DataObject);
            }
        }
        if (certificate != null) {
            return this;
        }
        throw new KeySelectorException("No key found!");
    }

    @Override
    public Key getKey() {
        return this.certificate.getPublicKey();
    }

    public X509Certificate getCertificate() {
        return this.certificate;
    }

    public List<X509Certificate> getCertificates() {
        return this.certList;
    }
}
