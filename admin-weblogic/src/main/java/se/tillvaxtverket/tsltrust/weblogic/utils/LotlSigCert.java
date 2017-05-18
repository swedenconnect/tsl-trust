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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import com.aaasec.lib.aaacert.AaaCertificate;
import se.tillvaxtverket.tsltrust.common.utils.general.Constants;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.GeneralStaticUtils;
import java.io.File;
import java.util.List;

/**
 * Default trust anchors for validating signature on the root TSL. These certificates 
 * may be replaced by other certificates in the lotlSigCert.pem file in the cfg folder
 */
public class LotlSigCert implements Constants {

    private static String[] defLotlSignCACert = new String[]{
        "-----BEGIN CERTIFICATE-----",
        "MIIFnDCCBISgAwIBAgIDAVZhMA0GCSqGSIb3DQEBBQUAMEUxCzAJBgNVBAYTAkxVMRYwFAYDVQQK",
        "Ew1MdXhUcnVzdCBTLkEuMR4wHAYDVQQDExVMdXhUcnVzdCBRdWFsaWZpZWQgQ0EwHhcNMDkxMjIz",
        "MTAzNTU2WhcNMTIxMjIzMTAzNTU2WjCCAQYxCzAJBgNVBAYTAkJFMQswCQYDVQQHEwJCRTEcMBoG",
        "A1UEChMTRXVyb3BlYW4gQ29tbWlzc2lvbjELMAkGA1UECxMCTkExJDAiBgNVBAMTG0thcmVsIExv",
        "ZGV3aWprIE0gRGUgVnJpZW5kdDETMBEGA1UEBBMKRGUgVnJpZW5kdDEZMBcGA1UEKhMQS2FyZWwg",
        "TG9kZXdpamsgTTEdMBsGA1UEBRMUMTAxMDAzMzIxMTAwMDQ3NDkzNzgxLDAqBgkqhkiG9w0BCQEW",
        "HUthcmVsLkRlLVZyaWVuZHRAZWMuZXVyb3BhLmV1MRwwGgYDVQQMExNQcm9mZXNzaW9uYWwgUGVy",
        "c29uMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC96JBqnjCmAk43LDuZDMu5lNGK7/UQnJBL",
        "DkUqN2Jb/STQSBJnRJchCY9DnKtqYG9cuSZDRxvIUkssqO/WpFu/I5fO9I4mvLGD9h4ot8/GkRnt",
        "bEl3CnMlRKZJkAdcSHwuqnuNpfaZ4yGGIPslRy9pbRkfXSeeneJAo0Ixiu3QJwIDAQABo4ICVDCC",
        "AlAwDAYDVR0TAQH/BAIwADBgBggrBgEFBQcBAQRUMFIwIwYIKwYBBQUHMAGGF2h0dHA6Ly9vY3Nw",
        "Lmx1eHRydXN0Lmx1MCsGCCsGAQUFBzAChh9odHRwOi8vY2EubHV4dHJ1c3QubHUvTFRRQ0EuY3J0",
        "MIIBHQYDVR0gBIIBFDCCARAwggECBggrgSsBAQIEATCB9TCBxwYIKwYBBQUHAgIwgboagbdMdXhU",
        "cnVzdCBRdWFsaWZpZWQgQ2VydGlmaWNhdGUgb24gQ0NTRCBjb21wbGlhbnQgd2l0aCBFVFNJIFRT",
        "IDEwMSA0NTYgUUNQKyBjZXJ0aWZpY2F0ZSBwb2xpY3kuIEtleSBHZW5lcmF0aW9uIGJ5IENTUC4g",
        "U29sZSBBdXRob3Jpc2VkIFVzYWdlOiBTdXBwb3J0IG9mIFF1YWxpZmllZCBFbGVjdHJvbmljIFNp",
        "Z25hdHVyZS4wKQYIKwYBBQUHAgEWHWh0dHA6Ly9yZXBvc2l0b3J5Lmx1eHRydXN0Lmx1MAgGBgQA",
        "izABATA2BggrBgEFBQcBAwQqMCgwCAYGBACORgEBMBIGBgQAjkYBAjAIEwACAQACAQAwCAYGBACO",
        "RgEEMBEGCWCGSAGG+EIBAQQEAwIFIDALBgNVHQ8EBAMCBkAwHwYDVR0jBBgwFoAUjZCjB90aE3eZ",
        "TJKrTUPeP80pZAUwMQYDVR0fBCowKDAmoCSgIoYgaHR0cDovL2NybC5sdXh0cnVzdC5sdS9MVFFD",
        "QS5jcmwwEQYDVR0OBAoECEwDOyVe3IV5MA0GCSqGSIb3DQEBBQUAA4IBAQBStS9gFx5BmEv5VciG",
        "7qEPR5VzqEpBElkjXDYe68Dw7Ol1nCt51CSuAB2/UcTkfCDh6022H7+EtiUpdM6GH/7qDqZdjqYp",
        "Qznbu4xkrGTMLrTA1lalhgWugGQIpmCg69kiXns/AXcuJVQSR3mA3BMvJrN5PNaALoguynWCrxRH",
        "PTUS3pT5J93jCn90ZPAGhxELRxMdZuFZ2UAn8D8XLUxiz0IK9yqdysJiLzWsQazUkZ+lzDG0mPom",
        "yRkl3LzEK8HXO3HKGr+2n760N+StYyjwywP/kS/3LoPQWfUfjWQE8jDiTgodZT4ewVSxuNrBH5Ls",
        "29aBi0YsT9NYstznKgaR",
        "-----END CERTIFICATE-----",
        "-----BEGIN CERTIFICATE-----",
        "MIIHPDCCBSSgAwIBAgIBcjANBgkqhkiG9w0BAQsFADAxMQswCQYDVQQGEwJFUzERMA8GA1UECgwI",
        "Rk5NVC1SQ00xDzANBgNVBAMMBklTQSBDQTAeFw0xMTExMTYxNTExMDdaFw0xNTExMTYxNTExMDda",
        "ME0xCzAJBgNVBAYTAkJFMRwwGgYDVQQKDBNFVVJPUEVBTiBDT01NSVNTSU9OMSAwHgYDVQQDDBco",
        "U0lHTikgQU5ORUxJIEFORFJFU1NPTjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMw8",
        "81fbxBn5Eq64Hf6pqEqn0Intg49k5D+E2JBMcyeOMYE4WwuzVqs7fuKMX5mhiFw7llVx42S7GIDn",
        "Ka6ascF7irISsYTGOtD6dGt8OEOQkG5i24wxT3we63VW1W/PEO0WoRjkPDBqgljnqcLn+WJLr3Yl",
        "9fpkxyT+H3pl0jnGkGi7UUKEDuoCglFy9Tb1TE+eVNiGWmfskRy6q9Pioujp2FqrwNm95VAkLFGo",
        "NF4iZQda79bgjvAYnA5p4QXgr/5sWZjgsa3Ea9PlRwEvO7C8ndnRE/PTUNwaSmOZsmPH/ksCCnfT",
        "DqqehHVnry3K6QIUKhRsoT1m60qv4UKrvrsCAwEAAaOCA0EwggM9MF0GA1UdEQRWMFSBHUFOTkVM",
        "SS5BTkRSRVNTT05ARUMuRVVST1BBLkVVpDMwMTEYMBYGCSsGAQQBrGYBAgwJQU5EUkVTU09OMRUw",
        "EwYJKwYBBAGsZgEBDAZBTk5FTEkwCQYDVR0TBAIwADAOBgNVHQ8BAf8EBAMCBkAwHQYDVR0OBBYE",
        "FEoUEf+0Oe59fVRs/rW2M0KhmlDsMB8GA1UdIwQYMBaAFEft+GPwma9e/n4OXFjL/uI1N6a9MIHg",
        "BgNVHSAEgdgwgdUwgcgGCisGAQQBrGYDBAEwgbkwKQYIKwYBBQUHAgEWHWh0dHA6Ly93d3cuY2Vy",
        "dC5mbm10LmVzL2RwY3MvMIGLBggrBgEFBQcCAjB/DH1RdWFsaWZpZWQgY2VydGlmaWNhdGUuIFVu",
        "ZGVyIHRoZSB1c2FnZSBjb25kaXRpb25zIGFzc2VydGVkIGluIHRoZSBGTk1ULVJDTSBDUFMgKDEw",
        "NiwgSm9yZ2UgSnVhbiBzdHJlZXQsMjgwMDksIE1hZHJpZCwgU3BhaW4pLjAIBgYEAIswAQEwgYYG",
        "CCsGAQUFBwEBBHoweDBBBggrBgEFBQcwAYY1aHR0cDovL29jc3BJU0FjYS5jZXJ0LmZubXQuZXMv",
        "b2NzcElTQWNhL09jc3BSZXNwb25kZXIwMwYIKwYBBQUHMAKGJ2h0dHA6Ly93d3cuY2VydC5mbm10",
        "LmVzL2NlcnRzL0lTQUNBLmNydDBGBggrBgEFBQcBAwQ6MDgwCAYGBACORgEBMAsGBgQAjkYBAwIB",
        "DzAVBgYEAI5GAQIwCxMDRVVSAgECAgECMAgGBgQAjkYBBDCBzAYDVR0fBIHEMIHBMIG+oIG7oIG4",
        "hoGIbGRhcDovL2xkYXBJU0FjYS5jZXJ0LmZubXQuZXMvQ049Q1JMMixjbj1JU0ElMjBDQSxvPUZO",
        "TVQtUkNNLEM9RVM/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdDtiaW5hcnk/YmFzZT9vYmplY3Rj",
        "bGFzcz1jUkxEaXN0cmlidXRpb25Qb2ludIYraHR0cDovL3d3dy5jZXJ0LmZubXQuZXMvY3Jsc19J",
        "U0FjYS9DUkwyLmNybDANBgkqhkiG9w0BAQsFAAOCAgEALIKumRPBo9NIYyzm4LSJklE9CmsirJC8",
        "9MPcFiC9+vg4YrO9Mmakr8G5ZAVZf+Zr6HgLcIf93/BSgE4UDFD9mAROPDElqx2ppg1+foIvlqg5",
        "7EiHH2kOS4IYJEJpWCs4IqcBkSKcS5vJ5XVpNJL4elpeMQjsGoqjmkKDr1IeZQVOxOiW/bNhM9Fw",
        "6R1aMMLbnziwAgHWDpmIj2Se74HuS3ca0ctm9fAPp/2Y87bgZ1ETs9A6wc4GxYEuGTwh1HPbDB6t",
        "gjSnjH0hY3qBITjB8IM0xGvUR9dQkt8Ez4dvyxcaFlyCMfLaeMf8jlJOv4uU1yRRT9wESIj5GcoE",
        "43cy1cuvRMifqYhoi3KPojJaLTV7ASy1X0yuBWyg0jGyvE6aTIAu3XGCwdGYB1WnSzEUPJboqcyJ",
        "ydkXXxnBKFchw602pVB0ITUqitGH+Kfix2mQQf+v0RDEWQU3WdADNHc+cqoc7sXkJXkO657dYZT/",
        "Ey5MR+gXRm6oK52iXIqjM8kbDqCqAOHVC73u+bUvh0y5B5PjGYuE2K9xK36ooavh5ytF/kY+y0Ju",
        "804CRF00b4wKcibGxplOrU1g+ncONNmBHGbsLULXrum8+fsD3O04Mf0t0O9Mt/ZwAZTa8Cwq/0pq",
        "OWdRqucV7pq/A7fEnRb5AdBaAzxqrXVaa92NkScAm80=",
        "-----END CERTIFICATE-----",
        "-----BEGIN CERTIFICATE-----",
        "MIID8DCCAtigAwIBAgICA+swDQYJKoZIhvcNAQEFBQAwQDELMAkGA1UEBhMCTFUxFjAUBgNVBAoT",
        "DUx1eFRydXN0IHMuYS4xGTAXBgNVBAMTEEx1eFRydXN0IHJvb3QgQ0EwHhcNMDgwNjA1MDkyNTI0",
        "WhcNMTYxMDE4MTA0MDM0WjBFMQswCQYDVQQGEwJMVTEWMBQGA1UEChMNTHV4VHJ1c3QgUy5BLjEe",
        "MBwGA1UEAxMVTHV4VHJ1c3QgUXVhbGlmaWVkIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB",
        "CgKCAQEAybFXzA+8RNnvlCd+sZ8BnH6WO3LmaLi419Ygd9VBYcIwLmMx9KgAKF3u4B87Hee5NL4X",
        "vhm/B9DuDUH5OGZ3P2Dwf7putVEvATvW8jzYq6CzarUthzb9ux+KTdTT+d4y6tkgVggy9DBe+bz6",
        "35oZm2PPQT9kzoR48RBN730KA/MJIa0Sa7ZDphL37WHSA4/TWh9F1/LBRVGC0F4Mg1hU/u+kovF5",
        "mTuUK+ncU7+FS0cQRhAD+C4WfLI/WuzuE+T6ZuZ6Iqg6+vqgf6iKwL6iVZmwKkJPvV3+3Wgy3zq5",
        "tpDvsIGj4kXd1riQGKsEeDfN8y71DG3OdBqF1Yd7ue7ziwIDAQABo4HuMIHrMA8GA1UdEwQIMAYB",
        "Af8CAQAwQgYDVR0gBDswOTA3BggrgSsBAQEBADArMCkGCCsGAQUFBwIBFh1odHRwOi8vcmVwb3Np",
        "dG9yeS5sdXh0cnVzdC5sdTARBglghkgBhvhCAQEEBAMCAAcwDgYDVR0PAQH/BAQDAgHGMB8GA1Ud",
        "IwQYMBaAFN2K1zDx+ZFx6UdwDCXlrKGN34wlMDEGA1UdHwQqMCgwJqAkoCKGIGh0dHA6Ly9jcmwu",
        "bHV4dHJ1c3QubHUvTFRSQ0EuY3JsMB0GA1UdDgQWBBSNkKMH3RoTd5lMkqtNQ94/zSlkBTANBgkq",
        "hkiG9w0BAQUFAAOCAQEAapxOpigXTejGgHBWMAwDBMdZQHpPyoCmw32OIj1qqezO5nDnjG5gfJni",
        "/rp5IFMpV//xmCkjqyO92PyYbcHNSUpP1SjCkyn10e6ipmzpXK0MbgFvIPglAgA5dXxTNf0Q77eW",
        "u36fz5VKQEmJzqoXTccq4nuLL9rLZ88YUlczMaWscETIZCB4kecKVyqHf4+T0JucZqX7zzfpiVyT",
        "r2M+OGl9qiOmKwBGkzseJt+MgYWrskJADKDZMr4bQxkxnhzCSQoraX7DugxM0fH47MitCc74uZrW",
        "IJ6qQjCLBtKzxUGy7B3pYOjLlThr7S64cd12yuR+NjHAFZ2DTXwxKg/FQg==",
        "-----END CERTIFICATE-----",};
    static String pemCert;

    static {
        StringBuilder b = new StringBuilder();
        for (String crtLine : defLotlSignCACert) {
            b.append(crtLine).append(LF);
        }
        pemCert = b.toString();
    };

    public static List<AaaCertificate> getCertificates(String dataLocation) {
        String lotlSigCertLocation = dataLocation + "cfg/lotlSigCert.pem";
        List<AaaCertificate> cert = null;

        File certFile = new File(lotlSigCertLocation);
        if (certFile.canRead()) {
            String pemCerts = FileOps.readTextFile(certFile);
            cert = GeneralStaticUtils.getCertsFromPemList(pemCerts);
        }

        if (cert == null) {
            if (!certFile.canRead()) {
                certFile.getParentFile().mkdirs();
            }
            FileOps.saveTxtFile(certFile, pemCert);
            cert = GeneralStaticUtils.getCertsFromPemList(pemCert);
        }
        return cert;
    }
}
