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
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.QualificationElementType;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.QualificationsType;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.QualifierType;
import org.w3c.dom.Node;

/**
 * Class holding information about a Qualifications extension.
 */
public class QualificatioinsSie implements ServiceInfoExtension{
    
    private Node node;
    private QualificationsType qf;
    boolean critical;
    List<QualificationsElement> qfElementList = new ArrayList<QualificationsElement>();

    /**
     * Constructor
     * @param qualifications the extension element.
     * @param sie The extension XML node
     * @param critical true if the extension is critical, otherwise false.
     */
    public QualificatioinsSie(QualificationsType qualifications, Node sie, boolean critical) {
        this.node = sie;
        this.critical = critical;
        this.qf = qualifications;
        parse();
    }        

    @Override
    public SieType getType() {
        return SieType.Qualifications;
    }

    @Override
    public String getInfo() {
        return "Qualifications - Service Information Extension";
    }

    @Override
    public String getName() {
        return "Qualifications";
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
     * @return The extension element
     */
    public QualificationsType getQualificationsElement() {
        return qf;
    }

    /**
     * Getter for a list of qualification elements found in the extension
     * @return List of qualifications elements
     */
    public List<QualificationsElement> getQualificationsList() {
        return qfElementList;
    }      
    
    private void parse(){
        QualificationElementType[] qfea = qf.getQualificationElementArray();
        for (QualificationElementType qfe:qfea){
            CriteriaListType cl = qfe.getCriteriaList();
            QualifierType[] qfa = qfe.getQualifiers().getQualifierArray();
            qfElementList.add(new QualificationsElement(qfa, cl));
        }
    }        
}
