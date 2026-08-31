package io.rcrm.api.pojo.emails;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendEmail {
    private String from;
    private ReceiverEmailsPage to;
    private List<ReceiverEmailsPage> cc;
    private List<ReceiverEmailsPage> bcc;
    private String body;
    private String subject;
    private Boolean include_signature;
    private Boolean include_opt_out_link;
    private String draft_id;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ReceiverEmailsPage getTo() {
        return to;
    }

    public void setTo(ReceiverEmailsPage to) {
        this.to = to;
    }

    public List<ReceiverEmailsPage> getCc() {
        return cc;
    }

    public void setCc(List<ReceiverEmailsPage> cc) {
        this.cc = cc;
    }

    public List<ReceiverEmailsPage> getBcc() {
        return bcc;
    }

    public void setBcc(List<ReceiverEmailsPage> bcc) {
        this.bcc = bcc;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Boolean getInclude_signature() {
        return include_signature;
    }

    public void setInclude_signature(Boolean include_signature) {
        this.include_signature = include_signature;
    }

    public Boolean getInclude_opt_out_link() {
        return include_opt_out_link;
    }

    public void setInclude_opt_out_link(Boolean include_opt_out_link) {
        this.include_opt_out_link = include_opt_out_link;
    }

    public String getDraft_id() {
        return draft_id;
    }

    public void setDraft_id(String draft_id) {
        this.draft_id = draft_id;
    }
}
