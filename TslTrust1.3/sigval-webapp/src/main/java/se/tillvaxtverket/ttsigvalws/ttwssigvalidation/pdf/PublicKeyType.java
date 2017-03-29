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
public enum PublicKeyType {
    EC(PdfObjectIds.ID_ECDSA),
    RSA(PdfObjectIds.ID_RSA),
    Unknown(null);
    
    String objectId;

    private PublicKeyType(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }
    
    public static PublicKeyType getTypeFromOid(String oid){
        for (PublicKeyType pkType: values()){
            if (pkType.getObjectId().equalsIgnoreCase(oid)){
                return pkType;
            }
        }
        return Unknown;
    }
}
