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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.core.CorePEM;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.data.ExternalCert;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.TslPolicy;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDb;
import se.tillvaxtverket.tsltrust.weblogic.db.ValPoliciesDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import iaik.x509.X509Certificate;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for TSL Trust policy handling
 */
public class PolicyUtils implements TTConstants, HtmlConstants {

    private ValPoliciesDbUtil policyDb;
    private TslCertDb tslCertDb;
    private File recacheFile;
    String lastrecache = "";
    List<String> tslStateVals, tslTypeVals, tslStatusVals, tslSignatureVals;

    public PolicyUtils(TslTrustModel model) {
        this.policyDb = model.getPolicyDb();
        this.tslCertDb = model.getTslCertDb();
        recacheFile = new File(model.getDataLocation() + "cfg/recacheTime");
        tslSignatureVals = new ArrayList<String>();
        tslSignatureVals.add(SIGNSTATUS_VERIFIED);
        tslSignatureVals.add(SIGNSTATUS_UNVERIFIABLE);
        tslSignatureVals.add(SIGNSTATUS_ABSENT);
        tslSignatureVals.add(SIGNSTATUS_SYNTAX);
        tslSignatureVals.add(SIGNSTATUS_INVALID_LOTL);
        tslSignatureVals.add(SIGNSTATUS_INVALID);

    }

    /**
     * If not initialized, or upon server update, reload policy parameter choices
     * from the current TslCertificates database.
     */
    public void recachePolicyParameters() {
        if (recacheFile.canRead()) {
            String recacheTime = FileOps.readTextFile(recacheFile).trim();
            if (!recacheTime.equals(lastrecache)) {
                lastrecache = recacheTime;
                recache();
            }
        }
    }

