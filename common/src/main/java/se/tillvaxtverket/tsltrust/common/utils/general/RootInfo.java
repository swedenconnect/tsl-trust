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
package se.tillvaxtverket.tsltrust.common.utils.general;

import com.aaasec.lib.aaacert.AaaCertificate;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private List<AaaCertificate> rootCerts;
    private boolean initialized = false;
    private Map<String, AaaCertificate> rootMap;
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
        rootCerts = new ArrayList<AaaCertificate>(rootNodes.getLength());
        rootMap = new HashMap<String, AaaCertificate>();
        policyDescMap = new HashMap<String, String>();

        for (int i = 0; i < rootNodes.getLength(); i++) {
            String pName = "", pem = "", desc="";
            Node rootNode = rootNodes.item(i);
            NodeList rootElements = rootNode.getChildNodes();
            for (int nc = 0; nc < rootElements.getLength(); nc++) {
                Node node = rootElements.item(nc);
                pName = (node.getNodeName().equals("tslt:PolicyName") ? node.getTextContent() : pName);
                pem = (node.getNodeName().equals("tslt:RootCertificate") ? node.getTextContent() : pem);
                desc = (node.getNodeName().equals("tslt:PolicyDescription") ? URIComponentCoder.decodeURIComponent(node.getTextContent()) : desc);
            }
            if (pName.length() > 0 && pem.length() > 0) {
                AaaCertificate rootCert = CertificateUtils.getCertificate(pem);
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

    public List<AaaCertificate> getRootCerts() {
        return rootCerts;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Map<String, AaaCertificate> getRootMap() {
        return rootMap;
    }

    public void setRootMap(Map<String, AaaCertificate> rootMap) {
        this.rootMap = rootMap;
    }

    public Map<String, String> getPolicyDescMap() {
        return policyDescMap;
    }
    
}
