<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="buildData" type="jetbrains.buildServer.serverSide.SBuild" scope="request"/>
<c:set var="buildType" value="${buildData.buildType}"/>
<c:url value="/repository/download/${buildType.externalId}/${buildData.buildId}:id/${startPage}?redirectSupported=false" var="reportUrl"/>
<li>
  <a href="${reportUrl}" target="_blank" rel="noopener">Open Kensa report</a>
</li>
