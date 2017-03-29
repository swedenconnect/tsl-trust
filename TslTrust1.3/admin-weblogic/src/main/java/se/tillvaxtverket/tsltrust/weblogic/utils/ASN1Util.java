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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import iaik.asn1.ASN;
import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.ObjectID;
import iaik.x509.X509Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;

/**
 * Utility class for ASN.1 related functions
 */
public class ASN1Util {

    private static final Logger LOG = Logger.getLogger(ASN1Util.class.getName());

    public static String getShortCertName(byte[] certBytes) {
        if (certBytes == null) {
            return "Invalid certificate";
        }
        X509Certificate iaikCert = KsCertFactory.getIaikCert(certBytes);
        return getShortCertName(iaikCert);
    }

    public static String getShortCertName(java.security.cert.X509Certificate cert) {
        try {
            return getShortCertName(cert.getEncoded());
        } catch (CertificateEncodingException ex) {
            return "Invalid certificate";
        }
    }

    public static String getShortCertName(X509Certificate iaikCert) {
        if (iaikCert == null) {
            return "Invalid certificate";
        }
        return getShortName(iaikCert.getSubjectX500Principal());
    }

    public static String getShortName(X500Principal dName) {
        Map<ObjectID, String> nameMap = getCertNameAttributes(dName);

        if (nameMap.containsKey(ObjectID.commonName)) {
            return nameMap.get(ObjectID.commonName);
        }
        StringBuilder b = new StringBuilder();
        if (nameMap.containsKey(ObjectID.surName)) {
            b.append(nameMap.get(ObjectID.surName));
        }
        if (nameMap.containsKey(ObjectID.givenName)) {
            b.append(" ").append(nameMap.get(ObjectID.givenName));
        }
        if (b.length() > 0) {
            return b.toString().trim();
        }
        if (nameMap.containsKey(ObjectID.organizationalUnit)) {
            b.append(nameMap.get(ObjectID.organizationalUnit));
        }
        if (nameMap.containsKey(ObjectID.organization)) {
            b.append(" ").append(nameMap.get(ObjectID.organization));
        }

        b.append(b.length() == 0 ? "No displayable name" : "");
        return b.toString().trim();
    }

    public static Map<ObjectID, String> getCertNameAttributes(X500Principal dName) {
        try {
            ASN1 subjectNameAsn1 = new ASN1(dName.getEncoded());
            int rdnCount = subjectNameAsn1.countComponents();
            //System.out.println("Number of RDNs: " + rdnCount);

            List<ASN1Object> attTaVs = new ArrayList<ASN1Object>();
            for (int i = 0; i < rdnCount; i++) {
                ASN1Object rdnSeq = subjectNameAsn1.getComponentAt(i);
                for (int j = 0; j < rdnSeq.countComponents(); j++) {
                    attTaVs.add(rdnSeq.getComponentAt(j));
                }
            }
            Map<ObjectID, String> nameMap = new HashMap<ObjectID, String>();
            for (ASN1Object attTaV : attTaVs) {
                ObjectID oid = new ObjectID((String) attTaV.getComponentAt(0).getValue());
                // Get name object
                Object no = attTaV.getComponentAt(1).getValue();
                String name = "**unknown value type**";
                if (no.getClass().equals(String.class)) {
                    name = (String) no;
                } else {
                    if (no.getClass().equals(ASN1Object.class)) {
                        name = ((ASN1Object) no).toString();
                    }
                }
                //System.out.println(oid.getNameAndID() + "\"" + name + "\"");
                nameMap.put(oid, name);
            }
            return nameMap;


        } catch (CodingException ex) {
            return null;
        }
    }

//    public static Set<Entry<ObjectID, String>> getCertNameAttributeSetOld(X509Certificate cert) {
//        try {
//            ASN1 subjectNameAsn1 = new ASN1(cert.getSubjectX500Principal().getEncoded());
//            int rdnCount = subjectNameAsn1.countComponents();
//            //System.out.println("Number of RDNs: " + rdnCount);
//
//            List<ASN1Object> attTaVs = new ArrayList<ASN1Object>();
//            for (int i = 0; i < rdnCount; i++) {
//                ASN1Object rdnSeq = subjectNameAsn1.getComponentAt(i);
//                for (int j = 0; j < rdnSeq.countComponents(); j++) {
//                    attTaVs.add(rdnSeq.getComponentAt(j));
//                }
//            }
//            Entry<ObjectID, String> entry;
//            Set<Entry<ObjectID, String>> set = new LinkedHashSet<Entry<ObjectID, String>>();
//            for (ASN1Object attTaV : attTaVs) {
//                ObjectID oid = new ObjectID((String) attTaV.getComponentAt(0).getValue());
//                // Get name object
//                Object no = attTaV.getComponentAt(1).getValue();
//                String name = "**unknown value type**";
//                if (no.getClass().equals(String.class)) {
//                    name = (String) no;
//                } else {
//                    if (no.getClass().equals(ASN1Object.class)) {
//                        name = ((ASN1Object) no).toString();
//                    }
//                }
//
//                //System.out.println(oid.getNameAndID() + "\"" + name + "\"");
//                entry = new SimpleEntry<ObjectID, String>(oid, name);
//                set.add(entry);
//            }
//            return set;
//
//
//        } catch (CodingException ex) {
//            return null;
//        }
//    }
    public static Set<Entry<ObjectID, String>> getCertNameAttributeSet(X509Certificate cert) {
        X500Principal distinguishedName = cert.getSubjectX500Principal();
        return getCertNameAttributeSet(distinguishedName);
    }

