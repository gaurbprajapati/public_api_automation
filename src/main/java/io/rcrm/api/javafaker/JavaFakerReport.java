package io.rcrm.api.javafaker;

import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.List;
import com.github.javafaker.Faker;

public class JavaFakerReport {
	static Faker faker = new Faker();

	public String getLabel() {
		return faker.lorem().characters(10);
	}

	public String getRandomWord() {
		return faker.lorem().word();
	}

	public String getRandomDigits(int count) {
		return faker.number().digits(count);
	}

	public String getPastDate(int days) {
		return String.valueOf(faker.date().past(days, TimeUnit.DAYS).getTime());
	}

	public String getFutureDate(int days) {
		return String.valueOf(faker.date().future(10, TimeUnit.DAYS).getTime());
	}

	public int getRandomDigit() {
		return faker.number().randomDigit();
	}

	public String getRandomCharacters(int count) {
		return faker.lorem().characters(count);
	}

	public int getFromDate() {
		long millis = faker.date().past(10, TimeUnit.DAYS).getTime();
		return (int) (millis / 1000);
	}

	public int getToDate() {
		long millis = faker.date().future(10, TimeUnit.DAYS).getTime();
		return (int) (millis / 1000);
	}

	public static List<Integer> getRecruiterIds() {
		int recruiterId = faker.number().numberBetween(1000000, 9999999);
		return Collections.singletonList(recruiterId);
	}

	public static String getSlug() {
		return faker.number().digits(20) + faker.lorem().characters(3);
	}

}
