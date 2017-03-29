<%-- 
    Document   : about
    Created on : May 1, 2012, 10:33:51 PM
    Author     : Tillväxtverket
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>About the TSL Trust Signature Validation Service</title>
        <link rel="stylesheet" type="text/css" href="sigstylesheet.css">
    </head>

    <body>
        <div class="top">
            <div class="rightTop">
                <a  class="image" href="http://www.tillvaxtverket.se/"><img src="img/tillvaxtverket.png" width="120" alt="Tillväxtverket"/></a>            
            </div>
            <div class="centerTop">
                <h1>About the sample TSL Trust Signature Validation Service</h1>
            </div>
        </div>

        <p>The TSL Trust Signature validation service is provided by two main components:</p>
        <table>
            <tr>
                <td>-</td>
                <td>A core server component performing signature validation using a 
                    TSL Trust defined policy and producing an XML signature validation
                    report as a result. This functionality is provided by the sigval-validation-1.0.jar 
                    component build by the sigval-validation module.</td>                
            </tr>
            <tr>
                <td>-</td>
                <td>A sample servlet implementation implementing the sigval-validation module component
                    to provide a signature validation service.
                    This servlet demonstrates how a web application can be constructed to upload signed documents
                    for signature validation and how the XML signature validation result report can be
                    displayed to the user.</td>                                
            </tr>                        
        </table>
        <p>The demo implementation servlet and it's API should only be regarded as an example for how signature validation based on
            TSL Trust validation policy administration can be provided to users. Other valid ways to use TSL Trust are;</p>
        <table>
            <tr>
                <td>-</td>
                <td>to integrate the sigval-validation-1.0.jar signature validation functionality directly
                    in a Java application,
                </td>                
            </tr>
            <tr>
                <td>-</td>
                <td>to re-write or to modify the demo servlet to provide a suitable API for a cloud service for signature validation, or;
                </td>
            </tr>
            <tr>
                <td>-</td>
                <td>to abandon the provided sigval-validation-1.0.jar signature validation functionality and instead
                    directly import the TSL Trust trust information exported by the TSL Trust administration service
                    in an existing signature validation solution, such as the DSS solution provided by the EU commission.
                </td>
            </tr>
        </table>
        <p>Implementation options are illustrated by the following overall architecture schematic:</p>
        <img src="img/architecture.png" height="550">
        <br />
        <br />
        <a href="index.jsp">Back to signature validation</a>        
    </body>
</html>
