package io.rcrm.api.javafaker.albatross.report;

import com.github.javafaker.Faker;

public class JavaFakerSavePerferences {

	// Locale locale = new Locale("en-IND");
		Faker faker = new Faker();

		public String getReportName() {
			String reportName = "Report - " + faker.superhero().name() + "/" + faker.superhero().power() + "/"
					+ faker.phoneNumber().cellPhone();
			return reportName;
		}

		public String getContactNumber() {
			// Generating password
			String phoneNumber = faker.phoneNumber().cellPhone();
			return phoneNumber;
		}

		public String getModes() {
			String mode = "Mode - " + faker.superhero().name();
			return mode;
		}

		public int getReportType() {
			int reportType = faker.number().numberBetween(10, 100);
			return reportType;
		}

}
