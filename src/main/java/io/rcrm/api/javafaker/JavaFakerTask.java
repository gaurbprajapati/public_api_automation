package io.rcrm.api.javafaker;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

public class JavaFakerTask {

		Faker faker = new Faker();
	
		public String getDescription() {
			String description = faker.matz().quote() + "<br><br>" + faker.lorem().paragraph(1);
			return description;
		}
	
		public String getTaskName() {
			String taskName = "Task- " + faker.superhero().name() + "/" + faker.superhero().power();
			return taskName;
		}
	
		public String getPastDate() {
			Date PastDate1 = faker.date().past(30, TimeUnit.DAYS);
			String pastDate = String.valueOf(PastDate1);
			return pastDate;
		}
	
		public String getFutureDate() {
			Date FutureDate1 = faker.date().future(30, TimeUnit.DAYS);
			String FutureDate = String.valueOf(FutureDate1);
			return FutureDate;
		}

}
