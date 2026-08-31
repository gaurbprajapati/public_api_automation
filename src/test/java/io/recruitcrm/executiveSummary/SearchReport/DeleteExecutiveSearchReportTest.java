package io.recruitcrm.executiveSummary.SearchReport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSearchReportFunctions;
import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSummaryFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.executive_summary.ExecutiveSearchReportFaker;
import io.rcrm.api.pojo.executiveSummary.Candidate_fields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteExecutiveSearchReportTest extends TestBase {

	String reportID = "";
	int jobID;

	ListFunctions listFunctions = new ListFunctions();
	commanFunction function = new commanFunction();
	ExecutiveSummaryFunctions executiveSummaryFunctions = new ExecutiveSummaryFunctions();

	ExecutiveSearchReportFaker ExecutiveSearchReportFaker = new ExecutiveSearchReportFaker();
	ExecutiveSearchReportFunctions executiveSearchReportFunctions = new ExecutiveSearchReportFunctions();

	@Owner("Sandeep")
	@Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void userShouldAbleToDeleteSearchReport_executiveSummaryTest(String reportIDString, int statusCode) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("report_id", reportIDString);
		String basePath = "/executive-search-reports/{report_id}";

		Response response = RestClient.doDelete("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		response.then().statusCode(200);
	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void userCannotDeleteSearchReportByInvalidID_executiveSummaryTest() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("report_id", "00xxxi001");
		String basePath = "/executive-search-reports/{report_id}";

		Response response = RestClient.doDelete("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		response.then().statusCode(404);
	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotDeleteReportSearch_executiveSummaryTest() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("report_id", reportID);
		String basePath = "/executive-search-reports/{report_id}";

		Response response = RestClient.doDelete("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null,
				pathParamters, false);

		response.then().statusCode(401);
	}

	@DataProvider
	public Object[][] getExecutiveSearchReportData() {

		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), jobSlug);

		// Verify Response using Assertion and Jsonpath
		JsonPath jpJob = getJobResponse.jsonPath();
		int jobID = jpJob.get("data.job.id");

		// Get all templates IDs

		int titleTemplateID = 0;
		int candidateProfileTemplateID = 0;

		for (int i = 1; i < 3; i++) {

			ExecutiveSummaryFunctions executiveSummaryFunctions = new ExecutiveSummaryFunctions();
			Response response = executiveSummaryFunctions.createTemplateForExecutiveSummary(i,
					executiveSummaryServiceURL, ThreadManager.getOwnerAlbatrossToken());

			response.then().statusCode(200);

			// Verify Response using Assertion and Jsonpath
			JsonPath jsonPathTemplate = response.jsonPath();

			if (i == 1)
				titleTemplateID = jsonPathTemplate.get("id");

			if (i == 2)
				candidateProfileTemplateID = jsonPathTemplate.get("id");
		}

		// Get collabrators

		JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		ArrayList<Integer> recruiterIds = jsonGetCollabrators.get("id");

		String selectedCollabrators = recruiterIds.toString();

		// Assign Candidates to job and get candidate ID for report

		String selected_candidates_ids = "";
		StringBuilder strcat = new StringBuilder();

		for (int i = 0; i < 3; i++) {
			JsonPath jsonAssigneCandidate;

			jsonAssigneCandidate = function.assignCandidateByJobSlug(baseURL, ThreadManager.getAccountApiKey(), jobSlug)
					.jsonPath();
			String candidateSlug = jsonAssigneCandidate.get("candidate_slug");

			Response getCandResponse = albatrossFunctions.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(),
					candidateSlug);

			// Verify Response using Assertion and Jsonpath
			JsonPath jpCand = getCandResponse.jsonPath();

			int candidateID = jpCand.get("data.candidate.id");

			String candidatesIds = String.valueOf(candidateID);

			strcat.append(selected_candidates_ids);
			strcat.append(candidatesIds);
			selected_candidates_ids = candidatesIds;

		}

		// get list of candidates column names with max 5 columns

		ArrayList<Object> candidate_fields = new ArrayList<Object>();

		Candidate_fields candidate_fields1 = new Candidate_fields();
		candidate_fields1.setColumn_name("first_name");

		Candidate_fields candidate_fields2 = new Candidate_fields();
		candidate_fields2.setColumn_name("last_name");

		candidate_fields.add(candidate_fields1);
		candidate_fields.add(candidate_fields2);

		Gson gson = new Gson();
		String candidateFieldsObjectInString = gson.toJson(candidate_fields);

		// create report content json

		// Create Executive Report and get Report ID

		Response getResponseOfExecutiveSearchReport = executiveSearchReportFunctions.createExecutiveSearchReport(jobID,
				titleTemplateID, candidateProfileTemplateID, selectedCollabrators, selected_candidates_ids,
				candidateFieldsObjectInString, executiveSummaryServiceURL, ThreadManager.getOwnerAlbatrossToken());

		// Verify Response using Assertion and Jsonpath
		JsonPath reportIDJson = getResponseOfExecutiveSearchReport.jsonPath();
		int reportIDInt = reportIDJson.get("id");
		reportID = Integer.toString(reportIDInt);

		Object data[][] = { { reportID, 200 } };
		return data;
	}

}
