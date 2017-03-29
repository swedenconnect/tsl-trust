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
