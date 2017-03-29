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
package se.tillvaxtverket.tsltrust.common.utils.core;

import java.math.BigInteger;

/**
 * FNV hash functions
 */
public class FnvHash {

    /**
     * 64 bit FNV-1 hash function.
     * This method correctly hash strings of data where each character is represented by one octet.
     * Any bits more significant than bit 0-7 are ignored.
     * If multi byte characters are used, the input should be provided as a byte array
     * @param inpString
     * The data to hash
     * @return
     * A 64 bit integer representation of the FNV-1 hash
     */
    static public BigInteger getFNV1(String inpString) {
        BigInteger fnvPrime = new BigInteger("1099511628211");
        BigInteger fnvOffsetBasis = new BigInteger("14695981039346656037");
        BigInteger m = new BigInteger("18446744073709551616");

        BigInteger digest = fnvOffsetBasis;

        for (int i = 0; i < inpString.length(); i++) {
            digest = digest.multiply(fnvPrime).mod(m);
            digest = digest.xor(BigInteger.valueOf((int) inpString.substring(i, i + 1).charAt(0) & 255));
        }
        return (digest);
    }

    /**
     * 64 bit FNV-1a hash function.
     * This method correctly hash strings of data where each character is represented by one octet.
     * Any bits more significant than bit 0-7 are ignored.
     * If multi byte characters are used, the input should be provided as a byte array
     * @param inpString
     * The data to hash
     * @return
     * A 64 bit integer representation of the FNV-1a hash
     */
    static public BigInteger getFNV1a(String inpString) {
        BigInteger m = new BigInteger("2").pow(64);
        BigInteger fnvPrime = new BigInteger("1099511628211");
        BigInteger fnvOffsetBasis = new BigInteger("14695981039346656037");

        BigInteger digest = fnvOffsetBasis;

        for (int i = 0; i < inpString.length(); i++) {
            digest = digest.xor(BigInteger.valueOf((int) inpString.substring(i, i + 1).charAt(0) & 255));
            digest = digest.multiply(fnvPrime).mod(m);
        }
        return (digest);
    }

    /**
     * 64 bit FNV-1a hash function.      *
     * @param inp
     * The data to hash
     * @return
     * BigInteger holding the FNV-1a hash
     */
    static public BigInteger getFNV1a(byte[] inp) {
        BigInteger m = new BigInteger("2").pow(64);
        BigInteger fnvPrime = new BigInteger("1099511628211");
        BigInteger fnvOffsetBasis = new BigInteger("14695981039346656037");

        BigInteger digest = fnvOffsetBasis;

        for (byte b : inp) {
            digest = digest.xor(BigInteger.valueOf((int) b & 255));
            digest = digest.multiply(fnvPrime).mod(m);
        }
        return digest;
    }

    /**
     * 64 bit FNV-1 hash function.
     * This method correctly hash strings of data where each character is represented by one octet.
     * Any bits more significant than bit 0-7 are ignored.
     * If multi byte characters are used, the input should be provided as a byte array
     * @param inpString
     * The data to hash
     * @return
     * A String holding the hex representation of the FNV-1 hash
     */
    static public String getFNV1ToHex(String inpString) {
        BigInteger fnvPrime = new BigInteger("1099511628211");
        BigInteger fnvOffsetBasis = new BigInteger("14695981039346656037");
        BigInteger m = new BigInteger("18446744073709551616");

        BigInteger digest = fnvOffsetBasis;

        for (int i = 0; i < inpString.length(); i++) {
            digest = digest.multiply(fnvPrime).mod(m);
            digest = digest.xor(BigInteger.valueOf((int) inpString.substring(i, i + 1).charAt(0) & 255));
        }
        return padHexString(digest);
    }

    /**
     * 64 bit FNV-1a hash function.
     * This method correctly hash strings of data where each character is represented by one octet.
     * Any bits more significant than bit 0-7 are ignored.
     * If multi byte characters are used, the input should be provided as a byte array
     * @param inpString
     * The data to hash
     * @return
     * A String holding the hex representation of the FNV-1a hash
     */
    static public String getFNV1aToHex(String inpString) {
        BigInteger m = new BigInteger("2").pow(64);
        BigInteger fnvPrime = new BigInteger("1099511628211");
        BigInteger fnvOffsetBasis = new BigInteger("14695981039346656037");

        BigInteger digest = fnvOffsetBasis;

        for (int i = 0; i < inpString.length(); i++) {
            digest = digest.xor(BigInteger.valueOf((int) inpString.substring(i, i + 1).charAt(0) & 255));
            digest = digest.multiply(fnvPrime).mod(m);
        }
        return padHexString(digest);
    }

    /**
     * 64 bit FNV-1a hash function.      *
     * @param inp
     * The data to hash
     * @return
     * A String holding the hex representationof the FNV-1a hash
     */
    static public String getFNV1aToHex(byte[] inp) {
        BigInteger m = new BigInteger("2").pow(64);
        BigInteger fnvPrime = new BigInteger("1099511628211");
        BigInteger fnvOffsetBasis = new BigInteger("14695981039346656037");

        BigInteger digest = fnvOffsetBasis;

        for (byte b : inp) {
            digest = digest.xor(BigInteger.valueOf((int) b & 255));
            digest = digest.multiply(fnvPrime).mod(m);
        }
        return padHexString(digest);
    }

    static private String padHexString(BigInteger digest) {
        return padHexString(digest.toString(16), 16);
    }

    static private String padHexString(String hexString) {
        return padHexString(hexString, 16);
    }

    static private String padHexString(String hexString, int len) {
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < (len - hexString.length()); i++) {
            b.append("0");
        }
        b.append(hexString);

        return b.toString();
    }
}
