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

import java.util.ArrayList;
import java.util.List;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.CriteriaListType;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.QualifierType;

/**
 * Class holding data about a Qualifications element in a Qualifications Sie
 */
public class QualificationsElement {

    List<String> qualifierUriList = new ArrayList<String>();
    QualificationsCriteria criteria;

    public QualificationsElement(QualifierType[] qfArray, CriteriaListType cl) {
        for (QualifierType qf : qfArray) {
            String qualifierUri = qf.getUri();
            if (qualifierUri != null) {
                qualifierUriList.add(qualifierUri);
            }
        }
        criteria = new QualificationsCriteria(cl);
    }

    /**
     * Getter for the criteria element if present
     * @return Qualifications criteria or null if no such information is present
     */
    public QualificationsCriteria getCriteria() {
        return criteria;
    }

    /**
     * Getter for the Qualifier URI list (If present)
     * @return List of Qualifications criteria URIs
     */
    public List<String> getQualifierUriList() {
        return qualifierUriList;
    }
}
