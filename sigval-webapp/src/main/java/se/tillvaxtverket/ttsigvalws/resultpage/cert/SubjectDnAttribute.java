/*
 * Copyright 2019-2020 IDsec Solutions AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.tillvaxtverket.ttsigvalws.resultpage.cert;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of common certificate subject attributes.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
 @AllArgsConstructor
public enum SubjectDnAttribute {

  /** Common name. */
  cn("2.5.4.3", 0),

  /** Given name. */
  givenName("2.5.4.42", 1),

  /** Surname. */
  surname("2.5.4.4", 2),

  /** Personal identity number. */
  personalIdentityNumber("1.2.752.29.4.13", 4),

  /** Country. */
  country("2.5.4.6", 11),

  /** Locality. */
  locality("2.5.4.7",10),

  /** Serial number. */
  serialNumber("2.5.4.5", 5),

  /** Organization name. */
  organizationName("2.5.4.10", 6),

  /** Organizational unit name. */
  organizationalUnitName("2.5.4.11", 7),

  /** Organization identifier. */
  organizationIdentifier("2.5.4.97", 8),

  /** Pseudonym. */
  pseudonym("2.5.4.65",0),

  /** DN qualifier. */
  dnQualifier("2.5.4.46",15),

  /** Title. */
  title("2.5.4.12",3),

  /** Unknown. */
  unknown("",20);

  /**
   * Based on the supplied OID the method returns the corresponding enum.
   * 
   * @param oid
   *          the object identifier of the attribute
   * @return a SubjectDnAttribute enum
   */
  public static SubjectDnAttribute getSubjectDnFromOid(final String oid) {
    for (SubjectDnAttribute subjDn : values()) {
      if (oid.equalsIgnoreCase(subjDn.getOid())) {
        return subjDn;
      }
    }
    return unknown;
  }

  /** The object identifier of the attribute. */
  @Getter
  private final String oid;
  /** The preferred display order index */
  @Getter
  private final int order;

}
