package io.rcrm.api.pojo;

public class GlobalSearchEntity {
    private String search;
    private boolean candidates;
    private boolean contacts;
    private boolean companies;
    private boolean deals;
    private boolean jobs;
    private boolean fileModalRequest;

    public GlobalSearchEntity(String search, boolean candidates, boolean contacts, boolean companies, boolean deals, boolean jobs, boolean fileModalRequest) {
        this.search = search;
        this.candidates = candidates;
        this.contacts = contacts;
        this.companies = companies;
        this.deals = deals;
        this.jobs = jobs;
        this.fileModalRequest = fileModalRequest;

    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public boolean isCandidates() {
        return candidates;
    }

    public void setCandidates(boolean candidates) {
        this.candidates = candidates;
    }

    public boolean isContacts() {
        return contacts;
    }

    public void setContacts(boolean contacts) {
        this.contacts = contacts;
    }

    public boolean isCompanies() {
        return companies;
    }

    public void setCompanies(boolean companies) {
        this.companies = companies;
    }

    public boolean isDeals() {
        return deals;
    }

    public void setDeals(boolean deals) {
        this.deals = deals;
    }

    public boolean isJobs() {
        return jobs;
    }

    public void setJobs(boolean jobs) {
        this.jobs = jobs;
    }

    public boolean isFileModalRequest() {
        return fileModalRequest;
    }

    public void setFileModalRequest(boolean fileModalRequest) {
        this.fileModalRequest = fileModalRequest;
    }
}
