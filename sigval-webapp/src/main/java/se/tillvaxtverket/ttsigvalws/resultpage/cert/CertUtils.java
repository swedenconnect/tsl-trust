package se.tillvaxtverket.ttsigvalws.resultpage.cert;

import iaik.x509.X509Certificate;
import lombok.extern.java.Log;
import org.bouncycastle.asn1.*;
import se.elegnamnden.id.authCont.x10.saci.SAMLAuthContextDocument;
import se.elegnamnden.id.authCont.x10.saci.SAMLAuthContextType;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

@Log
public class CertUtils {

  public static SAMLAuthContextType getAuthContextExtData(X509Certificate signerCertificate) {
    try {
      byte[] extensionValue = signerCertificate.getExtensionValue("1.2.752.201.5.1");
      ASN1InputStream asn1Is = new ASN1InputStream(extensionValue);
      byte[] extContent = DEROctetString.getInstance(asn1Is.readObject()).getOctets();
      ASN1InputStream contentIs = new ASN1InputStream(extContent);
      ASN1Sequence authnContexts = ASN1Sequence.getInstance(contentIs.readObject());
      ASN1Sequence authnContext = ASN1Sequence.getInstance(authnContexts.getObjectAt(0));
      String contextInfo = DERUTF8String.getInstance(authnContext.getObjectAt(1)).getString();
      SAMLAuthContextType samlAuthContext = SAMLAuthContextDocument.Factory.parse(contextInfo).getSAMLAuthContext();
      return samlAuthContext;
    }
    catch (Exception ex) {
      log.fine("Failed to parse AuthnContext Extension from signer cert in existing signature: " + ex.getMessage());
      return null;
    }
  }

  /**
   * Gets a map of recognized subject DN attributes.
   *
   * @param cert
   *          X.509 certificate
   * @return subject DN attribute map
   * @throws IOException
   *           for errors getting the subject attributes from the certificate
   */
  public static Map<SubjectDnAttribute, String> getSubjectAttributes(final Certificate cert) throws IOException {

    try {
      final ASN1InputStream ain = new ASN1InputStream(cert.getEncoded());
      ASN1Sequence certSeq = null;
      try {
        certSeq = (ASN1Sequence) ain.readObject();
      }
      finally {
        ain.close();
      }
      final ASN1Sequence tbsSeq = (ASN1Sequence) certSeq.getObjectAt(0);

      int counter = 0;
      while (tbsSeq.getObjectAt(counter) instanceof ASN1TaggedObject) {
        counter++;
      }
      // Get subject
      final ASN1Sequence subjectDn = (ASN1Sequence) tbsSeq.getObjectAt(counter + 4);
      final Map<SubjectDnAttribute, String> subjectDnAttributeMap = getSubjectAttributes(subjectDn);

      return subjectDnAttributeMap;
    }
    catch (CertificateEncodingException e) {
      throw new IOException("Failed to get subject attributes from certificate - " + e.getMessage(), e);
    }
  }

  /**
   * Gets a map of recognized subject DN attributes.
   *
   * @param subjectDn
   *          subject DN
   * @return subject DN attribute map
   */
  public static Map<SubjectDnAttribute, String> getSubjectAttributes(final ASN1Sequence subjectDn) {
    final Map<SubjectDnAttribute, String> subjectDnAttributeMap = new EnumMap<>(SubjectDnAttribute.class);

    final Iterator<ASN1Encodable> subjDnIt = subjectDn.iterator();
    while (subjDnIt.hasNext()) {
      final ASN1Set rdnSet = (ASN1Set) subjDnIt.next();
      final Iterator<ASN1Encodable> rdnSetIt = rdnSet.iterator();
      while (rdnSetIt.hasNext()) {
        final ASN1Sequence rdnSeq = (ASN1Sequence) rdnSetIt.next();
        final ASN1ObjectIdentifier rdnOid = (ASN1ObjectIdentifier) rdnSeq.getObjectAt(0);
        final String oidStr = rdnOid.getId();
        final ASN1Encodable rdnVal = rdnSeq.getObjectAt(1);
        final String rdnValStr = getStringValue(rdnVal);
        final SubjectDnAttribute subjectDnAttr = SubjectDnAttribute.getSubjectDnFromOid(oidStr);
        if (!subjectDnAttr.equals(SubjectDnAttribute.unknown)) {
          subjectDnAttributeMap.put(subjectDnAttr, rdnValStr);
        }
      }
    }

    return subjectDnAttributeMap;
  }

  private static String getStringValue(final ASN1Encodable rdnVal) {
    if (rdnVal instanceof DERUTF8String) {
      final DERUTF8String utf8Str = (DERUTF8String) rdnVal;
      return utf8Str.getString();
    }
    if (rdnVal instanceof DERPrintableString) {
      final DERPrintableString str = (DERPrintableString) rdnVal;
      return str.getString();
    }
    return rdnVal.toString();
  }

}
