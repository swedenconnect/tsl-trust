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
package se.tillvaxtverket.tsltrust.weblogic.content;

import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCert;
import se.tillvaxtverket.tsltrust.weblogic.data.ExternalCert;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import se.tillvaxtverket.tsltrust.weblogic.db.CaSQLiteUtil;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDb;
import se.tillvaxtverket.tsltrust.weblogic.db.ValPoliciesDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.ASN1Util;
import se.tillvaxtverket.tsltrust.weblogic.utils.ExtractorUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.HtmlUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.PolicyUtils;
import iaik.x509.X509Certificate;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Providing UI elements for the certificate management menu
 */
public class CertManagementInfoElements implements HtmlConstants, TTConstants {

    private static final Logger LOG = Logger.getLogger(CertManagementInfoElements.class.getName());
    private PolicyUtils policyUtils;
    private ValPoliciesDbUtil policyDb;
    private TslCertDb tslCertDb;
    private InfoTableElements issuedCertsElements;
    private InfoTableElements pendingCertsElements;
    private InfoTableElements pendingRevElements;
    private CertificateInformation certInfo;
    private Map<BigInteger, List<TslCertificates>> tslCertMap;
    private Map<BigInteger, DbCert> issuedCertsMap;
    private Map<BigInteger, X509Certificate> externalCertMap;
    private Map<String, Map<String, List<BigInteger>>> listedCertifiedMap, listedPendingMap, listedRevMap;
    private List<BigInteger> unlistedIssuedCerts, unlistedPendingRev, unlistedPendingCert;
    private Map<String, Integer> issuedItems, pendingItems, revItems;
    private int sumIssued, sumPending, sumRev;
    TslTrustModel model;

    /**
     * This class provides methods for building UI component in the policy
     * configuration menu of TSL Trust
     *
     * @param model The TslTrustModel object holding essential application
     * parameters and objects
     */
    public CertManagementInfoElements(TslTrustModel model) {
        this.model = model;
        policyDb = model.getPolicyDb();
        tslCertDb = model.getTslCertDb();
        policyUtils = new PolicyUtils(model);

        //init fields 
        issuedCertsMap = new HashMap<BigInteger, DbCert>();
        tslCertMap = new HashMap<BigInteger, List<TslCertificates>>();
        listedCertifiedMap = new HashMap<String, Map<String, List<BigInteger>>>();
        listedPendingMap = new HashMap<String, Map<String, List<BigInteger>>>();
        listedRevMap = new HashMap<String, Map<String, List<BigInteger>>>();
        unlistedPendingCert = new ArrayList<BigInteger>();
        unlistedPendingRev = new ArrayList<BigInteger>();
        unlistedIssuedCerts = new ArrayList<BigInteger>();
        externalCertMap = new HashMap<BigInteger, X509Certificate>();

        //Init item counts;
        issuedItems = new HashMap<String, Integer>();
        pendingItems = new HashMap<String, Integer>();
        revItems = new HashMap<String, Integer>();
    }

    /**
     * Generates the information table for presenting validation policy
     * information and for providing the validation policy administrative
     * actions
     *
     * @param valPolicy the selected validation policy
     * @param req http request model data
     * @return html element holding the validation policy information and
     * functions.
     */
    public HtmlElement getValPolicyInfoTable(ValidationPolicy valPolicy, RequestModel req) {
        getCurrentStatus(valPolicy);

        LOG.info("Getting certificate management table...");

        //Basic Strtucture
        InfoTableModel tm = new InfoTableModel("cm" + FnvHash.getFNV1aToHex(valPolicy.getPolicyName()));
        tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
        InfoTableElements certMgmntElements = tm.getElements();

        certInfo = new CertificateInformation(tm, req.getSession());

        //Certified services Node
        if (!issuedItems.isEmpty() || !unlistedIssuedCerts.isEmpty()) {
            String cntTxt = " (" + String.valueOf(sumIssued + unlistedIssuedCerts.size()) + ")";
            InfoTableSection issuedCertsNode = certMgmntElements.addNewSection(tm, "Issued Certificates", true);
            issuedCertsNode.setFoldedElement("Current Compliant Certified Services" + cntTxt);
            issuedCertsNode.setKeepFoldableElement(true);
            issuedCertsElements = issuedCertsNode.getElements();
            buildCertMgmntNodes(tm, issuedCertsElements, listedCertifiedMap, issuedItems, unlistedIssuedCerts);
        }

        //Pending Certification services Node
        if (!listedPendingMap.isEmpty() || !unlistedPendingCert.isEmpty()) {
            String cntTxt = " (" + String.valueOf(sumPending + unlistedPendingCert.size()) + ")";
            InfoTableSection pendingCertNode = certMgmntElements.addNewSection(tm, "Pending Issue", true);
            pendingCertNode.setFoldedElement("Services queued for certification" + cntTxt);
            pendingCertNode.setKeepFoldableElement(true);
            pendingCertsElements = pendingCertNode.getElements();
            buildCertMgmntNodes(tm, pendingCertsElements, listedPendingMap, pendingItems, unlistedPendingCert);
        }

        //Pending Revocation Node
        if (!listedRevMap.isEmpty() || !unlistedPendingRev.isEmpty()) {
            String cntTxt = " (" + String.valueOf(sumRev + unlistedPendingRev.size()) + ")";
            InfoTableSection pendingRevocationNode = certMgmntElements.addNewSection(tm, "Pending Revoke", true);
            pendingRevocationNode.setFoldedElement("Services queued for revocation" + cntTxt);
            pendingRevocationNode.setKeepFoldableElement(true);
            pendingRevElements = pendingRevocationNode.getElements();
            buildCertMgmntNodes(tm, pendingRevElements, listedRevMap, revItems, unlistedPendingRev);
        }

        InfoTableFactory fact = new InfoTableFactory(tm, req.getSession());
        return fact.getTable();
    }

