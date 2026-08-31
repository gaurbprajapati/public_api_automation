package io.rcrm.api.pojo.candidateService;

import java.util.List;

public class PitchCandidateToContactWithoutEmail {
    private List<Integer> candidateIds;
    private List<Integer> contactIds;

    public PitchCandidateToContactWithoutEmail() {
        super();
    }

    public PitchCandidateToContactWithoutEmail(List<Integer> candidateIds, List<Integer> contactIds) {
        super();
        this.candidateIds = candidateIds;
        this.contactIds = contactIds;
    }

    public List<Integer> getCandidateIds() {
        return candidateIds;
    }

    public void setCandidateIds(List<Integer> candidateIds) {
        this.candidateIds = candidateIds;
    }

    public List<Integer> getContactIds() {
        return contactIds;
    }

    public void setContactIds(List<Integer> contactIds) {
        this.contactIds = contactIds;
    }
}