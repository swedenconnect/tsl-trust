<%--
  Created by IntelliJ IDEA.
  User: stefan
  Date: 2020-06-21
  Time: 10:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">

    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="SUNET Sign Service Demo">
    <meta name="author" content="stefan - Sweden Connect">
    <meta http-equiv='pragma' content='no-cache'>
    <meta http-equiv='cache-control' content='no-cache, no-store, must-revalidate'>
    <meta http-equiv="Expires" content="-1">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <script src="webjars/jquery/3.5.1/jquery.min.js"></script>
    <script src="webjars/popper.js/1.16.0/dist/umd/popper.min.js"></script>
    <script src="webjars/bootstrap/4.5.0/js/bootstrap.min.js"></script>
    <script src="webjars/bootstrap-select/1.13.17/js/bootstrap-select.min.js"></script>
    <script src="webjars/bootstrap-select/1.13.17/js/i18n/defaults-sv_SE.min.js"></script>
    <script src="webjars/bootstrap-fileinput/5.1.0/js/fileinput.min.js"></script>
    <script src="webjars/bootstrap-fileinput/5.1.0/js/locales/sv.js"></script>
    <script src="webjars/jquery-cookie/1.4.1-1/jquery.cookie.js"></script>
    <script src="js/upload.js"></script>

    <link rel="stylesheet" href="css/bootstrap.min.css"/>
    <link rel="stylesheet" href="webjars/bootstrap-select/1.13.17/css/bootstrap-select.min.css"/>
    <link rel="stylesheet" href="webjars/bootstrap-fileinput/5.1.0/css/fileinput.min.css">

    <script>
        maxFileSizeKb=5000;
    </script>

    <title>Title</title>
</head>
<body>
<div class="container">
    <div class="card" style="margin-top: 10px">
        <div class="card-header bg-primary text-white"><h1>Signature validation service</h1></div>
        <div class="card-body">
            <p>Signature validation result</p>

            ${fileName}
            <br>
            <a class="btn btn-primary" href="sigval.jsp">Home</a>



        </div>
    </div>
</div>

</body>
</html>
