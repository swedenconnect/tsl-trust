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
package se.tillvaxtverket.tsltrust.common.tsl.sie;

import java.util.Locale;
import org.etsi.uri.x02231.v2.AnyType;
import org.etsi.uri.x02231.v2.MultiLangNormStringType;
import org.etsi.uri.x02231.v2.additionaltypes.TakenOverByType;
import org.w3c.dom.Node;
import se.tillvaxtverket.tsltrust.common.tsl.TslUtils;

/**
 * Class holding data about a Taken Over By Extension
 */
public class TakenOverBySie implements ServiceInfoExtension {

    private Node node;
    private TakenOverByType to;
    boolean critical;

    /**
     * Constructor
     * @param takenOverBy The extension element
     * @param sie The extension XML Node
     * @param critical  true if the extension is critical, otherwise false.
     */
    public TakenOverBySie(TakenOverByType takenOverBy, Node sie, boolean critical) {
        this.node = sie;
        this.critical = critical;
        this.to = takenOverBy;
    }

    @Override
    public SieType getType() {
        return SieType.TakenOverBy;
    }

    @Override
    public String getInfo() {
        return "Taken Over By - Service Information Extension";
    }

    @Override
    public String getName() {
        return "Taken over by";
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public boolean isCritical() {
        return critical;
    }

    /**
     * Getter for the extension element
     * @return extension element
     */
    public TakenOverByType getExtensionElement() {
        return to;
    }

    /**
     * Getter for Scheme operator name
     * @return Scheme operator name in English Locale
     */
    public String getSchemeOperatorName() {
        return getSchemeOperatorName(Locale.ENGLISH);
    }


    /**
     * Getter for Scheme operator name
     * @param locale preferred language
     * @return Scheme operator name in specified Locale
     */
    public String getSchemeOperatorName(Locale locale) {
        String data = null;
        try {
            MultiLangNormStringType[] nameArray = to.getSchemeOperatorName().getNameArray();
            data = TslUtils.getLocalisedNormString(nameArray, locale);
        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * Getter for TSP Name
     * @return TSP Name in English Locale
     */
    public String getTspName() {
        return getTspName(Locale.ENGLISH);
    }

    /**
     * Getter for TSP Name
     * @param locale preferred language
     * @return TSP name in the specified language (if present).
     */
    public String getTspName(Locale locale) {
        String data = null;
        try {
            MultiLangNormStringType[] nameArray = to.getTSPName().getNameArray();
            data = TslUtils.getLocalisedNormString(nameArray, locale);
        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * Getter for Scheme territory
     * @return Scheme territory
     */
    public String getSchemeTerritory() {
        return to.getSchemeTerritory();
    }

    /**
     * Getter for information URI
     * @return Information URI
     */
    public String getURI() {
        String data = null;
        try {
            data = to.getURI().getStringValue();
        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * Getter for the Any Type extensible value of the extension
     * @return Other Qualifiers Any Type.
     */
    public AnyType[] getOtherQualifiers() {
        return to.getOtherQualifierArray();
    }
}
