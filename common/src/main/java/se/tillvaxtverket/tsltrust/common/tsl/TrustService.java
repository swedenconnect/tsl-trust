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
    private List<AaaCertificate> sdiCertList;
    private byte[] sdiCertData;
    private AaaCertificate sdiCert;
    private List<ServiceHistoryInstance> serviceHistoryList;
    private List<ServiceInfoExtension> siExtensions;

    public TrustService(TSPServiceType ts) {
        this.ts = ts;
        getCerts();
        getHistory();
        getExtensions();
    }

    private void getCerts() {
        sdiCertList = new ArrayList<AaaCertificate>();
        sdiCertData = null;
        sdiCert = null;
        try {
            DigitalIdentityListType sdi = ts.getServiceInformation().getServiceDigitalIdentity();
            List<byte[]> digitalIdentityties = TslUtils.getDigitalIdentityties(sdi);
            for (byte[] certData : digitalIdentityties) {
                AaaCertificate cert = TslUtils.getServiceDigitalIdentityCert(certData);
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
