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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.etsi.uri.x02231.v2.DigitalIdentityListType;
import org.etsi.uri.x02231.v2.ExtensionType;
import org.etsi.uri.x02231.v2.MultiLangNormStringType;
import org.etsi.uri.x02231.v2.NonEmptyMultiLangURIType;
import org.etsi.uri.x02231.v2.ServiceHistoryInstanceType;
import org.etsi.uri.x02231.v2.TSPServiceType;
import se.tillvaxtverket.tsltrust.common.tsl.sie.ServiceInfoExtension;

/**
 * Java object for XML parsing of TSL trust services
 */
public class TrustService {

    private TSPServiceType ts;
    private List<X509Certificate> sdiCertList;
    private byte[] sdiCertData;
    private X509Certificate sdiCert;
    private List<ServiceHistoryInstance> serviceHistoryList;
    private List<ServiceInfoExtension> siExtensions;

    public TrustService(TSPServiceType ts) {
        this.ts = ts;
        getCerts();
        getHistory();
        getExtensions();
    }

    private void getCerts() {
        sdiCertList = new ArrayList<X509Certificate>();
        sdiCertData = null;
        sdiCert = null;
        try {
            DigitalIdentityListType sdi = ts.getServiceInformation().getServiceDigitalIdentity();
            List<byte[]> digitalIdentityties = TslUtils.getDigitalIdentityties(sdi);
            for (byte[] certData : digitalIdentityties) {
                X509Certificate cert = TslUtils.getServiceDigitalIdentityCert(certData);
                sdiCertList.add(cert);
            }
            if (!sdiCertList.isEmpty()) {
                sdiCertData = sdiCertList.get(0).getEncoded();
                sdiCert = sdiCertList.get(0);
            }
        } catch (Exception ex) {
        }
    }

    private void getHistory() {
        serviceHistoryList = new ArrayList<ServiceHistoryInstance>();
        try {
            ServiceHistoryInstanceType[] historyArray = ts.getServiceHistory().getServiceHistoryInstanceArray();
            for (ServiceHistoryInstanceType history : historyArray) {
                serviceHistoryList.add(new ServiceHistoryInstance(history));
            }
        } catch (Exception ex) {
        }
    }

    private void getExtensions() {
        siExtensions = new ArrayList<ServiceInfoExtension>();
        try {
            ExtensionType[] extensionArray = ts.getServiceInformation().getServiceInformationExtensions().getExtensionArray();
            siExtensions = TslUtils.getExtensions(extensionArray);
        } catch (Exception ex) {
        }
    }

    public TSPServiceType tsData() {
        return ts;
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
            data = ts.getServiceInformation().getServiceTypeIdentifier();
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
            MultiLangNormStringType[] name = ts.getServiceInformation().getServiceName().getNameArray();
            data = TslUtils.getLocalisedNormString(name, locale);
        } catch (Exception ex) {
        }
        return data;
    }

    public String getStatus() {
        String data = "";
        try {
            data = ts.getServiceInformation().getServiceStatus();
        } catch (Exception ex) {
        }
        return data;
    }

    public Date getStatusStartingTime() {
        Date data = null;
        try {
            data = ts.getServiceInformation().getStatusStartingTime().getTime();
        } catch (Exception ex) {
        }
        return data;
    }

    public List<ServiceHistoryInstance> getServiceHistory() {
        return serviceHistoryList;
    }

    public List<ServiceInfoExtension> getServiceInfoExtensions() {
        return siExtensions;
    }

    public List<String> getSchemeServiceUris() {
        List<String> data = new ArrayList<String>();
        try {
            NonEmptyMultiLangURIType[] uRIArray = ts.getServiceInformation().getSchemeServiceDefinitionURI().getURIArray();
            for (NonEmptyMultiLangURIType mlut : uRIArray) {
                data.add(mlut.getStringValue());
            }
        } catch (Exception ex) {
        }
        return data;
    }

    public List<String> getTSPServiceUris() {
        List<String> data = new ArrayList<String>();
        try {
            NonEmptyMultiLangURIType[] uRIArray = ts.getServiceInformation().getTSPServiceDefinitionURI().getURIArray();
            for (NonEmptyMultiLangURIType mlut : uRIArray) {
                data.add(mlut.getStringValue());
            }
        } catch (Exception ex) {
        }
        return data;
    }

    public List<String> getServiceSupplyPoints() {
        List<String> data = new ArrayList<String>();
        try {
            String[] serviceSupplyPointArray = ts.getServiceInformation().getServiceSupplyPoints().getServiceSupplyPointArray();
            data.addAll(Arrays.asList(serviceSupplyPointArray));
        } catch (Exception ex) {
        }
        return data;
    }
}
