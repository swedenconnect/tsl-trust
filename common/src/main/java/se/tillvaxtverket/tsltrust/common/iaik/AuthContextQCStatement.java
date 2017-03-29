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
package se.tillvaxtverket.tsltrust.common.iaik;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iaik.asn1.ASN1Object;
import iaik.asn1.IA5String;
import iaik.asn1.ObjectID;
import iaik.asn1.PrintableString;
import iaik.asn1.SEQUENCE;
import iaik.x509.extensions.qualified.structures.QCStatementInfo;
import java.nio.charset.Charset;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;

/**
 * Implements the SAML Assertion QCStatement.
 * <p>
 * This class extends
 * the {@link iaik.x509.extensions.qualified.structures.QCStatementInfo QCStatementInfo}
 * class. For using this private statement it has to be {@link 
 * iaik.x509.extensions.qualified.structures.QCStatement#register(ObjectID, Class)
 * registered} within the QCStatement framework:
 * <pre>
 * QCStatement.register(MyPrivateQCStatement.statementID, MyPrivateQCStatement.class);
 * </pre>
 * <p>
 * The ASN.1 structure of the statement info belonging to this private QCStatement
 * consists a string holding a Base64 encoded XML Structure:
 * <pre>
 * statementMessage ::= PrintableString
 * </pre>
 * @version File Revision <!-- $$Revision: --> 8 <!-- $ -->
 */
public class AuthContextQCStatement extends QCStatementInfo {

    /**
     * The statement id for this private QC statement.
     */
    public static final ObjectID statementID = new ObjectID("1.2.752.201.4.2.1", "AuthContextQCStatement");
    public static final ObjectID contentID = new ObjectID("1.2.752.201.4.2.1.1", "eID2SAMLContext");
    private static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    private static Gson gson = new Gson();
    SamlAssertionInfo statementInfo;
    String mimeType = "application/json";
    boolean oidMatch = true;

    public AuthContextQCStatement() {
    }

    /**
     * Creates a private QC statement for the given statement string.
     *
     * @param statement the statement message
     */
    public AuthContextQCStatement(SamlAssertionInfo statementInfo) {
        this.statementInfo = statementInfo;
    }

    /**
     * Returns the statement ID.
     *
     * @return the statement id
     */
    @Override
    public ObjectID getStatementID() {
        return statementID;
    }

    /**
     * Gets the statement message.
     * 
     * @return the statement message
     */
    public SamlAssertionInfo getStatement() {
        return statementInfo;
    }

    /**
     * Decodes the statement info.
     *
     * @param obj the statement info as ASN1Object
     */
    @Override
    public void decode(ASN1Object obj) {
        try {
            if (!((ObjectID) obj.getComponentAt(0)).equals(contentID)) {
                oidMatch = false;
                statementInfo = new SamlAssertionInfo();
                return;
            }
            mimeType = (String) obj.getComponentAt(1).getComponentAt(0).getValue();
            String b64Data = (String) obj.getComponentAt(1).getComponentAt(1).getValue();
            String json = new String(Base64Coder.decode(b64Data),Charset.forName("UTF-8"));
            statementInfo = gson.fromJson(json, SamlAssertionInfo.class);
            return;
        } catch (Exception ex) {
        }
        statementInfo = new SamlAssertionInfo();
    }

    /**
     * Returns an ASN.1 representation of this statement info.
     *
     * @return this statement info as ASN1Object
     */
    @Override
    public ASN1Object toASN1Object() {
        SEQUENCE asn1 = new SEQUENCE();
        asn1.addComponent(contentID);
        SEQUENCE content = new SEQUENCE();
        asn1.addComponent(content);
        content.addComponent(new PrintableString(mimeType));
        String jsonData = gson.toJson(statementInfo);
        IA5String b64String = new IA5String(String.valueOf(Base64Coder.encode(jsonData.getBytes(Charset.forName("UTF-8")))));
        content.addComponent(b64String);
        return asn1;
    }

    /**
     * Returns a string representation of the statement info
     *
     * @return a string representation of the statement info
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (oidMatch) {
            b.append(contentID.getNameAndID()).append("\n");
        }
        b.append("Content type: ").append(mimeType).append("\n");
        b.append(prettyGson.toJson(statementInfo)).append("\n");
        return b.toString();
    }
}
