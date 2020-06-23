/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.resultpage;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefan
 */
@AllArgsConstructor
@Getter
public enum SamlAttribute {

    cn("urn:oid:2.5.4.3",AttributeValueType.singleValuedString, 0),
    sn("urn:oid:2.5.4.4",AttributeValueType.singleValuedString, 2),
    givenName("urn:oid:2.5.4.42",AttributeValueType.singleValuedString, 1),
    mail("urn:oid:0.9.2342.19200300.100.1.3",AttributeValueType.singleValuedString, 10),
    telephoneNumber("urn:oid:2.5.4.20",AttributeValueType.singleValuedString, 11),
    title("urn:oid:2.5.4.12",AttributeValueType.singleValuedString, 4),
    initials("urn:oid:2.5.4.43)",AttributeValueType.singleValuedString, 4),
    description("urn:oid:2.5.4.13",AttributeValueType.singleValuedString, 20),
    departmentNumber("urn:oid:2.16.840.1.113730.3.1.2",AttributeValueType.singleValuedString, 35),
    employeeNumber("urn:oid:2.16.840.1.113730.3.1.3",AttributeValueType.singleValuedString, 32),
    employeeType("urn:oid:2.16.840.1.113730.3.1.4",AttributeValueType.singleValuedString, 32),
    preferredLanguage("urn:oid:2.16.840.1.113730.3.1.39",AttributeValueType.singleValuedString, 20),
    displayName("urn:oid:2.16.840.1.113730.3.1.241",AttributeValueType.singleValuedString, 0),
    street("urn:oid:2.5.4.9",AttributeValueType.singleValuedString, 12),
    postOfficeBox("urn:oid:2.5.4.18",AttributeValueType.singleValuedString, 13),
    postalCode("urn:oid:2.5.4.17",AttributeValueType.singleValuedString, 14),
    st("urn:oid:2.5.4.8",AttributeValueType.singleValuedString, 15),
    l("urn:oid:2.5.4.7",AttributeValueType.singleValuedString, 15),
    country("urn:oid:2.5.4.6",AttributeValueType.singleValuedString, 16),
    o("urn:oid:2.5.4.10",AttributeValueType.singleValuedString, 22),
    ou("urn:oid:2.5.4.11",AttributeValueType.singleValuedString, 23),
    norEduPersonNIN("urn:oid:1.3.6.1.4.1.2428.90.1.5",AttributeValueType.singleValuedString, 3),
    mobileTelephoneNumber("urn:oid:0.9.2342.19200300.100.1.41",AttributeValueType.singleValuedString, 11),
    personalIdentityNumber("urn:oid:1.2.752.29.4.13",AttributeValueType.singleValuedString, 3),
    persistentId("urn:oid:1.3.6.1.4.1.5923.1.1.1.10",AttributeValueType.nameId, 3),
    dateOfBirth("urn:oid:1.3.6.1.5.5.7.9.1",AttributeValueType.singleValuedString, 5),
    gender("urn:oid:1.3.6.1.5.5.7.9.3",AttributeValueType.singleValuedString, 6),
    provisionalId("urn:oid:1.2.752.201.3.4",AttributeValueType.singleValuedString, 3),
    pidQuality("urn:oid:1.2.752.201.3.5",AttributeValueType.singleValuedString, 3),
    orgAffiliation("urn:oid:1.2.752.201.3.1",AttributeValueType.singleValuedString, 24),
    sad("urn:oid:1.2.752.201.3.12",AttributeValueType.singleValuedString, 50),
    affiliation("urn:oid:1.3.6.1.4.1.5923.1.1.1.9",AttributeValueType.singleValuedString, 24),
    eppn("urn:oid:1.3.6.1.4.1.5923.1.1.1.6",AttributeValueType.singleValuedString, 3),
    eduPersonAssurance("urn:oid:1.3.6.1.4.1.5923.1.1.1.11",AttributeValueType.multiValuedStrings, 30),
    eIDASPersonIdentifier("urn:oid:1.2.752.201.3.7",AttributeValueType.singleValuedString, 3),
    eduPersonUniqueId("urn:oid:1.3.6.1.4.1.5923.1.1.1.13",AttributeValueType.singleValuedString, 3),
    norEduOrgAcronym("urn:oid:1.3.6.1.4.1.2428.90.1.6",AttributeValueType.singleValuedString, 25),
    friendlyCountryName("urn:oid:0.9.2342.19200300.100.1.43",AttributeValueType.singleValuedString, 17),
    schacHomeOrganization("urn:oid:1.3.6.1.4.1.25178.1.2.9",AttributeValueType.singleValuedString, 26);

    String samlName;
    AttributeValueType valueType;
    int displayOrder;

    public static SamlAttribute getAttributeFromSamlName(String samlName) {
        for (SamlAttribute attr : values()) {
            if (attr.getSamlName().equalsIgnoreCase(samlName)) {
                return attr;
            }
        }
        return null;
    }

    public static List<SamlAttribute> getIdAttributes() {
        List<SamlAttribute> attrList = new ArrayList<SamlAttribute>();
        attrList.add(personalIdentityNumber);
        attrList.add(provisionalId);
        attrList.add(sad);
        attrList.add(eppn);
        attrList.add(eIDASPersonIdentifier);
        attrList.add(eduPersonUniqueId);
        
        return attrList;
    }
    
    public static List<SamlAttribute> getDisplayNamedAttributes() {
        List<SamlAttribute> attrList = new ArrayList<SamlAttribute>();
        attrList.add(displayName);
        attrList.add(cn);        
        return attrList;
    }
    
    

}
