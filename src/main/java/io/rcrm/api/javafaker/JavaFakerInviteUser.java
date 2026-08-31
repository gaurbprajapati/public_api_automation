package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;
import java.util.concurrent.ThreadLocalRandom;

public class JavaFakerInviteUser {
	static Faker faker = new Faker();
	private static final String[] ROLES = { "Admin", "Team Member", "Restricted Team Member" };

	public String getEmail() {
		return faker.name().firstName() + "@yopmail.com";
	}

	public int getRoleId() {
		return faker.number().numberBetween(2, 5);
	}

	public String getRole() {
		int randomIndex = ThreadLocalRandom.current().nextInt(ROLES.length);
		return ROLES[randomIndex];
	}

	public Integer getRandomDigit() {
		return Integer.valueOf(faker.number().digits(5));
	}

	public long getCurrentDayTime() {
		return faker.date().past(365 * 5, java.util.concurrent.TimeUnit.DAYS).getTime();
	}

	public String getTeamName() {
		return faker.team().name();
	}

	public String getPageSize() {
		return String.valueOf(faker.number().numberBetween(1, 5));
	}

	public String getUserIds() {
		return faker.number().digits(6) + "," + faker.number().digits(6) + "," + faker.number().digits(6);
	}

}
