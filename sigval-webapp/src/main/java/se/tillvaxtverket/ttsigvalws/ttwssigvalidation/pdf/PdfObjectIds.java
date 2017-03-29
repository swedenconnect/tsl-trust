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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

/**
 *
 * @author stefan
 */
public class PdfObjectIds {

    public static final String ID_PKCS7_DATA = "1.2.840.113549.1.7.1";
    public static final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
    public static final String ID_RSA = "1.2.840.113549.1.1.1";
    public static final String ID_DSA = "1.2.840.10040.4.1";
    public static final String ID_ECDSA = "1.2.840.10045.2.1";
    public static final String ID_CONTENT_TYPE = "1.2.840.113549.1.9.3";
    public static final String ID_MESSAGE_DIGEST = "1.2.840.113549.1.9.4";
    public static final String ID_SIGNING_TIME = "1.2.840.113549.1.9.5";
    public static final String ID_ADBE_REVOCATION = "1.2.840.113583.1.1.8";
    public static final String ID_TSA = "1.2.840.113583.1.1.9.1";
    public static final String ID_OCSP = "1.3.6.1.5.5.7.48.1";
    public static final String ID_AA_SIGNING_CERTIFICATE_V1 = "1.2.840.113549.1.9.16.2.12";
    public static final String ID_AA_SIGNING_CERTIFICATE_V2 = "1.2.840.113549.1.9.16.2.47";
    public static final String ID_AA_CMS_ALGORITHM_PROTECTION = "1.2.840.113549.1.9.52";
    
    //Eliptic curves
    public static final String ID_EC_P256 = "1.2.840.10045.3.1.7";
}
