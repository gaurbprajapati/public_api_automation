package io.rcrm.api.javafaker;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

public class JavaFakerCompany {

	Faker faker = new Faker();

	public String getCompanyName() {
		return faker.company().name() + " Company";
	}

	public String getCompanyWebsite() {
		return "https://" + faker.company().url();
	}

	public String getCompanyAbout() {
		return "This is about company " + getCompanyName();
	}

	public String getContactNumber() {
		return faker.phoneNumber().phoneNumber();
	}

	public String getCity() {
		return faker.address().city();
	}

	public String getAddress() {
		return faker.address().fullAddress();
	}

	// In db there are 180 records but in webapp we fetched only 152
	public int getIndustry_id() {
		return faker.number().numberBetween(1, 150);
	}

	public String getCustomIndustry() {
		return "Custom Industry " + faker.number().digits(3);
	}

	public String getRandomId() {
		return "000" + faker.number().digits(3);
	}

	public String getLogoURL() {
		return faker.company().logo();
	}

	public String getUrl() {
		// Generating the fb,Twitter,Github,Linkedin URL's
		String socialurl = "https://" + faker.internet().url();
		return socialurl;
	}

	public String getInvalidCompanySlug() {
		return faker.lorem().characters(15);
	}

	public String getMappingTemplate() {
		return "Company Mapping Template" + faker.number().digits(3);
	}

	public String getCompanyFacebookURL() {
		return "https://www.facebook.com/" + getCompanyName();
	}

	public String getCompanyTwitterURL() {
		return "https://www.twitter.com/" + getCompanyName();
	}

	public String getCompanyLinkedinURL() {
		return "https://www.linkedin.com/company/" + getCompanyName();
	}

	public String getCompanyXingURL() {
		return "https://www.xing.com/company/" + getCompanyName();
	}

	public String getCompanyCustomField(String fieldType) {
		return "Contact Custom " + faker.number().numberBetween(0, 9999) + fieldType;
	}
	
	public int getInvalidCompanyId() {
		return Integer.parseInt(faker.number().digits(5));
	}
	
	public int getInvalidIndustryId() {
		return faker.number().numberBetween(9999999, 99999999);
	}

	public String getCompanyContent() {
		return String.join(" ", getCompanyName(), getCompanyAbout(), String.valueOf(getIndustry_id()),
				getCompanyWebsite(), getCompanyWebsite(), getAddress(), getCompanyFacebookURL(), getCompanyTwitterURL(),
				getCompanyLinkedinURL(), getCompanyXingURL(), getCompanyCustomField("Text"),
				getCompanyCustomField("Long Text"), getCompanyCustomField("Date"),
				String.valueOf(getCompanyCustomField("Number")), getCompanyCustomField("Checkbox"),
				getCompanyCustomField("Dropdown"), getCompanyCustomField("Multiselect"),
				getCompanyCustomField("Phone Number"), getCompanyCustomField("Email Address"),
				getCompanyCustomField("File"));
	}
	
	public String getDateTimeCustomFieldValue() {
	    Date futureDate = faker.date().future(10, TimeUnit.DAYS);
	    ZonedDateTime utcDateTime = futureDate.toInstant().atZone(ZoneOffset.UTC);
	    DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	    return isoFormatter.format(utcDateTime);
	}
	
	public String getRandomReason() {
		return faker.lorem().sentence(3, 5);
	}

	public String getCustomIndustryLabel() {
		return "Custom Industry " + faker.number().digits(3);
	}

	public String getOfflimitStatusLabel() {
		return "OffLimit Custom Status " + faker.number().digits(3);
	}

}