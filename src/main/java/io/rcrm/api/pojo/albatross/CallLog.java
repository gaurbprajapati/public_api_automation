package io.rcrm.api.pojo.albatross;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class CallLog {
    private String calltype;
    private String contactnumber;
    private String callfrom;
    private String callto;
    private String callnotes;
    private String subject;
    private int accountid;
    private long startedon;
    private String relatedcandidate;
    private int pin;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int customcalltypeid;

    private String type;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> collaborator_team_ids;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> collaborator_user_ids;

    private String duration;

    public String getCalltype() {
        return calltype;
    }

    public void setCalltype(String calltype) {
        this.calltype = calltype;
    }

    public String getContactnumber() {
        return contactnumber;
    }

    public void setContactnumber(String contactnumber) {
        this.contactnumber = contactnumber;
    }

    public String getCallfrom() {
        return callfrom;
    }

    public void setCallfrom(String callfrom) {
        this.callfrom = callfrom;
    }

    public String getCallto() {
        return callto;
    }

    public void setCallto(String callto) {
        this.callto = callto;
    }

    public String getCallnotes() {
        return callnotes;
    }

    public void setCallnotes(String callnotes) {
        this.callnotes = callnotes;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getAccountid() {
        return accountid;
    }

    public void setAccountid(int accountid) {
        this.accountid = accountid;
    }

    public long getStartedon() {
        return startedon;
    }

    public void setStartedon(long startedon) {
        this.startedon = startedon;
    }

    public String getRelatedcandidate() {
        return relatedcandidate;
    }

    public void setRelatedcandidate(String relatedcandidate) {
        this.relatedcandidate = relatedcandidate;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getCustomcalltypeid() {
        return customcalltypeid;
    }

    public void setCustomcalltypeid(int customcalltypeid) {
        this.customcalltypeid = customcalltypeid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getCollaborator_team_ids() {
        return collaborator_team_ids;
    }

    public void setCollaborator_team_ids(List<Integer> collaborator_team_ids) {
        this.collaborator_team_ids = collaborator_team_ids;
    }

    public List<Integer> getCollaborator_user_ids() {
        return collaborator_user_ids;
    }

    public void setCollaborator_user_ids(List<Integer> collaborator_user_ids) {
        this.collaborator_user_ids = collaborator_user_ids;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
