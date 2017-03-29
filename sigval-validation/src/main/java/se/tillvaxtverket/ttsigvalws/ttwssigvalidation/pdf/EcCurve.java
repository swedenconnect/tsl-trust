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
