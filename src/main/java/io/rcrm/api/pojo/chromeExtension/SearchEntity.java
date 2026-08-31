package io.rcrm.api.pojo.chromeExtension;

public class SearchEntity {
    private String search;
    private boolean candidates;
    private String contacts;
    private String compnaies;
    private boolean jobs;
    private boolean users;
    private String extension_version;

    // Constructor
    public SearchEntity(String search, boolean candidates, String contacts, String compnaies, boolean jobs, boolean users, String extension_version) {
        this.search = search;
        this.candidates = candidates;
        this.contacts = contacts;
        this.compnaies = compnaies;
        this.jobs = jobs;
        this.users = users;
        this.extension_version = extension_version;
    }

    // Getters and Setters
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

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getCompnaies() {
        return compnaies;
    }

    public void setCompnaies(String compnaies) {
        this.compnaies = compnaies;
    }

    public boolean isJobs() {
        return jobs;
    }

    public void setJobs(boolean jobs) {
        this.jobs = jobs;
    }

    public boolean isUsers() {
        return users;
    }

    public void setUsers(boolean users) {
        this.users = users;
    }

    public String getExtension_version() {
        return extension_version;
    }

    public void setExtension_version(String extension_version) {
        this.extension_version = extension_version;
    }
}