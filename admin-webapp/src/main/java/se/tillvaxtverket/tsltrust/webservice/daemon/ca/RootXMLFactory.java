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
