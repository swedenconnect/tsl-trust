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
package se.tillvaxtverket.tsltrust.weblogic.workareas;

import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;

/**
 * Abstract class for content providers
 */
public abstract class WorkArea implements TTConstants {

    /**
     *     static final int AUTH_CONTEXT_POLICY_MGMNT = 0;
     *     static final int AUTH_CONTEXT_CERT_MGMNT = 1;
     *     static final int AUTH_CONTEXT_AUTHORIZATION = 2;
     *     static final int AUTH_CONTEXT_LOGGING = 3;
     */
    protected Integer[][] authorizedRoles = new Integer[][]{
        //Policy management
        new Integer[]{ADM_ROLE_SUPER_ADMIN,ADM_ROLE_POLICY_ADMIN}, //Set that only Policy admin (role 2) has right to do changes/updates
        // Certificate management
        new Integer[]{ADM_ROLE_SUPER_ADMIN,ADM_ROLE_ADMIN, ADM_ROLE_POLICY_ADMIN, ADM_ROLE_LOG_ADMIN},
        // Authorization
        new Integer[]{ADM_ROLE_SUPER_ADMIN,ADM_ROLE_ADMIN},
        // Logging
        new Integer[]{ADM_ROLE_SUPER_ADMIN,ADM_ROLE_ADMIN, ADM_ROLE_LOG_ADMIN},
        // Request Authorization
        new Integer[]{ROLE_GUEST}
    };

    public abstract String getHtmlData(RequestModel req);

    /**
     * Gets the integer representation of an input string.
     * @param intString input string
     * @return integer representation of the input string, returns 0 of the string is not a legitimate integer string.
     */
    protected int getInt(String intString) {
        int val = 0;
        try {
            val = Integer.parseInt(intString);
        } catch (Exception ex) {
        }
        return val;
    }

    /**
     * Provides an empty html table with preset loading text
     * @return table html element
     */
    protected HtmlElement getLoadingTable() {
        HtmlElement emptyTable = new GenericHtmlElement("table");
        HtmlElement tr = new TableRowElement();
        HtmlElement td = new TableCellElement("Loading Data...");
        emptyTable.addHtmlElement(tr);
        tr.addHtmlElement(td);
        return emptyTable;
    }

    /**
     * Test whether the current user is authorized within a given application context
     * @param userRole The authenticated role of the user
     * @param authzContext the application context within which the user needs to be authorixed.
     * @return true if the user is authorixed, else false.
     */
    protected boolean authorized(int userRole, int authzContext) {
        Integer[] contextRoles = authorizedRoles[authzContext];
        boolean authorized = false;
        for (int role : contextRoles) {
            if (role == userRole) {
                authorized = true;
            }
        }
        return authorized;
    }
}
