<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="buildData" type="jetbrains.buildServer.serverSide.SBuild" scope="request"/>
<c:set var="buildType" value="${buildData.buildType}"/>
<c:url value="/repository/download/${buildType.externalId}/${buildData.buildId}:id/${startPage}?redirectSupported=false" var="reportUrl"/>
<div class="kensa-quick-open" style="margin: 0.75em 0;">
  <a href="${reportUrl}" target="_blank" rel="noopener" class="btn btn_primary">
    Open Kensa Report
    <span aria-hidden="true">&#x2197;</span>
  </a>
</div>
