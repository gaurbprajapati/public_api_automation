package io.rcrm.api.javafaker;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

public class JavaFakerDeal {

	public JavaFakerDeal() {
	}

	Faker faker = new Faker();

	public int getDealValue() {
		return faker.number().numberBetween(500000, 1000000);
	}

	public String getDealName() {
		return faker.job().title() + " Deal";
	}

	public String getNumber() {
		int openings = faker.number().numberBetween(1, 3);
		return String.valueOf(openings);
	}

	public String getMaxNumber() {
		return faker.number().digits(20);
	}

	public String getDealDate() {
		Date futureDate = faker.date().future(10, TimeUnit.DAYS);
		return String.valueOf(futureDate);
	}

	public String getStageUpdateReason() {
		return "Stage Update Reason : " + faker.number().digits(3);
	}

	public String getRandomDecimalValue(int decimals) {
		double randomDecimalValue = faker.number().randomDouble(decimals, 1, 100);
	    return String.format("%." + decimals + "f", randomDecimalValue);
	}
	
	public String getDateCustomFieldValue() {
	    Date date = faker.date().future(30, TimeUnit.DAYS);  // Random date within next 30 days
	    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
	}
}