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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Utility class for XML processing
 */
public class XmlUtils {

    private final static Logger LOG = Logger.getLogger(XmlUtils.class.getName());

    /**
     * Generates a pretty XML print of an XML document based on java.xml functions.
     * @param doc The doc being processed
     * @return Test representation of the XML document
     */
    public static String getDocText(Document doc) {
        if (doc == null) {
            return "";
        }

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
            LOG.log(Level.INFO, null, ex);
        }
        return "";
    }

    /**
     * Provides a canonical print of the XML document. The purpose of this print
     * is to try to preserve integrity of an existing signature.
     * @param doc The XML document being processed.
     * @return XML String
     */
    public static byte[] getCanonicalDocText(Document doc) {
        try {
            // Output the resulting document.
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(os));
            byte[] xmlData = os.toByteArray();
            return xmlData;
        } catch (TransformerException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Document getDocument(byte[] xmlData) {
        Document doc=null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new ByteArrayInputStream(xmlData));
            doc.getDocumentElement().normalize();
        } catch (Exception ex) {
        }
        return doc;
    }

    /**
     * Pare an XML file and returns an XML document
     * @param xmlFile The XML file being parsed
     * @return XML document
     */
    public static Document loadXMLContent(File xmlFile) {
        Document doc;
        try {
            InputStream is = new FileInputStream(xmlFile);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
            return null;
        }

        return doc;

    }

    /**
     * Parse an XML file and returns an XML string
     * @param xmlFile The XML file being parsed
     * @return XML String
     */
    public static String getParsedXMLText(File xmlFile) {
        return getDocText(loadXMLContent(xmlFile));
    }
}
