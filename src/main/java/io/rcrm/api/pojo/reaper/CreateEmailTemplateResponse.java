package io.rcrm.api.pojo.reaper;

public class CreateEmailTemplateResponse {
    
    private String responseMessage;
    private Integer templateId;
    private String status;

    public CreateEmailTemplateResponse() {
    }

    public CreateEmailTemplateResponse(String responseMessage, Integer templateId, String status) {
        this.responseMessage = responseMessage;
        this.templateId = templateId;
        this.status = status;
    }

    // Getters and Setters
    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    @Override
    public String toString() {
        return "CreateEmailTemplateResponse{" +
                "responseMessage='" + responseMessage + '\'' +
                ", templateId=" + templateId +
                ", status='" + status + '\'' +
                '}';
    }
}
