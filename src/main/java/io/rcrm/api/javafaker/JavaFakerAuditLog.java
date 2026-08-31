package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerAuditLog {
	Faker faker = new Faker();

	public int getValidIntervalType() {
		return faker.number().numberBetween(1, 3);
	}

	public int getInvalidIntervalType() {
		return Integer.parseInt(faker.number().digits(3));
	}

}