<%-- 
    Document   : xmlview
    Created on : Sep 26, 2011, 12:23:12 PM
    Author     : stefan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Certificate Information display Frame</title>
        <link rel="stylesheet" type="text/css" href="asn1js/main.css">
        <script type="text/javascript" charset="UTF-8" src="script/jquery.js"></script>
        <script type="text/javascript" src="asn1js/hex.js"></script>
        <script type="text/javascript" src="asn1js/base64.js"></script>
        <script type="text/javascript" src="asn1js/oids.js"></script>
        <script type="text/javascript" src="asn1js/asn1.js"></script>
        <script type="text/javascript" charset="UTF-8" src="script/certFrame.js"></script>
    </head>
    <body style="padding:0px; width:99%">
        <div style="position: relative; padding-bottom: 1em;">
            <div id="help">
                <div class="license">
                </div>
            </div>
            <div style="position: relative; padding-bottom: 1em;">
                <div id="dump" style="position: absolute; right: 0px;"></div>
                <div id="tree"></div>
            </div>
            <div id="formDiv">
                <form>
                    <textarea id="pem" style="width: 100%;" rows="8">MCcCAQEwHzAHBgUrDgMCGgQUnhEBKvg8fTVa5r0bftJbqOES7LYBAf8=</textarea>
                    <br/>
                    <label title="can be slow with big files"><input type="checkbox" id="wantHex" checked="checked"> with hex dump</label>
                    <input type="button" value="decode" onclick="decodeArea();">
                    <input type="button" value="clear" onclick="clearArea();">
                    <input type="button" value="help" onclick="help();">
                    <br/>
                    <input type="file" id="file"><input type="button" value="load" onclick="load();">
                </form>
            </div>
        </div>

    </body>
</html>
