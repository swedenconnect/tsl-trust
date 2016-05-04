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
package se.tillvaxtverket.tsltrust.admin.maint.config;

import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for importing and exporting data to a web.xml file
 */
public final class WebXmlData {

    private Document doc;
    private List<String> parameterList;
    private String selectedServlet = "HtmlProvider";
    private boolean initialized = false;
    private Map<String, Node> parameterNodes;
    private Map<String, List<String>> parameterValues;
    private File xmlFile;

    public WebXmlData(File xmlFile) {
        this.xmlFile = xmlFile;
        byte[] xmlData = FileOps.readBinaryFile(xmlFile);
        start(xmlData);
    }

    private void start(byte[] xmlData) {
        try {
            InputStream is = new ByteArrayInputStream(xmlData);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            testXML();

        } catch (Exception ex) {
            Logger.getLogger(WebXmlData.class.getName()).log(Level.WARNING, null, ex);
        }

    }

    private void testXML() {
        initialized = false;
        parameterList = new ArrayList<String>();
        parameterNodes = new HashMap<String, Node>();
        parameterValues = new HashMap<String, List<String>>();
        try {
            NodeList rootNodes = doc.getChildNodes();
            List<Node> waNodes = getNodeListByName(rootNodes, "web-app");
            for (Node node : waNodes) {
                NodeList waChildren = node.getChildNodes();
                List<Node> slNodes = getNodeListByName(waChildren, "servlet");
                for (Node slNode : slNodes) {
                    if (getServletName(slNode).equals(selectedServlet)) {
                        parseInitParamNodes(slNode.getChildNodes());
                    }
                }
                List<Node> contNodes = getNodeListByName(waChildren, "context-param");
                parseContextParamNodes(contNodes);
            }
        } catch (Exception ex) {
        }
    }

    private List<Node> getNodeListByName(NodeList nodes, String nodeName) {
        List<Node> nodeList = new ArrayList<Node>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = node.getNodeName();
            if (name.equals(nodeName)) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    private String getServletName(Node servletNode) {
        NodeList nodes = servletNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = node.getNodeName();
            if (name.equals("servlet-name")) {
                return node.getTextContent();
            }
        }
        return "";
    }

    private void parseInitParamNodes(NodeList servletNodes) {
        try {
            for (int i = 0; i < servletNodes.getLength(); i++) {
                Node parmNode = servletNodes.item(i);
                if (parmNode.getNodeName().equalsIgnoreCase("init-param")) {
                    List<String> parameter = new ArrayList<String>();
                    NodeList rootElements = parmNode.getChildNodes();
                    String key = "", param = "", descr = "";
                    Node paramNode = null;
                    for (int nc = 0; nc < rootElements.getLength(); nc++) {
                        Node node = rootElements.item(nc);
                        if (node.getNodeName().equals("param-name")) {
                            key = node.getTextContent();
                        }
                        if (node.getNodeName().equals("param-value")) {
                            param = node.getTextContent();
                            paramNode = node;
                        }
                        if (node.getNodeName().equals("description")) {
                            descr = node.getTextContent();
                        }
                        param = (node.getNodeName().equals("param-value") ? node.getTextContent() : param);
                        descr = (node.getNodeName().equals("description") ? node.getTextContent() : descr);
                    }
                    if (key.length() > 0) {
                        parameterList.add(key);
                        parameter.add(param);
                        parameter.add(descr);
                        parameterValues.put(key, parameter);
                        parameterNodes.put(key, paramNode);
                        initialized = true;
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    private void parseContextParamNodes(List<Node> contextNodes) {
        try {
            for (int i = 0; i < contextNodes.size(); i++) {
                Node parmNode = contextNodes.get(i);
                if (parmNode.getNodeName().equalsIgnoreCase("context-param")) {
                    List<String> initParam = new ArrayList<String>();
                    NodeList rootElements = parmNode.getChildNodes();
                    String key = "", param = "", descr = "";
                    Node paramNode = null;
                    for (int nc = 0; nc < rootElements.getLength(); nc++) {
                        Node node = rootElements.item(nc);
                        if (node.getNodeName().equals("param-name")) {
                            key = node.getTextContent();
                        }
                        if (node.getNodeName().equals("param-value")) {
                            param = node.getTextContent();
                            paramNode = node;
                        }
                        if (node.getNodeName().equals("description")) {
                            descr = node.getTextContent();
                        }
                        param = (node.getNodeName().equals("param-value") ? node.getTextContent() : param);
                        descr = (node.getNodeName().equals("description") ? node.getTextContent() : descr);
                    }
                    if (key.length() > 0) {
                        parameterList.add(key);
                        initParam.add(param);
                        initParam.add(descr);
                        parameterValues.put(key, initParam);
                        parameterNodes.put(key, paramNode);
                        initialized = true;
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public void updateParameter(String parameterName, String value) {
        if (!parameterValues.containsKey(parameterName)) {
            return;
        }
        Node valNode = parameterNodes.get(parameterName);
        valNode.setTextContent(value);
        saveWebXmlFile();
    }

    /**
     * Update parameter values of the web.xml file.
     * @param valueMap A map of parameter names and parameter values to be updated.
     */
    public void updateParameters(Map<String, String> valueMap) {
        Set<String> keySet = valueMap.keySet();
        for (String key : keySet) {
            if (parameterNodes.containsKey(key)) {
                String value = valueMap.get(key);
                parameterNodes.get(key).setTextContent(value);
            }
        }
        saveWebXmlFile();
    }

    /**
     * Save the current web.xml document. This document is altered by editing the value
     * nodes carried in the parameterNodes map.
     */
    public void saveWebXmlFile() {
        if (!initialized) {
            return;
        }
        FileOps.saveTxtFile(xmlFile, getDocText(doc));
        testXML();
    }

    private static String getDocText(Document doc) {
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
        }
        return "";
    }

    /**
     * Obtain the value of a particular parameter of the web.xml file.
     * @param parameter The parameter name
     * @return The parameter value
     */
    public String getValue(String parameter) {
        if (isInitialized()) {
            if (parameterValues.containsKey(parameter)) {
                return parameterValues.get(parameter).get(0);
            }
        }
        return "";
    }

    /**
     * Obtain the description of a particular parameter of the web.xml file.
     * @param parameter The parameter name
     * @return The parameter value
     */
    public String getDescription(String parameter) {
        if (isInitialized()) {
            if (parameterValues.containsKey(parameter)) {
                return parameterValues.get(parameter).get(1);
            }
        }
        return "";
    }

    /**
     * Checks if the XmlData is initialized with the minimum parameters
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    public List<String> getInitParamList() {
        return parameterList;
    }

    public Map<String, Node> getInitParamNodes() {
        return parameterNodes;
    }

    public Map<String, List<String>> getInitParamValues() {
        return parameterValues;
    }

    public String getSelectedServlet() {
        return selectedServlet;
    }

    public void setSelectedServlet(String selectedServlet) {
        this.selectedServlet = selectedServlet;
    }

    public File getXmlFile() {
        return xmlFile;
    }
}
