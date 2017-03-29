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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

/**
 *
 * @author stefan
 */
public enum SubjectDnAttribute {
    cn("2.5.4.3"),
    givenName("2.5.4.42"),
    surname("2.5.4.4"),
    personnummer("1.2.752.29.4.13"),
    country("2.5.4.6"),
    locality("2.5.4.7"),
    serialNumber("2.5.4.5"),
    orgnaizationName("2.5.4.10"),
    orgnaizationalUnitName("2.5.4.11"),
    organizationIdentifier("2.5.4.97"),
    pseudonym("2.5.4.65"),
    dnQualifier("2.5.4.46"),
    title("2.5.4.12"),
    unknown("");
    
    private final String oid;

    private SubjectDnAttribute(String oid) {
        this.oid = oid;
    }

    public String getOid() {
        return oid;
    }
    
    public static SubjectDnAttribute getSubjectDnFromOid (String oid){
        for (SubjectDnAttribute subjDn:values()){
            if (oid.equalsIgnoreCase(subjDn.getOid())){
                return subjDn;
            }
        }
        return unknown;
    }
    
}
