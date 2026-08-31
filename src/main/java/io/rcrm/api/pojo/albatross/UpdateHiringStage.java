package io.rcrm.api.pojo.albatross;

import java.util.ArrayList;

public class UpdateHiringStage {

	private ArrayList<Integer> id;
	private int candidatestatusid;
	private String remark;
	private int stagedate;
	private int jobid;
	private ArrayList<Integer> jobids;
	private ArrayList<Integer> candidateid;
	private boolean isMarkUnavailable;
	private boolean updateUserObj;

	public UpdateHiringStage() {
		super();
	}

	public UpdateHiringStage(ArrayList<Integer> id, int candidatestatusid, String remark, int stagedate, int jobid,
			ArrayList<Integer> jobids, ArrayList<Integer> candidateid, boolean isMarkUnavailable,
			boolean updateUserObj) {
		this.id = id;
		this.candidatestatusid = candidatestatusid;
		this.remark = remark;
		this.stagedate = stagedate;
		this.jobid = jobid;
		this.jobids = jobids;
		this.candidateid = candidateid;
		this.isMarkUnavailable = isMarkUnavailable;
		this.updateUserObj = updateUserObj;
	}

	public ArrayList<Integer> getId() {
		return id;
	}

	public void setId(ArrayList<Integer> id) {
		this.id = id;
	}

	public int getCandidatestatusid() {
		return candidatestatusid;
	}

	public void setCandidatestatusid(int candidatestatusid) {
		this.candidatestatusid = candidatestatusid;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getStagedate() {
		return stagedate;
	}

	public void setStagedate(int stagedate) {
		this.stagedate = stagedate;
	}

	public int getJobid() {
		return jobid;
	}

	public void setJobid(int jobid) {
		this.jobid = jobid;
	}

	public ArrayList<Integer> getJobids() {
		return jobids;
	}

	public void setJobids(ArrayList<Integer> jobids) {
		this.jobids = jobids;
	}

	public ArrayList<Integer> getCandidateid() {
		return candidateid;
	}

	public void setCandidateid(ArrayList<Integer> candidateid) {
		this.candidateid = candidateid;
	}

	public boolean isMarkUnavailable() {
		return isMarkUnavailable;
	}

	public void setMarkUnavailable(boolean isMarkUnavailable) {
		this.isMarkUnavailable = isMarkUnavailable;
	}

	public boolean isUpdateUserObj() {
		return updateUserObj;
	}

	public void setUpdateUserObj(boolean updateUserObj) {
		this.updateUserObj = updateUserObj;
	}

}
