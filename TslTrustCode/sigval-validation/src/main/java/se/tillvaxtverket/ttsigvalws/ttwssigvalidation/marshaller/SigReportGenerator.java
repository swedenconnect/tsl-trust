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
