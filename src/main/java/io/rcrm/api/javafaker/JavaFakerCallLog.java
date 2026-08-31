package io.rcrm.api.javafaker;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

public class JavaFakerCallLog {

	public JavaFakerCallLog() {
		// TODO Auto-generated constructor stub
	}

	Faker faker = new Faker();

	public String getMeetingName() {
		return "Meeting with - " + faker.superhero().name() + "/" + faker.superhero().power();
	}

	public String getCall_notes() {
		return "Call log Description - " + faker.matz().quote() + "<br><br>" + faker.lorem().paragraph(1);
	}

	public String getPastDate() {
		Date PastDate1 = faker.date().past(30, TimeUnit.DAYS);
		String pastDate = String.valueOf(PastDate1);
		return pastDate;
	}
	public String notesText() {
		// Generating the Job Description Text
		String notesText = faker.lorem().paragraph(10000);
		return notesText;
	}
	public String getFutureDate() {
		Date FutureDate1 = faker.date().future(10, TimeUnit.DAYS);
		String FutureDate = String.valueOf(FutureDate1);
		return FutureDate;
	}

	public String getEndDate() {
		Date endDate1 = faker.date().future(30, TimeUnit.DAYS);
		String endDate = String.valueOf(endDate1);
		return endDate;
	}
	
	public String getContactNumber() {
		//Generating password
		String phoneNumber = faker.phoneNumber().cellPhone();	
		return phoneNumber;
	}
}
