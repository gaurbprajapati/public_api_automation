package io.rcrm.api.javafaker.albatross;

import java.time.Year;
import java.util.UUID;
import com.github.javafaker.Faker;

public class JavaFakerAccountAgreement {

	Faker faker = new Faker();

	public int getRandomLimit() {
		return faker.number().numberBetween(1, 10);
	}

	public String getSubscriptionAgreementTitle() {
		return "Subscription Agreement (" + Year.now().getValue() + ")";
	}

	public String getSubscriptionAgreementTemplateContext() {
		return "agreement/agreement_template/rcrm_subscrition_agreement_" + Year.now().getValue();
	}

	public String getAgreementContent() {
		return "data:application/pdf;base64," + UUID.randomUUID().toString();
	}

}