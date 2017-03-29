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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 * @author stefan
 */
public class XmlBeansUtil {

    private static final Map<String, String> prefixMap = new HashMap<String, String>();
    public static final XmlOptions styled;
    public static final XmlOptions styledNoHeader;
    public static final XmlOptions noHeader;
    public static final XmlOptions stripWhiteSPcae;
    public static final XmlOptions canonical;

    static {
        prefixMap.put("urn:se:tillvaxtverket:tsltrust:1.0:sigval:report", "tslt");
        prefixMap.put("http://www.w3.org/2000/09/xmldsig#", "ds");
        prefixMap.put("urn:oasis:names:tc:SAML:2.0:assertion", "saml");
        prefixMap.put("urn:oasis:names:tc:SAML:1.0:assertion", "saml1");
        prefixMap.put("http://www.w3.org/2001/XMLSchema", "xs");
        prefixMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        prefixMap.put("http://id.elegnamnden.se/csig/1.0/dss-ext/ns", "eid2");
        prefixMap.put("urn:oasis:names:tc:dss:1.0:core:schema", "dss");
        prefixMap.put("http://id.elegnamnden.se/auth-cont/1.0/saml", "saci");
        prefixMap.put("urn:oasis:names:tc:SAML:2.0:metadata", "md");
        prefixMap.put("urn:oasis:names:tc:SAML:metadata:attribute", "mdattr");
        prefixMap.put("urn:oasis:names:tc:SAML:metadata:ui", "mdui");
        prefixMap.put("urn:oasis:names:tc:SAML:profiles:SSO:idp-discovery-protocol", "idpdisco");
        prefixMap.put("http://uri.etsi.org/01903/v1.3.2#", "xades");

        styled = new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4);
        styled.setSaveSuggestedPrefixes(prefixMap);
        styled.setSaveCDataLengthThreshold(10000);
        styled.setSaveCDataEntityCountThreshold(50);

        styledNoHeader = new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4);
        styledNoHeader.setSaveSuggestedPrefixes(prefixMap);
        styledNoHeader.setSaveCDataLengthThreshold(10000);
        styledNoHeader.setSaveCDataEntityCountThreshold(50);
        styledNoHeader.setSaveNoXmlDecl();

        noHeader = new XmlOptions().setSaveNoXmlDecl();
        noHeader.setSaveSuggestedPrefixes(prefixMap);
        
        stripWhiteSPcae = new XmlOptions().setLoadStripWhitespace();
        
        canonical = new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(0).setSaveNoXmlDecl().setSaveSuggestedPrefixes(prefixMap);
        canonical.setSaveCDataLengthThreshold(10000);
        canonical.setSaveCDataEntityCountThreshold(50);
        
    }

    public static XmlObject stripWhiteSpace(XmlObject xo) {
        try {
            XmlObject stripped = XmlObject.Factory.parse(xo.getDomNode(), stripWhiteSPcae);
            return stripped;
        } catch (XmlException ex) {
            return XmlObject.Factory.newInstance();
        }
    }

    public static byte[] getStyledBytes(XmlObject xo) {
        return getStyledBytes(xo, true);
    }

    public static byte[] getStyledBytes(XmlObject xo, boolean xmlHeader) {
        byte[] result = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (xmlHeader) {
                xo.save(bos, styled);
            } else {
                xo.save(bos, styledNoHeader);
            }
            result = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            Logger.getLogger(XmlBeansUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static byte[] getBytes(XmlObject xo) {
        return getBytes(xo, true);
    }

    public static byte[] getBytes(XmlObject xo, boolean xmlHeader) {
        byte[] result = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (xmlHeader) {
                xo.save(bos);
            } else {
                xo.save(bos, noHeader);
            }
            result = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            Logger.getLogger(XmlBeansUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static byte[] getCanonicalBytes(XmlObject xo) {
        byte[] result = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            xo.save(bos, canonical);
            result = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            Logger.getLogger(XmlBeansUtil.class.getName()).log(Level.SEVERE, null, ex);
}
        return result;
    }
}
