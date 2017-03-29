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
package se.tillvaxtverket.tsltrust.common.tsl;

import iaik.x509.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.etsi.uri.x02231.v2.AdditionalInformationType;
import org.etsi.uri.x02231.v2.AnyType;
import org.etsi.uri.x02231.v2.DigitalIdentityListType;
import org.etsi.uri.x02231.v2.DigitalIdentityType;
import org.etsi.uri.x02231.v2.InternationalNamesType;
import org.etsi.uri.x02231.v2.MultiLangStringType;
import org.etsi.uri.x02231.v2.NonEmptyURIListType;
import org.etsi.uri.x02231.v2.OtherTSLPointerType;
import org.etsi.uri.x02231.v2.SchemeOperatorNameDocument;
import org.etsi.uri.x02231.v2.SchemeTerritoryDocument;
import org.etsi.uri.x02231.v2.SchemeTypeCommunityRulesDocument;
import org.etsi.uri.x02231.v2.TSLTypeDocument;
import org.etsi.uri.x02231.v2.additionaltypes.MimeTypeDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;

/**
 * Java object for XML parsing TSL other pointers data
 */
public class OtherTSLPointerData {

    private OtherTSLPointerType otp;
    private boolean mrTslPointer = false;
    private boolean mimeTypePresent = false;
    private String tslType="";
    private String schemeTerritory="";
    private String mimeType="";
    private String tSLLocation="";
    private InternationalNamesType schemeOperatorName;
    private NonEmptyURIListType SchemeTypeCommunityRules;
    private List<iaik.x509.X509Certificate> otpCertificates = new ArrayList<iaik.x509.X509Certificate>();
    private List<MultiLangStringType> textualAdditionalInfo = new ArrayList<MultiLangStringType>();

    public OtherTSLPointerData(OtherTSLPointerType otp) {
        this.otp = otp;
        if (otp != null) {
            parseOtherTSLPointer();
        }
    }

    private void parseOtherTSLPointer() {

        //Attempt to derive certificates
        try {
            DigitalIdentityListType[] sdiList = otp.getServiceDigitalIdentities().getServiceDigitalIdentityArray();
            for (DigitalIdentityListType sdi : sdiList) {
                DigitalIdentityType[] digitalIdList = sdi.getDigitalIdArray();
                for (DigitalIdentityType digId : digitalIdList) {
                    byte[] cert = digId.getX509Certificate();
                    X509Certificate iaikCert = KsCertFactory.getIaikCert(cert);
                    if (iaikCert != null) {
                        otpCertificates.add(iaikCert);
                    }
                }
            }
        } catch (Exception ex) {
        }
        //Get TSL location and additional information
        AdditionalInformationType addInfo=null;
        try {
            // Get TSL Location URL;
            tSLLocation = otp.getTSLLocation();
            // Get Additional info
            addInfo = otp.getAdditionalInformation();
            textualAdditionalInfo = Arrays.asList(addInfo.getTextualInformationArray());
        } catch (Exception ex) {
        }
        try {
            AnyType[] oInfoArray = addInfo.getOtherInformationArray();
            for (AnyType oInfo : oInfoArray) {
                NodeList oInfoNodes = oInfo.getDomNode().getChildNodes();
                int items = oInfoNodes.getLength();
                for (int i = 0; i < items; i++) {
                    Node oin = oInfoNodes.item(i);
                    String nodeName = oin.getNodeName();
                    if (nodeName.endsWith("TSLType")) {
                        tslType = TSLTypeDocument.Factory.parse(oin).getTSLType();
                    }
                    if (nodeName.endsWith("SchemeOperatorName")) {
                        schemeOperatorName = SchemeOperatorNameDocument.Factory.parse(oin).getSchemeOperatorName();
                    }
                    if (nodeName.endsWith("SchemeTerritory")) {
                        schemeTerritory = SchemeTerritoryDocument.Factory.parse(oin).getSchemeTerritory();
                    }
                    if (nodeName.endsWith("SchemeTypeCommunityRules")) {
                        SchemeTypeCommunityRules = SchemeTypeCommunityRulesDocument.Factory.parse(oin).getSchemeTypeCommunityRules();
                    }
                    if (nodeName.endsWith("MimeType")) {
                        mimeType = MimeTypeDocument.Factory.parse(oin).getMimeType();
                        mimeTypePresent = true;
                        if (mimeType.toLowerCase().endsWith("tsl+xml")) {
                            mrTslPointer = true;
                        }
                    }
                }
            }

        } catch (Exception ex) {
        }
    }

    public NonEmptyURIListType getSchemeTypeCommunityRules() {
        return SchemeTypeCommunityRules;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isMrTslPointer() {
        return mrTslPointer;
    }

    public OtherTSLPointerType getOtp() {
        return otp;
    }

    public List<X509Certificate> getOtpCertificates() {
        return otpCertificates;
    }

    public InternationalNamesType getSchemeOperatorName() {
        return schemeOperatorName;
    }

    public String getSchemeTerritory() {
        return schemeTerritory;
    }

    public String getTSLLocation() {
        return tSLLocation;
    }

    public String getTslType() {
        return tslType;
    }

    public boolean isMimeTypePresent() {
        return mimeTypePresent;
    }

    public List<MultiLangStringType> getTextualAdditionalInfo() {
        return textualAdditionalInfo;
    }
}
