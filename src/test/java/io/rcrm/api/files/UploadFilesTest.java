package io.rcrm.api.files;

import com.qa.api.util.reaper.ThreadManager;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.Files;
import io.rcrm.api.restclient.RestClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UploadFilesTest extends TestBase {
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	commanFunction function = new commanFunction();
	String resumeUrl = fakerCandidate.getResumeURL();
	String jpegFileUrl = fakerCandidate.getResume();
	String largeFileUrl = fakerCandidate.getLargeFileURL();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String candidateSlug;
	String companySlug;
	String contactSlug;
	String jobSlug;
	String dealSlug;

	@Owner("Harika")
	@Test(dataProvider="getDataForEntity", groups = "nightly-build")
	public void addFileInFileSection(String relatedTo) {
		String slug = getEntitySlug(relatedTo);
		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", slug)
				.multiPart("related_to_type", relatedTo)
				.multiPart("files[]", resumeUrl)
				.post("files");

		response.then().statusCode(200);

		response.then().body("[0].file_name", Matchers.is("sandeep_resume.pdf"));
		response.then().body("[0].related_to", Matchers.is(slug));
		response.then().body("[0].related_to_type", Matchers.is(relatedTo));

	}
	
	
	
	@Owner("Ajendra Singh")
	@Test(dataProvider="getDataForEntity", groups = "nightly-build")
	public void addFileToFolderInFileSection(String relatedTo) {
		String slug = getEntitySlug(relatedTo);
		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", slug)
				.multiPart("related_to_type", relatedTo)
				.multiPart("folder", relatedTo+" "+generatedString )
				.multiPart("files[]", resumeUrl)
				.post("files");

		response.then().statusCode(200);

		response.then().body("[0].file_name", Matchers.is("sandeep_resume.pdf"));
		response.then().body("[0].related_to", Matchers.is(slug));
		response.then().body("[0].related_to_type", Matchers.is(relatedTo));
		response.then().body("[0].folder", Matchers.is(relatedTo+" "+generatedString));

	}
	
	@Owner("Ajendra Singh")
	@Test(dataProvider="getDataForEntity", groups = "nightly-build")
	public void addFileToFolderJsonContent(String relatedTo) {
		String slug = getEntitySlug(relatedTo);
		ArrayList<String> fileToUpload = new ArrayList<>();
		fileToUpload.add(resumeUrl);
		fileToUpload.add(jpegFileUrl);
		
		Files files = new Files();
		files.setRelated_to(slug);
		files.setRelated_to_type(relatedTo);
		files.setFiles(fileToUpload);

		Response response = RestClient.doPost("JSON", baseURL,"files", ThreadManager.getAccountApiKey(),null,true,files);

		response.then().statusCode(200);

		response.then().body("[0].file_name", Matchers.is("sandeep_resume.pdf"));
		response.then().body("[0].related_to", Matchers.is(slug));
		response.then().body("[0].related_to_type", Matchers.is(relatedTo));

	}
	
	@Owner("Ajendra Singh")
	@Test(dataProvider="getDataForEntity", groups = "nightly-build")
	public void addMultipleFiles(String relatedTo) {
		String slug = getEntitySlug(relatedTo);
		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", slug)
				.multiPart("related_to_type", relatedTo)
				.multiPart("folder", relatedTo+"2 "+generatedString )
				.multiPart("files[]", resumeUrl)
				.multiPart("files[]", jpegFileUrl)
				.post("files");

		response.then().statusCode(200);

		response.then().body("[0].file_name", Matchers.is("sandeep_resume.pdf"));
		response.then().body("[0].related_to", Matchers.is(slug));
		response.then().body("[0].folder", Matchers.is(relatedTo+"2 "+generatedString));
		response.then().body("[1].file_name", Matchers.containsString(".jpg"));
		response.then().body("[1].related_to", Matchers.is(slug));
		response.then().body("[1].folder", Matchers.is(relatedTo+"2 "+generatedString));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void verifyLimitToAddMultipleFiles() {
		JsonPath jsonCandidate = function
				.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = jsonCandidate.get("slug");

		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", candidateSlug)
				.multiPart("related_to_type", "candidate")
				.multiPart("files[]", resumeUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.multiPart("files[]", jpegFileUrl)
				.post("files");

		response.then().statusCode(422);

		response.then().body("files[0]", Matchers.is("Total number of files should not exceed 10"));
	}

	
	@Owner("Ajendra Singh")
	@Test(dataProvider="getDataForEntity", groups = "nightly-build")
	public void getFilesInFileSection(String relatedTo) {
		String slug = getEntitySlug(relatedTo);
		
		String basePath = "files/{entity}/{slug}";
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("entity", relatedTo);
		pathParameters.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParameters, true);

		response.then().statusCode(200);
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getFilesWithInvalidSlug() {
		JsonPath jsonCandidate = function
				.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = jsonCandidate.get("slug");
        
		String basePath = "files/{entity}/{slug}";
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("slug", candidateSlug+"123");
		pathParameters.put("entity", "candidate");

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParameters, true);

		response.then().statusCode(404);
		response.then().body("errorMessage", Matchers.is("Invalid slug"));
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getFilesWithInvalidAuth() {
		JsonPath jsonCandidate = function
				.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = jsonCandidate.get("slug");
        
		String basePath = "files/{entity}/{slug}";
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("entity", "candidate");
		pathParameters.put("slug", candidateSlug);

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParameters, true);

		response.then().statusCode(401);
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getFilesWithInvalidEntity() {
		JsonPath jsonCandidate = function
				.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = jsonCandidate.get("slug");
		
		String basePath = "files/{entity}/{slug}";
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("entity", "candidatexyz");
		pathParameters.put("slug", candidateSlug);

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParameters, true);

		response.then().statusCode(404);
		response.then().body("errorMessage", Matchers.is("Invalid Entity"));
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void addFileInFileSectionInvalidAuth() {
		JsonPath jsonCandidate = function
				.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = jsonCandidate.get("slug");

		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey()+"123")
				.multiPart("related_to", candidateSlug)
				.multiPart("related_to_type", "candidate")
				.multiPart("files[]", resumeUrl)
				.post("files");

		response.then().statusCode(401);

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void addFileInFileSectionWithEmptyFields() {

		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", "")
				.multiPart("related_to_type", "")
				.multiPart("files[]", "")
				.post("files");

		response.then().statusCode(422);
		
		response.then().body("related_to_type[0]", Matchers.is("The related to type field is required."));
		response.then().body("related_to[0]", Matchers.is("The related to field is required."));
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void addFileInFileSectionWithInvalidFields() {

		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", generatedString)
				.multiPart("related_to_type", generatedString)
				.multiPart("files[]", resumeUrl)
				.post("files");

		response.then().statusCode(422);
		
		response.then().body("related_to_type[0]", Matchers.is("The selected related to type is invalid."));
		response.then().body("related_to[0]", Matchers.is("related to is not valid."));
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void addFileWithLargeSize() {
		JsonPath jsonCandidate = function
				.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = jsonCandidate.get("slug");

		RestAssured.baseURI = baseURL;

		Response response = RestAssured.given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", candidateSlug)
				.multiPart("related_to_type", "candidate")
				.multiPart("files[]", largeFileUrl)
				.post("files");

		response.then().statusCode(422);

		response.then().body("files[0]", Matchers.is("The File should be a file with max size of 15 MB"));

	}
	
	public String getEntitySlug(String relatedTo) {
		String slug;
		if (relatedTo.equals("candidate")) {
			JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			slug = jsonCandidate.get("slug");
		} else if (relatedTo.equals("company")) {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			slug = jsonCompany.get("slug");
			companySlug = slug;
		} else if (relatedTo.equals("contact")) {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
			JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
			slug = jsonContact.get("slug");
			contactSlug = slug;
		} else if (relatedTo.equals("job")) {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
			String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
			JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
			slug = jsonJob.getString("slug");
			jobSlug = slug;
		} else {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
			String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
			String jobSlug = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath().get("slug");
			JsonPath jsonDeal = function
					.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug, jobSlug)
					.jsonPath();
			slug = jsonDeal.get("slug");
		}
		return slug;
	}

	@DataProvider
	public Object[][] getDataForEntity() {
    Object data[][] = {{ "candidate"},{"company"},{"contact"},{"job"},{"deal"}};
		return data;
	}
		
		
    
}
