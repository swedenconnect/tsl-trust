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
