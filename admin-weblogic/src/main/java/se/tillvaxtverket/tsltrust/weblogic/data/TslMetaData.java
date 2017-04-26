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
package se.tillvaxtverket.tsltrust.weblogic.data;

import com.aaasec.lib.aaacert.AaaCertificate;
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
    private List<AaaCertificate> certList;
    private AaaCertificate usedTslSigCert = null;
    private EuropeCountry country;

    public TslMetaData(String urlString, List<AaaCertificate> certList, EuropeCountry country) {
        this.urlString = urlString;
        this.certList = certList;
        this.country = country;
    }

    public TslMetaData(TrustServiceList tsl, URL url, File filename) {
        this(tsl, url, filename, new ArrayList<AaaCertificate>());
    }

    public TslMetaData(TrustServiceList tsl, URL url, File fileName, List<AaaCertificate> certList) {
        this.tsl = tsl;
        this.url = url;
        this.tslFile = fileName;
        this.certList = certList;
        try {
            this.country = EuropeCountry.valueOf(tsl.getSchemeTerritory().toUpperCase());
        } catch (Exception ex) {
        }
    }

    public List<AaaCertificate> getCertList() {
        return certList;
    }

    public void setCertList(List<AaaCertificate> certList) {
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

    public AaaCertificate getUsedTslSigCert() {
        return usedTslSigCert;
    }

    public void setUsedTslSigCert(AaaCertificate usedTslSigCert) {
        this.usedTslSigCert = usedTslSigCert;
    }

    public EuropeCountry getCountry() {
        return country;
    }

    public void setCountry(EuropeCountry country) {
        this.country = country;
    }

}
