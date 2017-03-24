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
package se.tillvaxtverket.tsltrust.weblogic.content;

/**
 * HTML UI generation constants
 */
public interface HtmlConstants {

    //Generic
    static final String SPACE = "&nbsp;";
    //Element names
    static final String MAIN_DATA_AREA = "mainArea";
    static final String MAIN_TSL_DIV = "mainTslDiv";
    static final String TSL_TABLE_DIV = "tslDiv";
    static final String POLICY_INFOTABLE_DIV = "policyInfoTableDiv";
    static final String TSL_INFO_DIV = "tslInfoDiv";
    static final String PRESENTATION_INFO_DIV = "presentationInfoDiv";
    static final String INFO_HEAD_DIV = "infoHeadDiv";
    static final String INFO_BODY_DIV = "infoBodyDiv";
    static final String XML_DIV = "xmlDataDiv";
    static final String VIEW_BUTTON = "viewButton";
    static final String IFRAME_MAIN_DIV = "infoDataDiv";
    //Class names    
    static final String MENU_CLASS = "menu";
    static final String SUBMENU_CLASS = "subMenu";
    static final String SELECTED_CLASS = "selected";
    static final String TABLE_ROW_SELECTED = "dataSelected";
    static final String TABLE_HEAD_CLASS = "tableHead";
    static final String TABLE_ODD_CLASS = "tableOdd";
    static final String TABLE_EVEN_CLASS = "tableEven";
    static final String TABLE_NEUTRAL_CLASS = "tableNeutral";
    static final String TABLE_STRIPED_CLASS = "tableStriped";
    static final String TABLE_SECTION_HEAD = "tableSectionHead";
    static final String TABLE_UNFOLD_HEAD = "tableUnfoldHead";
    static final String TABLE_UNFOLD_BOLD = "tableUnfoldBold";
    static final String TABLE_SECTION_ROW = "tableSectionRow";
    static final String TABLE_SECTION_ROW_ODD = "tableSectionRowOdd";
    static final String TABLE_SECTION_ROW_EVEN = "tableSectionRowEven";
    static final String TABLE_FOLD_ICON_CELL = "tableFoldIcon";
    static final String FOLD_ICON = "foldIcon";
    static final String UNFOLD_ICON = "unfoldIcon";
    static final String INFO_TABLE_CLASS = "infoTable";
    static final String INHERIT_BACKGROUND_HEAD = "inheritBgHead";
    static final String INVERTED_HEAD = "invertedHead";
    static final String ATTRIBUTE_NAME = "attrName";
    static final String ATTRIBUTE_VALUE = "attrValue";
    static final String PROPERTY_NAME = "propertyName";
    static final String PROPERTY_VALUE = "propertyValue";
    static final String EXTENSION_NAME = "extensionName";
    static final String EXTESION_PADDING = "extensionPadding";
    static final String ERROR = "error";
    static final String WARNING = "warning";
    static final String GOOD = "good";
    static final String CODE_TEXT = "codeText";
    static final String LABEL_TEXT = "labelText";
    static final String INFO_TABLE_AJAX_LOAD = "itAjaxLoad";
    
    // Arrays
    static final String[] TABLE_CLASS = new String[]{TABLE_HEAD_CLASS, TABLE_ODD_CLASS, TABLE_EVEN_CLASS};
    //JavaScript events
    static final String ONCLICK = "onclick";
    static final String ONCHANGE = "onchange";
    static final String ONMOUSEOVER = "onmouseover";
    static final String ONMOUSEOUT = "onmouseout";
    static final String ONKEYUP = "onkeyup";
    static final String ONMOUSEUP = "onmouseup";
    
