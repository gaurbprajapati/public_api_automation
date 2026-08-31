package io.rcrm.api.files;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.FileInfo;
import io.rcrm.api.pojo.FileRequest;
import io.rcrm.api.pojo.GlobalSearchEntity;
import io.rcrm.api.pojo.SelectedEntity;
import io.rcrm.api.pojo.SelectedEntities;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import com.qa.api.util.Owner;


@AccountType("Business|Email|AlbatrossTkn")
public class AttachFileTest extends TestBase {
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	commanFunction function = new commanFunction();
	String resumeUrl = fakerCandidate.getResumeURL();
	String candidateSlug;
	JsonPath jsonCandidate;
	String candidateName;
	int entityId;
	int ownerId;
	String objectKey;
	String fileName;
	String size;
	String contentType;

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void addFileInFileSection() {
		String slug = getEntitySlug();
		candidateSlug = slug;
		RestAssured.baseURI = baseURL;
		Response response = given()
				.header("Authorization", "Bearer "+ThreadManager.getAccountApiKey())
				.multiPart("related_to", slug)
				.multiPart("related_to_type", "candidate")
				.multiPart("files[]", resumeUrl)
				.post("files");

		response.then().statusCode(200);
		response.then().body("[0].file_name", Matchers.is("sandeep_resume.pdf"));
		response.then().body("[0].related_to", Matchers.is(slug));
		response.then().body("[0].related_to_type", Matchers.is("candidate"));

	}

	@Owner("Ajendra Singh")
	@Test (priority = 1, groups = "nightly-build")
	public void globalSearch(){
		GlobalSearchEntity globalSearchEntity = new GlobalSearchEntity(candidateName, true, true, true, true, true, true);

		Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", ThreadManager.getOwnerAlbatrossToken(), null, true, globalSearchEntity);

		response.then().statusCode(200);
		response.then().body("data['5'][0].slug", Matchers.is(candidateSlug));

		entityId = response.jsonPath().getInt("data['5'][0].id");
	}

	@Owner("Ajendra Singh")
	@Test(priority = 2, groups = "nightly-build")
	public void searchFile() {
		SelectedEntity entity = new SelectedEntity(5, candidateSlug, entityId, ownerId, "Candidates");
		List<SelectedEntity> selectedEntityList = new ArrayList<>();
		selectedEntityList.add(entity);
		SelectedEntities selectedEntities = new SelectedEntities(selectedEntityList, 0, 15, null);

		Response response = RestClient.doPost("JSON", albatrossURL, "files/search", ThreadManager.getOwnerAlbatrossToken(), null, true, selectedEntities);

		response.then().statusCode(200);
		response.then().body("data.files[0].file.name", Matchers.is("sandeep_resume.pdf"));
		response.then().body("data.files[0].file.content_type", Matchers.containsString("application/pdf"));
		response.then().body("data.files[0].user.id", Matchers.is(String.valueOf(ownerId)));
		response.then().body("data.files[0].user.entity_type", Matchers.is(5));
		response.then().body("data.files[0].user.slug", Matchers.is(candidateSlug));

		objectKey = response.jsonPath().get("data.files[0].file.key");
		fileName = response.jsonPath().get("data.files[0].file.name");
		size = response.jsonPath().get("data.files[0].file.size");
		contentType = response.jsonPath().get("data.files[0].file.content_type");
	}

	@Owner("Ajendra Singh")
	@Test(priority = 3, groups = "nightly-build")
	public void saveFile(){
		FileInfo file1 = new FileInfo(objectKey, fileName, null, size, String.valueOf(ownerId), contentType);
		List<FileInfo> fileList = new ArrayList<>();
		fileList.add(file1);
		FileRequest filesRequest = new FileRequest();
		filesRequest.setSelectedFiles(fileList);

		Response response = RestClient.doPost("JSON", albatrossURL,"files/save", ThreadManager.getOwnerAlbatrossToken(),null,true,filesRequest);
		response.then().statusCode(200);
	}


	@Owner("Ajendra Singh")
	@Test (priority = 4, groups = "nightly-build")
	public void getRecentFiles() {
		RestAssured.baseURI = albatrossURL;
		Response response = given()
				.header("Authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken())
				.body("{\"page_size\": 15}")
				.post("files/recent");

		response.then().statusCode(200);
		response.then().body("data.files[0].file.name", Matchers.is("sandeep_resume.pdf"));
		response.then().body("data.files[0].file.content_type", Matchers.containsString("application/pdf"));
		response.then().body("data.files[0].user.id", Matchers.is(ownerId));
		response.then().body("data.files[0].user.entity_type", Matchers.is(5));
		response.then().body("data.files[0].user.slug", Matchers.is(candidateSlug));
	}

	@Owner("Ajendra Singh")
	@Test (priority = 5, groups = "nightly-build")
	public void deleteUnusedFiles() {
		RestAssured.baseURI = albatrossURL;
		Response response = given()
				.header("Authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken())
				.delete("files/unused");

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.is("is-success"));
	}

	public String getEntitySlug() {
		jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		ownerId = jsonCandidate.get("owner");
		return jsonCandidate.get("slug");
	}

}
