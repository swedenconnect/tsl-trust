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
package se.tillvaxtverket.tsltrust.admin.maint.ca.utils;

/**
 * Constants for CA operations
 */
public interface CaKeyStoreConstants {
  public final static String ROOT             = "ROOT_CA";
  public final static String RSA_1024         = "RSA.1024";
  public final static String KS_FILEEXT       = ".keystore";
  public final static char[] KS_PASSWORD      = "topSecret".toCharArray();
  public final static String CERT_SERIAL_KEY  = "CertSerial";
  public final static String CRL_SERIAL_KEY   = "CRLSerial";
  public final static int ISSUE_EVENT         = 1;
  public final static int REVOKE_EVENT        = 2;
  public final static String[] REV_REASON     = new String[] {"unspecified", "keyCompromise", "cACompromise", 
      "affiliationChanged", "superseded", "cessationOfOperation", "certificateHold", "not used", "removeFromCRL",
      "privilegeWithdrawn", "aACompromise"};
}
