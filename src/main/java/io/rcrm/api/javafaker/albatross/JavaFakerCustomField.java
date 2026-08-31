package io.rcrm.api.javafaker.albatross;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javafaker.Faker;

public class JavaFakerCustomField {

	Faker faker = new Faker();

	public int getColumnId() {
		return faker.number().randomDigit();
	}

	public String getCustomFieldName(String entity) {
		return entity + faker.company().name();
	}

	public String getDefaultvalue(String fieldType) {
		if (fieldType.equals("dropdown") || fieldType.equals("multiselect"))
			return faker.food().fruit() + "," + faker.food().fruit() + "," + faker.food().fruit() + ","
					+ faker.food().fruit();
		else
			return null;
	}

	public int getRandomEntityId() {
		return faker.number().randomDigit();
	}

	public String getRandomCustomFieldName() {
		return faker.company().name();
	}

	public int getEntityId() {
		List<Integer> columnIds = List.of(2, 3, 4, 5, 11, 14);
		int randomIndex = ThreadLocalRandom.current().nextInt(columnIds.size());
		return columnIds.get(randomIndex);
	}

	public String getEntityName(int id) {
		switch (id) {
		case 5:
			return "candidates";
		case 3:
			return "companies";
		case 2:
			return "contacts";
		case 4:
			return "jobs";
		case 11:
			return "deals";
		case 14:
			return "job-associated";
		default:
			return null;
		}
	}

	public String getFieldType() {
		return ThreadLocalRandom.current().nextBoolean() ? "dropdown" : "multiselect";
	}

	public String getTempId() {
		return UUID.randomUUID().toString();
	}

	public String getNumberOfDefaultOptionsValues(int n) {
		StringBuilder fruits = new StringBuilder();
		for (int i = 0; i < n; i++) {
			fruits.append(faker.food().fruit() + " " + faker.number().digits(3));
			if (i < n - 1) {
				fruits.append(", ");
			}
		}
		return fruits.toString();

	}
	
	public int getValidEntityId() {
		List<Integer> columnIds = List.of(2, 3, 4, 5, 11);
		int randomIndex = ThreadLocalRandom.current().nextInt(columnIds.size());
		return columnIds.get(randomIndex);
	}

	public String getRandomOptionsValue() {
		return faker.company().name() + faker.number().digits(3);
	}

	public int getCustomFieldId(String type) {
		int id = 0;
		switch (type.toLowerCase()) {
		case "text":
			id = 1;
			break;
		case "longtext":
			id = 2;
			break;
		case "date":
			id = 3;
			break;
		case "number":
			id = 4;
			break;
		case "dropdown":
			id = 5;
			break;
		case "multiselect":
			id = 6;
			break;
		case "candidate":
			id = 7;
			break;
		case "company":
			id = 8;
			break;
		case "contact":
			id = 9;
			break;
		case "job":
			id = 10;
			break;
		case "deals":
			id = 11;
			break;
		case "user":
			id = 12;
			break;
		case "team":
			id = 13;
			break;
		}
		return id;
	}

	public String getRandomCustomTextValue() {
		return faker.lorem().sentence();
	}

	public String getRandomCustomFieldType() {
		List<String> fieldTypes = List.of("text", "longtext", "date", "number", "checkbox", "phonenumber", "email", "file", "date_time", "datetime", "dropdown", "multiselect");
		int randomIndex = ThreadLocalRandom.current().nextInt(fieldTypes.size());
		return fieldTypes.get(randomIndex);
	}

	public String getRandomEntityTypeCustomFieldType() {
		List<String> fieldTypes = List.of("candidate", "company", "contact", "job", "deal", "user", "team");
		int randomIndex = ThreadLocalRandom.current().nextInt(fieldTypes.size());
		return fieldTypes.get(randomIndex);
	}

}