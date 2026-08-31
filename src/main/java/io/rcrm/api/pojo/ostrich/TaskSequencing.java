package io.rcrm.api.pojo.ostrich;

import com.fasterxml.jackson.annotation.JsonInclude;

public class TaskSequencing {
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int userfilter;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String userfilterslug;
    private String status;
    private String startdate;
    private String enddate;
    private String sortOrder;
    private int page_size;
    private int page;
    private String sort_by;
    private int searchentity;
    private String searchTerm;

    // Getters and setters
    public int getUserfilter() {
        return userfilter;
    }

    public void setUserfilter(int userfilter) {
        this.userfilter = userfilter;
    }

    public String getUserfilterslug() {
        return userfilterslug;
    }

    public void setUserfilterslug(String userfilterslug) {
        this.userfilterslug = userfilterslug;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getPage_size() {
        return page_size;
    }

    public void setPage_size(int page_size) {
        this.page_size = page_size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getSort_by() {
        return sort_by;
    }

    public void setSort_by(String sort_by) {
        this.sort_by = sort_by;
    }

    public int getSearchentity() {
        return searchentity;
    }

    public void setSearchentity(int searchentity) {
        this.searchentity = searchentity;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

}


