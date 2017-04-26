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

import com.aaasec.lib.aaacert.AaaCertificate;
import com.aaasec.lib.aaacert.data.SubjectAttributeInfo;
import com.aaasec.lib.aaacert.enums.SubjectDnType;
import com.aaasec.lib.aaacert.utils.CertUtils;
import java.util.Map;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;

/**
 * Utility class for ASN.1 related functions
 */
public class ASN1Util {

    private static final Logger LOG = Logger.getLogger(ASN1Util.class.getName());

    public static String getShortCertName(byte[] certBytes) {
        try {
            AaaCertificate aCert = new AaaCertificate(certBytes);
            return getShortCertName(aCert);
            
        } catch (Exception e) {
            return "Invalid certificate";
        }
    }

    public static String getShortCertName(AaaCertificate cert) {
        if (cert == null) {
            return "Invalid certificate";
        }
        return getShortName(cert.getSubjectX500Principal());
    }

    public static String getShortName(X500Principal dName) {
        
        Map<SubjectDnType, SubjectAttributeInfo> nameMap = CertUtils.getSubjectDnAttributeMap(CertUtils.getAttributeInfoList(dName));
        
        if (nameMap.containsKey(SubjectDnType.cn)) {
            return nameMap.get(SubjectDnType.cn).getValue();
        }
        StringBuilder b = new StringBuilder();
        if (nameMap.containsKey(SubjectDnType.surname)) {
            b.append(nameMap.get(SubjectDnType.surname).getValue());
        }
        if (nameMap.containsKey(SubjectDnType.givenName)) {
            b.append(" ").append(nameMap.get(SubjectDnType.givenName).getValue());
        }
        if (b.length() > 0) {
            return b.toString().trim();
        }
        if (nameMap.containsKey(SubjectDnType.orgnaizationalUnitName)) {
            b.append(nameMap.get(SubjectDnType.orgnaizationalUnitName).getValue());
        }
        if (nameMap.containsKey(SubjectDnType.orgnaizationName)) {
            b.append(" ").append(nameMap.get(SubjectDnType.orgnaizationName).getValue());
        }

        b.append(b.length() == 0 ? "No displayable name" : "");
        return b.toString().trim();
    }

//    public static Map<ObjectID, String> getCertNameAttributes(X500Principal dName) {
//        try {
//            ASN1 subjectNameAsn1 = new ASN1(dName.getEncoded());
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
//            Map<ObjectID, String> nameMap = new HashMap<ObjectID, String>();
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
//                //System.out.println(oid.getNameAndID() + "\"" + name + "\"");
//                nameMap.put(oid, name);
//            }
//            return nameMap;
//
//
//        } catch (CodingException ex) {
//            return null;
//        }
//    }

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
//    public static Set<Entry<ObjectID, String>> getCertNameAttributeSet(X509Certificate cert) {
//        X500Principal distinguishedName = cert.getSubjectX500Principal();
//        return getCertNameAttributeSet(distinguishedName);
//    }
//
//    public static Set<Entry<ObjectID, String>> getCertNameAttributeSet(X500Principal distinguishedName) {
//        try {
//            ASN1 subjectNameAsn1 = new ASN1(distinguishedName.getEncoded());
//            int rdnCount = subjectNameAsn1.countComponents();
//            List<ASN1Object> attTaVs = new ArrayList<ASN1Object>();
//
//            for (int i = 0; i < rdnCount; i++) {
//                ASN1Object rdnSeq = subjectNameAsn1.getComponentAt(i);
//                for (int j = 0; j < rdnSeq.countComponents(); j++) {
//                    attTaVs.add(rdnSeq.getComponentAt(j));
//                }
//            }
//            List<OidNamePair> valuePairs = new ArrayList<OidNamePair>();
//            for (ASN1Object attTaV : attTaVs) {
//                getNameValue(attTaV, valuePairs);
//            }
//            Entry<ObjectID, String> entry;
//            Set<Entry<ObjectID, String>> set = new LinkedHashSet<Entry<ObjectID, String>>();
//
//            for (OidNamePair valuePair : valuePairs) {
//                //System.out.println(oid.getNameAndID() + "\"" + name + "\"");
//                entry = new SimpleEntry<ObjectID, String>(valuePair.oid, valuePair.name);
//                set.add(entry);
//            }
//            return reverseOrder(set);
//
//        } catch (CodingException ex) {
//            return null;
//        }
//    }
//
//    private static void getNameValue(ASN1Object attrTypeAndValue, List<OidNamePair> valuePairs) {
//
//        try {
//            ObjectID oid = new ObjectID((String) attrTypeAndValue.getComponentAt(0).getValue());
//            // Get name object
//            ASN1Object nameObject = attrTypeAndValue.getComponentAt(1);
//            String name;
//            if (oid.equals(ObjectID.postalAddress)) {
//                getPostalAddressPairs(nameObject, valuePairs);
//            } else {
//                if (nameObject.isStringType()) {
//                    name = (String) nameObject.getValue();
//                    valuePairs.add(new OidNamePair(oid, name));
//                }
//            }
//        } catch (Exception ex) {
//            LOG.warning(ex.getMessage());
//        }
//
//    }
//
//    private static void getPostalAddressPairs(ASN1Object postalAdrVal, List<OidNamePair> valuePairs) {
//        if (postalAdrVal.getAsnType().equals(ASN.SEQUENCE)) {
//            List<ASN1Object> nameList = getAsn1Objects(postalAdrVal);
//            StringBuilder b = new StringBuilder();
//            int i = 0;
//            for (ASN1Object nameObj : nameList) {
//                if (nameObj.isStringType()) {
//                    b.append(nameObj.getValue());
//                    if (++i < nameList.size()) {
//                        b.append(", ");
//                    }
//                }
//            }
//            valuePairs.add(new OidNamePair(ObjectID.postalAddress, b.toString()));
//        } else {
//            valuePairs.add(new OidNamePair(ObjectID.postalAddress, "** content not decoded **"));
//        }
//    }
//
//    private static List<ASN1Object> getAsn1Objects(ASN1Object asn1Obj) {
//        List<ASN1Object> asn1ObjList = new ArrayList<ASN1Object>();
//        try {
//            for (int i = 0; i < asn1Obj.countComponents(); i++) {
//                asn1ObjList.add(asn1Obj.getComponentAt(i));
//            }
//        } catch (CodingException ex) {
//            LOG.warning(ex.getMessage());
//        }
//        return asn1ObjList;
//    }
//
//    private static Set<Entry<ObjectID, String>> reverseOrder(Set<Entry<ObjectID, String>> set) {
//        Set<Entry<ObjectID, String>> reverse = new LinkedHashSet<Entry<ObjectID, String>>();
//        Object[] o = new Object[set.size()];
//
//        Iterator itr = set.iterator();
//        int i = 0;
//        while (itr.hasNext()) {
//            o[i++] = itr.next();
//        }
//        for (i = o.length; i > 0; i--) {
//            reverse.add((Entry<ObjectID, String>) o[i - 1]);
//        }
//        return reverse;
//    }
//
//    static class OidNamePair {
//
//        ObjectID oid;
//        String name;
//
//        public OidNamePair(ObjectID oid, String name) {
//            this.oid = oid;
//            this.name = name;
//        }
//    }
}