    //JavaScript Functions
    /**
     * Loads HTML data. Requires three parameters (target, id, parameter)
     * target=the target element to be loaded with data, id=identifier of the action
     * needed to generate the data (e.g. identifier of the originator of the request),
     * parameter=a parameter for the load function.
     */
    static final String LOAD_DATA_FUNCTION = "loadElementData";
    /**
     * Loads HTML data in two consecutive steps. Requires four parameters (selected, target, id, parameter).
     * selected=the selected menu to be loaded first. target=the target element to be loaded with data, id=identifier of the action
     * needed to generate the data (e.g. identifier of the originator of the request),
     * parameter=a parameter for the load function.
     * The last frame load is repeated by timer until interrupted by other function.
     */
    static final String TWO_STEP_MENU_FUNCTION = "twoStepLoadMenu";
    /**
     * Loads HTML data in two consecutive steps. Requires six parameters (target, id, parameter)x2, one set for each load function.
     * target=the target element to be loaded with data, id=identifier of the action
     * needed to generate the data (e.g. identifier of the originator of the request),
     * parameter=a parameter for the load function.
     * The last frame load is repeated by timer until interrupted by other function.
     */
    static final String TWO_STEP_LOAD_FUNCTION = "twoStepLoad";
    /**
     * Function within an iframe that loads a new parent window. 
     * Requires three parameters (target, id, parameter)
     * target=the target parent window  to be loaded, id=identifier of the action
     * needed to prepare the server for the parent frame reload,
     * parameter=a parameter for the action specified by the id parameter.
     */
    static final String FRAME_LOAD_FUNCTION = "frameLoad";
    /**
     * Repeatedly Loads HTML data. Requires four parameters (target, id, parameter, time)
     * target=the target element to be loaded with data, id=identifier of the action
     * needed to generate the data (e.g. identifier of the originator of the request),
     * parameter=a parameter for the load function. time is the amount of milliseconds to wait
     * after a successful load until a new data load is initiated.
     */
    static final String REPEAT_LOAD_FUNCTION = "repeatLoadData";
    /**
     * Selects a menu form the Main menu. Requires 1 parameter 
     * (selected = integer of the selected menu index)
     */
    static final String SELECT_MENU_FUNCTION = "selectMenu";
    /**
     * Selects a menu form the Main menu and repeats sub content. Requires 2 parameters 
     * (selected = integer of the selected menu index. timer = time in milliseconds 
     * between sub content refresh)
     */
    static final String REPEAT_MENU_FUNCTION = "repeatMenuSubContent";
    /**
     * Hides or shows data. Requires 4 parameters (selector, hideTarget, id, parameter)
     * selector = the checkbox (or similar) element holding status of hide or show (selected = show).
     * hideTarget= hide element. After hide a Load data function is executed with parameters (id,id,parameter).     * 
     */
    static final String HIDE_OR_SHOW_FUNCTION = "hideOrShow";
    /**
     * Executes the selected option of a selectbox. Requires 3 parameters (target, id, parameter)
     * Target = the element to be loaded with data. Id = identifies the action when forming the data.
     * parameter = the name of the select box holding the oprion elements.
     */
    static final String EXECUTE_OPTION_FUNCTION = "executeOption";
    /**
     * Logs out the user (Local SP logout)
     */
    static final String LOGOUT_FUNCTION = "userLogout";
    /**
     * Logout in development mode. This transfers the user to the "user.jsp" page for selection of
     * a new cookie based mockup identity.
     */
    static final String DEV_LOGOUT_FUNCTION = "devLogout";
    /**
     * Execute the settings of a group of select boxes. Requires 4 parameters (target, id, selectId, size)
     * target = the element to be loaded with html. id= an identifier of the action to be performed.
     * selectID. the name of the select box group, where each selectbox has the name "selectID"+index.
     * size = the number of select boxes each indexed from 0-size.
     */
    static final String EXECUTE_SELECTED_FUNCTION = "executeSelected";
    /**
     * Sends the information in an identified input element to the server. Requires 3 parameters (target, id, inputFieldId)
     * target = the element to be loaded with html. id= an identifier of the action to be performed.
     * inputFieldId = the name of the input field holding the text value to return to the server.
     */
    static final String SEND_INPUT_FUNCTION = "sendInputField";
    /**
     * Fold or unfolds table data cells. Requires 3 parameters (hideitemId, showItemId, tableName)
     */
    static final String FOLD_UNFOLD_FUNCTION = "foldUnfold";
    /**
     * Fold or unfolds table data cells. Requires 3 parameters (hideitemId, showItemId, tableName)
     */
    static final String AJAX_UNFOLD_FUNCTION = "ajaxUnfold";
    /**
     * Change icon image on mouseover. Requires two parameter (url to new icon src.
     */
    static final String CHANGE_ICON_FUNCTION = "changeIcon";
}
