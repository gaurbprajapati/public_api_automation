package io.rcrm.api.commanfunctions;

import org.hamcrest.Matchers;

import io.restassured.response.Response;

public class errorResponseBody {

	public errorResponseBody() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	public void verify422ForHotlistEndpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}
	public void verify422ResponseBody(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}
	public void verify401ResponseBody(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("error", Matchers.is(errorMessage));
	}

}
