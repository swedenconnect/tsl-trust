<%-- 
    Document   : user
    Created on : Oct 5, 2011, 1:03:25 PM
    Author     : stefan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link rel="stylesheet" type="text/css" href="script/stylesheet.css">
        <script type="text/javascript" src="script/jquery.js"></script>
        <script type="text/javascript" src="script/jquery_cookie.js"></script>
        <script type="text/javascript" charset="UTF-8">
            var userData;
            $(document).ready(function() {
                loadUsers();
            });
            
            function loadUsers(){
                var seed=Math.floor(Math.random()*100000001)
                $.getJSON("testid?action=userlist", function(json){
                    userData=json;
                    $.each(json, function(i,user){
                        $("<option></option>").html(user.name).appendTo("#userSelect"); 
                    });
                    var idx = $.cookie("selectedUser");
                    if (idx!=null){
                        $("#userSelect option")[idx].selected = true;
                    }
                });
            }
            
            function login(){
                var i = document.getElementById('userSelect').selectedIndex;
                $.cookie("selectedUser",i);
                var url = "testid?action=cookie"
                    +"&name=" + encodeURIComponent(userData[i].name)
                    +"&idp=" + encodeURIComponent(userData[i].idp)
                    +"&attr=" + encodeURIComponent(userData[i].attribute)
                    +"&id=" + encodeURIComponent(userData[i].id);
                $.getJSON(url, function(){
                    window.location="index.jsp";
                });
            }
            
        </script>
    </head>
    <body>
        <div>
            Select user
            <select id="userSelect"></select>
            <input type="button" onclick="login()" value="Login"></input>
        </div>

    </body>
</html>
