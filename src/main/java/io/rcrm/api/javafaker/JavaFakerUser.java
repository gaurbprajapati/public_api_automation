package io.rcrm.api.javafaker;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javafaker.Faker;

public class JavaFakerUser {

    Faker faker = new Faker();

    public String getUserFullName() {
        return faker.name().fullName();
    }

    public String getUserFirstName() {
        return faker.name().firstName();
    }

    public String getUserLastName() {
        return faker.name().lastName();
    }

    public String getUserContactNumber() {
        return faker.phoneNumber().cellPhone();
    }

    public String getUserCity() {
        return faker.address().city();
    }

    public String getUserState() {
        return faker.address().state();
    }

    public String getUserCountry() {
        return faker.address().country();
    }

    public String getUserRole() {
        return faker.job().title();
    }

    public String getUserEmail() {
        return faker.name().firstName() + "@yopmail.com";
    }

    public String getUserAccountName() {
        return faker.company().name();
    }
    
    public int getTimezone() {
		return faker.number().numberBetween(1, 100);
	}

	public String getPassword() {
		return faker.internet().password();
	}

	public String getLocale() {
		List<String> locales = List.of("en", "es", "fi", "fr", "it", "ja", "nl", "pt", "de");
		int randomIndex = ThreadLocalRandom.current().nextInt(locales.size());
		return locales.get(randomIndex);
	}
	
	public String getWebsite() {
        return faker.internet().emailAddress();
    }
	
	public String getComments() {
        return faker.lorem().paragraph(5);
    }
	
	public String getRandomEmailId() {
        return faker.name().firstName() + faker.number().digits(5) + "@yopmail.com";
    }

    public String getUserContent(String userId) {
        return String.join(" ",
                getUserFullName(),
                getUserFirstName(),
                getUserLastName(),
                getUserContactNumber(),
                getUserCity(),
                getUserState(),
                getUserCountry(),
                getUserRole(),
                getUserEmail(),
                String.valueOf(userId),
                getUserAccountName()
        );
    }

}
