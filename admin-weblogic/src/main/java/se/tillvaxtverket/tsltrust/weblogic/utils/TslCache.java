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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import iaik.x509.X509Certificate;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.etsi.uri.x02231.v2.DigitalIdentityListType;
import org.etsi.uri.x02231.v2.DigitalIdentityType;
import org.etsi.uri.x02231.v2.OtherTSLPointerType;
import org.etsi.uri.x02231.v2.ServiceDigitalIdentityListType;
import se.tillvaxtverket.tsltrust.common.tsl.OtherTSLPointerData;
import se.tillvaxtverket.tsltrust.common.tsl.TSLFactory;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceList;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.EuropeCountry;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.xmldsig.SigVerifyResult;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.data.TslMetaData;
import se.tillvaxtverket.tsltrust.weblogic.db.LogDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.IssueChecker;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.TSLIssueID;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.TSLIssueStack;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.TSLIssueSubcode;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;

/**
 * Main class for handling TSL caching, either from local cache or through
 * downloads
 */
public class TslCache implements TTConstants {

    private static final Logger LOG = Logger.getLogger(TslCache.class.getName());
    private static final long maxAllowedDownloadTime = 1000 * 60 * 10;
    private static TSLFactory tslFact = new TSLFactory();
    private static boolean enforceValidityPeriod = false;
    private List<TslMetaData> cachedTslList = new ArrayList<TslMetaData>();
    private TslTrustModel model;
    private TrustServiceList lotl;
    File lotlTempFile, lotlFile;
    LogDbUtil log;

    /**
     * Creates a TSL caching object for managing TSL loading from cache and
     * recaching from URI source specified in the root TSL (LotL)
     *
     * @param model TSL Trust application model data
     */
    public TslCache(TslTrustModel model) {
        this.model = model;
        lotlTempFile = new File(model.getTempDataLocation() + "lotltempTSL.xml");
        lotlFile = new File(model.getTempDataLocation() + "lotl.xml");
        log = model.getLogDb();
        init();
    }

    /**
     * Returns the currently cached TSLs. For this call to return any data, the
     * function loadTslData or recacheTsl must have been called at least once.
     *
     * @return The current list och cached TSLs
     */
    public List<TslMetaData> getCachedTslList() {
        return cachedTslList;
    }

    /**
     * Returns the current cached root TSL (LotL). The root TSL is loaded from
     * cache when the class is instantiated.
     *
     * @return Root TSL (LotL)
     */
    public TrustServiceList getLotl() {
        return lotl;
    }

    private void init() {
        // Check if List of the lists file is present, else, create directories for its storage below.
        if (!lotlFile.canRead()) {
            try {
                FileUtils.forceMkdir(lotlTempFile.getParentFile());
                recacheLotl();
            } catch (IOException ex) {
                LOG.warning(ex.getMessage());
            }
        } else {
            // If a cached Lotl exists, then also get the cached TSLs.
            lotl = openTsl(lotlFile);
        }

    }

    private TrustServiceList openTsl(File tslFile) {
        TrustServiceList trustServiceList;
        try {
            trustServiceList = tslFact.getTsl(tslFile);
        } catch (IOException e) {

            LOG.warning("Error loading TSL file: " + tslFile.toString());
            trustServiceList = null;
        }
        return trustServiceList;
    }

    /**
     * Downloads the root TSL form the configured location (Configured in the
     * WEB.XML deployment descriptor)
     *
     * @return true if the root TSL was successfully downloaded and parsed.
     */
    public boolean recacheLotl() {
        //recache lotl
        TrustServiceList reCachedLotl = null;
        URL url;
        try {
            url = new URL(model.getLotlUrl());
            TslDownload.getTsl(url, lotlTempFile, log);
            reCachedLotl = validateTslFile(lotlTempFile, lotlFile, EuropeCountry.EU, model.getLotlUrl());
            if (reCachedLotl == null) {
                return false;
            }
            lotl = reCachedLotl;
            return true;
        } catch (MalformedURLException ex) {
            log.addConsoleEvent(new ConsoleLogRecord("Error", "Bad List of the Lists URL", "TSL Extractor"));
        }
        return false;
    }

