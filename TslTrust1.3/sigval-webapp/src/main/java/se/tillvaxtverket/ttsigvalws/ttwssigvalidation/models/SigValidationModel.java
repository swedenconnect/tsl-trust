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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models;

import se.tillvaxtverket.tsltrust.common.utils.general.ContextLogger;
import java.util.ArrayList;
import java.util.List;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.DocType;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.SigDocument;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.SignatureValidationContext;

/**
 * The PdfValidation model holds all data related to the validation of an instance
 * of a PDF document with respect to signature validation.
 * 
 * This model is always associated with e base model holding information about
 * trusted certificates.
 */
public class SigValidationModel {

    public final ContextLogger OLOG = new ContextLogger("OCSP", false);
    public final ContextLogger CLOG = new ContextLogger("CRL", false);
    public final ContextLogger ELOG = new ContextLogger("Exceptions", false);
    SigDocument sigDocument;
    private String policyName="";
    private String policyDescription = "";
    private List<SignatureValidationContext> signatureContexts = new ArrayList<SignatureValidationContext>();
    private SigValidationBaseModel baseModel;
    private boolean checkOcspAndCrl=false;
    private boolean prefSpeed=true;
    private boolean abort = false;
    private boolean signVerificationComplete = false;

    public SigValidationModel() {
    }

    public List<SignatureValidationContext> getSignatureContexts() {
        return signatureContexts;
    }

    public void setSignatureContexts(List<SignatureValidationContext> pdfSigContexts) {
        this.signatureContexts = pdfSigContexts;
    }

    public SigValidationBaseModel getBaseModel() {
        return baseModel;
    }

    public void setBaseModel(SigValidationBaseModel baseModel) {
        this.baseModel = baseModel;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public boolean isCheckOcspAndCrl() {
        return checkOcspAndCrl;
    }

    public void setCheckOcspAndCrl(boolean checkOcspAndCrl) {
        this.checkOcspAndCrl = checkOcspAndCrl;
    }

    public boolean isPrefSpeed() {
        return prefSpeed;
    }

    public void setPrefSpeed(boolean prefOcsp) {
        this.prefSpeed = prefOcsp;
    }

    public boolean isAbort() {
        return abort;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }

    public boolean isSignVerificationComplete() {
        return signVerificationComplete;
    }

    public void setSignVerificationComplete(boolean signVerificationComplete) {
        this.signVerificationComplete = signVerificationComplete;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }

   
    public enum DataSource{
        bytes,file;
    }

    public SigDocument getSigDocument() {
        return sigDocument;
    }

    public void setSigDocument(SigDocument sigDocument) {
        this.sigDocument = sigDocument;
    }
    
}
