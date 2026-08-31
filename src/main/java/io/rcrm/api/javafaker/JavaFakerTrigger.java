package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerTrigger {

	Faker faker = new Faker();

	public String getTriggerName() {
		return faker.lorem().word() + "Trigger";
	}

	public int getStageId() {
		return faker.random().nextInt(3, 7);
	}

	public int getTriggerId() {
		return faker.random().nextInt(5, 8);
	}

	public int getValidTrigger() {
		return faker.random().nextInt(3, 5);
	}

	public int getRandomIntValue(int start, int end) {
		return faker.random().nextInt(start, end);
	}
}
