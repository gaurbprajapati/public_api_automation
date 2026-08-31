package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerMFA {

	Faker faker = new Faker();

	public String getRandomOTP() {
		return faker.number().digits(6);
	}

	public String getSecretKey() {
		return faker.regexify("[A-Z2-7]{16}");
	}

	public String getFullName() {
		return faker.name().fullName();
	}

	public String getSlug() {
		return faker.internet().slug();
	}

	public String getEmailAddress() {
		return faker.name().firstName() + "@yopmail.com";
	}

	public String getJobTitle() {
		return faker.job().title();
	}

	public String getPassword() {
		return faker.internet().password();
	}

	public boolean getRandomBoolean() {
		return faker.random().nextBoolean();
	}

	public int getRandomDigit() {
		return faker.number().randomDigit();
	}
}