    /**
     * Checks the signature on the root TSL (LotL) and updates the valid LotL
     * status in the application model object.
     *
     * @return true if the signature was valid.
     */
    public boolean lotlSignatureCheck() {
        //Signature check of Lotl
        boolean validLotlSignature = checkLotlSignature();
        if (!validLotlSignature) {
            TSLIssueStack.push(EuropeCountry.EU, TSLIssueID.invalidSignature, TSLIssueSubcode.NULL, null);
        } else {
            TSLIssueStack.clear(EuropeCountry.EU, TSLIssueID.invalidSignature);
        }
        model.setValidLotl(validLotlSignature);
        return validLotlSignature;
    }

    /**
     * Loads TSL data from the current cached TSL files.
     */
    public void loadTslData() {
        collectTslLists(true);
    }

    /**
     * Downloads all TSL files (except for TSL files matching the configured
     * exception conditions found in the WEB.XML deployment descriptor). If the
     * downloaded file holds a valid XML formatted TSL, this TSL is imported
     * into the cache.
     */
    public void recacheTsl() {
        TslRecache tslRecache = new TslRecache();
        tslRecache.downloadAndParseTsls();
//        collectTslLists(false);
    }

    private static String getUrlId(String url) {
        return FnvHash.getFNV1aToHex(url);

    }

    private void collectTslLists(Boolean useCache) {
        List<TslMetaData> tslList = new ArrayList<TslMetaData>();
        List<TslMetaData> candidateList = getLolOtherTslPointers();
        String tempDir = model.getTempDataLocation();

        for (TslMetaData candidate : candidateList) {
            File tempFile = null;
            try {
                URL url = new URL(candidate.getUrlString());
                candidate.setUrl(url);
                String fileName = url.getPath();
                String fingerPrint = FnvHash.getFNV1aToHex(candidate.getUrlString());
                //Get trimmed file name (exclude url path and %20)
                String fn = TslCache.getUrlFileName(fileName);

                if (fileName.toLowerCase().endsWith("zip")) {
                    File zipTempDir = new File(FileOps.getfileNameString(tempDir, "zipTemp/"));
                    FileUtils.deleteQuietly(zipTempDir);
                    zipTempDir.mkdir();

                    File tslFile = new File(FileOps.getfileNameString(tempDir,
                            fingerPrint + "_" + fn.substring(0, fn.length() - 4) + ".xml"));
                    File zipFile = new File(tslFile.toString() + ".zip");
                    if (!useCache) {
                        tempFile = new File(tslFile.getAbsolutePath() + "temp.xml");
                        TslDownload.getTsl(url, zipFile, log);
                        Unzip.unzipSingleXmlFile(zipFile, tempFile, log);
                        candidate.setTsl(validateTslFile(tempFile, tslFile, candidate.getCountry(), candidate.getUrlString()));
                    }
                    if (tslFile.canRead()) {
                        candidate.setTslFile(tslFile);
                        addTslToList(candidate, tslList);
                    }
                } else {
                    if (!fn.toLowerCase().endsWith(".xml")) {
                        fn += ".xml";
                    }
                    File tslFile = new File(FileOps.getfileNameString(tempDir,
                            fingerPrint + "_" + fn));
                    if (!useCache) {
                        tempFile = new File(tslFile.getAbsolutePath() + "temp.xml");
                        TslDownload.getTsl(url, tempFile, log);
                        candidate.setTsl(validateTslFile(tempFile, tslFile, candidate.getCountry(), candidate.getUrlString()));
                    }
                    if (tslFile.canRead()) {
                        candidate.setTslFile(tslFile);
                        addTslToList(candidate, tslList);
                    }
                }

            } catch (MalformedURLException ex) {
                log.addConsoleEvent(new ConsoleLogRecord("Error", "Invalid TSL URL: " + ex.getMessage(), "TSL Extractor"));
            }
            if (tempFile != null) {
                FileUtils.deleteQuietly(tempFile);
            }

        }
        cachedTslList.clear();
        for (TslMetaData tm : tslList) {
            cachedTslList.add(tm);
        }
        //Release classes
        tslList = null;
        candidateList = null;
    }

