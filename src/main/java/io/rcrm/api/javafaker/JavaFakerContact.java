package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

import java.util.Random;

public class JavaFakerContact {

	Faker faker = new Faker();

	public String getFirstName() {
		return faker.name().firstName() + " Contact";
	}

	public String getLastName() {
		return faker.name().lastName();
	}

	public String getEmailID() {
		return faker.name().firstName() + "@yopmail.com";
	}

	public String getContactNumber() {
		return faker.phoneNumber().cellPhone();
	}

	public String getUrl() {
		// Generating the fb,Twitter,Github,Linkedin,Xing URL's
		return "https://" + faker.internet().url();
	}

	public String getAvatarUrl() {
		int index = faker.number().numberBetween(1, 5);
		return "https://files-for-testing.s3-ap-southeast-1.amazonaws.com/avatar_"+index+".jpg";
	}

	public String getCity() {
		return faker.address().city();
	}

	public String getAddress() {
		return faker.address().fullAddress();
	}

	public String getLocality() {
		return faker.address().streetName();
	}

	public String getDesignation() {
		return faker.job().position();
	}

	public String getInvalidContactSlug() {
		return faker.lorem().characters(15);
	}

	public String getContactLastName() {
		return faker.lorem().characters(1);
	}

	public String getMappingTemplate() {
		return "Company Mapping Template" + faker.number().digits(3);
	}

	public String getStage() {
		String[] stages = {"Lead", "Follow-up", "Stage"};
		Random random = new Random();
		return stages[random.nextInt(stages.length)];
	}

	public String getContactFacebookURL(){
		return "https://www.facebook.com/" + getFirstName();
	}

	public String getContactTwitterURL(){
		return "https://www.twitter.com/" + getFirstName();
	}

	public String getContactLinkedinURL(){
		return "https://www.linkedin.com/" + getFirstName();
	}

	public String getContactXingURL(){
		return "https://www.xing.com/" + getFirstName();
	}

	public String getContactCustomField(String fieldType){
		return "Contact Custom " + faker.number().numberBetween(0,9999) + fieldType;
	}

	public String getStageUpdateReason() {
		return "Stage Update Reason : " + faker.number().digits(3);
	}

	public String getContactContent(String companyName, String jobTitle) {
		return String.join(" ",
				getFirstName() + " " + getLastName(),
				getFirstName(),
				getLastName(),
				getContactNumber(),
				companyName,
				getCity(),
				getLocality(),
				jobTitle,
				getEmailID(),
				getStage(),
				getAddress(),
				getContactFacebookURL(),
				getContactTwitterURL(),
				getContactLinkedinURL(),
				getContactXingURL(),
				getContactCustomField("Text"),
				getContactCustomField("Long Text"),
				getContactCustomField("Date"),
				getContactCustomField("Number"),
				getContactCustomField("Checkbox"),
				getContactCustomField("Dropdown"),
				getContactCustomField("Multiselect"),
				getContactCustomField("Phone Number"),
				getContactCustomField("Email Address"),
				getContactCustomField("File")
		);
	}
}
