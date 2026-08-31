package io.recruitcrm.albatross.company;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.MappingTemplate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CompanyMappingTemplateTest extends TestBase {

	JavaFakerCompany faker = new JavaFakerCompany();
	String templateContent = "{\"companyname\":\"companies_companyname\"}";

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createCompanyMappingTemplate_Test() {

		String basePath = "import/templates";

		String templateName = faker.getMappingTemplate();
		MappingTemplate mappingTemplate = new MappingTemplate();
		mappingTemplate.setEntity_type("3");
		mappingTemplate.setSharewithteammates(1);
		mappingTemplate.setTemplate_content(templateContent);
		mappingTemplate.setTemplate_name(templateName);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, true, mappingTemplate);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Mapping saved successfully"));
		response.then().body("data.template_content", Matchers.is(templateContent));
		response.then().body("data.template_name", Matchers.is(templateName));
		response.then().body("data.id", Matchers.notNullValue());
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createCompanyMappingTemplateWithEmptyData_Test() {

		String basePath = "import/templates";

		MappingTemplate mappingTemplate = new MappingTemplate();
		mappingTemplate.setEntity_type("3");
		mappingTemplate.setSharewithteammates(1);
		mappingTemplate.setTemplate_content(templateContent);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, true, mappingTemplate);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("message", Matchers.is("The template name field is required."));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("status", Matchers.is("fail"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotCreateCompanyMappingTemplate_Test() {

		String basePath = "import/templates";

		MappingTemplate mappingTemplate = new MappingTemplate();

		Response response = RestClient.doPost("JSON", albatrossURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "x003", null, true, mappingTemplate);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllCompanyMappingTemplates_Test() {

		String basePath = "import/get-templates/3";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.is("Template fetched Succefully"));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllCompanyMappingTemplates_Test() {

		String basePath = "import/get-templates/3";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "x003", null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

}
