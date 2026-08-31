package io.rcrm.api.pojo.albatross;

import java.util.List;

public class GetCallLogsList {

    private String sortBy;
    private String sortOrder;
    private int page;
    private int page_size;
    private String filter;
    private String relatedToCandidate;
    private String relatedToCompany;
    private List<Integer> teamFilter;
    private List<Integer> createdBy;
    private List<Integer> customCallTypeId;
    private List<Integer> callOutcomeId;
    private String fromNumber;
    private String toNumber;

    // Default Constructor
    public GetCallLogsList() {
        super();
        // TODO Auto-generated constructor stub
    }

    // Getters and Setters
    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage_size() {
        return page_size;
    }

    public void setPage_size(int pageSize) {
        this.page_size = pageSize;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getRelatedToCandidate() {
        return relatedToCandidate;
    }

    public void setRelatedToCandidate(String relatedToCandidate) {
        this.relatedToCandidate = relatedToCandidate;
    }

    public String getRelatedToCompany() {
        return relatedToCompany;
    }

    public void setRelatedToCompany(String relatedToCompany) {
        this.relatedToCompany = relatedToCompany;
    }

    public List<Integer> getTeamFilter() {
        return teamFilter;
    }

    public void setTeamFilter(List<Integer> teamFilter) {
        this.teamFilter = teamFilter;
    }

    public List<Integer> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(List<Integer> createdBy) {
        this.createdBy = createdBy;
    }

    public List<Integer> getCustomCallTypeId() {
        return customCallTypeId;
    }

    public void setCustomCallTypeId(List<Integer> customCallTypeId) {
        this.customCallTypeId = customCallTypeId;
    }

    public List<Integer> getCallOutcomeId() {
        return callOutcomeId;
    }

    public void setCallOutcomeId(List<Integer> callOutcomeId) {
        this.callOutcomeId = callOutcomeId;
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }

    public String getToNumber() {
        return toNumber;
    }

    public void setToNumber(String toNumber) {
        this.toNumber = toNumber;
    }
}
