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
package se.tillvaxtverket.tsltrust.weblogic.db;

/**
 * Utility class for access to validation policy database records
 */
import se.tillvaxtverket.tsltrust.weblogic.data.TslPolicy;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import se.tillvaxtverket.tsltrust.weblogic.data.ExternalCert;
import java.util.List;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HibernateDbFactory;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HigernateDbUtil;

public class ValPoliciesDbUtil {

    private HigernateDbUtil<ValidationPolicy> dbVp = HibernateDbFactory.getDbValidationPolicy();
    private HigernateDbUtil<TslPolicy> dbTp = HibernateDbFactory.getDbTlsPolicy();
    private HigernateDbUtil<ExternalCert> dbEc = HibernateDbFactory.getDbExternalCertificates();

    public ValPoliciesDbUtil() {
    }

    public void addOrReplaceValidationPolicy(ValidationPolicy valPolicy, boolean replace) {
        dbVp.saveRecord(valPolicy);
    }

    public void addOrReplaceTslPolicy(TslPolicy tslPolicy, boolean replace) {
        dbTp.saveRecord(tslPolicy);
    }

    public void addOrReplaceCert(ExternalCert extCert, boolean replace) {
        dbEc.saveRecord(extCert);
    }

    public List<ValidationPolicy> getValidationPolicies() {
        return dbVp.getAllRecords();
    }

    public List<TslPolicy> getTslPolicies() {
        return dbTp.getAllRecords();
    }

    public List<ExternalCert> getExternalCerts() {
        return dbEc.getAllRecords();
    }

    public int deteleRecord(ValidationPolicy vp) {
        return dbVp.deleteRecords(vp.getPolicyName());
    }

    public int deteleRecord(TslPolicy tp) {
        return dbTp.deleteRecords(tp.getTslPolicyName());
    }

    public int deteleRecord(ExternalCert ec) {
        return dbEc.deleteRecords(ec.getCertificateId());
    }
}
