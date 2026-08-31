package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.Random;

public class JavaFakerMails {

	private Locale locale = new Locale("en", "US");
	private Faker faker = new Faker(locale);
	private Random random = new Random();

	// Larger set of recruitment-related words for subject and body generation
	private String[] recruitmentWords = {
			"career", "opportunity", "position", "interview", "application", "candidate",
			"resume", "talent", "growth", "insight", "opening", "role", "skills", "fit",
			"development", "guidance", "potential", "goal", "network", "connect", "vision",
			"advancement", "program", "assessment", "recommendation", "qualification",
			"profile", "experience", "future", "success", "access", "progress", "achievement",
			"background", "evaluation", "expertise", "support", "session", "value", "learn",
			"training", "improvement", "status", "essential", "engagement", "feedback",
			"strengths", "weaknesses", "insights", "recruitment", "hiring", "recruiter",
			"employer", "industry", "job", "matching", "motivation", "aspiration", "objective",
			"networking", "mentorship", "collaboration", "culture", "placement", "qualifications",
			"workshop", "webinar", "conference", "growth", "achievement", "target",
			"candidate", "portfolio", "project", "credentials", "endorsement", "referral",
			"potential", "capability", "learning", "pathway", "reputation", "connections",
			"communication", "resource", "background", "suitability", "update", "status",
			"team", "compensation", "benefits", "offer", "onboarding",
			"transition", "milestone", "probation", "promotion", "success", "initiative",
			"readiness", "commitment", "focus", "inspiration", "leadership",
			"relationship", "contribution", "coaching", "upskilling", "empowerment", "appraisal",
			"alignment", "empowerment", "feedback", "performance", "mentoring",
			"rewards", "appreciation", "ethics", "values", "purpose", "accomplishment",
			"satisfaction", "innovation", "progression", "diversity", "integrity",
			"reliability", "responsibility", "transparency", "learning-opportunity", "collaborative",
			"community", "initiative","placement", "professionalism", "loyalty",
	};

	// Generate a fake email address
	public String getFakeEmail() {
		return getRandomRecruitmentWord()+"@yopmail.com";
	}

	// Generate a recruitment-focused email subject using the larger word set
	public String getFakeEmailSubject() {
		int numberOfWords = random.nextInt(3) + 3; // 3 to 5 words
		StringBuilder emailSubject = new StringBuilder();

		for (int i = 0; i < numberOfWords; i++) {
			if (i > 0) {
				emailSubject.append(" "); // Add space between words
			}
			emailSubject.append(recruitmentWords[random.nextInt(recruitmentWords.length)]);
		}

		// Capitalize the first letter of the first word
		return capitalizeFirstLetter(emailSubject.toString());
	}

	// Helper method to capitalize the first letter of a string
	private String capitalizeFirstLetter(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public String getRandomRecruitmentWord() {
		return recruitmentWords[random.nextInt(recruitmentWords.length)];
	}

	// Generate a fake email body with a specified number of sentences, each ending with a full stop and new line
	public String getFakeEmailBody(int numberOfSentences) {
		StringBuilder emailBody = new StringBuilder();

		for (int i = 0; i < numberOfSentences; i++) {
			emailBody.append(capitalizeFirstLetter(getRandomSentence()));
			emailBody.append(".\n"); // End each sentence with a period and a new line
		}

		return emailBody.toString().trim();
	}

	// Helper to generate a single sentence with random recruitment-related words
	private String getRandomSentence() {
		int numberOfWords = random.nextInt(5) + 5; // 5 to 9 words per sentence
		StringBuilder sentence = new StringBuilder();

		for (int i = 0; i < numberOfWords; i++) {
			if (i > 0) {
				sentence.append(" "); // Add space between words
			}
			sentence.append(getRandomRecruitmentWord());
		}
		return sentence.toString();
	}
}
