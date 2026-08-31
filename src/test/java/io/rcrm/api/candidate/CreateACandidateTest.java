package io.rcrm.api.candidate;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.User;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateACandidateTest extends TestBase {

	public CreateACandidateTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";

	@Owner("Sampurn Chouksey")
	@Test
	public void createNewCandidate() {

		Candidate candidate = new Candidate("Sandeep1", "Patil1", "spi504@yopmail.com1", "909090909091");
		Response response1 = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null,
				true, candidate);


		// 4. get the response body:
		String responseBody = response1.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response1.jsonPath();

		slug = jp.get("slug");
		
	}

	@Owner("Gaurav Prajapati")
	@Test
	public void showAllcandidates_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURI, "candidates", ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidate")
	public void editCandidateBySlug_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";

		Candidate candidateObject = new Candidate("Sandeep5551", "Patil1555", "spi504234@yopmail.com1",
				"902342349090909091");

		Response response1 = RestClient.doPost1("JSON", baseURI, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true, candidateObject);

		Assert.assertEquals(response1.getStatusCode(), 200);
		/*
		 * Assert.assertEquals(response.getStatusCode(), 200);
		 */

	}

	@Owner("Raj Pandey")
	@Test
	public void createNewCandidateWithResume_POST() {

		Map<String, String> formsdata = new HashMap<String, String>();
		formsdata.put("first_name", "Sandeep ");
		formsdata.put("email", "rcrmtest0@gmail.com ");

		Response response1 = RestClient.doPost("multipart", baseURI, "candidates", ThreadManager.getAccountApiKey(),
				null, true, formsdata);

		Assert.assertEquals(response1.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response1.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response1.jsonPath();

		String filename = jp.get("resume.filename");

	}

	@DataProvider
	public Object[][] getDataForCandidateFields() {
		Object data[][] = {
				{ "candidate", slug },
				// {"Locality", slug},
		};
		return data;
	}

}