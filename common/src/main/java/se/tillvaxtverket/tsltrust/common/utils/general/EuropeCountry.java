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
package se.tillvaxtverket.tsltrust.common.utils.general;

/**
 * European country names
 */
public enum EuropeCountry {

    EU("EU", "European Union", "European Union", "European Union"),
    BE("BE", "Belgique/België", "Belgium", "Kingdom of Belgium"),
    BG("BG", "България (*)", "Bulgaria", "Republic of Bulgaria"),
    CZ("CZ", "Česká republika", "Czech Republic", "Czech Republic"),
    DK("DK", "Danmark", "Denmark", "Kingdom of Denmark"),
    DE("DE", "Deutschland", "Germany", "Federal Republic of Germany"),
    EE("EE", "Eesti", "Estonia", "Republic of Estonia"),
    IE("IE", "Éire/Ireland", "Ireland", "Ireland"),
    EL("GR", "Ελλάδα (*)", "Greece", "Hellenic Republic"),
    ES("ES", "España", "Spain", "Kingdom of Spain"),
    FR("FR", "France", "France", "French Republic"),
    IT("IT", "Italia", "Italy", "Italian Republic"),
    IS("IS", "Ísland", "Iceland", "Iceland"),
    CY("CY", "Κύπρος/Kıbrıs (*)", "Cyprus", "Republic of Cyprus"),
    LI("LI", "Liechtenstein", "Liechtenstein", "Principality of Liechtenstein"),
    LV("LV", "Latvija", "Latvia", "Republic of Latvia"),
    LT("LT", "Lietuva", "Lithuania", "Republic of Lithuania"),
    LU("LU", "Luxembourg", "Luxembourg", "Grand Duchy of Luxembourg"),
    HU("HU", "Magyarország", "Hungary", "Republic of Hungary"),
    MT("MT", "Malta", "Malta", "Republic of Malta"),
    NL("NL", "Nederland", "Netherlands", "Kingdom of the Netherlands"),
    AT("AT", "Österreich", "Austria", "Republic of Austria"),
    PL("PL", "Polska", "Poland", "Republic of Poland"),
    PT("PT", "Portugal", "Portugal", "Portuguese Republic"),
    RO("RO", "România", "Romania", "Romania"),
    SI("SI", "Slovenija", "Slovenia", "Republic of Slovenia"),
    SK("SK", "Slovensko", "Slovakia", "Slovak Republic"),
    FI("FI", "Suomi/Finland", "Finland", "Republic of Finland"),
    SE("SE", "Sverige", "Sweden", "Kingdom of Sweden"),
    NO("NO", "Norge", "Norway", "Kingdom of Norway"),
    HR("HR", "Hrvatska", "Croatia", "Republic of Croatia"),
    UK("GB", "United Kingdom", "United Kingdom", "United Kingdom of Great Britain and Northern Ireland");
    private final String isoCode;
    private final String shortSrcLangName;
    private final String shortEnglishName;
    private final String officialEnglishName;

    private EuropeCountry(final String isoCode, final String shortSrcLangName,
            final String shortEnglishName, final String officialEnglishName) {
        this.isoCode = isoCode;
        this.officialEnglishName = officialEnglishName;
        this.shortEnglishName = shortEnglishName;
        this.shortSrcLangName = shortSrcLangName;
    }

    public String getIsoCode() {
        return this.isoCode;
    }

    public String getShortSrcLangName() {
        return this.shortSrcLangName;
    }

    public String getShortEnglishName() {
        return this.shortEnglishName;
    }

    public String getOfficialEnglishName() {
        return this.officialEnglishName;
    }
}
