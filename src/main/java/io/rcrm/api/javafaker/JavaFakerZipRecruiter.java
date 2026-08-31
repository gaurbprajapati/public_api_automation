package io.rcrm.api.javafaker;

import org.apache.commons.lang3.RandomStringUtils;

import com.github.javafaker.Faker;

public class JavaFakerZipRecruiter {

	public JavaFakerZipRecruiter() {
		super();
		// TODO Auto-generated constructor stub
	}

	Faker faker = new Faker();

	public String getResponseId() {
		return RandomStringUtils.randomAlphanumeric(6);
	}

	public String getFirstName() {
		return faker.name().firstName();
	}

	public String getLastName() {
		return faker.name().lastName();
	}

	public String getName() {
		return faker.name().fullName();
	}

	public String getEmail() {
		return faker.name().firstName() + "@yopmail.com";
	}

	public String getPhoneNumber() {
		return faker.phoneNumber().toString();
	}

	public String getExecutiveSummary() {
		return RandomStringUtils.randomAlphabetic(50);
	}

	public String getPosition() {
		return faker.job().position();
	}

	public String getDescription() {
		return faker.job().toString();
	}

	public String getEmployer() {
		return faker.company().name();
	}

	public String getTextResume() {
		return RandomStringUtils.randomAlphabetic(10);
	}

}
