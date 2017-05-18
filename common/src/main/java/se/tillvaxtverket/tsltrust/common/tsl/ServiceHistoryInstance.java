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
package se.tillvaxtverket.tsltrust.common.tsl;

import com.aaasec.lib.aaacert.AaaCertificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.etsi.uri.x02231.v2.DigitalIdentityListType;
import org.etsi.uri.x02231.v2.ExtensionType;
import org.etsi.uri.x02231.v2.MultiLangNormStringType;
import org.etsi.uri.x02231.v2.ServiceHistoryInstanceType;
import se.tillvaxtverket.tsltrust.common.tsl.sie.ServiceInfoExtension;

/**
 *
 * @author stefan
 */
public class ServiceHistoryInstance {

    ServiceHistoryInstanceType sh;
    private List<AaaCertificate> sdiCertList;
    private byte[] sdiCertData;
    private AaaCertificate sdiCert;
    private List<ServiceInfoExtension> siExtensions;

    public ServiceHistoryInstance(ServiceHistoryInstanceType serviceHistory) {
        this.sh = serviceHistory;
        getCerts();
        getExtensions();
    }

    private void getCerts() {
        sdiCertList = new ArrayList<AaaCertificate>();
        sdiCertData = null;
        sdiCert = null;
        try {
            DigitalIdentityListType sdi = sh.getServiceDigitalIdentity();
            List<byte[]> digitalIdentityties = TslUtils.getDigitalIdentityties(sdi);
            for (byte[] certData : digitalIdentityties) {
                AaaCertificate cert = TslUtils.getServiceDigitalIdentityCert(certData);
                sdiCertList.add(cert);
            }
            sdiCertData = sdiCertList.get(0).getEncoded();
            sdiCert = sdiCertList.get(0);
        } catch (Exception ex) {
        }
    }

    private void getExtensions() {
        siExtensions = new ArrayList<ServiceInfoExtension>();
        try {
            ExtensionType[] extensionArray = sh.getServiceInformationExtensions().getExtensionArray();
            siExtensions = TslUtils.getExtensions(extensionArray);
        } catch (Exception ex) {
        }
    }

    public ServiceHistoryInstanceType serviceHistoryInstanceData() {
        return sh;
    }

    public byte[] getServiceDigitalIdentityData() {
        return sdiCertData;
    }

    public AaaCertificate getServiceDigitalIdentityCert() {
        return sdiCert;
    }

    public List<AaaCertificate> getServiceDigitalIdentityCerts() {
        return sdiCertList;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getType() {
        String data = "";
        try {
            data = sh.getServiceTypeIdentifier();
        } catch (Exception ex) {
        }
        return data;
    }

    public String getName() {
        return getName(Locale.ENGLISH);
    }

    public String getName(Locale locale) {
        String data = "";
        try {
            MultiLangNormStringType[] name = sh.getServiceName().getNameArray();
            data = TslUtils.getLocalisedNormString(name, locale);
        } catch (Exception ex) {
        }
        return data;
    }

    public String getStatus() {
        String data = "";
        try {
            data = sh.getServiceStatus();
        } catch (Exception ex) {
        }
        return data;
    }

    public Date getStatusStartingTime() {
        Date data = null;
        try {
            data = sh.getStatusStartingTime().getTime();
        } catch (Exception ex) {
        }
        return data;
    }

    public List<ServiceInfoExtension> getServiceInfoExtensions() {
        return siExtensions;
    }
}
