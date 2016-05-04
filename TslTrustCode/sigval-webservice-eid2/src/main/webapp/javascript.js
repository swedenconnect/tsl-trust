/**
 * Javascript for the sample signature validation web application.
 * The provided signature validation web pages (including the login page), this script and
 * the provided servlet implementation is provided for demonstration purposes only.
 * 
 * It is assumed that any serious production deployment of a signature validation service
 * will replace or modify this web application to meet local needs with respect to 
 * user inteface, language and API requirements.
 */
var docr;
var sigr;
var certr;
var signatures;
var validSignatures;

/**
 * Functions performed on page load
 */
$(document).ready(function() {
    getAuthData();  // Remove this call if no user authentication to the signature validatio page is performed
    getPolicyChoices();
    getServerDocList();
    $("#resultArea").hide();
    $("#certArea").hide();
    $("#authArea").hide();
    $("#serverDocs").removeAttr('checked');
    $("#serverFileRow").hide();
    $('#uploadFileForm').ajaxForm(function(xml) { 
        clearSigResult();
        signatureResult(xml); 
    });
    $("#filename-field").change(function(){
        $("#signedFileInput").replaceWith($("#signedFileInput").clone(true));
        $('#signedFileInput').val("");
    });    
});

/**
 * Retieves available validation policy choices from the servlet
 */
function getPolicyChoices(){
    $.ajax({
        type:'GET',
        url:"TTSigValServlet?action=policylist&id=null",
        dataType:'xml',
        success: addPolicyChoices,
        error: function(xhr, error){
            alert("No connection with the server. Try reloading this page");
            $("<option></option>").html('No policcies').appendTo("#policy-field");
        //console.debug(xhr); console.debug(error);
        }
    });
}

/**
 * Adds policy choices to the web page
 */
function addPolicyChoices(xml){
    $("#policy-field").empty();
    $(xml).find("policy").each(function(){
        var policyName = $(this).find("policyName").text();
        var policyInfo = $(this).find("policyInfo").text();
        $("<option></option>").html(policyName).appendTo("#policy-field");
    });
}

/**
 * Retrieves a list of server stored signed documents that may be selectd for test validatoin purposes
 */
function getServerDocList(){
    $.ajax({
        type:'GET',
        url:"TTSigValServlet?action=serverdoclist&id=null",
        dataType:'xml',
        success: addServerDocs,
        error: function(xhr, error){
            $("<option></option>").html('No policcies').appendTo("#filename-field");
        }
    });
}

/**
 * Adds a list of server documents to the web page
 */
function addServerDocs(xml){
    $("#filename-field").empty();
    $("<option></option>").html("--Select--").appendTo("#filename-field");    
    $(xml).find("fileName").each(function(){
        var fileName = $(this).text();
        $("<option></option>").html(fileName).appendTo("#filename-field");
    });
}

/**
 * This perfoms a call to the servlet to verify the signature on one of the server stored
 * documents and to display the result of this signature verification.
 * If a file is specified for upload, the form post functionality is used instead of thsi function.
 * See the form post definition in the page load above for form post details.
 */
function validateSignature(){
    clearSigResult();
    var reqUrl = "TTSigValServlet?action=verify"+
    "&id=" + escape($("#filename-field").val())+
    "&policy="+ escape($("#policy-field").val());
    $.ajax({
        type:'GET',
        url: reqUrl,
        cache: false,
        dataType:'xml',
        success: signatureResult,
        error: function(xhr, error){
            console.debug(xhr);
            console.debug(error);
        }
    });
}

/**
 * Clears previous signature result information from the web page
 */
function clearSigResult(){
    //cleanup
    $("#resultArea").hide();
    $("#resultHead").empty();
    $("#resultTable").empty();
    $("#certArea").hide();
    $("#certHead").empty();
    $("#certTable").empty();    
}

/**
 * Displays signature result information on the web page based on the signature
 * validation result XML data
 */
