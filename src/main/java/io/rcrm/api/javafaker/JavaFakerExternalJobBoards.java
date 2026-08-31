package io.rcrm.api.javafaker;

import org.apache.commons.lang3.RandomStringUtils;

import com.github.javafaker.Faker;

public class JavaFakerExternalJobBoards {
	Faker faker = new Faker();

	public String getEmailAddress() {
		String userName = faker.internet().emailAddress();
		return userName;
	}

	public String getPassword() {
		String password = faker.internet().password();
		return password;
	}

	public String getId() {
		String id = faker.number().digits(3);
		return id;
	}

	public int getJobBoardId() {
		int id = 1;
		return id;
	}

	public int getEnable_logicmelon() {
		int enable_logicmelon = faker.number().numberBetween(0, 1);
		return enable_logicmelon;
	}

	public String getFakerApikey() {
		return RandomStringUtils.randomAlphabetic(10);
	}

	public String getAccountAlias() {
		String accountAlias = faker.name().username();
		return accountAlias;
	}

	public String getClientId() {
		String clientId = faker.number().digits(3);
		return clientId;
	}

	public String getSecret() {
		String secret = faker.internet().password();
		return secret;
	}

	public String getAdcUsername(){
		String adcUsername = faker.name().username();
		return adcUsername;
	}

	public String getCreatedOnTimestampInSecs() {
		return System.currentTimeMillis() / 1000 + "";
	}
}