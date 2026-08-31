package com.qa.api.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mysql.cj.exceptions.CJCommunicationsException;

import io.rcrm.api.pojo.albatross.Login;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class GenerateToken {
	public static void generateTokenFromAccount(Properties prop1,String albatrossUrl) throws IOException {
		Map<String, String> authTokenMap1 = new HashMap<String, String>();
		Login login = new Login();
			login.setEmail(System.getProperty("emailid"));
			login.setPassword(System.getProperty("password"));

		Response response = RestClient.doPost("JSON", albatrossUrl, "login", authTokenMap1, null, true, login);

		JsonPath jp_private_api = response.jsonPath();

		String tkn = jp_private_api.get("data.token");
		String authcode = jp_private_api.get("data.auth_code");
		try {
			prop1.setProperty("tkn", tkn);
			prop1.setProperty("authcode", authcode);
			prop1.store(
					new FileWriter(System.getProperty("user.dir")
							+ "/src/main/java/io/rcrm/api/config/account.properties"),
					"New Email for demo data free account is updated");

		} catch (CJCommunicationsException e) {
			// TODO Auto-generated catch block
		}
	}

}
