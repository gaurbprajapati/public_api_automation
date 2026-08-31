package io.rcrm.api.hotlists;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Hotlist;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class EditHotlistTest extends TestBase {

	public EditHotlistTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getHotlistIdAndValidData", groups = "nightly-build")
	public void editHotlistByName_HotlistTest(String realtedToType, String hotlistID, int shared, String hotlistName,
			String hotlistMessage, int statusCode) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(shared);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				hotlist);

		if (statusCode == 200) {
			response.then().statusCode(statusCode);
			response.then().body("id", Matchers.notNullValue());
			response.then().body("name", Matchers.containsString(hotlistName));
			response.then().body("shared", Matchers.is(shared));

		} else {
			response.then().body("name[0]", Matchers.containsString(hotlistMessage));
		}
	}

	@DataProvider
	public Object[][] getHotlistIdAndValidData() {

		String generatedString = RandomStringUtils.randomAlphabetic(4);

		JavaFakerHotlist fakerHotlist_d = new JavaFakerHotlist();
		String hotlistName_d = fakerHotlist_d.getHotlistName() + " " + generatedString;
		JavaFakerJob jobFaker_d = new JavaFakerJob();
		String longText_d = "Hotlist - " + jobFaker_d.getJobDescriptionText() + jobFaker_d.getNoteForCandidate();

		JsonPath jsonCandidateHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		String candidateHotlistID = jsonCandidateHotlist.getString("id");

		JsonPath jsonCompanyHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "company").jsonPath();
		String companyHotlistID = jsonCompanyHotlist.getString("id");

		JsonPath jsonContactHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "contact").jsonPath();
		String contactHotlistID = jsonContactHotlist.getString("id");

		JsonPath jsonJobHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "job").jsonPath();
		String jobHotlistID = jsonJobHotlist.getString("id");

		Object data[][] = {

				{ "candidate", candidateHotlistID, 1, hotlistName_d, "", 200 },
				{ "company", companyHotlistID, 1, hotlistName_d, "", 200 },
				{ "contact", contactHotlistID, 1, hotlistName_d, "", 200 },
				{ "job", jobHotlistID, 1, hotlistName_d, "", 200 },

				{ "candidate", candidateHotlistID, 0, hotlistName_d, "", 200 },
				{ "company", companyHotlistID, 0, hotlistName_d, "", 200 },
				{ "contact", contactHotlistID, 0, hotlistName_d, "", 200 },
				{ "job", jobHotlistID, 0, hotlistName_d, "", 200 },

				{ "candidate", candidateHotlistID, 1, longText_d, "The name may not be greater than 191 characters.",
						422 },
				{ "company", companyHotlistID, 1, longText_d, "The name may not be greater than 191 characters.", 422 },
				{ "contact", contactHotlistID, 1, longText_d, "The name may not be greater than 191 characters.", 422 },
				{ "job", jobHotlistID, 1, longText_d, "The name may not be greater than 191 characters.", 422 },

				{ "candidate", candidateHotlistID, 1, "", "The name field is required.", 422 },
				{ "company", companyHotlistID, 1, "", "The name field is required.", 422 },
				{ "contact", contactHotlistID, 1, "", "The name field is required.", 422 },
				{ "job", jobHotlistID, 1, "", "The name field is required.", 422 }

		};

		return data;
	}

}
