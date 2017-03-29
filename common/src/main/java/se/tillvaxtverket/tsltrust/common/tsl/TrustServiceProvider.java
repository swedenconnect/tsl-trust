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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.etsi.uri.x02231.v2.MultiLangNormStringType;
import org.etsi.uri.x02231.v2.NonEmptyMultiLangURIType;
import org.etsi.uri.x02231.v2.PostalAddressType;
import org.etsi.uri.x02231.v2.TSPServiceType;
import org.etsi.uri.x02231.v2.TSPType;

/**
 * Java object for XML parsing of TSL trust service providers
 */
public class TrustServiceProvider {

    private TSPType tsp;
    List<TrustService> tsList = new ArrayList<TrustService>();

    public TrustServiceProvider(TSPType tsp) {
        this.tsp = tsp;
        try {
            TSPServiceType[] tSPServiceList = tsp.getTSPServices().getTSPServiceArray();
            for (TSPServiceType ts : tSPServiceList) {
                tsList.add(new TrustService(ts));
            }
        } catch (Exception ex) {
        }
    }

    public TSPType getTspData() {
        return tsp;
    }

    public List<TrustService> getTrustServices() {
        return tsList;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return getName(Locale.ENGLISH);
    }

    public String getName(Locale locale) {
        String data = "";
        try {
            MultiLangNormStringType[] name = tsp.getTSPInformation().getTSPName().getNameArray();
            data = TslUtils.getLocalisedNormString(name, locale);
        } catch (Exception ex) {
        }
        return data;
    }

    public String getTradeName() {
        return getTradeName(Locale.ENGLISH);
    }

    public String getTradeName(Locale locale) {
        String data = "";
        try {
            MultiLangNormStringType[] name = tsp.getTSPInformation().getTSPTradeName().getNameArray();
            data = TslUtils.getLocalisedNormString(name, locale);
        } catch (Exception ex) {
        }
        return data;
    }

    public List<String> getInformationUris() {
        List<String> data = new ArrayList<String>();
        try {
            List<NonEmptyMultiLangURIType> uriList = Arrays.asList(tsp.getTSPInformation().getTSPInformationURI().getURIArray());
            for (NonEmptyMultiLangURIType mlut : uriList) {
                data.add(mlut.getStringValue());
            }
        } catch (Exception ex) {
        }
        return data;
    }

    public List<String> getElectronicAddress() {
        List<String> data = new ArrayList<String>();
        try {
            data = Arrays.asList(tsp.getTSPInformation().getTSPAddress().getElectronicAddress().getURIArray());
        } catch (Exception ex) {
        }
        return data;
    }

    public PostalAddressType getPostalAddress() {
        return getPostalAddress(Locale.ENGLISH);
    }

    public PostalAddressType getPostalAddress(Locale locale) {
        PostalAddressType data = null;
        try {
            PostalAddressType[] paList = tsp.getTSPInformation().getTSPAddress().getPostalAddresses().getPostalAddressArray();
            PostalAddressType targetPa = null, defPa = null;
            for (PostalAddressType pa : paList) {
                if (pa.getLang().equalsIgnoreCase(locale.getLanguage())) {
                    targetPa = pa;
                }
                if (pa.getLang().toLowerCase(locale).startsWith(Locale.ENGLISH.getLanguage())) {
                    defPa = pa;
                }
            }
            data = (targetPa != null) ? targetPa : defPa;
        } catch (Exception ex) {
        }
        return data;
    }
}
