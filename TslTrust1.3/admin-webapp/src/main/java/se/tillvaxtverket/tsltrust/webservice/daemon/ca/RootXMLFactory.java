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
package se.tillvaxtverket.tsltrust.webservice.daemon.ca;

import se.tillvaxtverket.tsltrust.common.utils.core.PEM;
import se.tillvaxtverket.tsltrust.common.utils.general.URIComponentCoder;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import iaik.x509.X509Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Factory class for creation of rootlist.xml files for export of trust information
 */
public class RootXMLFactory {
    private static final Logger LOG = Logger.getLogger(RootXMLFactory.class.getName());
    public static String generateRootInfo(List<CertificationAuthority> caList, List<ValidationPolicy> vpList) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document doc = impl.createDocument(null, null, null);
            doc.setXmlStandalone(true);
            Element e1 = doc.createElement("tslt:TSLTrustRootList");
            e1.setAttribute("xmlns:tslt", "http://3xasecurity.com/2011/TSLTrust");
            doc.appendChild(e1);

            for (CertificationAuthority ca : caList) {

                try {
                    X509Certificate cert = getRoot(ca);
                    Element root = doc.createElement("tslt:Root");
                    e1.appendChild(root);
                    Element rootName = doc.createElement("tslt:PolicyName");
                    rootName.setTextContent(ca.getCaName());
                    root.appendChild(rootName);
                    Element policyDescription = doc.createElement("tslt:PolicyDescription");
                    policyDescription.setTextContent(URIComponentCoder.encodeURIComponent(getPolicyDescription(ca,vpList)));
                    root.appendChild(policyDescription);
                    Element rootCert = doc.createElement("tslt:RootCertificate");                    
                    String pemCert = PEM.getPemCert(cert.getEncoded());
                    rootCert.setTextContent(PEM.trimPemCert(pemCert).trim());
                    root.appendChild(rootCert);

                } catch (CertificateEncodingException ex) {
                    Logger.getLogger(RootXMLFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //System.out.println("The new XML document:\n" + getDocText(doc));
            return getDocText(doc);
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return "No XML Created";
    }

    public static String getDocText(Document doc) {
        DOMSource domSource = new DOMSource(doc);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            //transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            String xml = sw.toString();
            return xml;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return "";
    }

    private static X509Certificate getRoot(CertificationAuthority ca) {
        if (ca.initKeyStore()) {
            return ca.getSelfSignedCert();
        }
        return null;
    }

    private static String getPolicyDescription(CertificationAuthority ca, List<ValidationPolicy> vpList) {
        String name = ca.getCaName();
        String description = "";
        for (ValidationPolicy vp:vpList){
            if (vp.getPolicyName().equals(name)){
                description = vp.getDescription();
            }
        }
        return description;
    }
}
