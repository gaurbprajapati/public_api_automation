package io.rcrm.api.javafaker.albatross.notification;

import com.github.javafaker.Faker;

public class JavaFakerNotification {

	Faker faker = new Faker();

	public int getRandomValidLimit() {
		return faker.number().numberBetween(1, 30);
	}

	public int getRandomInvalidLimit() {
		return faker.number().numberBetween(31, 100);
	}

	public boolean getRandomBooleanValue() {
		return faker.random().nextBoolean();
	}

	public String getRandomString() {
		return faker.lorem().characters(10).toString();
	}

	public String getRandomNumericValue() {
		return String.valueOf(faker.number().numberBetween(100, 200));
	}

}
