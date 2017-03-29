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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify;

import iaik.asn1.ASN;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.OCSPVerifyContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.CertVerifyContext;
import se.tillvaxtverket.tsltrust.common.utils.general.ObjectTree;
import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.ObjectID;
import iaik.x509.X509Certificate;
import iaik.x509.ocsp.OCSPResponse;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import javax.swing.tree.DefaultMutableTreeNode;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.CollectionStore;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.SignatureValidationContext;

/**
 * Class relating multiple data objects to a tree structure.
 * This class is likely to be replaced in future updates of TSL Trust and replaced
 * with trees holding single consolidated objects.
 */
public class TreeUtil {

    private static ObjectTree objTree;
    private static String rootKey;

    public static ObjectTree getTreeNodes(List<SignatureValidationContext> pdfContList, ObjectTree objectTree, boolean orgPath, boolean justCerts) {
        if (pdfContList.isEmpty()) {
            return objectTree;
        }

        if (orgPath) {
            return getOrgPathTreeNodes(pdfContList, objectTree);
        }

        X509Certificate root = getRoot(pdfContList);
        if (root == null) {
            return objectTree;
        }

        //Set root node
        rootKey = root.getSubjectDN().getName();
        objTree = new ObjectTree(getTreeName(root), rootKey, root);

        //Parese pdf signatures
        for (SignatureValidationContext pdfContext : pdfContList) {
            boolean timeStamped = (pdfContext.getTstContext() != null);
            List<CertVerifyContext> empty = new ArrayList<CertVerifyContext>();
            try {
                List<CertVerifyContext> sigContexts = getCertContexts(pdfContext.getSignCertValidation());
                List<CertVerifyContext> tsContexts = timeStamped ? getCertContexts(pdfContext.getTstContext().getCertVerifyContext()) : empty;
                List<CertVerifyContext> ocspContexts = getOcspCertContexts(sigContexts, tsContexts);

                initNodes(pdfContext.getSignCertValidation());
                if (timeStamped) {
                    initNodes(pdfContext.getTstContext().getCertVerifyContext());
                }
                initNodes(ocspContexts);

                //Set final objects
                setObjects(sigContexts, justCerts);
                setObjects(tsContexts, justCerts);
                setObjects(ocspContexts, justCerts);

                if (!justCerts) {
                    //Set signature node
                    String parentKey = pdfContext.getSignCertValidation().getChain().get(0).getSubjectDN().getName();
                    String nameKey = "Signature (" + pdfContext.getSignatureName() + ")";
                    objTree.addChildNode(parentKey, nameKey, nameKey, pdfContext);

                    //Set timestamp token
                    nameKey = "TimeStamp (" + pdfContext.getSignatureName() + ")";
                    parentKey = pdfContext.getTstContext().getCertVerifyContext().getChain().get(0).getSubjectDN().getName();
                    objTree.addChildNode(parentKey, nameKey, nameKey, pdfContext.getTstContext());
                }
            } catch (Exception ex) {
            }
        }
        return objTree;
    }

    private static ObjectTree getOrgPathTreeNodes(List<SignatureValidationContext> pdfContList, ObjectTree objectTree) {
        List<X509Certificate> chain = pdfContList.get(0).getProvidedChain();
        TimeStampToken tst;
        if (chain.isEmpty()) {
            return objectTree;
        }
        X509Certificate root = chain.get(chain.size() - 1);
        //Set root node
        rootKey = "root";
        objTree = new ObjectTree("root", "root", "root");
        for (SignatureValidationContext pdfContext : pdfContList) {
            chain = pdfContext.getProvidedChain();
            if (!chain.isEmpty()) {
                X509Certificate cert = chain.get(chain.size() - 1);
                objTree.addChildNode("root", cert.getSubjectDN().getName(), getTreeName(cert), cert);
            }
            if (pdfContext.isTimestamped()) {
                tst = pdfContext.getTstContext().getTimeStampToken();
                chain = getTimeStampChain(tst);
                if (!chain.isEmpty()) {
                    X509Certificate cert = chain.get(chain.size() - 1);
                    objTree.addChildNode("root", cert.getSubjectDN().getName(), getTreeName(cert), cert);
                }
            }
        }

        for (SignatureValidationContext pdfContext : pdfContList) {
            chain = pdfContext.getProvidedChain();
            initNodesFromCertList(chain);

            if (pdfContext.isTimestamped()) {
                tst = pdfContext.getTstContext().getTimeStampToken();
                chain = getTimeStampChain(tst);
                initNodesFromCertList(chain);
            }
        }

        return objTree;
    }

