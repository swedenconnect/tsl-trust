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
