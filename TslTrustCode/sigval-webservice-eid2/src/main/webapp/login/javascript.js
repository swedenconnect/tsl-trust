/**
 * Javascript, providing login functions for the sample signature validation web application.
 * The provided signature validation web pages (including the login page), this script and
 * the provided servlet implementation is provided for demonstration purposes only.
 * 
 * It is assumed that any serious production deployment of a signature validation service
 * will replace or modify this web application to meet local needs with respect to 
 * user inteface, language and API requirements.
 * 
 * This login service is a sample login service that integrates with a servlet deployed behind a web server
 * supporting SAML based authenticatioin using a Shibboleth SP module.
 * 
 * In particular, this sample implementation integrates with the Swedish testinfrastructure
 * EID-2.0 for national federated authentication.
 */
var feedSource = "source=https://eid2.3xasecurity.com/Shibboleth.sso/DiscoFeed";
//var jsonpDisco="https://ds.test.eid2.se/JsonDiscoFeed/";  // Alternative discovery feed source
var jsonpDisco="https://jsonpdisco.3xasecurity.com/"; //Discovery data json feed source
var entityID=new Array(); // Array holding IdP entity IDs obtained from the discovery feed source
var displayName=new Array(); // Array holding display names of IdPs obtained from the discovery feed source

/**
 * Functions performed on page load
 */
$(document).ready(function() {
    if (getQueryVariable("logout")=="true"){
        $(".information").hide();
        alert("Du har blivit utloggad. För säkrast möjliga utloggning, stäng ner din webbläsare");
        window.location="https://eid2.3xasecurity.com/login/";
    }
    eid2DiscoUI();
    $("#sessionFrame").hide();
    if ($.cookie("showInfo")=="1"){
        $("#infoBox").prop("checked", true);
        $(".information").show();
    } else {
        $(".information").hide();        
    }
});

///**
// * Gets information about available IdPs from a JSONP discovery data feed and
// * builds UI components for selecting an IdP for login
// */
//function getDiscoFeed(){
//    var at = docRow(1,"prop",str("<u>Anslutna identitetsutfärdare</u>"));
//    var previous;
//    var prevIdx =-1;
//
//    $.getJSON(jsonpDisco+'/feed?action=discoFeed&callback=?', function(data) {
//        previous = data.last[0].entityID;
//        $.each(data.discoFeed, function(i,idp){
//            entityID[i]=idp.entityID;
//            if (entityID[i]==previous){
//                prevIdx=i;
//            }
//            displayName[i]=idp.DisplayNames[0].value;            
//            $.each(idp.DisplayNames, function(j,idpName){
//                if (idpName.lang == "sv"){
//                    displayName[i]=idpName.value;
//                }
//            });
//            at+=docRow(2,"prop","-",displayName[i]);
//            $("<option></option>").html(displayName[i]).appendTo("#idpSelect");
//        });
//        if (prevIdx>0){
//            $('#idpSelect option')[prevIdx].selected = true;
//        }
//        $("#idpTable").append(at);
//    }).error(function() {
//        $("#idpArea").hide();
//    });
//}
//
///**
//* Login action for logging in to the signature validation service using the selected IdP 
//*/
//function idpLogin(){
//    var i = document.getElementById('idpSelect').selectedIndex;
//    $.getJSON(jsonpDisco+'/feed?action=setCookie&entityID='
//        +entityID[i]+'&maxAge=120&callback=?', function(data) {
//            window.location = "https://eid2.3xasecurity.com/Shibboleth.sso/Login?entityID="
//            + entityID[i] + "&target=https://eid2.3xasecurity.com/sig/";
//        });
//}


function eid2DiscoUI(){
    $.eid2Disco({
        discodiv:"discodiv",
        success : function(entityId){
            login(entityId);
        },
        error : function(data){
            errorFunction(data);
        }
    });

}

function login(idpEntityId){
    window.location = "https://eid2.3xasecurity.com/Shibboleth.sso/Login?entityID="
    + encodeURIComponent(idpEntityId) + "&target="+encodeURIComponent("https://eid2.3xasecurity.com/sig/");
}

function errorFunction(message){
    alert(message);
}

/**
 * Utility. Returns a string with bold tags
 */
function b(txt){
    return "<b class='colored'>"+txt+"</b>";
}

/**
 * Utility. Returns a string with strong tags
 */
function str(txt){
    return "<strong class='colored'>"+txt+"</strong>";
}

/**
 * Utility. Returns a string with heading 3 tags
 */
function h3(txt){
    return "<h3 class='fat'>"+txt+"</h3>";
}


/**
 * Constructs a table row based on provided parameters. 
 */
function docRow (elements, style, p1,p2,p3){
    var tr = getTrClass(style);
    if (elements==1){
        return tr+'<td colspan="3" class="'+style+'">'+p1+"</td></tr>";
    }
    if (elements==2){
        return tr+"<td>"+p1+'</td><td  colspan="2" class="'+style+'">'+p2+"</td></tr>";
    }
    if (elements==3){
        return tr+"<td>"+p1+'</td><td class="'+style+'">'+p2+"</td><td>"+p3+"</td></tr>";
    }
    return "";
}

/**
 * Constructs a table row based on provided parameters. 
 */
function docRowB (style, p1,p2,p3){
    var tr = getTrClass(style);
    return tr+"<td>"+p1+'</td><td class="'+style+'">'+p2+'</td><td class="'+style+'">'+p3+"</td></tr>";
}

/**
 * Returns class names for different display features 
 */
function getTrClass (style){
    var tr = "<tr>";
    if (style == "errorMess" || style=="warnMess"){
        tr='<tr class="errorRow">';
    }
    if (style == "verbAttr"){
        tr='<tr class="verboseRow">';
    }
    return tr;    
}

/**
 * Switches on or off display for current authenticated session and a list of available IdPs
 */
function showInfo(){
    if ($("#infoBox").attr('checked')){
        $(".information").fadeIn(500);
        $.cookie("showInfo", "1");
    } else {
        $(".information").fadeOut(500);
        $.cookie("showInfo", "0");
    }    
}

/**
 * Returns the requested query string value provided when loading the web page
 */
function getQueryVariable(variable) { 
    var query = window.location.search.substring(1); 
    var vars = query.split("&"); 
    for (var i=0;i<vars.length;i++) { 
        var pair = vars[i].split("="); 
        if (pair[0] == variable) { 
            return pair[1]; 
        } 
    }
    return "";
} 