    public void addTslToList(TslMetaData candidate, List<TslMetaData> tslList) {
        URL url = candidate.getUrl();
        File tslFile = candidate.getTslFile();
        TrustServiceList tsl = candidate.getTsl();
        boolean downloaded = true;

        // If TSL was not downloaded, try loading the previoudly cached TSL
        if (tsl == null) {
            try {
                tsl = tslFact.getTsl(tslFile);
                if (tsl == null) {
                    return;
                }
                downloaded = false;
            } catch (IOException ex) {
                return;
            }
        }
        candidate.setTsl(tsl);
        addFreshestTSL(candidate, tslList, downloaded);

    }

    private void addFreshestTSL(TslMetaData candidate, List<TslMetaData> tslList, boolean downloaded) {
        String url = candidate.getUrlString();
        try {
            String candTerritory = candidate.getTsl().getSchemeTerritory().trim();
            for (int i = 0; i < tslList.size(); i++) {
                TslMetaData tslMd = tslList.get(i);
                String listedTerr = tslMd.getTsl().getSchemeTerritory().trim();
                if (listedTerr.equalsIgnoreCase(candTerritory)) {
                    Date listedDate = tslMd.getTsl().getIssueDate();
                    if (listedDate.before(candidate.getTsl().getIssueDate())) {
                        checkTslSignature(candidate);
                        tslList.set(i, candidate);
                        if (downloaded) {
                            log.addConsoleEvent(new ConsoleLogRecord("Duplicate TSL downloaded", "Fresher TSL from: " + url, "TSL Extractor"));
                        } else {
                            log.addConsoleEvent(new ConsoleLogRecord("Duplicate cached TSL loaded", "Fresher TSL originating from: " + url, "TSL Extractor"));
                        }
                        return;
                    }
                    if (downloaded) {
                        log.addConsoleEvent(new ConsoleLogRecord("TSL discarded", "From: " + url, "TSL Extractor"));
                    } else {
                        log.addConsoleEvent(new ConsoleLogRecord("TSL discarded", "Originating from: " + url, "TSL Extractor"));
                    }
                    return;
                }
            }

        } catch (Exception ex) {
            return;
        }
        checkTslSignature(candidate);
        tslList.add(candidate);
        if (downloaded) {
            log.addConsoleEvent(new ConsoleLogRecord("Downloaded TSL parsed", "From: " + url, "TSL Extractor"));
        } else {
            log.addConsoleEvent(new ConsoleLogRecord("Cached TSL loaded", "Originating from: " + url, "TSL Extractor"));
        }
    }

