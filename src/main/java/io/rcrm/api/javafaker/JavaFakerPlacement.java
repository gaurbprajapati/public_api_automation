package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerPlacement {
    Faker faker = new Faker();

    public int getRandomID (){
        String randomNumber = "999" + faker.number().digits(6);
        return Integer.parseInt(randomNumber);
    }

    public long getRandomIDWithMoreThan10Digits(){
        String randomNumber = "999" + faker.number().digits(8);
        return Long.parseLong(randomNumber);
    }

    public String getInvalidToken(){
        return faker.lorem().characters(10);
    }

    public int getCurrencyId(){
        return Integer.parseInt(faker.number().digits(2));
    }

    public String getRandomSlug(){
        return "177" + faker.number().digits(15) + faker.lorem().characters(3, false, false);
    }
    
}
