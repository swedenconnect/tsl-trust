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
