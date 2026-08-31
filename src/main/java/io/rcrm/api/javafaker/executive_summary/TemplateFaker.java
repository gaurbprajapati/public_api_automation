package io.rcrm.api.javafaker.executive_summary;

import com.github.javafaker.Faker;

public class TemplateFaker {

	// Locale locale = new Locale("en-IND");
	Faker faker = new Faker();

	public int getTemplateType() {

		return faker.number().numberBetween(1, 3);
	}

	public String getTemplateName() {

		return "Template Name" + faker.superhero().name() + "/" + faker.superhero().power() + "/"
				+ faker.phoneNumber().cellPhone();

	}

	public String getTemplateContent() {

		// return " Executive Summary Report";
		return TemplateConstants.DEFAULT_TEMPLATE;

	}

	public String getCandidateSummaryTemplate() {
		return TemplateConstants.DEFAULT_CANDIDATE_SUMMARY_TEMPLATE;
	}

	public String getExecutiveSearchTitleTemplate() {
		return TemplateConstants.DEFAULT_EXECUTIVE_SEARCH_TITLE_TEMPLATE;
	}
	
	public String getCandidateProfileTemplate() {
		return TemplateConstants.DEFAULT_CANDIDATE_PROFILE;
	}
	

}
