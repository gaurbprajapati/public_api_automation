package io.recruitcrm.albatross;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.Login;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

public class getTokenTest extends TestBase {

	public getTokenTest() {
		// TODO Auto-generated constructor stub
	}

	Map<String, String> authTokenMap = null;
	// String resume =
	// "https://files-for-testing.s3.ap-southeast-1.amazonaws.com/Anchit+Lalwani+Resume.pdf";

	@BeforeTest
	public void setUp() throws IOException {
		authTokenMap = new HashMap<String, String>();
		// authTokenMap.put("Authorization", "Private " + "9acssa2najshdo1");
	}

	@Owner("Akshaya Uppala")
	@Test
	public void getTokenKey_test() {

		Login login = new Login();
		login.setEmail("dev31122021@yopmail.com");
		login.setPassword("123456");

		Response response = RestClient.doPost("JSON", albatrossURL, "login", authTokenMap, null, true, login);


		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().statusCode(200);

	}

}