    private void buildCertMgmntNodes(InfoTableModel tm, InfoTableElements elements,
            Map<String, Map<String, List<BigInteger>>> listedCertMap,
            Map<String, Integer> items, List<BigInteger> unlistedCertIds) {

        InfoTableSection issuedTslSect = getSection(tm, elements, "TSL supported trust services", true);
        getListedServices(tm, issuedTslSect, listedCertMap, items);

        String cnt = HtmlUtil.countServices(unlistedCertIds.size());
        InfoTableSection issuedUnlistSect = getSection(tm, elements, "Additional trust services" + cnt, true);
        getUnlistedServices(tm, issuedUnlistSect, unlistedCertIds);
    }

    private InfoTableSection getSection(InfoTableModel tm, InfoTableElements elm, String foldHeading, boolean fold) {
        InfoTableSection sect = elm.addNewSection(tm, fold);
        sect.setFoldedElement(foldHeading);
        sect.setKeepFoldableElement(true);
        sect.setSectionHeadingClasses(new String[]{TABLE_UNFOLD_BOLD, TABLE_UNFOLD_BOLD});
        return sect;
    }

    private void getListedServices(InfoTableModel tm, InfoTableSection section,
            Map<String, Map<String, List<BigInteger>>> countryMap, Map<String, Integer> cntMap) {
        InfoTableElements elements = section.getElements();
        List<String> countries = getSorted(countryMap.keySet());
        // Get certs by country
        String label;
        for (String country : countries) {
            label = country + HtmlUtil.countServices(cntMap.get("0" + country));
            InfoTableElements countryElm = getSection(tm, elements, label, true).getElements();
            Map<String, List<BigInteger>> tspMap = countryMap.get(country);
            List<String> tspList = getSorted(tspMap.keySet());
            // Get certs by Trust Service Provider
            for (String tsp : tspList) {
                label = tsp + HtmlUtil.countServices(cntMap.get("1" + tsp));
                InfoTableElements tspElm = getSection(tm, countryElm, label, false).getElements();
                List<BigInteger> certIdList = tspMap.get(tsp);
                // Get individual certs
                for (BigInteger certId : certIdList) {
                    // Get the associated TslCertificates objeccts (Some certs may be associated with multiple services
                    try {
                        List<TslCertificates> tcList = tslCertMap.get(certId);
                        X509Certificate cert = getCert(tcList.get(0));
                        String name = ASN1Util.getShortCertName(getCert(tcList.get(0)));
                        InfoTableElements certElm = getSection(tm, tspElm, name, false).getElements();
                        InfoTableSection infoSect = certElm.addNewSection(tm, "Service Info", false);
                        for (TslCertificates tc : tcList) {
                            infoSect.addNewElement(new String[]{"Service Name", tc.getTsName()}, new String[]{ATTRIBUTE_NAME, ATTRIBUTE_VALUE});
                            infoSect.addNewElement(new String[]{"Service Type", ExtractorUtil.stripRefUrl(tc.getTrustServiceType())}, new String[]{ATTRIBUTE_NAME, ATTRIBUTE_VALUE});
                            infoSect.addNewElement(new String[]{"Service Status", ExtractorUtil.stripRefUrl(tc.getServiceStatus())}, new String[]{ATTRIBUTE_NAME, ATTRIBUTE_VALUE});
                        }
                        InfoTableSection tslCertSect = certElm.addNewSection(tm, "TSL Cert", false);
                        tslCertSect.setFoldedElement("TSL Service digital identity Certificate");
                        tslCertSect.setAjaxContentId("tsl" + certId.toString());
                        if (issuedCertsMap.containsKey(certId)) {
                            InfoTableSection xCertSect = certElm.addNewSection(tm, "Policy Cert", false);
                            xCertSect.setAjaxContentId("iss" + certId.toString());
                            xCertSect.setFoldedElement("Policy Certificate issued by TSL Trust");
                        }
                    } catch (Exception ex) {
                    }

                }
            }
        }
    }

