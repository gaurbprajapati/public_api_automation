package io.rcrm.api.javafaker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

public class JavaFakerMeeting {

	Faker faker = new Faker();
	Date meetingStartDate = faker.date().future(10, TimeUnit.DAYS);

	public String getMeetingName() {
		return "Meeting with - " + faker.superhero().name() + "/" + faker.superhero().power();
	}

	public String getDescription() {
		return faker.matz().quote() + "<br><br>" + faker.lorem().paragraph(1);
	}
	
	public String getDescription(int size) {
		return faker.matz().quote() + "<br><br>" + faker.lorem().characters(size);
	}

	public String getAddress() {
		return faker.address().fullAddress();
	}

	public String getPastDate() {
		Date PastDate1 = faker.date().past(30, TimeUnit.DAYS);
		String pastDate = String.valueOf(PastDate1);
		return pastDate;
	}

	public String getFutureDate() {
		Date FutureDate1 = faker.date().future(10, TimeUnit.DAYS);
		String FutureDate = String.valueOf(FutureDate1);
		
		return FutureDate;
	}

	public String getDelayedFutureDate(int delay, TimeUnit unit) {
		Date now = new Date();
		Date minimumDate = new Date(now.getTime() + unit.toMillis(delay));
		Date FutureDate1 = faker.date().future(10, TimeUnit.DAYS, minimumDate);
		String FutureDate = String.valueOf(FutureDate1);
		return FutureDate;
	}

	public String getEndDate() {
		Date startDate = faker.date().future(10, TimeUnit.DAYS);
		Date endDate1 = faker.date().future(15, TimeUnit.DAYS, startDate);
		String endDate = String.valueOf(endDate1);
	
		
		
		return endDate;
	}
	
	public String getReminderData() {
		String[] timeIntervals = { "0", "900", "1800", "3600", "86400" };
		return faker.options().option(timeIntervals);
	}

	public long getMeetingStartDate() {
		long startTimestamp = meetingStartDate.getTime() / 1000;
		return startTimestamp;
	}

	public long getMeetingEndDate() {
		Date endDate1 = faker.date().future(15, TimeUnit.DAYS, meetingStartDate);
		long endTimestamp = endDate1.getTime() / 1000;
		return endTimestamp;
	}

	public String getEndDateWithReferenceDate(String endDate) {
		
		
		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
		Date startDate = null;
		try {
			startDate = formatter.parse(endDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		
		Date endDate1 = faker.date().future(15, TimeUnit.DAYS, startDate);
		String endDateWithReferenceDate = String.valueOf(endDate1);
		
		return endDateWithReferenceDate;
	}
}
