package io.rcrm.api.pojo.candidateService;

import java.util.List;

public class UpdatePitchCandidateStage {
    private List<Integer> ids;
    private int statusId;
    private String remark;
    private String stageDate; // Optional field, can be null

    // Constructors
    public UpdatePitchCandidateStage() {

    }

    public UpdatePitchCandidateStage(List<Integer> ids, int statusId, String remark, String stageDate) {
        this.ids = ids;
        this.statusId = statusId;
        this.remark = remark;
        this.stageDate = stageDate;
    }

    // Getters and Setters
    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStageDate() {
        return stageDate;
    }

    public void setStageDate(String stageDate) {
        this.stageDate = stageDate;
    }
}
