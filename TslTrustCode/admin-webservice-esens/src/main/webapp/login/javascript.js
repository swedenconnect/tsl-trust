//var feedSource = "source=https://ttadmin.konki.se/Shibboleth.sso/DiscoFeed";
//var jsonpDisco="https://ds.test.eid2.se/JsonDiscoFeed/";
//var jsonpDisco="https://jsonpdisco.3xasecurity.com/";
//var entityID=new Array();
//var displayName=new Array();

// Old
//var loginURL = "https://ttadmin.konki.se/login/";
//var serviceURL = "https://ttadmin.konki.se/tsltrust/";
var loginURL = "http://esp-ttadmin.se/login/";
var serviceURL = "https://tsltrust.esp-ttadmin.se/admin/";
var authServiceLoginUrl = "https://tsltrust.esp-ttadmin.se/Shibboleth.sso/Login";


$(document).ready(function() {
    if (getQueryVariable("logout") === "true") {
        $(".information").hide();
//        alert("You have been logged out from the service.\nShut down your browser to disble any further use of your authenticated identity");
        window.location = loginURL;
    } else {
        $.eid2Disco({
            lang: "en",
//            jsonp: false,
//            discofeed: "/discofeed",
//            storagefeed: "/discopref",
            discodiv: "eid2Disco",
            success: function(entityId) {
                idpLogin(entityId);
            },
            error: function(message) {
                alert(message);
            }
        });
    }
});


function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] === variable) {
            return pair[1];
        }
    }
    return "";
}

/**
 * Executes login to the service using the selected IdP for user authentication * 
 * @param {type} idp The entity ID of the identity service provider
 */
function idpLogin(idp) {
    window.location = authServiceLoginUrl + "?entityID="
            + encodeURIComponent(idp) + "&target=" + encodeURIComponent(serviceURL);
}

