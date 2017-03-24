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
package se.tillvaxtverket.tsltrust.common.tsl.sie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.CriteriaListType;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.CriteriaListType.Assert;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.KeyUsageBitType;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.KeyUsageType;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.PoliciesListType;
import org.etsi.uri.x01903.v13.AnyType;
import org.etsi.uri.x01903.v13.DocumentationReferencesType;
import org.etsi.uri.x01903.v13.IdentifierType;
import org.etsi.uri.x01903.v13.ObjectIdentifierType;

/**
 *
 * @author stefan
 */
public class QualificationsCriteria {

    CriteriaListType cl;
    String assStr;
    String description;
    AnyType otherCriteriaList;
    List<List<String>> keyUsages = new ArrayList<List<String>>();
    List<List<PolicyOid>> policyList = new ArrayList<List<PolicyOid>>();
    List<QualificationsCriteria> criteriaList = new ArrayList<QualificationsCriteria>();

    public QualificationsCriteria(CriteriaListType cl) {
        this.cl = cl;
        parse();
    }

    /**
     * Assertion
     * @return Text representation of the assertion
     */
    public String getAssStr() {
        return assStr;
    }

    /**
     * Getter for the criteria element processed in the present class object.
     * @return Criteria element
     */
    public CriteriaListType getCriteriaListElement() {
        return cl;
    }

    /**
     * Getter for any present Criteria list stored in this criteria list.
     * @return Criteria list
     */
    public List<QualificationsCriteria> getCriteriaList() {
        return criteriaList;
    }

    /**
     * Getter for the description of this criteria
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for key usage data.
     * @return Key usage data.
     */
    public List<List<String>> getKeyUsages() {
        return keyUsages;
    }

    /**
     * Getter for extensible AnyType element
     * @return AnyType element
     */
    public AnyType getOtherCriteriaList() {
        return otherCriteriaList;
    }

    /**
     * Getter for policy information
     * @return A list of list of policy OID information
     */
    public List<List<PolicyOid>> getPolicyList() {
        return policyList;
    }

    private void parse() {
        assStr = null;
        try {
            int ass = cl.getAssert().intValue();
            switch (ass) {
                case Assert.INT_ALL:
                    assStr = "All";
                    break;
                case Assert.INT_AT_LEAST_ONE:
                    assStr = "At least one";
                    break;
                case Assert.INT_NONE:
                    assStr = "None";
            }
        } catch (Exception ex) {
        }

        description = cl.getDescription();
        otherCriteriaList = cl.getOtherCriteriaList();

        try {
            List<String> keyUsageBits = new ArrayList<String>();
            KeyUsageType[] keyUsageArray = cl.getKeyUsageArray();
            for (KeyUsageType ku : keyUsageArray) {
                KeyUsageBitType[] keyUsageBitArray = ku.getKeyUsageBitArray();
                for (KeyUsageBitType kub : keyUsageBitArray) {
                    String keyUsageName = kub.getName().toString();
                    if (kub.getBooleanValue()) {
                        keyUsageBits.add(keyUsageName);
                    }
                }
                keyUsages.add(keyUsageBits);
            }
        } catch (Exception ex) {
        }


        try {
            PoliciesListType[] policySetArray = cl.getPolicySetArray();
            for (PoliciesListType policy : policySetArray) {
                List<PolicyOid> policyOidList = new ArrayList<PolicyOid>();
                ObjectIdentifierType[] policyIdentifierArray = policy.getPolicyIdentifierArray();
                for (ObjectIdentifierType policyOid : policyIdentifierArray) {
                    policyOidList.add(new PolicyOid(policyOid));
                }
                policyList.add(policyOidList);
            }
        } catch (Exception ex) {
        }

        try {
            CriteriaListType[] criteriaListArray = cl.getCriteriaListArray();
            for (CriteriaListType criteria : criteriaListArray) {
                criteriaList.add(new QualificationsCriteria(criteria));
            }
        } catch (Exception ex) {
        }

    }

    /**
     * Class holding policy information in a criteria element of a Sie Extension
     */
    public class PolicyOid {

        private ObjectIdentifierType oid;
        private String oidDescription;
        private String identifierUri;
        private List<String> documentationRef = new ArrayList<String>();

        /**
         * Constructor
         * @param oid The OID object
         */
        public PolicyOid(ObjectIdentifierType oid) {
            this.oid = oid;
            parse();
        }

        private void parse() {
            oidDescription = oid.getDescription();

            try {
                DocumentationReferencesType documentationReferences = oid.getDocumentationReferences();
                String[] documentationReferenceArray = documentationReferences.getDocumentationReferenceArray();
                documentationRef.addAll(Arrays.asList(documentationReferenceArray));
            } catch (Exception ex) {
            }

            try {
                IdentifierType identifier = oid.getIdentifier();
                identifierUri = identifier.getStringValue();
            } catch (Exception ex) {
            }

        }

        /**
         * Getter for documentation references
         * @return Documentations references
         */
        public List<String> getDocumentationRef() {
            return documentationRef;
        }

        /**
         * Getter for identifier URI
         * @return identifier URI
         */
        public String getIdentifierUri() {
            return identifierUri;
        }

        /**
         * Getter for OID description
         * @return OID description
         */
        public String getOidDescription() {
            return oidDescription;
        }

        /**
         * Getter for the OID data element
         * @return OID data element
         */
        public ObjectIdentifierType getOidIdentifierElement() {
            return oid;
        }
    }
}
