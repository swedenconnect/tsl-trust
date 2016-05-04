var baseUrl = "TThtml?action=";

$(document).ready(function() {
    init();
});


function init(){
    var parameter=getQueryVariable("parameter");
    var seed=Math.floor(Math.random()*100000001)

    $("#infoDataDiv").load(baseUrl +"loadFrameInfo&parameter="+encodeURIComponent(parameter)+"&nonce="+seed);

}

function frameLoad (target, id, parameter){
    loadFrame(target, "frameLoad&id="+id+"&parameter="+encodeURIComponent(parameter));
}

function frameExecOption(target, id, parameter){
    var i = document.getElementById(parameter).selectedIndex;
    frameLoad(target, id, i);
}

function frameExecSelected(target, id, selectGrp, size){
    var i;
    var selectValues="";
    for (i=0;i<size;i++){
        var sel= document.getElementById(selectGrp+i).selectedIndex;
        selectValues+=sel+":";
    }
    frameLoad(target, id, selectValues);
}

function frameSendInput (target, id, inputFieldId){
    var data = $("#"+inputFieldId).val();
    loadFrame(target, "loadElement&id="+id+"&parameter="+encodeURIComponent(data));    
}



function loadFrame (target, action){
    var seed=Math.floor(Math.random()*100000001)
    
    $.ajax({
        type:'GET',
        url: baseUrl + action +"&nonce="+seed,
        dataType:'text',
        success: function(html){
            parent.location=target;
        },
        error: function(xhr, error){
        }
    });
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


// Replicated functions from javascript.js

function loadHtml (container, action){
    var seed=Math.floor(Math.random()*100000001)
    $("#"+container).load(baseUrl + action +"&container="+container+getHeight()+"&nonce="+seed);
}

function loadElementData (target, id, parameter){
    loadHtml(target, "loadElement&id="+id+"&parameter="+encodeURIComponent(parameter));
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

function getHeight(){
    return "&winheight="+$(window).height();
}


