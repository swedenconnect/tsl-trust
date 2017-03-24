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
