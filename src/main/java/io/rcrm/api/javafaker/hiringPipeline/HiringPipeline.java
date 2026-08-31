package io.rcrm.api.javafaker.hiringPipeline;

import com.github.javafaker.Faker;

public class HiringPipeline {
	
	
	//Locale locale = new Locale("en-IND");
		Faker faker = new Faker();
		
		public String getHiringPipelineName() {
			String reportName = "Custom Hiring Pipeline - " + faker.superhero().name() + "/" + faker.superhero().power() +"/"+faker.phoneNumber().cellPhone() ;
			return reportName;
		}
		
		public String getHiringPipelineNameWithMoreThan150Chars() {
			// Generating the Job Description Text
			String hiringPipelineName = faker.lorem().paragraph(151);
			return hiringPipelineName;
		}

}
