<%-- 
    Document   : index
    Created on : May 1, 2012, 10:33:51 PM
    Author     : Tillväxtverket
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Signature validation using TSL Trust</title>

        <script type="text/javascript" charset="UTF-8" src="jquery.js"></script>
        <script type="text/javascript" charset="UTF-8" src="jquery.form.js"></script>
        <script type="text/javascript" charset="UTF-8" src="javascript.js"></script>
        <link rel="stylesheet" type="text/css" href="sigstylesheet.css">
    </head>

    <body>
        <div class="top">
            <div class="rightTop">
                <a  class="image" href="http://www.tillvaxtverket.se/"><img src="img/tillvaxtverket.png" width="120" alt="Tillväxtverket"/></a>            
            </div>
            <div class="centerTop">
                <h1>Signature validation using TSL Trust</h1>
            </div>
        </div>

        <div id="authArea">
            <div class="inpbar" style="min-height: 25px">
                <div id="authInfo" style="background-color: inherit; float: right;min-height: inherit"></div>
                <div id="userInfo" style="background-color: inherit; min-height: inherit"></div>                
            </div>
            <div id="authTableArea">
                <div style="float: right"><a href="/login/">Login page</a></div>
                <table id="authTable"></table></div>
        </div>

        <p>This is a test service for verifying signed PDF and XML documents using the TSL Trust signature validation model.
            TSL Trust is developed by the Swedish agency for regional and economic 
            growth (<a href="http://www.tillvaxtverket.se/">Tillväxtverket</a>).
            For more information about this service see (<a href="about.jsp">About the sample TSL Trust Signature Validation Service</a>)</p>

        <div style="float:right;width: 40%">
            <table>
                <TR>
                    <td colspan="2"><b><u>Resources</u></b></td>
                </TR>
                <TR>
                    <td>-</td><TD><a href="http://esp-ttadmin.se/login/">Validation policy details</a></TD>
                </TR>
                <tr><td>-</td><td>TSL Trust validation policy administration service <a href="https://tsltrust.esp-ttadmin.se/admin/">login</td></tr>
                <TR>
                    <td>-</td><TD>Documentation of the&nbsp;
                        <a href="schemadoc/">XML schema</a>
                        &nbsp;for signature validation responses.</TD>
                </TR>
            </table>
        </div>

        <div id="selectArea">
            <form id="uploadFileForm"  enctype="multipart/form-data" action="TTSigValServlet" method="post">
                <table>
                    <tbody>
                        <tr><td><b><u>Select signed document</u></b></td>
                            <td><input type="checkbox" id="serverDocs" onclick="useServerDocs()"/>
                                Use test documents on server</td>
                        </tr>
                        <tr>
                            <td class="b">Select policy: </td>
                            <td><select name="policy" id="policy-field"></select> 
                                &nbsp;&nbsp;&nbsp;</td>
                        </tr>
                        <tr id="serverFileRow">
                            <td class="b">Select signed file on server </td><td><select name="fileName" id="filename-field"></select></td>
                        </tr>
                        <tr id="uploadFileRow">
                            <td class="b">Upload signed document </td><td><input type="file" name="sigfile" id="signedFileInput" size="34"></td>
                        </tr>
                        <tr>
                            <td><input type="submit" onclick="clearSigResult()" value="Verify Signature" /></td>
                        </tr>
                    </tbody>
                </table>
            </form>
            <%--<br/>
            <input type="button" value="Verify Signature"onclick="validateSignature()"/>--%>
        </div>
        <br/>
        <div id="resultArea">            
            <div class="inpbar">Show
                <input class="space" type="checkbox" id="showDetails" onclick="showDetails()"/>Details
                <input class="space" type="checkbox" id="showError" onclick="showErrors()"/>Error messages
                <input class="space" type="checkbox" id="showCert" onclick="showCerts()"/>Certificate information
            </div>
            <div id="resultHead"></div>
            <table id="resultTable"></table>
            <br/>
            <%--
            <input id="showButton" type="button" value="Show certificate info"/>
            <input id="hideButton" type="button" value="Hide certificate info"/>
            --%>
        </div>

        <div id="certArea">                
            <h2 id="certHead"></h2>
            <br/>
            <table id="certTable"></table>
        </div>
    </body>
</html>
