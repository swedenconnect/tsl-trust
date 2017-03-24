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
package se.tillvaxtverket.tsltrust.weblogic.hibernate;

import se.tillvaxtverket.tsltrust.weblogic.data.AdminLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.data.ExternalCert;
import se.tillvaxtverket.tsltrust.weblogic.data.MajorLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.data.TslPolicy;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;


/**
 * Factory for obtaining utility objects for hibernate database tables
 */
public class HibernateDbFactory {
    private static final HibernateUtil logUtil = new LogHibernateUtil();
    private static final HibernateUtil policyUtil = new PoilcyHibernateUtil();
    private static final HigernateDbUtil<AdminLogRecord> dbAdminLog = new HigernateDbUtil<AdminLogRecord>("logtime", "AdminLogRecord", logUtil);
    private static final HigernateDbUtil<MajorLogRecord> dbMajorLog = new HigernateDbUtil<MajorLogRecord>("logtime", "MajorLogRecord", logUtil);
    private static final HigernateDbUtil<ConsoleLogRecord> dbConsoleLog = new HigernateDbUtil<ConsoleLogRecord>("logtime", "ConsoleLogRecord", logUtil);
    private static final HigernateDbUtil<ValidationPolicy> dbValidationPolicy = new HigernateDbUtil<ValidationPolicy>("name", "ValidationPolicy", policyUtil);
    private static final HigernateDbUtil<TslPolicy> dbTslPolicy = new HigernateDbUtil<TslPolicy>("name", "TslPolicy", policyUtil);
    private static final HigernateDbUtil<ExternalCert> dbExternalCertificates = new HigernateDbUtil<ExternalCert>("certId", "ExternalCert", policyUtil);

    /**
     * @return Admin log database
     */
    public static HigernateDbUtil<AdminLogRecord> getDbAdminLog() {
        return dbAdminLog;
    }

    /**
     * @return Console log database
     */
    public static HigernateDbUtil<ConsoleLogRecord> getDbConsoleLog() {
        return dbConsoleLog;
    }

    /**
     * @return External certificate Database
     */
    public static HigernateDbUtil<ExternalCert> getDbExternalCertificates() {
        return dbExternalCertificates;
    }

    /**
     * @return  Major event log database
     */
    public static HigernateDbUtil<MajorLogRecord> getDbMajorEventLog() {
        return dbMajorLog;
    }

    /**
     * @return TslPolicyDatabase
     */
    public static HigernateDbUtil<TslPolicy> getDbTlsPolicy() {
        return dbTslPolicy;
    }

    /**
     * @return ValidationPolicyDatabase
     */
    public static HigernateDbUtil<ValidationPolicy> getDbValidationPolicy() {
        return dbValidationPolicy;
    }        
}
