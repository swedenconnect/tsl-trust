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
