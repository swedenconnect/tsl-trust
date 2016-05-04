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
    private List<X509Certificate> sdiCertList;
    private byte[] sdiCertData;
    private X509Certificate sdiCert;
    private List<ServiceInfoExtension> siExtensions;

    public ServiceHistoryInstance(ServiceHistoryInstanceType serviceHistory) {
        this.sh = serviceHistory;
        getCerts();
        getExtensions();
    }

    private void getCerts() {
        sdiCertList = new ArrayList<X509Certificate>();
        sdiCertData = null;
        sdiCert = null;
        try {
            DigitalIdentityListType sdi = sh.getServiceDigitalIdentity();
            List<byte[]> digitalIdentityties = TslUtils.getDigitalIdentityties(sdi);
            for (byte[] certData : digitalIdentityties) {
                X509Certificate cert = TslUtils.getServiceDigitalIdentityCert(certData);
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

    public X509Certificate getServiceDigitalIdentityCert() {
        return sdiCert;
    }

    public List<X509Certificate> getServiceDigitalIdentityCerts() {
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
