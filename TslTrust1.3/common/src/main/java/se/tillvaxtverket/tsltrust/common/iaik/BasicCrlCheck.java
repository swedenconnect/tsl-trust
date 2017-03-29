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
package se.tillvaxtverket.tsltrust.common.iaik;

import java.net.MalformedURLException;
import iaik.asn1.structures.DistributionPoint;
import iaik.x509.X509CRL;
import iaik.x509.X509Certificate;
import iaik.x509.X509ExtensionInitException;
import iaik.x509.extensions.CRLDistributionPoints;
import java.io.File;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import se.tillvaxtverket.tsltrust.common.utils.core.DerefUrl;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.Constants;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.GeneralStaticUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;

/**
 * CRL checking functions for the root TSL
 */
public class BasicCrlCheck implements Constants {
    
    public static boolean checkLotlCertRevocation(List<X509Certificate> chain, String dataDir){
        String crlDir = dataDir + "lotlCrlCache/";
        return checkCertRevocation(chain, crlDir);
    }
    
    /**
     * Extracts all distribution point URIs from a cert and checks if the referenced CRL is cached.
     * If not cached, the CRL is retrieved and cached.
     * @param chain Certificate chain (IAIK Class) holding CRL Distribution point URIs
     * @param crlDir The name of the directory holding CRL cache data.
     * @return A list of keys for retrieving associated CRLs from the cache.
     */    
    public static boolean checkCertRevocation(List<X509Certificate> chain, String crlDir) {
        File cDir = new File(crlDir);
        if (!cDir.canRead()) {
            cDir.mkdirs();
        }
        X509Certificate cert = chain.get(0);
        //Checking validity
        CRLDistributionPoints cdp;
        X509CRL crl = null;

        try {
            cdp = (CRLDistributionPoints) cert.getExtension(CRLDistributionPoints.oid);
            if (cdp != null) {
                Enumeration dPoints = cdp.getDistributionPoints();
                //For every distribution point
                while (dPoints.hasMoreElements()) {
                    DistributionPoint dp = (DistributionPoint) dPoints.nextElement();
                    String[] uris = dp.getDistributionPointNameURIs();

                    //For every URI
                    for (String uri : uris) {
                        if (uri.toLowerCase().startsWith("http")) {
                            crl = getIaikCRLfromUrl(uri, crlDir);
                            if (checkRevocation(crl, chain)){
                                return true;
                            }                            
                        }
                    }
                }
            }
        } catch (X509ExtensionInitException ex) {
        }
        return false;
    }

    public static CRL getCRLfromUrl(String urlString, String crlDir) {
        return KsCertFactory.convertCRL(getIaikCRLfromUrl(urlString, crlDir));
    }

    public static X509CRL getIaikCRLfromUrl(String urlString, String crlDir) {
        String key = FnvHash.getFNV1a(urlString).toString(16); //Gen FNV 64 bit hash of URI
        File crlFile = new File(crlDir, key + ".crl");
        X509CRL crl = getCRLfromFile(crlFile);
        //If no fresh cached CRL, download it
        if (crl == null) {
            try {
                URL url = new URL(urlString);
                DerefUrl.downloadFile(url, crlFile);
                crl = getCRLfromFile(crlFile);
            } catch (MalformedURLException ex) {
            }
        }
        return crl;
    }

    private static X509CRL getCRLfromFile(File crlFile) {
        X509CRL crl = null;
        if (crlFile.canRead()) {
            crl = KsCertFactory.getCRL(FileOps.readBinaryFile(crlFile));
        }
        //Check if cached CRL is within its validity period
        if (crl != null) {
            if (GeneralStaticUtils.getTime(crl.getNextUpdate()).before(Calendar.getInstance())) {
                crl = null;
            }
        }
        return crl;
    }

    public static boolean checkRevocation(X509CRL crl, List<X509Certificate> chain) {

        if (chain == null || chain.size() < 2 || crl == null) {
            return false;
        }
        X509Certificate cert = chain.get(0);
        PublicKey pk = chain.get(1).getPublicKey();

        Calendar present = Calendar.getInstance();
        Calendar nextUpdate = Calendar.getInstance();
        nextUpdate.setTime(crl.getNextUpdate());
        if (present.after(nextUpdate)) {
            return false;
        }
        if (pk != null) {
            try {
                crl.verify(pk);
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
        if (crlIsInScope(crl, cert)) {
            if (crl.isRevoked(cert)) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    private static boolean crlIsInScope(X509CRL crl, X509Certificate cert) {
        boolean inScope = true;
        return inScope;
    }
}
