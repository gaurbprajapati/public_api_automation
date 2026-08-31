package com.qa.api.util;

import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebhookHelper {

	private String webhookToken;
	private Map<String, String> authTokenMap = new HashMap<String, String>();
	private String webhookBaseURL = "https://webhook.site";
	private String targetURL;

	public WebhookHelper() {
		Response response = RestClient.doPost1("JSON", webhookBaseURL, "token", authTokenMap, null, null, false, null);
		JsonPath jp = response.jsonPath();
		webhookToken = jp.get("uuid").toString();
		targetURL = webhookBaseURL + "/" + webhookToken;
	}

	public String getTargetURL() {
		return targetURL;
	}

	public String getData(String searchParameter) {
		//Updated the method as automation required whole data
		return getAllData(searchParameter, 0);
	}

	public String getAllData(String searchParameter, int i) {
		int maxRetries = 3;
		int retryCount = 0;

		while (retryCount < maxRetries) {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Map<String, String> pathParamters = new HashMap<String, String>();
			pathParamters.put("token", webhookToken);
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put("query", searchParameter);

			Response response = RestClient.doGet("JSON", "https://webhook.site", "token/{token}/requests", authTokenMap,
					queryParameters, pathParamters, true);

			String rawBody = response.getBody().asString();

			try {
				JSONObject root = new JSONObject(rawBody);
				JSONArray dataArray = root.getJSONArray("data");
				if (dataArray.length() > i) {
					String payload = dataArray.getJSONObject(i).getString("content");
					return payload;
				}
			} catch (Exception e) {
				// parse error — will retry
			}

			retryCount++;
			if (retryCount < maxRetries) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		throw new RuntimeException("Failed to fetch Webhook data for parameter: " + searchParameter + " after " + maxRetries + " attempts");
	}

	public JsonPath getJsonData() {
		int maxRetries = 3;
		int retryCount = 0;

		while (retryCount < maxRetries) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Map<String, String> pathParamters = new HashMap<String, String>();
			pathParamters.put("token", webhookToken);

			Response response = RestClient.doGet("JSON", "https://webhook.site", "token/{token}/requests", authTokenMap,
					null, pathParamters, true);

			// Use raw response string + JSONObject to avoid RestAssured treating "content"
			// as a named parameter placeholder in GPath expressions
			String rawBody = response.getBody().asString();

			try {
				JSONObject root = new JSONObject(rawBody);
				JSONArray dataArray = root.getJSONArray("data");
				if (dataArray.length() > 0) {
					String payload = dataArray.getJSONObject(0).getString("content");
					return new JsonPath(payload);
				}
			} catch (Exception e) {
				// parse error — will retry
			}

			retryCount++;
			if (retryCount < maxRetries) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		throw new RuntimeException("Failed to fetch Webhook JSON data after " + maxRetries + " attempts");
	}

	public void clearRequests() {
		Map<String, String> pathParams = new HashMap<String, String>();
		pathParams.put("token", webhookToken);
		RestClient.doDelete("JSON", webhookBaseURL, "token/{token}/requests", authTokenMap, null, pathParams, false);
	}

	// Use this method in the end
	public void clear() {
		RestClient.doDelete("JSON", webhookBaseURL, "token/" + webhookToken, authTokenMap, null, null, false);
		webhookToken = "";
		targetURL = "";
	}
}
