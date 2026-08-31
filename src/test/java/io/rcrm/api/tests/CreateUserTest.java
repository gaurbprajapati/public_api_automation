package io.rcrm.api.tests;

import java.util.HashMap;

import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.ExcelUtil;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.rcrm.api.pojo.User;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@Epic("create user go rest api feature implementation.....")
@Feature("create user api feature....")
public class CreateUserTest {

	String baseURI = "https://gorest.co.in";
	String basePath = "/public-api/users";
	String token = "lVJLgwLuUZRHSj50ltkPFLMwj2nYGidvoWwW";
	//  _FWTKt73f0EeVrfWj7d3sKoLMnw_9dqVcs0k

	@DataProvider
	public Object[][] getUserData() {
		Object userData[][] = ExcelUtil.getTestData("userdata");
		return userData;
	}

	@Description("create a user api test...verify create user from post call....")
	@Severity(SeverityLevel.BLOCKER)
	@Owner("Smit Patel")
	@Test(dataProvider = "getUserData")
	public void createUserAPIPOSTTest(String firstname, String lastname, String gender, String dob, String email,
			String phonenumber, String website, String address, String status) {

		Map<String, String> authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Bearer " + token);


		User user = new User(firstname, lastname, gender, dob, email, phonenumber, website, address, status);
		Response response = RestClient.doPost("JSON", baseURI, basePath, authTokenMap, null, true, user);
		
		

	}

}
