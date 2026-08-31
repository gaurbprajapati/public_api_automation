package io.rcrm.api.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.qa.api.util.Owner;

public class UpdateUserTest {

	String baseURI = "https://gorest.co.in";
	String basePath = "/public-api/users";
	String token = "lVJLgwLuUZRHSj50ltkPFLMwj2nYGidvoWwW";
	
	@Owner("Smit Patel")
	@Test
	public void runSession1() {
RestAssured.baseURI = baseURI;
		
		//2. define the http request:
		RequestSpecification httpRequest = RestAssured.given().cookie("authcode", "nu7p1nPA");
		
		//3. make a request/execute the request:
		Response response = httpRequest.request(Method.GET, "/"+basePath);
		
		String verifyText = "Unauthorized";
		
		//4. get the response body:
		String responseBody = response.getBody().asString();
		
		JsonPath jp = new JsonPath(responseBody);
		
		//jp.get("intercom.intercom_metadata.State");
		
//		//validate city name or validate the key or value
		Assert.assertEquals(responseBody.contains(verifyText), true);
	}
}
