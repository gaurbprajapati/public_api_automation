package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerSSO {
	Faker faker = new Faker();

	public String getClientId() {
		return faker.number().digits(12) + "-" + faker.lorem().characters(20) + ".apps.googleusercontent.com";
	}

	public String getClientSecret() {
		return "GOCSPX" + "-_-" + faker.lorem().characters(20);
	}

	public int getIsGoogleIdp() {
		return faker.number().numberBetween(0, 1);
	}

}
