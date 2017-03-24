$(document).ready(function() {
    if (getQueryVariable("logout")=="true"){
        alert("Du har blivit utloggad. För säkrast möjliga utloggning, stäng ner din webbläsare");
        window.location="https://eid2.3xasecurity.com/login/";
    }
    getDiscoFeed();
    if ($.cookie("showInfo")=="1"){
        $("#infoBox").prop("checked", true);
        $(".information").show();
    } else {
        $(".information").hide();        
    }
});
            
var entityID=new Array();
var displayName=new Array();


function getDiscoFeed(){
    var at = docRow(1,"prop",str("<u>Anslutna identitetsutfärdare</u>"));
    var previous = $.cookie("selectedIdp");
    var prevIdx =-1;
    $("#idpTable").empty();
    $("#idpSelect").empty();
  $.getJSON('TTloginServlet?action=jsonfeed', function(data) {
//    $.getJSON('/Shibboleth.sso/DiscoFeed', function(data) {        
        $.each(data, function(i,idp){
            entityID[i]=idp.entityID;
            if (entityID[i]==previous){
                prevIdx=i;
            }
            displayName[i]=idp.DisplayNames[0].value;            
            $.each(idp.DisplayNames, function(j,idpName){
                if (idpName.lang == "sv"){
                    displayName[i]=idpName.value;
                }
            });
            at+=docRow(2,"prop","-",displayName[i]);
            $("<option></option>").html(displayName[i]).appendTo("#idpSelect");
        });
        if (prevIdx!=-1){
            $('#idpSelect option')[prevIdx].selected = true;
        }
        $("#idpTable").append(at);
    }).error(function() { 
        $("#idpArea").hide();
    });
}


function idpLogin(){
    var i = document.getElementById('idpSelect').selectedIndex;
    $.cookie("selectedIdp", entityID[i], { expires: 100 });
    window.location = "https://eid2.3xasecurity.com/Shibboleth.sso/Login?entityID="
    + entityID[i] + "&target=https://eid2.3xasecurity.com/sig/";    
}

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

//function getDiscoFeedExtended(){
//    var at = docRow(1,"prop",str("<u>Anslutna identitetsutfärdare</u>"));
//    var previous;
//    var prevIdx =-1;
//    var nonce = Math.floor(Math.random()*1000001) // nonce in request to prevent caching
//    $.getJSON('TTloginServlet?action=discoFeed&nonce='+nonce, "null", function(data) {
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
//function idpLoginExtended(){
//    var i = document.getElementById('idpSelect').selectedIndex;
//    $.getJSON('TTloginServlet?action=setCookie&entityID='+entityID[i], function(data) {
//        window.location = "https://eid2.3xasecurity.com/Shibboleth.sso/Login?entityID="
//        + entityID[i] + "&target=https://eid2.3xasecurity.com/sig/";    
//    });
////    $.cookie("selectedIdp", entityID[i]);
//}

