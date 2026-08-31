package io.rcrm.api.pojo.ostrich;

public class GetMeetingInCalendar {

        private String user_ids;
        private long startDate;
        private long endDate;
        private String searchTerm;

        // Getters and Setters
        public String getUser_ids() {
            return user_ids;
        }

        public void setUser_ids(String user_ids) {
            this.user_ids = user_ids;
        }

        public long getStartDate() {
            return startDate;
        }

        public void setStartDate(long startDate) {
            this.startDate = startDate;
        }

        public long getEndDate() {
            return endDate;
        }

        public void setEndDate(long endDate) {
            this.endDate = endDate;
        }

        public String getSearchTerm() {
            return searchTerm;
        }

        public void setSearchTerm(String searchTerm) {
            this.searchTerm = searchTerm;
        }

}
