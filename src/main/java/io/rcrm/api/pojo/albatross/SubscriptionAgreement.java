package io.rcrm.api.pojo.albatross;

public class SubscriptionAgreement {

    private String agreementContent;
    private String title;

    public SubscriptionAgreement() {
    }

    // Getter and Setter for agreementContent
    public String getAgreementContent() {
        return agreementContent;
    }

    public void setAgreementContent(String agreementContent) {
        this.agreementContent = agreementContent;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}