    private List<TslMetaData> getLolOtherTslPointers() {
        List<TslMetaData> ptrs = new LinkedList<TslMetaData>();
        try {
            for (OtherTSLPointerData otp : lotl.getOtherTSLPointers()) {
                String tslLocation = (otp.getTSLLocation());
                EuropeCountry country = null;
                try {
                    country = EuropeCountry.valueOf(otp.getSchemeTerritory().toUpperCase());
                } catch (Exception ex) {
                    // The country is not supported by TSL Browser, skip and allert error
                    TSLIssueStack.push(null, TSLIssueID.illegalCountry, TSLIssueSubcode.NULL, otp.getSchemeTerritory());
                    continue;
                }
                if (otp.isMrTslPointer() || !otp.isMimeTypePresent()) {
                    try {
                        if (hasNonASCII(tslLocation)) {
                            String decoded = URLDecoder.decode(tslLocation, "ISO-8859-1");
                            tslLocation = URLEncoder.encode(decoded, "ISO-8859-1");
                        }

//                        // Normalize the URL path and query part to escape encode any illegal non ASCII characters 
//                        URL url = new URL(tslLocation);
//                        String protocol = url.getProtocol();
//                        String host = url.getHost();
//                        String path = url.getPath();
//                        String query = url.getQuery();
//                        if (tslLocation.indexOf("digst.dk")>-1){
//                            String decPath = URIComponentCoder.decodeURIComponent(path);
//                            String decPathISO = URLDecoder.decode(path, "ISO-8859-1");
//                            String decPathUTF = URLDecoder.decode(path, "UTF-8");
//                            int lkasjdf=0;
//                            
//                            
//                        }
//                        URI uri = new URI(protocol, host, URIComponentCoder.decodeURIComponent(path), URIComponentCoder.decodeURIComponent(query), null);
//                        String normalizedURLstring = uri.toASCIIString();
//                        tslLocation = normalizedURLstring;
                        // Get OTP data
                        List<iaik.x509.X509Certificate> certList = otp.getOtpCertificates();
                        ptrs.add(new TslMetaData(tslLocation, certList, country));
                    } catch (Exception ex) {
                    }
                }

            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return ptrs;
    }

    private static boolean hasNonASCII(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((int) c > 127) {
                return true;
            }
        }
        return false;
    }

    private List<X509Certificate> getTSLSignerCert(OtherTSLPointerType otpt) {
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        //Test
        try {
            ServiceDigitalIdentityListType sdis = otpt.getServiceDigitalIdentities();
            DigitalIdentityListType[] sdil = sdis.getServiceDigitalIdentityArray();
            for (DigitalIdentityListType sdi : sdil) {
                DigitalIdentityType[] digitalIdList = sdi.getDigitalIdArray();
                for (DigitalIdentityType di : digitalIdList) {
                    X509Certificate certificate = CertificateUtils.getCertificate(di.getX509Certificate());
                    certList.add(certificate);
                }
            }
        } catch (Exception ex) {
        }
        return certList;
    }

    /**
     * Checks the signature on a TSL and updates signature validation status
     *
     * @param tslMd The metadata object holding the TSL to be checked for
     * validity
     */
    public void checkTslSignature(TslMetaData tslMd) {
        TrustServiceList tsl = tslMd.getTsl();
        List<X509Certificate> otherTslPointerCerts = tslMd.getCertList();
        String sigStatus;
        X509Certificate usedSignCert = null;
        SigVerifyResult sigVer = null;
        try {
            sigVer = tsl.verifySignature();
        } catch (Exception ex) {
            sigStatus = SIGNSTATUS_SYNTAX;
            tslMd.setSignStatus(sigStatus);
            TSLIssueStack.push(tslMd.getCountry(), TSLIssueID.sigSyntax, TSLIssueSubcode.NULL, null);
            return;
        }
        try {
            if (sigVer.cert != null) {
                usedSignCert = CertificateUtils.getCertificate(sigVer.cert.getEncoded());
                tslMd.setUsedTslSigCert(usedSignCert);
            }
        } catch (CertificateEncodingException ex) {
        }
        if (sigVer.status.equalsIgnoreCase("no signature")) {
            sigStatus = SIGNSTATUS_ABSENT;
            tslMd.setSignStatus(sigStatus);
            TSLIssueStack.push(tslMd.getCountry(), TSLIssueID.unsigned, TSLIssueSubcode.NULL, null);
            return;
        }
        if (usedSignCert == null) {
            sigStatus = SIGNSTATUS_SYNTAX;
            tslMd.setSignStatus(sigStatus);
            TSLIssueStack.push(tslMd.getCountry(), TSLIssueID.sigSyntax, TSLIssueSubcode.NULL, null);
            return;
        }
        if (!sigVer.valid) {
            sigStatus = SIGNSTATUS_INVALID;
            tslMd.setSignStatus(sigStatus);
            TSLIssueStack.push(tslMd.getCountry(), TSLIssueID.invalidSignature, TSLIssueSubcode.NULL, null);
            return;
        }

        if (enforceValidityPeriod) {
            if (!isWithinValidityPeriod(usedSignCert)) {
                sigStatus = SIGNSTATUS_INVALID;
                tslMd.setSignStatus(sigStatus);
                return;
            }
        }
        IssueChecker.checkCertExpiry(tslMd.getCountry(), usedSignCert);

        sigStatus = SIGNSTATUS_UNVERIFIABLE;
        for (X509Certificate lotlOtpCert : otherTslPointerCerts) {
            // Acept cert match
            if (usedSignCert.equals(lotlOtpCert)) {
                sigStatus = SIGNSTATUS_VERIFIED;
            }
            // Allow also PK and DN match
            if (usedSignCert.getPublicKey().equals(lotlOtpCert.getPublicKey())) {
                if (usedSignCert.getSubjectDN().equals(lotlOtpCert.getSubjectDN())) {
                    sigStatus = SIGNSTATUS_VERIFIED;
                }
            }

        }

        if (sigStatus.equals(SIGNSTATUS_UNVERIFIABLE)) {
            TSLIssueStack.push(tslMd.getCountry(), TSLIssueID.unknownSigCert, TSLIssueSubcode.NULL, null);
        } else {
            TSLIssueStack.clear(tslMd.getCountry(), TSLIssueID.invalidSignature);
            TSLIssueStack.clear(tslMd.getCountry(), TSLIssueID.sigSyntax);
            TSLIssueStack.clear(tslMd.getCountry(), TSLIssueID.unsigned);
            TSLIssueStack.clear(tslMd.getCountry(), TSLIssueID.unknownSigCert);
        }

        sigStatus = (model.isValidLotl()) ? sigStatus : SIGNSTATUS_INVALID_LOTL;
        tslMd.setSignStatus(sigStatus);
    }

    private boolean checkLotlSignature() {
        try {
            SigVerifyResult sigVer = lotl.verifySignature();
            if (sigVer.valid) {
                return LotlVerifier.validateLotlSignCert(sigVer.cert, model);
            }
        } catch (Exception ex) {
        }
        return false;
    }

    private boolean isWithinValidityPeriod(X509Certificate cert) {
        Calendar present = Calendar.getInstance();
        Calendar certNotBefore = Calendar.getInstance();
        certNotBefore.setTime(cert.getNotBefore());
        Calendar certNotAfter = Calendar.getInstance();
        certNotAfter.setTime(cert.getNotAfter());
        if (present.before(certNotBefore)) {
            return false;
        }
        if (present.after(certNotAfter)) {
            return false;
        }
        return true;
    }

    /**
     * Validates a newly downloaded TSL. If that TSL is not valid. Use instead
     * the previously cached TSL
     *
     * @param temporaryFile
     * @param tslFile
     * @param country
     * @param url
     * @return
     */
    private TrustServiceList validateTslFile(File temporaryFile, File tslFile, EuropeCountry country, String url) {
        TrustServiceList tsl;
        boolean valid = true;
        try {
            // Try to read the TSL from the temp file
            tsl = tslFact.getTsl(temporaryFile);
            valid = tslContentCheck(tsl);
            // Upon no exceptions, the temp file is OK. Now store the temp file content in the 
            // permanent TSL file.
            if (valid) {
                FileOps.saveByteFile(FileOps.readBinaryFile(temporaryFile), tslFile);
                TSLIssueStack.clear(country, TSLIssueID.unavailable);
            } else {
                tsl = null;
            }
            //The replaced library does not calculate hash from the file, but from the retrieved data.
        } catch (IOException ex) {
            tsl = null;
        }

        //If tsl==null attempt to recover from prestored file.
        if (tsl == null) {
            if (tslFile == null) {
                log.addConsoleEvent(new ConsoleLogRecord("Error", "Failed to parse dowloaded TSL: NULL", "TSL Extractor"));
            } else {
                log.addConsoleEvent(new ConsoleLogRecord("Error", "Failed to parse dowloaded TSL: " + tslFile.getName() + " Attempting recover...", "TSL Extractor"));
                TSLIssueStack.push(country, TSLIssueID.unavailable, TSLIssueSubcode.NULL, url);
                try {
                    tsl = tslFact.getTsl(tslFile);
                } catch (IOException ex) {
                    log.addConsoleEvent(new ConsoleLogRecord("Error", "Failed to recover from precached file: " + tslFile.getName(), "TSL Extractor"));
                }
            }
        }

        //Check for expiry
        IssueChecker.checkTslExpiry(country, tsl);

        return tsl;
    }

    public void saveFile(File file, String saveString) {
        /**/
        //if (file.canWrite()) {
        //    jTextArea2.setText(file.getName() + (char) 10 + file.getPath());
        try {
            Writer output = null;
            output = new BufferedWriter(new FileWriter(file));
            output.write(saveString);
            output.close();
            /*
             * If the selected filenamne ends with .nroff - Save the encoded output as a .txt file with same file name.
             */
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }

    public static String getUrlFileName(String inpString) {
        int len = inpString.length();
        char SLASH = (char) 47;

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i < len - 2 && inpString.substring(i, i + 3).equalsIgnoreCase("%20")) {
                b.append("_");
                i++;
                i++; //Skip next two chars
            } else {
                b.append(inpString.charAt(i));
            }
        }
        inpString = b.toString();
        len = inpString.length();
        String outString = inpString;

        for (int i = 0; i < len; i++) {
            if (inpString.charAt(i) == SLASH) {
                outString = inpString.substring(i + 1, len);
            }
        }
        return outString;
    }

