package io.rcrm.api.nyma;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.nyma.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EmailSignatureTest extends TestBase {
	commanFunction function = new commanFunction();
	int accountID;
	String albatrossAuthToken;

	@BeforeClass(alwaysRun = true)	public void Setup() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		accountID = ThreadManager.getAccount().getAccountId();
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "emailSignatureType", groups = "nightly-build")
	public void saveEmailSignature(int emailSignatureType) {

		String randomString = RandomStringUtils.randomAlphabetic(4);
		EmailSignature emailSignature = new EmailSignature();
		emailSignature.setKey((emailSignatureType == 1) ? "emailsignature" : "emailsignature_2");
		emailSignature.setTableFlag("User Detail");
		emailSignature.setValue("-- \n" + "Mr. Ajendra Singh.\n" + "QA Automation Engineer.\n"
				+ "RecruitCRM |  www.recruitcrm.net.\n" + "Meerut, 245101\n" + randomString);
		emailSignature.setId(accountID);

		Response response1 = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null,
				true, emailSignature);

		response1.then().statusCode(200);
		response1.then().body("status", Matchers.is("success"));
		response1.then().body("message_type", Matchers.is("is-success"));
		response1.then().body("intercom.intercom_metadata.Message", Matchers.is("Field Updated Successfully"));

	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "emailSignatureType", groups = "nightly-build")
	public void getEmailSignature(int emailSignatureType) {
		String basePath = (emailSignatureType == 1) ? "email-setting/signature/1" : "email-setting/signature/2";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, albatrossAuthToken, null, null, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.is("success"));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@DataProvider(parallel = true)
	public Object[][] emailSignatureType() {
		return new Object[][] { { 1 }, { 2 } };
	}
}
