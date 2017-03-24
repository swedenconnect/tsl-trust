/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

/**
 *
 * @author stefan
 */
public enum EcCurve {

    P256(PdfObjectIds.ID_EC_P256, 256),
    unknown(null, 0);

    String oid;
    int keyLength;

    private EcCurve(String oid, int keyLength) {
        this.oid = oid;
        this.keyLength = keyLength;
    }

    public String getOid() {
        return oid;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public static EcCurve getEcCurveFromOid(String oid) {
        for (EcCurve curve : values()) {
            if (curve.getOid().equalsIgnoreCase(oid)) {
                return curve;
            }
        }
        return unknown;
    }

}
