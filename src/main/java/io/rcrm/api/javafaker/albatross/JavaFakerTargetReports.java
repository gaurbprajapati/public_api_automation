package io.rcrm.api.javafaker.albatross;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javafaker.Faker;

public class JavaFakerTargetReports {

	Faker faker = new Faker();

	public String getTargetReportName() {
		return faker.company().name() + " Target Report";
	}

	public String getAssigneeType() {
		List<String> assigneeTypes = List.of("Individuals", "Roles", "Teams","Company Wide");
		int randomIndex = ThreadLocalRandom.current().nextInt(assigneeTypes.size());
		return assigneeTypes.get(randomIndex);
	}

	public String getFrequency() {
		List<String> assigneeTypes = List.of("Daily", "Weekly", "Monthly", "Quarterly", "Yearly");
		int randomIndex = ThreadLocalRandom.current().nextInt(assigneeTypes.size());
		return assigneeTypes.get(randomIndex);
	}

	public long getEndDate(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, days);
		return calendar.getTimeInMillis() / 1000;
	}

	public long getStartDate(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -Math.abs(days));
		return calendar.getTimeInMillis() / 1000;
	}

	public String getKPILabel() {
		List<String> kpiList = List.of("Candidates Added", "Companies Added", "Contacts Added", "Jobs Added");
		int randomIndex = ThreadLocalRandom.current().nextInt(kpiList.size());
		return kpiList.get(randomIndex);
	}

	public String getKPIValue(String value) {
		switch (value) {
		case "Candidates Added":
			return "cadded";
		case "Companies Added":
			return "company";
		case "Contacts Added":
			return "contact";
		case "Jobs Added":
			return "job";
		default:
			return null;
		}
	}

	public int getRandomTargetId() {
		String id = "999" + faker.number().digits(3);
		return Integer.parseInt(id);
	}
	
	public String getAssigneeId() {
		return faker.number().digits(3);
	}

	public String getKPICount() {
		return faker.number().digits(2);
	}

	public int getRefreshTime() {
		return ThreadLocalRandom.current().nextBoolean() ? 30 : 60;
	}

	public String getSortBy() {
		return ThreadLocalRandom.current().nextBoolean() ? "created_on" : "updated_on";
	}

	public String getSortOrder() {
		return ThreadLocalRandom.current().nextBoolean() ? "asc" : "desc";
	}
	
	public int getPageSize() {
		return faker.number().randomDigit();
	}
	
	public String getPageNumber() {
		return faker.number().digits(2);
	}
	
	public String getNotificationStatus() {
		return ThreadLocalRandom.current().nextBoolean() ? "0" : "1";
	}

}