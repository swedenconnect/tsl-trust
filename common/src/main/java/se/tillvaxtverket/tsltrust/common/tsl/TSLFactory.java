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
