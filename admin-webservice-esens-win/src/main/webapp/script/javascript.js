var baseUrl = "TThtml?action=";
var tr;
var repeatTime=5000;
var repeatId;
var repeatTarget;
var repeatPara;
var stopTimer="";
//var logoutURL = "/Shibboleth.sso/LocLogout?return=https://ttadmin.konki.se/login/index.jsp?logout=true";
var logoutURL = "/Shibboleth.sso/LocLogout?return=http:/esp-ttadmin.se/login/index.jsp?logout=true";

$(document).ready(function() {
    loadHtml("mainMenu","loadMenu");
    loadHtml("mainArea", "loadMain");
});


function loadHtml (container, action){
    var seed=Math.floor(Math.random()*100000001)
    $("#"+container).load(baseUrl + action +"&container="+container+getHeight()+"&nonce="+seed);
}

function selectMenu(selected){
    clearTimeout(tr);
    var seed=Math.floor(Math.random()*100000001)    
    $("#mainMenu").load(baseUrl + "loadMenu&parameter="+encodeURIComponent(selected)+"&nonce="+seed, function(){
        loadHtml("mainArea", "loadMain");
    });
}

function repeatMenuSubContent(selected, time){
    clearTimeout(tr);
    repeatTime=time;
    var seed=Math.floor(Math.random()*100000001); 
    $("#mainMenu").load(baseUrl + "loadMenu&parameter="+encodeURIComponent(selected)+"&nonce="+seed, function(){
        repeatSubContent();
    });
}

function repeatLoadData (target, id, parameter, time){
    clearTimeout(tr);
    repeatTime=time;
    var seed=Math.floor(Math.random()*100000001)
    $("#"+target).load(baseUrl + "loadElement&id="+id+"&parameter="+encodeURIComponent(parameter) +getHeight()+"&nonce="+seed, function(){
        tr = setTimeout('repeatSubContent()',time);
    });
}

function repeatSubContent(){
    clearTimeout(tr);
    var seed=Math.floor(Math.random()*100000001);
    $("#mainArea").load(baseUrl + "loadMain"+getHeight()+"&nonce="+seed, function(){
        tr = setTimeout('repeatSubContent()',repeatTime);
    });
}


function loadElementData (target, id, parameter){
    clearTimeout(tr);
    if (parameter=="stopTimer"){
        stopTimer="";
    }
    loadHtml(target, "loadElement&id="+id+"&parameter="+encodeURIComponent(parameter));
}


function twoStepLoadMenu (selected,target,id,para){
    clearTimeout(tr);
    repeatTarget = target;
    repeatId = id;
    repeatPara = para;
    stopTimer="run";
    var seed=Math.floor(Math.random()*100000001)    
    $("#mainMenu").load(baseUrl + "loadMenu&parameter="+encodeURIComponent(selected)+"&nonce="+seed, function(){
        loadHtml("mainArea", "loadMain");
        repeatParaFrame();
    });
}

function twoStepLoad (primTar,primId,primPara,target,id,para){
    clearTimeout(tr);
    repeatTarget = target;
    repeatId = id;
    repeatPara = para;
    stopTimer="run";
    var seed=Math.floor(Math.random()*100000001)    
    $("#"+primTar).load(baseUrl + "loadElement&id="+primId+"&parameter="+encodeURIComponent(primPara)+
        "&container="+primTar+getHeight()+"&nonce="+seed, function(){
            repeatParaFrame();
        });
}

function repeatParaFrame(){
    clearTimeout(tr);
    var seed=Math.floor(Math.random()*100000001);
    $("#"+repeatTarget).load(baseUrl + "loadElement&id="+repeatId+"&parameter="+encodeURIComponent(repeatPara)+
        "&container="+repeatTarget+getHeight()+"&nonce="+seed, function(){
            if (stopTimer=="run"){
                tr = setTimeout('repeatParaFrame()',repeatTime);            
            }
        });
}

function hideOrShow(selector, hideTarget, id, parameter){
    if ($("#"+selector).attr('checked')){
        $("."+hideTarget).fadeIn(500);
    } else {
        $("."+hideTarget).fadeOut(500);
    }
    loadElementData(id, id, parameter);
}

function executeOption(target, id, parameter){
    var i = document.getElementById(parameter).selectedIndex;
    loadElementData(target, id, i);
}

function executeSelected(target, id, selectGrp, size){
    var i;
    var selectValues="";
    for (i=0;i<size;i++){
        var sel= document.getElementById(selectGrp+i).selectedIndex;
        selectValues+=sel+":";
    }
    loadElementData(target, id, selectValues);
}

function sendInputField (target, id, inputFieldId){
    var data = $("#"+inputFieldId).val();
    loadHtml(target, "loadElement&id="+id+"&parameter="+encodeURIComponent(data));    
}

function foldUnfold(hideElement, showElement,table){
    $("#"+hideElement).hide();
    $("#"+showElement).show();
    $.getJSON(baseUrl+"foldchange&id="+table+"&parameter="+encodeURIComponent(showElement));
}

function ajaxUnfold(ajaxParm, elementId ,table){
    var showElement = elementId;
    var hideElement = elementId+"fold"
    var target = "itAjax"+elementId
    var loadHtmlId = "itAjaxLoad"
    $("#"+hideElement).hide();
    $("#"+showElement).show();
    loadHtml(target, "loadElement&id="+loadHtmlId+"&parameter="+encodeURIComponent(ajaxParm));    
}

function changeIcon (imgID, src){
    $("#"+imgID).attr('src',src);
}


function userLogout(){
    window.location =logoutURL;
}

function devLogout(){
    $.getJSON("testid?action=resetCookie", function (){
        window.location="user.jsp";        
    });    
}

function getHeight(){
    return "&winheight="+$(window).height();
}