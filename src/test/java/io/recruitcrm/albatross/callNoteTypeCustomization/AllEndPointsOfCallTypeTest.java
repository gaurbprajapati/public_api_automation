package io.recruitcrm.albatross.callNoteTypeCustomization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.CallTypeCustomizationPage;
import io.rcrm.api.pojo.albatross.New_call_TypePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndPointsOfCallTypeTest extends TestBase {
	
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	
	static int id;
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getCallTypes_Test() {
		
		String basePath = "call-logs/get-call-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createCallType_Test() {
		
		New_call_TypePage new_call_TypePage = new New_call_TypePage();
		new_call_TypePage.setLabel("call Type" + generatedString);
		new_call_TypePage.setDefaultvalue(0);
		ArrayList<Object> CallTypes = new ArrayList<>();
		CallTypes.add(new_call_TypePage);
		
		CallTypeCustomizationPage callTypeCustomizationPage = new CallTypeCustomizationPage();
		callTypeCustomizationPage.setCustomizedCallTypes(CallTypes);
		
		String basePath = "call-logs/customize-call-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,callTypeCustomizationPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		JsonPath jp = response.jsonPath();
		id = jp.get("data.customizeCallType[0].id");

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteCallType_Test() {
		
		New_call_TypePage new_call_TypePage = new New_call_TypePage();
		new_call_TypePage.setLabel("call Type" + generatedString);
		new_call_TypePage.setDefaultvalue(0);
		new_call_TypePage.setDeleted(true);
		new_call_TypePage.setId(id);
		ArrayList<Object> CallTypes = new ArrayList<>();
		CallTypes.add(new_call_TypePage);
		
		CallTypeCustomizationPage callTypeCustomizationPage = new CallTypeCustomizationPage();
		callTypeCustomizationPage.setCustomizedCallTypes(CallTypes);
		
		String basePath = "call-logs/customize-call-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,callTypeCustomizationPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getCallTypesInvalidAuth_Test() {
		
		String basePath = "call-logs/get-call-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true);
		response.then().statusCode(401);

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createCallTypeInvalidAuth_Test() {
		
		New_call_TypePage new_call_TypePage = new New_call_TypePage();
		new_call_TypePage.setLabel("call Type" + generatedString);
		new_call_TypePage.setDefaultvalue(0);
		ArrayList<Object> CallTypes = new ArrayList<>();
		CallTypes.add(new_call_TypePage);
		
		CallTypeCustomizationPage callTypeCustomizationPage = new CallTypeCustomizationPage();
		callTypeCustomizationPage.setCustomizedCallTypes(CallTypes);
		
		String basePath = "call-logs/customize-call-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,callTypeCustomizationPage);
		response.then().statusCode(401);

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteCallTypeInvalidAuth_Test() {
		
		New_call_TypePage new_call_TypePage = new New_call_TypePage();
		new_call_TypePage.setLabel("call Type" + generatedString);
		new_call_TypePage.setDefaultvalue(0);
		new_call_TypePage.setDeleted(true);
		new_call_TypePage.setId(id);
		ArrayList<Object> CallTypes = new ArrayList<>();
		CallTypes.add(new_call_TypePage);
		
		CallTypeCustomizationPage callTypeCustomizationPage = new CallTypeCustomizationPage();
		callTypeCustomizationPage.setCustomizedCallTypes(CallTypes);
		
		String basePath = "call-logs/customize-call-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,callTypeCustomizationPage);
		response.then().statusCode(401);

	}

}
