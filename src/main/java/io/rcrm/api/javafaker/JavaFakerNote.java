package io.rcrm.api.javafaker;

import java.util.Locale;

import com.github.javafaker.Faker;

public class JavaFakerNote {

	Faker faker = new Faker();

	public String getNotes() {
		// Generating the note For Candidate
		String noteForCandidate = faker.matz().quote();
		return noteForCandidate;
	}

	public String getNoteDescriptionText() {
		// Generating the Job Description Text
		String jobDescriptionText = faker.lorem().paragraph(100);
		return jobDescriptionText;
	}

}
