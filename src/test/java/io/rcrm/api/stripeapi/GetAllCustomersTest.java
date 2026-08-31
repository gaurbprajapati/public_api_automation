package io.rcrm.api.stripeapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.net.RequestOptions;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

public class GetAllCustomersTest extends TestBase{

	public GetAllCustomersTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	Map<String, String> authTokenMap = null;

	@BeforeTest
	public void setUp() throws IOException {
		authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Basic " + "c2tfdGVzdF9XQUhUS0ZPTElTR0NWbjRCZ21uQVU1RFY6");

	}

	@Owner("Smit Patel")
	@Test
	public void getAllCustomerByEmailID() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "3");
		queryParameters.put("email", "test2_05082021@yopmail.com");

		Response response = RestClient.doGet("JSON", "https://api.stripe.com/v1/", "customers", authTokenMap, queryParameters, null, true);


		response.then().statusCode(200);
		response.then().body("data[0].email", Matchers.containsString("test2_05082021@yopmail.com"));
	}


	@Owner("Akshaya Uppala")
	@Test
	public void stripeCustomers() {
		RequestOptions requestOptions = RequestOptions.builder()
				.setApiKey("sk_test_WAHTKFOLISGCVn4BgmnAU5DV")
				.build();

		Stripe.apiKey = "sk_test_WAHTKFOLISGCVn4BgmnAU5DV";

		Map<String, Object> params = new HashMap<>();
		params.put("limit", 3);
		params.put("email", "test2_05082021@yopmail.com");

		try {
			CustomerCollection customers =
					Customer.list(params);

		} catch (StripeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


//	OkHttpClient client = new OkHttpClient().newBuilder()
//			  .build();
//			Request request = new Request.Builder()
//			  .url("https://api.stripe.com/v1/customers?limit=3&email=test2_05082021@yopmail.com")
//			  .method("GET", null)
//			  .addHeader("Authorization", "Basic c2tfdGVzdF9XQUhUS0ZPTElTR0NWbjRCZ21uQVU1RFY6")
//			  .addHeader("Content-Type", "application/x-www-form-urlencoded")
//			  .build();
//			Response response = client.newCall(request).execute();

}