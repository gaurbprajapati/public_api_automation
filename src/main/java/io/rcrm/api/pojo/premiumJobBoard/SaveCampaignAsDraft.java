package io.rcrm.api.pojo.premiumJobBoard;

public class SaveCampaignAsDraft {

	public SaveCampaignAsDraft() {
	}

	private String campaign_name;
	private int job_id;
	private String campaign_data;
	private String channel_data;

	public String getCampaign_name() {
		return campaign_name;
	}

	public void setCampaign_name(String campaign_name) {
		this.campaign_name = campaign_name;
	}

	public int getJob_id() {
		return job_id;
	}

	public void setJob_id(int job_id) {
		this.job_id = job_id;
	}

	public String getCampaign_data() {
		return campaign_data;
	}

	public void setCampaign_data(String campaign_data) {
		this.campaign_data = campaign_data;
	}

	public String getChannel_data() {
		return channel_data;
	}

	public void setChannel_data(String channel_data) {
		this.channel_data = channel_data;
	}
}