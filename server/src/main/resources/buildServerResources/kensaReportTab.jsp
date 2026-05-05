<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<jsp:useBean id="buildData" type="jetbrains.buildServer.serverSide.SBuild" scope="request"/>
<c:set var="buildType" value="${buildData.buildType}"/>
<c:url value="/repository/download/${buildType.externalId}/${buildData.buildId}:id/${startPage}?redirectSupported=false" var="reportUrl"/>
<c:choose>
  <c:when test="${empty isDisabled}">
    <div style="margin: 0 0 0.75em;">
      <a href="${reportUrl}" target="_blank" rel="noopener" class="btn">Open in new tab</a>
    </div>
    <bs:iframe url="${reportUrl}"/>
  </c:when>
  <c:otherwise>
    <div class="attentionComment" style="padding: 1em;">
      <p>
        <bs:buildStatusIcon type="red-sign" className="warningIcon"/>
        Inline rendering is disabled because TeamCity's domain isolation protection for artifacts is enabled but the artifacts' URL has not been configured.
      </p>
      <c:choose>
        <c:when test="${afn:permissionGrantedGlobally('CHANGE_SERVER_SETTINGS')}">
          <p>
            To enable inline rendering, configure the artifacts URL in
            <a href="<c:url value="/admin/admin.html?item=serverConfigGeneral"/>">Global Settings</a>,
            or disable the domain isolation (not recommended).
          </p>
        </c:when>
        <c:otherwise>
          <p>Ask your administrator to configure the artifacts URL in Global Settings.</p>
        </c:otherwise>
      </c:choose>
      <p style="margin-top: 1em;">
        <a href="${reportUrl}" target="_blank" rel="noopener" class="btn btn_primary">Download Kensa report</a>
      </p>
    </div>
  </c:otherwise>
</c:choose>
