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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlbeans.XmlOptions;
import x0SigvalReport.seTillvaxtverketTsltrust1.SignedDocumentValidationDocument;
import x0SigvalReport.seTillvaxtverketTsltrust1.SignedDocumentValidationType;

/**
 * Utility class for creation of the signature report element and constructing the XML output of the signature report
 */
public final class SigReportGenerator implements SigreportConstants {

    private static final Map<String, String> prefixMap = new HashMap<String, String>();
    private SignedDocumentValidationDocument sigReportDoc;
    private SignedDocumentValidationType sigReport;
    private static final Logger LOG = Logger.getLogger(SigReportGenerator.class.getName());

    static {
        prefixMap.put("urn:se:tillvaxtverket:tsltrust:1.0:sigval:report", "tslt");
    }
    

    public SigReportGenerator() {
        sigReportDoc = SignedDocumentValidationDocument.Factory.newInstance();
        sigReport = sigReportDoc.addNewSignedDocumentValidation();
    }

    public SignedDocumentValidationType getSigReport() {
        return sigReport;
    }

    public String getValidationReport() {
        ByteArrayOutputStream xmlBytes = new ByteArrayOutputStream();
        XmlOptions xo = new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4);
        xo.setSaveSuggestedPrefixes(prefixMap);
        xo.setSaveCDataLengthThreshold(10000);
        xo.setSaveCDataEntityCountThreshold(50);
        try {
            sigReportDoc.save(xmlBytes, xo);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        String xmlString="";
        try {
            xmlString = xmlBytes.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return xmlString;
    }
}
