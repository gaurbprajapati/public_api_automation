package io.rcrm.api.restclient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.TestUtil;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This class is having all http methods which will call the apis and having
 * generic methods for getting the response and fetch the values from response.
 * 
 *
 *
 */
@SuppressWarnings("unchecked")
public class RestClient {

	// HTTP Methods: GET POST PUT DELETE

	/**
	 * This method is used to call GET API
	 * 
	 * @param contentType
	 * @param baseURI
	 * @param basePath
	 * @param token
	 * @param paramsMap
	 * @param log
	 * @return this method is returning response from the GET call
	 */
	public static Response doGet(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest1(contentType, tokenMap, paramsMap, pathParamsMap, log);
			return getResponse("GET", request, basePath);
		}
		return null;
	}

	public static Response doGet1(String contentType, String baseURI, String basePath, Object token,
			Map<String, Boolean> paramsMap, Map<String, String> pathParamsMap, boolean log, Object obj) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest2(contentType, tokenMap, paramsMap, pathParamsMap, log);
			addRequestPayload(request, obj);
			return getResponse("GET", request, basePath);
		}
		return null;
	}

	/**
	 * This method is used to call POST API
	 * 
	 * @param contentType
	 * @param baseURI
	 * @param basePath
	 * @param token
	 * @param paramsMap
	 * @param log
	 * @param obj
	 * @return this method is returning response from the POST call
	 */
	@Step("post call with {0} , {1}, {2}, {3}, {4}")
	public static Response doPost(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, boolean log, Object obj) {
		int maxRetries = 2;
		long initialBackoffMillis = 1000; // Initial backoff duration in milliseconds
		Response lastResponse = null;

		for (int retryCount = 0; retryCount <= maxRetries; retryCount++) {
			Response response = doPostOnce(contentType, baseURI, basePath, token, paramsMap, log, obj);
			lastResponse = response; // Store the response

			if (response.getStatusCode() < 500) {
				return response; // Success, return response
			} else {
				// Retry logic with exponential backoff
				long backoffMillis = initialBackoffMillis * (long) Math.pow(2, retryCount);
				try {
					Thread.sleep(backoffMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		// Return the last response if no successful response was obtained
		return lastResponse;
	}

	@Step("post call with {0} , {1}, {2}, {3}, {4}")
	public static Response doPostWithBooleanQueryParams(String contentType, String baseURI, String basePath,
			Object token,
			Map<String, Boolean> paramsMap, boolean log, Object obj) {
		int maxRetries = 2;
		long initialBackoffMillis = 1000; // Initial backoff duration in milliseconds
		Response lastResponse = null;

		for (int retryCount = 0; retryCount <= maxRetries; retryCount++) {
			Response response = doPostForBooleanQueryParams(contentType, baseURI, basePath, token, paramsMap, log, obj);
			lastResponse = response; // Store the response

			if (response.getStatusCode() < 500) {
				return response; // Success, return response
			} else {
				// Retry logic with exponential backoff
				long backoffMillis = initialBackoffMillis * (long) Math.pow(2, retryCount);
				try {
					Thread.sleep(backoffMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		// Return the last response if no successful response was obtained
		return lastResponse;
	}

	@Step("post call with {0} , {1}, {2}, {3}, {4}")
	public static Response doPostOnce(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, boolean log, Object obj) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest(contentType, tokenMap, paramsMap, log);
			addRequestPayload(request, obj);
			return getResponse("POST", request, basePath);
		}
		return null;
	}

	@Step("post call with {0} , {1}, {2}, {3}, {4}")
	public static Response doPostForBooleanQueryParams(String contentType, String baseURI, String basePath,
			Object token,
			Map<String, Boolean> paramsMap, boolean log, Object obj) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequestWithBooleanQueryParamater(contentType, tokenMap, paramsMap,
					log);
			addRequestPayload(request, obj);
			return getResponse("POST", request, basePath);
		}
		return null;
	}

	public static Response doDelete(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest1(contentType, tokenMap, paramsMap, pathParamsMap, log);
			return getResponse("DELETE", request, basePath);
		}
		return null;
	}

	/**
	 * This method is used to call DELETE API with request payload
	 * 
	 * @param contentType
	 * @param baseURI
	 * @param basePath
	 * @param token
	 * @param paramsMap
	 * @param pathParamsMap
	 * @param log
	 * @param obj           - Request payload object
	 * @return this method is returning response from the DELETE call
	 */
	public static Response doDelete(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log, Object obj) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest1(contentType, tokenMap, paramsMap, pathParamsMap, log);
			addRequestPayload(request, obj);
			return getResponse("DELETE", request, basePath);
		}
		return null;
	}

	public static void addRequestPayload(RequestSpecification request, Object obj) {
		if (obj instanceof Map) {
			request.formParams((Map<String, String>) obj);
		} else if (obj instanceof String && ((String) obj).trim().startsWith("{")) {
			request.body((String) obj);
		} else if (obj instanceof JSONObject) {
			request.body(obj.toString());
		} else if (obj instanceof JSONArray) {
			// For JSONArray, convert to List first for proper serialization
			JSONArray jsonArray = (JSONArray) obj;
			String jsonPayload = TestUtil.getSerializedJSON(jsonArray.toList());
			request.body(jsonPayload);
		} else {
			String jsonPayload = TestUtil.getSerializedJSON(obj);
			request.body(jsonPayload);
		}
	}

	private static boolean setBaseURI(String baseURI) {

		if (baseURI == null || baseURI.isEmpty()) {
			return false;
		}
		try {
			RestAssured.baseURI = baseURI;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static RequestSpecification createRequest(String contentType, Map<String, String> token,
			Map<String, String> paramsMap, boolean log) {
		RequestSpecification request;
		if (log) {
			request = RestAssured.given().log().all();
		} else {
			request = RestAssured.given();
		}

		if (token != null && !token.isEmpty()) {
			if (token.size() == 1)
				request.headers(token);
			else if (token.size() == 2) {
				request.auth().basic(token.get("reaper_username"), token.get("reaper_password"));
			}
		}

		if (!(paramsMap == null)) {
			request.queryParams(paramsMap);
		}

		// request.multiPart("resume", new File(System.getProperty("user.dir") +
		// "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));

		// Add Path Parameter in request body
		/*
		 * Map<String, String> capitalCities = new HashMap<String, String>();
		 * capitalCities.put("candidate","1309619430");
		 * request.pathParams(capitalCities);
		 */
		if (contentType != null) {
			if (contentType.equalsIgnoreCase("JSON")) {
				request.contentType(ContentType.JSON);
				request.accept("application/json");
			} else if (contentType.equalsIgnoreCase("XML")) {
				request.contentType(ContentType.XML);
			} else if (contentType.equalsIgnoreCase("TEXT")) {
				request.contentType(ContentType.TEXT);
			} else if (contentType.equalsIgnoreCase("multipart")) {
				request.multiPart("resume", new File(
						System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));
			}
		}

		return request;
	}

	private static RequestSpecification createRequestWithBooleanQueryParamater(String contentType,
			Map<String, String> token,
			Map<String, Boolean> paramsMap, boolean log) {
		RequestSpecification request;
		if (log) {
			request = RestAssured.given().log().all();
		} else {
			request = RestAssured.given();
		}

		if (token != null && !token.isEmpty()) {
			if (token.size() == 1)
				request.headers(token);
			else if (token.size() == 2) {
				request.auth().basic(token.get("reaper_username"), token.get("reaper_password"));
			}
		}

		if (!(paramsMap == null)) {
			request.queryParams(paramsMap);
		}

		// request.multiPart("resume", new File(System.getProperty("user.dir") +
		// "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));

		// Add Path Parameter in request body
		/*
		 * Map<String, String> capitalCities = new HashMap<String, String>();
		 * capitalCities.put("candidate","1309619430");
		 * request.pathParams(capitalCities);
		 */
		if (contentType != null) {
			if (contentType.equalsIgnoreCase("JSON")) {
				request.contentType(ContentType.JSON);
				request.accept("application/json");
			} else if (contentType.equalsIgnoreCase("XML")) {
				request.contentType(ContentType.XML);
			} else if (contentType.equalsIgnoreCase("TEXT")) {
				request.contentType(ContentType.TEXT);
			} else if (contentType.equalsIgnoreCase("multipart")) {
				request.multiPart("resume", new File(
						System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));
			}
		}

		return request;
	}

	private static Response getResponse(String httpMethod, RequestSpecification request, String basePath) {
		return executeAPI(httpMethod, request, basePath);
	}

	private static Response executeAPI(String httpMethod, RequestSpecification request, String basePath) {
		Response response = null;
		switch (httpMethod) {
			case "GET":
				response = request.get(basePath);
				break;
			case "POST":
				response = request.post(basePath);
				break;
			case "PUT":
				response = request.put(basePath);
				break;
			case "DELETE":
				response = request.delete(basePath);
				break;
			case "PATCH":
				response = request.patch(basePath);
				break;

			default:
				break;
		}

		return response;
	}

	@Step("post call with {0} , {1}, {2}, {3}, {4}")
	public static Response doPost1(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log, Object obj) {
		int maxRetries = 2;
		long initialBackoffMillis = 1000; // Initial backoff duration in milliseconds
		Response lastResponse = null;

		for (int retryCount = 0; retryCount <= maxRetries; retryCount++) {
			Response response = doPostOnce1(contentType, baseURI, basePath, token, paramsMap, pathParamsMap, log, obj);
			lastResponse = response; // Store the response

			if (response.getStatusCode() < 500) {
				return response; // Success, return response
			} else {
				// Retry logic with exponential backoff
				long backoffMillis = initialBackoffMillis * (long) Math.pow(2, retryCount);
				try {
					Thread.sleep(backoffMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		// Return the last response if no successful response was obtained
		return lastResponse;
	}

	@Step("post call with {0} , {1}, {2}, {3}, {4}")
	public static Response doPostOnce1(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log, Object obj) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest1(contentType, tokenMap, paramsMap, pathParamsMap, log);
			addRequestPayload(request, obj);
			return getResponse("POST", request, basePath);
		}
		return null;
	}

	private static RequestSpecification createRequest1(String contentType, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		RequestSpecification request;
		if (log) {
			request = RestAssured.given().log().all();
		} else {
			request = RestAssured.given();
		}

		if (tokenMap != null && !tokenMap.isEmpty()) {
			if (tokenMap.size() == 1)
				request.headers(tokenMap);
			else if (tokenMap.size() == 2) {
				request.auth().basic(tokenMap.get("reaper_username"), tokenMap.get("reaper_password"));
			}
		}

		if (!(paramsMap == null)) {
			request.queryParams(paramsMap);
		}

		// Add Path Parameter in request body
		if (!(pathParamsMap == null)) {
			request.pathParams(pathParamsMap);
		}

		// request.multiPart("resume", new File(System.getProperty("user.dir") +
		// "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));

		if (contentType != null) {
			if (contentType.equalsIgnoreCase("JSON")) {
				request.contentType(ContentType.JSON);
				request.accept("*/*");
				request.accept("application/json");
			} else if (contentType.equalsIgnoreCase("XML")) {
				request.contentType(ContentType.XML);
			} else if (contentType.equalsIgnoreCase("TEXT")) {
				request.contentType(ContentType.TEXT);
			} else if (contentType.equalsIgnoreCase("multipart")) {
				request.multiPart("resume", new File(
						System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));
			}
		}

		return request;
	}

	private static RequestSpecification createRequest2(String contentType, Object token,
			Map<String, Boolean> paramsMap, Map<String, String> pathParamsMap, boolean log) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		RequestSpecification request;
		if (log) {
			request = RestAssured.given().log().all();
		} else {
			request = RestAssured.given();
		}

		if (!tokenMap.isEmpty()) {
			// request.header("Authorization", "Bearer " + token);
			request.headers(tokenMap);
		}

		if (!(paramsMap == null)) {
			request.queryParams(paramsMap);
		}

		// Add Path Parameter in request body
		if (!(pathParamsMap == null)) {
			request.pathParams(pathParamsMap);
		}

		// request.multiPart("resume", new File(System.getProperty("user.dir") +
		// "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));

		if (contentType != null) {
			if (contentType.equalsIgnoreCase("JSON")) {
				request.contentType(ContentType.JSON);
				request.accept("*/*");
				request.accept("application/json");
			} else if (contentType.equalsIgnoreCase("XML")) {
				request.contentType(ContentType.XML);
			} else if (contentType.equalsIgnoreCase("TEXT")) {
				request.contentType(ContentType.TEXT);
			} else if (contentType.equalsIgnoreCase("multipart")) {
				request.multiPart("resume", new File(
						System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg"));
			}
		}

		return request;
	}

	private static Map<String, String> getAuthTokenMap(Object authToken) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		if (authToken instanceof Map) {
			authTokenMap = (Map<String, String>) authToken;
		} else {
			String apiKey = (String) authToken;
			authTokenMap = new HashMap<String, String>();
			authTokenMap.put("Authorization", "Bearer " + apiKey);
		}
		return authTokenMap;
	}

	@Step("PUT call with {0}, {1}, {2}, {3}, {4}, {5}")
	public static Response doPut(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, boolean log, Object body) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest(contentType, tokenMap, paramsMap, log);
			addRequestPayload(request, body);
			return getResponse("PUT", request, basePath);
		}
		return null;
	}

	@Step("PUT call with {0}, {1}, {2}, {3}, {4}, {5}")
	public static Response doPut1(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log, Object body) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest1(contentType, tokenMap, paramsMap, pathParamsMap, log);
			addRequestPayload(request, body);
			return getResponse("PUT", request, basePath);
		}
		return null;
	}

	public static Response doDeleteOnce(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log, Object obj) {
		RequestSpecification request = RestAssured.given();

		if (log) {
			request = request.log().all();
		}

		request.baseUri(baseURI);
		request.basePath(basePath);

		if (contentType != null) {
			request.contentType(contentType);
		}

		if (token != null) {
			request.header("Authorization", "Bearer " + token);
		}

		if (paramsMap != null) {
			request.params(paramsMap);
		}

		if (pathParamsMap != null) {
			request.pathParams(pathParamsMap);
		}

		if (obj != null) {
			request.body(obj);
		}

		return request.delete();
	}

	@Step("PATCH call with {0}, {1}, {2}, {3}, {4}")
	public static Response doPatchOnce(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, boolean log, Object obj) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request = createRequest(contentType, tokenMap, paramsMap, log);
			addRequestPayload(request, obj);
			return getResponse("PATCH", request, basePath);
		}
		return null;
	}

	public static Response doPatch(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log, Object obj) {
		int maxRetries = 2;
		long initialBackoffMillis = 1000; // Initial backoff duration in milliseconds
		Response lastResponse = null;

		for (int retryCount = 0; retryCount <= maxRetries; retryCount++) {
			Response response = doPatchOnce(contentType, baseURI, basePath, token, paramsMap, pathParamsMap, log, obj);
			lastResponse = response; // Store the response

			if (response.getStatusCode() < 500) {
				return response; // Success, return response
			} else {
				// Retry logic with exponential backoff
				long backoffMillis = initialBackoffMillis * (long) Math.pow(2, retryCount);
				try {
					Thread.sleep(backoffMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break; // Exit the loop if interrupted
				}
			}
		}
		return lastResponse; // Return the last response after retries
	}

	private static Response doPatchOnce(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log, Object obj) {
		RequestSpecification request = RestAssured.given();
		if (log) {
			request = request.log().all();
		}
		if (contentType != null) {
			request = request.contentType(contentType);
		}
		if (token != null) {
			request = request.header("Authorization", "Bearer " + token);
		}
		if (paramsMap != null) {
			request = request.queryParams(paramsMap);
		}
		if (pathParamsMap != null) {
			request = request.pathParams(pathParamsMap);
		}
		if (obj != null) {
			request = request.body(obj);
		}

		return request.patch(baseURI + basePath);
	}

	public static Response doPostExtension(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, boolean log, Object obj) {
		Map<String, String> tokenMap = getAuthTokenMapExtension(token);

		if (!setBaseURI(baseURI)) {
			throw new IllegalArgumentException("Invalid base URI: " + baseURI);
		}

		RequestSpecification request = createRequest(contentType, tokenMap, paramsMap, log);
		addRequestPayload(request, obj);
		return getResponse("POST", request, basePath);
	}

	public static Response doGetExtension(String contentType, String baseURI, String basePath, Object token,
			Map<String, String> paramsMap, boolean log) {
		Map<String, String> tokenMap = getAuthTokenMapExtension(token);

		if (!setBaseURI(baseURI)) {
			throw new IllegalArgumentException("Invalid base URI: " + baseURI);
		}

		RequestSpecification request = createRequest(contentType, tokenMap, paramsMap, log);
		return getResponse("GET", request, basePath);
	}

	public static Response doGetExtension1(String contentType, String baseURI, String basePath, Object token, Map<String, String> paramsMap, Map<String, String> pathParamsMap, boolean log) {
		Map<String, String> tokenMap = getAuthTokenMapExtension(token);

		if (!setBaseURI(baseURI)) {
			throw new IllegalArgumentException("Invalid base URI: " + baseURI);
		}

		RequestSpecification request = createRequest1(contentType, tokenMap, paramsMap, pathParamsMap, log);
		return getResponse("GET", request, basePath);
	}

	private static Map<String, String> getAuthTokenMapExtension(Object authToken) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		if (authToken instanceof Map) {
			authTokenMap = (Map<String, String>) authToken;
		} else {
			String apiKey = (String) authToken;
			authTokenMap = new HashMap<String, String>();
			authTokenMap.put("cookie", "_extToken=" + apiKey);
		}
		return authTokenMap;
	}

	@Step("POST multipart call with file upload")
	public static Response doPostMultipart(String baseURI, String basePath, Object token, File file, 
			String fileParamName, String fileMimeType, Map<String, String> formParams, boolean log) {
		Map<String, String> tokenMap = getAuthTokenMap(token);
		if (setBaseURI(baseURI)) {
			RequestSpecification request;
			if (log) {
				request = RestAssured.given().log().all();
			} else {
				request = RestAssured.given();
			}

			if (tokenMap != null && !tokenMap.isEmpty()) {
				request.headers(tokenMap);
			}

			request.multiPart(fileParamName, file, fileMimeType);

			if (formParams != null && !formParams.isEmpty()) {
				for (Map.Entry<String, String> entry : formParams.entrySet()) {
					request.multiPart(entry.getKey(), entry.getValue());
				}
			}

			return request.post(basePath);
		}
		return null;
	}

}