    public static Set<Entry<ObjectID, String>> getCertNameAttributeSet(X500Principal distinguishedName) {
        try {
            ASN1 subjectNameAsn1 = new ASN1(distinguishedName.getEncoded());
            int rdnCount = subjectNameAsn1.countComponents();
            List<ASN1Object> attTaVs = new ArrayList<ASN1Object>();

            for (int i = 0; i < rdnCount; i++) {
                ASN1Object rdnSeq = subjectNameAsn1.getComponentAt(i);
                for (int j = 0; j < rdnSeq.countComponents(); j++) {
                    attTaVs.add(rdnSeq.getComponentAt(j));
                }
            }
            List<OidNamePair> valuePairs = new ArrayList<OidNamePair>();
            for (ASN1Object attTaV : attTaVs) {
                getNameValue(attTaV, valuePairs);
            }
            Entry<ObjectID, String> entry;
            Set<Entry<ObjectID, String>> set = new LinkedHashSet<Entry<ObjectID, String>>();

            for (OidNamePair valuePair : valuePairs) {
                //System.out.println(oid.getNameAndID() + "\"" + name + "\"");
                entry = new SimpleEntry<ObjectID, String>(valuePair.oid, valuePair.name);
                set.add(entry);
            }
            return reverseOrder(set);

        } catch (CodingException ex) {
            return null;
        }
    }

    private static void getNameValue(ASN1Object attrTypeAndValue, List<OidNamePair> valuePairs) {

        try {
            ObjectID oid = new ObjectID((String) attrTypeAndValue.getComponentAt(0).getValue());
            // Get name object
            ASN1Object nameObject = attrTypeAndValue.getComponentAt(1);
            String name;
            if (oid.equals(ObjectID.postalAddress)) {
                getPostalAddressPairs(nameObject, valuePairs);
            } else {
                if (nameObject.isStringType()) {
                    name = (String) nameObject.getValue();
                    valuePairs.add(new OidNamePair(oid, name));
                }
            }
        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
        }

    }

    private static void getPostalAddressPairs(ASN1Object postalAdrVal, List<OidNamePair> valuePairs) {
        if (postalAdrVal.getAsnType().equals(ASN.SEQUENCE)) {
            List<ASN1Object> nameList = getAsn1Objects(postalAdrVal);
            StringBuilder b = new StringBuilder();
            int i = 0;
            for (ASN1Object nameObj : nameList) {
                if (nameObj.isStringType()) {
                    b.append(nameObj.getValue());
                    if (++i < nameList.size()) {
                        b.append(", ");
                    }
                }
            }
            valuePairs.add(new OidNamePair(ObjectID.postalAddress, b.toString()));
        } else {
            valuePairs.add(new OidNamePair(ObjectID.postalAddress, "** content not decoded **"));
        }
    }

    private static List<ASN1Object> getAsn1Objects(ASN1Object asn1Obj) {
        List<ASN1Object> asn1ObjList = new ArrayList<ASN1Object>();
        try {
            for (int i = 0; i < asn1Obj.countComponents(); i++) {
                asn1ObjList.add(asn1Obj.getComponentAt(i));
            }
        } catch (CodingException ex) {
            LOG.warning(ex.getMessage());
        }
        return asn1ObjList;
    }

    private static Set<Entry<ObjectID, String>> reverseOrder(Set<Entry<ObjectID, String>> set) {
        Set<Entry<ObjectID, String>> reverse = new LinkedHashSet<Entry<ObjectID, String>>();
        Object[] o = new Object[set.size()];

        Iterator itr = set.iterator();
        int i = 0;
        while (itr.hasNext()) {
            o[i++] = itr.next();
        }
        for (i = o.length; i > 0; i--) {
            reverse.add((Entry<ObjectID, String>) o[i - 1]);
        }
        return reverse;
    }

    static class OidNamePair {

        ObjectID oid;
        String name;

        public OidNamePair(ObjectID oid, String name) {
            this.oid = oid;
            this.name = name;
        }
    }
}