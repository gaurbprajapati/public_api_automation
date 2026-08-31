package io.rcrm.api.javafaker.executive_summary;

import com.github.javafaker.Faker;

public class ExecutiveSearchReportFaker {

	// Locale locale = new Locale("en-IND");
	Faker faker = new Faker();

	public String getExecutiveSearchReportName() {
		return faker.job().title() + " Report";
	}

}