    private static List<X509Certificate> getTimeStampChain(TimeStampToken tst) {
        try {
            CollectionStore certStore = (CollectionStore) tst.getCertificates();
            ArrayList<X509CertificateHolder> tsCertList = orderCertList((ArrayList<X509CertificateHolder>) certStore.getMatches(null));
            LinkedList<X509Certificate> chain = new LinkedList<X509Certificate>();
            for (X509CertificateHolder certHolder : tsCertList) {
                try {
                    chain.add(KsCertFactory.getIaikCert(certHolder.getEncoded()));
                } catch (IOException ex) {
                }
            }
            return chain;
        } catch (NullPointerException ex) {
            return new ArrayList<X509Certificate>();
        }
    }

    private static void initNodes(List<CertVerifyContext> contexts) {
        for (CertVerifyContext context : contexts) {
            initNodes(context);
        }
    }

    private static void initNodes(CertVerifyContext certContext) {
        DefaultMutableTreeNode target;
        DefaultMutableTreeNode related;
        List<X509Certificate> chain = certContext.getChain();
        initNodesFromCertList(chain);
    }

    private static void initNodesFromCertList(List<X509Certificate> chain) {
        int size = chain.size();
        if (size > 1) {
            for (int i = size - 2; i > -1; i--) {
                X509Certificate cert = chain.get(i);
                String parentKey = chain.get(i + 1).getSubjectDN().getName();
                objTree.addChildNode(parentKey, cert.getSubjectDN().getName(), getTreeName(cert), cert);
            }
        }
    }

    private static void setObjects(List<CertVerifyContext> contexts, boolean justCerts) {
        for (CertVerifyContext cont : contexts) {
            //Set CertChainContext info
            if (cont.getChain() != null && cont.getChain().size() > 0) {
                String key = cont.getChain().get(0).getSubjectDN().getName();
                objTree.updateNodeObject(key, cont);
                //Add CRL info
                if (cont.getCrlKeys() != null && !justCerts) {
                    String parentKey = cont.getChain().get(1).getSubjectDN().getName();
                    List<String> crlKeys = cont.getCrlKeys();
                    for (String crlKey : crlKeys) {
                        objTree.addChildNode(parentKey, crlKey, "CRL", crlKey);
                    }
                }
            }
            // Add OCSP info
            if (cont.getOcspVerifyContext() != null && !justCerts) {
                OCSPVerifyContext ocspCont = cont.getOcspVerifyContext();
                if (ocspCont.getCertVerifyContxt() != null) {
                    List<X509Certificate> chain = ocspCont.getCertVerifyContxt().getChain();
                    if (chain != null && chain.size() > 0) {
                        String parentkey = ocspCont.getCertVerifyContxt().getChain().get(0).getSubjectDN().getName();
                        OCSPResponse response = ocspCont.getResponse();
                        if (response != null) {
                            String key = FnvHash.getFNV1aToHex(response.getEncoded());
                            objTree.addChildNode(parentkey, key, "OCSP Response", ocspCont);
                        }

                    }
                }
            }
        }
    }

    private static List<CertVerifyContext> getOcspCertContexts(List<CertVerifyContext> sigContexts, List<CertVerifyContext> tsContexts) {
        List<CertVerifyContext> ocspContexts = new LinkedList<CertVerifyContext>();
        CertVerifyContext ocspCont;
        for (CertVerifyContext cont : sigContexts) {
            try {
                ocspCont = cont.getOcspVerifyContext().getCertVerifyContxt();
                if (ocspCont != null) {
                    ocspContexts.add(ocspCont);
                    while (ocspCont.getIssuingCertContext() != null) {
                        ocspCont = ocspCont.getIssuingCertContext();
                        ocspContexts.add(ocspCont);
                    }
                }
            } catch (NullPointerException ex) {
            }
        }

        for (CertVerifyContext cont : tsContexts) {
            try {
                ocspCont = cont.getOcspVerifyContext().getCertVerifyContxt();
                if (ocspCont != null) {
                    ocspContexts.add(ocspCont);
                    while (ocspCont.getIssuingCertContext() != null) {
                        ocspCont = ocspCont.getIssuingCertContext();
                        ocspContexts.add(ocspCont);
                    }
                }
            } catch (NullPointerException ex) {
            }
        }
        return ocspContexts;
    }