function signatureResult(xml){
    docr="";
    sigr="";
    certr="";
    signatures =0;
    validSignatures=0;
    var parsed=0;

    //doc context
    $(xml).find("SignedDocumentValidation").each(function(){
        parseDoc(this);
        parsed=1;
    });
    if (parsed==0){
        $(xml).find("tslt\\:SignedDocumentValidation").each(function(){
            parseDoc(this);
        });        
    }
    
    // display result;
    var goodBad;
    var okIcon = "";
    if (validSignatures>0){
        goodBad = "goodBig";
        okIcon = '<img style="margin-right: 15px" src="img/Ok-icon.png" height="32" alt="OK"/>';
    } else {
        goodBad = "errorBig";
        okIcon = '<img style="margin-right: 15px" src="img/Nok-icon.png" height="25" alt="Not OK"/>';
    }
    var headString = "<h2>Signature Validation Result</h2>";
    if (validSignatures==1){
        headString+= '<p class="goodBig">'+okIcon+'1 valid signature (out of '+signatures;
    } else {
        headString+= '<p class="'+goodBad+'">'+okIcon+validSignatures+' valid signatures (out of '+signatures;        
    }
    headString+=")</p>";
    
    $("#resultHead").append(headString);
    $("#resultTable").append(docr);
    $("#resultTable").append(sigr);

    $("#certHead").append("Certificate information");
    
    $("#certTable").append(certr);
    showErrors();
    showDetails();
    $("#resultArea").show(500);
    showCerts();
}

/**
 * This function is part of the XML signature validation report display
 */
function parseDoc(doc){
    //Document properties
    //docr+=docRow(2,"attr",h3("Document"),'<a href="docs/'+$(doc).find("documentName").text()+'">'+$(doc).find("documentName").text()+'</a>');
    docr+=docRow(2,"attr",h3("Document"),$(doc).find("documentName").text());
    docr+=docRow(2,"attr","Document type",$(doc).attr("documentType"));
    var policyName = $(doc).find("policyName").text();
    var policyInfo='<i>No information about the policy "'+policyName +'" is available<i>';
    $(doc).find("policyInformation").each(function(){
        policyInfo = $(doc).find("policyInformation").text();
    });
    
    // Validation policy 
    docr+=docRow(3,"attr","Validation Policy",policyName,policyInfo );
    $(doc).find("signatureValidation").each(function(){
        parseSig(this);
    });
}

/**
 * This function is part of the XML signature validation report display
 */
function parseSig(sig){
    //found signature
    signatures+=1;
    var signName = $(sig).attr("signatureName");
    sigr+=docRow(2,"normal",str("Signature (name)"), str(signName));
    //Signature valid
    var validity = $(sig).find("validationResult").text();
    if (validity=="valid"){
        validSignatures+=1;
        sigr+=docRow(2,"good","Signature validity", validity)
    } else {
        sigr+=docRow(2,"error","Signature validity", validity)        
    }
    //Error messages
    $(sig).find("validationErrorMessages").each(function(){
        $(this).find("message").each(function(){
            var messType = $(this).attr("type");
            if (messType=="error"){
                sigr+=docRowB("errorMess","",messType,$(this).text());                
            } else {
                sigr+=docRowB("warn","",messType,$(this).text());                                
            }
        });
    });
    //EU Qualifications;
    $(sig).find("euQualifications").each(function(){
        sigr+=docRow(2,"attr","EU Qualifications",$(this).text());                
    });
    
    //Signature Algorithms
    $(sig).find("signatureAlgorithms").each(function(){
        var first = "Signature algorithms";
        $(this).find("algorithm").each(function(){
            sigr+=docRow(3,"verbAttr",first,$(this).attr("OID"),$(this).text()); 
            first="";
        });
    });
    

    //Signing time
    $(sig).find("claimedSigningTime").each(function(){
        sigr+=docRow(2,"verbAttr","Claimed signing time", $(this).text().replace("T", "&nbsp;&nbsp;&nbsp;"));
    });
                        
    //time stamp
    $(sig).find("timeStamp").each(function(){
        var tsError=0;
        var tsTime = $(this).find("time").text()
        var errorMsg="";
        $(this).find("statusMessages").each(function(){
            //sigr+=docRow(2,"good","","Found message");                            
            $(this).find("message").each(function(){
                var messType = $(this).attr("type");
                if (messType=="error"){
                    tsError = 256;
                    errorMsg+=docRowB("errorMess","",messType,$(this).text());                
                } else {
                    tsError +=1;
                    errorMsg+=docRowB("warnMess","",messType,$(this).text());                
                }                
            });            
        });
        if (tsError ==0){
            sigr+=docRow(2,"good","Timestamp", tsTime.replace("T", "&nbsp;&nbsp;&nbsp;"));            
        } else {
            if (tsError >255){
                sigr+=docRow(2,"error","Timestamp", tsTime.replace("T", "&nbsp;&nbsp;&nbsp;"));
            } else {
                sigr+=docRow(2,"attr","Timestamp", tsTime.replace("T", "&nbsp;&nbsp;&nbsp;"));                            
            }
        }
        sigr+=errorMsg;
    });
            
    //Subject Name
    $(sig).find("signerDistinguishedName").each(function(){
        sigr+=emptyRow();
        var first = b("Signer identity");
        $(this).find("attributeValue").each(function(){
            sigr+=docRow(3,"prop",first,$(this).attr("type"), $(this).text());
            first="";
        });
    });
    
    //Certificate info
    var parsed=0;
    $(sig).find("signerCertificateInfo").each(function(){
        certr+=docRow(2,"normal"," ", "");                
        certr+=docRow(2,"normal",str("Signature (name)"), str(signName));
        certr+=docRow(1,"normal",b('<I>Signer Certificate</I>'));
        parseCertInfo(this);
        parsed=1;
    }); 
    if (parsed==0){
        certr+=docRow(2,"normal"," ", "");                
        certr+=docRow(2,"normal",str("Signature (name)"), str(signName));
        certr+=docRow(2,"error",b('<I>Signer Certificate</I>'),"Certificate path could not be built to a trusted authority");        
    }
}

