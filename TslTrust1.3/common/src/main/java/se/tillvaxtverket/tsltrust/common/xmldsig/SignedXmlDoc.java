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
package se.tillvaxtverket.tsltrust.common.xmldsig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.w3c.dom.Document;

/**
 *
 * @author stefan
 */
public class SignedXmlDoc {

    public byte[] signedInfoOctets;
    public Document doc;
    public byte[] sigDocBytes;
    private static int[] sha256Prefix = new int[]{0x30, 0x31, 0x30, 0x0D, 0x06, 0x09, 0x60, 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20};

    public SignedXmlDoc(byte[] hash, Document doc) {
        this.signedInfoOctets = hash;
        this.doc = doc;
    }

    public SignedXmlDoc() {
    }

    public byte[] getSha256Hash() {
        return getSha256Hash(signedInfoOctets);
    }

    public static byte[] getSha256Hash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            byte[] hashValue = md.digest();
            return hashValue;
        } catch (NoSuchAlgorithmException ex) {
            return new byte[]{};
        }
    }
    
    public byte[] getPkcs1Sha256TbsDigest(){
        return getPkcs1Sha256TbsDigest(getSha256Hash());
    }
    public static byte[] getPkcs1Sha256TbsDigest(byte[] hashValue){
        return addSha256Prefix(hashValue);
    }

    public static byte[] addSha256Prefix(byte[] hash) {
        int len = hash.length + sha256Prefix.length;
        byte[] p1Hash = new byte[len];
        for (int i = 0; i < sha256Prefix.length; i++) {
            p1Hash[i] = (byte) sha256Prefix[i];
        }
        System.arraycopy(hash, 0, p1Hash, sha256Prefix.length, hash.length);
        return p1Hash;
    }
}
