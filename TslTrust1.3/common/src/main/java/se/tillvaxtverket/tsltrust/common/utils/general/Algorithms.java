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
package se.tillvaxtverket.tsltrust.common.utils.general;

import java.util.HashMap;

/**
 * Algorithm identifier class
 */
public class Algorithms {

    /**
     * Mapping hash OID to algorithm names
     */
    public static final HashMap<String, String> digestNames = new HashMap<String, String>();
    /**
     * Mapping signature algorithm OID to public key algorithm names 
     */
    public static final HashMap<String, String> algorithmNames = new HashMap<String, String>();
    /**
     * Mapping signature algorithm OID to algorithm names 
     */
    public static final HashMap<String, String> sigAlgorithmNames = new HashMap<String, String>();
    /**
     * Mapping public key algorithm OID to algorithm names 
     */
    public static final HashMap<String, String> pkAlgorithmNames = new HashMap<String, String>();
    /**
     * Mapping recognized hash algorithm names to corresponding OID 
     */
    public static final HashMap<String, String> allowedDigests = new HashMap<String, String>();
    /**
     * Mapping XML signature algorithm identifiers to corresponding OIDs 
     */
    public static final HashMap<String, String> xmlAlgIds = new HashMap<String, String>();
    /**
     * Mapping supported signature algorithm OID to algorithm names 
     */
    public static final HashMap<String, String> supportedSigAlgs = new HashMap<String, String>();

