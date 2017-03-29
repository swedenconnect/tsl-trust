var baseUrl = "TThtml?action=";

$(document).ready(function() {
    init();
});


function init(){
    var selected = getQueryVariable("selected");
    var seed=Math.floor(Math.random()*100000001)
    $.ajax({
        type:'GET',
        url: baseUrl+"loadxml&parameter="+selected+"&seed="+seed,
        dataType:'xml',
        success: function(xml){
            LoadXMLDom('XMLHolder',xml);
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


//function getAndDispXML(){
//    var selected = getQueryVariable("selected");
//    //    var seed=Math.floor(Math.random()*100000001)
//    $.ajax({
//        type:'GET',
//        url: baseUrl+"loadxml&parameter="+selected,
//        dataType:'xml',
//        success: dispXML,
//        error: function(xhr, error){
//        }
//    });
//}
//
//
//var container = 'XMLHolder';
//var htmlData="";
//
//function dispXML(data){
//    $(data).childNodes.each(function(){
//            htmlData+= p($(this).text());
//            $("#"+container).append(htmlData);                    
//    });    
//}
//
//function p(text){
//    return "<p>"+text+"</p>";
//}

//function dispXML(root){
//    var TagEmptyElement = document.createElement('div');
//    TagEmptyElement.className = 'Element';
//    TagEmptyElement.style.position = 'relative';
//    TagEmptyElement.style.left = NestingIndent+'px';
//    if ($(root).childNodes.length=0){
//        var ClickableElement = AddTextNode(TagEmptyElement,'','Clickable') ;
//        ClickableElement.id = 'div_empty_' + IDCounter;	  
//        newTextNode(TagEmptyElement,'<','Utility') ;
//        newTextNode(TagEmptyElement,RootNode.nodeName ,'NodeName') 
//        for (var i = 0; RootNode.attributes && i < RootNode.attributes.length; ++i) {
//            CurrentAttribute  = RootNode.attributes.item(i);
//            newTextNode(TagEmptyElement,' ' + CurrentAttribute.nodeName ,'AttributeName') ;
//            newTextNode(TagEmptyElement,'=','Utility') ;
//            newTextNode(TagEmptyElement,'"' + CurrentAttribute.nodeValue + '"','AttributeValue') ;
//        }
//        if ($(root).text().length>0){
//            newTextNode(TagEmptyElement,' >','Utility');
//            newTextNode(TagEmptyElement, RootNode.childNodes.item(i).nodeValue,'NodeValue');
//            newTextNode(TagEmptyElement,'</','Utility') ;
//            newTextNode(TagEmptyElement,RootNode.nodeName,'NodeName') ;
//            newTextNode(TagEmptyElement,'>','Utility') ;            
//        } else {
//            newTextNode(TagEmptyElement,' />','Utility') ;
//        }
//        $("#"+container).append(TagEmptyElement);	
//    } else {
//        $(root).childNodes.each(function(){
//           displayXML(this); 
//        });
//    }
//}

//function newTextNode(ParentNode,Text,Class) 
//{
//    NewNode = document.createElement('span');
//    if (Class) {
//        NewNode.className  = Class;
//    }
//    if (Text) {
//        NewNode.appendChild(document.createTextNode(Text));
//    }
//    if (ParentNode) {
//        ParentNode.appendChild(NewNode);
//    }
//    return NewNode;		
//}
//function loadHtml (container, action){
//    var seed=Math.floor(Math.random()*100000001)
//    $("#"+container).load(baseUrl + action +"&nonce="+seed);
//}
//
//function selectMenu(selected){
//    
//    $("#mainMenu").load(baseUrl + "loadMenu&parameter="+selected, function(){
//        loadHtml("mainArea", "loadMain");
//    });
//}
//
//function loadElementData (target, id, parameter){
//    loadHtml(target, "loadElement&id="+id+"&parameter="+parameter);
//}
//
//function hideOrShow(selector, hideTarget, id, parameter){
//    if ($("#"+selector).attr('checked')){
//        $("."+hideTarget).fadeIn(500);
//    } else {
//        $("."+hideTarget).fadeOut(500);
//    }
//    loadElementData(id, id, parameter);
//}
//
//function executeOption(target, id, parameter){
//    var i = document.getElementById(parameter).selectedIndex;
//    loadElementData(target, id, i);
//}

