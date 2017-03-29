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


$(document).ready(function() {
    if (getQueryVariable("logout")=="true"){
        $(".information").hide();
//        alert("You have been logged out from the service.\nShut down your browser to disble any further use of your authenticated identity");
        window.location = loginURL;
    }
    $("#loginArea").hide();
    getDiscoFeed();
    if ($.cookie("showInfo")=="1"){
        $("#infoBox").prop("checked", true);
        $(".information").show();
    } else {
        $(".information").hide();        
    }
});

function b(txt){
    return "<b class='colored'>"+txt+"</b>";
}

function str(txt){
    return "<strong class='colored'>"+txt+"</strong>";
}

function h3(txt){
    return "<h3 class='fat'>"+txt+"</h3>";
}


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

function docRowB (style, p1,p2,p3){
    var tr = getTrClass(style);
    return tr+"<td>"+p1+'</td><td class="'+style+'">'+p2+'</td><td class="'+style+'">'+p3+"</td></tr>";
}

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

function showInfo(){
    if ($("#infoBox").attr('checked')){
        $(".information").fadeIn(500);
        $.cookie("showInfo", "1");
    } else {
        $(".information").fadeOut(500);
        $.cookie("showInfo", "0");
    }    
}

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

function getDiscoFeed(){
//    var at = docRow(1,"prop",str("<u>Available Identity Providers</u>"));
//    var previous;
//    var prevIdx =-1;
//
//    // Provide Guest login alternative
//    $("<option></option>").html("--Anonymous/Guest--").appendTo("#idpSelect");
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
//        if (prevIdx>-1){
//            $('#idpSelect option')[prevIdx+1].selected = true;
//        }
//        $("#idpTable").append(at);
//    }).error(function() {
//        $("#idpArea").hide();
//    });
}

function idpLogin(){
    var seed=Math.floor(Math.random()*100000001)
    window.location= serviceURL +"index.jsp?nonce="+seed;
}
//function idpLogin(){
//    $(".information").fadeOut(100);
//    $("#loginText").hide();
//
//    var i = document.getElementById('idpSelect').selectedIndex;
//    if (i==0){
//        window.location="https://ttadmin.konki.se/tsltrustguest/";
//    } else {
//        i-=1;
//        if (i>-1){
//            $.getJSON(jsonpDisco+'/feed?action=setCookie&entityID='
//                +entityID[i]+'&maxAge=120&callback=?', function(data) {        
//                    var source = "https://ttadmin.konki.se/Shibboleth.sso/Login?entityID="
//                    + entityID[i] + "&target=https://ttadmin.konki.se/tsltrust/open.html";
//                    //        $('<iframe id="loginFrame" width="80%" height="500" frameborder="0" src="http://aaa-sec.com">').appendTo        
//                    //        $('<iframe id="loginFrame" width="80%" height="500" frameborder="0" src="'
//                    //                +source+'>').appendTo('#loginDiv');
//                    $("#loginIframe").attr('src',source);
//                    $("#loginArea").fadeIn(200);
//                });                    
//        }
//    }
//    
//}

