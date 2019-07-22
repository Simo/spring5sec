<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it" class="no-js">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Login</title>
    <meta name="description" content="" />
    <meta name="keywords" content="" />
    <meta name="author" content="Simone Bierti" />
</head>
<body>
<div class="container">
    <form id="formLogin" method="post" action="<c:out value="${idpServerUrl}" />" >
        <input type="hidden" name="COD_DOMINIO" value="<c:out value="${domainId}" />" />
        <input type="hidden" name="COD_APPLICAZIONE" value="<c:out value="${applicationId}" />" />
        <input type="hidden" name="URL_REQUEST" value="<c:out value="${attributesRedirectUrl}" />" />
        <button type="submit">Invia</button>
    </form>
</div>
<script type="text/javascript">
    //document.getElementById("formLogin").submit();
</script>
</body>
</html>