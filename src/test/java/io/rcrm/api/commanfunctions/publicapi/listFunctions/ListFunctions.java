package io.rcrm.api.commanfunctions.publicapi.listFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.hamcrest.Matchers;

import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;

public class ListFunctions {

	public ListFunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Response getAllCollabrators(String baseURL, Object authTokenMap) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "2");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "collaborators", authTokenMap, queryParameters, null,
				true);

		response.then().statusCode(200);

		response.then().body("id", Matchers.notNullValue());
		response.then().body("first_name", Matchers.notNullValue());
		response.then().body("email", Matchers.notNullValue());
		return response;
	}

	public Response getAllCandidateHiringStages(String baseURL, Object authTokenMap) {

		Map<String, String> queryParameters = new HashMap<String, String>();

		Response response = RestClient.doGet("JSON", baseURL, "hiring-pipeline", authTokenMap, queryParameters, null,
				true);

		response.then().statusCode(200);

		response.then().body("status_id", Matchers.notNullValue());
		response.then().body("label", Matchers.notNullValue());

		return response;
	}

}
