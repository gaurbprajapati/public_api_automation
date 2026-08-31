package io.recruitcrm.report;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetUserForReports_Test extends TestBase {

	public GetUserForReports_Test() {
		// TODO Auto-generated constructor stub
		super();
	}
	JavaFakerSavePerferences fakerReportData = new JavaFakerSavePerferences();

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getUserForReports_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String mode = "job_recruiter";
		queryParameters.put("report", mode);

		String basePath = "global/get-users-for-rpr";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,null,
				true,null);
		response.then().body("data[0].name", Matchers.notNullValue());
		Assert.assertEquals(response.getStatusCode(), 200, "Users For Reports Called Successfully");
		response.then().body("status", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("userForReportsData.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getUserForReportsWithInvalidData_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String mode = fakerReportData.getModes();
		queryParameters.put("report", mode);

		String basePath = "global/get-users-for-rpr";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,null,
				true,null);
		Assert.assertEquals(response.getStatusCode(), 200, "Team Mode Called Successfully");// Response has Data for all Reports for null or invalid value 
		response.then().body("data[0].name", Matchers.notNullValue());
    response.then().body("status", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("userForReportsData.json"));

	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getUserForReportsWithEmptyData_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("report", "");

		String basePath = "global/get-users-for-rpr";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,null,
				true,null);
		Assert.assertEquals(response.getStatusCode(), 200, "Team Mode Called Successfully");// Response has Data for all Reports for null or invalid value 
		response.then().body("data[0].name", Matchers.notNullValue());
		response.then().body("status", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("userForReportsData.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetUserForReports_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String mode = "job_recruiter";
		queryParameters.put("report", mode);

		String basePath = "global/get-users-for-rpr";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters,null,
				true,null);

		Assert.assertEquals(response.getStatusCode(), 401, "Unauthorized Access Validated Successfully");
		response.then().body("error", Matchers.containsString("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));

	}

}
