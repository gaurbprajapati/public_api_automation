package io.rcrm.api.javafaker.jobboard;

import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;

public class JavaFakerVonq {

	public JavaFakerVonq() {
		super();
	}

	Faker faker = new Faker();

	public String getJobBoardCompanyName() {
		return faker.company().name();
	}

	public String getCompanyEmail() {
		return faker.internet().emailAddress();
	}

	public String getJobBoardUrl() {
		return faker.company().url();
	}

	public String getJobDescription() {
		return faker.lorem().paragraph();
	}

	public int getGeneratedRandomNumber() {
		return faker.number().numberBetween(100, 999);
	}

	public String getCampaignName() {
		return "Campaign for " + RandomStringUtils.randomAlphabetic(4);
	}

	public String getCampaignId() {
		return "40c89c31-d3e9-5fce-862e-82a07e3e8153";
	}

	public long getStartDate() {
		return System.currentTimeMillis() / 1000; // current time in seconds
	}

	public long getEndDate() {
		return System.currentTimeMillis() / 1000 + 86400; // current time in seconds + 1 day
	}

}
