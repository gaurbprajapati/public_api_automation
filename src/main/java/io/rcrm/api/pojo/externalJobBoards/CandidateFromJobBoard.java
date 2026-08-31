package io.rcrm.api.pojo.externalJobBoards;

public class CandidateFromJobBoard {

    private String job_reference_id;
    private Candidate candidate;

    // Getters and Setters
    public String getJob_reference_id() {
        return job_reference_id;
    }

    public void setJob_reference_id(String job_reference_id) {
        this.job_reference_id = job_reference_id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    // Inner class representing the Candidate object
    public class Candidate {
        private String first_name;
        private String last_name;
        private String email_id;
        private String contact_number;
        private Integer work_experience_year;
        private String resume;
        private String current_organization_name;
        private String address;
        private String city;
        private String state;
        private String country;
        private String profile_facebook;
        private String profile_twitter;
        private String profile_linkedin;
        private String profile_github;
        private String profile_xing;
        private Integer notice_period_days;
        private String candidate_dob;

        // Getters and Setters
        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getEmail_id() {
            return email_id;
        }

        public void setEmail_id(String email_id) {
            this.email_id = email_id;
        }

        public String getContact_number() {
            return contact_number;
        }

        public void setContact_number(String contact_number) {
            this.contact_number = contact_number;
        }

        public Integer getWork_experience_year() {
            return work_experience_year;
        }

        public void setWork_experience_year(Integer work_experience_year) {
            this.work_experience_year = work_experience_year;
        }

        public String getResume() {
            return resume;
        }

        public void setResume(String resume) {
            this.resume = resume;
        }

        public String getCurrent_organization_name() {
            return current_organization_name;
        }

        public void setCurrent_organization_name(String current_organization_name) {
            this.current_organization_name = current_organization_name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProfile_facebook() {
            return profile_facebook;
        }

        public void setProfile_facebook(String profile_facebook) {
            this.profile_facebook = profile_facebook;
        }

        public String getProfile_twitter() {
            return profile_twitter;
        }

        public void setProfile_twitter(String profile_twitter) {
            this.profile_twitter = profile_twitter;
        }

        public String getProfile_linkedin() {
            return profile_linkedin;
        }

        public void setProfile_linkedin(String profile_linkedin) {
            this.profile_linkedin = profile_linkedin;
        }

        public String getProfile_github() {
            return profile_github;
        }

        public void setProfile_github(String profile_github) {
            this.profile_github = profile_github;
        }

        public String getProfile_xing() {
            return profile_xing;
        }

        public void setProfile_xing(String profile_xing) {
            this.profile_xing = profile_xing;
        }

        public Integer getNotice_period_days() {
            return notice_period_days;
        }

        public void setNotice_period_days(Integer notice_period_days) {
            this.notice_period_days = notice_period_days;
        }

        public String getCandidate_dob() {
            return candidate_dob;
        }

        public void setCandidate_dob(String candidate_dob) {
            this.candidate_dob = candidate_dob;
        }
    }

}
