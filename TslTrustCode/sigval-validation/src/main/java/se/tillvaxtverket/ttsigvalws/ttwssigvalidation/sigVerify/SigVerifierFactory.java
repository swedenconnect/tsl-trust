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

import java.util.logging.Logger;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.DocType;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;

/**
 * Factory class determining and returning an appropriate signature validation
 * object for a given document.
 */
public class SigVerifierFactory {

    private static final Logger LOG = Logger.getLogger(SigVerifierFactory.class.getName());

    public static SigVerifier getSigVerifier(SigValidationModel model) {
        //Check if the model file holds any data
//        InputStream is;
//        File signedFile = new File(model.getSigFileName());
//        if (signedFile.canRead()) {
//            SigDocument sigDoc = new SigDocument(signedFile);
//            model.setSigDocument(sigDoc);
//        } else {
//            
//        }
//        try {
//            is = new FileInputStream(signedFile);
//            model.setDataSource(DataSource.file);
//        } catch (FileNotFoundException ex) {
//            is = new ByteArrayInputStream(model.getSignedData());
//            model.setDataSource(DataSource.bytes);
//        }

        SigVerifier sigVerifier = null;
        DocType docType = model.getSigDocument().getDocType();
        if (docType.equals(DocType.PDF)) {
            sigVerifier = new PdfSigVerifier(model);
            return sigVerifier;
        }
        if (docType.equals(DocType.XML)) {
            sigVerifier = new XmlSigVerifier(model);
            return sigVerifier;
        }
        return null;
    }

//    /**
//     * Guess the document format and return an appropriate document type string
//     * 
//     * @param is An InputStream holding the document
//     * @return "xml" if the document is an XML document or "pdf" if the document is a PDF document, or else an error message.
//     */
//    public static String getDocType(InputStream is) {
//        InputStream input = null;
//
//        try {
//            input = new BufferedInputStream(is);
//            input.mark(5);
//            byte[] preamble = new byte[5];
//            int read = 0;
//            try {
//                read = input.read(preamble);
//                input.reset();
//            } catch (IOException ex) {
//                return "null;Not a valid document";
//            }
//            if (read < 5) {
//                return "null;Not a valid document";
//            }
//            String preambleString = new String(preamble);
//            byte[] xmlPreable = new byte[]{'<', '?', 'x', 'm', 'l'};
//            byte[] xmlUtf8 = new byte[]{-17, -69, -65, '<', '?'};
//            if (Arrays.equals(preamble, xmlPreable) || Arrays.equals(preamble, xmlUtf8)) {
//                return "xml";
//            } else if (preambleString.equals("%PDF-")) {
//                return "pdf";
//            } else if (preamble[0] == 'P' && preamble[1] == 'K') {
//                ZipInputStream asics = new ZipInputStream(new BufferedInputStream(is));
//                ByteArrayOutputStream datafile = null;
//                ByteArrayOutputStream signatures = null;
//                ZipEntry entry;
//                try {
//                    while ((entry = asics.getNextEntry()) != null) {
//                        if (entry.getName().equals("META-INF/signatures.p7s")) {
//                            signatures = new ByteArrayOutputStream();
//                            IOUtils.copy(asics, signatures);
//                            signatures.close();
//                        } else if (entry.getName().equalsIgnoreCase("META-INF/signatures.p7s")) {
//                            /* Wrong case */
//                            return "asics;Non ETSI compliant";
//                        } else if (entry.getName().indexOf("/") == -1) {
//                            if (datafile == null) {
//                                datafile = new ByteArrayOutputStream();
//                                IOUtils.copy(asics, datafile);
//                                datafile.close();
//                            } else {
//                                return "asics;ASiC-S profile support only one data file";
//                            }
//                        }
//                    }
//                } catch (Exception ex) {
//                    return "null;Invalid ASiC-S";
//                }
//                if (datafile == null || signatures == null) {
//                    return "asics;ASiC-S profile support only one data file with CAdES signature";
//                }
//                return "asics/cades";
//
//            } else if (preambleString.getBytes()[0] == 0x30) {
//                return "cades";
//            } else {
//                return "null;Document format not recognized/handled";
//            }
//        } finally {
//            if (input != null) {
//                try {
//                    input.close();
//                } catch (IOException e) {
//                }
//            }
//        }
//    }
//    private static boolean loadpdfFile(SigValidationModel model) {
//        DataSource dataSource = model.getDataSource();
//        PdfReader reader;
//        try {
//            switch (dataSource) {
//                case bytes:
//                    reader = new PdfReader(new ByteArrayInputStream(model.getSignedData()));
//                    break;
//                default:
//                    reader = new PdfReader(model.getSigFileName());
//            }
//            model.setReader(reader);
//            return true;
//        } catch (IOException ex) {
//            LOG.info("Failed reading PDF");
//            return false;
//        }
//    }
}
