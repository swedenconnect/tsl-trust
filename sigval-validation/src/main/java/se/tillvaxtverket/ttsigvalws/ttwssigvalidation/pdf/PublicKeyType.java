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
