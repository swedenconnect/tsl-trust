/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.tsltrust.common.xmldsig;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;

/**
 *
 * @author stefan
 */
public class SigVerifyResult {

    public X509Certificate cert = null;
    public String status = "";
    public boolean valid = false;
    public List<IndivdualSignatureResult> resultList = new ArrayList<IndivdualSignatureResult>();
    public int sigCnt;
    public int validSignatures;

    public SigVerifyResult() {
    }

    /**
     * Sets the certificate and the status to valid
     *
     * @param cert
     */
    public SigVerifyResult(X509Certificate cert) {
        this.status = "ok";
        this.cert = cert;
        this.valid = true;
    }

    /**
     * Sets the values of the signature verification result
     *
     * @param cert Certificate
     * @param status Status
     * @param valid true if the signature is valid.
     */
    public SigVerifyResult(X509Certificate cert, String status, boolean valid) {
        this.status = status;
        this.cert = cert;
        this.valid = valid;
    }

    /**
     * Sets the status comment for failed validation
     *
     * @param status Status
     */
    public SigVerifyResult(String status) {
        this.status = status;
        this.cert = null;
        this.valid = false;
    }

    /**
     * Analyzes the result of individual signatures and generates the summary
     * of the signature validation results.
     */
    public void consolidateResults() {
        if (resultList.isEmpty()) {
            valid = false;
            status = "No signature covers this document";
            sigCnt = 0;
            validSignatures = 0;
            return;
        }
        sigCnt = resultList.size();
        int firstValid = -1, idx = 0, validCnt = 0;
        for (IndivdualSignatureResult result : resultList) {
            if (result.thisValid) {
                validCnt++;
            }
            if (firstValid < 0 && result.thisValid) {
                firstValid = idx;
            }
            idx++;
        }
        validSignatures = validCnt;

        //Get the most relevant signature result
        if (firstValid < 0) {
            valid = false;
            status = resultList.get(0).thisStatus;
        } else {
            IndivdualSignatureResult result = resultList.get(firstValid);
            valid = result.thisValid;
            status = result.thisStatus;
            cert = result.thisCert;
        }
    }

    public IndivdualSignatureResult addNewIndividualSignatureResult() {
        IndivdualSignatureResult result = new IndivdualSignatureResult();
        resultList.add(result);
        return result;
    }

    public class IndivdualSignatureResult {

        public X509Certificate thisCert = null;
        public String thisStatus = "";
        public boolean thisValid = false;
        public Node thisSignatureNode = null;

        public IndivdualSignatureResult() {
        }

        /**
         * Retrieves the list of X.509 certificate bytes present in the signature node
         * @return list of certificate byte arrays
         */
        public List<byte[]> getCertList() {
            List<byte[]> certList = new ArrayList<byte[]>();
            Node sigValNode = null;
            Node x509Data = null;
            List<Node> certNodes = new ArrayList<Node>();

            try {
                NodeList sigChilds = thisSignatureNode.getChildNodes();
                for (int i = 0; i < sigChilds.getLength(); i++) {
                    Node item = sigChilds.item(i);
                    if (item.getNodeName().endsWith("KeyInfo")) {
                        NodeList kiNodes = item.getChildNodes();
                        for (int j = 0; j < kiNodes.getLength(); j++) {
                            Node kiNode = kiNodes.item(j);
                            if (kiNode.getNodeName().endsWith("X509Data")) {
                                x509Data = kiNode;
                            }

                        }
                    }
                }
                NodeList x509Nodes = x509Data.getChildNodes();
                for (int i = 0; i < x509Nodes.getLength(); i++) {
                    Node x509Node = x509Nodes.item(i);
                    if (x509Node.getNodeName().endsWith("X509Certificate")) {
                        certNodes.add(x509Node);
                    }
                }

                // get certificates
                for (int i = 0; i < certNodes.size(); i++) {
                    Node certNode = certNodes.get(i);
                    try {
                        byte[] certBytes = Base64Coder.decodeLines(certNode.getTextContent());
                        certList.add(certBytes);

                    } catch (Exception ex) {
                    }
                }
            } catch (Exception ex) {
            }
            return certList;
        }
    }
}