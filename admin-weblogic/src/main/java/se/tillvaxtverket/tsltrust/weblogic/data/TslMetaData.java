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
package se.tillvaxtverket.tsltrust.weblogic.data;

import iaik.x509.X509Certificate;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceList;
import se.tillvaxtverket.tsltrust.common.utils.general.EuropeCountry;

/**
 * Data class holding metadata for cached Trust service Status Lists (TSL)
 */
public class TslMetaData {

    private TrustServiceList tsl = null;
    private URL url;
    private String urlString, signStatus = "";
    private File tslFile;
    private List<X509Certificate> certList;
    private X509Certificate usedTslSigCert = null;
    private EuropeCountry country;

    public TslMetaData(String urlString, List<X509Certificate> certList, EuropeCountry country) {
        this.urlString = urlString;
        this.certList = certList;
        this.country = country;
    }

    public TslMetaData(TrustServiceList tsl, URL url, File filename) {
        this(tsl, url, filename, new ArrayList<X509Certificate>());
    }

    public TslMetaData(TrustServiceList tsl, URL url, File fileName, List<X509Certificate> certList) {
        this.tsl = tsl;
        this.url = url;
        this.tslFile = fileName;
        this.certList = certList;
        try {
            this.country = EuropeCountry.valueOf(tsl.getSchemeTerritory().toUpperCase());
        } catch (Exception ex) {
        }
    }

    public List<X509Certificate> getCertList() {
        return certList;
    }

    public void setCertList(List<X509Certificate> certList) {
        this.certList = certList;
    }

    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    public TrustServiceList getTsl() {
        return tsl;
    }

    public void setTsl(TrustServiceList tsl) {
        this.tsl = tsl;
    }

    public File getTslFile() {
        return tslFile;
    }

    public void setTslFile(File tslFile) {
        this.tslFile = tslFile;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public X509Certificate getUsedTslSigCert() {
        return usedTslSigCert;
    }

    public void setUsedTslSigCert(X509Certificate usedTslSigCert) {
        this.usedTslSigCert = usedTslSigCert;
    }

    public EuropeCountry getCountry() {
        return country;
    }

    public void setCountry(EuropeCountry country) {
        this.country = country;
    }

}
