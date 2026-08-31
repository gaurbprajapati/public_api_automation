package io.rcrm.api.javafaker.neptune;

import com.github.javafaker.Faker;

public class JavaFakerSummary {

	Faker faker = new Faker();

	public String getNoteText() {
		return faker.lorem().paragraph();
	}

	public int getCandidateId() {
		return faker.number().numberBetween(1000000, 2000000);
	}

	public int getContactId() {
		return faker.number().numberBetween(100000, 200000);
	}

	public String getPromptText() {
		return faker.lorem().paragraph().substring(0, 30);
	}

	public String getRandomKey() {
		return faker.name().name();
	}

	public String getRandomString(int i) {
		return faker.lorem().characters(i);
	}

}