    static {
        digestNames.put("1.2.840.113549.2.5", "MD5");
        digestNames.put("1.2.840.113549.2.2", "MD2");
        digestNames.put("1.3.14.3.2.26", "SHA1");
        digestNames.put("2.16.840.1.101.3.4.2.4", "SHA224");
        digestNames.put("2.16.840.1.101.3.4.2.1", "SHA256");
        digestNames.put("2.16.840.1.101.3.4.2.2", "SHA384");
        digestNames.put("2.16.840.1.101.3.4.2.3", "SHA512");
        digestNames.put("1.3.36.3.2.2", "RIPEMD128");
        digestNames.put("1.3.36.3.2.1", "RIPEMD160");
        digestNames.put("1.3.36.3.2.3", "RIPEMD256");
        digestNames.put("1.2.840.113549.1.1.4", "MD5");
        digestNames.put("1.2.840.113549.1.1.2", "MD2");
        digestNames.put("1.2.840.113549.1.1.5", "SHA1");
        digestNames.put("1.2.840.113549.1.1.14", "SHA224");
        digestNames.put("1.2.840.113549.1.1.11", "SHA256");
        digestNames.put("1.2.840.113549.1.1.12", "SHA384");
        digestNames.put("1.2.840.113549.1.1.13", "SHA512");
        digestNames.put("1.2.840.113549.2.5", "MD5");
        digestNames.put("1.2.840.113549.2.2", "MD2");
        digestNames.put("1.2.840.10040.4.3", "SHA1");
        digestNames.put("2.16.840.1.101.3.4.3.1", "SHA224");
        digestNames.put("2.16.840.1.101.3.4.3.2", "SHA256");
        digestNames.put("2.16.840.1.101.3.4.3.3", "SHA384");
        digestNames.put("2.16.840.1.101.3.4.3.4", "SHA512");
        digestNames.put("1.3.36.3.3.1.3", "RIPEMD128");
        digestNames.put("1.3.36.3.3.1.2", "RIPEMD160");
        digestNames.put("1.3.36.3.3.1.4", "RIPEMD256");

        algorithmNames.put("1.2.840.113549.1.1.1", "RSA");
        algorithmNames.put("1.2.840.10040.4.1", "DSA");
        algorithmNames.put("1.2.840.113549.1.1.2", "RSA");
        algorithmNames.put("1.2.840.113549.1.1.4", "RSA");
        algorithmNames.put("1.2.840.113549.1.1.5", "RSA");
        algorithmNames.put("1.2.840.113549.1.1.14", "RSA");
        algorithmNames.put("1.2.840.113549.1.1.11", "RSA");
        algorithmNames.put("1.2.840.113549.1.1.12", "RSA");
        algorithmNames.put("1.2.840.113549.1.1.13", "RSA");
        algorithmNames.put("1.2.840.10040.4.3", "DSA");
        algorithmNames.put("2.16.840.1.101.3.4.3.1", "DSA");
        algorithmNames.put("2.16.840.1.101.3.4.3.2", "DSA");
        algorithmNames.put("1.3.36.3.3.1.3", "RSA");
        algorithmNames.put("1.3.36.3.3.1.2", "RSA");
        algorithmNames.put("1.3.36.3.3.1.4", "RSA");

        pkAlgorithmNames.put("1.2.840.113549.1.1.1", "RSA");
        pkAlgorithmNames.put("1.2.840.10040.4.1", "DSA");

        sigAlgorithmNames.put("1.2.840.113549.1.1.2", "RSA with MD2");
        sigAlgorithmNames.put("1.2.840.113549.1.1.4", "RSA with MD5");
        sigAlgorithmNames.put("1.2.840.113549.1.1.5", "RSA with SHA1");
        sigAlgorithmNames.put("1.2.840.113549.1.1.14", "RSA with SHA224");
        sigAlgorithmNames.put("1.2.840.113549.1.1.11", "RSA with SHA256");
        sigAlgorithmNames.put("1.2.840.113549.1.1.12", "RSA with SHA384");
        sigAlgorithmNames.put("1.2.840.113549.1.1.13", "RSA with SHA512");
        sigAlgorithmNames.put("1.2.840.10040.4.3", "DSA with SHA1");
        sigAlgorithmNames.put("2.16.840.1.101.3.4.3.1", "DSA with SHA224");
        sigAlgorithmNames.put("2.16.840.1.101.3.4.3.2", "DSA with SHA256");
        sigAlgorithmNames.put("1.3.36.3.3.1.3", "RSA with RIPEMD128");
        sigAlgorithmNames.put("1.3.36.3.3.1.2", "RSA with RIPEMD160");
        sigAlgorithmNames.put("1.3.36.3.3.1.4", "RSA with RIPEMD256");

        allowedDigests.put("MD5", "1.2.840.113549.2.5");
        allowedDigests.put("MD2", "1.2.840.113549.2.2");
        allowedDigests.put("SHA1", "1.3.14.3.2.26");
        allowedDigests.put("SHA224", "2.16.840.1.101.3.4.2.4");
        allowedDigests.put("SHA256", "2.16.840.1.101.3.4.2.1");
        allowedDigests.put("SHA384", "2.16.840.1.101.3.4.2.2");
        allowedDigests.put("SHA512", "2.16.840.1.101.3.4.2.3");
        allowedDigests.put("MD-5", "1.2.840.113549.2.5");
        allowedDigests.put("MD-2", "1.2.840.113549.2.2");
        allowedDigests.put("SHA-1", "1.3.14.3.2.26");
        allowedDigests.put("SHA-224", "2.16.840.1.101.3.4.2.4");
        allowedDigests.put("SHA-256", "2.16.840.1.101.3.4.2.1");
        allowedDigests.put("SHA-384", "2.16.840.1.101.3.4.2.2");
        allowedDigests.put("SHA-512", "2.16.840.1.101.3.4.2.3");
        allowedDigests.put("RIPEMD128", "1.3.36.3.2.2");
        allowedDigests.put("RIPEMD-128", "1.3.36.3.2.2");
        allowedDigests.put("RIPEMD160", "1.3.36.3.2.1");
        allowedDigests.put("RIPEMD-160", "1.3.36.3.2.1");
        allowedDigests.put("RIPEMD256", "1.3.36.3.2.3");
        allowedDigests.put("RIPEMD-256", "1.3.36.3.2.3");
        
        xmlAlgIds.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "1.2.840.113549.1.1.11");
        xmlAlgIds.put("http://www.w3.org/2001/04/xmldsig-more#rsa-md5", "1.2.840.113549.1.1.2");
        xmlAlgIds.put("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "1.2.840.113549.1.1.5");
        xmlAlgIds.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", "1.2.840.113549.1.1.12");
        xmlAlgIds.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "1.2.840.113549.1.1.13");
        xmlAlgIds.put("http://www.w3.org/2000/09/xmldsig#dsa-sha1", "1.2.840.10040.4.3");
        xmlAlgIds.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1", "1.2.840.10045.4.1");
        xmlAlgIds.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224", "1.2.840.10045.4.3.1");
        xmlAlgIds.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", "1.2.840.10045.4.3.2");
        
        supportedSigAlgs.put("1.2.840.113549.1.1.5", "RSA with SHA1");
        supportedSigAlgs.put("1.2.840.113549.1.1.11", "RSA with SHA256");
        
    }
}
