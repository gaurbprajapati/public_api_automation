package io.rcrm.api.pojo.auditLog;

public class AuditLogList {
	private String orderBy;
	private String order;
	private String actionType;
	private int dateFrom;
	private int dateTo;
	private String[] performedBy;
	private String[] companySlugs;
	private String[] contactSlugs;
	private String[] jobSlugs;
	private String[] dealSlugs;
	private String[] candidateSlugs;
	private String[] otherSlugs;
	private int onLoadData;

	// constructor
	public AuditLogList() {
   // TODO document why this constructor is empty
 }

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public int getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(int dateFrom) {
		this.dateFrom = dateFrom;
	}

	public int getDateTo() {
		return dateTo;
	}

	public void setDateTo(int dateTo) {
		this.dateTo = dateTo;
	}

	public String[] getPerformedBy() {
		return performedBy;
	}

	public void setPerformedBy(String[] performedBy) {
		this.performedBy = performedBy;
	}

	public String[] getCompanySlugs() {
		return companySlugs;
	}

	public void setCompanySlugs(String[] companySlugs) {
		this.companySlugs = companySlugs;
	}

	public String[] getContactSlugs() {
		return contactSlugs;
	}

	public void setContactSlugs(String[] contactSlugs) {
		this.contactSlugs = contactSlugs;
	}

	public String[] getJobSlugs() {
		return jobSlugs;
	}

	public void setJobSlugs(String[] jobSlugs) {
		this.jobSlugs = jobSlugs;
	}

	public String[] getDealSlugs() {
		return dealSlugs;
	}

	public void setDealSlugs(String[] dealSlugs) {
		this.dealSlugs = dealSlugs;
	}

	public String[] getCandidateSlugs() {
		return candidateSlugs;
	}

	public void setCandidateSlugs(String[] candidateSlugs) {
		this.candidateSlugs = candidateSlugs;
	}

	public String[] getOtherSlugs() {
		return otherSlugs;
	}

	public void setOtherSlugs(String[] otherSlugs) {
		this.otherSlugs = otherSlugs;
	}

	public int getOnLoadData() {
		return onLoadData;
	}

	public void setOnLoadData(int onLoadData) {
		this.onLoadData = onLoadData;
	}

}
