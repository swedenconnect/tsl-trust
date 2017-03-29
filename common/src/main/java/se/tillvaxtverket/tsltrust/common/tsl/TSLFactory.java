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
package se.tillvaxtverket.tsltrust.common.tsl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import org.etsi.uri.x02231.v2.TrustServiceStatusListDocument;
import org.etsi.uri.x02231.v2.TrustStatusListType;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;

/**
 * Factory class for parsing TSL files in xml format
 */
public class TSLFactory {

    private static final int TSL_MAX_LEN = 4000000;
    private static final Logger LOG = Logger.getLogger(TSLFactory.class.getName());

    public TSLFactory() {
    }

    public TrustServiceList getTsl(InputStream is) throws IOException {
        try {
            byte[] tslBytes = getBytesFromInputStream(is, TSL_MAX_LEN);
            TrustStatusListType tslObject = TrustServiceStatusListDocument.Factory.parse(new ByteArrayInputStream(tslBytes)).getTrustServiceStatusList();
            TrustServiceList tsl = new TrustServiceList(tslObject, tslBytes);
            if (tslObject == null) {
                LOG.info("Unable to read tsl from input stream");
                throw new IOException("Unable to read tsl from Input Stream");
            }
            return tsl;
        } catch (Exception ex) {
            LOG.info("Unable to read tsl from input stream");
            throw new IOException("Unable to read tsl from Input Stream");
        }
    }

    public TrustServiceList getTsl(File tslFile) throws IOException {
        try {
            byte[] tslBytes = FileOps.readBinaryFile(tslFile);
            TrustStatusListType tslObject = TrustServiceStatusListDocument.Factory.parse(tslFile).getTrustServiceStatusList();
            TrustServiceList tsl = new TrustServiceList(tslObject, tslBytes);
            if (tslObject == null) {
                LOG.info("Unable to read tsl File: " + tslFile.getAbsolutePath());
                throw new IOException("Unable to read tsl File: " + tslFile.getAbsolutePath());
            }
            return tsl;
        } catch (Exception ex) {
            LOG.info("Unable to read tsl File: " + tslFile.getAbsolutePath());
            throw new IOException("Unable to read tsl File: " + tslFile.getAbsolutePath());
        }
    }

    public String getTslXmlString(TrustServiceList tsl) {
        try {
            byte[] tslXml = getTslXml(tsl);
            return new String(tslXml, Charset.forName("UTF-8"));
        } catch (Exception ex) {
            return "";
        }
    }

    public byte[] getTslXml(TrustServiceList tsl) {
        try {
            TrustStatusListType tslData = tsl.getTslData();
            String xmlText = tslData.xmlText();
            return xmlText.getBytes(Charset.forName("UTF-8"));
        } catch (Exception ex) {
            return null;
        }
    }

    private static byte[] getBytesFromInputStream(InputStream is, int maxLen)
            throws IOException {

        byte[] bytes = null;
        try {
            // Get the size of the file
            long length = is.available();

            if (length > maxLen) {
                return null;
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file ");
            }
        } catch (Exception ex) {
        } finally {
            is.close();
        }


        // Close the input stream and return bytes
        return bytes;
    }
}
