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

        <script type="text/javascript" src="https://ds.esp-disco.se/storage/discoweb/jquery-1.8.3.js"></script>
        <script type="text/javascript" src="https://ds.esp-disco.se/storage/discoweb/jquery_espDisco.js"></script>
        <script type="text/javascript" src="https://ds.esp-disco.se/storage/discoweb/jQuery_cookie-1.3.js"></script>
        <script type="text/javascript" charset="UTF-8" src="javascript.js"></script>
        <link rel="stylesheet" type="text/css" href="https://ds.esp-disco.se/storage/discoweb/eid2Disco.css">
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
<!--                <input type="button" onclick="idpLogin()" value="Login">-->
            </div>
            <table class='menu'><tr>
                    <td class='menu'>Policy Manager</td>
                    <td class='selected'>TSL viewer</td>
                    <td class='menu'>TSP Records</td>
            </table>
        </div>

        <div id="loginText" style="padding-top: 20"><p><b>Select identity provider service for login</b></p>
            <div id="eid2Disco"></div>
            <br />
            <li><a href="https://sig.esp-validation.se/sigval/">Signature validation Service</a></li>
        </div>

    </body>
</html>
