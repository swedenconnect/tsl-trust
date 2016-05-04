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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;

/**
 * Abstract class for hibernate utility.
 * A separate hibernate utility class extends this class for each database
 * All hibernate utility classes must extend this abstract class.
 */
public abstract class HibernateUtil {
//    protected static final String LOG_CFG = "log_hibernate.cfg.xml";
//    protected static final String POLICY_CFG = "policy_hibernate.cfg.xml";

    protected static SessionFactory logSessionFactory = null;
    protected static SessionFactory policySessionFactory = null;
    protected static final Logger LOG = Logger.getLogger(LogHibernateUtil.class.getName());

    public static boolean initSessionFactories() {
        boolean success = true;
        try {
            Document logConfig = HibernateConfigFactory.getLogHibernateConfigDoc();
            if (logConfig != null) {
                logSessionFactory = new Configuration().configure(logConfig).buildSessionFactory();
            } else {
                success = false;
            }

            Document policyConfig = HibernateConfigFactory.getPolicyHibernateConfigDoc();
            if (policyConfig != null) {
                policySessionFactory = new Configuration().configure(policyConfig).buildSessionFactory();
            } else {
                success = false;
            }

        } catch (Exception ex) {
            success=false;
        }
        return success;
    }

    /**
     * Get Hibernate session
     * @return Hibernate Session
     * @throws IOException 
     */
    public abstract SessionFactory getSessionFactory() throws HibernateException;

    /**
     * Shutdown session. This is not necessary when session has been obtained by hibernate
     */
    public abstract void shutdown();
}
