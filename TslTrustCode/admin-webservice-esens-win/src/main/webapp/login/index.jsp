<%-- 
    Document   : index
    Created on : Aug 4, 2008, 10:33:51 PM
    Author     : nbuser
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tsl Trust Admin Service Eid 2.0 login page</title>

        <script type="text/javascript" src="jquery.js"></script>
        <script type="text/javascript" src="jquery_cookie.js"></script>
        <script type="text/javascript" charset="UTF-8" src="javascript.js"></script>
        <link rel="stylesheet" type="text/css" href="adminlogin_EU.css">
    </head>

    <body>

        <div class="rightTop">
            <a  class="image" href="http://www.tillvaxtverket.se/"><img src="img/tillvaxtverket.png" width="80" alt="Tillväxtverket"/></a>            
        </div>
        <div id="titleDiv">TSL Trust Administration Service</div>
        <div id="mainMenu">
        </div>

        <div class='menu'>
            <div style="background-color: inherit;float:right">
<!--                <b id="userInfo" style="margin-right: 15px;color:#ffffff">Choose identity provider</b>
                <select id="idpSelect" style="margin-right: 15px"></select>-->
                <input type="button" onclick="idpLogin()" value="Login">
            </div>
            <table class='menu'><tr>
                    <td class='menu'>Policy Manager</td>
                    <td class='selected'>TSL viewer</td>
                    <td class='menu'>TSP Records</td>
            </table>
        </div>

        <div style="width:100%;padding-bottom: 20px">
            <div style="float:right;width:40%">
                <div style="background-color: inherit;float:right">
                    <b class="information" style="margin-right: 50px">Pågående session</b>
<!--                    <input type="checkbox" onclick="showInfo()" id="infoBox" 
                           style="margin-right: 5px"/>Info-->
                </div>
                <div class="information" id="sessionFrame" 
                     style="height: 350px;min-width: 300px;margin-right: 5px; background-color: inherit">
                    <iframe src="https://tsltrust.esp-ttadmin.se/Shibboleth.sso/Session" width="100%" height="340" frameborder="1" 
                            style="border-color: #be7429;border-width: 5; background-color: #e8e8e8"></iframe>
                </div>
            </div>
            <div id="idpArea" style="width:50%">
                <table id="idpTable" class="information"></table>
            </div>
        </div>

        <div id="loginText" style="padding-top: 20"><p><b>Login using Eid 2.0 credentials</b></p>
            <li>Login to access administration service</li>
            <li><a href="https://sig.esp-validation.se/sigval/">Signature validation Service</a></li>
        </div>

        <div id="loginArea">            
            <div id="loginDiv">
                <%--<div id="loginHeaderDiv">Authenticate to service:</div>--%>
                <iframe id="loginIframe" width="100%" height="700" frameborder="0"></iframe>
            </div>            
        </div>
    </body>
</html>