/**
 * This function is part of the XML signature validation report display
 */
function parseCertInfo(certInfo){
    //Validity
    $(certInfo).find("certificate:first").each(function(){
        //Certificate status
        $(this).find("certificateStatus").each(function(){
            //Valid status
            var status = $(this).find("validityStatus").text();
            if (status=="valid"){
                certr+=docRow(2,"good","Revocation status",status);                            
            }else {
                certr+=docRow(2,"error","Revocation status",status);            
            }
            var first = "Revocation source";
            $(this).find("validationSource").each(function(){
                certr+=docRow(3,"attr",first,$(this).attr("type"), $(this).text());
                first="";
            });
        });
    
        certr+=docRow(3,"attr","Validity","Not before",$(this).find("notValidBefore").text().replace("T", "&nbsp;&nbsp;&nbsp;"));
        certr+=docRow(3,"attr","","Not After",$(this).find("notValidAfter").text().replace("T", "&nbsp;&nbsp;&nbsp;"));
    
        //Subject Name
        $(this).find("subjectName").each(function(){
            certr+=emptyRow();
            var first = b("Subject name");
            $(this).find("attributeValue").each(function(){
                certr+=docRow(3,"prop",first,$(this).attr("type"), $(this).text());
                first="";
            });
        });
    
        //Issuer Name
        $(this).find("issuerName").each(function(){
            certr+=emptyRow();
            var first = b("Issuer name");
            $(this).find("attributeValue").each(function(){
                certr+=docRow(3,"prop",first,$(this).attr("type"), $(this).text());
                first="";
            });        
        });
        
        //Public Key Algorithm
        $(this).find("publicKeyAlgorithm").each(function(){
            certr+=docRow(3,"attr","Public Key",$(this).attr("keyLength")+" bit", $(this).text());            
        });
    
        //Extensions
        $(this).find("certificateExtensions").each(function(){
            certr+=emptyRow();
            certr+=docRow(1,"normal",'<I><U>Certificate extensions</U></I>');
            parseExtensions(this);
        });
    });
    
    
    //IssuerCertificate info
    $(certInfo).find("issuerCertificateInfo:first").each(function(){
        certr+=docRow(1,"normal", "");
        certr+=docRow(1,"normal",b('<I>Issuer Certificate</I>'));
        parseCertInfo(this);
    });

}


/**
 * This function is part of the XML signature validation report display
 */
function parseExtensions (extensions){
    $(extensions).find("certificateExtension").each(function(){
        certr+=docRow(2,"attr",$(this).attr("name"), "Critical="+$(this).attr("critical"));
        $(this).find("parameter").each(function(){
            certr+=docRow(3,"prop","",$(this).attr("type"), $(this).text());
        });
    });
}

/**
 * Utility. Returns a string with bold tags
 */
function b(txt){
    return "<b class='big'>"+txt+"</b>";
}

/**
 * Utility. Returns a string with strong tags
 */
function str(txt){
    return "<strong class='big'>"+txt+"</strong>";
}

/**
 * Utility. Returns a string with heading 3 tags
 */
