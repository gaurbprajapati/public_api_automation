package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerHotlist {
	
	Faker faker = new Faker();
	
	public String getHotlistName() {
		String hotlistName = faker.superhero().name() + "/" + faker.team().name()+" - Hotlist";
		return hotlistName;
	}

}
