var baseUrl = "TThtml?action=";

$(document).ready(function() {
    $("#formDiv").hide();
    init();
});


function init(){
    var seed=Math.floor(Math.random()*100000001)
    var parameter=getQueryVariable("parameter");

    $.getJSON(baseUrl + "loadCertInfo&parameter="+encodeURIComponent(parameter)+"&nonce="+seed, function(json){
        decodeArea(json.pem);
    });
}

//function foldUnfold(hideElement, showElement,table){
//    $("#"+hideElement).hide();
//    $("#"+showElement).show();
//    $.getJSON(baseUrl+"foldchange&id="+table+"&parameter="+showElement);
//}
//
//function changeIcon (imgID, src){
//    $("#"+imgID).attr('src',src);
//}
//
//
//function getQueryVariable(variable) { 
//    var query = window.location.search.substring(1); 
//    var vars = query.split("&"); 
//    for (var i=0;i<vars.length;i++) { 
//        var pair = vars[i].split("="); 
//        if (pair[0] == variable) { 
//            return pair[1]; 
//        } 
//    }
//    return "";
//} 



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


var reHex = /^\s*(?:[0-9A-Fa-f][0-9A-Fa-f]\s*)+$/;
function decode(der) {
    document.getElementById('help').style.display = 'none';
    var tree = document.getElementById('tree');
    var dump = document.getElementById('dump');
    tree.innerHTML = '';
    dump.innerHTML = '';
    try {
        var asn1 = ASN1.decode(der);
        tree.appendChild(asn1.toDOM());
        if (document.getElementById('wantHex').checked)
            dump.appendChild(asn1.toHexDOM());
    } catch (e) {
        tree.innerHTML = e;
    }
    return false;
}
function decodeArea(pem) {
    try {
        //            var pem = document.getElementById('pem').value;
        var der = reHex.test(pem)
        ? Hex.decode(pem)
        : Base64.unarmor(pem);
        decode(der);
    } catch (e) {
        tree.innerHTML = e;
    }
    return false;
}
function decodeBinaryString(str) {
    try {
        var der = [];
        for (var i = 0; i < str.length; ++i)
            der[der.length] = str.charCodeAt(i);
        decode(der);
    } catch (e) {
        tree.innerHTML = e;
    }
    return false;
}
function clearArea() {
    document.getElementById('pem').value = "";
}
function help() {
    document.getElementById('tree').innerHTML = '';
    document.getElementById('dump').innerHTML = '';
    document.getElementById('help').style.display = 'block';
    return false;
}
function load() {
    var file = document.getElementById('file');
    if (!file.files) {
        alert("Your browser doesn't support reading files; try Firefox or Chrome.");
        return false;
    }
    if (file.files.length == 0) {
        alert("Select a file to load first.");
        return false;
    }
    var f = file.files[0];
    if (f.getAsBinary) { // Firefox way
        var derStr = f.getAsBinary();
        decodeBinaryString(derStr);
    } else if (window.FileReader) { // Chrome way
        var r = new FileReader();
        r.onloadend = function() {
            decodeBinaryString(r.result);
        }
        derStr = r.readAsBinaryString(f);
    } else {
        alert("Your browser doesn't support reading files; try Firefox or Chrome.");
        return false;
    }
    return false;
}
