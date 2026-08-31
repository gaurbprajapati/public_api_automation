package io.rcrm.api.pojo.albatross.dashboard;

import java.util.List;

public class HiringStage {
    private List<HiringStageCandidate> hiring_stage_list;
    private int ownerid;
    private String search_text = "";
    private String job_status_filter_ids ="";

    // Getters and Setters
    public List<HiringStageCandidate> getHiring_stage_list() {
        return hiring_stage_list;
    }

    public void setHiring_stage_list(List<HiringStageCandidate> hiring_stage_list) {
        this.hiring_stage_list = hiring_stage_list;
    }

    public int getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(int ownerid) {
        this.ownerid = ownerid;
    }

    public String getSearch_text() {
        return search_text;
    }

    public void setSearch_text(String search_text) {
        this.search_text = search_text;
    }

    public String getJob_status_filter_ids() {
        return job_status_filter_ids;
    }

    public void setJob_status_filter_ids(String job_status_filter_ids) {
        this.job_status_filter_ids = job_status_filter_ids;
    }

    // Inner class for HiringStage
    public static class HiringStageCandidate {
        private int id;
        private String label;
        private String pagemode;
        private int accountid;
        private int sequenceno;
        private int sharedwithclient;

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getPagemode() {
            return pagemode;
        }

        public void setPagemode(String pagemode) {
            this.pagemode = pagemode;
        }

        public int getAccountid() {
            return accountid;
        }

        public void setAccountid(int accountid) {
            this.accountid = accountid;
        }

        public int getSequenceno() {
            return sequenceno;
        }

        public void setSequenceno(int sequenceno) {
            this.sequenceno = sequenceno;
        }

        public int getSharedwithclient() {
            return sharedwithclient;
        }

        public void setSharedwithclient(int sharedwithclient) {
            this.sharedwithclient = sharedwithclient;
        }
    }
}