    private void getUnlistedServices(InfoTableModel tm, InfoTableSection section, List<BigInteger> certIds) {
        InfoTableElements elements = section.getElements();
        for (BigInteger certId : certIds) {
            if (externalCertMap.containsKey(certId)) {
                try {
                    X509Certificate cert = externalCertMap.get(certId);
                    String name = ASN1Util.getShortCertName(cert);
                    InfoTableElements extCertElm = getSection(tm, elements, name, false).getElements();
                    InfoTableSection tslCertSect = extCertElm.addNewSection(tm, "TSL Cert", false);
                    tslCertSect.setFoldedElement("Imported Service Certificate");
                    tslCertSect.setAjaxContentId("ext" + certId.toString());
                    if (issuedCertsMap.containsKey(certId)) {
                        InfoTableSection xCertSect = extCertElm.addNewSection(tm, "Policy Cert", false);
                        xCertSect.setAjaxContentId("iss" + certId.toString());
                        xCertSect.setFoldedElement("Policy Certificate issued by TSL Trust");
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Determines whether a validation policy is in sync with the current status
     * of issued policy certificates for the validation policy. The result is
     * stored into the session model data
     *
     * @param valPolicy the validation policy which status is to be checked
     * @param session http session model data
     */
    public void isPendingAction(ValidationPolicy valPolicy, SessionModel session) {

        List<TslCertificates> policyCompliantCerts = new LinkedList<TslCertificates>();
        List<DbCert> issuedUnrevCerts = new ArrayList<DbCert>();
        List<String> addCertIds = new ArrayList<String>();
        try {
            policyCompliantCerts = policyUtils.getPolicyCompliantCerts(valPolicy);
            issuedUnrevCerts = getPolicyTableData(valPolicy.getPolicyName());
            addCertIds = valPolicy.getAddCertIds();
        } catch (Exception ex) {
            session.setPolicyPending(valPolicy.getPolicyName(), true);
            return;
        }

        List<BigInteger> adKeyId = new LinkedList<BigInteger>();
        List<ExternalCert> externalCerts = policyDb.getExternalCerts();
        Map<BigInteger, String> pcKeyId = new HashMap<BigInteger, String>();
        for (TslCertificates tc : policyCompliantCerts) {
            pcKeyId.put(key(tc), tc.getTslCertHash());
        }
        for (ExternalCert exCert : externalCerts) {
            if (addCertIds.contains(exCert.getCertificateId())) {
                if (!pcKeyId.containsKey(key(exCert.getCert()))) {
                    adKeyId.add(key(exCert.getCert()));
                }
            }
        }

        // If number of certs, including external certs differ. abort
        if (pcKeyId.size() + adKeyId.size() != issuedUnrevCerts.size()) {
            session.setPolicyPending(valPolicy.getPolicyName(), true);
            return;
        }

        for (DbCert dbCert : issuedUnrevCerts) {
            BigInteger key = key(dbCert);
            if (!pcKeyId.containsKey(key) && !adKeyId.contains(key)) {
                session.setPolicyPending(valPolicy.getPolicyName(), true);
                return;
            }
        }
        session.setPolicyPending(valPolicy.getPolicyName(), false);
        return;
    }

    /**
     * Generates the statistical data for the validation policy.
     *
     * @param valPolicy the target validation policy
     */
    public void getCurrentStatus(ValidationPolicy valPolicy) {
        issuedCertsMap.clear();
        tslCertMap.clear();
        listedCertifiedMap.clear();
        listedPendingMap.clear();
        listedRevMap.clear();
        unlistedPendingCert.clear();
        unlistedPendingRev.clear();
        unlistedIssuedCerts.clear();
        externalCertMap.clear();

        //Init item counts;
        issuedItems.clear();
        pendingItems.clear();
        revItems.clear();
        sumIssued = 0;
        sumPending = 0;
        sumRev = 0;

        try {

            List<TslCertificates> policyCompliantCerts = policyUtils.getPolicyCompliantCerts(valPolicy);
            List<TslCertificates> allTslCertificate = tslCertDb.getAllTslCertificate(false);
            List<DbCert> issuedUnrevCerts = getPolicyTableData(valPolicy.getPolicyName());
            
            LOG.info("Certificate management: Certificate data retrieved from database");


            // Store all issued certificates in a map indexed by a public key hash
            for (DbCert dbCert : issuedUnrevCerts) {
                put(dbCert, issuedCertsMap);
            }
            // Store all TSL certificates in a map indexed by a public key hash
            for (TslCertificates tc : allTslCertificate) {
                put(tc, tslCertMap);
            }
            // Store all external certs in a map indexed by a public key hash
            // Store all compliant external certs in a list of cert keys
            List<BigInteger> complExtCertList = new LinkedList<BigInteger>();
            List<String> addCertIds = valPolicy.getAddCertIds();
            for (String certHash : valPolicy.getAddCertIds()) {
                X509Certificate cert = policyUtils.getIAIKCert(certHash);
                BigInteger certId =null;
                if (cert != null) {
                    certId = key(cert);
                    externalCertMap.put(certId, cert);
                }
                if (certId != null && addCertIds.contains(certHash)) {
                    listAdd(certId, complExtCertList);
                }
            }

            // Get valid services services
            BigInteger key;
            List<BigInteger> complTslCertList = new LinkedList<BigInteger>();
            for (TslCertificates tc : policyCompliantCerts) {
                key = key(tc);
                listAdd(key, complTslCertList);
                if (issuedCertsMap.containsKey(key)) {
                    putMap(tc, key, listedCertifiedMap, issuedItems);
                    sumIssued++;
                } else {
                    putMap(tc, key, listedPendingMap, pendingItems);
                    sumPending++;
                }
            }

            // Get external certs pending certificatioin
            Set<BigInteger> keySet = externalCertMap.keySet();
            for (BigInteger certKey : keySet) {
                // check that the external cert is not allready one of the TSL supported compliant certs 
                if (!complTslCertList.contains(certKey)) {
                    if (issuedCertsMap.containsKey(certKey)) {
                        unlistedIssuedCerts.add(certKey);
                    } else {
                        unlistedPendingCert.add(certKey);
                    }
                }
            }

            // Get services pending revoication
            for (DbCert dbCert : issuedUnrevCerts) {
                key = key(dbCert);
                // Check that the certificate is not policy compliant
                if (!complTslCertList.contains(key) && !complExtCertList.contains(key)) {
                    if (tslCertMap.containsKey(key)) {
                        //Add to listed TSL certificates that are pending revocatioin
                        List<TslCertificates> tcList = tslCertMap.get(key);
                        for (TslCertificates tc : tcList) {
                            putMap(tc, key, listedRevMap, revItems);
                            sumRev++;
                        }
                    } else {
                        // Add to unlisted certs (Certs no longer supported by a current TSL) for revocation
                        if (!externalCertMap.containsKey(key)) {
                            unlistedPendingRev.add(key);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Failed getting cert status", ex);
        }        
    }

    private List<DbCert> getPolicyTableData(String policyName) {
        List<DbCert> validCerts = new ArrayList<DbCert>();
        String caDir = model.getDataLocation() + "CA/" + policyName;
        List<DbCert> unrevCerts = CaSQLiteUtil.getCertificates(caDir, false);
        for (DbCert dbCert : unrevCerts) {
            if (!isExpired(dbCert.getCertificate())) {
                validCerts.add(dbCert);
            }
        }
        return validCerts;
    }

    private boolean isExpired(X509Certificate cert) {
        return cert.getNotAfter().before(new Date());
    }

    private BigInteger key(byte[] data) {
        return FnvHash.getFNV1a(data);
    }

    private BigInteger key(X509Certificate cert) {
        return key(cert.getPublicKey().getEncoded());
    }

    private BigInteger key(PublicKey publicKey) {
        return key(publicKey.getEncoded());
    }

    private BigInteger key(DbCert dbCert) {
        try {
            return key(dbCert.getCertificate());
        } catch (Exception ex) {
            return null;
        }
    }

    private BigInteger key(TslCertificates tc) {
        try {
            return key(CertificateUtils.getCertificate(tc.getTslCertificate()));
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean listAdd(BigInteger item, List<BigInteger> list) {
        if (list.contains(item)) {
            return false;
        }
        list.add(item);
        return true;
    }

    private boolean put(DbCert dbCert, Map<BigInteger, DbCert> map) {
        BigInteger key = key(dbCert);
        if (key == null) {
            return false;
        }
        map.put(key, dbCert);
        return true;
    }

    private boolean put(TslCertificates tc, Map<BigInteger, List<TslCertificates>> map) {
        List<TslCertificates> tcList = new LinkedList<TslCertificates>();
        BigInteger key = key(tc);
        if (key == null) {
            return false;
        }
        if (map.containsKey(key)) {
            tcList = map.get(key);
        }
        tcList.add(tc);
        map.put(key, tcList);
        return true;
    }

    private boolean putMap(TslCertificates tc, BigInteger key,
            Map<String, Map<String, List<BigInteger>>> listedMap, Map<String, Integer> cntMap) {
        Map<String, List<BigInteger>> tspCertMap = new HashMap<String, List<BigInteger>>();
        List<BigInteger> certList = new ArrayList<BigInteger>();
        String territory = tc.getTerritory();
        String tspName = tc.getTspName();
        if (noValue(territory) || noValue(tspName)) {
            return false;
        }
        if (listedMap.containsKey(territory)) {
            tspCertMap = listedMap.get(territory);
        }
        if (tspCertMap.containsKey(tspName)) {
            certList = tspCertMap.get(tspName);
        }
        certList.add(key);
        tspCertMap.put(tspName, certList);
        listedMap.put(territory, tspCertMap);

        //increment itemcount
        int terrCount = cntMap.containsKey("0" + territory) ? cntMap.get("0" + territory) : 0;
        int tspCount = cntMap.containsKey("1" + tspName) ? cntMap.get("1" + tspName) : 0;
        cntMap.put("0" + territory, terrCount + 1);
        cntMap.put("1" + tspName, tspCount + 1);

        return true;
    }

    private boolean noValue(String str) {
        return (str == null || str.length() == 0);
    }

    private List<String> getSorted(Set<String> stringSet) {
        List<String> sorted = new ArrayList<String>();
        for (String str : stringSet) {
            sorted.add(str);
        }
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Extracts the X509 Certificate from a TSL trust service database record
     *
     * @param tc the TSL trust service database record
     * @return X509 Certificate
     */
    public X509Certificate getCert(TslCertificates tc) {
        try {
            return CertificateUtils.getCertificate(tc.getTslCertificate());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Provides the X509 Certificate that is identified by a particular cert
     * hash
     *
     * @param refCertId a 64 bit FNV-1a hash of the target certificate
     * @return X509 Certificate identified by the refCertId
     */
    public X509Certificate getCert(String refCertId) {
        BigInteger certId = null;
        int type = 0;
        try {
            String typeStr = refCertId.substring(0, 3);
            String bigIntId = refCertId.substring(3);
            certId = new BigInteger(bigIntId);
            if (typeStr.equals("iss")) {
                type = 1;
            }
            if (typeStr.equals("ext")) {
                type = 2;
            }
        } catch (Exception ex) {
            return null;
        }
        switch (type) {
            case 1:
                if (issuedCertsMap.containsKey(certId)) {
                    return issuedCertsMap.get(certId).getCertificate();
                }
                break;
            case 2:
                if (externalCertMap.containsKey(certId)) {
                    return externalCertMap.get(certId);
                }
                break;
            default:
                if (tslCertMap.containsKey(certId)) {
                    return getCert(tslCertMap.get(certId).get(0));
                }
        }
        return null;
    }

    /**
     * Getter for the Map holding public key identifiers for all TSL
     * certificates
     *
     * @return Hash Map
     */
    public Map<String, Map<String, List<BigInteger>>> getListedCertifiedMap() {
        return listedCertifiedMap;
    }

    /**
     * Getter for a Map holding public key identifiers for all TSL certificates
     * that are pending certification by the policy CA
     *
     * @return Hash Map
     */
    public Map<String, Map<String, List<BigInteger>>> getListedPendingMap() {
        return listedPendingMap;
    }

    /**
     * Getter for a Map holding public key identifiers for all TSL certificates
     * that are pending revocation by the policy CA
     *
     * @return Hash Map
     */
    public Map<String, Map<String, List<BigInteger>>> getListedRevMap() {
        return listedRevMap;
    }

    /**
     * Getter for a list of public key identifiers for unlisted certificates
     * that are pending certification by the policy CA
     *
     * @return list of BigInteger identifiers (FNV-1a hash of public key binary
     * bytes)
     */
    public List<BigInteger> getUnlistedPendingCert() {
        return unlistedPendingCert;
    }

    /**
     * Getter for a list of public key identifiers for unlisted certificates
     * that are pending revocation by the policy CA
     *
     * @return list of BigInteger identifiers (FNV-1a hash of public key binary
     * bytes)
     */
    public List<BigInteger> getUnlistedPendingRev() {
        return unlistedPendingRev;
    }
}