    private void recache() {
        tslStateVals = new ArrayList<String>();
        tslTypeVals = new ArrayList<String>();
        tslStatusVals = new ArrayList<String>();
        //Add the All States value
        tslStateVals.add(ALL_STATES);
        try {
            List<TslCertificates> allTslCertificate = tslCertDb.getAllTslCertificate(false);
            for (TslCertificates tc : allTslCertificate) {
                String territory = tc.getTerritory();
                String serviceType = tc.getTrustServiceType();
                String serviceStatus = tc.getServiceStatus();

                if (territory != null && !tslStateVals.contains(territory)) {
                    tslStateVals.add(territory);
                }
                if (serviceType != null && !tslTypeVals.contains(serviceType)) {
                    tslTypeVals.add(serviceType);
                }
                if (serviceStatus != null && !tslStatusVals.contains(serviceStatus)) {
                    tslStatusVals.add(serviceStatus);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Get signature configuration parameter choices
     * @return Signature configuration parameter choices
     */
    public List<String> getTslSignatureVals() {
        return tslSignatureVals;
    }

    /**
     * Get country configuration parameter choices
     * @return Country configuration parameter choices
     */
    public List<String> getTslStateVals() {
        return tslStateVals;
    }

    /**
     * Get service status configuration parameter choices
     * @return Service status configuration parameter choices
     */
    public List<String> getTslStatusVals() {
        return tslStatusVals;
    }

    /**
     * Get service type configuration parameter choices
     * @return Service type configuration parameter choices
     */
    public List<String> getTslTypeVals() {
        return tslTypeVals;
    }

    /**
     * Get the list of unselected TSL policies within a validation policy. This function is primarily used to
     * provide data to a select drop-down list for adding a new currently not selected TSL policy.
     * @param valPolicy The Validation policy where the list of TSL policies are not currently in use
     * @return List of TSL policy names
     */
    public List<String> getUnselectedTslPolicies(ValidationPolicy valPolicy) {
        List<String> tpList = new ArrayList<String>();
        List<String> tslPolicyNames = valPolicy.getTslPolicies();
        List<TslPolicy> tslPolicies = policyDb.getTslPolicies();
        for (TslPolicy tp : tslPolicies) {
            if (!tslPolicyNames.contains(tp.getTslPolicyName())) {
                tpList.add(tp.getTslPolicyName());
            }
        }
        return tpList;
    }

    /**
     * Get the list of TSL service certificates that matches a given validation policy
     * @param valPolicy The validation policy used to test compliance
     * @return List of TSL service certificate database records.
     */
    public List<TslCertificates> getPolicyCompliantCerts(ValidationPolicy valPolicy) {
        List<TslCertificates> compliantList = new LinkedList<TslCertificates>();
        List<String> dbCertId = new LinkedList<String>();
        List<TslCertificates> tcList = reduceTslCertList(tslCertDb.getAllTslCertificate(false), valPolicy);
        List<String> tslPolicyNames = valPolicy.getTslPolicies();
        List<TslPolicy> tslPolicies = policyDb.getTslPolicies();

        for (TslPolicy tp : tslPolicies) {
            if (tslPolicyNames.contains(tp.getTslPolicyName())) {
                addCompliantCerts(tp, tcList, compliantList, dbCertId);
            }
        }
        return compliantList;
    }

    private List<TslCertificates> reduceTslCertList(List<TslCertificates> allTslCertificate, ValidationPolicy valPolicy) {
        //Remove EE Certs and expired certs and certs on block list
        long thresTime = System.currentTimeMillis() + DAY_MILLIS; // Threasold time = current time+ one day
        List<TslCertificates> reducedList = new LinkedList<TslCertificates>();
        for (TslCertificates tc : allTslCertificate) {
            long certExpiry = tc.getCertExpiry();
            short sdiType = tc.getSdiType();
            if (sdiType == 0 || sdiType == 3) { // EE or QcEE cert
                continue;
            }
            if (certExpiry < thresTime) {
                continue;
            }
            if (valPolicy.getBlockCertIds().contains(tc.getTslCertHash())) {
                continue;
            }
            reducedList.add(tc);
        }
        return reducedList;
    }

    private void addCompliantCerts(TslPolicy tp, List<TslCertificates> tcList, List<TslCertificates> compliantList, List<String> complIdList) {
        for (TslCertificates tc : tcList) {
            // Test if this certificate has be approved by previously tested TslPolicies
            String tcId = tc.getTslCertHash();
            if (complIdList.contains(tcId)) {
                continue;
            }
            // Check Tsl expiry compliance
            long tslExp = tc.getTslExpDate();
            int grace = tp.getExpiredTslGrace();
            long graceMillis = (long) grace * DAY_MILLIS;
            long currentTime = System.currentTimeMillis();
            long cutTime = (grace < 0) ? currentTime + DAY_MILLIS : tslExp + graceMillis;

            if (currentTime > cutTime) {
                continue;
            }
            // Check compliant state
            List<String> states = tp.getStates();
            String tcState = tc.getTerritory();
            if (!states.contains(ALL_STATES) && !states.contains(tcState)) {
                continue;
            }
            // Check service type compliance
            List<String> serviceTypes = tp.getServiceTypes();
            String tcServType = tc.getTrustServiceType();
            if (!serviceTypes.contains(tcServType)) {
                continue;
            }
            // Check service status compliance
            List<String> statusTypes = tp.getStatusTypes();
            String tcStatus = tc.getServiceStatus();
            if (!statusTypes.contains(tcStatus)) {
                continue;
            }
            // Check signature status compliance
            List<String> signStatus = tp.getSignStatus();
            String tcSigStatus = tc.getSignStatus();
            if (!signStatus.contains(tcSigStatus)) {
                continue;
            }
            //Passing the tests above, the certificate is compliant and new to the compliant list
            complIdList.add(tcId);
            compliantList.add(tc);
        }
    }

    /**
     * Get a list of countries supported by a list of TSL service certificate records
     * @param policyCompliantCerts The list of TSL certificate records from which country names are extracted     * 
     * @return List of country names
     */
    public List<String> getStateList(List<TslCertificates> policyCompliantCerts) {
        List<String> stateList = new ArrayList<String>();
        for (TslCertificates tc : policyCompliantCerts) {
            String state = tc.getTerritory();
            if (!stateList.contains(state)) {
                stateList.add(state);
            }
        }
        Collections.sort(stateList);
        return stateList;
    }

    /**
     * Provides the list of TSL service certificate records that is related to a particular national TSL
     * @param state The 2 letter country code, specifying the target country
     * @param certList The list of TSL service certificate records from which matching records are extracted
     * @return List of TSL service certificate records
     */
    public List<TslCertificates> getStateCertList(String state, List<TslCertificates> certList) {
        List<TslCertificates> stateCertList = new ArrayList<TslCertificates>();
        for (TslCertificates tc : certList) {
            if (tc.getTerritory().equalsIgnoreCase(state)) {
                stateCertList.add(tc);
            }
        }
        return stateCertList;
    }

    /**
     * Get a list of TSP names supported by a list of TSL service certificate records
     * @param certList The list of TSL certificate records from which TSP names are extracted     * 
     * @return List of TSP names
     */
    public List<String> getTspList(List<TslCertificates> certList) {
        List<String> tspList = new ArrayList<String>();
        for (TslCertificates tc : certList) {
            String tsp = tc.getTspName();
            if (!tspList.contains(tsp)) {
                tspList.add(tsp);
            }
        }
        Collections.sort(tspList);
        return tspList;
    }

    /**
     * Provides the list of TSL service certificate records that is related to a particular national TSP
     * @param tsp The name of the TSP, specifying the target TSP
     * @param certList The list of TSL service certificate records from which matching records are extracted
     * @return List of TSL service certificate records
     */
    public List<TslCertificates> getTspCertList(String tsp, List<TslCertificates> certList) {
        List<TslCertificates> tspCertList = new ArrayList<TslCertificates>();
        for (TslCertificates tc : certList) {
            if (tc.getTspName().equalsIgnoreCase(tsp)) {
                tspCertList.add(tc);
            }
        }
        return tspCertList;
    }

    public String getBase64Cert(String certHash, String extCertEntry) {
        List<TslCertificates> dbCertList = tslCertDb.getAllTslCertificate(false);
        for (TslCertificates tc : dbCertList) {
            if (tc.getTslCertHash().equals(certHash)) {
                try {
                    byte[] cert = CertificateUtils.getCertificate(tc.getTslCertificate()).getEncoded();
                    return new String(Base64Coder.encode(cert));
                } catch (Exception ex) {
                }
            }
        }
        List<ExternalCert> externalCerts = policyDb.getExternalCerts();
        for (ExternalCert extCert : externalCerts) {
            if (extCert.getCertificateId().equals(certHash)) {
                return extCert.getB64Cert();
            }
        }
        // Final chance, check latest entered external cert
        X509Certificate cert;
        String pemCert;
        try {
            cert = KsCertFactory.getIaikCert(CertificateUtils.getCertificate(extCertEntry));
            pemCert = CorePEM.getPemCert(cert.getEncoded());
            return (CorePEM.trimPemCert(pemCert));
        } catch (Exception ex) {
        }

        return "";
    }

    public X509Certificate getIAIKCert(String certHash) {
        List<TslCertificates> dbCertList = tslCertDb.getAllTslCertificate(false);
        for (TslCertificates tc : dbCertList) {
            if (tc.getTslCertHash().equals(certHash)) {
                try {
                    byte[] cert = CertificateUtils.getCertificate(tc.getTslCertificate()).getEncoded();
                    X509Certificate iaikCert = KsCertFactory.getIaikCert(cert);
                    return iaikCert;
                } catch (Exception ex) {
                }
            }
        }
        List<ExternalCert> externalCerts = policyDb.getExternalCerts();
        for (ExternalCert extCert : externalCerts) {
            if (extCert.getCertificateId().equals(certHash)) {
                return KsCertFactory.getIaikCert(Base64Coder.decodeLines(extCert.getB64Cert()));
            }
        }
        return null;
    }

    public void resetPolicyTableFolding(ValidationPolicy valPolicy, SessionModel session) {
        String tableID = "vp" + FnvHash.getFNV1aToHex(valPolicy.getPolicyName());
        session.resetTableFold(tableID);
    }

    public void blockallTSPServices(ValidationPolicy vp, String parameter) {
        List<TslCertificates> complList = getPolicyCompliantCerts(vp);
        List<String> blockCertIds = vp.getBlockCertIds();
        for (TslCertificates tc : complList) {
            String state = tc.getTerritory();
            String tsp = tc.getTspName();
            String id = FnvHash.getFNV1aToHex(state + tsp);
            if (id.equals(parameter)) {
                if (!blockCertIds.contains(tc.getTslCertHash())) {
                    blockCertIds.add(tc.getTslCertHash());
                }
            }
        }
        policyDb.addOrReplaceValidationPolicy(vp, true);
    }
}
