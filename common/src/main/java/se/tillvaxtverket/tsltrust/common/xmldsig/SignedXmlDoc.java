/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
