package io.rcrm.api.commanfunctions.executiveSummaryService;

import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.javafaker.executive_summary.ExecutiveSearchReportFaker;
import io.rcrm.api.javafaker.executive_summary.TemplateFaker;
import io.rcrm.api.pojo.executiveSummary.ExecutiveSearchReport;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;

public class ExecutiveSearchReportFunctions {
	String templateID = "";
	Map<String, String> authTokenMap = null;

	ExecutiveSearchReportFaker ExecutiveSearchReportFaker = new ExecutiveSearchReportFaker();

	public ExecutiveSearchReportFunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Response createExecutiveSearchReport(int jobId_report, int titleTemplateID, int candidateProfileTemplateID,
			String selectedCollabrators, String selected_candidates_ids, String candidateFields,
			String executiveSummaryServiceURL, Object authTokenExecutiveSummary) {

		String reportTitleName = ExecutiveSearchReportFaker.getExecutiveSearchReportName();

//			try {
		ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
		executiveSearchReport.setJob_id(jobId_report);
		executiveSearchReport.setReport_title(reportTitleName);
		executiveSearchReport.setTitle_template_id(titleTemplateID);
		executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
		executiveSearchReport.setShow_candidates(1);
		executiveSearchReport.setShow_collaborators(1);
		executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
		executiveSearchReport.setSelected_collaborators(selectedCollabrators);

		executiveSearchReport.setCandidate_fields(candidateFields);
		executiveSearchReport.setReport_content_json("\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\"");

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
				authTokenExecutiveSummary, null, true, executiveSearchReport);

		response.then().statusCode(200);

		return response;
	}

	public Response deleteExecutiveSearchByID(String reportID, String ExecutiveSummaryServiceURL,
			Object authTokenMap) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("report_id", reportID);
		String basePath = "/executive-search-reports/{report_id}";

		Response response = RestClient.doDelete("JSON", ExecutiveSummaryServiceURL, basePath, authTokenMap, null,
				pathParamters, false);

		response.then().statusCode(200);

		return response;
	}

}