function h3(txt){
    return "<strong class='fat'>"+txt+"</h3>";
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
        return tr;
    }
    if (style == "verbAttr"){
        tr='<tr class="verboseRow">';
        return tr;
    }
    return "<tr class=normRow>";    
}

/**
 * returns an empty table row
 */
function emptyRow(){
    return "<tr class=emptyRow><td>&nbsp</td></tr>";
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


/**
 * Switches on or off eror details display
 */
function showErrors(){
    if ($("#showError").attr('checked')){
        $("#resultTable tr.errorRow").show();
    } else {
        $("#resultTable tr.errorRow").hide();
    }
}

/**
 * Switches on or off detailed information display
 */
function showDetails(){
    if ($("#showDetails").attr('checked')){
        $("#resultTable tr.verboseRow").show();
    } else {
        $("#resultTable tr.verboseRow").hide();
    }
}

/**
 * Switches on or off certificate information display
 */
function showCerts(){
    if ($("#showCert").attr('checked')){
        $("#certArea").fadeIn(500);
    } else {
        $("#certArea").fadeOut(500);
    }    
}

/**
 * Shiches on or off the feature to use server stored documents for signature validation tests
 */
function useServerDocs(){
    $("#signedFileInput").replaceWith($("#signedFileInput").clone(true));
    $('#signedFileInput').val("");
    $('#filename-field option')[0].selected = true;

    if ($("#serverDocs").attr('checked')){
        $("#serverFileRow").show();
        $("#uploadFileRow").hide();
    } else {
        $("#uploadFileRow").show();
        $("#serverFileRow").hide();
    }    
}




/**********************************************************************************
 * Below follows optional script functionality to handle user logon to the service
 **********************************************************************************/

/**
 * If the user authenticates to the signature validation web application
 * using SAML based credentials using the login page available at /login/index.jsp
 * then the user identity data is obtained using this call to the servlet.
 */            
function getAuthData(){
    $.ajax({
        type:'GET',
        url:"TTSigValServlet?action=authdata&id=null",
        dataType:'xml',
        success: addAuthData,
        error: function(xhr, error){
        }
    });
}

/**
 * Process and displays user authentication data
 */
function addAuthData(xml){
    var userBar;
    var authInfo;
    var at;
    $("#userInfo").empty();
    $("#authTable").empty();
    $(xml).find("remoteUser").each(function(){
        userBar = "Welcome <strong class='colorSpace'>"+$(this).text()+"</strong>";
        authInfo='<input type="checkbox" id="checkAuthInfo" onclick="showAuthInfo()"/>User authentication details';
        authInfo+='&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
        authInfo+='<input type="button" onclick="logoutShib()" value="Logout">'
        $("#userInfo").append(userBar);
        $("#authInfo").append(authInfo);

        //Authentication Context
        at= docRow(3,"attr",b("Authentication context"),"AuthType",$(this).attr("type"));
        $(xml).find("authContext").each(function(){
            $(this).find("context").each(function(){
                at+=docRow(3,"attr","",$(this).attr("type"), $(this).text());
            });                
        });

        //User Attributes
        $(xml).find("userAttributes").each(function(){
            var first = b("User attributes");
            $(this).find("attribute").each(function(){
                at+=docRow(3,"prop",first,$(this).attr("type"), $(this).text());
                first="";
            });
        });

        $("#authTable").append(at);
        $("#authTableArea").hide();
        $("#authArea").show();
    });
}

/**
 * Provided that the user is authenticated. This call get information from the servlet
 * the determine how the user should be logged out from the service.
 */
function logoutShib(){
    $.ajax({
        type:'GET',
        url:"TTSigValServlet?action=logout&id=null",
        dataType:'xml',
        success: logoutAction,
        error: function(xhr, error){
            window.location="https://eid2.3xasecurity.com/login/";
        }
    });
}

/**
 * Perfom logout functin based on information from the servlet.
 */
function logoutAction(xml){
    $(xml).find("logout").each(function(){
        if ($(this).text()=="shibboleth"){
            window.location ="/Shibboleth.sso/LocLogout?return=https://eid2.3xasecurity.com/login/index.jsp?logout=true";            
        } else {
            window.location="https://eid2.3xasecurity.com/login/";            
        }
    });
}

/**
 * Switches on or off user authentication details display
 */
function showAuthInfo(){
    if ($("#checkAuthInfo").attr('checked')){
        $("#authTableArea").fadeIn(500);
    } else {
        $("#authTableArea").fadeOut(500);
    }    
}

