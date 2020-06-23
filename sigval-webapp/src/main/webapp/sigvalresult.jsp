<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ page import="se.tillvaxtverket.ttsigvalws.resultpage.*" %><%--
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
    <link rel="stylesheet" href="webjars/font-awesome/5.13.0/css/all.min.css">
    <link rel="stylesheet" href="webjars/bootstrap-select/1.13.17/css/bootstrap-select.min.css"/>
    <link rel="stylesheet" href="webjars/bootstrap-fileinput/5.1.0/css/fileinput.min.css">
    <link rel="stylesheet" href="css/result.css">

    <script>
        maxFileSizeKb = 5000;
    </script>

    <title>Title</title>
</head>
<body>
<div class="container">
    <div class="card" style="margin-top: 10px">
        <div class="card-header"></div>
        <div class="card-body">
            <%
                UIText resultText = new UIText((Locale) request.getAttribute("lang"));
                ResultPageData data = (ResultPageData) request.getAttribute("result");
                String okIcn = "<i class='fas fa-check-circle icon-ok'></i>";
                String nokIcn = "<i class='fas fa-times-circle icon-error'></i>";
                String warnIcn = "<i class='fas fa-exclamation-triangle icon-warning'></i>";
            %>
            <h3><%=resultText.get("title1")%>
            </h3>
            <%=resultText.get("document")%>:&nbsp;<b>${fileName}</b>

            <table class="table table-sm table-borderless" style="margin-top: 20px">
                <tr>
                    <td class="overall-param"><%=resultText.get("status")%>
                    </td>
                    <td>
                        <%
                            switch (data.getStatus()) {
                            case ok:
                                out.print(okIcn + " " + resultText.get("docOk"));
                                break;
                            case unsigned:
                            case invalid:
                                out.print(nokIcn + " " + resultText.get(data.getStatus().name()));
                                break;
                            case someinvalid:
                                out.print(warnIcn + " " + resultText.get("someinvalid"));
                                break;
                            }
                        %>
                    </td>
                </tr>
                <tr>
                    <td class="overall-param"><%=resultText.get("doctype")%>
                    </td>
                    <td><%=data.getDocumentType()%>
                    </td>
                </tr>
<%--
                <tr>
                    <td class="doc-valid-param"><%=resultText.get("sigcount")%>
                    </td>
                    <td class="doc-valid-param"><%=data.getNumberOfSignatures()%>
                    </td>
                </tr>
                <tr>
                    <td><%=resultText.get("validcount")%>
                    </td>
                    <td><%=data.getValidSignatures()%>
                    </td>
                </tr>
--%>
            </table>
            <%
                List<ResultSignatureData> resultSignatureDataList = data.getResultSignatureDataList();
                for (int i = 0; i < resultSignatureDataList.size(); i++) {
                    ResultSignatureData sigData = resultSignatureDataList.get(i);
            %>
            <div class="card">
                <div class="card-header bg-secondary text-white"><h5><%out.print(resultText.get("signature") + " " + (i + 1));%></h5></div>
            </div>
            <div class="card-body">
                <table class="table table-sm">
                    <tr>
                        <td class="sig-res-param"><%=resultText.get("status")%>
                        </td>
                        <td>
                            <%
                                switch (sigData.getStatus()) {
                                case ok:
                                    out.print(okIcn + " " + resultText.get("sigOK"));
                                    break;
                                case sigerror:
                                case invalidCert:
                                case incomplete:
                                    out.print(nokIcn + " " + resultText.get(sigData.getStatus().name()));
                                    break;
                                }
                            %>
                        </td>
                    </tr>
                    <tr>
                        <td class="sig-res-param"><%=resultText.get("coversDoc")%>
                        </td>
                        <td><%
                            out.print(sigData.isCoversAllData()
                                    ? resultText.get("coverAll")
                                    : resultText.get("coverSome"));
                        %></td>
                    </tr>
                    <%
                        if (sigData.getIdp() != null) {
                    %>
                    <tr>
                        <td class="sig-res-param"><%=resultText.get("signtime")%>
                        </td>
                        <td><%=sigData.getSigningTime()%>
                        </td>
                    </tr>
                    <tr>
                        <td class="sig-res-param"><%=resultText.get("idp")%>
                        </td>
                        <td><%=sigData.getIdp()%>
                        </td>
                    </tr>
                    <tr>
                        <td class="sig-res-param"><%=resultText.get("sp")%>
                        </td>
                        <td><%=sigData.getServiceProvider()%>
                        </td>
                    </tr>
                    <%
                        if (request.getAttribute("showLoa") != null) {
                    %>
                    <tr>
                        <td class="sig-res-param"><%=resultText.get("loa")%>
                        </td>
                        <td><%=sigData.getLoa()%>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    <%
                        }
                    %>
                </table>
                <h6 class="text-dark"><b><%=resultText.get("signer")%></b></h6>
                <table class="table table-sm table-striped">
                    <%
                        List<DisplayAttribute> signerAttributes = sigData.getSignerAttribute();
                        for (int j = 0; j < signerAttributes.size(); j++) {
                            DisplayAttribute attr = signerAttributes.get(j);
                    %>
                    <tr>
                        <td class="attr-td"><%=attr.getName()%>
                        </td>
                        <td><%=attr.getValue()%>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </table>
            </div>
            <%
                }
            %>


            <br>
            <a class="btn btn-secondary" href="sigval.jsp">Home</a>


        </div>
    </div>
</div>

</body>
</html>
