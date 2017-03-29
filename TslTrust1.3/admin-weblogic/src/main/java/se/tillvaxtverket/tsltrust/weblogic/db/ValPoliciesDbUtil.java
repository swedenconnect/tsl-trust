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