    private static List<CertVerifyContext> getCertContexts(CertVerifyContext certContext) {
        List<CertVerifyContext> contexts = new LinkedList<CertVerifyContext>();
        contexts.add(certContext);
        CertVerifyContext issuingContext = certContext.getIssuingCertContext();
        while (issuingContext != null) {
            contexts.add(issuingContext);
            issuingContext = issuingContext.getIssuingCertContext();
        }
        return contexts;
    }

    private static X509Certificate getRoot(List<SignatureValidationContext> pdfContexts) {
        for (SignatureValidationContext pdfContext : pdfContexts) {
            try {
                List<X509Certificate> chain = pdfContext.getSignCertValidation().getChain();
                return chain.get(chain.size() - 1);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public static String getTreeName(X509Certificate cert) {
        Map<ObjectID, String> nameMap = getCertNameAttributes(cert);

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

    public static Map<ObjectID, String> getCertNameAttributes(X509Certificate cert) {
        try {
            ASN1 subjectNameAsn1 = new ASN1(cert.getSubjectX500Principal().getEncoded());
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

    public static Set<Entry<ObjectID, String>> getCertNameAttributeSetOld(X509Certificate cert) {
        try {
            ASN1 subjectNameAsn1 = new ASN1(cert.getSubjectX500Principal().getEncoded());
            int rdnCount = subjectNameAsn1.countComponents();
            //System.out.println("Number of RDNs: " + rdnCount);

            List<ASN1Object> attTaVs = new ArrayList<ASN1Object>();
            for (int i = 0; i < rdnCount; i++) {
                ASN1Object rdnSeq = subjectNameAsn1.getComponentAt(i);
                for (int j = 0; j < rdnSeq.countComponents(); j++) {
                    attTaVs.add(rdnSeq.getComponentAt(j));
                }
            }
            Entry<ObjectID, String> entry;
            Set<Entry<ObjectID, String>> set = new LinkedHashSet<Entry<ObjectID, String>>();
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
                entry = new SimpleEntry<ObjectID, String>(oid, name);
                set.add(entry);
            }
            return set;


        } catch (CodingException ex) {
            return null;
        }
    }

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
            ex.printStackTrace();
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
            ex.printStackTrace();
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

    public static ArrayList<X509CertificateHolder> orderCertList(ArrayList<X509CertificateHolder> certList) {
        ArrayList<X509CertificateHolder> orderdList = new ArrayList<X509CertificateHolder>();
        ArrayList<X509CertificateHolder> sortList = new ArrayList<X509CertificateHolder>();

        // if only one cert or less. return 
        if (certList == null) {
            return orderdList;
        }
        if (certList.size() < 2) {
            return certList;
        }

        // find root
        boolean rootFound = false;
        for (X509CertificateHolder certHolder : certList) {
            if (certHolder.getSubject().equals(certHolder.getIssuer())) {
                sortList.add(certHolder);
                rootFound = true;
                break;
            }
        }
        if (!rootFound) {
            return certList;
        }

        boolean foundNew = true;
        X509CertificateHolder parent = sortList.get(0);
        while (foundNew) {
            foundNew = false;
            for (X509CertificateHolder certHolder : certList) {
                if (parent.getSubject().equals(certHolder.getIssuer())) {
                    sortList.add(certHolder);
                    parent = certHolder;
                    foundNew = true;
                }
            }
        }
        for (int i = sortList.size(); i > 0; i--) {
            orderdList.add(sortList.get(i - 1));
        }
        return orderdList;
    }
}