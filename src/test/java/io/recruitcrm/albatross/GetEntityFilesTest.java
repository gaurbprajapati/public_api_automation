package io.recruitcrm.albatross;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.GetEntityFiles;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;


@AccountType("CrossAccount")
public class GetEntityFilesTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	private String tokenA;
	private String publicAPIKeyA;
	private int accountIdA;
	private String tokenB;
	private String publicAPIKeyB;
	private int accountIdB;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		tokenA =  getTokenForAccount("AccountA", "valid");
		publicAPIKeyA = getAccountApiKey("AccountA");
		accountIdA = getAccountId("AccountA");

		tokenB =  getTokenForAccount("AccountB", "valid");
		publicAPIKeyB = getAccountApiKey("AccountB");
		accountIdB = getAccountId("AccountB");

	}
	
	@Owner("Harika")
	@Test(dataProvider = "getValidEntity", groups = "nightly-build")
	public void getEntityFiles(String entityType) {
		String folderPath = getFolderPath(entityType,accountIdA,tokenA,publicAPIKeyA);
		GetEntityFiles getEntityFiles = new GetEntityFiles();
		getEntityFiles.setFolder(folderPath);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "global/get-entity-files/get", tokenA, null, true, getEntityFiles);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Harika")
	@Test(dataProvider = "getValidEntity", groups = "nightly-build")
	public void crossAccountGetEntityFiles(String entityType) {
		String folderPath = getFolderPath(entityType,accountIdB,tokenB,publicAPIKeyB);
		GetEntityFiles getEntityFiles = new GetEntityFiles();
		getEntityFiles.setFolder(folderPath);

		Response response = RestClient.doPost("JSON", albatrossURL, "global/get-entity-files/get", tokenA, null, true, getEntityFiles);

		response.then().statusCode(404);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	
	@Owner("Harika")
	@Test(dataProvider = "getEmptyFolders", groups = "nightly-build")
	public void getEntityFilesWithEmptyFolder(String path) {
		GetEntityFiles getEntityFiles = new GetEntityFiles();
		getEntityFiles.setFolder(path);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "global/get-entity-files/get", tokenA, null, true, getEntityFiles);

		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The folder field is required."));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getEntityFilesInvalidToken() {
		GetEntityFiles getEntityFiles = new GetEntityFiles();
		getEntityFiles.setFolder("");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/get-entity-files/get", tokenA + "inValid", null, true, getEntityFiles);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));

	}

	public String getFolderPath(String entityType,int accountId,String privateAuth, String publicAuth){
		String path = "";
		if(entityType.equals("candidate")){
			String candidateEntitySlug = function.createNewCandidateWithMandatoryFields(baseURL, publicAuth).jsonPath()
					.get("slug");
			path = accountId + "/Candidates/" + candidateEntitySlug;
		}else{

			Response getUsers = allCrudFunctions.getUsers(albatrossURL, privateAuth);
			JsonPath jp = getUsers.jsonPath();
			path = jp.get("data.records[0].id")+"/Users/"+jp.get("data.records[0].firstname")+"_"+jp.get("data.records[0].lastname");
		}
		return path;
	}
	
	@DataProvider(parallel = true)
	public Object[][] getValidEntity() {
        return new Object[][]{
            { "candidate" },
            { "user" }
        };
	}
	
	@DataProvider(parallel = true)
	public Object[][] getEmptyFolders() {
        return new Object[][]{
            { "" },
            { null }
        };
	}
}
