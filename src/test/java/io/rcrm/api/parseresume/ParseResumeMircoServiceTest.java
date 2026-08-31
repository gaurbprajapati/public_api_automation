package io.rcrm.api.parseresume;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.ParseResume;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

public class ParseResumeMircoServiceTest extends TestBase {

	public ParseResumeMircoServiceTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	Map<String, String> authTokenMap = null;
	String resume = "https://files-for-testing.s3.ap-southeast-1.amazonaws.com/Anchit+Lalwani+Resume.pdf";

	@BeforeTest
	public void setUp() throws IOException {
		authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Private " + "9acssa2najshdo1");
	}

	@Owner("Gaurav Prajapati")
	@Test
	public void parseResumeUsingURl() {

//		ParseResume parseResume = new ParseResume();
//		parseResume.setFile_url(resume);
//
		Map<String, String> formsdata = new HashMap<String, String>();
		formsdata.put("file_url", resume);

		String baseURL = "http://13.213.69.7/";
		Response response = RestClient.doPost("multipart", baseURL, "parse", authTokenMap, null, true, formsdata);


		//Response response = RestClient.doPost("JSON", baseURL, "parse", authTokenMap, null, true, parseResume);


		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().statusCode(200);

	}




}