package io.rcrm.api.hotlists;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class RemoveRecordsFromHotlistTest extends TestBase {

	public RemoveRecordsFromHotlistTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();

	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	String hotlistName = fakerHotlist.getHotlistName();

	@Owner("Yash Rampal")
	@Test(dataProvider = "getHotlistIdAndEntitySlug", groups = "nightly-build")
	public void removeEntityFromHotlist(String realtedToType, String entitySlug, String hotlistID, int httpStatus) {

		HotlistRelated hotlistRelated = new HotlistRelated();
		hotlistRelated.setRelated(entitySlug);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}/remove-record";

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				hotlistRelated);

		if (httpStatus == 200) {
			int hotlistid1 = Integer.parseInt(hotlistID);
			verfiy200ForAddrecordToHotlist(response, httpStatus, hotlistid1, realtedToType, entitySlug);
		} else if (httpStatus == 404) {

			JsonPath jp = response.jsonPath();
			String errorMessage = jp.get("errorMessage");

			if (errorMessage.contains("Record doesn't exist"))
				verify422ForHotlistEndpoint(response, httpStatus, "Record doesn't exist", true);
			else {
				verify422ForHotlistEndpoint(response, httpStatus, "Hotlist doesn't exist", true);
			}
		}
	}

	public void verify422ForHotlistEndpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	public void verfiy200ForAddrecordToHotlist(Response response, int httpStatus, int HotlistID, String realtedToType,
			String entitySlug) {

		// response.then().statusCode(httpStatus);
		response.then().body("id", Matchers.is(HotlistID));
		response.then().body("name", Matchers.containsString("Hotlist"));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("related", Matchers.nullValue());

	}

	@DataProvider
	public Object[][] getHotlistIdAndEntitySlug() {
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");

		JsonPath jsonCandidateHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		String candidateHotlistID = jsonCandidateHotlist.getString("id");

		JsonPath jsonCompanyHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "company").jsonPath();
		String companyHotlistID = jsonCompanyHotlist.getString("id");

		JsonPath jsonContactHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "contact").jsonPath();
		String contactHotlistID = jsonContactHotlist.getString("id");

		JsonPath jsonJobHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "job").jsonPath();
		String jobHotlistID = jsonJobHotlist.getString("id");

		Object data[][] = {

				{ "candidate", candidateEntitySlug + "1", candidateHotlistID, 404 },
				{ "company", companySlug + "1", companyHotlistID, 404 },
				{ "contact", contactSlug + "1", contactHotlistID, 404 }, 
				{ "job", jobSlug + "1", jobHotlistID, 404 },
				
				{ "candidate", candidateEntitySlug, candidateHotlistID+ "1", 404 },
				{ "company", companySlug, companyHotlistID+ "1", 404 },
				{ "contact", contactSlug, contactHotlistID+ "1", 404 }, 
				{ "job", jobSlug, jobHotlistID+ "1", 404 },
				
				{ "candidate", candidateEntitySlug, candidateHotlistID, 200 },
				{ "company", companySlug, companyHotlistID, 200 }, 
				{ "contact", contactSlug, contactHotlistID, 200 },
				{ "job", jobSlug, jobHotlistID, 200 }

		};

		return data;
	}

}
