package io.rcrm.api.pojo.premiumJobBoard;

public class TakeCampaignOffline {

	public TakeCampaignOffline() {
	}

	private String type;
	private int campaign_id;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCampaign_id() {
		return campaign_id;
	}

	public void setCampaign_id(int campaign_id) {
		this.campaign_id = campaign_id;
	}
}