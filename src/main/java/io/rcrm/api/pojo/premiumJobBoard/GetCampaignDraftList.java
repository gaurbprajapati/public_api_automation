package io.rcrm.api.pojo.premiumJobBoard;

public class GetCampaignDraftList {

	public GetCampaignDraftList() {
	}

	private String sort_by;
	private String sort_order;

	public String getSort_order() {
		return sort_order;
	}

	public void setSort_order(String sort_order) {
		this.sort_order = sort_order;
	}

	public String getSort_by() {
		return sort_by;
	}

	public void setSort_by(String sort_by) {
		this.sort_by = sort_by;
	}
}