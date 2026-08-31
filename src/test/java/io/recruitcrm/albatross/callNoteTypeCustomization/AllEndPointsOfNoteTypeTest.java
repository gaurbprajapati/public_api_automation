package io.recruitcrm.albatross.callNoteTypeCustomization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.New_note_typePage;
import io.rcrm.api.pojo.albatross.NoteTypeCustomizationPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndPointsOfNoteTypeTest extends TestBase{
	
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	
	static int id;
	int is_custom;
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getNoteTypes_Test() {
		
		String basePath = "notes/get-note-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNoteType_Test() {
		
		New_note_typePage new_note_typePage = new New_note_typePage();
		new_note_typePage.setLabel("note Type" + generatedString);
		new_note_typePage.setDefault(0);
		ArrayList<Object> noteTypes = new ArrayList<>();
		noteTypes.add(new_note_typePage);
		
		NoteTypeCustomizationPage noteTypeCustomizationPage = new NoteTypeCustomizationPage();
		noteTypeCustomizationPage.setCustomizedNoteTypes(noteTypes);
		
		String basePath = "notes/customize-note-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,noteTypeCustomizationPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		JsonPath jp = response.jsonPath();
		id = jp.get("data.customizeNoteType[0].id");
		is_custom = jp.get("data.customizeNoteType[0].is_custom");

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteNoteType_Test() {
		
		New_note_typePage new_note_typePage = new New_note_typePage();
		new_note_typePage.setLabel("note Type" + generatedString);
		new_note_typePage.setDefault(0);
		new_note_typePage.setId(id);
		new_note_typePage.setIs_custom(is_custom);
		new_note_typePage.setDeleted(true);
		ArrayList<Object> noteTypes = new ArrayList<>();
		noteTypes.add(new_note_typePage);
		
		NoteTypeCustomizationPage noteTypeCustomizationPage = new NoteTypeCustomizationPage();
		noteTypeCustomizationPage.setCustomizedNoteTypes(noteTypes);
		
		String basePath = "notes/customize-note-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,noteTypeCustomizationPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getNoteTypesInvalidAuth_Test() {
		
		String basePath = "notes/get-note-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true);
		response.then().statusCode(401);

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNoteTypeInvalidAuth_Test() {
		
		New_note_typePage new_note_typePage = new New_note_typePage();
		new_note_typePage.setLabel("note Type" + generatedString);
		new_note_typePage.setDefault(0);
		ArrayList<Object> noteTypes = new ArrayList<>();
		noteTypes.add(new_note_typePage);
		
		NoteTypeCustomizationPage noteTypeCustomizationPage = new NoteTypeCustomizationPage();
		noteTypeCustomizationPage.setCustomizedNoteTypes(noteTypes);
		
		String basePath = "notes/customize-note-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,noteTypeCustomizationPage);
		response.then().statusCode(401);

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteNoteTypeInvalid_Test() {
		
		New_note_typePage new_note_typePage = new New_note_typePage();
		new_note_typePage.setLabel("note Type" + generatedString);
		new_note_typePage.setDefault(0);
		new_note_typePage.setId(id);
		new_note_typePage.setIs_custom(is_custom);
		new_note_typePage.setDeleted(true);
		ArrayList<Object> noteTypes = new ArrayList<>();
		noteTypes.add(new_note_typePage);
		
		NoteTypeCustomizationPage noteTypeCustomizationPage = new NoteTypeCustomizationPage();
		noteTypeCustomizationPage.setCustomizedNoteTypes(noteTypes);
		
		String basePath = "notes/customize-note-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,noteTypeCustomizationPage);
		response.then().statusCode(401);

	}

}
