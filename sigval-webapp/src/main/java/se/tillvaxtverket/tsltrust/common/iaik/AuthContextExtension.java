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
package se.tillvaxtverket.tsltrust.common.iaik;

import com.aaasec.lib.crypto.xml.XmlBeansUtil;
import iaik.asn1.ASN1Object;
import iaik.asn1.ObjectID;
import iaik.asn1.SEQUENCE;
import iaik.asn1.UTF8String;
import iaik.x509.V3Extension;
import iaik.x509.X509ExtensionException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.xmlbeans.XmlException;
import se.elegnamnden.id.authCont.x10.saci.SAMLAuthContextDocument;

/**
 *
 * @author stefan
 */
public class AuthContextExtension extends V3Extension {

    public static final ObjectID extensionOid = new ObjectID("1.2.752.201.5.1", "AuthContextExtension", "Authentication Context Extension");
    public static final String contentType = "http://id.elegnamnden.se/auth-cont/1.0/saci";
    List<SAMLAuthContextDocument> statementInfoList = new ArrayList<SAMLAuthContextDocument>();

    public AuthContextExtension() {
    }

    public AuthContextExtension(List<SAMLAuthContextDocument> statementInfoList) {
        this.statementInfoList = statementInfoList;
    }

    public AuthContextExtension(SAMLAuthContextDocument statementInfo) {
        statementInfoList.clear();
        statementInfoList.add(statementInfo);

    }

    @Override
    public ASN1Object toASN1Object() throws X509ExtensionException {
        SEQUENCE ext = new SEQUENCE();
        for (SAMLAuthContextDocument statementInfo : statementInfoList) {
            try {
                SEQUENCE authCtxt = new SEQUENCE();
                ext.addComponent(authCtxt);
                authCtxt.addComponent(new UTF8String(contentType));
                SAMLAuthContextDocument strippedContextInfo = SAMLAuthContextDocument.Factory.parse(statementInfo.getDomNode(),XmlBeansUtil.stripWhiteSPcae);
                String contextXML = new String(XmlBeansUtil.getBytes(strippedContextInfo, false), Charset.forName("UTF-8"));
                UTF8String statementInfoData = new UTF8String(contextXML);
                authCtxt.addComponent(statementInfoData);
            } catch (XmlException ex) {
                Logger.getLogger(AuthContextExtension.class.getName()).warning(ex.getMessage());
            }
        }
        return ext;
    }

    @Override
    public void init(ASN1Object asno) throws X509ExtensionException {
        statementInfoList.clear();
        try {
            int len = asno.countComponents();
            for (int i = 0; i < len; i++) {
                ASN1Object authCont = asno.getComponentAt(i);
                UTF8String contType = (UTF8String) authCont.getComponentAt(0);
                String type = new String(contType.getByteValue(), Charset.forName("UTF-8"));
                if (!type.equals(contentType)) {
                    continue;
                }
                byte[] statementInfoData = ((UTF8String) authCont.getComponentAt(1)).getByteValue();
                SAMLAuthContextDocument statementInfo = SAMLAuthContextDocument.Factory.parse(new ByteArrayInputStream(statementInfoData));
                statementInfoList.add(statementInfo);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public int hashCode() {
        return extensionOid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuthContextExtension other = (AuthContextExtension) obj;
        if (this.statementInfoList != other.statementInfoList && (this.statementInfoList == null || !this.statementInfoList.equals(other.statementInfoList))) {
            return false;
        }
        return true;
    }

    @Override
    public ObjectID getObjectID() {
        return extensionOid;
    }

    public List<SAMLAuthContextDocument> getStatementInfoList() {
        return statementInfoList;
    }

    /**
     * Returns a string representation of the statement info
     *
     * @return a string representation of the statement info
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (SAMLAuthContextDocument statementInfo : statementInfoList) {
            b.append("SAML Authentication Context Info:\n");
            b.append(new String(XmlBeansUtil.getStyledBytes(statementInfo, false), Charset.forName("UTF-8"))).append("\n");
        }
        return b.toString();
    }
}
