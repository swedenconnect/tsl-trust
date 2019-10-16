<%-- 
    Document   : index
    Created on : Aug 4, 2011, 10:33:51 PM
    Author     : Tillv�xtverket
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Eid 2.0 login to TSL Trust Signature validation</title>

       <script type="text/javascript" src="https://eid2.3xasecurity.com/disco/jquery-1.8.3.js"></script>
        <script type="text/javascript" src="https://eid2.3xasecurity.com/disco/jquery_eid2Disco.js"></script>
        <script type="text/javascript" src="https://eid2.3xasecurity.com/disco/jQuery_cookie-1.3.js"></script>
        <script type="text/javascript" charset="UTF-8" src="javascript.js"></script>
        <link rel="stylesheet" type="text/css" href="sigstylesheet.css">
        <link rel="stylesheet" type="text/css" href="https://eid2.3xasecurity.com/disco/eid2Disco.css">
    </head>

    <body>
        <div class="top" style="min-height: 90px">
            <div class="rightTop">
                <a  class="image" href="https://sandbox.swedenconnect.se/home/index.html"><img src="img/sweden-connect.svg" height="50" alt="Sweden Connect"/></a>
            </div>
            <div class="centerTop">
                <h1>Signature validation using TSL Trust</h1>
            </div>
            <p><strong class ="colored">Test login through Testb�dden f�r Eid 2.0</strong></p>
        </div>

        <div class="inpbar" style="min-height: 20px">
            <div style="float: right;background-color: inherit;margin-right: 10px">
                <b class="information" style="margin-right: 50px">Active session</b>
                <input type="checkbox" onclick="showInfo()" id="infoBox" 
                       style="margin-right: 5px"/>Info
            </div>
            <div style="background-color: inherit">
                <b id="userInfo">Login to service</b>
            </div>
        </div>
        <div id="discodiv" style="width: 450px"></div>

        <div class="information" id="sessionFrame" 
             style="height: 350px;min-width: 300px;width: 40%;margin-right: 5px; background-color: inherit;float:right">
            <iframe src="https://eid2.3xasecurity.com/Shibboleth.sso/Session" width="100%" height="340" frameborder="1" 
                    style="border-color: #be7429;border-width: 5; background-color: #e8e8e8"></iframe>
        </div>
        <div id="idpArea">
            <table id="idpTable" class="information"></table>
        </div>

        <div style="margin-top: 10px;background-color: #f8f8f8">
            <table id="refTable">
                <tbody>
                    <tr><td colspan="2"><u><Strong class="colored">Related resources and links</Strong></u></td></tr>
                    <tr><td><a href="https://ttadmin.konki.se/login/">Tsl Trust Admin Service</a></td>
                        <td class="prop">Log on to TSL Trust policy administration (Test SP f�r Eid 2.0)</td></tr>
                    <tr><td><a href="https://docs.eid2.se/">docs.eid2.se</a></td>
                        <td class="prop">Information about testb�dden f�r Eid 2.0</td></tr>
                    <tr><td><a href="dslogin.html">Discovery Service Login</a></td>
                        <td class="prop">Use the Discovery Service for selecting identity provider (Requires a logged out user)</td></tr>
                    <tr><td><a href="https://eid2.3xasecurity.com/docs/DeploymentEid2.pdf">DeploymentEid2.pdf</a></td>
                        <td class="prop">Implementation description</td></tr>
                    <tr><td><a href="https://jsonpdisco.3xasecurity.com/">Discovery data</a></td>
                        <td class="prop">External jsonp feed for discovery data och central cookie handling. See further <a 
                                   href="http://aaa-sec.com/pub/eid2/Common_discovery_datafeed.pdf">discovery feed documentation</a>.</td></tr>
                    <tr><td><a href="https://eid2.3xasecurity.com/Shibboleth.sso/Session">SAML Session</a></td>
                        <td class="prop">Status for current SAML session</td></tr>
                    <tr><td><a href="https://eid2.3xasecurity.com/Shibboleth.sso/Metadata">Metadata</a></td>
                        <td class="prop">Metadata for this service in the SAML infrastructure</td></tr>
                </tbody>
            </table>            
        </div>
        <div style="margin-top: 10px;background-color: #ffffff">
            <p>This web application is implemented with "testb�dden f�r Eid 2.0" using the following architecture:</p>
            <a style="margin-left: 30px"class="image" href="https://eid2.3xasecurity.com/docs/DeploymentEid2.pdf"><img src="img/eid2SpService.png" width="600" alt="3xA Security"/></a>                
        </div>

    </body>
</html>
