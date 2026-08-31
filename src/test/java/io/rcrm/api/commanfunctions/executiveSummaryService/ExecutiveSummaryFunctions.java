package io.rcrm.api.commanfunctions.executiveSummaryService;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;

import io.rcrm.api.javafaker.executive_summary.TemplateFaker;

import io.rcrm.api.pojo.executiveSummary.Template;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class ExecutiveSummaryFunctions {

	String templateID = "";
	Map<String, String> authTokenMap = null;

	TemplateFaker templateFaker = new TemplateFaker();

	public ExecutiveSummaryFunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Response createTemplateForExecutiveSummary(int type, String executiveSummaryServiceURL,
			Object authTokenExecutiveSummary) {

		Template template = new Template();
		template.setType(type);
		template.setTemplate_name(templateFaker.getTemplateName());

		if (type == 1)
			template.setTemplate_content(templateFaker.getExecutiveSearchTitleTemplate());

		if (type == 2)
			template.setTemplate_content(templateFaker.getCandidateProfileTemplate());

		if (type == 3)
			template.setTemplate_content(templateFaker.getCandidateSummaryTemplate());

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates",
				authTokenExecutiveSummary, null, true, template);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		templateID = Integer.toString(ID);

		response.then().statusCode(200);

		return response;
	}

	public Response deleteTemplateByID(String templateID, String ExecutiveSummaryServiceURL,
			Object authTokenMap) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID);
		String basePath = "/templates/{template_id}";

		Response response = RestClient.doDelete("JSON", ExecutiveSummaryServiceURL, basePath, authTokenMap, null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);

		return response;
	}

}
