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
package se.tillvaxtverket.tsltrust.common.utils.general;

import iaik.utils.URLDecoder;
import iaik.x509.X509Certificate;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for extracting data from and creating the rootlist.xml file for export of trust root information
 */
public final class RootInfo {

    private static final Logger LOG = Logger.getLogger(RootInfo.class.getName());
    private Document doc;
    private List<String> caNames;
    private List<X509Certificate> rootCerts;
    private boolean initialized = false;
    private Map<String, X509Certificate> rootMap;
    private Map<String, String> policyDescMap;

    public RootInfo(byte[] xmlData) {
        start(xmlData);
    }

    public RootInfo(File xmlFile) {
        byte[] xmlData = FileOps.readBinaryFile(xmlFile);
        start(xmlData);
    }

    public void start(byte[] xmlData) {
        try {
            InputStream is = new ByteArrayInputStream(xmlData);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            parseXML();

        } catch (Exception ex) {
            LOG.warning("Unable to find or parse Root information file, casued by :" + ex.getMessage());
        }

    }

    private void parseXML() {
        NodeList rootNodes = doc.getElementsByTagName("tslt:Root");
        caNames = new ArrayList<String>(rootNodes.getLength());
        rootCerts = new ArrayList<X509Certificate>(rootNodes.getLength());
        rootMap = new HashMap<String, X509Certificate>();
        policyDescMap = new HashMap<String, String>();

        for (int i = 0; i < rootNodes.getLength(); i++) {
            String pName = "", pem = "", desc="";
            Node rootNode = rootNodes.item(i);
            NodeList rootElements = rootNode.getChildNodes();
            for (int nc = 0; nc < rootElements.getLength(); nc++) {
                Node node = rootElements.item(nc);
                pName = (node.getNodeName().equals("tslt:PolicyName") ? node.getTextContent() : pName);
                pem = (node.getNodeName().equals("tslt:RootCertificate") ? node.getTextContent() : pem);
                try {
                    desc = (node.getNodeName().equals("tslt:PolicyDescription") ? URLDecoder.decode(node.getTextContent()) : desc);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            if (pName.length() > 0 && pem.length() > 0) {
                X509Certificate rootCert = CertificateUtils.getCertificate(pem);
                if (rootCert != null) {
                    caNames.add(pName);
                    rootCerts.add(rootCert);
                    rootMap.put(pName, rootCert);
                    policyDescMap.put(pName, desc);
                    initialized = true;
                }

            }

        }
    }

    public List<String> getCaNames() {
        return caNames;
    }

    public List<X509Certificate> getRootCerts() {
        return rootCerts;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Map<String, X509Certificate> getRootMap() {
        return rootMap;
    }

    public void setRootMap(Map<String, X509Certificate> rootMap) {
        this.rootMap = rootMap;
    }

    public Map<String, String> getPolicyDescMap() {
        return policyDescMap;
    }
    
}
