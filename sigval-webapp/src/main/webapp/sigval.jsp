<%@ page import="se.tillvaxtverket.ttsigvalws.resultpage.UIText" %>
<%@ page import="java.util.Locale" %>
<%@ page import="se.tillvaxtverket.ttsigvalws.resultpage.LogoImage" %><%--
  Created by IntelliJ IDEA.
  User: stefan
  Date: 2020-06-21
  Time: 10:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Locale lang = (Locale) request.getAttribute("lang");
    UIText resultText = new UIText(lang);
%>

<html>
<head>
    <meta charset="UTF-8">

    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="Signature Validation Service">
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
    <script src="js/lang.js"></script>

    <link rel="stylesheet" href="css/${bootstrapCss}"/>
    <link rel="stylesheet" href="webjars/bootstrap-select/1.13.17/css/bootstrap-select.min.css"/>
    <link rel="stylesheet" href="webjars/bootstrap-fileinput/5.1.0/css/fileinput.min.css">
    <link rel="stylesheet" href="css/main.css">

    <script>
        maxFileSizeKb=10000;
        lang="<%=lang.getLanguage()%>";
    </script>

    <title>${htmlTitle}</title>
</head>
<body>
<div class="container">
    <div class="card" style="margin-top: 10px">
        <div class="card-header" style="text-align: center">
            <h2>
            <img align="left" src="${logoImage}" alt="Logo" height="50">
            <%
                boolean devmode = (boolean) request.getAttribute("devmode");
                if (devmode){
                  out.print("<span style='margin-right: 115px'>(<span style='color: #FF500D'>Utveckling</span>)</span>");
                }
                LogoImage secondaryLogoImage = (LogoImage) request.getAttribute("secondaryLogoImage");
                if (secondaryLogoImage != null) {
                  out.print("<img align='right' src='" + secondaryLogoImage.getDataUrl() + "' alt='Logo' height='50' >");
                }
            %>
            </h2>
        </div>
        <div class="card-body">
            <div style="float: right">
                <a href="javascript:selectLang('en','<%=lang.getLanguage()%>','result')" class="<%=lang.getLanguage().equals("en")?"lang-selected":""%>">en</a>
                <a href="javascript:selectLang('sv','<%=lang.getLanguage()%>','result')" class="<%=lang.getLanguage().equals("sv")?"lang-selected":""%>">sv</a>
            </div>
            <h4><%=resultText.get("title1")%></h4>
            <!-- Upload widget -->
            <div id="uploadDocDiv" class="form-group" style="width: 100%;margin-top: 10px;">
                <label for="uploadedFileInput"><bolder><%=resultText.get("title2")%></bolder></label>
                <input id="uploadedFileInput" name="uploadedFile" type="file" multiple="" class="form-control file-loading"/>
                <div id="kv-error-2" style="margin-top:10px;display:none"></div>
                <div id="kv-success-2" class="alert alert-success fade in" style="margin-top:10px;display:none"></div>
            </div>

        </div>
    </div>
</div>

</body>
</html>
