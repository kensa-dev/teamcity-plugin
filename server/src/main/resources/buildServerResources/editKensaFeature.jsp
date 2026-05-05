<%@ include file="/include.jsp" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<tr>
  <th><label for="kensa.output.path">Kensa output path:</label></th>
  <td>
    <props:textProperty name="kensa.output.path" style="width: 25em;"/>
    <span class="smallNote">Override Kensa output dir (relative to checkout). Leave blank to auto-detect.</span>
  </td>
</tr>
<tr>
  <th>Features:</th>
  <td>
    <props:checkboxProperty name="kensa.feature.reportTab"/> <label for="kensa.feature.reportTab">Show Kensa Report tab</label><br/>
    <props:checkboxProperty name="kensa.feature.testReporter"/> <label for="kensa.feature.testReporter">Report Kensa tests as TeamCity tests</label><br/>
    <props:checkboxProperty name="kensa.feature.failureSummary"/> <label for="kensa.feature.failureSummary">Attach Given-When-Then narrative to failures</label>
  </td>
</tr>