    private boolean tslContentCheck(TrustServiceList tsl) {
        try {
            String strVal = tsl.getSchemeTerritory();
            if (!(strVal.length() > 0)) {
                return false;
            }
            Date issueDate = tsl.getIssueDate();
            if (issueDate == null) {
                return false;
            }
            strVal = tsl.getSchemeOperatorName();
            if (!(strVal.length() > 0)) {
                return false;
            }
            BigInteger sequenceNumber = tsl.getSequenceNumber();
            if (sequenceNumber == null) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public void httpGet(URL url, File resultFile) {
        if (url == null) {
            log.addConsoleEvent(new ConsoleLogRecord("Error", "Http error: Attempted to download null URL", "Download Utils"));
            return;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedInputStream bufIn = new BufferedInputStream(conn.getInputStream());
            try {

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    FileOutputStream fos = new FileOutputStream(resultFile);
                    byte[] b = new byte[100];
                    for (;;) {
                        int len = bufIn.read(b);
                        if (len == -1) {
                            break;
                        } else {
                            fos.write(b, 0, len);
                        }
                    }
                    fos.close();
                } else {
                    log.addConsoleEvent(new ConsoleLogRecord("Error", "Http error: " + String.valueOf(responseCode) + " " + url.toString(), "Download Utils"));
                    return;
                }
            } catch (Exception ex) {
                log.addConsoleEvent(new ConsoleLogRecord("Error", "I/O Error: " + url.toString() + " " + ex.getMessage(), "Download Utils"));
                return;
            } finally {
                bufIn.close();
            }

        } catch (Exception e) {
            log.addConsoleEvent(new ConsoleLogRecord("Error", "I/O Error: " + url.toString() + " " + e.getMessage(), "Download Utils"));
            return;
        }
        log.addConsoleEvent(new ConsoleLogRecord("TSL downloaded", "From: " + url, "TSL Extractor"));
    }

    class TslRecache implements Runnable {

        Map<String, TslDownLoadData> downloadMap = new HashMap<String, TslDownLoadData>();
        boolean allTslUrlDerefed = false;
        List<TslMetaData> tslList = new ArrayList<TslMetaData>();
        List<TslMetaData> candidateList = getLolOtherTslPointers();
        String tempDir = model.getTempDataLocation();

        public TslRecache() {
        }

        @Override
        public void run() {
            downloadAndParseTsls();
        }

        public void downloadAndParseTsls() {

            for (TslMetaData candidate : candidateList) {
                String urlString = candidate.getUrlString();
                String urlId = getUrlId(urlString);
                TslDownLoadData tslData = new TslDownLoadData(urlString);
                downloadMap.put(urlId, tslData);

                Thread downloadThread = new Thread(new TslDownloader(tslData, candidate.getCountry()));
                downloadThread.setDaemon(true);
                downloadThread.start();
            }

            allTslUrlDerefed = false;
            long startTime = System.currentTimeMillis();

            while (!allTslUrlDerefed) {
                allTslUrlDerefed = isDownloaded();
                if (!allTslUrlDerefed) {
                    if (System.currentTimeMillis() > startTime + maxAllowedDownloadTime) {
                        allTslUrlDerefed = true;
                        LOG.warning("Reached maximum downloading time. Aborting download....");
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }

            processTsls();

        }

        private void processTsls() {
            tslList = new ArrayList<TslMetaData>();

            for (TslMetaData candidate : candidateList) {

                boolean success = true;
                String id = getUrlId(candidate.getUrlString());
                if (downloadMap.containsKey(id)) {
                    TslDownLoadData tslData = downloadMap.get(id);
                    if (tslData.tsl != null) {
                        candidate.setTsl(tslData.tsl);
                        candidate.setTslFile(tslData.tslFile);
                        candidate.setUrl(tslData.url);
                        addTslToList(candidate, tslList);
                    } else {
                        success = false;
                    }

                    if (!success) {
                        //Attempt to recover from stored file
                        try {
                            TrustServiceList tsl = tslFact.getTsl(tslData.tslFile);
                            if (tsl != null) {
                                tslData.tsl = tsl;
                                candidate.setTsl(tsl);
                                candidate.setTslFile(tslData.tslFile);
                                candidate.setUrl(tslData.url);
                                addTslToList(candidate, tslList);
                                log.addConsoleEvent(new ConsoleLogRecord("Revocered TSL", "From: " + tslData.urlStr, "TSL Extractor"));
                            }

                        } catch (IOException ex) {
                        }
                    }
                    if (tslData.tsl == null) {
                        log.addConsoleEvent(new ConsoleLogRecord("Error", "Failed to recover from precache: " + tslData.urlStr, "TSL Extractor"));
                    }

                }
            }
            //Save result
            cachedTslList.clear();
            for (TslMetaData tm : tslList) {
                cachedTslList.add(tm);
            }
            //Release classes
            tslList = null;
            candidateList = null;

        }

        private boolean isDownloaded() {
            Set<String> keySet = downloadMap.keySet();
            for (String key : keySet) {
                TslDownLoadData tslData = downloadMap.get(key);
                if (!tslData.downloaded) {
                    return false;
                }
            }
            return true;
        }
    }

    class TslDownloader implements Runnable {

        TslDownLoadData tslData;
        EuropeCountry country;

        public TslDownloader(TslDownLoadData tslData, EuropeCountry country) {
            this.tslData = tslData;
            this.country = country;
        }

        @Override
        public void run() {
            downloadTSL(tslData);
            tslData.tsl = validateTslFile(tslData.tempFile, tslData.tslFile, country, tslData.urlStr);
            tslData.downloaded = true;
            if (tslData.tempFile != null && tslData.tempFile.canRead()) {
                tslData.tempFile.delete();
            }
        }

        private void downloadTSL(TslDownLoadData tslData) {
            String tempDir = model.getTempDataLocation();

            if (tslData.zip) {
                File zipTempDir = new File(FileOps.getfileNameString(tempDir, "zipTemp/"));
                FileUtils.deleteQuietly(zipTempDir);
                zipTempDir.mkdirs();
                File zipFile = new File(zipTempDir, tslData.tempFile.getName() + ".zip");
                httpGet(tslData.url, zipFile);
                Unzip.unzipSingleXmlFile(zipFile, tslData.tempFile, log);
            } else {
                httpGet(tslData.url, tslData.tempFile);
            }

        }
    }

    class TslDownLoadData {

        public String id;
        public String urlStr;
        public URL url;
        public File tempFile;
        public File tslFile;
        public TrustServiceList tsl;
        public boolean downloaded = false;
        public boolean zip = false;

        public TslDownLoadData(String urlStr) {
            if (!urlStr.startsWith("http")) {
                urlStr = "http://" + urlStr;
            }
            this.urlStr = urlStr;
            this.id = getUrlId(urlStr);
            tempFile = new File(FileOps.getfileNameString(model.getTempDataLocation(), id + "_temp.xml"));
            String tempDir = model.getTempDataLocation();

            try {
                url = new URL(urlStr);
                String fileName = url.getPath();
                String fingerPrint = getUrlId(urlStr);
                //Get trimmed file name (exclude url path and %20)
                String fn = TslCache.getUrlFileName(fileName);

                tempFile = new File(FileOps.getfileNameString(tempDir, fingerPrint + "_temp.xml"));
                if (fileName.toLowerCase().endsWith("zip")) {
                    zip = true;
                    tslFile = new File(FileOps.getfileNameString(tempDir,
                            fingerPrint + "_" + fn.substring(0, fn.length() - 4) + ".xml"));
                } else {
                    if (!fn.toLowerCase().endsWith(".xml")) {
                        fn += ".xml";
                    }
                    tslFile = new File(FileOps.getfileNameString(tempDir,
                            fingerPrint + "_" + fn));
                }
            } catch (MalformedURLException ex) {
                log.addConsoleEvent(new ConsoleLogRecord("Error", "Http error: " + urlStr + " is not a valid URL", "Download Utils"));
                Logger.getLogger(TslCache.class.getName()).warning(ex.getMessage());
            }
        }
    }
}
