package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerXmlFeed {

    Faker faker = new Faker();

    final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <source>\n" +
            "{xml_body}\n" +
            "</source>";

    final String XML_BODY = "<job>\n" +
            "<title><![CDATA[{job_title}]]></title>\n" +
            "<company><![CDATA[{job_company_name}]]></company>\n" +
            "</job>";

    public String getXmlFeedTitle() {
        return "XML Feed "+faker.number().digits(3);
    }

    public String getXmlHeader() {
        return XML_HEADER;
    }

    public String getXmlBody() {
        return XML_BODY;
    }

    public int getDecodeValue() {
        return faker.number().numberBetween(0, 1);
    }

    public int getPreselectValue() {
        return faker.number().numberBetween(0, 1);
    }

    public int getJobLastUpdatedOnLimit() {
        return faker.number().numberBetween(1, 10);
    }

}
