package io.rcrm.api.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.qa.api.util.Owner;

public class CreateACandidateTest {

	public CreateACandidateTest() {
		// TODO Auto-generated constructor stub

	}

	String baseURI = "https://api.recruitcrm.io";
	String basePath = "v1/candidates";
	String token = "HOltPVzoRHYyRuzgGvU5P6CElvG-evBusKkhTqVCllHFMWqEdgvT0Nw8kynyqgI54_mg80ORXlpbWWtZVzsy318xNTkyODk2NzU5";

	@Owner("Sai Teja SG")
	@Test
	public void createCandidatePOST() {
		// 1. define the base url
		// http://restapi.demoqa.com/utilities/weather/city
		RestAssured.baseURI = System.getProperty("apiKey");
		;

		// 2. define the http request:
		RequestSpecification httpRequest = RestAssured.given();

		// 3. make a request/execute the request:
		Response response = httpRequest
				.auth().oauth2(token)
				.request(Method.GET, "/" + basePath);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		JsonPath jp = response.jsonPath();
		jp.get("current_page");

		int currentPage = jp.get("current_page");
		// Assert.assertEquals(responseBody.contains("currentPage"), true);

	}

}
